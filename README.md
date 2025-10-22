# GitHub Repository Scorer

A Spring Boot application that searches public GitHub repositories by language and creation date, then ranks them using a configurable popularity score.

## 🎯 Features

- **GitHub Integration**: Search repositories by programming language and creation date.
- **Smart Scoring Algorithm**: Uses Min-Max normalization and exponential decay to produce an easily interpretable 0-100 score.
- **RESTful API**: Clean, well-documented endpoints for easy integration.
- **Interactive API Documentation**: Explore and test the API using the integrated **Swagger UI**.
- **Configurable**: Easily tune scoring weights and parameters via `application.yml`.
- **Production-Ready**: Includes caching (`@Cacheable`), retries (`@Retryable`), validation (`@Valid`), and custom global error handling.
- **Clean Architecture**: Follows best practices like the Single Responsibility Principle, using mappers and a layered architecture.
- **Comprehensive Tests**: High test coverage with unit and integration tests for all layers of the application.

## 🔍 API Documentation (Swagger UI)

Once the application is running, you can access the interactive Swagger UI to view detailed API documentation and execute test requests directly from your browser.

**URL**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

## 🚀 Quick Start

### Prerequisites
- Java 21
- Maven 3.8+

### Running the Application

```bash
# Clone the repository
git clone https://github.com/ajithsolomonsekar/repo-scorer.git
cd repo-scorer

# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

The application will start on `http://localhost:8080`.

### Using the API

**Example Request:**

```bash
curl -X POST http://localhost:8080/api/v1/repositories/search \
  -H "Content-Type: application/json" \
  -d '{
    "language": "Java",
    "createdAfter": "2023-01-01",
    "maxResults": 1
  }'
```

**Example Response:**

```json
{
  "searchMetadata": {
    "language": "Java",
    "createdAfter": "2023-01-01"
  },
  "repositories": [
    {
      "name": "spring-boot",
      "fullName": "spring-projects/spring-boot",
      "description": "Spring Boot makes it easy to create stand-alone, production-grade Spring based Applications that you can \"just run\".",
      "url": "https://github.com/spring-projects/spring-boot",
      "stars": 68000,
      "forks": 38000,
      "updatedAt": "2024-10-22T10:00:00Z",
      "score": 95.75,
      "scoreBreakdown": {
        "starsScore": 98.5,
        "forksScore": 95.2,
        "recencyScore": 99.8
      }
    }
  ],
  "totalCount": 1500,
  "incompleteResults": false,
  "returnedCount": 1,
  "scoredAt": "2024-10-22T12:00:00Z"
}
```

## 📊 Scoring Algorithm

The application calculates a popularity score on a **0-100 scale**. To achieve this, it uses the **Min-Max Normalization** method for stars and forks, and **exponential decay** for recency. This provides an easily interpretable score where 100 represents the highest popularity and 0 the lowest within a given set of results.

The final score is a weighted sum of three key metrics:

1.  **Stars Score (Weight: 50%)**: Normalized using Min-Max scaling across the batch of results. This provides a relative measure of popularity.
    - **Formula**: `(value - min) / (max - min) * 100`
2.  **Forks Score (Weight: 30%)**: Also normalized using Min-Max scaling. This indicates how often the community is creating its own version of the project.
    - **Formula**: `(value - min) / (max - min) * 100`
3.  **Recency Score (Weight: 20%)**: Calculated using an **exponential decay** function based on the last update time. This heavily rewards actively maintained projects.
    - **Formula**: `exp(-daysSinceUpdate / halfLife) * 100`
    - This decay is controlled by a **half-life** period (default: 180 days), which is the time it takes for the recency score to reduce by 50%. Therefore, a repository updated today receives ~100 points, while one updated 6 months ago receives ~50 points, and one updated a year ago receives ~25 points.

## ⚙️ Configuration

The weights for each metric and the half-life for the recency score are configurable in `src/main/resources/application.yml`:

```yaml
scoring:
  stars-weight: 0.5      # Adjust weights (should sum to 1.0)
  forks-weight: 0.3
  recency-weight: 0.2
  recency-half-life-days: 180.0  # The time in days for the recency score to decay to 50% of its value.

github:
  api-url: https://api.github.com
  token: ${GITHUB_TOKEN:}  # Optional: For higher rate limits
```

