"""
Stock Analysis Router - Custom analysis queries
"""
from fastapi import APIRouter, HTTPException
import logging

from app.models.schemas import StockAnalysisRequest, StockAnalysisResponse, Recommendation
from app.services.stock_service import StockService
from app.services.llm_service import LLMService

logger = logging.getLogger(__name__)
router = APIRouter()

stock_service = StockService()
llm_service = LLMService()


@router.post("/analyze", response_model=StockAnalysisResponse)
async def analyze_stock(request: StockAnalysisRequest):
    """
    Custom stock analysis with user query

    Example queries:
    - "Is this a good time to buy?"
    - "What are the main risks?"
    - "Compare to industry average"
    """
    symbol = request.symbol.upper()
    logger.info(f"Custom analysis requested for {symbol}: {request.query}")

    try:
        # Fetch stock data
        stock_info = stock_service.get_stock_info(symbol)

        # Generate analysis
        # (Simplified for now - can be enhanced with RAG in Phase 2)
        analysis_text = f"Stock {symbol} is currently trading at ${stock_info.current_price} "
        analysis_text += f"with a {stock_info.change_percent:+.2f}% change. "

        if request.query:
            analysis_text += f"\nRegarding '{request.query}': "
            analysis_text += "Configure OPENAI_API_KEY for detailed AI-powered analysis."
        else:
            analysis_text += "The stock shows standard market behavior."

        # Determine recommendation based on price change
        if stock_info.change_percent > 3:
            recommendation = Recommendation.BUY
        elif stock_info.change_percent < -3:
            recommendation = Recommendation.SELL
        else:
            recommendation = Recommendation.HOLD

        response = StockAnalysisResponse(
            symbol=symbol,
            analysis=analysis_text,
            recommendation=recommendation,
            risk_level="Medium",
            sources=["Yahoo Finance"]
        )

        return response

    except Exception as e:
        logger.error(f"Error analyzing {symbol}: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))
