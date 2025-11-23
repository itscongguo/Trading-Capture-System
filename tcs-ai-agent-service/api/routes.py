from fastapi import APIRouter, HTTPException
from datetime import datetime
from api.models import (
    ChatRequest,
    ChatResponse,
    DailySummaryResponse,
    SymbolAnalysisResponse,
    RefreshNewsRequest,
    RefreshNewsResponse,
    StatsResponse,
    NewsSource,
)
from agents.market_intelligence import market_intelligence_agent
from rag.news_loader import news_loader
from rag.vector_store import vector_store_manager
from config import settings

router = APIRouter()


@router.post("/chat", response_model=ChatResponse)
async def chat(request: ChatRequest):
    """
    Chat with the market intelligence agent

    Args:
        request: Chat request with message and optional context

    Returns:
        AI response with sources
    """
    try:
        result = market_intelligence_agent.query(request.message)

        sources = [NewsSource(**source) for source in result.get("sources", [])]

        return ChatResponse(
            answer=result.get("answer", ""),
            sources=sources,
            conversation_id=request.conversation_id,
            success=result.get("success", True),
            error=result.get("error"),
        )

    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.get("/insights/daily-summary", response_model=DailySummaryResponse)
async def get_daily_summary():
    """Get daily market summary"""
    try:
        result = market_intelligence_agent.get_daily_summary()

        sources = [NewsSource(**source) for source in result.get("sources", [])]

        return DailySummaryResponse(
            summary=result.get("answer", ""),
            date=datetime.now().strftime("%Y-%m-%d"),
            sources=sources,
        )

    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.get("/insights/symbol/{symbol}", response_model=SymbolAnalysisResponse)
async def get_symbol_analysis(symbol: str):
    """Get analysis for a specific symbol"""
    try:
        result = market_intelligence_agent.get_symbol_analysis(symbol.upper())

        sources = [NewsSource(**source) for source in result.get("sources", [])]

        return SymbolAnalysisResponse(
            symbol=symbol.upper(), analysis=result.get("answer", ""), sources=sources
        )

    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.post("/admin/refresh-news", response_model=RefreshNewsResponse)
async def refresh_news(request: RefreshNewsRequest):
    """
    Manually trigger news refresh and load into vector store

    Args:
        request: Refresh configuration

    Returns:
        Number of documents added
    """
    try:
        all_documents = []

        # Load general market news
        for category in request.categories:
            docs = news_loader.load_market_news(category, request.days_back)
            all_documents.extend(docs)

        # Load symbol-specific news
        if request.symbols:
            symbol_docs = news_loader.load_multiple_symbols(
                request.symbols, request.days_back
            )
            all_documents.extend(symbol_docs)
        else:
            # Load default symbols
            default_symbols = settings.get_default_symbols_list()
            symbol_docs = news_loader.load_multiple_symbols(
                default_symbols, request.days_back
            )
            all_documents.extend(symbol_docs)

        # Add to vector store
        ids = vector_store_manager.add_documents(all_documents)

        return RefreshNewsResponse(
            success=True,
            documents_added=len(ids),
            message=f"Successfully added {len(ids)} documents to vector store",
        )

    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.get("/admin/stats", response_model=StatsResponse)
async def get_stats():
    """Get vector store statistics"""
    try:
        count = vector_store_manager.get_collection_count()

        return StatsResponse(
            total_documents=count,
            collection_name=settings.chroma_collection_name,
            persist_directory=settings.chroma_persist_directory,
        )

    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
