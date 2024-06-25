"""
LLM Service - Integrates with OpenAI for market analysis
"""
import os
import logging
from typing import Dict, List
from openai import OpenAI

from app.models.schemas import StockInfo, NewsArticle, Recommendation, Sentiment

logger = logging.getLogger(__name__)


class LLMService:
    """Service for LLM-powered market analysis"""

    def __init__(self):
        api_key = os.getenv("OPENAI_API_KEY")
        self.client = OpenAI(api_key=api_key) if api_key else None
        self.model = os.getenv("OPENAI_MODEL", "gpt-3.5-turbo")
        self.enabled = api_key is not None

        if not self.enabled:
            logger.warning("OpenAI API key not configured. AI features will be limited.")

    def analyze_stock(
        self,
        stock_info: StockInfo,
        news_articles: List[NewsArticle]
    ) -> Dict:
        """
        Generate comprehensive stock analysis using LLM

        Args:
            stock_info: Current stock information
            news_articles: Recent news articles

        Returns:
            Dictionary with analysis results
        """
        if not self.enabled:
            return self._get_fallback_analysis(stock_info)

        try:
            logger.info(f"Generating AI analysis for {stock_info.symbol}")

            # Prepare context from news
            news_context = self._prepare_news_context(news_articles)

            # Build prompt
            prompt = self._build_analysis_prompt(stock_info, news_context)

            # Call OpenAI API
            response = self.client.chat.completions.create(
                model=self.model,
                messages=[
                    {
                        "role": "system",
                        "content": "You are a professional financial analyst providing stock market insights. "
                                   "Be concise, objective, and focus on key actionable points."
                    },
                    {
                        "role": "user",
                        "content": prompt
                    }
                ],
                temperature=0.7,
                max_tokens=800
            )

            analysis_text = response.choices[0].message.content

            # Parse the response
            result = self._parse_analysis(analysis_text, stock_info)

            logger.info(f"Successfully generated analysis for {stock_info.symbol}")
            return result

        except Exception as e:
            logger.error(f"Error generating LLM analysis: {str(e)}")
            return self._get_fallback_analysis(stock_info)

    def _build_analysis_prompt(self, stock_info: StockInfo, news_context: str) -> str:
        """Build the prompt for LLM analysis"""

        return f"""
Analyze {stock_info.company_name} ({stock_info.symbol}) stock:

Current Price: ${stock_info.current_price}
Change: {stock_info.change_percent:+.2f}%
Volume: {stock_info.volume:,}
Market Cap: ${stock_info.market_cap / 1e9:.2f}B

Recent News:
{news_context}

Provide a brief analysis with:
1. SUMMARY (2-3 sentences)
2. SENTIMENT (VERY_BULLISH/BULLISH/NEUTRAL/BEARISH/VERY_BEARISH)
3. RECOMMENDATION (STRONG_BUY/BUY/HOLD/SELL/STRONG_SELL)
4. KEY_POINTS (3-5 bullet points)
5. RISKS (2-3 risk factors)
6. CONFIDENCE (0.0-1.0)

Format your response clearly with these section headers.
"""

    def _prepare_news_context(self, news_articles: List[NewsArticle]) -> str:
        """Prepare news context for LLM"""
        if not news_articles:
            return "No recent news available."

        context_parts = []
        for article in news_articles[:5]:  # Limit to 5 articles
            context_parts.append(
                f"- {article.title} ({article.source})\n"
                f"  {article.description or 'No description'}"
            )

        return "\n".join(context_parts)

    def _parse_analysis(self, analysis_text: str, stock_info: StockInfo) -> Dict:
        """Parse LLM response into structured format"""

        # Simple parsing (can be enhanced)
        lines = analysis_text.split('\n')

        summary = ""
        key_points = []
        risks = []
        sentiment = Sentiment.NEUTRAL
        recommendation = Recommendation.HOLD
        confidence = 0.7

        current_section = None

        for line in lines:
            line = line.strip()
            if not line:
                continue

            # Detect sections
            if 'SUMMARY' in line.upper() or 'Summary' in line:
                current_section = 'summary'
                continue
            elif 'SENTIMENT' in line.upper():
                # Extract sentiment
                for s in Sentiment:
                    if s.value in line.upper():
                        sentiment = s
                        break
                continue
            elif 'RECOMMENDATION' in line.upper():
                # Extract recommendation
                for r in Recommendation:
                    if r.value in line.upper():
                        recommendation = r
                        break
                continue
            elif 'KEY_POINTS' in line.upper() or 'Key Points' in line:
                current_section = 'key_points'
                continue
            elif 'RISKS' in line.upper() or 'Risks' in line or 'RISK' in line.upper():
                current_section = 'risks'
                continue
            elif 'CONFIDENCE' in line.upper():
                # Extract confidence score
                try:
                    confidence = float(line.split(':')[-1].strip())
                except:
                    pass
                continue

            # Add content to appropriate section
            if current_section == 'summary':
                summary += line + " "
            elif current_section == 'key_points' and (line.startswith('-') or line.startswith('•')):
                key_points.append(line.lstrip('-•').strip())
            elif current_section == 'risks' and (line.startswith('-') or line.startswith('•')):
                risks.append(line.lstrip('-•').strip())

        return {
            "summary": summary.strip() or "Analysis completed successfully.",
            "sentiment": sentiment,
            "recommendation": recommendation,
            "key_points": key_points or ["Detailed analysis provided"],
            "risks": risks or ["Standard market risks apply"],
            "confidence": min(max(confidence, 0.0), 1.0)
        }

    def _get_fallback_analysis(self, stock_info: StockInfo) -> Dict:
        """Provide fallback analysis when LLM is unavailable"""

        # Simple rule-based analysis
        change_pct = stock_info.change_percent

        if change_pct > 5:
            sentiment = Sentiment.VERY_BULLISH
            recommendation = Recommendation.BUY
        elif change_pct > 2:
            sentiment = Sentiment.BULLISH
            recommendation = Recommendation.BUY
        elif change_pct > -2:
            sentiment = Sentiment.NEUTRAL
            recommendation = Recommendation.HOLD
        elif change_pct > -5:
            sentiment = Sentiment.BEARISH
            recommendation = Recommendation.SELL
        else:
            sentiment = Sentiment.VERY_BEARISH
            recommendation = Recommendation.SELL

        return {
            "summary": f"{stock_info.symbol} is currently trading at ${stock_info.current_price} "
                       f"with a {change_pct:+.2f}% change.",
            "sentiment": sentiment,
            "recommendation": recommendation,
            "key_points": [
                f"Price: ${stock_info.current_price}",
                f"Change: {change_pct:+.2f}%",
                "AI analysis requires OpenAI API key"
            ],
            "risks": [
                "Market volatility",
                "Configure OPENAI_API_KEY for detailed AI analysis"
            ],
            "confidence": 0.5
        }
