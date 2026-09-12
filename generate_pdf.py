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
        
        # Header (pages > 1)
        if self._pageNumber > 1:
            self.drawString(54, 750, "Itvedant Internship — Order Management Module Documentation")
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

def generate_pdf(output_filename="Order_Management_Module_End_User_Documentation.pdf"):
    doc = SimpleDocTemplate(
        output_filename,
        pagesize=letter,
        leftMargin=54,
        rightMargin=54,
        topMargin=54,
        bottomMargin=54
    )
    
    styles = getSampleStyleSheet()
    
    # Custom styles
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
    
    h2_style = ParagraphStyle(
        'SectionH2',
        parent=styles['Heading3'],
        fontName='Helvetica-Bold',
        fontSize=10.5,
        leading=14,
        textColor=accent_color,
        spaceBefore=8,
        spaceAfter=4,
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
    
    # Header Banner
    elements.append(Paragraph("E-Commerce Management System", title_style))
    elements.append(Paragraph("<b>Module:</b> Order Management Module — End User & Technical Documentation", subtitle_style))
    elements.append(HRFlowable(width="100%", thickness=2, color=primary_color, spaceAfter=10))
    
    # Metadata Box Table
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
        "The <b>Order Management Module</b> facilitates the complete processing and tracking of customer orders "
        "within the E-Commerce platform. It enables customers to place orders by selecting products and adding them "
        "to a shopping cart with automated calculations for subtotal, taxes, and shipping fees. It provides administrators "
        "with an interactive Order Dashboard to view, filter, update statuses (Pending, Shipped, Delivered), and execute "
        "order cancellations through a non-destructive soft-delete mechanism that automatically restocks product inventory."
    )
    elements.append(Paragraph(intro_p, body_style))
    
    # 2. Database Design
    elements.append(Paragraph("2. Database Design & Specification", h1_style))
    elements.append(Paragraph("The database architecture strictly adheres to the Itvedant requirements with the <code>orders</code> table and associated entities:", body_style))
    
    # Table orders
    elements.append(Paragraph("<b>Table 1: orders</b>", h2_style))
    orders_table_data = [
        [Paragraph("Column", table_header), Paragraph("Data Type", table_header), Paragraph("Key / Constraint", table_header), Paragraph("Description", table_header)],
        [Paragraph("id", table_cell), Paragraph("BIGINT", table_cell), Paragraph("PK, Auto-Increment", table_cell), Paragraph("Unique identifier for each order", table_cell)],
        [Paragraph("user_id", table_cell), Paragraph("BIGINT", table_cell), Paragraph("FK (users.id)", table_cell), Paragraph("Foreign key referencing users table (customer)", table_cell)],
        [Paragraph("customer_name", table_cell), Paragraph("VARCHAR(100)", table_cell), Paragraph("NOT NULL", table_cell), Paragraph("Customer name for direct dashboard display", table_cell)],
        [Paragraph("total_amount", table_cell), Paragraph("DECIMAL(10,2)", table_cell), Paragraph("NOT NULL", table_cell), Paragraph("Total cost of order (Subtotal + Tax + Shipping)", table_cell)],
        [Paragraph("subtotal", table_cell), Paragraph("DECIMAL(10,2)", table_cell), Paragraph("NOT NULL", table_cell), Paragraph("Subtotal of ordered products", table_cell)],
        [Paragraph("tax_amount", table_cell), Paragraph("DECIMAL(10,2)", table_cell), Paragraph("NOT NULL", table_cell), Paragraph("Calculated tax (5% GST/VAT)", table_cell)],
        [Paragraph("shipping_cost", table_cell), Paragraph("DECIMAL(10,2)", table_cell), Paragraph("NOT NULL", table_cell), Paragraph("Calculated shipping fee ($0 if >= $100, else $10)", table_cell)],
        [Paragraph("order_status", table_cell), Paragraph("VARCHAR(50)", table_cell), Paragraph("NOT NULL", table_cell), Paragraph("Status: Pending, Shipped, Delivered, Cancelled", table_cell)],
        [Paragraph("shipping_address", table_cell), Paragraph("VARCHAR(300)", table_cell), Paragraph("NOT NULL", table_cell), Paragraph("Shipping destination address", table_cell)],
        [Paragraph("created_at", table_cell), Paragraph("DATETIME", table_cell), Paragraph("AUTO-POPULATED", table_cell), Paragraph("Timestamp when order was placed", table_cell)],
        [Paragraph("updated_at", table_cell), Paragraph("DATETIME", table_cell), Paragraph("AUTO-UPDATED", table_cell), Paragraph("Timestamp when status was last modified", table_cell)],
        [Paragraph("status", table_cell), Paragraph("BOOLEAN", table_cell), Paragraph("NOT NULL, DEFAULT TRUE", table_cell), Paragraph("True for active orders, False for cancelled (soft delete)", table_cell)]
    ]
    t_orders = Table(orders_table_data, colWidths=[80, 80, 110, 234])
    t_orders.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,0), primary_color),
        ('ALIGN', (0,0), (-1,-1), 'LEFT'),
        ('VALIGN', (0,0), (-1,-1), 'MIDDLE'),
        ('ROWBACKGROUNDS', (0,1), (-1,-1), [colors.white, colors.HexColor("#f8fafc")]),
        ('GRID', (0,0), (-1,-1), 0.5, colors.HexColor("#cbd5e1")),
        ('TOPPADDING', (0,0), (-1,-1), 3),
        ('BOTTOMPADDING', (0,0), (-1,-1), 3),
    ]))
    elements.append(t_orders)
    elements.append(Spacer(1, 8))
    
    # Table order_items
    elements.append(Paragraph("<b>Table 2: order_items (Line Items)</b>", h2_style))
    items_table_data = [
        [Paragraph("Column", table_header), Paragraph("Data Type", table_header), Paragraph("Key / Constraint", table_header), Paragraph("Description", table_header)],
        [Paragraph("id", table_cell), Paragraph("BIGINT", table_cell), Paragraph("PK, Auto-Increment", table_cell), Paragraph("Unique line item ID", table_cell)],
        [Paragraph("order_id", table_cell), Paragraph("BIGINT", table_cell), Paragraph("FK (orders.id)", table_cell), Paragraph("Foreign key referencing parent order", table_cell)],
        [Paragraph("product_id", table_cell), Paragraph("BIGINT", table_cell), Paragraph("FK (products.id)", table_cell), Paragraph("Foreign key referencing catalog product", table_cell)],
        [Paragraph("product_name", table_cell), Paragraph("VARCHAR(255)", table_cell), Paragraph("NOT NULL", table_cell), Paragraph("Product snapshot name at time of order", table_cell)],
        [Paragraph("quantity", table_cell), Paragraph("INT", table_cell), Paragraph("NOT NULL", table_cell), Paragraph("Quantity purchased", table_cell)],
        [Paragraph("price", table_cell), Paragraph("DECIMAL(10,2)", table_cell), Paragraph("NOT NULL", table_cell), Paragraph("Unit price at time of purchase", table_cell)],
        [Paragraph("item_total", table_cell), Paragraph("DECIMAL(10,2)", table_cell), Paragraph("NOT NULL", table_cell), Paragraph("Line total: quantity * price", table_cell)]
    ]
    t_items = Table(items_table_data, colWidths=[80, 80, 110, 234])
    t_items.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,0), primary_color),
        ('ALIGN', (0,0), (-1,-1), 'LEFT'),
        ('VALIGN', (0,0), (-1,-1), 'MIDDLE'),
        ('ROWBACKGROUNDS', (0,1), (-1,-1), [colors.white, colors.HexColor("#f8fafc")]),
        ('GRID', (0,0), (-1,-1), 0.5, colors.HexColor("#cbd5e1")),
        ('TOPPADDING', (0,0), (-1,-1), 3),
        ('BOTTOMPADDING', (0,0), (-1,-1), 3),
    ]))
    elements.append(t_items)
    elements.append(Spacer(1, 10))
    
    # 3. Core Functionalities
    elements.append(Paragraph("3. Core Functionalities & Business Logic", h1_style))
    
    func_text = (
        "<b>1. Place an Order:</b> Customers add products to the shopping cart, specify quantities, and provide shipping details. "
        "The system calculates: <code>Subtotal = Sum(Item Price * Quantity)</code>, <code>Tax = Subtotal * 5%</code>, and "
        "<code>Shipping Cost = Free ($0) if Subtotal >= $100 else $10.00</code>. On order placement, product stock is decremented.<br/><br/>"
        "<b>2. Order Dashboard:</b> Admins view all orders in reverse chronological order. Quick status filter tabs "
        "(All, Pending, Shipped, Delivered, Cancelled) provide fast triage. Each record displays Order ID, Customer Name, "
        "Shipping Address, Items Count, Total Amount, Order Status badge, Active Status, and action buttons.<br/><br/>"
        "<b>3. Update Order Status:</b> Admins can progress order lifecycle (Pending -> Shipped -> Delivered). Last updated "
        "timestamp is refreshed automatically.<br/><br/>"
        "<b>4. Order Cancellation (Soft Delete):</b> Customers and admins can cancel orders only if not yet shipped (Pending). "
        "Cancellation performs a soft delete: <code>orders.status</code> is set to <code>false</code>, <code>order_status</code> "
        "is set to <code>Cancelled</code>, and reserved inventory is automatically restored back to the catalog."
    )
    elements.append(Paragraph(func_text, body_style))
    elements.append(Spacer(1, 8))
    
    # 4. REST API Endpoints
    elements.append(Paragraph("4. REST API Endpoint Specifications", h1_style))
    api_table_data = [
        [Paragraph("Method", table_header), Paragraph("Endpoint", table_header), Paragraph("Description", table_header), Paragraph("Sample Payload / Response", table_header)],
        [
            Paragraph("POST", table_cell),
            Paragraph("<b>/api/orders</b>", table_cell),
            Paragraph("Place a new order with cart items", table_cell),
            Paragraph("<code>{\"customerName\":\"Zeenat\", \"shippingAddress\":\"Mumbai\", \"items\":[{\"productId\":1,\"quantity\":2}]}</code>", code_style)
        ],
        [
            Paragraph("GET", table_cell),
            Paragraph("<b>/api/orders</b>", table_cell),
            Paragraph("Retrieve all orders (supports <code>?status=Pending</code>)", table_cell),
            Paragraph("Returns JSON list of orders sorted newest first", table_cell)
        ],
        [
            Paragraph("GET", table_cell),
            Paragraph("<b>/api/orders/{id}</b>", table_cell),
            Paragraph("Fetch single order with full item details", table_cell),
            Paragraph("Returns order object with line items array", table_cell)
        ],
        [
            Paragraph("PUT", table_cell),
            Paragraph("<b>/api/orders/{id}/status</b>", table_cell),
            Paragraph("Update order status", table_cell),
            Paragraph("<code>{\"orderStatus\":\"Shipped\"}</code>", code_style)
        ],
        [
            Paragraph("DELETE / PUT", table_cell),
            Paragraph("<b>/api/orders/{id}</b><br/><b>/api/orders/{id}/cancel</b>", table_cell),
            Paragraph("Soft-delete cancel order (Pending only) & restock stock", table_cell),
            Paragraph("Sets status=false, orderStatus='Cancelled'", table_cell)
        ]
    ]
    t_api = Table(api_table_data, colWidths=[65, 125, 150, 164])
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
    
    # 5. Verification & Testing Results
    elements.append(Paragraph("5. Automated Test Suite & Verification", h1_style))
    test_p = (
        "Automated unit and integration tests were implemented in <code>src/test/java/com/ecommerce/ecommerce/OrderServiceTest.java</code>:<br/>"
        "• <b>testPlaceOrder_Success_AndCalculation:</b> Verified Subtotal, 5% Tax, and Free Shipping calculations.<br/>"
        "• <b>testPlaceOrder_WithShippingCost:</b> Verified $10 shipping fee applied for orders below $100.<br/>"
        "• <b>testPlaceOrder_InsufficientInventoryThrowsException:</b> Verified inventory boundary validation.<br/>"
        "• <b>testUpdateOrderStatus:</b> Verified admin status transition.<br/>"
        "• <b>testCancelOrder_SoftDeleteAndInventoryRestock:</b> Verified soft delete (status=false) and restock.<br/>"
        "• <b>testCancelOrder_AlreadyShippedFails:</b> Verified cancellation is rejected for shipped orders.<br/>"
        "<b>Result:</b> <code>[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0 — BUILD SUCCESS</code>"
    )
    elements.append(Paragraph(test_p, body_style))
    elements.append(Spacer(1, 10))
    
    # 6. Assignment Deliverables & Submission
    elements.append(Paragraph("6. Assignment Submission Summary", h1_style))
    summary_data = [
        [Paragraph("<b>Item</b>", table_header), Paragraph("<b>Details & Links</b>", table_header)],
        [Paragraph("1. GitHub Repository", table_cell), Paragraph("<b>https://github.com/ansarizeenat/ecommerce-category-management</b>", table_cell)],
        [Paragraph("2. Free Cloud Hosted URL", table_cell), Paragraph("<b>https://ecommerce-category-management.onrender.com</b>", table_cell)],
        [Paragraph("3. End User Documentation", table_cell), Paragraph("Comprehensive guide generated in PDF format (attached document).", table_cell)],
        [Paragraph("4. Modules Completed", table_cell), Paragraph("1. Category Management (Complete)<br/>2. Product Management (Complete)<br/>3. Order Management (Complete)", table_cell)]
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
    
    # Build Document
    doc.build(elements, canvasmaker=NumberedCanvas)
    print(f"Documentation PDF generated successfully at: {output_filename}")

if __name__ == "__main__":
    out_file = sys.argv[1] if len(sys.argv) > 1 else "Order_Management_Module_End_User_Documentation.pdf"
    generate_pdf(out_file)
