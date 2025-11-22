import os
from typing import Optional
from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    """Application settings loaded from environment variables"""

    # API Keys
    gemini_api_key: Optional[str] = None
    finnhub_api_key: Optional[str] = None

    # Server Configuration
    host: str = "0.0.0.0"
    port: int = 8090

    # LLM Configuration
    gemini_model: str = "gemini-2.5-flash"
    gemini_temperature: float = 0.3
    gemini_max_tokens: int = 1024

    # Embeddings Configuration
    embedding_model: str = "models/embedding-001"

    # Vector Store Configuration
    chroma_persist_directory: str = "./data/chroma_db"
    chroma_collection_name: str = "finnhub_news"

    # News Loading Configuration
    news_refresh_interval_hours: int = 1
    news_categories: str = "general"
    default_symbols: str = "AAPL,GOOGL,MSFT"

    class Config:
        env_file = ".env"
        case_sensitive = False

    def __init__(self, **kwargs):
        super().__init__(**kwargs)
        # Read from shell environment if not in .env
        if not self.gemini_api_key:
            self.gemini_api_key = os.getenv("GEMINI_API_KEY")
        if not self.finnhub_api_key:
            self.finnhub_api_key = os.getenv("FINNHUB_API_KEY")

    def validate_api_keys(self) -> None:
        """Validate that required API keys are present"""
        if not self.gemini_api_key:
            raise ValueError(
                "GEMINI_API_KEY not found. Please set it in ~/.zshrc or .env file"
            )
        if not self.finnhub_api_key:
            raise ValueError(
                "FINNHUB_API_KEY not found. Please set it in ~/.zshrc or .env file"
            )

    def get_default_symbols_list(self) -> list[str]:
        """Get default symbols as a list"""
        return [s.strip() for s in self.default_symbols.split(",")]


settings = Settings()
