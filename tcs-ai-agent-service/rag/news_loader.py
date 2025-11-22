from typing import List
from langchain.schema import Document
from services.finnhub_service import finnhub_service
from datetime import datetime


class NewsLoader:
    """Load and process news from Finnhub into LangChain Documents"""

    def load_market_news(self, category: str = "general", days_back: int = 1) -> List[Document]:
        """
        Load market news as LangChain Documents

        Args:
            category: News category
            days_back: Number of days to look back

        Returns:
            List of Document objects
        """
        news_articles = finnhub_service.get_market_news(category, days_back)
        return self._convert_to_documents(news_articles)

    def load_symbol_news(self, symbol: str, days_back: int = 7) -> List[Document]:
        """
        Load symbol-specific news as LangChain Documents

        Args:
            symbol: Stock symbol
            days_back: Number of days to look back

        Returns:
            List of Document objects
        """
        news_articles = finnhub_service.get_company_news(symbol, days_back)
        return self._convert_to_documents(news_articles, symbol=symbol)

    def load_multiple_symbols(self, symbols: List[str], days_back: int = 7) -> List[Document]:
        """Load news for multiple symbols"""
        all_documents = []
        for symbol in symbols:
            docs = self.load_symbol_news(symbol, days_back)
            all_documents.extend(docs)
        return all_documents

    def _convert_to_documents(
        self, news_articles: List[dict], symbol: str = None
    ) -> List[Document]:
        """Convert Finnhub news articles to LangChain Documents"""
        documents = []

        for article in news_articles:
            # Create document content
            content = self._format_article_content(article)

            # Create metadata
            metadata = {
                "source": article.get("source", "Unknown"),
                "url": article.get("url", ""),
                "datetime": article.get("datetime", 0),
                "headline": article.get("headline", ""),
                "category": article.get("category", "general"),
            }

            # Add symbol if available
            if symbol:
                metadata["symbol"] = symbol
            elif article.get("related"):
                metadata["symbol"] = article.get("related", "")

            # Add timestamp
            timestamp = article.get("datetime", 0)
            if timestamp:
                metadata["date"] = datetime.fromtimestamp(timestamp).strftime("%Y-%m-%d %H:%M:%S")

            doc = Document(page_content=content, metadata=metadata)
            documents.append(doc)

        return documents

    def _format_article_content(self, article: dict) -> str:
        """Format article into readable content"""
        headline = article.get("headline", "")
        summary = article.get("summary", "")
        source = article.get("source", "Unknown")
        timestamp = article.get("datetime", 0)

        date_str = ""
        if timestamp:
            date_str = datetime.fromtimestamp(timestamp).strftime("%Y-%m-%d %H:%M")

        content_parts = []

        if headline:
            content_parts.append(f"标题: {headline}")

        if date_str:
            content_parts.append(f"时间: {date_str}")

        if source:
            content_parts.append(f"来源: {source}")

        if summary:
            content_parts.append(f"\n摘要: {summary}")

        return "\n".join(content_parts)


news_loader = NewsLoader()
