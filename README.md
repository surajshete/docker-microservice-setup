
# 🧩 Dockerized Microservice Architecture with Spring Boot

This project demonstrates a complete **Spring Boot Microservices architecture** powered by **Docker Compose**, including essential systems for:

- 🧾 Order and Inventory Management
- 🛡️ Authentication with Keycloak
- 📦 Messaging with Kafka
- 📡 Distributed Tracing with Zipkin
- 📊 Monitoring via Kafka UI
- ☁️ Eureka Discovery
- 🌐 API Gateway

---

## 🔧 Tech Stack

| Layer              | Technology                  |
|-------------------|-----------------------------|
| API Gateway       | Spring Cloud Gateway        |
| Service Registry  | Netflix Eureka              |
| Auth Service      | Keycloak (with MySQL)       |
| Messaging         | Apache Kafka + Zookeeper    |
| Tracing           | Zipkin                      |
| Databases         | PostgreSQL, MongoDB, MySQL  |
| Inter-service Comm| WebClient (Spring WebFlux)  |
| Circuit Breaking  | Resilience4j                |
| Containerization  | Docker + Docker Compose     |

---

## 🧱 Microservices

| Service              | Description                                                                 |
|----------------------|-----------------------------------------------------------------------------|
| `discovery-server`   | Eureka server for registering and discovering services                      |
| `api-gateway`        | Gateway to route and secure API traffic                                     |
| `order-service`      | Handles order creation and persistence (PostgreSQL)                         |
| `inventory-service`  | Manages stock validation and updates (PostgreSQL)                           |
| `product-service`    | Maintains product catalog (MongoDB)                                         |
| `notification-service`| Sends asynchronous notifications via Kafka                                 |

---

## 📦 Supporting Services

| Service            | Description                                             |
|--------------------|---------------------------------------------------------|
| `postgres-order`   | Database for `order-service`                            |
| `postgres-inventory`| Database for `inventory-service`                      |
| `mongo`            | NoSQL DB for `product-service`                          |
| `keycloak`         | Identity and Access Management                          |
| `keycloak-mysql`   | Backend DB for Keycloak                                 |
| `broker`           | Apache Kafka broker                                     |
| `zookeeper`        | Kafka's coordination service                            |
| `kafka-ui`         | Web UI to monitor Kafka topics and messages             |
| `zipkin`           | Distributed tracing dashboard                           |

---

## 🚀 Getting Started

### 🛠️ Prerequisites

- Docker & Docker Compose
- Java 21
- Maven

### ▶️ Run the entire stack

```bash
git clone https://github.com/surajshete/docker-microservice-setup.git
cd docker-microservice-setup
docker-compose up --build
````

> Ensure the port `9761` (Eureka), `9191` (API Gateway), `8080` (Keycloak), and `9411` (Zipkin) are free.

---

## 🌐 Service Access Points

| Service          | URL                                            |
| ---------------- | ---------------------------------------------- |
| Eureka Dashboard | [http://localhost:9761](http://localhost:9761) |
| API Gateway      | [http://localhost:9191](http://localhost:9191) |
| Keycloak Admin   | [http://localhost:8080](http://localhost:8080) |
| Zipkin Tracing   | [http://localhost:9411](http://localhost:9411) |
| Kafka UI         | [http://localhost:8085](http://localhost:8085) |

---

## 📌 Keycloak Setup

* Username: `admin`
* Password: `admin`
* Realm: Loaded from `./realms/`
* Import realm automatically using Docker volume mount

---

## 📜 Kafka Topics

* `orderTopic`
* `notificationTopic`

These topics are used by `order-service` and `notification-service`.

---

## ⚙️ Configuration

Each service uses its own `application.yml` or `application-docker.properties` file. Environment variables are injected using `SPRING_PROFILES_ACTIVE=docker`.

---

## 📂 Project Structure

```
docker-microservice-setup/
├── api-gateway/
├── discovery-server/
├── inventory-service/
├── order-service/
├── product-service/
├── notification-service/
├── docker-compose.yml
├── wait-for-it.sh
└── README.md
```

---

## 📈 Observability

Each service includes Micrometer + Zipkin tracing. You can track API calls via the Zipkin UI once services are running.

---
## ✅ TODOs

- ✅ Support bulk order placement in `order-service`
- ✅ Fetch product price in `order-service`, calculate total, and return price as string
- ⬜ Integrate Prometheus and Grafana for monitoring
- ⬜ Add email provider support in `notification-service`
- ⬜ Implement rate-limiting in `api-gateway`
- ⬜ Add a short video demo or README with screenshots.
- ⬜ Include unit tests and basic API docs (Swagger, Postman collection).

---
## 🤝 Contributing

Feel free to fork the repo, raise issues or submit PRs for new features, bug fixes, or improvements!

---
