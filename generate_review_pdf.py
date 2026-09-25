import sys
from reportlab.lib.pagesizes import letter
from reportlab.lib import colors
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.platypus import (
    SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle, HRFlowable, KeepTogether
)
from reportlab.pdfgen import canvas

class NumberedCanvas(canvas.Canvas):
    def __init__(self, *args, **kwargs):
        super(NumberedCanvas, self).__init__(*args, **kwargs)
        self._saved_page_states = []

    def showPage(self):
        self._saved_page_states.append(dict(self.__dict__))
        self._startPage()

    def save(self):
        num_pages = len(self._saved_page_states)
        for state in self._saved_page_states:
            self.__dict__.update(state)
            self.draw_page_decorations(num_pages)
            super(NumberedCanvas, self).showPage()
        super(NumberedCanvas, self).save()

    def draw_page_decorations(self, page_count):
        self.saveState()
        self.setFont("Helvetica", 9)
        self.setFillColor(colors.HexColor("#6c757d"))
        if self._pageNumber > 1:
            self.drawString(54, 750, "Itvedant Internship — Review & Rating Management Module Documentation")
            self.setStrokeColor(colors.HexColor("#dee2e6"))
            self.setLineWidth(0.5)
            self.line(54, 742, 558, 742)
        footer_text = f"Page {self._pageNumber} of {page_count}"
        self.drawRightString(558, 36, footer_text)
        self.drawString(54, 36, "Candidate: Zeenat Ansari | E-Commerce System")
        self.setStrokeColor(colors.HexColor("#dee2e6"))
        self.setLineWidth(0.5)
        self.line(54, 48, 558, 48)
        self.restoreState()

