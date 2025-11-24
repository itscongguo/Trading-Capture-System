import axios from 'axios'

const AI_AGENT_BASE_URL = 'http://localhost:8090/api/v1'

export interface ChatMessage {
  role: 'user' | 'assistant'
  content: string
  timestamp: string
}

export interface NewsSource {
  headline: string
  source: string
  url: string
  date: string
  symbol?: string
}

export interface ChatResponse {
  answer: string
  sources: NewsSource[]
  conversation_id?: string
  success: boolean
  error?: string
}

class AIAgentService {
  async chat(message: string, conversationId?: string, symbols?: string[]): Promise<ChatResponse> {
    try {
      const response = await axios.post(`${AI_AGENT_BASE_URL}/chat`, {
        message,
        conversation_id: conversationId,
        symbols,
      })
      return response.data
    } catch (error) {
      console.error('Error calling AI agent:', error)
      throw error
    }
  }

  async getDailySummary(): Promise<{ summary: string; date: string; sources: NewsSource[] }> {
    try {
      const response = await axios.get(`${AI_AGENT_BASE_URL}/insights/daily-summary`)
      return response.data
    } catch (error) {
      console.error('Error fetching daily summary:', error)
      throw error
    }
  }

  async getSymbolAnalysis(symbol: string): Promise<{ symbol: string; analysis: string; sources: NewsSource[] }> {
    try {
      const response = await axios.get(`${AI_AGENT_BASE_URL}/insights/symbol/${symbol}`)
      return response.data
    } catch (error) {
      console.error(`Error fetching analysis for ${symbol}:`, error)
      throw error
    }
  }

  async refreshNews(categories?: string[], symbols?: string[], daysBack?: number): Promise<{ success: boolean; documents_added: number; message: string }> {
    try {
      const response = await axios.post(`${AI_AGENT_BASE_URL}/admin/refresh-news`, {
        categories: categories || ['general'],
        symbols,
        days_back: daysBack || 1,
      })
      return response.data
    } catch (error) {
      console.error('Error refreshing news:', error)
      throw error
    }
  }
}

export const aiAgentService = new AIAgentService()
