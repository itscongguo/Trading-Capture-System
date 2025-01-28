import React from 'react'
import { useNavigate } from 'react-router-dom'
import styled from 'styled-components'
import { LogoutOutlined, UserOutlined, ClockCircleOutlined } from '@ant-design/icons'
import { useAuthStore } from '@/stores/authStore'
import { TerminalButton } from '@/components'
import dayjs from 'dayjs'

const HeaderContainer = styled.header`
  height: 48px;
  background: var(--bg-tertiary);
  border-bottom: 2px solid var(--accent-orange);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  position: sticky;
  top: 0;
  z-index: 1000;
`

const Logo = styled.div`
  display: flex;
  align-items: center;
  gap: 12px;
`

const LogoText = styled.h1`
  margin: 0;
  font-size: 14px;
  font-weight: 700;
  color: var(--accent-orange);
  letter-spacing: 2px;
  font-family: Monaco, Consolas, monospace;
`

const RightSection = styled.div`
  display: flex;
  align-items: center;
  gap: 24px;
`

const Clock = styled.div`
  display: flex;
  align-items: center;
  gap: 6px;
  font-family: Monaco, Consolas, monospace;
  font-size: 12px;
  color: var(--text-secondary);

  .anticon {
    color: var(--accent-orange);
  }
`

const UserInfo = styled.div`
  display: flex;
  align-items: center;
  gap: 8px;
  font-family: Monaco, Consolas, monospace;
  font-size: 11px;
  color: var(--text-primary);
  text-transform: uppercase;

  .anticon {
    color: var(--accent-orange);
  }
`

const Header: React.FC = () => {
  const navigate = useNavigate()
  const { logout, userInfo } = useAuthStore()
  const [currentTime, setCurrentTime] = React.useState(dayjs())

  React.useEffect(() => {
    const timer = setInterval(() => {
      setCurrentTime(dayjs())
    }, 1000)
    return () => clearInterval(timer)
  }, [])

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  return (
    <HeaderContainer>
      <Logo>
        <LogoText>TCS TERMINAL</LogoText>
      </Logo>

      <RightSection>
        <Clock>
          <ClockCircleOutlined />
          <span>{currentTime.format('YYYY-MM-DD HH:mm:ss')}</span>
        </Clock>

        <UserInfo>
          <UserOutlined />
          <span>{userInfo?.username || 'User'}</span>
        </UserInfo>

        <TerminalButton
          variant="secondary"
          size="small"
          onClick={handleLogout}
        >
          <LogoutOutlined /> Logout
        </TerminalButton>
      </RightSection>
    </HeaderContainer>
  )
}

export default Header
