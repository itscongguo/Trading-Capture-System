import axios from 'axios'
import { MarketNews, MarketQuote } from '@/types'

const FINNHUB_BASE_URL = 'https://finnhub.io/api/v1'

class FinnhubService {
  private apiKey: string
  private axiosInstance

  constructor() {
    // Read API key from environment variable
    // In production, this should be set via VITE_FINNHUB_API_KEY
    // For development, user can set FINNHUB_API_KEY in their shell environment
    this.apiKey = import.meta.env.VITE_FINNHUB_API_KEY || process.env.FINNHUB_API_KEY || ''

    if (!this.apiKey) {
      console.warn('Finnhub API key not found. Please set VITE_FINNHUB_API_KEY or FINNHUB_API_KEY environment variable.')
    }

    this.axiosInstance = axios.create({
      baseURL: FINNHUB_BASE_URL,
      timeout: 10000,
    })
  }

  /**
   * Get market news
   * @param category - News category (general, forex, crypto, merger)
   * @returns Promise<MarketNews[]>
   */
  async getMarketNews(category: string = 'general'): Promise<MarketNews[]> {
    try {
      const response = await this.axiosInstance.get('/news', {
        params: {
          category,
          token: this.apiKey,
        },
      })
      return response.data.slice(0, 20) // Limit to 20 news items
    } catch (error) {
      console.error('Error fetching market news:', error)
      return []
    }
  }

  /**
   * Get stock quote
   * @param symbol - Stock symbol (e.g., AAPL, TSLA)
   * @returns Promise<MarketQuote | null>
   */
  async getQuote(symbol: string): Promise<MarketQuote | null> {
    try {
      const response = await this.axiosInstance.get('/quote', {
        params: {
          symbol,
          token: this.apiKey,
        },
      })
      return response.data
    } catch (error) {
      console.error(`Error fetching quote for ${symbol}:`, error)
      return null
    }
  }

  /**
   * Get multiple stock quotes
   * @param symbols - Array of stock symbols
   * @returns Promise<Record<string, MarketQuote>>
   */
  async getMultipleQuotes(symbols: string[]): Promise<Record<string, MarketQuote>> {
    try {
      const promises = symbols.map(symbol =>
        this.getQuote(symbol).then(quote => ({ symbol, quote }))
      )
      const results = await Promise.all(promises)

      const quotes: Record<string, MarketQuote> = {}
      results.forEach(({ symbol, quote }) => {
        if (quote) {
          quotes[symbol] = quote
        }
      })

      return quotes
    } catch (error) {
      console.error('Error fetching multiple quotes:', error)
      return {}
    }
  }

  /**
   * Search for stocks
   * @param query - Search query
   * @returns Promise<any>
   */
  async searchSymbol(query: string): Promise<any> {
    try {
      const response = await this.axiosInstance.get('/search', {
        params: {
          q: query,
          token: this.apiKey,
        },
      })
      return response.data
    } catch (error) {
      console.error('Error searching symbol:', error)
      return { result: [] }
    }
  }
}

export const finnhubService = new FinnhubService()
