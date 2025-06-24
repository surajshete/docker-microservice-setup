# 🧩 Microservice Architecture - Spring Boot + Docker + Kafka + Keycloak

This project demonstrates a production-grade **Spring Boot Microservices** system orchestrated with **Docker Compose**, incorporating components like **Kafka**, **Keycloak**, **PostgreSQL**, **MongoDB**, and **Zipkin** for tracing.

---

## 📦 Project Structure

```plaintext
microservice-new/
├── api-gateway/
├── discovery-server/
├── inventory-service/
├── order-service/
├── product-service/
├── notification-service/
├── docker-compose.yml
├── wait-for-it.sh
└── README.md

````

---

## 🧰 Tech Stack

* **Spring Boot 3.x**
* **Spring Cloud (Eureka, Config)**
* **PostgreSQL (Order & Inventory DBs)**
* **MongoDB (Product DB)**
* **Kafka + Zookeeper** (Messaging)
* **Keycloak + MySQL** (Authentication & Authorization)
* **Zipkin** (Distributed Tracing)
* **Docker Compose** (Orchestration)

---

## 🧪 Services Overview

### 1. 🧾 `order-service`

* Handles order placement and tracking.
* Communicates with `inventory-service` and publishes to Kafka.
* Database: **PostgreSQL** (`postgres-order:6431`)

### 2. 📦 `inventory-service`

* Verifies and reserves stock.
* Supports stock rollbacks.
* Database: **PostgreSQL** (`postgres-inventory:6432`)

### 3. 🛍 `product-service`

* Manages product catalog.
* Database: **MongoDB** (`mongo:27017`)

### 4. 🔔 `notification-service`

* Listens to Kafka topic (`orderTopic`) and processes notifications.

### 5. 🚪 `api-gateway`

* Central entry point for external requests.
* Handles routing and integrates with Keycloak for security.

### 6. 🔍 `discovery-server`

* **Eureka server** to enable service registration and discovery.

### 7. 🔐 `keycloak`

* Identity and Access Management (IAM).
* Uses **MySQL** (`keycloak-mysql`) as the database.
* Realm and clients are imported at startup.

### 8. 🧵 `kafka + zookeeper`

* Backbone for async communication.
* Used for publishing order events and notifications.

### 9. 📊 `kafka-ui`

* Kafka web interface to monitor topics, producers, and consumers.

### 10. 🔭 `zipkin`

* Collects and visualizes distributed tracing data.

---

## ⚙️ How to Run

### 🔁 Pre-requisites

* Docker & Docker Compose
* Java 21
* Maven

### ▶️ Start All Services

```bash
docker-compose up --build
```

This will spin up all microservices and infrastructure containers.

---

## 🌐 Service Endpoints

| Service                | URL                                            |
| ---------------------- | ---------------------------------------------- |
| API Gateway            | [http://localhost:9191](http://localhost:9191) |
| Eureka Discovery       | [http://localhost:9761](http://localhost:9761) |
| Zipkin Tracing         | [http://localhost:9411](http://localhost:9411) |
| Kafka UI               | [http://localhost:8085](http://localhost:8085) |
| Keycloak Admin Console | [http://localhost:8080](http://localhost:8080) |

---

## 🧩 Ports & Databases

| Service             | Port  | Database   |
| ------------------- | ----- | ---------- |
| `order-service`     | 6431  | PostgreSQL |
| `inventory-service` | 6432  | PostgreSQL |
| `product-service`   | 27017 | MongoDB    |
| `keycloak-mysql`    | 3306  | MySQL      |

---

## 🔑 Keycloak Configuration

* Admin Username: `admin`
* Admin Password: `admin`
* Realm and client configurations are auto-imported from `./realms/`.

---

## 📢 Kafka Topics

* **orderTopic**: Used by `order-service` to notify `notification-service`.

---

## 🩺 Health Check

Spring Actuator is enabled on all services.

Example:

```
GET http://<service-host>:<port>/actuator/health
```

---

## 🧼 Clean Up

To stop and remove all containers:

```bash
docker-compose down -v
```

---

## 📌 TODOs

* [ ] Add Prometheus + Grafana for metrics.
* [ ] Add email provider integration to `notification-service`.
* [ ] Rate-limiting and throttling on `api-gateway`.

---

## 🤝 Contribution

Pull requests and issues are welcome. Let’s build together!

---

## 📝 License

This project is licensed under the MIT License.

```

---

Let me know if you'd like a separate `CONTRIBUTING.md` or example API usage section added.
```
