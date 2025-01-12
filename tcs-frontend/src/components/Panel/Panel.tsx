import React from 'react'
import styled from 'styled-components'

interface PanelProps {
  title?: string
  children: React.ReactNode
  className?: string
  actions?: React.ReactNode
  height?: string
  noPadding?: boolean
}

const PanelContainer = styled.div<{ height?: string }>`
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: 4px;
  display: flex;
  flex-direction: column;
  height: ${props => props.height || 'auto'};
  overflow: hidden;
`

const PanelHeader = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background: var(--bg-tertiary);
  border-bottom: 2px solid var(--accent-orange);
  min-height: 36px;
`

const PanelTitle = styled.h3`
  margin: 0;
  font-size: 11px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 1px;
  color: var(--accent-orange);
  font-family: Monaco, Consolas, monospace;
`

const PanelActions = styled.div`
  display: flex;
  gap: 8px;
  align-items: center;
`

const PanelBody = styled.div<{ noPadding?: boolean }>`
  flex: 1;
  overflow: auto;
  padding: ${props => props.noPadding ? '0' : '12px'};
`

const Panel: React.FC<PanelProps> = ({
  title,
  children,
  className,
  actions,
  height,
  noPadding,
}) => {
  return (
    <PanelContainer className={className} height={height}>
      {title && (
        <PanelHeader>
          <PanelTitle>{title}</PanelTitle>
          {actions && <PanelActions>{actions}</PanelActions>}
        </PanelHeader>
      )}
      <PanelBody noPadding={noPadding}>{children}</PanelBody>
    </PanelContainer>
  )
}

export default Panel
