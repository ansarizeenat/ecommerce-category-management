# E-Commerce Management System

A full-featured Spring Boot and MySQL web application and REST API developed as part of the **Itvedant Internship**.

- **GitHub Repository:** [https://github.com/ansarizeenat/ecommerce-category-management](https://github.com/ansarizeenat/ecommerce-category-management)
- **Live Deployed URL (Render):** [https://ecommerce-category-management.onrender.com](https://ecommerce-category-management.onrender.com)
- **Candidate:** Zeenat Ansari

---

## Modules Overview

### 1. Category Management Module
- Create, view, update, and soft-delete product categories.
- Tracks created/updated timestamps automatically.
- Prevents deactivation of categories that contain active products.

### 2. Product Management Module
- Add, update, view, and deactivate products.
- Inventory tracking, SKU management, price definitions, and category mapping.

### 3. Order Management Module (Latest)
- **Place an Order:** Customers select products and add to shopping cart. Real-time automatic calculation of subtotal, tax (5%), shipping cost, and grand total. Product inventory is deducted upon order placement.
- **Order Dashboard:** Admins view all customer orders, with quick status filters (`All`, `Pending`, `Shipped`, `Delivered`, `Cancelled`).
- **Update Order Status:** Admins can transition order lifecycle (`Pending` -> `Shipped` -> `Delivered`).
- **Cancel Order (Soft Delete):** Customers and admins can cancel orders before shipment (`Pending` orders only). Changes status to `Cancelled` and `status` to `false`. Automatically restores product quantities to inventory.
- **Comprehensive End User Documentation:** Available in PDF format (`Order_Management_Module_End_User_Documentation.pdf`).

---

## Tech Stack

- **Java:** 25
- **Framework:** Spring Boot 4.1.1 / 3.x (Spring Web MVC, Spring Data JPA, Thymeleaf, Validation)
- **Database:** MySQL 8.0 (Local & Cloud MySQL for Production)
- **Frontend:** HTML5, Bootstrap 5.3, Bootstrap Icons, JavaScript
- **Build Tool:** Maven 3.9 (`mvnw`)
- **Deployment:** Docker, Render Cloud Platform

---

## Database Design: `orders`

| Column | Datatype | Constraint | Description |
|---|---|---|---|
| `id` | `BIGINT` | PK, Auto Increment | Unique identifier for each order |
| `user_id` | `BIGINT` | FK (`users.id`) | Foreign key referencing the users table |
| `customer_name` | `VARCHAR(100)` | NOT NULL | Display name of customer |
| `total_amount` | `DECIMAL(10,2)` | NOT NULL | Total cost of order (Subtotal + Tax + Shipping) |
| `subtotal` | `DECIMAL(10,2)` | NOT NULL | Item subtotal |
| `tax_amount` | `DECIMAL(10,2)` | NOT NULL | 5% Tax |
| `shipping_cost` | `DECIMAL(10,2)` | NOT NULL | Shipping fee |
| `order_status` | `VARCHAR(50)` | NOT NULL | Status (`Pending`, `Shipped`, `Delivered`, `Cancelled`) |
| `shipping_address`| `VARCHAR(300)` | NOT NULL | Delivery address |
| `created_at` | `DATETIME` | NOT NULL | Timestamp when order was placed |
| `updated_at` | `DATETIME` | NOT NULL | Timestamp when status was updated |
| `status` | `BOOLEAN` | NOT NULL | `true` for active orders, `false` for cancelled (soft delete) |

---

## API Endpoints Summary

### Order Management (`/api/orders`)
| Method | Endpoint | Purpose |
|---|---|---|
| `POST` | `/api/orders` | Place a new order with cart items |
| `GET` | `/api/orders` | Get all orders (supports `?status=Pending`, etc.) |
| `GET` | `/api/orders/{id}` | Get detailed order by ID |
| `PUT` | `/api/orders/{id}/status` | Update order status (`Pending`, `Shipped`, `Delivered`, `Cancelled`) |
| `DELETE` / `PUT` | `/api/orders/{id}` / `/api/orders/{id}/cancel` | Soft-delete cancel order (restocks inventory) |
| `GET` | `/api/orders/users` | List registered customers |

### Product Management (`/products`)
| Method | Endpoint | Purpose |
|---|---|---|
| `GET` | `/products` | Get all products |
| `GET` | `/products/{id}` | Get product by ID |
| `POST` | `/products` | Add new product |
| `PUT` | `/products/{id}` | Update product |
| `PUT` | `/products/{id}/deactivate` | Deactivate product |

### Category Management (`/api/categories`)
| Method | Endpoint | Purpose |
|---|---|---|
| `GET` | `/api/categories` | Get all categories |
| `POST` | `/api/categories` | Create a category |
| `PUT` | `/api/categories/{id}` | Update category |
| `DELETE` | `/api/categories/{id}` | Soft delete category |

---

## How to Run Locally

1. **Clone repository:**
   ```powershell
   git clone https://github.com/ansarizeenat/ecommerce-category-management.git
   cd ecommerce-category-management
   ```

2. **Database Setup:**
   Ensure MySQL is running with database `ecommerce_db` and configure `src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/ecommerce_db
   spring.datasource.username=YOUR_USER
   spring.datasource.password=YOUR_PASSWORD
   ```

3. **Run Application:**
   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

4. **Access in Browser:**
   ```text
   http://localhost:8080
   ```

5. **Run Tests:**
   ```powershell
   .\mvnw.cmd test
   ```
