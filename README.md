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

- **Backend**: Java 17, Spring Boot 3, Spring Cloud
- **Messaging**: Apache Kafka (event-driven)
- **Databases**: PostgreSQL (orders, trades, risk), MongoDB (audit), Redis (cache/locks)
- **Container Orchestration**: Kubernetes
- **Service Mesh**: Istio
- **Observability**: Prometheus, Grafana
- **Build Tool**: Maven

## Services

### 1. Order Service (Port 8081)
- Accepts and validates orders from clients
- Performs initial risk checks via Risk Service
- Publishes order events to Kafka
- Provides order query APIs

### 2. Risk Service (Port 8082)
- Real-time risk checks using Redis
- Manages user quotas and limits
- Account-level and symbol-level risk management
- Quota reservation and release

### 3. Trade Engine (Port 8083)
- Kafka consumer for order events
- Implements matching logic (price-time priority)
- Generates trade execution events
- Persists trade records

### 4. Common Module
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

Open 3 terminal windows and run each service:

**Terminal 1 - Order Service:**
```bash
cd tcs-order-service
mvn spring-boot:run
```

**Terminal 2 - Risk Service:**
```bash
cd tcs-risk-service
mvn spring-boot:run
```

**Terminal 3 - Trade Engine:**
```bash
cd tcs-trade-engine
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

### Submit an Order

```bash
curl -X POST http://localhost:8081/api/orders \
  -H "Content-Type: application/json" \
  -H "X-User-Id: test-user-1" \
  -H "X-Trace-Id: test-trace-$(date +%s)" \
  -d '{
    "symbol": "AAPL",
    "side": "BUY",
    "type": "LIMIT",
    "quantity": 100,
    "price": 180.50,
    "timeInForce": "GTC",
    "accountId": "test-account-1"
  }'
```

### Query Order Status

```bash
curl http://localhost:8081/api/orders/{orderId}
```

### View User Orders

```bash
curl http://localhost:8081/api/orders \
  -H "X-User-Id: test-user-1"
```

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

## Future Enhancements

- [ ] WebSocket Notification Service
- [ ] Audit Service with MongoDB
- [ ] Auth Service with OAuth2/JWT
- [ ] API Gateway with Spring Cloud Gateway
- [ ] Frontend React UI
- [ ] Balance/Settlement Service
- [ ] Advanced matching algorithms
- [ ] Market data integration
- [ ] Position management
- [ ] Reporting and analytics

## License

Proprietary - Trading Capture System

## Contact

For questions or support, contact the TCS development team.