import React, { useState } from 'react'
import { Form, Input, Select, InputNumber, message } from 'antd'
import styled from 'styled-components'
import { OrderSide, OrderType, TimeInForce, CreateOrderRequest } from '@/types'
import { orderService } from '@/services/orderService'
import { TerminalButton } from '@/components'

const FormContainer = styled.div`
  padding: 12px;
  font-family: Monaco, Consolas, monospace;

  .ant-form-item {
    margin-bottom: 12px;
  }

  .ant-form-item-label > label {
    color: var(--text-secondary);
    font-size: 10px;
    text-transform: uppercase;
  }

  .ant-select-selector,
  .ant-input-number {
    background: var(--bg-tertiary) !important;
    border-color: var(--border-color) !important;
    color: var(--text-primary) !important;
  }
`

const ButtonGroup = styled.div`
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
  margin-top: 16px;
`

const OrderForm: React.FC = () => {
  const [form] = Form.useForm()
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (values: any) => {
    try {
      setLoading(true)
      const request: CreateOrderRequest = {
        ...values,
        side: values.side as OrderSide,
        type: values.type as OrderType,
        timeInForce: values.timeInForce as TimeInForce,
      }

      await orderService.createOrder(request)
      message.success('Order created successfully')
      form.resetFields()
    } catch (error: any) {
      message.error(error.response?.data?.message || 'Failed to create order')
    } finally {
      setLoading(false)
    }
  }

  const handleBuy = () => {
    form.setFieldValue('side', OrderSide.BUY)
    form.submit()
  }

  const handleSell = () => {
    form.setFieldValue('side', OrderSide.SELL)
    form.submit()
  }

  return (
    <FormContainer>
      <Form
        form={form}
        layout="vertical"
        initialValues={{
          type: OrderType.LIMIT,
          timeInForce: TimeInForce.DAY,
        }}
        onFinish={handleSubmit}
        size="small"
      >
        <Form.Item name="side" hidden>
          <Input />
        </Form.Item>

        <Form.Item
          label="Symbol"
          name="symbol"
          rules={[{ required: true, message: 'Symbol required' }]}
        >
          <Input placeholder="e.g., AAPL" />
        </Form.Item>

        <Form.Item
          label="Order Type"
          name="type"
          rules={[{ required: true }]}
        >
          <Select>
            <Select.Option value={OrderType.MARKET}>Market</Select.Option>
            <Select.Option value={OrderType.LIMIT}>Limit</Select.Option>
            <Select.Option value={OrderType.STOP}>Stop</Select.Option>
            <Select.Option value={OrderType.STOP_LIMIT}>Stop Limit</Select.Option>
          </Select>
        </Form.Item>

        <Form.Item
          label="Quantity"
          name="quantity"
          rules={[{ required: true, message: 'Quantity required' }]}
        >
          <InputNumber
            style={{ width: '100%' }}
            min={1}
            placeholder="Shares"
          />
        </Form.Item>

        <Form.Item
          label="Limit Price"
          name="price"
          dependencies={['type']}
          rules={[
            ({ getFieldValue }) => ({
              required: [OrderType.LIMIT, OrderType.STOP_LIMIT].includes(getFieldValue('type')),
              message: 'Price required',
            }),
          ]}
        >
          <InputNumber
            style={{ width: '100%' }}
            min={0}
            step={0.01}
            placeholder="USD"
          />
        </Form.Item>

        <Form.Item
          label="Time in Force"
          name="timeInForce"
        >
          <Select>
            <Select.Option value={TimeInForce.DAY}>Day</Select.Option>
            <Select.Option value={TimeInForce.GTC}>GTC</Select.Option>
            <Select.Option value={TimeInForce.IOC}>IOC</Select.Option>
            <Select.Option value={TimeInForce.FOK}>FOK</Select.Option>
          </Select>
        </Form.Item>

        <ButtonGroup>
          <TerminalButton
            variant="buy"
            size="large"
            onClick={handleBuy}
            loading={loading}
            block
          >
            Buy
          </TerminalButton>
          <TerminalButton
            variant="sell"
            size="large"
            onClick={handleSell}
            loading={loading}
            block
          >
            Sell
          </TerminalButton>
        </ButtonGroup>
      </Form>
    </FormContainer>
  )
}

export default OrderForm
