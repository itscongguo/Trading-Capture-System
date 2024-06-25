"""
Stock Data Service - Integrates with Yahoo Finance API
"""
import yfinance as yf
import logging
from typing import Dict, Optional
from datetime import datetime

from app.models.schemas import StockInfo

logger = logging.getLogger(__name__)


class StockService:
    """Service for fetching stock data from Yahoo Finance"""

    @staticmethod
    def get_stock_info(symbol: str) -> StockInfo:
        """
        Fetch stock information from Yahoo Finance

        Args:
            symbol: Stock ticker symbol (e.g., 'AAPL')

        Returns:
            StockInfo object with current stock data

        Raises:
            ValueError: If symbol is invalid or data cannot be fetched
        """
        try:
            logger.info(f"Fetching stock data for {symbol}")

            ticker = yf.Ticker(symbol)
            info = ticker.info
            history = ticker.history(period="1d")

            if history.empty:
                raise ValueError(f"No data available for symbol: {symbol}")

            current_price = history['Close'].iloc[-1]
            previous_close = info.get('previousClose', current_price)

            change = current_price - previous_close
            change_percent = (change / previous_close) * 100 if previous_close != 0 else 0.0

            stock_info = StockInfo(
                symbol=symbol.upper(),
                company_name=info.get('longName', symbol),
                current_price=round(current_price, 2),
                change=round(change, 2),
                change_percent=round(change_percent, 2),
                volume=int(history['Volume'].iloc[-1]),
                market_cap=info.get('marketCap')
            )

            logger.info(f"Successfully fetched data for {symbol}: ${stock_info.current_price}")
            return stock_info

        except Exception as e:
            logger.error(f"Error fetching stock data for {symbol}: {str(e)}")
            raise ValueError(f"Failed to fetch stock data for {symbol}: {str(e)}")

    @staticmethod
    def get_historical_data(symbol: str, period: str = "1mo") -> Dict:
        """
        Fetch historical price data

        Args:
            symbol: Stock ticker symbol
            period: Time period (1d, 5d, 1mo, 3mo, 6mo, 1y, 2y, 5y, 10y, ytd, max)

        Returns:
            Dictionary with historical price data
        """
        try:
            ticker = yf.Ticker(symbol)
            history = ticker.history(period=period)

            return {
                "symbol": symbol,
                "period": period,
                "data": history.to_dict(orient='index'),
                "high": float(history['High'].max()),
                "low": float(history['Low'].min()),
                "avg": float(history['Close'].mean())
            }
        except Exception as e:
            logger.error(f"Error fetching historical data for {symbol}: {str(e)}")
            raise ValueError(f"Failed to fetch historical data: {str(e)}")
