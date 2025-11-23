# TCS AI Agent Service

Market Intelligence Agent powered by Google Gemini and LangChain for the Trading Capture System.

## Features

- **RAG-based News Analysis**: Vector search over Finnhub market news using Chroma
- **Gemini Integration**: Powered by `gemini-2.5-flash` for fast, accurate responses
- **LangChain Framework**: Production-ready agent architecture
- **Real-time Market Data**: Integration with Finnhub API
- **REST API**: FastAPI endpoints for chat and insights

## Prerequisites

- Python 3.11+
- Gemini API Key (set in `~/.zshrc`)
- Finnhub API Key (set in `~/.zshrc`)

## Setup

### 1. Install Dependencies

```bash
pip install -r requirements.txt
```

### 2. Configure Environment Variables

The service reads API keys from your shell environment:

```bash
# Already set in ~/.zshrc
export GEMINI_API_KEY="your_gemini_api_key"
export FINNHUB_API_KEY="your_finnhub_api_key"
```

Reload your shell configuration:

```bash
source ~/.zshrc
```

### 3. Load Initial News Data

```bash
python scripts/load_initial_news.py
```

This will:
- Fetch latest market news from Finnhub
- Embed news using Gemini Embeddings
- Store in Chroma vector database

### 4. Start the Server

```bash
python main.py
```

The service will start on `http://localhost:8090`

## API Endpoints

### Chat with Agent

```bash
POST /api/v1/chat
Content-Type: application/json

{
  "message": "Why did AAPL drop today?",
  "conversation_id": "optional-uuid",
  "symbols": ["AAPL"]
}
```

### Get Daily Summary

```bash
GET /api/v1/insights/daily-summary
```

### Get Symbol Analysis

```bash
GET /api/v1/insights/symbol/AAPL
```

### Refresh News Data (Admin)

```bash
POST /api/v1/admin/refresh-news
Content-Type: application/json

{
  "categories": ["general"],
  "symbols": ["AAPL", "GOOGL", "MSFT"],
  "days_back": 1
}
```

### Get Vector Store Stats

```bash
GET /api/v1/admin/stats
```

## Architecture

```
tcs-ai-agent-service/
├── main.py                  # FastAPI application
├── config/
│   └── settings.py          # Environment configuration
├── agents/
│   └── market_intelligence.py  # Main AI agent
├── rag/
│   ├── news_loader.py       # Finnhub news loader
│   └── vector_store.py      # Chroma vector store
├── api/
│   ├── routes.py            # API endpoints
│   └── models.py            # Pydantic models
├── services/
│   └── finnhub_service.py   # Finnhub API client
└── scripts/
    └── load_initial_news.py # Initial data loader
```

## LangChain Components

- **LLM**: `ChatGoogleGenerativeAI` with `gemini-2.5-flash`
- **Embeddings**: `GoogleGenerativeAIEmbeddings` with `models/embedding-001`
- **Vector Store**: Chroma with cosine similarity
- **Chain**: RetrievalQA for question answering
- **Retriever**: VectorStoreRetriever with k=5

## Example Queries

- "Summarize today's market news"
- "Why did Apple stock drop today?"
- "What's the latest on Google?"
- "Any news about Microsoft earnings?"
- "Compare AAPL and GOOGL performance"

## Development

### Run in Development Mode

```bash
uvicorn main:app --reload --port 8090
```

### Clear Vector Store

```python
from rag.vector_store import vector_store_manager
vector_store_manager.delete_collection()
```

## Integration with TCS Frontend

The frontend can call this service via the API Gateway:

```typescript
// tcs-frontend/src/services/aiAgentService.ts
const response = await axios.post('/api/ai-agent/chat', {
  message: "What's the latest on AAPL?"
})
```

## License

MIT
