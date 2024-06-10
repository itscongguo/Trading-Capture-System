# Trading Capture System (TCS)

A microservice-based financial trading platform built with Java, Spring Boot, Kafka, and Kubernetes.

## Architecture Overview

The Trading Capture System follows a **microservices architecture** with **event-driven design** using Kafka as the message backbone. It supports high-throughput trading, real-time order processing, and comprehensive risk management.

### Core Components

```
┌─────────────┐     ┌──────────────┐     ┌─────────────┐
│   React UI  │────▶│ API Gateway  │────▶│Order Service│
└─────────────┘     │   (Istio)    │     └──────┬──────┘
                    └──────────────┘            │
                                                 ▼
                                            ┌─────────┐
                                            │  Kafka  │
                                            └────┬────┘
                    ┌────────────────────────────┼────────────┐
                    ▼                            ▼            ▼
            ┌───────────────┐          ┌─────────────┐  ┌────────────┐
            │ Trade Engine  │          │Risk Service │  │Audit Service│
            └───────┬───────┘          └─────────────┘  └────────────┘
                    │
                    ▼
            ┌──────────────┐
            │ Notification │
            │   Service    │
            └──────────────┘
```

### Technology Stack

**Frontend**
- **Framework**: React 18 with TypeScript
- **Build Tool**: Vite
- **UI Library**: Ant Design with custom Bloomberg Terminal theme
- **State Management**: Zustand
- **Real-Time**: WebSocket for live updates
- **Charts**: Recharts for data visualization
- **HTTP Client**: Axios with JWT interceptors

**Backend**
- **Language**: Java 17
- **Framework**: Spring Boot 3, Spring Cloud
- **Messaging**: Apache Kafka (event-driven)
- **Databases**: PostgreSQL (orders, trades, risk), MongoDB (audit), Redis (cache/locks)
- **Container Orchestration**: Kubernetes
- **Service Mesh**: Istio
- **Observability**: Prometheus, Grafana
- **Build Tool**: Maven

## Services

### 1. API Gateway (Port 8080)
- Single entry point for all client requests
- JWT token validation and authentication
- Route management with Spring Cloud Gateway
- Circuit breaker and fallback handling
- CORS configuration

### 2. Auth Service (Port 8084)
- User authentication with JWT tokens
- Admin user: **username: `congguo`**, **password: `congguooo`**
- Token generation and validation
- Refresh token support
- Session management with Redis

### 3. Order Service (Port 8081)
- Accepts and validates orders from clients
- **Saga Pattern**: Long-running distributed transactions with compensation
- **Two-Phase Commit (2PC)**: Strongly consistent short transactions
- **<0.2% Rollback Rate**: Achieves high success rate under heavy concurrency
- Performs initial risk checks via Risk Service
- Publishes order events to Kafka
- Provides order query APIs
- Requires JWT authentication

**Distributed Transaction Architecture**:
- **Saga Orchestration**: Coordinates multi-step order workflows with automatic compensation
- **2PC Coordination**: Ensures ACID properties for critical operations (order persistence, ID reservation)
- **Compensation Handlers**: Automatic rollback on failures (risk quota release, order cancellation)
- **Retry Mechanism**: Exponential backoff with configurable max retries
- **Distributed Locks**: Redis-based locking to prevent concurrent conflicts

### 4. Risk Service (Port 8082)
- Real-time risk checks using Redis
- Manages user quotas and limits
- Account-level and symbol-level risk management
- Quota reservation and release

### 5. Trade Engine (Port 8083)
- Kafka consumer for order events
- Implements matching logic (price-time priority)
- Generates trade execution events
- Persists trade records

### 6. Notification Service (Port 8085)
- WebSocket server for real-time notifications
- Kafka consumer for order status and trade events
- Pushes updates to connected clients
- Session management for multiple users

### 7. Audit Service (Port 8086)
- Kafka consumer for all events
- Persists audit logs to MongoDB
- Event tracking and compliance
- Query APIs for audit history

### 8. Common Module
- Shared DTOs, enums, and utilities
- Kafka event schemas (Avro)
- Exception handling framework
- Constants and common configurations

## Prerequisites

- **Java 17** or later
- **Maven 3.8+**
- **Docker** and **Docker Compose**
- **Kubernetes** (optional, for production deployment)
- **kubectl** and **istioctl** (for K8s deployment)

## Quick Start - Local Development

### 1. Start Infrastructure Services

Start all required infrastructure (Kafka, PostgreSQL, MongoDB, Redis):

```bash
docker-compose up -d
```

This starts:
- PostgreSQL (ports 5432, 5433, 5434)
- MongoDB (port 27017)
- Redis (port 6379)
- Kafka (port 9092)
- Zookeeper (port 2181)
- Kafka UI (port 8090)
- Prometheus (port 9090)
- Grafana (port 3000)

### 2. Build All Services

```bash
mvn clean install -DskipTests
```

### 3. Run Services

Open multiple terminal windows and run each service:

**Terminal 1 - Auth Service:**
```bash
cd tcs-auth-service
mvn spring-boot:run
```

