import React, { useEffect, useState } from 'react'
import { Table, Tag, Space } from 'antd'
import type { ColumnsType } from 'antd/es/table'
import styled from 'styled-components'
import dayjs from 'dayjs'
import { Order, OrderSide } from '@/types'
import { orderService } from '@/services/orderService'
import { StatusBadge, PriceDisplay, TerminalButton } from '@/components'

const TableContainer = styled.div`
  height: 100%;

  .ant-table {
    font-size: 11px;
  }

  .ant-table-cell {
    padding: 8px !important;
  }
`

const SideTag = styled(Tag)<{ side: OrderSide }>`
  background: ${props => props.side === OrderSide.BUY ?
    'rgba(38, 166, 154, 0.2)' : 'rgba(239, 83, 80, 0.2)'};
  color: ${props => props.side === OrderSide.BUY ?
    'var(--color-buy)' : 'var(--color-sell)'};
  border-color: ${props => props.side === OrderSide.BUY ?
    'var(--color-buy)' : 'var(--color-sell)'};
  font-family: Monaco, Consolas, monospace;
  font-weight: 600;
  font-size: 10px;
`

const OrderList: React.FC = () => {
  const [orders, setOrders] = useState<Order[]>([])
  const [loading, setLoading] = useState(false)
  const [pagination, setPagination] = useState({ page: 0, size: 20, total: 0 })

  const fetchOrders = async (page: number = 0) => {
    try {
      setLoading(true)
      const response = await orderService.getOrders({ page, size: 20 })
      setOrders(response.content)
      setPagination(prev => ({ ...prev, page, total: response.totalElements }))
    } catch (error) {
      console.error('Failed to fetch orders:', error)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchOrders()
  }, [])

  const handleCancel = async (orderId: string) => {
    try {
      await orderService.cancelOrder(orderId)
      fetchOrders(pagination.page)
    } catch (error) {
      console.error('Failed to cancel order:', error)
    }
  }

  const columns: ColumnsType<Order> = [
    {
      title: 'Order ID',
      dataIndex: 'orderId',
      key: 'orderId',
      width: 160,
      render: (id: string) => (
        <span style={{ fontFamily: 'Monaco, monospace', fontSize: '10px' }}>
          {id.substring(0, 12)}...
        </span>
      ),
    },
    {
      title: 'Time',
      dataIndex: 'createdAt',
      key: 'createdAt',
      width: 140,
      render: (time: string) => dayjs(time).format('MM/DD HH:mm:ss'),
    },
    {
      title: 'Symbol',
      dataIndex: 'symbol',
      key: 'symbol',
      width: 80,
    },
    {
      title: 'Side',
      dataIndex: 'side',
      key: 'side',
      width: 60,
      render: (side: OrderSide) => (
        <SideTag side={side}>{side}</SideTag>
      ),
    },
    {
      title: 'Type',
      dataIndex: 'type',
      key: 'type',
      width: 80,
    },
    {
      title: 'Qty',
      dataIndex: 'quantity',
      key: 'quantity',
      width: 80,
      align: 'right',
    },
    {
      title: 'Price',
      dataIndex: 'price',
      key: 'price',
      width: 100,
      align: 'right',
      render: (price: number) => price ? (
        <PriceDisplay value={price} decimals={2} size="small" />
      ) : '-',
    },
    {
      title: 'Filled',
      dataIndex: 'filledQuantity',
      key: 'filledQuantity',
      width: 80,
      align: 'right',
    },
    {
      title: 'Status',
      dataIndex: 'status',
      key: 'status',
      width: 120,
      render: (status) => <StatusBadge status={status} />,
    },
    {
      title: 'Action',
      key: 'action',
      width: 80,
      fixed: 'right',
      render: (_, record) => (
        <Space size="small">
          {['PENDING', 'APPROVED', 'PARTIALLY_FILLED'].includes(record.status) && (
            <TerminalButton
              size="small"
              variant="danger"
              onClick={() => handleCancel(record.orderId)}
            >
              Cancel
            </TerminalButton>
          )}
        </Space>
      ),
    },
  ]

  return (
    <TableContainer>
      <Table
        columns={columns}
        dataSource={orders}
        rowKey="orderId"
        loading={loading}
        pagination={{
          current: pagination.page + 1,
          pageSize: pagination.size,
          total: pagination.total,
          onChange: (page) => fetchOrders(page - 1),
          showSizeChanger: false,
          size: 'small',
        }}
        scroll={{ y: 'calc(100% - 55px)' }}
        size="small"
      />
    </TableContainer>
  )
}

export default OrderList
