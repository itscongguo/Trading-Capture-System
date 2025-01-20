import React, { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Form, Input, message } from 'antd'
import { UserOutlined, LockOutlined } from '@ant-design/icons'
import styled from 'styled-components'
import { authService } from '@/services/authService'
import { useAuthStore } from '@/stores/authStore'
import { TerminalButton } from '@/components'

const LoginContainer = styled.div`
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: var(--bg-primary);
  padding: 20px;
`

const LoginBox = styled.div`
  width: 100%;
  max-width: 420px;
  background: var(--bg-secondary);
  border: 2px solid var(--accent-orange);
  border-radius: 4px;
  padding: 40px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.4);
`

const Logo = styled.div`
  text-align: center;
  margin-bottom: 32px;
`

const LogoText = styled.h1`
  font-family: Monaco, Consolas, monospace;
  font-size: 24px;
  font-weight: 700;
  color: var(--accent-orange);
  margin: 0 0 8px 0;
  letter-spacing: 2px;
  text-transform: uppercase;
`

const Subtitle = styled.p`
  font-family: Monaco, Consolas, monospace;
  font-size: 11px;
  color: var(--text-secondary);
  margin: 0;
  letter-spacing: 1px;
  text-transform: uppercase;
`

const StyledForm = styled(Form)`
  .ant-form-item {
    margin-bottom: 20px;
  }

  .ant-form-item-label > label {
    color: var(--text-secondary);
    font-family: Monaco, Consolas, monospace;
    font-size: 11px;
    text-transform: uppercase;
    letter-spacing: 0.5px;
  }

  .ant-input-affix-wrapper {
    background: var(--bg-tertiary) !important;
    border-color: var(--border-color) !important;
    padding: 10px 12px;

    input {
      background: transparent !important;
      color: var(--text-primary) !important;
      font-family: Monaco, Consolas, monospace;
    }

    .anticon {
      color: var(--text-secondary);
    }

    &:focus,
    &:focus-within {
      border-color: var(--accent-orange) !important;
      box-shadow: 0 0 0 2px rgba(255, 140, 0, 0.1) !important;
    }
  }
`

const Footer = styled.div`
  margin-top: 24px;
  text-align: center;
  font-size: 10px;
  color: var(--text-muted);
  font-family: Monaco, Consolas, monospace;
`

const ErrorMessage = styled.div`
  background: rgba(239, 83, 80, 0.1);
  border: 1px solid var(--color-danger);
  color: var(--color-danger);
  padding: 12px;
  border-radius: 2px;
  margin-bottom: 20px;
  font-family: Monaco, Consolas, monospace;
  font-size: 11px;
`

const Login: React.FC = () => {
  const navigate = useNavigate()
  const { login } = useAuthStore()
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const onFinish = async (values: { username: string; password: string }) => {
    try {
      setLoading(true)
      setError(null)

      const response = await authService.login(values)

      login(response.accessToken, response.refreshToken, response.userInfo)

      message.success('Login successful')
      navigate('/dashboard')
    } catch (err: any) {
      const errorMessage =
        err.response?.data?.message || 'Invalid username or password'
      setError(errorMessage)
      message.error(errorMessage)
    } finally {
      setLoading(false)
    }
  }

  return (
    <LoginContainer>
      <LoginBox>
        <Logo>
          <LogoText>TCS TERMINAL</LogoText>
          <Subtitle>Trading Capture System</Subtitle>
        </Logo>

        {error && <ErrorMessage>{error}</ErrorMessage>}

        <StyledForm
          name="login"
          initialValues={{ remember: true }}
          onFinish={onFinish}
          autoComplete="off"
          size="large"
        >
          <Form.Item
            label="Username"
            name="username"
            rules={[{ required: true, message: 'Please input your username' }]}
          >
            <Input
              prefix={<UserOutlined />}
              placeholder="Enter username"
              autoComplete="username"
            />
          </Form.Item>

          <Form.Item
            label="Password"
            name="password"
            rules={[{ required: true, message: 'Please input your password' }]}
          >
            <Input.Password
              prefix={<LockOutlined />}
              placeholder="Enter password"
              autoComplete="current-password"
            />
          </Form.Item>

          <Form.Item style={{ marginBottom: 0 }}>
            <TerminalButton
              variant="primary"
              size="large"
              loading={loading}
              block
              onClick={() => {}}
            >
              Login
            </TerminalButton>
          </Form.Item>
        </StyledForm>

        <Footer>
          © 2025 Trading Capture System · Admin Access Only
        </Footer>
      </LoginBox>
    </LoginContainer>
  )
}

export default Login
