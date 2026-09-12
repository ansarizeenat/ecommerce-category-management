import sys
from reportlab.lib.pagesizes import letter
from reportlab.lib import colors
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.platypus import (
    SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle, HRFlowable
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
        
        # Header (pages > 1)
        if self._pageNumber > 1:
            self.drawString(54, 750, "Itvedant Internship — Customer Management Module Documentation")
            self.setStrokeColor(colors.HexColor("#dee2e6"))
            self.setLineWidth(0.5)
            self.line(54, 742, 558, 742)
            
        # Footer
        footer_text = f"Page {self._pageNumber} of {page_count}"
        self.drawRightString(558, 36, footer_text)
        self.drawString(54, 36, "Candidate: Zeenat Ansari | E-Commerce System")
        self.setStrokeColor(colors.HexColor("#dee2e6"))
        self.setLineWidth(0.5)
        self.line(54, 48, 558, 48)
        
        self.restoreState()

def generate_pdf(output_filename="Customer_Management_Module_End_User_Documentation.pdf"):
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
        'DocTitle',
        parent=styles['Heading1'],
        fontName='Helvetica-Bold',
        fontSize=20,
        leading=24,
        textColor=primary_color,
        spaceAfter=6
    )
    
    subtitle_style = ParagraphStyle(
        'DocSubtitle',
        parent=styles['Normal'],
        fontName='Helvetica',
        fontSize=11,
        leading=15,
        textColor=colors.HexColor("#495057"),
        spaceAfter=12
    )
    
    h1_style = ParagraphStyle(
        'SectionH1',
        parent=styles['Heading2'],
        fontName='Helvetica-Bold',
        fontSize=13,
        leading=17,
        textColor=primary_color,
        spaceBefore=12,
        spaceAfter=6,
        keepWithNext=True
    )
    
    body_style = ParagraphStyle(
        'BodyDark',
        parent=styles['Normal'],
        fontName='Helvetica',
        fontSize=9,
        leading=13,
        textColor=dark_neutral,
        spaceAfter=6
    )
    
    code_style = ParagraphStyle(
        'CodeSnippet',
        parent=styles['Normal'],
        fontName='Courier',
        fontSize=8,
        leading=10.5,
        textColor=colors.HexColor("#1e293b")
    )
    
    table_cell = ParagraphStyle(
        'TableCell',
        parent=styles['Normal'],
        fontName='Helvetica',
        fontSize=8,
        leading=10.5,
        textColor=dark_neutral
    )
    
    table_header = ParagraphStyle(
        'TableHeader',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=8.5,
        leading=11,
        textColor=colors.white
    )
    
    elements = []
    
    # Title & Subtitle
    elements.append(Paragraph("E-Commerce Management System", title_style))
    elements.append(Paragraph("<b>Module:</b> Customer Management Module — End User & Technical Documentation", subtitle_style))
    elements.append(HRFlowable(width="100%", thickness=2, color=primary_color, spaceAfter=10))
    
    # Metadata Table
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
        ('BACKGROUND', (0,0), (-1,-1), colors.HexColor("#f1f5f9")),
        ('BOX', (0,0), (-1,-1), 1, colors.HexColor("#cbd5e1")),
        ('INNERGRID', (0,0), (-1,-1), 0.5, colors.HexColor("#e2e8f0")),
        ('TOPPADDING', (0,0), (-1,-1), 5),
        ('BOTTOMPADDING', (0,0), (-1,-1), 5),
        ('LEFTPADDING', (0,0), (-1,-1), 8),
        ('RIGHTPADDING', (0,0), (-1,-1), 8),
    ]))
    elements.append(meta_table)
    elements.append(Spacer(1, 10))
    
    # 1. Introduction
    elements.append(Paragraph("1. Introduction", h1_style))
    intro_p = (
        "The <b>Customer Management Module</b> manages customer data, including personal contact information, "
        "order history, and account status settings. It enables customers to register directly through the frontend, "
        "allows administrators to onboard and modify customer profiles, inspect their lifetime purchase history and orders, "
        "and safely deactivate customer accounts via a non-destructive soft-delete mechanism."
    )
    elements.append(Paragraph(intro_p, body_style))
    
    # 2. Database Design
    elements.append(Paragraph("2. Database Design & Specification", h1_style))
    elements.append(Paragraph("The database schema strictly conforms to the Itvedant design specification for the <code>users</code> table:", body_style))
    
    users_table_data = [
        [Paragraph("Column Name", table_header), Paragraph("Data Type", table_header), Paragraph("Key / Constraint", table_header), Paragraph("Description", table_header)],
        [Paragraph("user_id", table_cell), Paragraph("INT / BIGINT", table_cell), Paragraph("PK, Auto-Increment", table_cell), Paragraph("Unique identifier for each customer", table_cell)],
        [Paragraph("first_name", table_cell), Paragraph("VARCHAR(100)", table_cell), Paragraph("NOT NULL", table_cell), Paragraph("Customer's first name", table_cell)],
        [Paragraph("last_name", table_cell), Paragraph("VARCHAR(100)", table_cell), Paragraph("NULLABLE", table_cell), Paragraph("Customer's last name", table_cell)],
        [Paragraph("email", table_cell), Paragraph("VARCHAR(100)", table_cell), Paragraph("NOT NULL, UNIQUE", table_cell), Paragraph("Customer's unique email address", table_cell)],
        [Paragraph("phone", table_cell), Paragraph("VARCHAR(15)", table_cell), Paragraph("NULLABLE", table_cell), Paragraph("Customer's contact phone number", table_cell)],
        [Paragraph("created_at", table_cell), Paragraph("DATETIME", table_cell), Paragraph("AUTO-GENERATED", table_cell), Paragraph("Timestamp when customer account was created", table_cell)],
        [Paragraph("updated_at", table_cell), Paragraph("DATETIME", table_cell), Paragraph("AUTO-UPDATED", table_cell), Paragraph("Timestamp when customer details were last updated", table_cell)],
        [Paragraph("status", table_cell), Paragraph("BOOLEAN", table_cell), Paragraph("NOT NULL, DEFAULT TRUE", table_cell), Paragraph("True for active customers, False for inactive (soft delete)", table_cell)]
    ]
    t_users = Table(users_table_data, colWidths=[85, 80, 115, 224])
    t_users.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,0), primary_color),
        ('ALIGN', (0,0), (-1,-1), 'LEFT'),
        ('VALIGN', (0,0), (-1,-1), 'MIDDLE'),
        ('ROWBACKGROUNDS', (0,1), (-1,-1), [colors.white, colors.HexColor("#f8fafc")]),
        ('GRID', (0,0), (-1,-1), 0.5, colors.HexColor("#cbd5e1")),
        ('TOPPADDING', (0,0), (-1,-1), 4),
        ('BOTTOMPADDING', (0,0), (-1,-1), 4),
    ]))
    elements.append(t_users)
    elements.append(Spacer(1, 10))
    
    # 3. Core Functionalities
    elements.append(Paragraph("3. Core Functionalities & Business Logic", h1_style))
    func_text = (
        "<b>1. Add a New Customer:</b> Customers register through the web interface or administrators add customer details manually. "
        "The system enforces mandatory fields (First Name, Email) and unique email validation to eliminate duplicate accounts.<br/><br/>"
        "<b>2. Customer Dashboard:</b> Administrators view all customers, contact details, account status, lifetime spend, and total orders placed. "
        "Quick status filters (All, Active, Inactive) streamline account management. An integrated <b>Order History Modal</b> displays "
        "every order placed by the selected customer.<br/><br/>"
        "<b>3. Update Customer Details:</b> Administrators can modify customer names, email addresses, phone numbers, and status. "
        "Email collision checks ensure integrity, and the <code>updated_at</code> timestamp is recorded.<br/><br/>"
        "<b>4. Delete / Deactivate Customer Account (Soft Delete):</b> Administrators can deactivate accounts without removing data. "
        "Sets <code>status = false</code> while retaining historical orders and metrics. Deactivated accounts can be reactivated with one click."
    )
    elements.append(Paragraph(func_text, body_style))
    elements.append(Spacer(1, 10))
    
    # 4. REST API Endpoints
    elements.append(Paragraph("4. REST API Endpoint Specifications", h1_style))
    api_table_data = [
        [Paragraph("Method", table_header), Paragraph("Endpoint", table_header), Paragraph("Description", table_header), Paragraph("Sample Payload / Response", table_header)],
        [
            Paragraph("POST", table_cell),
            Paragraph("<b>/api/customers</b>", table_cell),
            Paragraph("Add / Register customer", table_cell),
            Paragraph("<code>{\"firstName\":\"Zeenat\", \"lastName\":\"Ansari\", \"email\":\"zeenat@example.com\", \"phone\":\"+91 9876543210\"}</code>", code_style)
        ],
        [
            Paragraph("GET", table_cell),
            Paragraph("<b>/api/customers</b>", table_cell),
            Paragraph("Get all customers (supports <code>?status=true/false</code>)", table_cell),
            Paragraph("Returns list of CustomerDTOs with total orders and lifetime spend", table_cell)
        ],
        [
            Paragraph("GET", table_cell),
            Paragraph("<b>/api/customers/{id}</b>", table_cell),
            Paragraph("Get customer profile by ID", table_cell),
            Paragraph("Returns customer entity JSON", table_cell)
        ],
        [
            Paragraph("GET", table_cell),
            Paragraph("<b>/api/customers/{id}/orders</b>", table_cell),
            Paragraph("Get customer with full order history", table_cell),
            Paragraph("Returns customer profile + array of orders placed", table_cell)
        ],
        [
            Paragraph("PUT", table_cell),
            Paragraph("<b>/api/customers/{id}</b>", table_cell),
            Paragraph("Update customer details", table_cell),
            Paragraph("<code>{\"firstName\":\"Zeenat\", \"email\":\"zeenat.new@example.com\"}</code>", code_style)
        ],
        [
            Paragraph("DELETE / PUT", table_cell),
            Paragraph("<b>/api/customers/{id}</b><br/><b>/api/customers/{id}/deactivate</b>", table_cell),
            Paragraph("Soft delete / Deactivate account", table_cell),
            Paragraph("Sets status=false, updates timestamp", table_cell)
        ],
        [
            Paragraph("PUT", table_cell),
            Paragraph("<b>/api/customers/{id}/activate</b>", table_cell),
            Paragraph("Reactivate customer account", table_cell),
            Paragraph("Sets status=true, updates timestamp", table_cell)
        ]
    ]
    t_api = Table(api_table_data, colWidths=[65, 130, 145, 164])
    t_api.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,0), primary_color),
        ('ALIGN', (0,0), (-1,-1), 'LEFT'),
        ('VALIGN', (0,0), (-1,-1), 'TOP'),
        ('ROWBACKGROUNDS', (0,1), (-1,-1), [colors.white, colors.HexColor("#f8fafc")]),
        ('GRID', (0,0), (-1,-1), 0.5, colors.HexColor("#cbd5e1")),
        ('TOPPADDING', (0,0), (-1,-1), 4),
        ('BOTTOMPADDING', (0,0), (-1,-1), 4),
    ]))
    elements.append(t_api)
    elements.append(Spacer(1, 10))
    
    # 5. Verification & Testing
    elements.append(Paragraph("5. Verification & Automated Test Results", h1_style))
    test_p = (
        "Automated unit and integration tests were implemented in <code>CustomerServiceTest.java</code>:<br/>"
        "• <b>testCreateCustomer_Success:</b> Validated registration, default active status, and auto-timestamps.<br/>"
        "• <b>testCreateCustomer_DuplicateEmailThrowsException:</b> Validated unique email constraint.<br/>"
        "• <b>testUpdateCustomer_Success:</b> Validated customer profile editing.<br/>"
        "• <b>testDeactivateCustomer_SoftDelete:</b> Validated soft delete behavior (status=false, record preserved).<br/>"
        "• <b>testActivateCustomer_Reactivation:</b> Validated reactivation back to active status.<br/>"
        "• <b>testGetCustomerWithOrders_OrderHistory:</b> Validated customer order history linkage.<br/>"
        "<b>Suite Test Summary:</b> <code>[INFO] Tests run: 13, Failures: 0, Errors: 0, Skipped: 0 — BUILD SUCCESS</code>"
    )
    elements.append(Paragraph(test_p, body_style))
    elements.append(Spacer(1, 10))
    
    # 6. Assignment Deliverables
    elements.append(Paragraph("6. Assignment Submission Summary", h1_style))
    summary_data = [
        [Paragraph("<b>Item</b>", table_header), Paragraph("<b>Details & Links</b>", table_header)],
        [Paragraph("1. GitHub Repository", table_cell), Paragraph("<b>https://github.com/ansarizeenat/ecommerce-category-management</b>", table_cell)],
        [Paragraph("2. Free Cloud Hosted URL", table_cell), Paragraph("<b>https://ecommerce-category-management.onrender.com</b>", table_cell)],
        [Paragraph("3. End User Documentation", table_cell), Paragraph("Prepared in PDF format (attached document).", table_cell)],
        [Paragraph("4. Modules Completed", table_cell), Paragraph("1. Category Management<br/>2. Product Management<br/>3. Order Management<br/>4. Customer Management", table_cell)]
    ]
    t_summary = Table(summary_data, colWidths=[160, 344])
    t_summary.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,0), primary_color),
        ('ALIGN', (0,0), (-1,-1), 'LEFT'),
        ('VALIGN', (0,0), (-1,-1), 'MIDDLE'),
        ('ROWBACKGROUNDS', (0,1), (-1,-1), [colors.white, colors.HexColor("#f8fafc")]),
        ('GRID', (0,0), (-1,-1), 0.5, colors.HexColor("#cbd5e1")),
        ('TOPPADDING', (0,0), (-1,-1), 5),
        ('BOTTOMPADDING', (0,0), (-1,-1), 5),
    ]))
    elements.append(t_summary)
    
    doc.build(elements, canvasmaker=NumberedCanvas)
    print(f"Customer Documentation PDF generated successfully at: {output_filename}")

if __name__ == "__main__":
    out_file = sys.argv[1] if len(sys.argv) > 1 else "Customer_Management_Module_End_User_Documentation.pdf"
    generate_pdf(out_file)
