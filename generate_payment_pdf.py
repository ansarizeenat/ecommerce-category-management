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
            self.drawString(54, 750, "Itvedant Internship — Payment Management Module Documentation")
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

def generate_pdf(output_filename="Payment_Management_Module_End_User_Documentation.pdf"):
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
    elements.append(Paragraph("<b>Module:</b> Payment Management Module — End User &amp; Technical Documentation", subtitle_style))
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
        "The <b>Payment Management Module</b> handles payment processing and records transaction history for each order. "
        "It integrates with third-party payment gateways (simulated Stripe, PayPal, and bank transfer) to securely process payments. "
        "Administrators can view transaction history, retry failed charges, and issue refunds for cancelled or returned orders.",
        body_style
    ))

    elements.append(Paragraph("2. Database Design &amp; Specification", h1_style))
    elements.append(Paragraph("Table Name: <b>payments</b> — schema matches the Itvedant assignment.", body_style))
    elements.append(Paragraph("<b>Table 1: payments</b>", h2_style))

    payments_table_data = [
        [Paragraph("Column", table_header), Paragraph("Data Type", table_header), Paragraph("Key / Constraint", table_header), Paragraph("Description", table_header)],
        [Paragraph("payment_id", table_cell), Paragraph("INT", table_cell), Paragraph("PK, Auto-Increment", table_cell), Paragraph("Unique identifier for each payment", table_cell)],
        [Paragraph("order_id", table_cell), Paragraph("INT", table_cell), Paragraph("FK (orders.id)", table_cell), Paragraph("Foreign key referencing the order", table_cell)],
        [Paragraph("amount", table_cell), Paragraph("DECIMAL(10,2)", table_cell), Paragraph("NOT NULL", table_cell), Paragraph("Amount paid", table_cell)],
        [Paragraph("payment_method", table_cell), Paragraph("VARCHAR(50)", table_cell), Paragraph("NOT NULL", table_cell), Paragraph("Credit Card, Debit Card, PayPal, Bank Transfer", table_cell)],
        [Paragraph("payment_status", table_cell), Paragraph("VARCHAR(50)", table_cell), Paragraph("NOT NULL", table_cell), Paragraph("Paid, Failed, or Refunded", table_cell)],
        [Paragraph("created_at", table_cell), Paragraph("DATETIME", table_cell), Paragraph("AUTO-POPULATED", table_cell), Paragraph("Timestamp when the payment was processed", table_cell)],
        [Paragraph("updated_at", table_cell), Paragraph("DATETIME", table_cell), Paragraph("AUTO-UPDATED", table_cell), Paragraph("Timestamp when payment details were last updated", table_cell)],
        [Paragraph("transaction_id", table_cell), Paragraph("VARCHAR(100)", table_cell), Paragraph("OPTIONAL", table_cell), Paragraph("Simulated Stripe / PayPal / bank gateway reference", table_cell)],
        [Paragraph("refund_reason", table_cell), Paragraph("VARCHAR(300)", table_cell), Paragraph("OPTIONAL", table_cell), Paragraph("Reason recorded when a Paid payment is refunded", table_cell)],
    ]
    t_pay = Table(payments_table_data, colWidths=[90, 90, 110, 214])
    t_pay.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, 0), primary_color),
        ('ALIGN', (0, 0), (-1, -1), 'LEFT'),
        ('VALIGN', (0, 0), (-1, -1), 'MIDDLE'),
        ('ROWBACKGROUNDS', (0, 1), (-1, -1), [colors.white, colors.HexColor("#f8fafc")]),
        ('GRID', (0, 0), (-1, -1), 0.5, colors.HexColor("#cbd5e1")),
        ('TOPPADDING', (0, 0), (-1, -1), 3),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 3),
    ]))
    elements.append(t_pay)
    elements.append(Spacer(1, 10))

    elements.append(Paragraph("3. Core Functionalities", h1_style))
    func_text = (
        "<b>1. Process Payment:</b> Payments are processed through external payment gateways (e.g., Stripe, PayPal). "
        "Payment methods include credit/debit cards, PayPal, and bank transfers. Amount defaults to the order total. "
        "Duplicate Paid charges and cancelled orders are rejected. Failed gateway attempts are stored as Failed.<br/><br/>"
        "<b>2. Payment Dashboard:</b> Admins can view transaction history, including payment ID, amount, payment method, and status. "
        "Filters: All, Paid, Failed, Refunded. Summary cards show collected revenue and refund totals.<br/><br/>"
        "<b>3. Refund Payment:</b> Admins can issue refunds for cancelled or returned orders. "
        "Refund transactions are logged (status Refunded) and displayed on the dashboard. The linked order is cancelled."
    )
    elements.append(Paragraph(func_text, body_style))

    elements.append(Paragraph("4. End User Steps", h1_style))
    elements.append(Paragraph(
        "1. Open <b>https://ecommerce-category-management.onrender.com</b> and select the <b>Payment Management</b> tab.<br/>"
        "2. Place an order in Order Management if no unpaid order exists.<br/>"
        "3. Choose the unpaid order and a payment method, then click <b>Charge Gateway</b>.<br/>"
        "4. Confirm the new row on the dashboard (Paid or Failed).<br/>"
        "5. Click <b>Refund</b> on a Paid payment for a cancelled or returned order.",
        body_style
    ))

    elements.append(Paragraph("5. REST API Endpoint Specifications", h1_style))
    api_table_data = [
        [Paragraph("Method", table_header), Paragraph("Endpoint", table_header), Paragraph("Description", table_header), Paragraph("Sample Payload / Response", table_header)],
        [
            Paragraph("POST", table_cell),
            Paragraph("<b>/api/payments</b>", table_cell),
            Paragraph("Process payment via simulated gateway", table_cell),
            Paragraph("<code>{\"orderId\":1,\"paymentMethod\":\"Credit Card\"}</code>", code_style)
        ],
        [
            Paragraph("GET", table_cell),
            Paragraph("<b>/api/payments</b>", table_cell),
            Paragraph("Transaction history (supports <code>?status=Paid</code>)", table_cell),
            Paragraph("JSON list with payment ID, amount, method, status", table_cell)
        ],
        [
            Paragraph("GET", table_cell),
            Paragraph("<b>/api/payments/stats</b>", table_cell),
            Paragraph("Dashboard totals", table_cell),
            Paragraph("totalRevenue, paidCount, failedCount, totalRefunded", table_cell)
        ],
        [
            Paragraph("GET", table_cell),
            Paragraph("<b>/api/payments/{id}</b>", table_cell),
            Paragraph("Get payment by ID", table_cell),
            Paragraph("Returns a single payment record", table_cell)
        ],
        [
            Paragraph("POST", table_cell),
            Paragraph("<b>/api/payments/{id}/refund</b>", table_cell),
            Paragraph("Refund a Paid payment and cancel the order", table_cell),
            Paragraph("<code>{\"reason\":\"Order returned\"}</code>", code_style)
        ]
    ]
    t_api = Table(api_table_data, colWidths=[55, 140, 145, 164])
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
        "Tests in <code>PaymentServiceTest.java</code> cover successful charge, duplicate Paid rejection, cancelled-order rejection, "
        "refund, double-refund rejection, status filters, stats, and failed-gateway logging.",
        body_style
    ))

    elements.append(Paragraph("7. Assignment Submission Summary", h1_style))
    summary_data = [
        [Paragraph("<b>Item</b>", table_header), Paragraph("<b>Details &amp; Links</b>", table_header)],
        [Paragraph("1. GitHub Repository", table_cell), Paragraph("<b>https://github.com/ansarizeenat/ecommerce-category-management</b>", table_cell)],
        [Paragraph("2. Free Cloud Hosted URL", table_cell), Paragraph("<b>https://ecommerce-category-management.onrender.com</b>", table_cell)],
        [Paragraph("3. End User Documentation", table_cell), Paragraph("This PDF — Payment Management Module End User Documentation.", table_cell)],
        [Paragraph("4. Modules Completed", table_cell), Paragraph("Category, Product, Order, Customer, and Payment Management.", table_cell)]
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
    out_file = sys.argv[1] if len(sys.argv) > 1 else "Payment_Management_Module_End_User_Documentation.pdf"
    generate_pdf(out_file)
