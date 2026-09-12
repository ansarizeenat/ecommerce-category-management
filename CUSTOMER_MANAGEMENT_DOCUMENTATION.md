# Customer Management Module - End User Documentation

**Project Name:** E-Commerce Management System  
**Internship Organization:** Itvedant Education Pvt. Ltd.  
**Candidate Name:** Zeenat Ansari  
**GitHub Repository:** [https://github.com/ansarizeenat/ecommerce-category-management](https://github.com/ansarizeenat/ecommerce-category-management)  
**Live Deployed Application:** [https://ecommerce-category-management.onrender.com](https://ecommerce-category-management.onrender.com)  

---

## 1. Introduction

The **Customer Management Module** manages customer profiles, personal data, order history, and account settings within the E-Commerce platform. It empowers administrators to maintain comprehensive visibility over the customer base, observe customer ordering patterns, update contact information, and provide prompt customer support. In addition, it provides self-registration capabilities for customers directly through the web interface.

---

## 2. System Architecture & Tech Stack

- **Backend Framework:** Java 25, Spring Boot 4.1.1 / 3.x
- **ORM & Data Layer:** Spring Data JPA / Hibernate ORM
- **Database:** MySQL 8.0 (Relational schema with foreign keys and unique constraints)
- **Frontend Template:** Thymeleaf, Bootstrap 5.3, Bootstrap Icons, Responsive Vanilla JavaScript
- **Build Tool:** Maven 3.9 Wrapper (`mvnw`)
- **Cloud Deployment:** Docker containerized deployment on Render

---

## 3. Database Design: Table `users`

The database schema strictly adheres to the Itvedant design requirements:

| Column Name | Data Type | Key / Constraint | Description |
|---|---|---|---|
| `user_id` | `INT / BIGINT` | PRIMARY KEY, AUTO_INCREMENT | Unique identifier for each customer |
| `first_name` | `VARCHAR(100)` | NOT NULL | Customer's first name |
| `last_name` | `VARCHAR(100)` | NULLABLE | Customer's last name |
| `email` | `VARCHAR(100)` | NOT NULL, UNIQUE | Customer's unique email address |
| `phone` | `VARCHAR(15)` | NULLABLE | Customer's contact phone number |
| `created_at` | `DATETIME` | AUTO-GENERATED | Timestamp when customer account was registered |
| `updated_at` | `DATETIME` | AUTO-UPDATED | Timestamp when customer details were last modified |
| `status` | `BOOLEAN` | NOT NULL, DEFAULT TRUE | `true` for active customers, `false` for inactive (soft delete) |

---

## 4. Key Functionalities & Business Rules

### 4.1 Add / Register New Customer
- Customers can register through the frontend interface or administrators can manually onboard customers via the admin dashboard.
- System validates mandatory fields (`first_name`, `email`).
- Unique email validation prevents duplicate accounts.
- Initial account state is marked as active (`status = true`).

### 4.2 Customer Dashboard
- Real-time customer metrics: Total Customers, Active Customers, and Deactivated Customers.
- Status filters: `All`, `Active`, `Inactive`.
- View all registered customers with full name, email, phone number, registration date, and order history statistics.
- **Order History Modal:** Administrators can click "View Order History" on any customer to see all past orders placed by that user, including Order ID, Delivery Address, Order Total, Status (`Pending`, `Shipped`, `Delivered`, `Cancelled`), and Placement Date.

### 4.3 Update Customer Details
- Administrators can edit customer contact details (first name, last name, email, phone) and account status.
- Duplicate email prevention ensures modified emails do not collide with another customer.
- Last updated timestamp (`updated_at`) is automatically refreshed.

### 4.4 Delete / Deactivate Customer Account (Soft Delete)
- Administrators can deactivate a customer account without permanently deleting records.
- Soft delete sets `status = false` and updates `updated_at`.
- Historical order records remain intact for accounting and auditing.
- Deactivated customers can be reactivated at any time with a single click.

---

## 5. REST API Specifications

### 5.1 Register / Add Customer
- **Endpoint:** `POST /api/customers`
- **Request Body:**
```json
{
  "firstName": "Zeenat",
  "lastName": "Ansari",
  "email": "zeenat@example.com",
  "phone": "+91 9876543210"
}
```
- **Response (201 Created):**
```json
{
  "userId": 1,
  "firstName": "Zeenat",
  "lastName": "Ansari",
  "email": "zeenat@example.com",
  "phone": "+91 9876543210",
  "status": true,
  "createdAt": "2026-09-12T13:00:00",
  "updatedAt": "2026-09-12T13:00:00"
}
```

### 5.2 Get All Customers (With Optional Status Filter)
- **Endpoint:** `GET /api/customers` or `GET /api/customers?status=true`
- **Response (200 OK):** List of customer DTOs with order counts and lifetime spend.

### 5.3 Get Customer with Order History
- **Endpoint:** `GET /api/customers/{id}/orders`
- **Response (200 OK):** Customer details including array of all orders placed by this customer.

### 5.4 Update Customer Details
- **Endpoint:** `PUT /api/customers/{id}`
- **Request Body:**
```json
{
  "firstName": "Zeenat",
  "lastName": "Ansari",
  "email": "zeenat.ansari@example.com",
  "phone": "+91 9876543210"
}
```
- **Response (200 OK):** Updated user object.

### 5.5 Deactivate Customer (Soft Delete)
- **Endpoint:** `DELETE /api/customers/{id}` or `PUT /api/customers/{id}/deactivate`
- **Response (200 OK):** User object with `status = false`.

### 5.6 Reactivate Customer
- **Endpoint:** `PUT /api/customers/{id}/activate`
- **Response (200 OK):** User object with `status = true`.

---

## 6. Automated Testing Suite

Unit and integration tests are implemented in `src/test/java/com/ecommerce/ecommerce/CustomerServiceTest.java`:
- `testCreateCustomer_Success`: Successful customer creation and timestamp generation.
- `testCreateCustomer_DuplicateEmailThrowsException`: Validation that duplicate emails are rejected.
- `testUpdateCustomer_Success`: Contact information and name modification.
- `testDeactivateCustomer_SoftDelete`: Verification that deactivation preserves the record with `status = false`.
- `testActivateCustomer_Reactivation`: Reactivation of an inactive customer account.
- `testGetCustomerWithOrders_OrderHistory`: Verification that past orders are correctly linked to customer profile.

**Result:** `Tests run: 13, Failures: 0, Errors: 0, Skipped: 0 — BUILD SUCCESS`

---

## 7. Assignment Deliverables & Submission

- **GitHub Repository Link:** [https://github.com/ansarizeenat/ecommerce-category-management](https://github.com/ansarizeenat/ecommerce-category-management)
- **Hosted Cloud URL (Render):** [https://ecommerce-category-management.onrender.com](https://ecommerce-category-management.onrender.com)
- **Documentation Format:** PDF End-User Documentation attached (`Customer_Management_Module_End_User_Documentation.pdf`).
