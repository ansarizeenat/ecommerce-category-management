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
        if self._pageNumber > 1:
            self.drawString(54, 750, "Itvedant Internship — Cart Management Module Documentation")
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

def generate_pdf(output_filename="Cart_Management_Module_End_User_Documentation.pdf"):
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
    code_style = ParagraphStyle(
        'CodeSnippet', parent=styles['Normal'], fontName='Courier',
        fontSize=8, leading=10.5, textColor=colors.HexColor("#1e293b")
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
    elements.append(Paragraph("<b>Module:</b> Cart Management Module — End User &amp; Technical Documentation", subtitle_style))
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
        "The <b>Cart Management Module</b> allows customers to add, update, and remove products from the shopping cart. "
        "It ensures that customers can review their selected items before proceeding to checkout, updating prices and quantities "
        "in real time. Inventory is validated for every change so the shop never oversells. Admins have a dashboard to view any "
        "customer's cart and to analyse cart-abandonment patterns.",
        body_style
    ))

    elements.append(Paragraph("2. Database Design &amp; Specification", h1_style))
    elements.append(Paragraph("Table Name: <b>carts</b> — schema matches the Itvedant assignment specification.", body_style))
    elements.append(Paragraph("<b>Table 1: carts</b>", h2_style))

    carts_table_data = [
        [Paragraph("Column", table_header), Paragraph("Data Type", table_header), Paragraph("Key / Constraint", table_header), Paragraph("Description", table_header)],
        [Paragraph("cart_id", table_cell), Paragraph("INT", table_cell), Paragraph("PK, Auto-Increment", table_cell), Paragraph("Unique identifier for each cart line item", table_cell)],
        [Paragraph("customer_id", table_cell), Paragraph("INT", table_cell), Paragraph("FK (users.user_id)", table_cell), Paragraph("Customer who owns this cart row (shopper)", table_cell)],
        [Paragraph("product_id", table_cell), Paragraph("INT", table_cell), Paragraph("FK (products.id)", table_cell), Paragraph("Product added to the cart", table_cell)],
        [Paragraph("quantity", table_cell), Paragraph("INT", table_cell), Paragraph("NOT NULL", table_cell), Paragraph("Number of units of the product in this row", table_cell)],
        [Paragraph("total_price", table_cell), Paragraph("DECIMAL(10,2)", table_cell), Paragraph("NOT NULL", table_cell), Paragraph("Line total (unit price × quantity)", table_cell)],
        [Paragraph("created_at", table_cell), Paragraph("DATETIME", table_cell), Paragraph("AUTO-POPULATED", table_cell), Paragraph("Timestamp when the item was added to cart", table_cell)],
        [Paragraph("updated_at", table_cell), Paragraph("DATETIME", table_cell), Paragraph("AUTO-UPDATED", table_cell), Paragraph("Timestamp when the cart row was last modified", table_cell)],
    ]
    t_cart = Table(carts_table_data, colWidths=[90, 90, 110, 214])
    t_cart.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, 0), primary_color),
        ('ALIGN', (0, 0), (-1, -1), 'LEFT'),
        ('VALIGN', (0, 0), (-1, -1), 'MIDDLE'),
        ('ROWBACKGROUNDS', (0, 1), (-1, -1), [colors.white, colors.HexColor("#f8fafc")]),
        ('GRID', (0, 0), (-1, -1), 0.5, colors.HexColor("#cbd5e1")),
        ('TOPPADDING', (0, 0), (-1, -1), 3),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 3),
    ]))
    elements.append(t_cart)
    elements.append(Spacer(1, 10))

    elements.append(Paragraph("3. Core Functionalities", h1_style))
    func_text = (
        "<b>1. Add to Cart:</b> Customers can add products to the shopping cart directly from the product page. "
        "The system validates that the product is active and in stock before saving. If the same product is already in the "
        "customer's cart, the quantities are <b>merged</b> instead of creating a duplicate row.<br/><br/>"
        "<b>2. Update Cart:</b> Customers can modify the quantity of any line item in the cart and the total price is updated "
        "accordingly in real time. The system re-checks inventory for the new quantity to prevent oversell.<br/><br/>"
        "<b>3. Remove from Cart:</b> Customers remove individual items; the total is recalculated. Admins can also clear an "
        "entire customer's cart in a single click (useful for abandoned cart or support).<br/><br/>"
        "<b>4. Cart Dashboard:</b> Customers see their cart contents (product, SKU, unit price, qty, line total, stock). "
        "Admins view all customers' carts and use summary stats for cart-abandonment analysis: Total Cart Value, Customers "
        "With Carts, Total Items Quantity, and Avg Items / Customer."
    )
    elements.append(Paragraph(func_text, body_style))

    elements.append(Paragraph("4. End User Steps", h1_style))
    elements.append(Paragraph(
        "1. Open <b>https://ecommerce-category-management.onrender.com</b> and select the <b>Cart Management</b> tab.<br/>"
        "2. Ensure a customer and an in-stock product exist (create them via the Customer / Product tabs if needed).<br/>"
        "3. Choose a customer, pick a product, enter a quantity and click <b>Add to Cart</b>.<br/>"
        "4. Use the <b>Qty</b> pencil button to change quantities, or the trash icon to remove a line.<br/>"
        "5. Use the customer filter to select a specific customer and then click <b>Clear Cart</b> to wipe their entire cart.<br/>"
        "6. Read the four dashboard summary cards to analyse total cart value and cart-abandonment patterns.",
        body_style
    ))

    elements.append(Paragraph("5. REST API Endpoint Specifications", h1_style))
    api_table_data = [
        [Paragraph("Method", table_header), Paragraph("Endpoint", table_header), Paragraph("Description", table_header), Paragraph("Sample Payload / Response", table_header)],
        [
            Paragraph("POST", table_cell),
            Paragraph("<b>/api/carts</b>", table_cell),
            Paragraph("Add product to cart (merges with existing)", table_cell),
            Paragraph("<code>{\"customerId\":1,\"productId\":5,\"quantity\":2}</code>", code_style)
        ],
        [
            Paragraph("GET", table_cell),
            Paragraph("<b>/api/carts</b>", table_cell),
            Paragraph("All carts (use <code>?customerId=X</code> to filter)", table_cell),
            Paragraph("JSON list of cart rows", table_cell)
        ],
        [
            Paragraph("GET", table_cell),
            Paragraph("<b>/api/carts/stats</b>", table_cell),
            Paragraph("Cart-abandonment summary stats", table_cell),
            Paragraph("totalCartValue, uniqueCustomers, totalQty, avgItems", table_cell)
        ],
        [
            Paragraph("GET", table_cell),
            Paragraph("<b>/api/carts/customer/{id}</b>", table_cell),
            Paragraph("Get one customer's cart", table_cell),
            Paragraph("Filtered cart list", table_cell)
        ],
        [
            Paragraph("GET", table_cell),
            Paragraph("<b>/api/carts/customer/{id}/summary</b>", table_cell),
            Paragraph("Customer cart grand total + counts", table_cell),
            Paragraph("grandTotal, uniqueItems, totalQuantity", table_cell)
        ],
        [
            Paragraph("PUT", table_cell),
            Paragraph("<b>/api/carts/{id}/quantity</b>", table_cell),
            Paragraph("Update qty (re-checks stock)", table_cell),
            Paragraph("<code>{\"quantity\":3}</code>", code_style)
        ],
        [
            Paragraph("DELETE", table_cell),
            Paragraph("<b>/api/carts/{id}</b>", table_cell),
            Paragraph("Remove one cart line item", table_cell),
            Paragraph("Success message", table_cell)
        ],
        [
            Paragraph("DELETE", table_cell),
            Paragraph("<b>/api/carts/customer/{id}</b>", table_cell),
            Paragraph("Clear ENTIRE cart for a customer", table_cell),
            Paragraph("Success message", table_cell)
        ]
    ]
    t_api = Table(api_table_data, colWidths=[55, 145, 145, 159])
    t_api.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, 0), primary_color),
        ('ALIGN', (0, 0), (-1, -1), 'LEFT'),
        ('VALIGN', (0, 0), (-1, -1), 'TOP'),
        ('ROWBACKGROUNDS', (0, 1), (-1, -1), [colors.white, colors.HexColor("#f8fafc")]),
        ('GRID', (0, 0), (-1, -1), 0.5, colors.HexColor("#cbd5e1")),
        ('TOPPADDING', (0, 0), (-1, -1), 4),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 4),
    ]))
    elements.append(t_api)
    elements.append(Spacer(1, 10))

    elements.append(Paragraph("6. Automated Test Suite", h1_style))
    elements.append(Paragraph(
        "Tests in <code>CartServiceTest.java</code> cover: add success, qty merge for duplicate product + customer, "
        "insufficient stock rejection, merge-exceeds-stock rejection, quantity update + price recalc, update-exceeds-stock, "
        "remove item, admin stats + customer summary, clear-customer-cart, and inactive-product blocking.",
        body_style
    ))

    elements.append(Paragraph("7. Assignment Submission Summary", h1_style))
    summary_data = [
        [Paragraph("<b>Item</b>", table_header), Paragraph("<b>Details &amp; Links</b>", table_header)],
        [Paragraph("1. GitHub Repository", table_cell), Paragraph("<b>https://github.com/ansarizeenat/ecommerce-category-management</b>", table_cell)],
        [Paragraph("2. Free Cloud Hosted URL", table_cell), Paragraph("<b>https://ecommerce-category-management.onrender.com</b>", table_cell)],
        [Paragraph("3. End User Documentation", table_cell), Paragraph("This PDF — Cart Management Module End User Documentation.", table_cell)],
        [Paragraph("4. Modules Completed", table_cell), Paragraph("Category, Product, Order, Customer, Payment, and Cart Management.", table_cell)]
    ]
    t_summary = Table(summary_data, colWidths=[160, 344])
    t_summary.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, 0), primary_color),
        ('ALIGN', (0, 0), (-1, -1), 'LEFT'),
        ('VALIGN', (0, 0), (-1, -1), 'MIDDLE'),
        ('ROWBACKGROUNDS', (0, 1), (-1, -1), [colors.white, colors.HexColor("#f8fafc")]),
        ('GRID', (0, 0), (-1, -1), 0.5, colors.HexColor("#cbd5e1")),
        ('TOPPADDING', (0, 0), (-1, -1), 5),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 5),
    ]))
    elements.append(t_summary)

    doc.build(elements, canvasmaker=NumberedCanvas)
    print(f"Documentation PDF generated successfully at: {output_filename}")

if __name__ == "__main__":
    out_file = sys.argv[1] if len(sys.argv) > 1 else "Cart_Management_Module_End_User_Documentation.pdf"
    generate_pdf(out_file)
