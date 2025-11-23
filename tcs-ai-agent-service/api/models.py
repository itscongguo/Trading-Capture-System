from pydantic import BaseModel, Field
from typing import List, Optional


class ChatRequest(BaseModel):
    """Request model for chat endpoint"""

    message: str = Field(..., description="User message/question")
    conversation_id: Optional[str] = Field(None, description="Conversation ID for context")
    symbols: Optional[List[str]] = Field(None, description="List of stock symbols to focus on")


class NewsSource(BaseModel):
    """News source information"""

    headline: str
    source: str
    url: str
    date: str
    symbol: str = ""


class ChatResponse(BaseModel):
    """Response model for chat endpoint"""

    answer: str = Field(..., description="AI generated answer")
    sources: List[NewsSource] = Field(default_factory=list, description="Source news articles")
    conversation_id: Optional[str] = Field(None, description="Conversation ID")
    success: bool = Field(True, description="Whether the query was successful")
    error: Optional[str] = Field(None, description="Error message if any")


class DailySummaryResponse(BaseModel):
    """Response model for daily summary"""

    summary: str
    date: str
    sources: List[NewsSource]


class SymbolAnalysisResponse(BaseModel):
    """Response model for symbol analysis"""

    symbol: str
    analysis: str
    sources: List[NewsSource]


class RefreshNewsRequest(BaseModel):
    """Request model for refreshing news"""

    categories: Optional[List[str]] = Field(default=["general"], description="News categories")
    symbols: Optional[List[str]] = Field(None, description="Stock symbols to fetch news for")
    days_back: int = Field(default=1, description="Number of days to look back")


class RefreshNewsResponse(BaseModel):
    """Response model for refresh news"""

    success: bool
    documents_added: int
    message: str


class StatsResponse(BaseModel):
    """Response model for vector store stats"""

    total_documents: int
    collection_name: str
    persist_directory: str
