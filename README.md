# Explore with me - a service for finding companions for events.

Explore with me is a multimodule backend service developed with layered architecture, as part of the Yandex
Practicum - Java developer program.
The application allows users to create events, manage user accounts, handle participation requests, add comments, also
provides categories and compilations.
---

## Technologies Used

- **Java 21**
- **Spring Boot** – Framework
- **Spring Data JPA** – Persistence layer abstraction
- **Spring Cloud Eureka** – Service discovery that lets microservices register and find each other dynamically
- **Spring Cloud Config Server** – Centralized configuration service for managing properties across distributed
  environments
- **Spring Retry** – Provides automatic retry logic for failed operations in Spring applications
- **Hibernate** – ORM for relational database mapping
- **H2 / PostgreSQL** – Databases for development and production
- **Bean Validation** – Input validation
- **Feign Client** – A declarative HTTP client for simplifying communication between microservices
- **Resilience4j Circuit Breaker** – Fault tolerance library enabling circuit breaker patterns to handle failures
  gracefully
- **Lombok** – Reduces boilerplate code in Java by automatically generating common methods through annotations
- **MapStruct** - Implementation of mappings
- **Maven** – Project modules and dependency management
- **Docker / Docker Compose** – Containerized local deployment

---

## Start Procedure

- **Start DB servers**
  - **docker-compose/stats-db**
  - **docker-compose/event-db**
  - **docker-compose/microservices-db**
  - **`stats-db` and `event-db` are initialized using `schema.sql` located in `/resources/schema.sql` of each module**
  - **`microservices-db` is initialized using `schema.sql` located in `/db/schema.sql`**

---

- **Start Spring Cloud applications**
  - **infra/discovery-server**
  - **infra/config-server**
  - **wait 10-15 seconds to ensure the config server is registered in Eureka**

---

- **Start applications**
  - **stats/stats-server**
  - **core/user-service**
  - **core/request-service**
  - **core/comment-service**
  - **core/event-service**
  - **wait 10–15 seconds to ensure all services are registered in Eureka and can discover each other via the load
    balancer**

---

## Microservices Interaction

- **feign-clients**
  - clients are located in `core/interaction-api/src/main/java/ru/yandex/practicum/interaction/api/feign`
  - client discovers services by name via the Eureka load balancer. Basic Fallback classes have been added, returning
    default values.
  - integrated with Spring Retry for automatic retries and Resilience4j Circuit Breaker for fault tolerance.

---

- **event-service**
  - uses `user-service` via `UserClient.java`
  - uses `comment-service` via `CommentClient.java`
  - uses `request-service` via `RequestClient.java`

---

- **comment-service**
  - uses `user-service` via `UserClient.java`
  - uses `event-service` via `EventClient.java`
  - uses `request-service` via `RequestClient.java`

---

- **request-service**
  - uses `user-service` via `UserClient.java`
  - uses `event-service` via `EventClient.java`

---

## Mappings

- **Category Management (Admin)**
    - POST `/admin/categories` -> create a new category
    - PATCH `/admin/categories/{id}` -> update an existing category by ID
    - DELETE `/admin/categories/{id}` -> delete a category by ID

- **Category Management (Public)**
    - GET `/categories` -> Get paginated list of categories (`from`, `size`)
    - GET `/categories/{id}` -> Get details of a category by ID

---

- **Compilation Management (Admin)**
    - POST `/admin/compilations` -> create a new compilation
    - PATCH `/admin/compilations/{compId}` -> update an existing compilation by ID
    - DELETE `/admin/compilations/{compId}` -> delete a compilation by ID

- **Compilation Management (Public)**
    - GET `/compilations` -> get a paginated list of compilations (filer by `pinned`, `from`, `size`)
    - GET `/compilations/{compId}` -> get details of a compilation by ID

---

- **Event Management (Internal)**
    - GET `/internal/events/{eventId}` -> retrieve event details by ID for internal services; **tracks access statistics
      **

