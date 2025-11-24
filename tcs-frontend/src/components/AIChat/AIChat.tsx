import React, { useState, useRef, useEffect } from 'react'
import styled from 'styled-components'
import { SendOutlined, RobotOutlined, UserOutlined, CloseOutlined, LinkOutlined } from '@ant-design/icons'
import { aiAgentService, ChatMessage, NewsSource } from '@/services/aiAgentService'

const ChatContainer = styled.div<{ isOpen: boolean }>`
  position: fixed
  bottom: 20px
  right: 20px
  width: 400px
  height: ${props => props.isOpen ? '600px' : '0px'}
  background: var(--bg-secondary)
  border: 1px solid var(--accent-orange)
  border-radius: 4px
  display: flex
  flex-direction: column
  box-shadow: 0 4px 12px rgba(255, 140, 0, 0.2)
  transition: height 0.3s ease
  overflow: hidden
  z-index: 1000
`

const ChatHeader = styled.div`
  background: var(--accent-orange)
  color: var(--bg-primary)
  padding: 12px 16px
  font-weight: 600
  font-size: 13px
  display: flex
  justify-content: space-between
  align-items: center
  font-family: Monaco, Consolas, monospace
`

const CloseButton = styled.button`
  background: none
  border: none
  color: var(--bg-primary)
  cursor: pointer
  font-size: 16px
  padding: 0
  display: flex
  align-items: center

  &:hover {
    opacity: 0.8
  }
`

const MessagesContainer = styled.div`
  flex: 1
  overflow-y: auto
  padding: 16px
  display: flex
  flex-direction: column
  gap: 12px

  &::-webkit-scrollbar {
    width: 6px
  }

  &::-webkit-scrollbar-track {
    background: var(--bg-primary)
  }

  &::-webkit-scrollbar-thumb {
    background: var(--border-color)
    border-radius: 3px
  }
`

const Message = styled.div<{ isUser: boolean }>`
  display: flex
  gap: 10px
  align-items: flex-start
  align-self: ${props => props.isUser ? 'flex-end' : 'flex-start'}
  max-width: 85%
`

const MessageIcon = styled.div<{ isUser: boolean }>`
  width: 28px
  height: 28px
  border-radius: 50%
  background: ${props => props.isUser ? 'var(--color-info)' : 'var(--accent-orange)'}
  display: flex
  align-items: center
  justify-content: center
  color: var(--bg-primary)
  font-size: 14px
  flex-shrink: 0
`

const MessageBubble = styled.div<{ isUser: boolean }>`
  background: ${props => props.isUser ? 'var(--bg-tertiary)' : 'rgba(255, 140, 0, 0.1)'}
  border: 1px solid ${props => props.isUser ? 'var(--border-color)' : 'rgba(255, 140, 0, 0.3)'}
  border-radius: 8px
  padding: 10px 12px
  font-size: 11px
  line-height: 1.5
  color: var(--text-primary)
  font-family: Monaco, Consolas, monospace
  white-space: pre-wrap
  word-break: break-word
`

const SourcesContainer = styled.div`
  margin-top: 8px
  display: flex
  flex-direction: column
  gap: 6px
`

const SourceItem = styled.a`
  background: var(--bg-tertiary)
  border: 1px solid var(--border-color)
  border-radius: 4px
  padding: 6px 8px
  font-size: 9px
  color: var(--text-secondary)
  text-decoration: none
  display: flex
  align-items: center
  gap: 6px
  transition: all 0.2s

  &:hover {
    border-color: var(--accent-orange)
    color: var(--accent-orange)
  }
`

const SourceHeadline = styled.div`
  flex: 1
  white-space: nowrap
  overflow: hidden
  text-overflow: ellipsis
`

const InputContainer = styled.div`
  padding: 12px
  border-top: 1px solid var(--border-color)
  display: flex
  gap: 8px
`

const Input = styled.input`
  flex: 1
  background: var(--bg-tertiary)
  border: 1px solid var(--border-color)
  border-radius: 4px
  padding: 8px 12px
  color: var(--text-primary)
  font-size: 11px
  font-family: Monaco, Consolas, monospace

  &:focus {
    outline: none
    border-color: var(--accent-orange)
  }

  &::placeholder {
    color: var(--text-muted)
  }
`

const SendButton = styled.button`
  background: var(--accent-orange)
  border: none
  border-radius: 4px
  padding: 8px 16px
  color: var(--bg-primary)
  font-size: 14px
  cursor: pointer
  display: flex
  align-items: center
  gap: 6px
  font-weight: 600
  transition: opacity 0.2s

  &:hover {
    opacity: 0.9
  }

  &:disabled {
    opacity: 0.5
    cursor: not-allowed
  }
`

