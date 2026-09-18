# Shipping Management Module - End User Documentation

**Project Name:** E-Commerce Management System  
**Internship Organization:** Itvedant Education Pvt. Ltd.  
**Candidate Name:** Zeenat Ansari  
**GitHub Repository:** [https://github.com/ansarizeenat/ecommerce-category-management](https://github.com/ansarizeenat/ecommerce-category-management)  
**Live Deployed Application:** [https://ecommerce-category-management.onrender.com](https://ecommerce-category-management.onrender.com)  

---

## 1. Introduction

The **Shipping Management Module** handles the complete shipping and fulfillment lifecycle for an e-commerce platform. It calculates dynamic shipping costs based on weight, delivery location, and shipping method, provides real-time shipment tracking for customers using courier tracking numbers, and offers an administrative dashboard to manage courier logistics, dispatch packages, and update shipping details. It ensures that products are delivered to customers efficiently, transparently, and on time.

---

## 2. System Architecture & Tech Stack

- **Backend Framework:** Java 25, Spring Boot 4.1 / Spring Boot 3.x
- **Data Persistence:** Spring Data JPA / Hibernate ORM
- **Database:** MySQL 8.0 (Production & Local) / H2 in-memory (Test suite)
- **Frontend Template:** Thymeleaf, Bootstrap 5.3, Bootstrap Icons, Responsive Vanilla JavaScript
- **Logistics & Couriers:** Blue Dart, FedEx, DHL Express, Delhivery, India Post (Speed Post)
- **Cloud Deployment:** Render Cloud Platform & Docker
- **Documentation:** Markdown & ReportLab Python PDF Generator

---

## 3. Database Design: Table `shipping`

The database schema matches the exact Itvedant assignment requirements:

| Column Name | Data Type | Key / Constraint | Description |
|---|---|---|---|
| `shipping_id` | `INT` (PK, auto_increment) | PRIMARY KEY | Unique identifier for each shipping entry |
| `order_id` | `INT` (FK) | FOREIGN KEY → `orders.id` | Foreign key referencing the orders table |
| `courier_service` | `VARCHAR(100)` | NOT NULL | Name of the courier or shipping provider (e.g. Blue Dart, FedEx, DHL, Delhivery) |
| `tracking_number` | `VARCHAR(100)` | NOT NULL, UNIQUE | Unique tracking number provided by the courier |
| `shipping_status` | `VARCHAR(50)` | NOT NULL | Status of the shipment (`Shipped`, `In Transit`, `Delivered`) |
| `shipping_cost` | `DECIMAL(10,2)` | NOT NULL | Shipping cost calculated for the order |
| `created_at` | `DATETIME` | AUTO-GENERATED | Timestamp when the shipping was initiated |
| `updated_at` | `DATETIME` | AUTO-UPDATED | Timestamp when shipping details were last updated |

*Note:* Supporting fields such as `weight`, `shipping_method`, `delivery_location`, and `estimated_delivery` are also persisted to enable detailed rate breakdowns and milestone tracking.

---

## 4. Key Functionalities

### 4.1 Shipping Cost Calculation
- Shipping costs are calculated dynamically based on three primary factors:
  1. **Weight (kg):** Base shipping charge of \$5.00 plus \$2.00 per kilogram.
  2. **Delivery Location:** 
     - **Local (City):** Multiplier 1.0× (1–2 days delivery).
     - **Domestic (National):** Multiplier 1.4× (3–5 days delivery).
     - **International:** Multiplier 2.5× (7–10 days delivery).
  3. **Shipping Method:**
     - **Standard:** Multiplier 1.0×.
     - **Express:** Multiplier 1.5×.
     - **Overnight (Priority):** Multiplier 2.2×.
- **Formula:**
  $$\text{Shipping Cost} = (\text{Base Cost} + \text{Weight} \times \$2.00) \times \text{Location Multiplier} \times \text{Method Multiplier}$$
- Interactive calculator in the UI enables real-time estimation before order dispatch.

### 4.2 Shipping Dashboard
- Administrators can view all orders with their corresponding shipping details:
  - Shipping ID, Order ID, Customer Name, Destination Address, Courier Service, Tracking Number, Status, Cost, and Dispatch Timestamp.
- **Interactive Status Filters:**
  - `All`: Full shipment history.
  - `Shipped`: Dispatched packages handed over to the courier.
  - `In Transit`: Packages moving between transit sorting facilities.
  - `Delivered`: Successfully delivered shipments.
- **Summary Metrics Cards:**
  - Real-time counters for Total Shipments, Dispatched (Shipped), In Transit, and Delivered.

### 4.3 Track Shipment (Customer Self-Service)
- Customers and administrators can track any package by entering the tracking number into the dedicated search box.
- Visual milestone timeline stepper displays:
  1. **Order Confirmed:** Verification and packing.
  2. **Shipped:** Handed over to courier with tracking code.
  3. **In Transit:** Moving through logistics sorting hubs.
  4. **Delivered:** Package delivered to recipient.
- Displays recipient name, shipping address, and estimated delivery date.

### 4.4 Update Shipping Information
- Administrators can update shipping information once an order has been shipped:
  - Modify courier provider (e.g., re-assigned from Blue Dart to FedEx Express).
  - Update tracking number if re-routed.
  - Update shipment status (`Shipped` $\rightarrow$ `In Transit` $\rightarrow$ `Delivered`).
- **Order Synchronization:** Marking a shipment as `Delivered` automatically synchronizes the parent order's status to `Delivered`.

---

## 5. End User Guide: How to Use the Module

### 5.1 As an Administrator (Dispatching & Managing Shipments)
1. Navigate to the **Order Management** or **Shipping Management** tab.
2. In the **Calculate Cost & Dispatch Order** card:
   - Select an active unshipped order from the dropdown.
   - Select the courier service (Blue Dart, FedEx, DHL, Delhivery, Speed Post).
   - Adjust weight, delivery location, and shipping method (the cost preview updates live).
   - Enter a custom tracking number or leave blank to auto-generate a realistic courier tracking code.
   - Click **Create Shipment & Dispatch**.
3. The shipment immediately appears on the **Shipping Dashboard** table, and the order status transitions to `Shipped`.
4. To update details, click the **Pencil (Edit)** button in the Actions column of any shipment:
   - Update the courier, tracking number, or change the status to `In Transit` or `Delivered`.
   - Click **Save Changes**.

### 5.2 As a Customer (Tracking a Package)
1. Open the application and click the **Shipping Management** tab.
2. In the **Track Shipment (Customer)** section, enter the courier tracking number (e.g., `BD-73829103` or `FDX-19283746`).
3. Click **Track**.
4. The system renders the live tracking card displaying current delivery status, destination address, and interactive milestone progress timeline.

---

## 6. REST API Specifications

| HTTP Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/shipping` | Retrieve all shipments (optional `?status=Shipped`) |
| `GET` | `/api/shipping/stats` | Retrieve metrics (total, shipped, in transit, delivered, total cost) |
| `GET` | `/api/shipping/{id}` | Retrieve single shipment by ID |
| `GET` | `/api/shipping/order/{orderId}` | Retrieve shipment by Order ID |
| `GET` | `/api/shipping/track/{trackingNumber}` | Public tracking endpoint with milestone timeline |
| `POST` | `/api/shipping/calculate-cost` | Calculate dynamic shipping cost |
| `POST` | `/api/shipping` | Create and dispatch new shipment |
| `PUT` | `/api/shipping/{id}` | Update courier service, tracking number, or status |
| `PUT` | `/api/shipping/{id}/status` | Quick update shipment status |

### Sample Request: Calculate Shipping Cost
```http
POST /api/shipping/calculate-cost
Content-Type: application/json

{
  "weight": 2.5,
  "deliveryLocation": "Domestic",
  "shippingMethod": "Express"
}
```

### Sample Response:
```json
{
  "baseCost": 5.00,
  "weightCost": 5.00,
  "locationMultiplier": 1.40,
  "methodMultiplier": 1.50,
  "totalShippingCost": 21.00,
  "estimatedDeliveryDays": 2,
  "breakdown": "Base: $5.00 + Weight (2.5kg): $5.00 x Loc (Domestic: 1.4x) x Method (Express: 1.5x) = $21.00"
}
```

### Sample Request: Create Shipment
```http
POST /api/shipping
Content-Type: application/json

{
  "orderId": 1,
  "courierService": "Blue Dart",
  "trackingNumber": "",
  "weight": 1.5,
  "deliveryLocation": "Domestic",
  "shippingMethod": "Standard"
}
```

---

## 7. Assignment Submission Details

| Item | Submission Details |
|---|---|
| **1. GitHub Repository Link** | [https://github.com/ansarizeenat/ecommerce-category-management](https://github.com/ansarizeenat/ecommerce-category-management) |
| **2. URL of Module Hosted on Free Server** | [https://ecommerce-category-management.onrender.com](https://ecommerce-category-management.onrender.com) |
| **3. End User Documentation** | `SHIPPING_MANAGEMENT_DOCUMENTATION.md` and `Shipping_Management_Module_End_User_Documentation.pdf` |

> **Note:** The assignment has been compiled into the PDF file `Shipping_Management_Module_End_User_Documentation.pdf` ready for submission on the Itvedant portal.
