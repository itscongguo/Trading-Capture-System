import React from 'react'
import styled from 'styled-components'

interface TerminalButtonProps {
  children: React.ReactNode
  onClick?: () => void
  variant?: 'primary' | 'buy' | 'sell' | 'secondary' | 'danger'
  size?: 'small' | 'medium' | 'large'
  disabled?: boolean
  loading?: boolean
  block?: boolean
  className?: string
}

const Button = styled.button<{
  variant: string,
  size: string,
  block?: boolean
}>`
  font-family: Monaco, Consolas, monospace;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  border: none;
  border-radius: 2px;
  cursor: pointer;
  transition: all 0.2s ease;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  width: ${props => props.block ? '100%' : 'auto'};

  font-size: ${props =>
    props.size === 'small' ? '10px' :
    props.size === 'medium' ? '11px' :
    '12px'
  };

  padding: ${props =>
    props.size === 'small' ? '4px 12px' :
    props.size === 'medium' ? '6px 16px' :
    '8px 20px'
  };

  background: ${props => {
    switch (props.variant) {
      case 'primary':
        return 'var(--accent-orange)'
      case 'buy':
        return 'var(--color-buy)'
      case 'sell':
        return 'var(--color-sell)'
      case 'danger':
        return 'var(--color-danger)'
      case 'secondary':
        return 'var(--bg-tertiary)'
      default:
        return 'var(--accent-orange)'
    }
  }};

  color: ${props =>
    props.variant === 'secondary' ? 'var(--text-primary)' : '#000'
  };

  border: 1px solid ${props => {
    switch (props.variant) {
      case 'primary':
        return 'var(--accent-orange)'
      case 'buy':
        return 'var(--color-buy)'
      case 'sell':
        return 'var(--color-sell)'
      case 'danger':
        return 'var(--color-danger)'
      case 'secondary':
        return 'var(--border-color)'
      default:
        return 'var(--accent-orange)'
    }
  }};

  &:hover:not(:disabled) {
    opacity: 0.8;
    transform: translateY(-1px);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
  }

  &:active:not(:disabled) {
    transform: translateY(0);
  }

  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
`

const TerminalButton: React.FC<TerminalButtonProps> = ({
  children,
  onClick,
  variant = 'primary',
  size = 'medium',
  disabled = false,
  loading = false,
  block = false,
  className,
}) => {
  return (
    <Button
      onClick={onClick}
      variant={variant}
      size={size}
      disabled={disabled || loading}
      block={block}
      className={className}
    >
      {loading ? '...' : children}
    </Button>
  )
}

export default TerminalButton