const FloatingButton = styled.button<{ isOpen: boolean }>`
  position: fixed
  bottom: 20px
  right: 20px
  width: 60px
  height: 60px
  border-radius: 50%
  background: var(--accent-orange)
  border: none
  color: var(--bg-primary)
  font-size: 24px
  cursor: pointer
  display: ${props => props.isOpen ? 'none' : 'flex'}
  align-items: center
  justify-content: center
  box-shadow: 0 4px 12px rgba(255, 140, 0, 0.3)
  transition: transform 0.2s
  z-index: 999

  &:hover {
    transform: scale(1.05)
  }
`

const LoadingDots = styled.div`
  display: flex
  gap: 4px
  padding: 8px 0

  span {
    width: 6px
    height: 6px
    border-radius: 50%
    background: var(--accent-orange)
    animation: bounce 1.4s infinite ease-in-out both

    &:nth-child(1) {
      animation-delay: -0.32s
    }

    &:nth-child(2) {
      animation-delay: -0.16s
    }
  }

  @keyframes bounce {
    0%, 80%, 100% {
      transform: scale(0)
    }
    40% {
      transform: scale(1)
    }
  }
`

interface AIChatProps {
  defaultOpen?: boolean
}

const AIChat: React.FC<AIChatProps> = ({ defaultOpen = false }) => {
  const [isOpen, setIsOpen] = useState(defaultOpen)
  const [messages, setMessages] = useState<ChatMessage[]>([])
  const [input, setInput] = useState('')
  const [loading, setLoading] = useState(false)
  const [sources, setSources] = useState<Record<number, NewsSource[]>>({})
  const messagesEndRef = useRef<HTMLDivElement>(null)

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' })
  }

  useEffect(() => {
    scrollToBottom()
  }, [messages])

  const handleSend = async () => {
    if (!input.trim() || loading) return

    const userMessage: ChatMessage = {
      role: 'user',
      content: input,
      timestamp: new Date().toISOString(),
    }

    setMessages(prev => [...prev, userMessage])
    setInput('')
    setLoading(true)

    try {
      const response = await aiAgentService.chat(input)

      const assistantMessage: ChatMessage = {
        role: 'assistant',
        content: response.answer,
        timestamp: new Date().toISOString(),
      }

      setMessages(prev => [...prev, assistantMessage])

      if (response.sources && response.sources.length > 0) {
        setSources(prev => ({
          ...prev,
          [messages.length + 1]: response.sources,
        }))
      }
    } catch (error) {
      const errorMessage: ChatMessage = {
        role: 'assistant',
        content: 'Sorry, I encountered an error processing your request. Please try again.',
        timestamp: new Date().toISOString(),
      }
      setMessages(prev => [...prev, errorMessage])
    } finally {
      setLoading(false)
    }
  }

  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault()
      handleSend()
    }
  }

  return (
    <>
      <FloatingButton isOpen={isOpen} onClick={() => setIsOpen(true)}>
        <RobotOutlined />
      </FloatingButton>

      <ChatContainer isOpen={isOpen}>
        <ChatHeader>
          <div>AI Market Assistant</div>
          <CloseButton onClick={() => setIsOpen(false)}>
            <CloseOutlined />
          </CloseButton>
        </ChatHeader>

        <MessagesContainer>
          {messages.length === 0 && (
            <MessageBubble isUser={false}>
              Hello! I'm your AI market assistant. Ask me about market news, stock analysis, or trading insights.
            </MessageBubble>
          )}

          {messages.map((message, index) => (
            <Message key={index} isUser={message.role === 'user'}>
              <MessageIcon isUser={message.role === 'user'}>
                {message.role === 'user' ? <UserOutlined /> : <RobotOutlined />}
              </MessageIcon>
              <div style={{ flex: 1 }}>
                <MessageBubble isUser={message.role === 'user'}>
                  {message.content}
                </MessageBubble>
                {message.role === 'assistant' && sources[index] && (
                  <SourcesContainer>
                    {sources[index].map((source, idx) => (
                      <SourceItem
                        key={idx}
                        href={source.url}
                        target="_blank"
                        rel="noopener noreferrer"
                      >
                        <LinkOutlined />
                        <SourceHeadline>{source.headline}</SourceHeadline>
                      </SourceItem>
                    ))}
                  </SourcesContainer>
                )}
              </div>
            </Message>
          ))}

          {loading && (
            <Message isUser={false}>
              <MessageIcon isUser={false}>
                <RobotOutlined />
              </MessageIcon>
              <LoadingDots>
                <span />
                <span />
                <span />
              </LoadingDots>
            </Message>
          )}

          <div ref={messagesEndRef} />
        </MessagesContainer>

        <InputContainer>
          <Input
            value={input}
            onChange={e => setInput(e.target.value)}
            onKeyPress={handleKeyPress}
            placeholder="Ask about market news..."
            disabled={loading}
          />
          <SendButton onClick={handleSend} disabled={loading || !input.trim()}>
            <SendOutlined />
          </SendButton>
        </InputContainer>
      </ChatContainer>
    </>
  )
}

export default AIChat
