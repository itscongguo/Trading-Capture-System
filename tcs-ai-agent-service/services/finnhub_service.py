import finnhub
from typing import List, Dict, Any
from datetime import datetime, timedelta
from config import settings


class FinnhubService:
    """Service to interact with Finnhub API"""

    def __init__(self):
        self.client = finnhub.Client(api_key=settings.finnhub_api_key)

    def get_market_news(
        self, category: str = "general", days_back: int = 1
    ) -> List[Dict[str, Any]]:
        """
        Fetch market news from Finnhub

        Args:
            category: News category (general, forex, crypto, merger)
            days_back: Number of days to look back

        Returns:
            List of news articles
        """
        try:
            news = self.client.general_news(category, minid=0)

            # Filter news from last N days
            cutoff_time = datetime.now() - timedelta(days=days_back)
            cutoff_timestamp = int(cutoff_time.timestamp())

            filtered_news = [
                article for article in news
                if article.get('datetime', 0) >= cutoff_timestamp
            ]

            return filtered_news
        except Exception as e:
            print(f"Error fetching news: {e}")
            return []

    def get_company_news(
        self, symbol: str, days_back: int = 7
    ) -> List[Dict[str, Any]]:
        """
        Fetch company-specific news

        Args:
            symbol: Stock symbol (e.g., 'AAPL')
            days_back: Number of days to look back

        Returns:
            List of news articles
        """
        try:
            to_date = datetime.now()
            from_date = to_date - timedelta(days=days_back)

            news = self.client.company_news(
                symbol,
                _from=from_date.strftime("%Y-%m-%d"),
                to=to_date.strftime("%Y-%m-%d"),
            )

            return news
        except Exception as e:
            print(f"Error fetching company news for {symbol}: {e}")
            return []

    def get_quote(self, symbol: str) -> Dict[str, Any]:
        """Get real-time quote for a symbol"""
        try:
            return self.client.quote(symbol)
        except Exception as e:
            print(f"Error fetching quote for {symbol}: {e}")
            return {}


finnhub_service = FinnhubService()
