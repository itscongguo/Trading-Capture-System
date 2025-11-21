import React, { useEffect, useState } from 'react'
import styled from 'styled-components'
import { TrendingUpOutlined, TrendingDownOutlined, GlobalOutlined, DollarOutlined } from '@ant-design/icons'
import { MarketNews, MarketQuote } from '@/types'
import { finnhubService } from '@/services/finnhubService'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'

dayjs.extend(relativeTime)

const Container = styled.div`
  display: flex;
  flex-direction: column;
  height: 100%;
  padding: 12px;
  overflow: hidden;
`

const QuotesSection = styled.div`
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
  margin-bottom: 12px;
`

const QuoteCard = styled.div<{ isPositive: boolean }>`
  background: var(--bg-tertiary);
  border: 1px solid var(--border-color);
  border-left: 3px solid ${props => props.isPositive ? 'var(--color-success)' : 'var(--color-danger)'};
  padding: 8px;
  border-radius: 2px;
  font-family: Monaco, Consolas, monospace;
`

const QuoteSymbol = styled.div`
  font-size: 11px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 4px;
`

const QuotePrice = styled.div`
  font-size: 14px;
  font-weight: 700;
  color: var(--accent-orange);
  margin-bottom: 2px;
`

const QuoteChange = styled.div<{ isPositive: boolean }>`
  font-size: 9px;
  color: ${props => props.isPositive ? 'var(--color-success)' : 'var(--color-danger)'};
  display: flex;
  align-items: center;
  gap: 4px;
`

const NewsSection = styled.div`
  flex: 1;
  overflow-y: auto;

  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-track {
    background: var(--bg-primary);
  }

  &::-webkit-scrollbar-thumb {
    background: var(--border-color);
    border-radius: 3px;
  }
`

const SectionTitle = styled.div`
  font-size: 10px;
  font-weight: 600;
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin-bottom: 8px;
  padding-bottom: 4px;
  border-bottom: 1px solid var(--border-color);
  display: flex;
  align-items: center;
  gap: 6px;
`

const NewsItem = styled.a`
  display: block;
  background: var(--bg-tertiary);
  border: 1px solid var(--border-color);
  padding: 8px;
  margin-bottom: 6px;
  border-radius: 2px;
  text-decoration: none;
  transition: all 0.2s;

  &:hover {
    border-color: var(--accent-orange);
    transform: translateX(2px);
  }
`

const NewsHeader = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 4px;
`

const NewsSource = styled.span`
  font-size: 9px;
  color: var(--accent-orange);
  font-weight: 600;
  text-transform: uppercase;
`

const NewsTime = styled.span`
  font-size: 8px;
  color: var(--text-muted);
`

const NewsHeadline = styled.div`
  font-size: 10px;
  color: var(--text-primary);
  font-weight: 500;
  line-height: 1.4;
  margin-bottom: 4px;
`

const NewsSummary = styled.div`
  font-size: 9px;
  color: var(--text-secondary);
  line-height: 1.3;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
`

const LoadingState = styled.div`
  text-align: center;
  color: var(--text-muted);
  padding: 20px;
  font-size: 11px;
`

const ErrorState = styled.div`
  text-align: center;
  color: var(--color-danger);
  padding: 20px;
  font-size: 11px;
`

const DEFAULT_SYMBOLS = ['AAPL', 'GOOGL', 'MSFT']

const MarketDataPanel: React.FC = () => {
  const [news, setNews] = useState<MarketNews[]>([])
  const [quotes, setQuotes] = useState<Record<string, MarketQuote>>({})
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    const fetchMarketData = async () => {
      try {
        setLoading(true)
        setError(null)

        // Fetch news and quotes in parallel
        const [newsData, quotesData] = await Promise.all([
          finnhubService.getMarketNews('general'),
          finnhubService.getMultipleQuotes(DEFAULT_SYMBOLS),
        ])

        setNews(newsData)
        setQuotes(quotesData)
      } catch (err) {
        console.error('Error fetching market data:', err)
        setError('Failed to load market data. Please check your API key.')
      } finally {
        setLoading(false)
      }
    }

    fetchMarketData()

    // Refresh data every 5 minutes
    const interval = setInterval(fetchMarketData, 5 * 60 * 1000)

    return () => clearInterval(interval)
  }, [])

  if (loading) {
    return (
      <Container>
        <LoadingState>Loading market data...</LoadingState>
      </Container>
    )
  }

  if (error) {
    return (
      <Container>
        <ErrorState>{error}</ErrorState>
      </Container>
    )
  }

  return (
    <Container>
      {/* Market Quotes Section */}
      <QuotesSection>
        {DEFAULT_SYMBOLS.map(symbol => {
          const quote = quotes[symbol]
          if (!quote) return null

          const isPositive = quote.d >= 0

          return (
            <QuoteCard key={symbol} isPositive={isPositive}>
              <QuoteSymbol>{symbol}</QuoteSymbol>
              <QuotePrice>${quote.c.toFixed(2)}</QuotePrice>
              <QuoteChange isPositive={isPositive}>
                {isPositive ? <TrendingUpOutlined /> : <TrendingDownOutlined />}
                {quote.d > 0 ? '+' : ''}{quote.d.toFixed(2)} ({quote.dp.toFixed(2)}%)
              </QuoteChange>
            </QuoteCard>
          )
        })}
      </QuotesSection>

      {/* Market News Section */}
      <SectionTitle>
        <GlobalOutlined />
        Market News
      </SectionTitle>
      <NewsSection>
        {news.length === 0 ? (
          <LoadingState>No news available</LoadingState>
        ) : (
          news.map((item) => (
            <NewsItem
              key={item.id}
              href={item.url}
              target="_blank"
              rel="noopener noreferrer"
            >
              <NewsHeader>
                <NewsSource>{item.source}</NewsSource>
                <NewsTime>{dayjs.unix(item.datetime).fromNow()}</NewsTime>
              </NewsHeader>
              <NewsHeadline>{item.headline}</NewsHeadline>
              <NewsSummary>{item.summary}</NewsSummary>
            </NewsItem>
          ))
        )}
      </NewsSection>
    </Container>
  )
}

export default MarketDataPanel
