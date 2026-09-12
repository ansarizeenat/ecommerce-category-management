# Order Management Module - End User Documentation

**Project Name:** E-Commerce Management System  
**Internship Organization:** Itvedant Education Pvt. Ltd.  
**Candidate Name:** Zeenat Ansari  
**GitHub Repository:** [https://github.com/ansarizeenat/ecommerce-category-management](https://github.com/ansarizeenat/ecommerce-category-management)  
**Live Deployed Application:** [https://ecommerce-category-management.onrender.com](https://ecommerce-category-management.onrender.com)  

---

## 1. Introduction

The **Order Management Module** facilitates the end-to-end processing and tracking of customer orders in an e-commerce platform. It provides an intuitive interface and robust RESTful backend enabling:
- Customers to select products, manage shopping cart quantities, and place orders with automatic tax and shipping calculations.
- Administrators to monitor all orders in real-time through an interactive dashboard with status filtering (`Pending`, `Shipped`, `Delivered`, `Cancelled`).
- Administrators to update order statuses seamlessly as the order lifecycle progresses.
- Customers and administrators to safely cancel pending orders through a soft-delete mechanism that automatically restocks product inventory.

---

## 2. System Architecture & Tech Stack

- **Backend Framework:** Java 25, Spring Boot 4.x / 3.x
- **Data Persistence:** Spring Data JPA / Hibernate ORM
- **Database:** MySQL 8.0 (Local & Cloud MySQL for Production)
- **Frontend Template:** Thymeleaf, Bootstrap 5.3, Bootstrap Icons, Responsive Vanilla JavaScript
- **Build Tool:** Maven 3.9 Wrapper (`mvnw`)
- **Containerization & Deployment:** Docker & Render Cloud Platform

---

## 3. Database Design & Schema

### Table: `orders`

| Column Name | Data Type | Constraint / Key | Description |
|---|---|---|---|
| `id` | `BIGINT` | PRIMARY KEY, AUTO_INCREMENT | Unique identifier for each order |
| `user_id` | `BIGINT` | FOREIGN KEY (`users.id`) | References the customer/user placing the order |
| `customer_name` | `VARCHAR(100)` | NOT NULL | Display name of the customer |
| `total_amount` | `DECIMAL(10,2)` | NOT NULL | Grand total = Subtotal + Tax + Shipping |
| `subtotal` | `DECIMAL(10,2)` | NOT NULL | Sum of ordered items (price × quantity) |
| `tax_amount` | `DECIMAL(10,2)` | NOT NULL | Calculated tax amount (5% GST/VAT) |
| `shipping_cost` | `DECIMAL(10,2)` | NOT NULL | Shipping fee ($0 if subtotal ≥ $100, else $10) |
| `order_status` | `VARCHAR(50)` | NOT NULL | Order status (`Pending`, `Shipped`, `Delivered`, `Cancelled`) |
| `shipping_address` | `VARCHAR(300)` | NOT NULL | Delivery shipping address |
| `status` | `BOOLEAN` | NOT NULL, DEFAULT TRUE | Soft delete indicator (`true` = Active, `false` = Cancelled) |
| `created_at` | `DATETIME` | AUTO-GENERATED | Timestamp when order was placed |
| `updated_at` | `DATETIME` | AUTO-UPDATED | Timestamp when order was last modified |

### Table: `order_items`

| Column Name | Data Type | Constraint / Key | Description |
|---|---|---|---|
| `id` | `BIGINT` | PRIMARY KEY, AUTO_INCREMENT | Unique line item ID |
| `order_id` | `BIGINT` | FOREIGN KEY (`orders.id`) | Parent order reference |
| `product_id` | `BIGINT` | FOREIGN KEY (`products.id`)| Associated product reference |
| `product_name` | `VARCHAR(255)` | NOT NULL | Snapshot name of the product at order time |
| `quantity` | `INT` | NOT NULL | Quantity ordered |
| `price` | `DECIMAL(10,2)` | NOT NULL | Unit price at time of order |
| `item_total` | `DECIMAL(10,2)` | NOT NULL | Line item total (`quantity × price`) |

### Table: `users`

| Column Name | Data Type | Constraint / Key | Description |
|---|---|---|---|
| `id` | `BIGINT` | PRIMARY KEY, AUTO_INCREMENT | Unique user/customer ID |
| `name` | `VARCHAR(100)` | NOT NULL | Full name of customer |
| `email` | `VARCHAR(150)` | NOT NULL, UNIQUE | Customer email address |
| `phone` | `VARCHAR(20)` | NULLABLE | Contact telephone number |
| `created_at` | `DATETIME` | AUTO-GENERATED | Account registration timestamp |

---

## 4. Key Functionalities & Business Logic

### 4.1 Place an Order (Shopping Cart)
- **Product Selection:** Customers choose items from available inventory.
- **Inventory Check:** The system verifies stock before order placement. If requested quantity exceeds stock, order is rejected.
- **Price Calculation Formula:**
  $$\text{Subtotal} = \sum (\text{Price}_i \times \text{Quantity}_i)$$
  $$\text{Tax (5\%)} = \text{Subtotal} \times 0.05$$
  $$\text{Shipping Cost} = \begin{cases} \$0.00 & \text{if Subtotal} \ge \$100.00 \\ \$10.00 & \text{if Subtotal} < \$100.00 \end{cases}$$
  $$\text{Total Amount} = \text{Subtotal} + \text{Tax} + \text{Shipping Cost}$$
- **Inventory Deduction:** Each ordered product's inventory is reduced automatically upon order creation.

### 4.2 Order Dashboard
- Admins can view all orders sorted in reverse chronological order (newest first).
- **Interactive Status Filters:**
  - `All`: View every order in the system.
  - `Pending`: Orders awaiting processing or dispatch.
  - `Shipped`: Orders in transit.
  - `Delivered`: Completed deliveries.
  - `Cancelled`: Orders cancelled via soft delete.
- **Metrics Summary:** Real-time counters displaying Total, Pending, Shipped, and Delivered orders.
- **Detailed View:** Full modal breaking down customer info, delivery address, ordered items with unit prices, tax, shipping, and grand total.

### 4.3 Update Order Status
- Administrators can change the status of an active order (`Pending` $\rightarrow$ `Shipped` $\rightarrow$ `Delivered`).
- Transition history and last update timestamp are recorded.

### 4.4 Order Cancellation & Soft Delete
- Customers and administrators can cancel orders **only if they have not yet been shipped** (`orderStatus == "Pending"`).
- If an order is already marked as `Shipped` or `Delivered`, cancellation is prevented with an informative alert.
- **Soft Delete Mechanism:**
  - `orders.status` is updated to `false`.
  - `orders.order_status` is updated to `"Cancelled"`.
  - The record is preserved in the database for auditing and historical reporting.
- **Inventory Restock:** The system automatically restores the reserved quantities back to the product catalog inventory.

---

## 5. REST API Documentation

### 5.1 Place an Order
- **Endpoint:** `POST /api/orders`
- **Content-Type:** `application/json`
- **Request Body:**
```json
{
  "customerName": "Zeenat Ansari",
  "customerEmail": "zeenat@example.com",
  "customerPhone": "+91 9876543210",
  "shippingAddress": "Flat 402, Green Valley Apartments, Mumbai, Maharashtra 400001",
  "items": [
    {
      "productId": 1,
      "quantity": 2
    }
  ]
}
```
- **Response (201 Created):**
```json
{
  "id": 1,
  "userId": 1,
  "customerName": "Zeenat Ansari",
  "subtotal": 100.00,
  "taxAmount": 5.00,
  "shippingCost": 0.00,
  "totalAmount": 105.00,
  "orderStatus": "Pending",
  "shippingAddress": "Flat 402, Green Valley Apartments, Mumbai, Maharashtra 400001",
  "status": true,
  "createdAt": "2026-09-12T12:45:00",
  "updatedAt": "2026-09-12T12:45:00",
  "items": [
    {
      "id": 1,
      "productName": "Wireless Mouse",
      "quantity": 2,
      "price": 50.00,
      "itemTotal": 100.00
    }
  ]
}
```

### 5.2 Get All Orders (With Optional Filter)
- **Endpoint:** `GET /api/orders` or `GET /api/orders?status=Pending`
- **Response (200 OK):** List of orders matching criteria.

### 5.3 Get Order by ID
- **Endpoint:** `GET /api/orders/{id}`
- **Response (200 OK):** Order details with full items list.

### 5.4 Update Order Status
- **Endpoint:** `PUT /api/orders/{id}/status`
- **Request Body:**
```json
{
  "orderStatus": "Shipped"
}
```
- **Response (200 OK):** Updated order object.

### 5.5 Cancel Order (Soft Delete)
- **Endpoint:** `DELETE /api/orders/{id}` or `PUT /api/orders/{id}/cancel`
- **Response (200 OK):** Order object with `orderStatus: "Cancelled"` and `status: false`.

---

## 6. How to Run Locally

1. **Clone repository:**
   ```bash
   git clone https://github.com/ansarizeenat/ecommerce-category-management.git
   cd ecommerce-category-management
   ```
2. **Configure Database:**
   Ensure MySQL is running on `localhost:3306` with database `ecommerce_db`. Verify settings in `src/main/resources/application.properties`.
3. **Execute Spring Boot Application:**
   ```bash
   .\mvnw.cmd spring-boot:run
   ```
4. **Access Web Application:**
   Open browser at [http://localhost:8080](http://localhost:8080) to interact with the Order Management Dashboard.

---

## 7. Cloud Deployment & Submission Links

- **GitHub Repository:** [https://github.com/ansarizeenat/ecommerce-category-management](https://github.com/ansarizeenat/ecommerce-category-management)
- **Render Hosted Application:** [https://ecommerce-category-management.onrender.com](https://ecommerce-category-management.onrender.com)
- **Submission Format:** PDF End-User Documentation attached.
