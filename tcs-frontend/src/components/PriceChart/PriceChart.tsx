import React from 'react'
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts'
import styled from 'styled-components'

interface PriceChartProps {
  data: Array<{ time: string; price: number }>
  symbol?: string
}

const ChartContainer = styled.div`
  width: 100%;
  height: 100%;
  padding: 12px;

  .recharts-cartesian-grid-horizontal line,
  .recharts-cartesian-grid-vertical line {
    stroke: var(--border-color);
  }

  .recharts-text {
    fill: var(--text-secondary);
    font-family: Monaco, Consolas, monospace;
    font-size: 10px;
  }
`

const CustomTooltip = styled.div`
  background: var(--bg-tertiary);
  border: 1px solid var(--accent-orange);
  padding: 8px;
  font-family: Monaco, Consolas, monospace;
  font-size: 11px;

  .label {
    color: var(--text-secondary);
    margin-bottom: 4px;
  }

  .value {
    color: var(--accent-orange);
    font-weight: 600;
  }
`

const PriceChart: React.FC<PriceChartProps> = ({ data, symbol }) => {
  const renderTooltip = ({ active, payload }: any) => {
    if (active && payload && payload.length) {
      return (
        <CustomTooltip>
          <div className="label">{payload[0].payload.time}</div>
          <div className="value">${payload[0].value.toFixed(2)}</div>
        </CustomTooltip>
      )
    }
    return null
  }

  return (
    <ChartContainer>
      <ResponsiveContainer width="100%" height="100%">
        <LineChart data={data} margin={{ top: 5, right: 20, left: 10, bottom: 5 }}>
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis dataKey="time" />
          <YAxis domain={['dataMin - 5', 'dataMax + 5']} />
          <Tooltip content={renderTooltip} />
          <Line
            type="monotone"
            dataKey="price"
            stroke="var(--accent-orange)"
            strokeWidth={2}
            dot={false}
            activeDot={{ r: 4 }}
          />
        </LineChart>
      </ResponsiveContainer>
    </ChartContainer>
  )
}

export default PriceChart
