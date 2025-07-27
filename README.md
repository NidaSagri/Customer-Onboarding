# OFSS Bank - Customer Onboarding Microservices

This project is a complete, full-stack digital customer onboarding platform for a modern bank, built using a cloud-native microservices architecture. It features a secure backend with distinct roles, a rich web UI for both administrators and customers, and an intelligent AI assistant for conversational queries.

 <!-- It's highly recommended to replace this with a real screenshot of your app -->

## Features

*   **Microservices Architecture:** Scalable and resilient backend built with Spring Boot.
*   **Service Discovery:** Netflix Eureka for dynamic registration and discovery of services.
*   **Centralized Routing:** Spring Cloud Gateway as a single, secure entry point for all requests.
*   **Role-Based Security:** Distinct UI and API access for **Admins** and **Customers** using Spring Security.
*   **Digital KYC Process:** Customers upload ID documents (Aadhaar, PAN) and a photo during registration.
*   **Admin Verification Workflow:** A dedicated admin dashboard to review new customers, view uploaded documents, and approve/reject KYC applications.
*   **Asynchronous Notifications:** Apache Kafka and Spring Mail for sending real-time email notifications for registration and KYC status changes.
*   **Intelligent AI Assistant:** An admin-facing chatbot powered by Google Gemini that can answer natural language questions about customer data and dashboard statistics.
*   **Modern Frontend:** A professional, responsive UI built with Thymeleaf and modern CSS, featuring interactive elements and a premium document viewer.
*   **Persistent Data:** All data is stored in an Oracle database.

## Technology Stack

| Category              | Technology                                         |
| --------------------- | -------------------------------------------------- |
| **Backend**           | Java 17, Spring Boot 3, Spring Cloud, Spring Security, Spring Data JPA |
| **Database**          | Oracle Database                                    |
| **Messaging**         | Apache Kafka, Apache ZooKeeper                     |
| **Service Discovery** | Netflix Eureka Server                              |
| **API Gateway**       | Spring Cloud Gateway                               |
| **AI Assistant**      | Python 3, Flask, Google Gemini Pro                 |
| **Frontend**          | Thymeleaf, HTML5, CSS3, JavaScript                 |
| **Build Tool**        | Apache Maven                                       |

## Architecture Diagram

```
+----------------+      +-----------------+      +---------------------+
|                |      |                 |      |                     |
|   Web Browser  +----->+   API Gateway   +----->+   Customer Service  |
| (UI/Postman)   |      |  (Port 8080)    |      | (Port 8081)         |
|                |      |                 |      | - UI, Security, Users |
+----------------+      +-------+---------+      +----------+----------+
                                |                      /|\   |
                                |                       |    | (Feign)
+----------------+      +-------+---------+             |    |
|                |      |                 |      +----------+----------+
|  AI Assistant  +----->+ Python / Flask  |      |                     |
| (Admin UI)     |      |  (Port 5000)    |      |    Account Service  |
|                |      |                 |      | (Port 8082)         |
+----------------+      +-----------------+      | - Accounts Logic    |
                                                 +---------------------+
                                                          /|\
                                                           |
                                                +----------+----------+
                                                |                     |
                                                |    Eureka Server    |
                                                |    (Port 8761)      |
                                                |                     |
                                                +---------------------+

+-----------------+      +-----------------+      +---------------------+
| Oracle Database |<-----+ Customer Service|      |   Customer Service  +------>+  Apache Kafka   |
|                 |<-----+  Account Service|      |                     |       | (Port 9092)     |
+-----------------+      +-----------------+      +---------------------+       +-----------------+
```

---
## Getting Started

### Prerequisites

*   **Java JDK 17+**
*   **Apache Maven 3.8+**
*   **Python 3.8+**
*   **Apache Kafka & ZooKeeper:** Installed and accessible.
*   **Oracle Database:** An instance running with a user/schema created for this application.
*   **Google Gemini API Key**.

