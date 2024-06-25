"""
Market Insights Router - Main endpoint for AI-powered market insights
"""
from fastapi import APIRouter, HTTPException
import logging
from datetime import datetime

from app.models.schemas import (
    MarketInsightRequest,
    MarketInsightResponse,
    Sentiment,
    Recommendation
)
from app.services.stock_service import StockService
from app.services.news_service import NewsService
from app.services.llm_service import LLMService
from app.services.cache_service import CacheService

logger = logging.getLogger(__name__)
router = APIRouter()

# Initialize services
stock_service = StockService()
news_service = NewsService()
llm_service = LLMService()
cache_service = CacheService()


@router.post("/market-insights", response_model=MarketInsightResponse)
async def get_market_insights(request: MarketInsightRequest):
    """
    Get AI-powered market insights for a stock symbol

    This endpoint:
    1. Fetches current stock data from Yahoo Finance
    2. Retrieves latest news articles
    3. Analyzes everything using LLM
    4. Returns comprehensive insights with recommendations
    """
    symbol = request.symbol.upper()

    logger.info(f"Market insights requested for {symbol}")

    # Check cache first
    cache_key = f"insights:{symbol}"
    cached = cache_service.get(cache_key)
    if cached:
        logger.info(f"Returning cached insights for {symbol}")
        return MarketInsightResponse(**cached)

    try:
        # 1. Fetch stock data
        stock_info = stock_service.get_stock_info(symbol)

        # 2. Fetch news articles
        news_articles = []
        if request.include_news:
            news_articles = news_service.get_stock_news(symbol, request.max_news)

        # 3. Generate AI analysis
        analysis = llm_service.analyze_stock(stock_info, news_articles)

        # 4. Build response
        response = MarketInsightResponse(
            symbol=symbol,
            stock_info=stock_info,
            sentiment=analysis["sentiment"],
            recommendation=analysis["recommendation"],
            confidence=analysis["confidence"],
            summary=analysis["summary"],
            key_points=analysis["key_points"],
            risks=analysis["risks"],
            news_articles=news_articles,
            generated_at=datetime.utcnow()
        )

        # Cache the result for 5 minutes
        cache_service.set(cache_key, response.dict(), ttl=300)

        logger.info(f"Generated insights for {symbol}: {response.recommendation}")
        return response

    except ValueError as e:
        logger.error(f"Invalid request for {symbol}: {str(e)}")
        raise HTTPException(status_code=400, detail=str(e))
    except Exception as e:
        logger.error(f"Error generating insights for {symbol}: {str(e)}")
        raise HTTPException(status_code=500, detail="Failed to generate market insights")


@router.get("/market-insights/{symbol}", response_model=MarketInsightResponse)
async def get_market_insights_by_symbol(symbol: str):
    """
    Simplified GET endpoint for market insights
    """
    request = MarketInsightRequest(symbol=symbol)
    return await get_market_insights(request)