- **Event Management (Admin)**
    - GET `/admin/events` -> search events with filters (`users`, `states`, `categories`, date `rangeStart`, `rangeEnd`)
      and pagination (`from`, `size`)
    - PATCH `/admin/events/{eventId}` -> update an existing event by ID with `UpdateEventAdminRequestDto`

- **Event Management (Private)**
    - GET `/users/{userId}/events/{eventId}` -> get details of a specific event belonging to the user
    - GET `/users/{userId}/events` -> get a paginated list of the user's events (from, size)
    - GET `/users/{userId}/events/{eventId}/requests` -> get participation requests for a specific event
    - POST `/users/{userId}/events` -> create a new event by user
    - PATCH `/users/{userId}/events/{eventId}` -> update a user's event with `UpdateEventUserRequest`
    - PATCH `/users/{userId}/events/{eventId}/requests` -> update status of participation requests for an event

- **Event Management (Public)**
    - GET `/events` -> retrieve a list of public events with optional filters(`text`, `categories`, `paid`, date
      `rangeStart`, `rangeEnd`, `availability`, `sort`), and pagination (`from`, `size`); **tracks access statistics**
    - GET `/events/{eventId}` -> get full details of a specific public event by ID; **tracks access statistics**
    - GET `/events/{eventId}/comments` -> retrieve paginated comments for a specific event (`from`, `size`)

---

- **Participation request Management (Internal)**
    - GET `/internal/requests?requestIds=...` -> retrieve participation requests by a list of `request IDs`
    - GET `/internal/requests?eventId=...` -> retrieve all participation requests for a specific event
    - GET `/internal/requests?eventId=...&status=...` -> retrieve participation requests for a specific event filtered
      by `status`
    - GET `/internal/requests/count?eventIds=...` -> count participation requests grouped by event ID
    - GET `/internal/requests/exists?requesterId=...&eventId=...&status=...` -> heck if a participation request exists
      for a specific `user`, `event`, and `status`
    - POST `/internal/requests/status/update?requestIds=...&status=...` -> update the `status` of multiple participation
      requests by IDs
- **Participation request Management (Private)**
    - GET `/users/{userId}/requests` -> retrieve all participation requests made by the user
    - POST `/users/{userId}/requests?eventId=...` -> create a new participation request for a specific event
    - PATCH `/users/{userId}/requests/{requestId}/cancel` -> cancel a participation request by ID for the user

---

- **Comment Management (Internal)**
  - GET `/internal/{eventId}/comments` -> retrieve paginated comments for a specific event (`from`, `size`)

- **Comment Management (Admin)**
  - GET `/admin/comments/{commentId}` -> get a comment by its ID
  - DELETE `/admin/comments/{commentId}` -> delete a comment by its ID

- **Comment Management (Private)**
  - GET `/users/{userId}/comments` -> retrieve a paginated list of comments authored by the user (`from`, `size`)
  - POST `/users/{userId}/comments` -> create a comment for a specific event with `CreateUpdateCommentDto`
  - PATCH `/users/{userId}/comments/{commentId}` -> update an existing comment by its ID with `CreateUpdateCommentDto`
  - DELETE `/users/{userId}/comments/{commentId}` -> delete a comment authored by the user

---

- **User Management (Internal)**
  - GET `/internal/users?id=...` -> retrieve a user by ID

- **User Management (Admin)**
  - GET `/admin/users` -> retrieve a paginated list of users with optional filtering by IDs (`ids`, `from`, `size`)
  - POST `/admin/users` -> create a new user with `NewUserRequest`
  - DELETE `/admin/users/{userId}` -> delete a user by ID

---

## Configurations guide

- configuration file path pattern:
  `/infra/config-server/src/main/resources/config/{module-name}/{application-name}/{application-name}-{profile}.yml`
- Example:
  `/infra/config-server/src/main/resources/config/infra/gateway-server/gateway-server-default.yml`
