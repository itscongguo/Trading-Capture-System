"""
Pydantic models for API requests and responses
"""
from pydantic import BaseModel, Field
from typing import Optional, List
from datetime import datetime
from enum import Enum


class Sentiment(str, Enum):
    VERY_BULLISH = "VERY_BULLISH"
    BULLISH = "BULLISH"
    NEUTRAL = "NEUTRAL"
    BEARISH = "BEARISH"
    VERY_BEARISH = "VERY_BEARISH"


class Recommendation(str, Enum):
    STRONG_BUY = "STRONG_BUY"
    BUY = "BUY"
    HOLD = "HOLD"
    SELL = "SELL"
    STRONG_SELL = "STRONG_SELL"


class NewsArticle(BaseModel):
    title: str
    description: Optional[str] = None
    url: str
    source: str
    published_at: datetime
    sentiment: Optional[Sentiment] = None


class StockInfo(BaseModel):
    symbol: str
    company_name: str
    current_price: float
    change: float
    change_percent: float
    volume: int
    market_cap: Optional[float] = None


class MarketInsightRequest(BaseModel):
    symbol: str = Field(..., description="Stock symbol (e.g., AAPL)")
    include_news: bool = Field(default=True, description="Include latest news analysis")
    max_news: int = Field(default=5, ge=1, le=20, description="Maximum number of news articles")


class MarketInsightResponse(BaseModel):
    symbol: str
    stock_info: StockInfo
    sentiment: Sentiment
    recommendation: Recommendation
    confidence: float = Field(..., ge=0.0, le=1.0, description="Confidence score 0-1")
    summary: str
    key_points: List[str]
    risks: List[str]
    news_articles: List[NewsArticle]
    generated_at: datetime = Field(default_factory=datetime.utcnow)


class StockAnalysisRequest(BaseModel):
    symbol: str
    query: Optional[str] = Field(default=None, description="Specific question about the stock")
    time_horizon: Optional[str] = Field(default="short", description="short/medium/long term")


class StockAnalysisResponse(BaseModel):
    symbol: str
    analysis: str
    recommendation: Recommendation
    target_price: Optional[float] = None
    risk_level: str
    sources: List[str]
    generated_at: datetime = Field(default_factory=datetime.utcnow)


class ErrorResponse(BaseModel):
    error: str
    detail: Optional[str] = None
