import React, { useEffect, useState } from 'react'
import styled from 'styled-components'

interface PriceDisplayProps {
  value: number
  decimals?: number
  showChange?: boolean
  previousValue?: number
  size?: 'small' | 'medium' | 'large'
  className?: string
}

const Price = styled.span<{
  direction?: 'up' | 'down' | 'neutral',
  size: 'small' | 'medium' | 'large',
  flash: boolean
}>`
  font-family: Monaco, Consolas, monospace;
  font-weight: 600;
  color: ${props =>
    props.direction === 'up' ? 'var(--color-buy)' :
    props.direction === 'down' ? 'var(--color-sell)' :
    'var(--text-primary)'
  };
  font-size: ${props =>
    props.size === 'small' ? '12px' :
    props.size === 'medium' ? '14px' :
    '18px'
  };
  transition: color 0.3s ease;
  animation: ${props =>
    props.flash && props.direction === 'up' ? 'flash-green 0.5s ease-in-out' :
    props.flash && props.direction === 'down' ? 'flash-red 0.5s ease-in-out' :
    'none'
  };
`

const ChangeIndicator = styled.span<{ direction: 'up' | 'down' }>`
  margin-left: 6px;
  font-size: 10px;
  color: ${props =>
    props.direction === 'up' ? 'var(--color-buy)' : 'var(--color-sell)'
  };
`

const PriceDisplay: React.FC<PriceDisplayProps> = ({
  value,
  decimals = 2,
  showChange = false,
  previousValue,
  size = 'medium',
  className,
}) => {
  const [flash, setFlash] = useState(false)
  const [direction, setDirection] = useState<'up' | 'down' | 'neutral'>('neutral')

  useEffect(() => {
    if (previousValue !== undefined && value !== previousValue) {
      setDirection(value > previousValue ? 'up' : 'down')
      setFlash(true)
      const timer = setTimeout(() => setFlash(false), 500)
      return () => clearTimeout(timer)
    }
  }, [value, previousValue])

  const formattedValue = value.toFixed(decimals)
  const change = previousValue !== undefined ? value - previousValue : 0
  const changePercent = previousValue !== undefined && previousValue !== 0
    ? ((value - previousValue) / previousValue) * 100
    : 0

  return (
    <span className={className}>
      <Price direction={direction} size={size} flash={flash}>
        {formattedValue}
      </Price>
      {showChange && previousValue !== undefined && change !== 0 && (
        <ChangeIndicator direction={change > 0 ? 'up' : 'down'}>
          {change > 0 ? '▲' : '▼'} {Math.abs(changePercent).toFixed(2)}%
        </ChangeIndicator>
      )}
    </span>
  )
}

export default PriceDisplay
