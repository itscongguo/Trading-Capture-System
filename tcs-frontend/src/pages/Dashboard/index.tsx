import React, { useEffect } from 'react'
import styled from 'styled-components'
import Header from '@/components/Header/Header'
import { Panel } from '@/components'
import OrderForm from '@/components/OrderForm/OrderForm'
import OrderList from '@/components/OrderList/OrderList'
import NotificationPanel from '@/components/NotificationPanel/NotificationPanel'
import MarketDataPanel from '@/components/MarketDataPanel'
import { wsService } from '@/services/websocketService'

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

const MarketDataPanelWrapper = styled(Panel)`
  grid-column: 2;
  grid-row: 1;
`

const NotificationPanelWrapper = styled(Panel)`
  grid-column: 3;
  grid-row: 1 / 3;
`

const OrdersPanel = styled(Panel)`
  grid-column: 1 / 3;
  grid-row: 2;
`

const Dashboard: React.FC = () => {
  useEffect(() => {
    wsService.connect()

    return () => {
      wsService.disconnect()
    }
  }, [])

  return (
    <DashboardContainer>
      <Header />
      <Content>
        <OrderEntryPanel title="New Order" noPadding>
          <OrderForm />
        </OrderEntryPanel>

        <MarketDataPanelWrapper title="Market Data" noPadding>
          <MarketDataPanel />
        </MarketDataPanelWrapper>

        <NotificationPanelWrapper title="Notifications" noPadding>
          <NotificationPanel />
        </NotificationPanelWrapper>

        <OrdersPanel title="Orders & Trades" noPadding>
          <OrderList />
        </OrdersPanel>
      </Content>
    </DashboardContainer>
  )
}

export default Dashboard