**Terminal 2 - API Gateway:**
```bash
cd tcs-api-gateway
mvn spring-boot:run
```

**Terminal 3 - Order Service:**
```bash
cd tcs-order-service
mvn spring-boot:run
```

**Terminal 4 - Risk Service:**
```bash
cd tcs-risk-service
mvn spring-boot:run
```

**Terminal 5 - Trade Engine:**
```bash
cd tcs-trade-engine
mvn spring-boot:run
```

**Terminal 6 - Notification Service:**
```bash
cd tcs-notification-service
mvn spring-boot:run
```

**Terminal 7 - Audit Service:**
```bash
cd tcs-audit-service
mvn spring-boot:run
```

### 4. Verify Services

Check service health:
```bash
# Order Service
curl http://localhost:8081/actuator/health

# Risk Service
curl http://localhost:8082/actuator/health

# Trade Engine
curl http://localhost:8083/actuator/health
```

## Testing the System

### Step 1: Login to Get JWT Token

Login with the admin user:

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "congguo",
    "password": "congguooo"
  }'
```

Response will include an `accessToken`. Save this token for subsequent requests.

Example response:
```json
{
  "accessToken": "eyJhbGc...",
  "refreshToken": "eyJhbGc...",
  "tokenType": "Bearer",
  "expiresIn": 86400,
  "userInfo": {
    "userId": "admin-user-001",
    "username": "congguo",
    "accountId": "admin-account-001",
    "role": "ADMIN"
  }
}
```

### Step 2: Submit an Order

Use the access token from login:

```bash
export TOKEN="your_access_token_here"

curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "symbol": "AAPL",
    "side": "BUY",
    "type": "LIMIT",
    "quantity": 100,
    "price": 180.50,
    "timeInForce": "GTC",
    "accountId": "admin-account-001"
  }'
```

### Step 3: Query Order Status

```bash
curl http://localhost:8080/api/orders/{orderId} \
  -H "Authorization: Bearer $TOKEN"
```

### Step 4: View User Orders

```bash
curl http://localhost:8080/api/orders \
  -H "Authorization: Bearer $TOKEN"
```

### Step 5: Connect to WebSocket for Real-time Updates

Connect to WebSocket endpoint to receive real-time order updates:

```javascript
const ws = new WebSocket('ws://localhost:8085/ws/orders/admin-user-001');

ws.onmessage = function(event) {
  const notification = JSON.parse(event.data);
  console.log('Received notification:', notification);
};
```

Notifications will be sent for:
- Order status changes (PENDING → FILLED, REJECTED, etc.)
- Trade executions

## Monitoring

### Kafka UI
View Kafka topics and messages:
- URL: http://localhost:8090

### Prometheus
View metrics:
- URL: http://localhost:9090

### Grafana
View dashboards:
- URL: http://localhost:3000
- Username: `admin`
- Password: `admin123`

## Kubernetes Deployment

### Prerequisites
- Kubernetes cluster (EKS, GKE, AKS, or local Minikube)
- Istio installed on the cluster
- kubectl configured

### 1. Create Namespace

```bash
kubectl apply -f k8s/namespace.yaml
```

### 2. Create Secrets

```bash
kubectl create secret generic order-service-secret \
  --from-literal=DB_USERNAME=tcs_user \
  --from-literal=DB_PASSWORD=tcs_password \
  -n tcs

kubectl create secret generic risk-service-secret \
  --from-literal=DB_USERNAME=tcs_user \
  --from-literal=DB_PASSWORD=tcs_password \
  -n tcs

kubectl create secret generic trade-engine-secret \
  --from-literal=DB_USERNAME=tcs_user \
  --from-literal=DB_PASSWORD=tcs_password \
  -n tcs