### GitHub API Rate Limits

The GitHub API imposes rate limits on requests.

- **Unauthenticated Requests**: Limited to 60 requests per hour. You may hit this limit quickly during testing.
- **Authenticated Requests**: The limit increases to 5,000 requests per hour.

To use authenticated requests, you can provide a [Personal Access Token (PAT)](https://github.com/settings/tokens) with no special scopes required. Set it as an environment variable before running the application:

```bash
export GITHUB_TOKEN=your_personal_access_token
mvn spring-boot:run
```

## 🏗️ Architecture

```
Controller Layer (REST API, Validation, Swagger)
    ↓
Service Layer (Orchestration, Business Logic)
    ↓
Client Layer (GitHub API Integration, Error Handling)
```

### Project Structure

```
src/main/java/com/ajith/reposcorer/
├── controller/         # REST endpoints & Swagger Docs
│   └── RepositoryScoringController.java
├── service/            # Business logic and orchestration
│   ├── RepositorySearchService.java
│   └── RepositoryScoringService.java
├── client/             # External API communication
│   ├── GithubApiClient.java
│   └── dto/            # DTOs for external API
├── dto/                # DTOs for internal API contracts
├── exception/          # Custom exceptions & global handler
│   └── GlobalExceptionHandler.java
├── mapper/             # Object mapping and transformation
│   └── RepositoryMapper.java
└── properties/         # @ConfigurationProperties classes
    ├── ScoringProperties.java
    └── GithubProperties.java
```

## ⚖️ Design Trade-offs & Simplifications

This project was built with a focus on clean architecture and core functionality. To meet these goals, certain features were simplified:

- **No Full Pagination**: The API currently fetches only the first page of results from the GitHub API (up to `maxResults`, max 100). A production-ready system would implement full pagination controls (`page`, `per_page`) to navigate through all results.
- **Simple In-Memory Caching**: The application uses Spring's default in-memory cache. For a multi-instance, production environment, a distributed cache like **Redis** or **Memcached** would be necessary.
- **Synchronous Processing**: All operations are synchronous. For very large requests or to improve throughput, processing could be made asynchronous using `@Async` or a 
  message queue like Kafka.
- **No Circuit Breaker**: While the application has retries, it does not implement a full circuit breaker pattern (e.g., with Resilience4j) to protect against a completely unresponsive GitHub API.

## 🚦 Error Handling

The API provides detailed, structured error responses for a clean client experience.

**Validation Error (400 Bad Request):**
```json
{
  "status": 400,
  "message": "Validation Failed",
  "errors": {
    "language": "Language is required",
    "createdAfter": "Created after date is required"
  },
  "timestamp": "2024-10-22T21:40:14.828834"
}
```

**GitHub API Error (e.g., 502 Bad Gateway):**
```json
{
  "status": 502,
  "error": "GitHub API Error",
  "message": "GitHub API rate limit exceeded. Please try again later or add an access token.",
  "timestamp": "2024-10-22T21:45:00.123456"
}
```

## 🧪 Testing

The project has a comprehensive test suite covering all layers of the application to ensure reliability and correctness.

### How to Run Tests

```bash
# Run all unit and integration tests
mvn test
```

### Test Coverage
- **Controller Layer**: `RepositoryScoringControllerTest` (Integration Test)
- **Service Layer**: `RepositorySearchServiceTest`, `RepositoryScoringServiceTest` (Unit Tests)
- **Client Layer**: `GithubApiClientTest` (Unit Test)
- **Mappers**: `RepositoryMapperTest` (Unit Test)

## 🔐 Security

- **Input Validation**: All incoming requests are validated to prevent invalid data.
- **Secrets Management**: The GitHub token is loaded from an environment variable (`GITHUB_TOKEN`), not hardcoded.
- **Error Masking**: Internal exception details are not exposed in API error responses.

## 📝 Future Enhancements

- [ ] Implement full pagination support for search results.
- [ ] Add more advanced scoring metrics (e.g., issue resolution time, PR merge rate).
- [ ] Introduce a distributed cache like Redis for multi-instance deployments.
- [ ] Add a circuit breaker (e.g., Resilience4j) for more advanced resilience against GitHub API failures.
