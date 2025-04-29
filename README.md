# E-Learning Management Platform

[![Java](https://img.shields.io/badge/Java-11-blue.svg)](https://www.oracle.com/java/) [![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7+-green.svg)](https://spring.io/projects/spring-boot) [![MySQL](https://img.shields.io/badge/MySQL-8.0+-blue.svg)](https://www.mysql.com/) [![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

## Table of Contents
- [Overview](#overview)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Getting Started](#getting-started)
- [Project Structure](#project-structure)
- [Testing](#testing)
- [Contributing](#contributing)
- [License](#license)

## Overview
A scalable, secure e-learning platform built with **Java** and **Spring Boot**, featuring dynamic **JavaScript-AJAX** interactions for an enhanced user experience. This application provides role-based access control and automated email notifications, ensuring both usability and security.

## Features
- **Scalable Architecture**: Spring Boot microservices with RESTful APIs.
- **Dynamic UI**: AJAX-powered page updates for smooth interactions.
- **Role-Based Authentication**: Fine-grained access control for admins, instructors, and students.
- **Email Notifications**: Seamless Gmail API integration to automate user alerts.
- **Robust Testing**: JUnit test suite achieving 90% coverage, minimizing regression risks.

## Tech Stack
- **Backend**: Java, Spring Boot
- **Frontend**: HTML5, CSS3, JavaScript (AJAX)
- **Database**: MySQL
- **Email Service**: Gmail API
- **Testing**: JUnit

## Getting Started
### Prerequisites
- Java JDK 11+
- Maven 3.6+
- MySQL Server 8+
- Gmail account for API credentials

### Installation
1. **Clone the repo**
   ```bash
   git clone https://github.com/your-username/e-learning-platform.git
   cd e-learning-platform
   ```
2. **Configure `application.properties`**
   Edit `src/main/resources/application.properties` with your database and Gmail API credentials:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/elearning
   spring.datasource.username=your_db_user
   spring.datasource.password=your_db_password
   gmail.client.id=YOUR_CLIENT_ID
   gmail.client.secret=YOUR_CLIENT_SECRET
   ```
3. **Build and Run**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```
4. **Access**
   Visit `http://localhost:8080` in your browser.

## Project Structure
```plaintext
├── src
│   ├── main
│   │   ├── java/com/example/elearning
│   │   └── resources
│   │       └── application.properties
│   └── test
│       └── java/com/example/elearning
└── pom.xml
```

## Testing
Run the full test suite:
```bash
mvn test
```

## Contributing
1. Fork the repository  
2. Create a feature branch (`git checkout -b feature/YourFeature`)  
3. Commit your changes (`git commit -m "Add feature"`)  
4. Push to your branch (`git push origin feature/YourFeature`)  
5. Open a Pull Request

## License
This project is licensed under the MIT License. See [LICENSE](LICENSE) for details.
