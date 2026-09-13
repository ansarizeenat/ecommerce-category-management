# Cart Management Module - End User Documentation

**Project Name:** E-Commerce Management System
**Internship Organization:** Itvedant Education Pvt. Ltd.
**Candidate Name:** Zeenat Ansari
**GitHub Repository:** [https://github.com/ansarizeenat/ecommerce-category-management](https://github.com/ansarizeenat/ecommerce-category-management)
**Live Deployed Application:** [https://ecommerce-category-management.onrender.com](https://ecommerce-category-management.onrender.com)

---

## 1. Introduction

The **Cart Management Module** allows customers to add, update, and remove products from the shopping cart. It ensures that customers can review their selected items before proceeding to checkout, updating prices and quantities in real time. The module validates inventory for every action (add / update / remove) and protects the shop from overselling. A dashboard gives admins a view of every customer's cart and provides cart-abandonment analytics.

---

## 2. System Architecture & Tech Stack

- **Backend Framework:** Java 25, Spring Boot 4.1
- **ORM & Data Layer:** Spring Data JPA / Hibernate
- **Database:** MySQL 8.0 (`carts` table linked to `users` and `products`)
- **Frontend:** Thymeleaf, Bootstrap 5.3, Bootstrap Icons, Vanilla JavaScript
- **Cloud Deployment:** Render
- **Testing:** H2 in-memory database (MySQL mode) + JUnit 5

---

## 3. Database Design: Table `carts`

| Column Name | Data Type | Key / Constraint | Description |
|---|---|---|---|
| `cart_id` | `INT` (PK, auto_increment) | PRIMARY KEY | Unique identifier for each cart line item |
| `customer_id` | `INT` (FK) | FOREIGN KEY → `users.user_id` | Shopper / customer who owns the cart row |
| `product_id` | `INT` (FK) | FOREIGN KEY → `products.id` | Product added to the cart |
| `quantity` | `INT` | NOT NULL | Number of units in the cart row |
| `total_price` | `DECIMAL(10,2)` | NOT NULL | Line total (unit price × quantity) |
| `created_at` | `DATETIME` | AUTO-GENERATED | Timestamp when the item was added to cart |
| `updated_at` | `DATETIME` | AUTO-UPDATED | Timestamp when the cart item was last updated |

Each `carts` row represents a single **product × customer** line item. If the same product is added twice for the same customer, the quantities are **merged** (added together) into a single row instead of creating duplicates.

---

## 4. Key Functionalities

### 4.1 Add to Cart
- Products can be added directly from the product list or through the admin Cart Management form.
- The system validates that the product exists, is **active** (`status = true`), and has **sufficient stock** before adding.
- If the same product is already in the customer's cart, the **quantity is merged** into the existing row and the total is recalculated.
- Adding a product that would exceed available inventory triggers an error message.

### 4.2 Update Cart
- Customers / admins can modify the quantity of any line item in the cart.
- The line total is recalculated in real time (`total_price = unit_price × quantity`).
- The new quantity is **re-validated against inventory** — if the warehouse doesn't have enough stock, the update is rejected.
- `updated_at` is refreshed after every change.

### 4.3 Remove from Cart
- Customers can remove individual line items.
- The cart total is **automatically recalculated** (no stale totals).
- Admins can also **clear a customer's entire cart** in one click (useful for abandoned carts or support requests).

### 4.4 Cart Dashboard
- **Customer view:** See product name, SKU, unit price, quantity, line total, and remaining stock.
- **Admin view:** See all carts across all customers.
- **Summary stats:** Total Cart Value, Customers With Carts, Total Quantity, Avg Items/Customer (cart-abandonment analysis).
- **Per-customer filter:** Narrow the dashboard to a single shopper and show a live per-customer summary (unique items, total qty, grand total).

---

## 5. How to Use the Screen

1. Open the live app and click **Cart Management**.
2. **Add to Cart:** Select a customer → select an in-stock product → enter a quantity → click **Add to Cart**.
   - If the product is already in the customer's cart, the quantity will be merged automatically.
3. **Update Quantity:** Click the **Qty** pencil button on any row, enter the new quantity, and confirm. Stock will be re-checked.
4. **Remove Item:** Click the trash icon on a row and confirm to remove that single line item.
5. **Clear Entire Cart:** Use the customer filter to select a customer, then click **Clear Cart** (with confirmation) to wipe the customer's cart.
6. **Admin Analysis:** Use the four summary cards to identify cart value and abandonment patterns. Switch between customers using the filter dropdown.

---

## 6. REST API Specifications

| Method | Endpoint | Purpose |
|---|---|---|
| `POST` | `/api/carts` | Add product to cart (merges with existing same product) |
| `GET` | `/api/carts` | List all carts; optional `?customerId=X` filter |
| `GET` | `/api/carts/stats` | Dashboard summary stats (cart-abandonment analysis) |
| `GET` | `/api/carts/{id}` | Get one cart line item by `cart_id` |
| `GET` | `/api/carts/customer/{customerId}` | Get a specific customer's cart |
| `GET` | `/api/carts/customer/{customerId}/summary` | Customer cart grand total + unique items + total qty |
| `PUT` | `/api/carts/{id}/quantity` | Update quantity; re-validates inventory |
| `DELETE` | `/api/carts/{id}` | Remove a single cart item |
| `DELETE` | `/api/carts/customer/{customerId}` | Clear entire cart for a customer |

**Add to cart sample:**
```json
{
  "customerId": 1,
  "productId": 5,
  "quantity": 2
}
```

**Update quantity sample (PUT `/api/carts/42/quantity`):**
```json
{
  "quantity": 3
}
```

---

## 7. Assignment Submission

| Item | Details |
|---|---|
| 1. GitHub Repository Link | https://github.com/ansarizeenat/ecommerce-category-management |
| 2. URL of module hosted on free server | https://ecommerce-category-management.onrender.com |
| 3. End user documentation | This document and `Cart_Management_Module_End_User_Documentation.pdf` |

Upload the assignment in PDF format using the generated file `Cart_Management_Module_End_User_Documentation.pdf`.