```

### 3. Deploy Services

```bash
kubectl apply -f k8s/order-service-deployment.yaml
kubectl apply -f k8s/risk-service-deployment.yaml
kubectl apply -f k8s/trade-engine-deployment.yaml
```

### 4. Deploy Istio Gateway

```bash
kubectl apply -f k8s/istio-gateway.yaml
```

### 5. Verify Deployment

```bash
kubectl get pods -n tcs
kubectl get svc -n tcs
kubectl get gateway -n tcs
```

## Order Processing Flow

1. **Order Submission**: User submits order via API Gateway
2. **Validation**: Order Service validates request
3. **Risk Check**: Synchronous call to Risk Service
   - Checks notional limit
   - Checks position limit
   - Checks order count limit
   - Reserves quota in Redis
4. **Persistence**: Order saved to PostgreSQL with status `PENDING`
5. **Kafka Event**: `OrderCreated` event published to `orders` topic
6. **Trade Matching**: Trade Engine consumes event and attempts matching
7. **Execution**: On successful match:
   - Trade record created
   - `TradeExecuted` event published to `trades` topic
   - `OrderUpdated` event published to `order-status` topic
8. **Notification**: User notified via WebSocket (future enhancement)
9. **Audit**: All events logged to MongoDB (future enhancement)

## Configuration

### Application Properties

Each service has its own `application.yml` with environment-specific configurations:

- `DB_HOST`, `DB_PORT`, `DB_NAME`: Database connection
- `KAFKA_BOOTSTRAP_SERVERS`: Kafka brokers
- `REDIS_HOST`, `REDIS_PORT`: Redis connection
- Service-specific ports and settings

### Environment Variables

Set these in your environment or Docker Compose:

```bash
export DB_HOST=localhost
export DB_PORT=5432
export KAFKA_BOOTSTRAP_SERVERS=localhost:9092
export REDIS_HOST=localhost
```

## Performance Targets

- **Throughput**: 5,000+ orders/second sustained
- **Latency (P95)**:
  - POST /orders: < 80ms
  - GET /orders/{id}: < 30ms
- **Availability**: 99.9% for core services
- **Kafka**: Replication factor ≥ 3, `acks=all`

## Database Schemas

### Orders Table (tcs_orders database)
```sql
- order_id (PK, unique)
- user_id, account_id
- symbol, side, type
- quantity, price
- status, filled_quantity, avg_price
- created_at, updated_at
```

### Trades Table (tcs_trades database)
```sql
- trade_id (PK, unique)
- order_id
- symbol, side, quantity, price
- executed_at
```

### Risk Limits Table (tcs_risk database)
```sql
- user_id, account_id
- symbol (nullable for account-level)
- notional_limit, position_limit, order_count_limit
```

## Kafka Topics

- `orders`: Order creation events
- `order-status`: Order status updates
- `trades`: Trade execution events
- `risk-events`: Risk decision events
- `audit-events`: Audit logs

## Troubleshooting

### Services won't start
- Verify all infrastructure is running: `docker-compose ps`
- Check database connections
- Review logs: `docker-compose logs -f <service>`

### Kafka connection errors
- Ensure Kafka is healthy: `docker-compose logs kafka`
- Verify Kafka topics exist: Access Kafka UI at http://localhost:8090

### Database migration failures
- Check Flyway migration scripts
- Manually connect to PostgreSQL and verify schema

### Order rejected by risk service
- Verify risk limits in `risk_limits` table
- Check Redis quota keys: `redis-cli keys "quota:*"`

## Development

### Running Tests

```bash
# Run all tests
mvn test

# Run specific module
cd tcs-order-service
mvn test
```

### Building Docker Images

```bash
# Build all services
mvn clean package -DskipTests

# Build Docker images (add Dockerfile to each service)
docker build -t tcs/order-service:1.0.0-SNAPSHOT tcs-order-service/
docker build -t tcs/risk-service:1.0.0-SNAPSHOT tcs-risk-service/
docker build -t tcs/trade-engine:1.0.0-SNAPSHOT tcs-trade-engine/
```

## Implemented Features

**Backend Services**
- [x] Auth Service with JWT authentication (admin user: congguo/congguooo)
- [x] API Gateway with Spring Cloud Gateway
- [x] WebSocket Notification Service for real-time updates
- [x] Audit Service with MongoDB for compliance logging
- [x] Order Service with validation and risk integration
- [x] Risk Service with Redis-backed quota management
- [x] Trade Engine with matching logic
- [x] Complete Kafka event-driven architecture
- [x] Docker Compose for local development
- [x] Kubernetes deployment manifests with Istio

**Frontend Application**
- [x] Bloomberg Terminal-style React UI with TypeScript
- [x] Professional login page with JWT authentication
- [x] Multi-panel trading dashboard layout
- [x] Order entry form with Buy/Sell functionality
- [x] Real-time order list with status tracking
- [x] WebSocket integration for live notifications
- [x] Price visualization with animated charts
- [x] Responsive design with custom Bloomberg theme
- [x] Docker and Nginx production deployment

## Getting Started - Frontend

### Prerequisites

```bash
Node.js 18+
npm or yarn
```

### Running the Frontend

```bash
cd tcs-frontend

# Install dependencies
npm install

# Set environment variables
cp .env.example .env

# Start development server (http://localhost:3000)
npm run dev

# Build for production
npm run build

# Login credentials
# Username: congguo
# Password: congguooo
```

### Frontend Features

- **Bloomberg Terminal Theme**: Professional dark UI with orange accents (#FF8C00)
- **Real-Time Updates**: WebSocket connection for order status and trade notifications
- **Order Management**: Create, view, and cancel orders in real-time
- **Type Safety**: Full TypeScript implementation
- **Responsive Layout**: Multi-panel grid optimized for trading workflow

## Future Enhancements

- [ ] Balance/Settlement Service with transaction handling
- [ ] Advanced matching algorithms (FIFO, Pro-Rata, etc.)
- [ ] Market data integration and real-time price feeds
- [ ] Position management and P&L calculation
- [ ] Reporting and analytics dashboard
- [ ] Multi-user support beyond admin
- [ ] OAuth2 integration with external providers
- [ ] Advanced monitoring and alerting dashboards
- [ ] Order book depth visualization
- [ ] Trade blotter and execution quality analytics

## License

Proprietary - Trading Capture System

## Contact

For questions or support, contact haoconghe116@gmail.com.
