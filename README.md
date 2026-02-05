# Market Simulator

A Java console application that simulates a financial market with user authentication and asset trading capabilities.

## Features

- **User Authentication** - Secure login system with MariaDB backend
- **Asset Management** - Browse and search available financial assets
- **Trading Operations** - Buy and sell assets through a personal wallet
- **Interactive CLI** - Command-line menu interface for easy navigation

## Technologies

- Java
- JDBC
- MariaDB
- Docker

## Installation & Setup

### Prerequisites

- Java Development Kit (JDK)
- Docker and Docker Compose
- MariaDB JDBC driver

### Running the Application

1. **Add MariaDB driver to your project**
   
   Download the MariaDB JDBC driver and add it to your project dependencies.

2. **Start the database**
   
   ```bash
   docker compose up
   ```

3. **Launch the application**
   
   Run the main Java application file.

## Project Structure

```
market-simulator/
├── src/
│   └── com/market/
│       └── menu/
├── docker-compose.yml
└── README.md
```

## Authors - Group 5

- [Haithem](https://github.com/KeapRoof)
- [Ramazan](https://github.com/Rameray1)
- [Yanis](https://github.com/Taakeo)

## License

This project is part of an academic assignment.
