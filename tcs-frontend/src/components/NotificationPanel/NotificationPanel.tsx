import React, { useEffect, useState } from 'react'
import styled from 'styled-components'
import { BellOutlined, CheckCircleOutlined, CloseCircleOutlined } from '@ant-design/icons'
import dayjs from 'dayjs'
import { Notification } from '@/types'
import { wsService } from '@/services/websocketService'

const Container = styled.div`
  padding: 12px;
  height: 100%;
  overflow-y: auto;
`

const NotificationItem = styled.div<{ type: string }>`
  background: var(--bg-tertiary);
  border-left: 3px solid ${props => {
    switch (props.type) {
      case 'ORDER_STATUS':
        return 'var(--color-info)'
      case 'TRADE':
        return 'var(--color-success)'
      case 'SYSTEM':
        return 'var(--color-warning)'
      default:
        return 'var(--accent-orange)'
    }
  }};
  padding: 8px 10px;
  margin-bottom: 8px;
  border-radius: 2px;
  font-size: 10px;
  font-family: Monaco, Consolas, monospace;
`

const NotificationHeader = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
`

const NotificationTitle = styled.div`
  font-weight: 600;
  color: var(--text-primary);
  display: flex;
  align-items: center;
  gap: 6px;
`

const NotificationTime = styled.span`
  color: var(--text-muted);
  font-size: 9px;
`

const NotificationMessage = styled.div`
  color: var(--text-secondary);
  line-height: 1.4;
`

const EmptyState = styled.div`
  text-align: center;
  color: var(--text-muted);
  padding: 40px 20px;
  font-size: 11px;

  .anticon {
    font-size: 32px;
    margin-bottom: 12px;
    color: var(--text-muted);
  }
`

const NotificationPanel: React.FC = () => {
  const [notifications, setNotifications] = useState<Notification[]>([])

  useEffect(() => {
    const handleNotification = (data: any) => {
      const notification: Notification = {
        id: Date.now().toString(),
        type: data.type,
        title: data.title || 'Notification',
        message: data.message,
        timestamp: data.timestamp || new Date().toISOString(),
        read: false,
        data: data.data,
      }

      setNotifications(prev => [notification, ...prev].slice(0, 50))
    }

    wsService.on('*', handleNotification)

    return () => {
      wsService.off('*', handleNotification)
    }
  }, [])

  const getIcon = (type: string) => {
    switch (type) {
      case 'ORDER_STATUS':
        return <CheckCircleOutlined />
      case 'TRADE':
        return <CheckCircleOutlined />
      case 'SYSTEM':
        return <BellOutlined />
      default:
        return <BellOutlined />
    }
  }

  if (notifications.length === 0) {
    return (
      <Container>
        <EmptyState>
          <BellOutlined />
          <div>No notifications yet</div>
        </EmptyState>
      </Container>
    )
  }

  return (
    <Container>
      {notifications.map(notification => (
        <NotificationItem key={notification.id} type={notification.type}>
          <NotificationHeader>
            <NotificationTitle>
              {getIcon(notification.type)}
              {notification.title}
            </NotificationTitle>
            <NotificationTime>
              {dayjs(notification.timestamp).format('HH:mm:ss')}
            </NotificationTime>
          </NotificationHeader>
          <NotificationMessage>{notification.message}</NotificationMessage>
        </NotificationItem>
      ))}
    </Container>
  )
}

export default NotificationPanel
