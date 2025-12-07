# Casse

## Running the application

### Prerequisites

- Docker
- Docker Compose
- Java 21
- Gradle

### Instructions

1.  **Set up environment variables:**

    Copy the `.env.template` file to `.env`:

    ```bash
    cp .env.template .env
    ```

    Then, provide values for the missing environment variables in the `.env` file:


2.  **Start the infrastructure:**

    ```bash
    docker-compose -f casse-infra/docker-compose.yaml up -d
    ```

3.  **Run the backend:**

    ```bash
    export $(cat .env | xargs) && ./gradlew :casse-backend:bootRun
    ```
