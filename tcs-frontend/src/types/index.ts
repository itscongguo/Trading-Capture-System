export enum OrderSide {
  BUY = 'BUY',
  SELL = 'SELL',
}

export enum OrderType {
  MARKET = 'MARKET',
  LIMIT = 'LIMIT',
  STOP = 'STOP',
  STOP_LIMIT = 'STOP_LIMIT',
}

export enum OrderStatus {
  PENDING = 'PENDING',
  APPROVED = 'APPROVED',
  REJECTED = 'REJECTED',
  PARTIALLY_FILLED = 'PARTIALLY_FILLED',
  FILLED = 'FILLED',
  CANCELLED = 'CANCELLED',
  EXPIRED = 'EXPIRED',
}

export enum TimeInForce {
  DAY = 'DAY',
  GTC = 'GTC',
  IOC = 'IOC',
  FOK = 'FOK',
}

export interface Order {
  orderId: string
  userId: string
  accountId: string
  symbol: string
  side: OrderSide
  type: OrderType
  quantity: number
  price?: number
  stopPrice?: number
  timeInForce: TimeInForce
  status: OrderStatus
  filledQuantity: number
  averagePrice?: number
  createdAt: string
  updatedAt: string
  rejectionReason?: string
}

export interface Trade {
  tradeId: string
  orderId: string
  symbol: string
  side: OrderSide
  quantity: number
  price: number
  counterparty: string
  executedAt: string
}

export interface Position {
  symbol: string
  quantity: number
  averagePrice: number
  currentPrice: number
  unrealizedPnL: number
  realizedPnL: number
}

export interface CreateOrderRequest {
  symbol: string
  side: OrderSide
  type: OrderType
  quantity: number
  price?: number
  stopPrice?: number
  timeInForce: TimeInForce
}

export interface LoginRequest {
  username: string
  password: string
}

export interface LoginResponse {
  accessToken: string
  refreshToken: string
  tokenType: string
  expiresIn: number
  userInfo: UserInfo
}

export interface UserInfo {
  userId: string
  username: string
  accountId: string
  role: string
}

export interface MarketData {
  symbol: string
  lastPrice: number
  bidPrice: number
  askPrice: number
  bidSize: number
  askSize: number
  volume: number
  change: number
  changePercent: number
  high: number
  low: number
  timestamp: string
}

export interface Notification {
  id: string
  type: 'ORDER_STATUS' | 'TRADE' | 'SYSTEM'
  title: string
  message: string
  timestamp: string
  read: boolean
  data?: any
}
