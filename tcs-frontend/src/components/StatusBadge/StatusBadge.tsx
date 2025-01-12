import React from 'react'
import styled from 'styled-components'
import { OrderStatus } from '@/types'

interface StatusBadgeProps {
  status: OrderStatus
  className?: string
}

const Badge = styled.span<{ statusType: string }>`
  display: inline-block;
  padding: 2px 8px;
  font-size: 10px;
  font-weight: 600;
  font-family: Monaco, Consolas, monospace;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  border-radius: 2px;
  background: ${props => {
    switch (props.statusType) {
      case 'PENDING':
      case 'APPROVED':
        return 'rgba(33, 150, 243, 0.2)'
      case 'FILLED':
        return 'rgba(38, 166, 154, 0.2)'
      case 'PARTIALLY_FILLED':
        return 'rgba(255, 152, 0, 0.2)'
      case 'REJECTED':
      case 'CANCELLED':
      case 'EXPIRED':
        return 'rgba(239, 83, 80, 0.2)'
      default:
        return 'rgba(120, 123, 134, 0.2)'
    }
  }};
  color: ${props => {
    switch (props.statusType) {
      case 'PENDING':
      case 'APPROVED':
        return 'var(--color-info)'
      case 'FILLED':
        return 'var(--color-success)'
      case 'PARTIALLY_FILLED':
        return 'var(--color-warning)'
      case 'REJECTED':
      case 'CANCELLED':
      case 'EXPIRED':
        return 'var(--color-danger)'
      default:
        return 'var(--text-secondary)'
    }
  }};
  border: 1px solid ${props => {
    switch (props.statusType) {
      case 'PENDING':
      case 'APPROVED':
        return 'var(--color-info)'
      case 'FILLED':
        return 'var(--color-success)'
      case 'PARTIALLY_FILLED':
        return 'var(--color-warning)'
      case 'REJECTED':
      case 'CANCELLED':
      case 'EXPIRED':
        return 'var(--color-danger)'
      default:
        return 'var(--text-secondary)'
    }
  }};
`

const StatusBadge: React.FC<StatusBadgeProps> = ({ status, className }) => {
  return (
    <Badge statusType={status} className={className}>
      {status}
    </Badge>
  )
}

export default StatusBadge
