\# E-Commerce Category Management API



A Spring Boot REST API for managing e-commerce categories. This module supports category creation, viewing, updating, and soft deletion.



\## Features



\- Create a category

\- View all categories

\- Update category details

\- Soft delete a category

\- Store data in MySQL database

\- Automatically track created and updated timestamps



\## Tech Stack



\- Java

\- Spring Boot

\- Spring Data JPA

\- MySQL

\- Maven



\## Category API Endpoints



| Method | Endpoint | Purpose |

|---|---|---|

| GET | `/api/categories` | Get all categories |

| POST | `/api/categories` | Create a new category |

| PUT | `/api/categories/{id}` | Update a category |

| DELETE | `/api/categories/{id}` | Soft delete a category |



\## Create / Update Request Example



```json

{

&#x20; "categoryName": "Electronics",

&#x20; "description": "Electronic devices and accessories"

}

```



\## Soft Delete



The delete API does not permanently remove a category from the database. It changes the category `status` to `false`, so the record remains available for future reference.



\## Database Setup



1\. Create a MySQL database named `ecommerce\_db`.

2\. Create this file:



```text

src/main/resources/application.properties

```



3\. Add your own database details:



```properties

spring.application.name=ecommerce

spring.datasource.url=jdbc:mysql://localhost:3306/ecommerce\_db

spring.datasource.username=YOUR\_MYSQL\_USERNAME

spring.datasource.password=YOUR\_MYSQL\_PASSWORD

spring.jpa.hibernate.ddl-auto=update

```



> Never upload your real database password to GitHub.



\## Run the Project



```powershell

.\\mvnw.cmd spring-boot:run

```



The application will run locally at:



```text

http://localhost:8080

```



\## Module Status



Category Management module completed and tested:



\- Create Category

\- Get All Categories

\- Update Category

\- Soft Delete Category