def generate_pdf(output_filename="Review_and_Rating_Management_Module_End_User_Documentation.pdf"):
    doc = SimpleDocTemplate(
        output_filename,
        pagesize=letter,
        leftMargin=54,
        rightMargin=54,
        topMargin=54,
        bottomMargin=54
    )

    styles = getSampleStyleSheet()
    primary_color = colors.HexColor("#1f3c88")
    accent_color = colors.HexColor("#0779e4")
    dark_neutral = colors.HexColor("#212529")

    title_style = ParagraphStyle(
        'DocTitle', parent=styles['Heading1'], fontName='Helvetica-Bold',
        fontSize=20, leading=24, textColor=primary_color, spaceAfter=6
    )
    subtitle_style = ParagraphStyle(
        'DocSubtitle', parent=styles['Normal'], fontName='Helvetica',
        fontSize=11, leading=15, textColor=colors.HexColor("#495057"), spaceAfter=12
    )
    h1_style = ParagraphStyle(
        'SectionH1', parent=styles['Heading2'], fontName='Helvetica-Bold',
        fontSize=13, leading=17, textColor=primary_color, spaceBefore=12, spaceAfter=6, keepWithNext=True
    )
    h2_style = ParagraphStyle(
        'SectionH2', parent=styles['Heading3'], fontName='Helvetica-Bold',
        fontSize=10.5, leading=14, textColor=accent_color, spaceBefore=8, spaceAfter=4, keepWithNext=True
    )
    body_style = ParagraphStyle(
        'BodyDark', parent=styles['Normal'], fontName='Helvetica',
        fontSize=9, leading=13, textColor=dark_neutral, spaceAfter=6
    )
    bullet_style = ParagraphStyle(
        'BulletStyle', parent=styles['Normal'], fontName='Helvetica',
        fontSize=9, leading=13, textColor=dark_neutral, leftIndent=15, spaceAfter=4
    )
    table_cell = ParagraphStyle(
        'TableCell', parent=styles['Normal'], fontName='Helvetica',
        fontSize=8, leading=10.5, textColor=dark_neutral
    )
    table_header = ParagraphStyle(
        'TableHeader', parent=styles['Normal'], fontName='Helvetica-Bold',
        fontSize=8.5, leading=11, textColor=colors.white
    )

    elements = []
    elements.append(Paragraph("E-Commerce Management System", title_style))
    elements.append(Paragraph("<b>Module:</b> Review and Rating Management Module — End User &amp; Technical Documentation", subtitle_style))
    elements.append(HRFlowable(width="100%", thickness=2, color=primary_color, spaceAfter=10))

    meta_data = [
        [
            Paragraph("<b>Organization:</b> Itvedant Education Pvt. Ltd.", body_style),
            Paragraph("<b>Candidate Name:</b> Zeenat Ansari", body_style)
        ],
        [
            Paragraph("<b>GitHub Repo:</b> ansarizeenat/ecommerce-category-management", body_style),
            Paragraph("<b>Live Server (Render):</b> https://ecommerce-category-management.onrender.com", body_style)
        ],
        [
            Paragraph("<b>Tech Stack:</b> Java 25, Spring Boot 4.1, JPA, MySQL 8.0, Thymeleaf", body_style),
            Paragraph("<b>Date:</b> September 2026", body_style)
        ]
    ]
    meta_table = Table(meta_data, colWidths=[250, 254])
    meta_table.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, -1), colors.HexColor("#f1f5f9")),
        ('BOX', (0, 0), (-1, -1), 1, colors.HexColor("#cbd5e1")),
        ('INNERGRID', (0, 0), (-1, -1), 0.5, colors.HexColor("#e2e8f0")),
        ('TOPPADDING', (0, 0), (-1, -1), 5),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 5),
        ('LEFTPADDING', (0, 0), (-1, -1), 8),
        ('RIGHTPADDING', (0, 0), (-1, -1), 8),
    ]))
    elements.append(meta_table)
    elements.append(Spacer(1, 10))

    elements.append(Paragraph("1. Introduction", h1_style))
    elements.append(Paragraph(
        "The <b>Review and Rating Management Module</b> enables customers to leave feedback on products they have "
        "purchased. This feature helps prospective customers make well-informed purchasing decisions based on genuine "
        "customer experiences and allows businesses to gather actionable insights about product quality and customer satisfaction.",
        body_style
    ))
    elements.append(Paragraph(
        "To preserve review integrity and protect the platform against spam, abuse, or inappropriate content, "
        "all newly submitted customer reviews default to an <b>unapproved (pending moderation)</b> state. "
        "Administrators review each submission and selectively approve or reject them before they become visible to the public.",
        body_style
    ))

    elements.append(Paragraph("2. Database Design &amp; Specification", h1_style))
    elements.append(Paragraph("Table Name: <b>reviews</b> — strictly matches the Itvedant assignment specification.", body_style))
    elements.append(Paragraph("<b>Table 1: reviews</b>", h2_style))

    review_table_data = [
        [Paragraph("Column Name", table_header), Paragraph("Data Type", table_header), Paragraph("Key / Constraint", table_header), Paragraph("Description", table_header)],
        [Paragraph("review_id", table_cell), Paragraph("INT", table_cell), Paragraph("PK, Auto-Increment", table_cell), Paragraph("Unique identifier for each review", table_cell)],
        [Paragraph("product_id", table_cell), Paragraph("INT", table_cell), Paragraph("FK (products.id)", table_cell), Paragraph("Foreign key referencing the products table", table_cell)],
        [Paragraph("customer_id", table_cell), Paragraph("INT", table_cell), Paragraph("FK (users.user_id)", table_cell), Paragraph("Foreign key referencing the users / customers table", table_cell)],
        [Paragraph("rating", table_cell), Paragraph("INT", table_cell), Paragraph("NOT NULL (1 to 5)", table_cell), Paragraph("Rating given by the customer (1 to 5 stars)", table_cell)],
        [Paragraph("review_text", table_cell), Paragraph("VARCHAR(1000)", table_cell), Paragraph("NULLABLE", table_cell), Paragraph("Textual feedback provided by the customer", table_cell)],
        [Paragraph("status", table_cell), Paragraph("BOOLEAN", table_cell), Paragraph("DEFAULT FALSE", table_cell), Paragraph("True for approved reviews, False for unapproved/pending", table_cell)],
        [Paragraph("verified_purchase", table_cell), Paragraph("BOOLEAN", table_cell), Paragraph("DEFAULT FALSE", table_cell), Paragraph("True if customer purchased this item in confirmed order", table_cell)],
        [Paragraph("created_at", table_cell), Paragraph("DATETIME", table_cell), Paragraph("AUTO-GENERATED", table_cell), Paragraph("Timestamp when the review was created", table_cell)],
        [Paragraph("updated_at", table_cell), Paragraph("DATETIME", table_cell), Paragraph("AUTO-UPDATED", table_cell), Paragraph("Timestamp when the review was last updated", table_cell)],
    ]
    t_rev = Table(review_table_data, colWidths=[90, 75, 105, 234])
    t_rev.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, 0), primary_color),
        ('BOX', (0, 0), (-1, -1), 1, colors.HexColor("#cbd5e1")),
        ('INNERGRID', (0, 0), (-1, -1), 0.5, colors.HexColor("#e2e8f0")),
        ('ROWBACKGROUNDS', (0, 1), (-1, -1), [colors.white, colors.HexColor("#f8fafc")]),
        ('TOPPADDING', (0, 0), (-1, -1), 4),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 4),
        ('LEFTPADDING', (0, 0), (-1, -1), 6),
        ('RIGHTPADDING', (0, 0), (-1, -1), 6),
    ]))
    elements.append(t_rev)
    elements.append(Spacer(1, 10))

    elements.append(Paragraph("3. Key Functionalities", h1_style))

    elements.append(Paragraph("<b>3.1 Add a Review &amp; Rating</b>", h2_style))
    elements.append(Paragraph(
        "• Customers can add a review and rating for products they have purchased.<br/>"
        "• The system allows selecting a score between 1 and 5 stars with an interactive star selector.<br/>"
        "• Provides textual feedback of up to 1000 characters with live character counter.<br/>"
        "• <b>Purchase Verification:</b> Confirms that the customer has placed an order containing the item.<br/>"
        "• <b>Default Moderation:</b> Reviews automatically default to unapproved (status = False) until approved by Admin.",
        bullet_style
    ))

    elements.append(Paragraph("<b>3.2 Review Moderation</b>", h2_style))
    elements.append(Paragraph(
        "• Administrators can moderate reviews, approving or rejecting them based on quality and adherence to guidelines.<br/>"
        "• Moderation dashboard allows filtering by status (All, Pending Moderation, Approved) and by product.<br/>"
        "• One-click Approval marks status = True, instantly publishing it to the public storefront.<br/>"
        "• Reject / Unapprove toggles status back to False, hiding it from public display.",
        bullet_style
    ))

    elements.append(Paragraph("<b>3.3 View Product Reviews &amp; Rating Breakdown</b>", h2_style))
    elements.append(Paragraph(
        "• Customers can view reviews and ratings for each product on its product page / details view.<br/>"
        "• Computes the average rating (e.g. 4.8 / 5.0) rounded to 1 decimal place.<br/>"
        "• Provides a visual star distribution breakdown showing progress bars and percentages for 1 to 5 stars.<br/>"
        "• Displays reviewer name, verified purchase badge, star rating, creation date, and review text.",
        bullet_style
    ))

    elements.append(Paragraph("<b>3.4 Delete / Update Reviews</b>", h2_style))
    elements.append(Paragraph(
        "• Admins can delete inappropriate, profane, or abusive reviews at any time.<br/>"
        "• Customers can update or delete their own reviews.<br/>"
        "• When an approved review is updated by a customer, its status automatically reverts to Pending Moderation.",
        bullet_style
    ))

    elements.append(Spacer(1, 8))
    elements.append(Paragraph("4. End User Guide", h1_style))
    elements.append(Paragraph(
        "<b>Customer Workflow:</b><br/>"
        "1. Navigate to the <b>Review &amp; Rating</b> tab.<br/>"
        "2. Select your Customer account and the Product you wish to review.<br/>"
        "3. Click on the 1-5 gold stars to set your rating score.<br/>"
        "4. Write your experience in the feedback box and click <b>Submit Review &amp; Rating</b>.<br/>"
        "5. The review is submitted and queued for Admin moderation.<br/><br/>"
        "<b>Admin Moderation Workflow:</b><br/>"
        "1. Open the <b>Review Moderation &amp; Management Dashboard</b> at the bottom of the Reviews tab.<br/>"
        "2. Click <b>Pending Moderation</b> filter to view newly submitted reviews.<br/>"
        "3. Click <b>Approve</b> to publish the review, or <b>Reject</b> if it violates guidelines.<br/>"
        "4. Inappropriate submissions can be permanently removed with the <b>Delete</b> button.",
        body_style
    ))

    elements.append(Spacer(1, 8))
    elements.append(Paragraph("5. REST API Endpoints Reference", h1_style))

    api_table_data = [
        [Paragraph("Method", table_header), Paragraph("Endpoint", table_header), Paragraph("Description", table_header), Paragraph("Access", table_header)],
        [Paragraph("GET", table_cell), Paragraph("/api/reviews", table_cell), Paragraph("List all reviews with optional status/productId filters", table_cell), Paragraph("Public / Admin", table_cell)],
        [Paragraph("GET", table_cell), Paragraph("/api/reviews/stats", table_cell), Paragraph("Platform statistics: totals, approved, pending, avg rating", table_cell), Paragraph("Public / Admin", table_cell)],
        [Paragraph("GET", table_cell), Paragraph("/api/reviews/{id}", table_cell), Paragraph("Fetch review by primary key ID", table_cell), Paragraph("Public / Admin", table_cell)],
        [Paragraph("GET", table_cell), Paragraph("/api/reviews/product/{id}", table_cell), Paragraph("Get public approved reviews for a product", table_cell), Paragraph("Public", table_cell)],
        [Paragraph("GET", table_cell), Paragraph("/api/reviews/product/{id}/summary", table_cell), Paragraph("Average rating score and 1-5 star breakdown bars", table_cell), Paragraph("Public", table_cell)],
        [Paragraph("POST", table_cell), Paragraph("/api/reviews", table_cell), Paragraph("Submit review (default status: unapproved / pending)", table_cell), Paragraph("Customer", table_cell)],
        [Paragraph("PUT", table_cell), Paragraph("/api/reviews/{id}/moderate", table_cell), Paragraph("Moderate review: approve (true) or reject (false)", table_cell), Paragraph("Admin", table_cell)],
        [Paragraph("PUT", table_cell), Paragraph("/api/reviews/{id}", table_cell), Paragraph("Update review (resets status to unapproved)", table_cell), Paragraph("Customer / Admin", table_cell)],
        [Paragraph("DELETE", table_cell), Paragraph("/api/reviews/{id}", table_cell), Paragraph("Delete review (admin or owner authorization)", table_cell), Paragraph("Customer / Admin", table_cell)],
    ]
    t_api = Table(api_table_data, colWidths=[55, 175, 214, 60])
    t_api.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, 0), primary_color),
        ('BOX', (0, 0), (-1, -1), 1, colors.HexColor("#cbd5e1")),
        ('INNERGRID', (0, 0), (-1, -1), 0.5, colors.HexColor("#e2e8f0")),
        ('ROWBACKGROUNDS', (0, 1), (-1, -1), [colors.white, colors.HexColor("#f8fafc")]),
        ('TOPPADDING', (0, 0), (-1, -1), 3.5),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 3.5),
        ('LEFTPADDING', (0, 0), (-1, -1), 5),
        ('RIGHTPADDING', (0, 0), (-1, -1), 5),
    ]))
    elements.append(t_api)

    elements.append(Spacer(1, 10))
    elements.append(Paragraph("6. Automated Testing &amp; Verification", h1_style))
    elements.append(Paragraph(
        "A suite of 9 JUnit 5 Spring Boot integration tests was created in <code>ReviewServiceTest.java</code>:<br/>"
        "• Validated review creation, 1 to 5 star rating boundaries, and default unapproved status.<br/>"
        "• Verified purchase enforcement preventing unpurchased item reviews without permission.<br/>"
        "• Verified admin moderation (Approve / Reject workflow) and public visibility toggling.<br/>"
        "• Verified average rating calculation and percentage distribution calculation.<br/>"
        "• Verified customer update resetting status to pending, and admin delete privileges.<br/>"
        "<b>Suite Result:</b> 49 / 49 tests passing across all system modules (0 failures, 0 errors).",
        body_style
    ))

    doc.build(elements, canvasmaker=NumberedCanvas)
    print(f"Documentation PDF generated successfully: {output_filename}")

if __name__ == "__main__":
    generate_pdf()
