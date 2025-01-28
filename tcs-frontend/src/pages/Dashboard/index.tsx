import React from 'react'
import styled from 'styled-components'
import Header from '@/components/Header/Header'
import { Panel } from '@/components'

const DashboardContainer = styled.div`
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: var(--bg-primary);
  overflow: hidden;
`

const Content = styled.div`
  flex: 1;
  display: grid;
  grid-template-columns: 360px 1fr 320px;
  grid-template-rows: 280px 1fr;
  gap: 8px;
  padding: 8px;
  overflow: hidden;
`

const OrderEntryPanel = styled(Panel)`
  grid-column: 1;
  grid-row: 1;
`

const MarketDataPanel = styled(Panel)`
  grid-column: 2;
  grid-row: 1;
`

const NotificationPanel = styled(Panel)`
  grid-column: 3;
  grid-row: 1 / 3;
`

const OrdersPanel = styled(Panel)`
  grid-column: 1 / 3;
  grid-row: 2;
`

const Dashboard: React.FC = () => {
  return (
    <DashboardContainer>
      <Header />
      <Content>
        <OrderEntryPanel title="New Order" noPadding>
          <div style={{ padding: '12px' }}>
            <p style={{ color: 'var(--text-secondary)', fontSize: '11px' }}>
              Order entry form will appear here
            </p>
          </div>
        </OrderEntryPanel>

        <MarketDataPanel title="Market Data">
          <p style={{ color: 'var(--text-secondary)', fontSize: '11px' }}>
            Real-time market data will appear here
          </p>
        </MarketDataPanel>

        <NotificationPanel title="Notifications">
          <p style={{ color: 'var(--text-secondary)', fontSize: '11px' }}>
            Live notifications will appear here
          </p>
        </NotificationPanel>

        <OrdersPanel title="Orders & Trades" noPadding>
          <div style={{ padding: '12px' }}>
            <p style={{ color: 'var(--text-secondary)', fontSize: '11px' }}>
              Orders and trade history will appear here
            </p>
          </div>
        </OrdersPanel>
      </Content>
    </DashboardContainer>
  )
}

export default Dashboard
