# E-Commerce Management System

A full-featured Spring Boot and MySQL web application and REST API developed as part of the **Itvedant Internship**.

- **GitHub Repository:** [https://github.com/ansarizeenat/ecommerce-category-management](https://github.com/ansarizeenat/ecommerce-category-management)
- **Live Deployed URL (Render):** [https://ecommerce-category-management.onrender.com](https://ecommerce-category-management.onrender.com)
- **Candidate:** Zeenat Ansari

---

## Modules Overview

### 1. Customer Management Module (New)
- **Add / Register Customer:** Self-registration through frontend or manual creation via admin dashboard with unique email validation.
- **Customer Dashboard:** Real-time customer metrics (Total, Active, Deactivated), status filtering (`All`, `Active`, `Inactive`).
- **Customer Order History:** Admins can view complete order history, line items, and lifetime spend for every customer.
- **Update Customer Details:** Edit names, email addresses, phone numbers, and status.
- **Soft-Delete Deactivation:** Deactivate customer accounts (`status = false`) while safely retaining order history. Re-enable with one click.
- **PDF Documentation:** `Customer_Management_Module_End_User_Documentation.pdf`.

### 2. Order Management Module
- **Place an Order:** Customers select products and add to shopping cart. Automatic live calculation of subtotal, tax (5%), shipping cost, and grand total. Inventory is deducted automatically.
- **Order Dashboard:** Admins view all orders with status filters (`All`, `Pending`, `Shipped`, `Delivered`, `Cancelled`).
- **Update Order Status:** Admins transition orders (`Pending` -> `Shipped` -> `Delivered`).
- **Cancel Order (Soft Delete):** Orders can be cancelled if not yet shipped (`Pending` only). Changes status to `Cancelled` and `status` to `false`, with automatic inventory restock.
- **PDF Documentation:** `Order_Management_Module_End_User_Documentation.pdf`.

### 3. Product Management Module
- Add, update, view, and deactivate products.
- Inventory tracking, SKU management, price definitions, and category mapping.

### 4. Category Management Module
- Create, view, update, and soft-delete product categories.
- Tracks created/updated timestamps automatically.
- Prevents deactivation of categories that contain active products.

### 5. Payment Management Module
- **Process Payment:** Charge an unpaid order through a simulated Stripe, PayPal, or bank-transfer gateway. Methods: Credit Card, Debit Card, PayPal, Bank Transfer.
- **Payment Dashboard:** Admins view Payment ID, order, customer, amount, method, status (`Paid`, `Failed`, `Refunded`), gateway transaction ID, and timestamps. Status filters and revenue stats are included.
- **Refund Payment:** Admins refund a `Paid` transaction for cancelled or returned orders. The refund is logged and the linked order is cancelled.
- **PDF Documentation:** `Payment_Management_Module_End_User_Documentation.pdf`.

### 6. Cart Management Module
- **Add to Cart:** Customers add products directly from the product page. The system validates that the product is active and has sufficient stock before adding it. If the same product is already in the customer's cart, the quantities are merged instead of creating a duplicate row.
- **Update Cart:** Customers can modify the quantity of any cart line item. The total price is recalculated in real time and inventory is re-checked against the new quantity.
- **Remove from Cart:** Customers can remove individual items from the cart, and the cart total is recalculated automatically. Admins can also clear an entire customer's cart in one click.
- **Cart Dashboard:** Customers see their cart contents (product name, SKU, unit price, quantity, line total, stock remaining); admins view all customer carts and analyse cart abandonment via summary cards (Total Cart Value, Customers With Carts, Total Quantity, Avg Items / Customer) plus a per-customer filter.
- **PDF Documentation:** `Cart_Management_Module_End_User_Documentation.pdf`.

### 7. Shipping Management Module
- **Shipping Cost Calculation:** Dynamic calculation based on weight, delivery location (Local, Domestic, International), and shipping method (Standard, Express, Overnight).
- **Shipping Dashboard:** Admins view all orders with courier service, tracking number, shipping status, and cost. Filter by `Shipped`, `In Transit`, and `Delivered`.
- **Track Shipment (Customer):** Live tracking lookup by tracking number featuring an interactive milestone progress stepper (Placed -> Shipped -> In Transit -> Delivered).
- **Update Shipping Information:** Admins can re-assign couriers, update tracking numbers, and advance shipment status (which synchronizes order status to `Delivered`).
- **PDF Documentation:** `Shipping_Management_Module_End_User_Documentation.pdf`.

### 8. Review and Rating Management Module (New)
- **Add a Review/Rating:** Customers can add a review and 1 to 5 star rating for products they have purchased with verified purchase verification.
- **Review Moderation:** Admins moderate all incoming reviews with one-click approve/reject actions to prevent spam and abuse.
- **View Product Reviews:** Customers can view reviews and ratings on product pages, including 1 decimal average rating score and 1-to-5 star breakdown distribution bars.
- **Delete/Update Reviews:** Admins can delete inappropriate reviews; customers can update or delete their own feedback (with edits returning to pending moderation).
- **PDF Documentation:** `Review_and_Rating_Management_Module_End_User_Documentation.pdf`.

---

## Database Design

### Table: `users`
| Column | Datatype | Constraint | Description |
|---|---|---|---|
| `user_id` | `BIGINT` | PK, Auto Increment | Unique identifier for each customer |
| `first_name` | `VARCHAR(100)` | NOT NULL | Customer's first name |
| `last_name` | `VARCHAR(100)` | NULLABLE | Customer's last name |
| `email` | `VARCHAR(100)` | NOT NULL, UNIQUE | Customer's email address |
| `phone` | `VARCHAR(15)` | NULLABLE | Customer's phone number |
| `created_at` | `DATETIME` | NOT NULL | Timestamp when account was created |
| `updated_at` | `DATETIME` | NOT NULL | Timestamp when account was updated |
| `status` | `BOOLEAN` | NOT NULL | `true` for active, `false` for inactive (soft delete) |

### Table: `orders`
| Column | Datatype | Constraint | Description |
|---|---|---|---|
| `id` | `BIGINT` | PK, Auto Increment | Unique identifier for each order |
| `user_id` | `BIGINT` | FK (`users.user_id`) | References the customer |
| `customer_name` | `VARCHAR(100)` | NOT NULL | Customer display name |
| `total_amount` | `DECIMAL(10,2)` | NOT NULL | Grand total (Subtotal + Tax + Shipping) |
| `subtotal` | `DECIMAL(10,2)` | NOT NULL | Items subtotal |
| `tax_amount` | `DECIMAL(10,2)` | NOT NULL | 5% Tax |
| `shipping_cost` | `DECIMAL(10,2)` | NOT NULL | Shipping fee |
| `order_status` | `VARCHAR(50)` | NOT NULL | Status (`Pending`, `Shipped`, `Delivered`, `Cancelled`) |
| `shipping_address`| `VARCHAR(300)` | NOT NULL | Delivery address |
| `created_at` | `DATETIME` | NOT NULL | Timestamp when placed |
| `updated_at` | `DATETIME` | NOT NULL | Timestamp when updated |
| `status` | `BOOLEAN` | NOT NULL | `true` for active, `false` for cancelled |

### Table: `payments`
| Column | Datatype | Constraint | Description |
|---|---|---|---|
| `payment_id` | `INT / BIGINT` | PK, Auto Increment | Unique identifier for each payment |
| `order_id` | `INT / BIGINT` | FK (`orders.id`) | References the paid order |
| `amount` | `DECIMAL(10,2)` | NOT NULL | Amount paid |
| `payment_method` | `VARCHAR(50)` | NOT NULL | Credit Card, Debit Card, PayPal, Bank Transfer |
| `payment_status` | `VARCHAR(50)` | NOT NULL | `Paid`, `Failed`, or `Refunded` |
| `created_at` | `DATETIME` | NOT NULL | When the payment was processed |
| `updated_at` | `DATETIME` | NOT NULL | When the payment was last updated |

### Table: `carts`
| Column | Datatype | Constraint | Description |
|---|---|---|---|
| `cart_id` | `INT / BIGINT` | PK, Auto Increment | Unique identifier for each cart line item |
| `customer_id` | `INT / BIGINT` | FK (`users.user_id`) | References the customer (shopper) |
| `product_id` | `INT / BIGINT` | FK (`products.id`) | References the product added to the cart |
| `quantity` | `INT` | NOT NULL | Number of units of the product |
| `total_price` | `DECIMAL(10,2)` | NOT NULL | Line total (unit price × quantity) |
| `created_at` | `DATETIME` | NOT NULL | Timestamp when the item was added to cart |
| `updated_at` | `DATETIME` | NOT NULL | Timestamp when the cart item was last updated |

### Table: `shipping`
| Column | Datatype | Constraint | Description |
|---|---|---|---|
| `shipping_id` | `INT / BIGINT` | PK, Auto Increment | Unique identifier for each shipping entry |
| `order_id` | `INT / BIGINT` | FK (`orders.id`) | References the parent order |
| `courier_service` | `VARCHAR(100)` | NOT NULL | Name of courier / logistics provider |
| `tracking_number` | `VARCHAR(100)` | NOT NULL, UNIQUE | Unique courier tracking number |
| `shipping_status` | `VARCHAR(50)` | NOT NULL | `Shipped`, `In Transit`, or `Delivered` |
| `shipping_cost` | `DECIMAL(10,2)` | NOT NULL | Calculated shipping cost |
| `created_at` | `DATETIME` | AUTO-GENERATED | Timestamp when shipping was initiated |
| `updated_at` | `DATETIME` | AUTO-UPDATED | Timestamp when shipping was last updated |

### Table: `reviews`
| Column Name | Data Type | Key / Constraint | Description |
|---|---|---|---|
| `review_id` | `INT / BIGINT` | PK, Auto Increment | Unique identifier for each review |
| `product_id` | `INT / BIGINT` | FK (`products.id`) | References the product |
| `customer_id` | `INT / BIGINT` | FK (`users.user_id`) | References the reviewing customer |
| `rating` | `INT` | NOT NULL (1 to 5) | Star rating (1 to 5 stars) |
| `review_text` | `VARCHAR(1000)` | NULLABLE | Feedback text provided by customer |
| `status` | `BOOLEAN` | DEFAULT `FALSE` | `TRUE` for approved reviews, `FALSE` for unapproved |
| `verified_purchase` | `BOOLEAN` | DEFAULT `FALSE` | `TRUE` if product purchased by customer |
| `created_at` | `DATETIME` | AUTO-GENERATED | Timestamp when review was created |
| `updated_at` | `DATETIME` | AUTO-UPDATED | Timestamp when review was last updated |

---

## API Endpoints Summary

### Customer Management (`/api/customers`)
| Method | Endpoint | Purpose |
|---|---|---|
| `POST` | `/api/customers` | Register / create customer |
| `GET` | `/api/customers` | Get all customers (supports `?status=true/false`) |
| `GET` | `/api/customers/{id}` | Get customer by ID |
| `GET` | `/api/customers/{id}/orders` | Get customer profile with full order history |
| `PUT` | `/api/customers/{id}` | Update customer details |
| `DELETE` / `PUT` | `/api/customers/{id}` / `/api/customers/{id}/deactivate` | Deactivate customer (soft delete) |
| `PUT` | `/api/customers/{id}/activate` | Reactivate customer account |

### Order Management (`/api/orders`)
| Method | Endpoint | Purpose |
|---|---|---|
| `POST` | `/api/orders` | Place a new order with cart items |
| `GET` | `/api/orders` | Get all orders (supports `?status=Pending`, etc.) |
| `GET` | `/api/orders/{id}` | Get detailed order by ID |
| `PUT` | `/api/orders/{id}/status` | Update order status (`Pending`, `Shipped`, `Delivered`, `Cancelled`) |
| `DELETE` / `PUT` | `/api/orders/{id}` / `/api/orders/{id}/cancel` | Soft-delete cancel order (restocks inventory) |

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

### Payment Management (`/api/payments`)
| Method | Endpoint | Purpose |
|---|---|---|
| `POST` | `/api/payments` | Process payment for an order (gateway simulation) |
| `GET` | `/api/payments` | Transaction history (supports `?status=Paid`) |
| `GET` | `/api/payments/stats` | Revenue, paid, failed, and refunded totals |
| `GET` | `/api/payments/{id}` | Get payment by ID |
| `GET` | `/api/payments/order/{orderId}` | Latest payment for an order |
| `POST` | `/api/payments/{id}/refund` | Refund a paid payment and cancel the order |

### Cart Management (`/api/carts`)
| Method | Endpoint | Purpose |
|---|---|---|
| `POST` | `/api/carts` | Add a product to a customer's cart (merges if already present) |
| `GET` | `/api/carts` | List all cart line items (supports `?customerId=X`) |
| `GET` | `/api/carts/stats` | Cart-abandonment analysis stats (total value, unique customers, qty, avg) |
| `GET` | `/api/carts/{id}` | Get one cart row by cart_id |
| `GET` | `/api/carts/customer/{customerId}` | Get a specific customer's cart |
| `GET` | `/api/carts/customer/{customerId}/summary` | Get customer cart grand total, unique items, total qty |
| `PUT` | `/api/carts/{id}/quantity` | Update quantity of a cart item (rechecks stock) |
| `DELETE` | `/api/carts/{id}` | Remove an item from the cart |
| `DELETE` | `/api/carts/customer/{customerId}` | Clear the ENTIRE cart for a customer |

### Shipping Management (`/api/shipping`)
| Method | Endpoint | Purpose |
|---|---|---|
| `GET` | `/api/shipping` | List all shipments (supports `?status=Shipped/In Transit/Delivered`) |
| `GET` | `/api/shipping/stats` | Shipping metrics (Total, Shipped, In Transit, Delivered, Total Cost) |
| `GET` | `/api/shipping/{id}` | Retrieve shipping record by ID |
| `GET` | `/api/shipping/order/{orderId}` | Retrieve shipping details for an order |
| `GET` | `/api/shipping/track/{trackingNumber}` | Public tracking endpoint with milestone timeline |
| `POST` | `/api/shipping/calculate-cost` | Calculate shipping cost based on weight, location, and method |
| `POST` | `/api/shipping` | Create shipment and update linked order status to Shipped |
| `PUT` | `/api/shipping/{id}` | Update courier service, tracking number, or shipping status |
| `PUT` | `/api/shipping/{id}/status` | Quick update status (Shipped, In Transit, Delivered) |

### Review and Rating Management (`/api/reviews`)
| Method | Endpoint | Purpose |
|---|---|---|
| `GET` | `/api/reviews` | List all reviews (supports `?status=` and `?productId=`) |
| `GET` | `/api/reviews/stats` | Global platform review statistics |
| `GET` | `/api/reviews/{id}` | Get review by ID |
| `GET` | `/api/reviews/product/{productId}` | Get approved public reviews for a product |
| `GET` | `/api/reviews/product/{productId}/summary` | Get rating score, review count, and 1-5 star breakdown |
| `GET` | `/api/reviews/customer/{customerId}/purchased-products` | List of products purchased by a customer |
| `POST` | `/api/reviews` | Submit new review and rating (default: unapproved) |
| `PUT` | `/api/reviews/{id}/moderate` | Moderate review (Approve or Reject via `{ status: true/false }`) |
| `PUT` | `/api/reviews/{id}` | Update rating and feedback text (resets to unapproved) |
| `DELETE` | `/api/reviews/{id}` | Delete review (admin or review owner) |

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
