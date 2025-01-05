import { BrowserRouter } from 'react-router-dom'
import { ConfigProvider, theme } from 'antd'
import AppRoutes from './routes'

function App() {
  return (
    <ConfigProvider
      theme={{
        algorithm: theme.darkAlgorithm,
        token: {
          colorPrimary: '#FF8C00',
          colorBgBase: '#0A0E1A',
          colorBgContainer: '#131722',
          colorBorder: '#2A2E39',
          colorText: '#D1D4DC',
          colorTextSecondary: '#787B86',
          fontFamily: 'Monaco, Consolas, "Courier New", monospace',
        },
      }}
    >
      <BrowserRouter>
        <AppRoutes />
      </BrowserRouter>
    </ConfigProvider>
  )
}

export default App
