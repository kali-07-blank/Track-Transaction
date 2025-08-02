# Money Tracker Application

A comprehensive Spring Boot application for tracking personal finances with robust OOP design patterns.

## 🚀 Features

- **User Management**: Register, login, and manage user profiles
- **Transaction Tracking**: Add, edit, delete income/expense transactions
- **Category Management**: Organize transactions by categories
- **Financial Reports**: View summaries, monthly/yearly reports
- **RESTful API**: Complete REST API with proper HTTP status codes
- **Data Validation**: Comprehensive input validation and error handling
- **Security**: Password encryption and basic authentication
- **Database Integration**: JPA/Hibernate with H2 (development) and MySQL (production)

## 🏗️ Architecture & OOP Concepts

### Design Patterns Used
- **Repository Pattern**: Data access abstraction
- **Service Layer Pattern**: Business logic separation
- **DTO Pattern**: Data transfer objects for API communication
- **Dependency Injection**: Spring's IoC container
- **MVC Pattern**: Model-View-Controller architecture

### OOP Principles
- **Encapsulation**: Private fields with public getters/setters
- **Inheritance**: Entity relationships and exception hierarchy
- **Polymorphism**: Interface implementations and method overriding
- **Abstraction**: Service interfaces and repository abstractions

## 📁 Project Structure

```
src/main/java/com/moneytracker/
├── entity/
│   ├── Person.java
│   └── Transaction.java
├── dto/
│   ├── PersonDTO.java
│   ├── TransactionDTO.java
│   ├── TransactionSummaryDTO.java
│   ├── LoginRequestDTO.java
│   └── LoginResponseDTO.java
├── repository/
│   ├── PersonRepository.java
│   └── TransactionRepository.java
├── service/
│   ├── PersonService.java
│   ├── PersonServiceImpl.java
│   ├── TransactionService.java
│   ├── TransactionServiceImpl.java
│   ├── AuthService.java
│   └── TransactionReportService.java
├── controller/
│   ├── PersonController.java
│   ├── TransactionController.java
│   └── AuthController.java
├── config/
│   ├── SecurityConfig.java
│   └── CorsConfig.java
├── exception/
│   ├── ResourceNotFoundException.java
│   ├── DuplicateResourceException.java
│   └── GlobalExceptionHandler.java
├── enums/
│   └── TransactionType.java
├── validator/
│   └── TransactionValidator.java
├── util/
│   └── DateRangeUtil.java
└── MoneyTrackerApplication.java
```

## 🛠️ Technologies Used

- **Framework**: Spring Boot 3.2.0
- **Language**: Java 17
- **Database**: H2 (development), MySQL (production)
- **ORM**: Spring Data JPA / Hibernate
- **Security**: Spring Security
- **Validation**: Bean Validation (JSR-303)
- **Build Tool**: Maven
- **Testing**: JUnit 5, Mockito

## 🚀 Getting Started

### Prerequisites
- Java 17 or higher
- Maven 3.6+
- IDE (IntelliJ IDEA, Eclipse, VS Code)

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/kali-07-blank/Track-Transaction.git
   cd Track-Transaction
   ```

2. **Build the project**
   ```bash
   mvn clean install
   ```

3. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

4. **Access the application**
   - API Base URL: `http://localhost:8080/api`
   - H2 Console: `http://localhost:8080/h2-console`

## 📖 API Documentation

### Authentication Endpoints

#### Register User
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "password123",
  "fullName": "John Doe"
}
```

#### Login User
```http
POST /api/auth/login
Content-Type: application/json

{
  "identifier": "john_doe",
  "password": "password123"
}
```

### Transaction Endpoints

#### Create Transaction
```http
POST /api/transactions
Content-Type: application/json

{
  "amount": 100.50,
  "description": "Grocery shopping",
  "transactionType": "EXPENSE",
  "category": "Food & Dining",
  "personId": 1
}
```

#### Get User Transactions
```http
GET /api/transactions/person/{personId}
```

#### Get Transaction Summary
```http
GET /api/transactions/person/{personId}/summary
```

### Person Endpoints

#### Get Person by ID
```http
GET /api/persons/{id}
```

#### Update Person
```http
PUT /api/persons/{id}
Content-Type: application/json

{
  "username": "john_doe_updated",
  "email": "john.updated@example.com",
  "fullName": "John Doe Updated"
}
```

## 🧪 Testing

Run unit tests:
```bash
mvn test
```

Run integration tests:
```bash
mvn verify
```

## 🔧 Configuration

### Database Configuration

#### Development (H2)
```properties
spring.datasource.url=jdbc:h2:mem:moneytracker
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.hibernate.ddl-auto=create-drop
```

#### Production (MySQL)
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/moneytracker
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
```

### Security Configuration
The application uses Spring Security with:
- Password encryption using BCrypt
- CORS configuration for frontend integration
- Public endpoints for registration and login

## 📊 Database Schema

### Person Table
| Column    | Type         | Constraints      |
|-----------|--------------|------------------|
| id        | BIGINT       | PRIMARY KEY, AUTO_INCREMENT |
| username  | VARCHAR(50)  | UNIQUE, NOT NULL |
| email     | VARCHAR(255) | UNIQUE, NOT NULL |
| password  | VARCHAR(255) | NOT NULL         |
| full_name | VARCHAR(255) | NOT NULL         |

### Transaction Table
| Column           | Type           | Constraints      |
|------------------|----------------|------------------|
| id               | BIGINT         | PRIMARY KEY, AUTO_INCREMENT |
| amount           | DECIMAL(10,2)  | NOT NULL         |
| description      | VARCHAR(255)   | NOT NULL         |
| transaction_type | VARCHAR(20)    | NOT NULL         |
| transaction_date | TIMESTAMP      | NOT NULL         |
| category         | VARCHAR(50)    | NULL             |
| person_id        | BIGINT         | FOREIGN KEY      |

## 🚀 Deployment

### Using Maven
```bash
mvn clean package
java -jar target/money-tracker-1.0.0.jar
```

### Using Docker
```dockerfile
FROM openjdk:17-jdk-slim
COPY target/money-tracker-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 📞 Support

For support and questions:
- Create an issue in the GitHub repository
- Email: support@moneytracker.com

## 🙏 Acknowledgments

- Spring Boot Team for the excellent framework
- H2 Database for easy development setup
- All contributors and testers
