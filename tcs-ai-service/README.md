# TCS AI Market Intelligence Service

AI-powered market insights and trading recommendations using LLM and real-time financial data.

## Features

- **Real-Time Stock Data**: Integration with Yahoo Finance API
- **News Analysis**: Latest financial news from NewsAPI
- **AI-Powered Insights**: LLM-based market analysis using OpenAI GPT
- **Smart Caching**: Redis-based caching to optimize API usage
- **RESTful API**: Easy integration with frontend and other services

## Technology Stack

- **Framework**: FastAPI (Python 3.11)
- **LLM**: OpenAI GPT-3.5-turbo / GPT-4
- **Data Sources**:
  - Yahoo Finance (stock data)
  - NewsAPI (financial news)
- **Cache**: Redis
- **Future**: ChromaDB for RAG capabilities

## Quick Start

### Prerequisites

- Python 3.11+
- Redis (optional, for caching)
- OpenAI API Key (for AI features)
- NewsAPI Key (optional, for news)

### Installation

```bash
# Install dependencies
pip install -r requirements.txt

# Copy environment file
cp .env.example .env

# Edit .env and add your API keys
# OPENAI_API_KEY=your-key-here
# NEWS_API_KEY=your-key-here
```

### Running the Service

```bash
# Development mode
python -m app.main

# Or with uvicorn
uvicorn app.main:app --reload --port 8087
```

### Docker

```bash
# Build image
docker build -t tcs-ai-service .

# Run container
docker run -p 8087:8087 \
  -e OPENAI_API_KEY=your-key \
  -e NEWS_API_KEY=your-key \
  tcs-ai-service
```

## API Endpoints

### GET /health
Health check endpoint

```bash
curl http://localhost:8087/health
```

### POST /api/ai/market-insights
Get comprehensive market insights for a stock

```bash
curl -X POST http://localhost:8087/api/ai/market-insights \
  -H "Content-Type: application/json" \
  -d '{
    "symbol": "AAPL",
    "include_news": true,
    "max_news": 5
  }'
```

**Response:**
```json
{
  "symbol": "AAPL",
  "stock_info": {
    "symbol": "AAPL",
    "company_name": "Apple Inc.",
    "current_price": 178.50,
    "change": 2.30,
    "change_percent": 1.31,
    "volume": 52000000,
    "market_cap": 2800000000000
  },
  "sentiment": "BULLISH",
  "recommendation": "BUY",
  "confidence": 0.85,
  "summary": "Apple stock shows strong momentum with positive earnings outlook...",
  "key_points": [
    "Strong revenue growth in services segment",
    "New product launches expected soon",
    "Solid balance sheet with high cash reserves"
  ],
  "risks": [
    "Supply chain uncertainties",
    "Market saturation in smartphone segment"
  ],
  "news_articles": [...],
  "generated_at": "2024-06-15T10:30:00Z"
}
```

### GET /api/ai/market-insights/{symbol}
Simplified GET endpoint

```bash
curl http://localhost:8087/api/ai/market-insights/TSLA
```

### POST /api/ai/analyze
Custom stock analysis with specific query

```bash
curl -X POST http://localhost:8087/api/ai/analyze \
  -H "Content-Type: application/json" \
  -d '{
    "symbol": "TSLA",
    "query": "Is this a good time to buy?",
    "time_horizon": "short"
  }'
```

## Environment Variables

| Variable | Description | Required | Default |
|----------|-------------|----------|---------|
| `OPENAI_API_KEY` | OpenAI API key for LLM | No* | - |
| `NEWS_API_KEY` | NewsAPI key for news data | No | - |
| `SERVICE_PORT` | Service port | No | 8087 |
| `REDIS_HOST` | Redis host for caching | No | localhost |
| `REDIS_PORT` | Redis port | No | 6379 |
| `ENABLE_CACHE` | Enable Redis caching | No | true |

*Service works without OpenAI key but with limited AI capabilities

## Architecture

```
┌─────────────┐
│   Frontend  │
└──────┬──────┘
       │ HTTP
       ▼
┌─────────────────────────┐
│  AI Service (FastAPI)   │
│  ┌──────────────────┐   │
│  │  Market Insights │   │
│  │  Router          │   │
│  └────────┬─────────┘   │
│           │             │
│  ┌────────▼─────────┐   │
│  │  LLM Service     │   │
│  │  (OpenAI)        │   │
│  └────────┬─────────┘   │
│           │             │
│  ┌────────▼─────────┐   │
│  │  Stock Service   │   │
│  │  (Yahoo Finance) │   │
│  └──────────────────┘   │
│                         │
│  ┌──────────────────┐   │
│  │  News Service    │   │
│  │  (NewsAPI)       │   │
│  └──────────────────┘   │
│                         │
│  ┌──────────────────┐   │
│  │  Cache Service   │   │
│  │  (Redis)         │   │
│  └──────────────────┘   │
└─────────────────────────┘
```

## Future Enhancements (Phase 2)

- [ ] RAG integration with ChromaDB
- [ ] PDF财报解析和索引
- [ ] LangChain Agent for complex queries
- [ ] Historical data analysis
- [ ] Custom trading strategies
- [ ] Multi-asset comparison
- [ ] Risk scoring models

## License

MIT
