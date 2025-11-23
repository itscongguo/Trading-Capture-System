"""
Script to load initial news data into the vector store
Run this script once to populate the vector store with news data
"""

import sys
import os

# Add parent directory to path
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from rag.news_loader import news_loader
from rag.vector_store import vector_store_manager
from config import settings


def load_initial_news():
    """Load initial news data"""
    print("Loading initial news data...")

    # Validate API keys
    try:
        settings.validate_api_keys()
    except ValueError as e:
        print(f"Error: {e}")
        return

    all_documents = []

    # Load general market news
    print("Loading general market news...")
    general_docs = news_loader.load_market_news("general", days_back=1)
    all_documents.extend(general_docs)
    print(f"  Loaded {len(general_docs)} general news articles")

    # Load symbol-specific news
    symbols = settings.get_default_symbols_list()
    print(f"Loading news for symbols: {', '.join(symbols)}...")

    symbol_docs = news_loader.load_multiple_symbols(symbols, days_back=7)
    all_documents.extend(symbol_docs)
    print(f"  Loaded {len(symbol_docs)} symbol-specific news articles")

    # Add to vector store
    print("\nAdding documents to vector store...")
    ids = vector_store_manager.add_documents(all_documents)

    print(f"\n✓ Successfully loaded {len(ids)} documents into vector store")

    # Print stats
    count = vector_store_manager.get_collection_count()
    print(f"Total documents in collection: {count}")


if __name__ == "__main__":
    load_initial_news()
