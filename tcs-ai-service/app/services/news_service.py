"""
News Service - Integrates with NewsAPI and other news sources
"""
import os
import logging
from typing import List
from datetime import datetime, timedelta
from newsapi import NewsApiClient

from app.models.schemas import NewsArticle

logger = logging.getLogger(__name__)


class NewsService:
    """Service for fetching financial news"""

    def __init__(self):
        api_key = os.getenv("NEWS_API_KEY")
        self.client = NewsApiClient(api_key=api_key) if api_key else None
        self.enabled = api_key is not None

        if not self.enabled:
            logger.warning("NewsAPI key not configured. News features will be limited.")

    def get_stock_news(self, symbol: str, max_results: int = 5) -> List[NewsArticle]:
        """
        Fetch latest news for a stock symbol

        Args:
            symbol: Stock ticker symbol
            max_results: Maximum number of articles to return

        Returns:
            List of NewsArticle objects
        """
        if not self.enabled:
            logger.warning("News service disabled - no API key configured")
            return self._get_fallback_news(symbol)

        try:
            logger.info(f"Fetching news for {symbol}")

            # Search for news from the past week
            from_date = (datetime.now() - timedelta(days=7)).strftime('%Y-%m-%d')

            # Query NewsAPI
            response = self.client.get_everything(
                q=f"{symbol} stock OR {symbol} shares",
                language='en',
                sort_by='publishedAt',
                from_param=from_date,
                page_size=max_results
            )

            articles = []
            for article in response.get('articles', [])[:max_results]:
                news_article = NewsArticle(
                    title=article['title'],
                    description=article.get('description'),
                    url=article['url'],
                    source=article['source']['name'],
                    published_at=datetime.fromisoformat(
                        article['publishedAt'].replace('Z', '+00:00')
                    )
                )
                articles.append(news_article)

            logger.info(f"Found {len(articles)} news articles for {symbol}")
            return articles

        except Exception as e:
            logger.error(f"Error fetching news for {symbol}: {str(e)}")
            return self._get_fallback_news(symbol)

    def _get_fallback_news(self, symbol: str) -> List[NewsArticle]:
        """
        Provide fallback news when API is unavailable
        """
        return [
            NewsArticle(
                title=f"{symbol} Stock News - API Key Required",
                description="Configure NEWS_API_KEY to get real-time news updates",
                url="https://newsapi.org/",
                source="System",
                published_at=datetime.now()
            )
        ]

    def analyze_sentiment(self, text: str) -> str:
        """
        Simple sentiment analysis (can be enhanced with ML models)

        Args:
            text: Text to analyze

        Returns:
            Sentiment string: BULLISH, BEARISH, or NEUTRAL
        """
        if not text:
            return "NEUTRAL"

        text_lower = text.lower()

        # Simple keyword-based sentiment
        bullish_keywords = ['surge', 'gain', 'rise', 'up', 'profit', 'growth', 'strong', 'beat', 'exceed']
        bearish_keywords = ['drop', 'fall', 'down', 'loss', 'decline', 'weak', 'miss', 'disappointing']

        bullish_count = sum(1 for word in bullish_keywords if word in text_lower)
        bearish_count = sum(1 for word in bearish_keywords if word in text_lower)

        if bullish_count > bearish_count:
            return "BULLISH"
        elif bearish_count > bullish_count:
            return "BEARISH"
        else:
            return "NEUTRAL"
