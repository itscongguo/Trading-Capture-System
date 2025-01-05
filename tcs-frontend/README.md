# TCS Frontend - Trading Capture System

Bloomberg Terminal-style trading application built with React and TypeScript.

## Features

- Bloomberg Terminal-inspired dark theme
- Real-time order management
- WebSocket integration for live updates
- Professional trading interface
- TypeScript for type safety

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

```
VITE_API_BASE_URL=http://localhost:8080
VITE_WS_URL=ws://localhost:8085/ws
```

## Project Structure

```
src/
├── components/     # Reusable components
├── pages/         # Page components
├── services/      # API services
├── stores/        # State management
├── types/         # TypeScript types
├── styles/        # Global styles
└── routes/        # Routing configuration
```

## License

MIT
