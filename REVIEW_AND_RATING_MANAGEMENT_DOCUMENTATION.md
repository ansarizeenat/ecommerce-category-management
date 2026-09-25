# Review and Rating Management Module - End User Documentation

**Project Name:** E-Commerce Management System  
**Internship Organization:** Itvedant Education Pvt. Ltd.  
**Candidate Name:** Zeenat Ansari  
**GitHub Repository:** [https://github.com/ansarizeenat/ecommerce-category-management](https://github.com/ansarizeenat/ecommerce-category-management)  
**Live Deployed Application:** [https://ecommerce-category-management.onrender.com](https://ecommerce-category-management.onrender.com)  

---

## 1. Introduction

The **Review and Rating Management Module** enables customers to leave feedback and rating evaluations on products they have purchased. This feature helps prospective buyers make well-informed purchasing decisions through genuine social proof, while simultaneously providing store administrators with critical data-driven insights into product quality and customer satisfaction.

The module implements strict moderation safeguards: newly submitted customer reviews default to an **unapproved (pending moderation)** state, empowering store administrators to vet feedback for quality and compliance with community guidelines before publishing them publicly on the storefront.

---

## 2. System Architecture & Tech Stack

- **Backend Framework:** Java 25, Spring Boot 4.1 / Spring Boot 3.x
- **Data Persistence:** Spring Data JPA / Hibernate ORM
- **Database:** MySQL 8.0 (Production & Local) / H2 in-memory (Test suite)
- **Frontend Template:** Thymeleaf, Bootstrap 5.3, Bootstrap Icons, Responsive Vanilla JavaScript
- **API Architecture:** RESTful JSON Web Services
- **Cloud Deployment:** Render Cloud Platform & Docker
- **Documentation:** Markdown & ReportLab Python PDF Generator

---

## 3. Database Design: Table `reviews`

The database schema strictly adheres to the Itvedant assignment specification:

| Column Name | Data Type | Key / Constraint | Description |
|---|---|---|---|
| `review_id` | `INT` (PK, auto_increment) | PRIMARY KEY | Unique identifier for each review |
| `product_id` | `INT` (FK) | FOREIGN KEY → `products.id` | Foreign key referencing the products table |
| `customer_id` | `INT` (FK) | FOREIGN KEY → `users.user_id` | Foreign key referencing the customers/users table |
| `rating` | `INT` | NOT NULL (1 to 5) | Rating given by the customer (1 to 5 stars) |
| `review_text` | `VARCHAR(1000)` | NULLABLE | Textual feedback provided by the customer |
| `status` | `BOOLEAN` | DEFAULT `FALSE` | `TRUE` for approved reviews, `FALSE` for unapproved/pending |
| `verified_purchase` | `BOOLEAN` | DEFAULT `FALSE` | `TRUE` if customer placed a confirmed order for this product |
| `created_at` | `DATETIME` | AUTO-GENERATED | Timestamp when the review was created |
| `updated_at` | `DATETIME` | AUTO-UPDATED | Timestamp when the review was last updated |

---

## 4. Key Functionalities

### 4.1 Add a Review & Rating
- Customers can submit a review and rating for products they have purchased.
- The rating system allows choosing between **1 to 5 stars** alongside textual feedback of up to **1000 characters**.
- **Verified Purchase Verification:** The backend verifies whether the customer has completed a non-cancelled order containing the target product before accepting the review. A bypass flag is also supported for administrative or test scenarios.
- **Pending Moderation Workflow:** Any newly created review is initially stored with `status = FALSE` (unapproved) to prevent spam or abusive content from immediately showing up publicly.

### 4.2 Review Moderation
- Store administrators can moderate all submitted reviews directly from the dedicated moderation dashboard.
- Admins can **Approve** (`status = TRUE`) or **Reject / Unapprove** (`status = FALSE`) reviews based on quality, language, and compliance with guidelines.
- Approved reviews immediately reflect on the public product review showcase and update the product's average score.

### 4.3 View Product Reviews & Rating Summary
- Customers can view reviews and ratings for each product on its dedicated product details page.
- **Rating Summary & Breakdown:**
  - Calculates the **Average Star Rating** (e.g. 4.8 / 5.0) rounded to 1 decimal place.
  - Visual star icons (`★★★★★`).
  - Total count of approved reviews.
  - Star breakdown distribution displaying percentage progress bars for 5-star, 4-star, 3-star, 2-star, and 1-star reviews.
- Only approved reviews (`status = TRUE`) are displayed in the customer public view stream.

### 4.4 Delete & Update Reviews
- **Customer Self-Service:** Customers can update the rating or review text of their own reviews. Once an existing review is edited by a customer, its status automatically resets to **Pending Moderation** to prevent malicious edits after approval.
- Customers can also delete their own reviews.
- **Admin Authority:** Store administrators have full authority to delete inappropriate or abusive reviews at any time.

---

## 5. End User Guide: How to Use the Module

### 5.1 Submitting a Review (Customer Action)
1. Open the application and click on the **Review & Rating** tab in the navigation bar.
2. In the **Add a Review / Rating** form on the left:
   - Select your registered **Customer Name** from the dropdown. (The system will display any verified purchases linked to that account).
   - Select the **Product** you wish to review.
   - Click on the interactive gold stars to select your rating (1 to 5 stars).
   - Enter your detailed feedback in the **Review Feedback** textarea (up to 1000 characters).
   - Click **Submit Review & Rating**.
3. A success notification will appear stating that your review was submitted and is currently **Pending Admin Moderation**.

### 5.2 Browsing Product Reviews (Customer View)
1. In the **Review & Rating** tab, navigate to the **View Product Reviews** card on the right (or click the **Reviews** button next to any product in the Product Catalog).
2. Choose any product from the dropdown.
3. The system displays:
   - Overall Average Rating (e.g., 4.7 / 5.0) and total count of verified reviews.
   - 5-Star distribution percentage bars.
   - Full list of customer reviews, complete with reviewer names, star ratings, submission timestamps, and "Verified Purchase" badges.

### 5.3 Moderating Reviews (Administrator Action)
1. Scroll down to the **Review Moderation & Management Dashboard** table.
2. Filter reviews by status using the quick filter buttons:
   - **All:** View all submitted reviews across all products.
   - **Pending Moderation:** Focus exclusively on new or modified reviews awaiting vetting.
   - **Approved:** View currently published reviews.
3. To approve a review, click the green **Approve** button. The status badge will change to green `Approved`, and the review will immediately become visible to shoppers.
4. To unapprove or reject a review, click the **Reject** button.
5. To permanently delete inappropriate content, click the red **Delete** button.

---

## 6. REST API Endpoints Specification

| HTTP Method | Endpoint | Description | Access Level |
|---|---|---|---|
| `GET` | `/api/reviews` | Retrieve all reviews (supports `?status=` and `?productId=` query params) | Admin / Public |
| `GET` | `/api/reviews/stats` | Global platform review statistics (totals, approved, pending, avg rating) | Public / Admin |
| `GET` | `/api/reviews/{id}` | Get review by unique ID | Public / Admin |
| `GET` | `/api/reviews/product/{productId}` | Retrieve all approved reviews for a specific product | Public |
| `GET` | `/api/reviews/product/{productId}/summary` | Retrieve average rating, review count, and 1-5 star breakdown | Public |
| `GET` | `/api/reviews/customer/{customerId}/purchased-products` | List of products purchased by a customer in completed orders | Customer / Admin |
| `POST` | `/api/reviews` | Submit a new review and rating (default status: unapproved) | Customer |
| `PUT` | `/api/reviews/{id}/moderate` | Moderate review (Approve or Reject via `{ status: true/false }`) | Admin |
| `PUT` | `/api/reviews/{id}` | Update rating or review text (resets status to unapproved) | Customer / Admin |
| `DELETE` | `/api/reviews/{id}` | Delete a review (Admin delete or review owner delete) | Customer / Admin |

---

## 7. Automated Test Suite

A comprehensive test suite was implemented in `src/test/java/com/ecommerce/ecommerce/ReviewServiceTest.java` verifying all business requirements:
1. `testAddReview_Success`: Validates review creation, 1-5 star limits, default pending moderation, and purchase verification.
2. `testAddReview_UnpurchasedProduct_ThrowsException`: Ensures customers cannot review unpurchased products without permission.
3. `testAddReview_InvalidRating_ThrowsException`: Rejects ratings outside 1-5 stars.
4. `testModerateReview_ApproveAndReject`: Validates admin moderation toggle.
5. `testGetProductReviews_OnlyApprovedReviews`: Verifies unapproved reviews remain hidden until approved.
6. `testGetProductRatingSummary`: Tests mathematical calculation of average rating and star percentages.
7. `testUpdateReview_Success`: Verifies review edits reset status back to pending moderation.
8. `testDeleteReview_AdminAndOwner`: Validates access controls for review deletion.
9. `testGetReviewStats`: Verifies platform review metrics and aggregates.

**Result:** `49 / 49 Tests Passed (0 Failures, 0 Errors)`.

---

## 8. Deployment Details

- **GitHub Repository:** [https://github.com/ansarizeenat/ecommerce-category-management](https://github.com/ansarizeenat/ecommerce-category-management)
- **Hosted Cloud Server:** [https://ecommerce-category-management.onrender.com](https://ecommerce-category-management.onrender.com)
- **Build Tool:** Maven 3.9 & Java 25
- **Continuous Deployment:** Automatic build and rollout triggered upon Git push to the `main` branch.
