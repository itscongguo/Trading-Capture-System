-- Create trades table
CREATE TABLE IF NOT EXISTS trades (
    id BIGSERIAL PRIMARY KEY,
    trade_id VARCHAR(64) NOT NULL UNIQUE,
    order_id VARCHAR(64) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    symbol VARCHAR(32) NOT NULL,
    side VARCHAR(4) NOT NULL CHECK (side IN ('BUY', 'SELL')),
    quantity NUMERIC(20, 8) NOT NULL CHECK (quantity > 0),
    price NUMERIC(20, 8) NOT NULL CHECK (price > 0),
    total_amount NUMERIC(20, 2) NOT NULL CHECK (total_amount > 0),
    trace_id VARCHAR(64),
    executed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for common queries
CREATE INDEX idx_trades_order ON trades(order_id);
CREATE INDEX idx_trades_user_executed ON trades(user_id, executed_at DESC);
CREATE INDEX idx_trades_symbol_executed ON trades(symbol, executed_at DESC);
CREATE INDEX idx_trades_executed ON trades(executed_at DESC);

-- Create comment on table
COMMENT ON TABLE trades IS 'Trade executions table';
COMMENT ON COLUMN trades.trade_id IS 'Unique trade identifier';
COMMENT ON COLUMN trades.order_id IS 'Reference to the order that generated this trade';
COMMENT ON COLUMN trades.total_amount IS 'Total trade amount (price * quantity)';
