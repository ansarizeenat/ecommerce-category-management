# Payment Management Module - End User Documentation

**Project Name:** E-Commerce Management System  
**Internship Organization:** Itvedant Education Pvt. Ltd.  
**Candidate Name:** Zeenat Ansari  
**GitHub Repository:** [https://github.com/ansarizeenat/ecommerce-category-management](https://github.com/ansarizeenat/ecommerce-category-management)  
**Live Deployed Application:** [https://ecommerce-category-management.onrender.com](https://ecommerce-category-management.onrender.com)  

---

## 1. Introduction

The **Payment Management Module** handles payment processing and records transaction history for each order. It integrates with third-party payment gateways (simulated Stripe, PayPal, and bank transfer) to securely process payments. Administrators can view every transaction, retry a failed charge, and issue refunds for cancelled or returned orders.

---

## 2. System Architecture & Tech Stack

- **Backend Framework:** Java 25, Spring Boot 4.1
- **ORM & Data Layer:** Spring Data JPA / Hibernate
- **Database:** MySQL 8.0 (`payments` table linked to `orders`)
- **Frontend:** Thymeleaf, Bootstrap 5.3, Bootstrap Icons, Vanilla JavaScript
- **Gateways (simulated):** Stripe (cards), PayPal, Bank Transfer
- **Cloud Deployment:** Render

---

## 3. Database Design: Table `payments`

| Column Name | Data Type | Key / Constraint | Description |
|---|---|---|---|
| `payment_id` | `INT` (PK, auto_increment) | PRIMARY KEY | Unique identifier for each payment |
| `order_id` | `INT` (FK) | FOREIGN KEY → `orders.id` | Order that this payment belongs to |
| `amount` | `DECIMAL(10,2)` | NOT NULL | Amount paid |
| `payment_method` | `VARCHAR(50)` | NOT NULL | Credit Card, Debit Card, PayPal, Bank Transfer |
| `payment_status` | `VARCHAR(50)` | NOT NULL | `Paid`, `Failed`, or `Refunded` |
| `created_at` | `DATETIME` | AUTO-GENERATED | Timestamp when the payment was processed |
| `updated_at` | `DATETIME` | AUTO-UPDATED | Timestamp when the payment was last updated |

An extra `transaction_id` column stores the simulated gateway reference (for example `STRIPE-...` or `PAYPAL-...`). A `refund_reason` column records the reason given when a Paid payment is refunded.

---

## 4. Key Functionalities

### 4.1 Process Payment
- Payments are processed through external payment gateways (Stripe for cards, PayPal, bank transfer).
- Payment methods include credit/debit cards, PayPal, and bank transfers.
- Amount defaults to the order grand total.
- An order cannot be charged twice while a `Paid` record already exists.
- Cancelled orders cannot be charged.
- Failed gateway attempts (demo “Simulate fail”, or test card ending in `0002`) are stored with status `Failed` so they appear on the dashboard. The order can then be retried.

### 4.2 Payment Dashboard
- Admins can view transaction history, including payment ID, amount, payment method, and status.
- Filters: All, Paid, Failed, Refunded.
- Summary cards: collected revenue, paid count, refunded amount, failed count.

### 4.3 Refund Payment
- Admins can issue refunds for cancelled or returned orders.
- Only `Paid` payments can be refunded.
- Refund transactions are logged (`payment_status = Refunded`) and displayed on the dashboard.
- The linked order is cancelled so inventory and order status stay consistent.

---

## 5. How to Use the Screen

1. Open the live app and click **Payment Management**.
2. Place an order first (Order Management) if no unpaid orders exist.
3. Select the unpaid order, choose a payment method, then click **Charge Gateway**.
4. Confirm the new row on the Payment Dashboard (`Paid` or `Failed`).
5. For a return or cancellation, click **Refund** on a Paid row and enter a reason.

You can also open an unpaid order in Order Management and click the card icon to jump to Payment Management with that order selected.

---

## 6. REST API Specifications

| Method | Endpoint | Purpose |
|---|---|---|
| `POST` | `/api/payments` | Process a payment |
| `GET` | `/api/payments` | List transactions (`?status=Paid`) |
| `GET` | `/api/payments/stats` | Dashboard totals |
| `GET` | `/api/payments/{id}` | Get one payment |
| `GET` | `/api/payments/order/{orderId}` | Latest payment for an order |
| `POST` | `/api/payments/{id}/refund` | Issue a refund |

**Process payment sample:**
```json
{
  "orderId": 1,
  "paymentMethod": "Credit Card",
  "cardDetails": "4242424242424242",
  "simulateFailure": false
}
```

**Refund sample:**
```json
{
  "reason": "Order returned by customer"
}
```

---

## 7. Assignment Submission

| Item | Details |
|---|---|
| 1. GitHub Repository Link | https://github.com/ansarizeenat/ecommerce-category-management |
| 2. URL of module hosted on free server | https://ecommerce-category-management.onrender.com |
| 3. End user documentation | This document and `Payment_Management_Module_End_User_Documentation.pdf` |

Upload the assignment in PDF format using the generated file `Payment_Management_Module_End_User_Documentation.pdf`.
