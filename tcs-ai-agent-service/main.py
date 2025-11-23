from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from api.routes import router
from config import settings
import uvicorn

app = FastAPI(
    title="TCS AI Agent Service",
    description="Market Intelligence Agent powered by Gemini and LangChain",
    version="1.0.0",
)

# Configure CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:3000", "http://localhost:8080"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Include API routes
app.include_router(router, prefix="/api/v1", tags=["AI Agent"])


@app.get("/")
async def root():
    """Root endpoint"""
    return {
        "service": "TCS AI Agent Service",
        "version": "1.0.0",
        "status": "running",
        "endpoints": {
            "chat": "/api/v1/chat",
            "daily_summary": "/api/v1/insights/daily-summary",
            "symbol_analysis": "/api/v1/insights/symbol/{symbol}",
            "refresh_news": "/api/v1/admin/refresh-news",
            "stats": "/api/v1/admin/stats",
        },
    }


@app.get("/health")
async def health_check():
    """Health check endpoint"""
    try:
        settings.validate_api_keys()
        return {"status": "healthy", "api_keys_configured": True}
    except ValueError as e:
        return {"status": "unhealthy", "error": str(e), "api_keys_configured": False}


if __name__ == "__main__":
    # Validate API keys on startup
    try:
        settings.validate_api_keys()
        print("✓ API keys validated successfully")
    except ValueError as e:
        print(f"✗ API key validation failed: {e}")
        print("Please set GEMINI_API_KEY and FINNHUB_API_KEY in your environment")
        exit(1)

    print(f"Starting TCS AI Agent Service on {settings.host}:{settings.port}")
    uvicorn.run(app, host=settings.host, port=settings.port)