### 1. How to get a Google Gemini API Key

1.  Go to **[Google AI Studio](https://aistudio.google.com/app/apikey)**.
2.  Sign in with your Google account.
3.  Click **"Create API key in new project"**.
4.  Copy the generated API key. You will need it for the Python server setup.

### 2. Configuration

#### a) Backend Services (`customer-service` & `account-service`)
In both modules, open the `src/main/resources/application.properties` file and configure your database and email credentials.

**Oracle Database:**
```properties
spring.datasource.url=jdbc:oracle:thin:@localhost:1521:orcl
spring.datasource.username=your_oracle_username
spring.datasource.password=your_oracle_password
```

**Spring Mail (for `customer-service`):**
*   **Important:** You must generate a 16-character **App Password** from your Google Account's security settings (2-Step Verification must be enabled).
```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your_16_character_app_password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

#### b) Python AI Assistant
1.  Navigate to the `python` directory.
2.  Create a Python virtual environment: `python -m venv venv`
3.  Activate it: `source venv/bin/activate` (macOS/Linux) or `.\venv\Scripts\activate` (Windows).
4.  Install dependencies: `pip install -r requirements.txt`
5.  Set your Gemini API Key as an environment variable:
    ```bash
    # macOS/Linux
    export GOOGLE_API_KEY="YOUR_API_KEY_HERE"

    # Windows (Command Prompt)
    set GOOGLE_API_KEY="YOUR_API_KEY_HERE"
    ```

### 3. How to Run the Application (Order is CRITICAL)

You will need **seven** separate terminal windows.

1.  **Terminal 1 (ZooKeeper):** Navigate to your Kafka directory and run:
    `bin/zookeeper-server-start.sh config/zookeeper.properties` (or the Windows `.bat` equivalent).

2.  **Terminal 2 (Kafka Broker):** Navigate to your Kafka directory and run:
    `bin/kafka-server-start.sh config/server.properties` (or `.bat`).

3.  **Terminal 3 (Build Project):** Navigate to the root of the parent Maven project (`onboarding-project`) and run a full clean build:
    `./mvnw clean install`

4.  **Terminal 3 (Eureka Server):** After the build succeeds, run:
    `./mvnw spring-boot:run -pl eureka-server`

5.  **Terminal 4 (Customer Service):** In a new terminal, run:
    `./mvnw spring-boot:run -pl customer-service`

6.  **Terminal 5 (Account Service):** In a new terminal, run:
    `./mvnw spring-boot:run -pl account-service`

7.  **Terminal 6 (API Gateway):** In a new terminal, run:
    `./mvnw spring-boot:run -pl api-gateway`

8.  **Terminal 7 (Python AI Assistant):** Navigate to the `python` directory, activate your virtual environment, and run:
    `python app.py`

**Your system is now fully operational!**

---
## Application Endpoints

All UI and most API traffic goes through the **API Gateway** at `http://localhost:8080`.

### UI Endpoints (Access via Browser)

*   **Login Page:** `http://localhost:8080/login`
    *   **Admin:** `admin` / `password`
    *   **Customer:** Credentials created during registration.
*   **Registration Page:** `http://localhost:8080/ui/register`
*   **Admin Dashboard:** `http://localhost:8080/admin/dashboard` (Accessible after admin login)
*   **Customer Dashboard:** `http://localhost:8080/customer/dashboard` (Accessible after customer login)

### Postman API Endpoints

#### Collection Setup
*   Create a collection and set its **Authorization** to **Basic Auth** (`admin` / `password`). This will be used for all admin-protected endpoints.

#### Customer Onboarding
*   **Register New Customer** (`POST`) `http://localhost:8080/api/customers/register`
    *   **Authorization:** No Auth
    *   **Body (raw, JSON):** (Requires full `CustomerRegistrationRequest` object with Base64 image strings)

#### Admin Actions
*   **Verify/Reject KYC** (`POST`) `http://localhost:8080/api/kyc/{customerId}/verify`
    *   **Authorization:** Inherit from parent (Basic Auth).
    *   **Body (raw, JSON):** `{"isVerified": true}` or `{"isVerified": false}`
*   **Delete Customer** (`POST`) `http://localhost:8080/admin/customer/{customerId}/delete`
    *   **Authorization:** Inherit from parent.

#### AI Assistant (Directly to Python Server)
*   **Admin Chat** (`POST`) `http://localhost:5000/chat`
    *   **Authorization:** No Auth.
    *   **Body (raw, JSON):**
        ```json
        {
            "query": "how many kyc are pending?",
            "role": "ADMIN",
            "history": []
        }
        ```
---
## Project Structure

The project is organized into a multi-module Maven project. Each directory in the root is a self-contained microservice with its own responsibilities.

```
onboarding-project/
├── 📂 eureka-server/
│   └── The service registry. All other microservices register here to be discoverable.
│
├── 📂 api-gateway/
│   └── The single entry point for all external traffic. It routes requests to the
│       appropriate downstream service based on URL paths.
│
├── 📂 customer-service/
│   └── The largest and most central service. It is responsible for:
│       ├── model/         (Customer, User, Role entities)
│       ├── repository/    (JPA repositories for customer data)
│       ├── service/       (Business logic for registration, KYC, security, notifications)
│       ├── controller/    (Handles all UI and API requests related to customers)
│       ├── security/      (Spring Security configuration and user details service)
│       ├── feign/         (Feign clients for communicating with other services)
│       ├── config/        (Kafka and other configurations)
│       └── resources/
│           ├── static/    (CSS, JS, and image files)
│           └── templates/ (All Thymeleaf HTML pages for the UI)
│
├── 📂 account-service/
│   └── A specialized service responsible only for bank accounts.
│       ├── model/         (Account entity)
│       ├── repository/    (JPA repository for accounts)
│       ├── service/       (Business logic for creating and activating accounts)
│       └── controller/    (Internal API controllers for service-to-service calls)
│
├── 📂 python/
│   └── The AI Assistant. A Flask server that interprets natural language,
│       calls the Spring Boot backend APIs, and provides intelligent responses.
│
└── 📄 pom.xml
    └── The parent Maven project file that manages shared dependencies and modules.
```

## Workflow Overview

1.  A **new user** visits the **UI** (`http://localhost:8080/ui/register`) served by the **API Gateway**, which routes the request to the **`customer-service`**.
2.  The user fills out the form, including uploading documents. On submission, the `customer-service` saves the `Customer` and `User` to the **Oracle DB**, calls the `account-service` via **Feign Client** to create an `INACTIVE` account, and publishes a `NewCustomerEvent` to **Kafka**.
3.  The `EmailNotificationService` (a Kafka consumer in `customer-service`) receives the event and sends an **email** to the admin.
4.  An **admin** logs in and views the new customer on their dashboard. They review the details and uploaded documents.
5.  The admin clicks "Approve KYC". This request hits the `customer-service`, which updates the customer's status, calls the `account-service` via **Feign Client** to set the account status to `ACTIVE`, and publishes a `KycStatusUpdateEvent` to **Kafka**.
6.  The `EmailNotificationService` receives this new event and sends a **welcome email** to the customer with their now-active account details.
7.  At any time, the **admin** can use the **AI Assistant**. Their natural language query is sent to the **Python/Flask** server. The Python server calls the secure `/api/chatbot` endpoints (routed by the **API Gateway** to the `customer-service`) to fetch data, which it then formats into an intelligent, human-readable response.

## Conclusion

This project serves as a comprehensive, end-to-end example of a modern, cloud-native application. It demonstrates key enterprise patterns including microservice decomposition, service discovery, asynchronous messaging, role-based security, and the integration of an intelligent AI layer. It is a robust foundation that can be extended with further banking functionalities.
