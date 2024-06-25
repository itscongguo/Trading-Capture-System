"""
TCS AI Market Intelligence Service
Provides AI-powered market insights, news analysis, and trading recommendations
"""
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
import logging
from dotenv import load_dotenv
import os

from app.routers import insights, analysis
from app.services.cache_service import CacheService

# Load environment variables
load_dotenv()

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

# Initialize FastAPI app
app = FastAPI(
    title="TCS AI Market Intelligence Service",
    description="AI-powered market insights and trading recommendations",
    version="1.0.0"
)

# CORS middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Initialize cache service
cache_service = CacheService()

# Health check endpoint
@app.get("/health")
async def health_check():
    return {
        "status": "healthy",
        "service": "tcs-ai-service",
        "version": "1.0.0"
    }

# Include routers
app.include_router(insights.router, prefix="/api/ai", tags=["Market Insights"])
app.include_router(analysis.router, prefix="/api/ai", tags=["Stock Analysis"])

# Error handlers
@app.exception_handler(HTTPException)
async def http_exception_handler(request, exc):
    return JSONResponse(
        status_code=exc.status_code,
        content={"error": exc.detail}
    )

@app.exception_handler(Exception)
async def general_exception_handler(request, exc):
    logger.error(f"Unhandled exception: {str(exc)}")
    return JSONResponse(
        status_code=500,
        content={"error": "Internal server error"}
    )

if __name__ == "__main__":
    import uvicorn
    port = int(os.getenv("SERVICE_PORT", 8087))
    host = os.getenv("SERVICE_HOST", "0.0.0.0")

    logger.info(f"Starting TCS AI Service on {host}:{port}")
    uvicorn.run(app, host=host, port=port)
