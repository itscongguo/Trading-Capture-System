# TCS Frontend - Trading Capture System

Bloomberg Terminal-style trading application built with React and TypeScript.

## Features

- **Bloomberg Terminal-Inspired UI** - Professional dark theme with orange accents
- **Real-Time Order Management** - Create, track, and cancel orders in real-time
- **WebSocket Integration** - Live order status updates and trade notifications
- **Live Market Data** - Real-time stock quotes and financial news via Finnhub API
- **Comprehensive Dashboard** - Multi-panel layout for trading operations
- **Type-Safe Development** - Full TypeScript implementation
- **Responsive Design** - Optimized for different screen sizes
- **JWT Authentication** - Secure token-based authentication
- **Price Visualization** - Interactive charts for market data

## Tech Stack

- **React 18** - UI framework
- **TypeScript** - Type safety
- **Vite** - Build tool
- **Ant Design** - UI components
- **Zustand** - State management
- **Axios** - HTTP client
- **Recharts** - Data visualization

## Getting Started

### Prerequisites

- Node.js 18+
- npm or yarn

### Installation

```bash
# Install dependencies
npm install

# Copy environment variables
cp .env.example .env

# Start development server
npm run dev
```

### Build

```bash
# Production build
npm run build

# Preview production build
npm run preview
```

## Environment Variables

```bash
VITE_API_BASE_URL=http://localhost:8080
VITE_WS_URL=ws://localhost:8085/ws
```

### Finnhub API Configuration

To enable real-time market data and financial news, you need a Finnhub API key:

1. **Get a free API key**: Register at [https://finnhub.io/register](https://finnhub.io/register)

2. **Configure the API key** (choose one method):

   **Option 1: Shell Environment Variable (Recommended for macOS/Linux)**
   ```bash
   # Add to ~/.zshrc or ~/.bashrc
   export FINNHUB_API_KEY="your_api_key_here"

   # Reload shell configuration
   source ~/.zshrc  # or source ~/.bashrc
   ```

   **Option 2: Local .env file**
   ```bash
   # Create .env.local (not committed to git)
   echo "VITE_FINNHUB_API_KEY=your_api_key_here" > .env.local
   ```

3. **Restart the development server** to apply changes

## Project Structure

```
src/
├── components/          # Reusable UI components
│   ├── Header/         # Application header
│   ├── Panel/          # Bloomberg-style panel container
│   ├── OrderForm/      # Order entry form
│   ├── OrderList/      # Orders table
│   ├── MarketDataPanel/ # Market data and news feed
│   ├── NotificationPanel/ # Real-time notifications
│   ├── PriceChart/     # Price visualization
│   ├── PriceDisplay/   # Animated price display
│   ├── StatusBadge/    # Order status indicator
│   └── TerminalButton/ # Bloomberg-style button
├── pages/              # Page components
│   ├── Login/          # Login page
│   └── Dashboard/      # Main trading dashboard
├── services/           # API and WebSocket services
│   ├── api.ts          # Axios instance with interceptors
│   ├── authService.ts  # Authentication API
│   ├── orderService.ts # Order management API
│   ├── finnhubService.ts # Finnhub market data API
│   └── websocketService.ts # Real-time updates
├── stores/             # Zustand state management
│   └── authStore.ts    # Authentication state
├── types/              # TypeScript interfaces
│   └── index.ts        # All type definitions
├── styles/             # Global styles
│   └── global.css      # Bloomberg Terminal theme
└── routes/             # Routing configuration
    ├── index.tsx       # Route definitions
    └── PrivateRoute.tsx # Protected route wrapper
```

## UI Components

### Bloomberg Terminal Theme

The application features a professional Bloomberg Terminal-inspired design:

- **Colors**: Dark background (#0A0E1A) with orange accents (#FF8C00)
- **Typography**: Monaco, Consolas monospace fonts
- **Layout**: Multi-panel grid layout for optimal trading workflow
- **Animations**: Smooth price change animations with flash indicators

### Key Components

- **Panel**: Reusable container with header and orange border
- **OrderForm**: Complete order entry with buy/sell buttons
- **OrderList**: Real-time order table with status badges
- **PriceChart**: Interactive price visualization using Recharts
- **NotificationPanel**: Live WebSocket notifications

## API Integration

The frontend connects to the TCS backend services:

```typescript
// Example: Create a new order
import { orderService } from '@/services/orderService'

const order = await orderService.createOrder({
  symbol: 'AAPL',
  side: 'BUY',
  type: 'LIMIT',
  quantity: 100,
  price: 150.50,
  timeInForce: 'DAY'
})
```

## WebSocket Real-Time Updates

```typescript
// Listen for order status updates
wsService.on('ORDER_STATUS', (data) => {
  console.log('Order update:', data)
})

// Listen for trade executions
wsService.on('TRADE', (data) => {
  console.log('Trade executed:', data)
})
```

## Default Credentials

For demo purposes, use these credentials:

- **Username**: `congguo`
- **Password**: `congguooo`

## License

MIT
