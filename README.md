# Kleer - Currency Exchange Application

A full-stack currency exchange application with Riksbank integration. The backend provides exchange rate data and conversion services, while the frontend offers a user-friendly interface for currency conversions.

## Prerequisites

Before running this project, ensure you have the following installed:

- **Java 21** or higher
- **Node.js** (v16 or higher recommended)
- **npm** (comes with Node.js)
- **Maven** (optional - the project includes Maven wrapper)

## Project Structure

```
kleer/
├── backend/        # Spring Boot backend (Java 21)
└── frontend/       # React + TypeScript frontend (Vite)
```

## Quick Start

### 1. Running the Backend

The backend is a Spring Boot application that runs on **port 8080**.

```bash
# Navigate to the backend directory
cd backend

# On Windows - Use the custom build script
build.cmd

# On macOS/Linux
./mvnw spring-boot:run
```

The backend will start at `http://localhost:8080`

**Alternative:** If you have Maven installed globally:
```bash
cd backend
mvn spring-boot:run
```

### 2. Running the Frontend

The frontend is a React application that runs on **port 5173** (default Vite port).

```bash
# Navigate to the frontend directory
cd frontend

# Install dependencies (first time only)
npm install

# Start the development server
npm run dev
```

The frontend will start at `http://localhost:5173`

## Additional Information

### Backend Features

- **API Base URL:** `http://localhost:8080`
- **Database:** H2 in-memory database (no setup required)
- **H2 Console:** Available at `http://localhost:8080/h2-console`
  - JDBC URL: `jdbc:h2:mem:currencydb`
  - Username: `sa`
  - Password: (leave empty)

### Frontend Features

- React 19 with TypeScript
- Vite for fast development and building
- Modern UI for currency exchange

### Building for Production

**Backend:**
```bash
cd backend

# Build and run with custom script (Windows)
build.cmd

# OR manually run the existing JAR
java -jar target/currency-exchange-1.0.0.jar
```

**Frontend:**
```bash
cd frontend
npm run build

# Preview the production build
npm run preview
```

## Troubleshooting

- **JAVA_HOME not set (Windows):** The `build.cmd` script requires `JAVA_HOME` to be set. Set it with:
  ```bash
  set JAVA_HOME=C:\path\to\your\jdk-21
  ```
- **Port conflicts:** If port 8080 or 5173 is already in use, you'll need to stop the conflicting service or modify the port configuration.
- **Java version:** Ensure you're using Java 21. Check with `java -version`
- **Node version:** Check your Node version with `node -v`
- **Backend not connecting to Riksbank API:** Check your internet connection and firewall settings.

## Development

- **Backend hot reload:** Spring Boot DevTools is included for automatic restarts
- **Frontend hot reload:** Vite provides instant HMR (Hot Module Replacement)

## License

This project is part of the Kleer application suite.

