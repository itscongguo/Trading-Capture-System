-- Create risk_limits table
CREATE TABLE IF NOT EXISTS risk_limits (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    account_id VARCHAR(36) NOT NULL,
    symbol VARCHAR(32),
    notional_limit NUMERIC(20, 2),
    position_limit NUMERIC(20, 8),
    order_count_limit INTEGER,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes
CREATE INDEX idx_risk_limits_user_account ON risk_limits(user_id, account_id);
CREATE INDEX idx_risk_limits_user_symbol ON risk_limits(user_id, symbol);
CREATE UNIQUE INDEX idx_risk_limits_unique ON risk_limits(user_id, account_id, symbol)
    WHERE symbol IS NOT NULL;
CREATE UNIQUE INDEX idx_risk_limits_account_unique ON risk_limits(user_id, account_id)
    WHERE symbol IS NULL;

-- Create trigger to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_risk_limits_updated_at BEFORE UPDATE ON risk_limits
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Insert default risk limits for testing
INSERT INTO risk_limits (user_id, account_id, notional_limit, position_limit, order_count_limit)
VALUES
    ('test-user-1', 'test-account-1', 1000000.00, 10000, 100),
    ('test-user-2', 'test-account-2', 500000.00, 5000, 50);

COMMENT ON TABLE risk_limits IS 'Risk limits for users and accounts';
COMMENT ON COLUMN risk_limits.symbol IS 'NULL means account-level limit, specific symbol means symbol-level limit';
