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
            self.drawString(54, 750, "Itvedant Internship — Shipping Management Module Documentation")
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

def generate_pdf(output_filename="Shipping_Management_Module_End_User_Documentation.pdf"):
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
    elements.append(Paragraph("<b>Module:</b> Shipping Management Module — End User &amp; Technical Documentation", subtitle_style))
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
        "The <b>Shipping Management Module</b> handles the end-to-end shipping and fulfillment process, including "
        "calculating dynamic shipping costs, tracking shipments with real-time milestones, and managing courier logistics providers. "
        "It guarantees that customer orders are dispatched, tracked, and delivered efficiently, accurately, and on schedule.",
        body_style
    ))

    elements.append(Paragraph("2. Database Design &amp; Specification", h1_style))
    elements.append(Paragraph("Table Name: <b>shipping</b> — schema exactly matches the Itvedant assignment specification.", body_style))
    elements.append(Paragraph("<b>Table 1: shipping</b>", h2_style))

    shipping_table_data = [
        [Paragraph("Column Name", table_header), Paragraph("Data Type", table_header), Paragraph("Key / Constraint", table_header), Paragraph("Description", table_header)],
        [Paragraph("shipping_id", table_cell), Paragraph("INT", table_cell), Paragraph("PK, Auto-Increment", table_cell), Paragraph("Unique identifier for each shipping entry", table_cell)],
        [Paragraph("order_id", table_cell), Paragraph("INT", table_cell), Paragraph("FK (orders.id)", table_cell), Paragraph("Foreign key referencing the orders table", table_cell)],
        [Paragraph("courier_service", table_cell), Paragraph("VARCHAR(100)", table_cell), Paragraph("NOT NULL", table_cell), Paragraph("Name of courier or shipping provider (e.g. Blue Dart, FedEx, DHL)", table_cell)],
        [Paragraph("tracking_number", table_cell), Paragraph("VARCHAR(100)", table_cell), Paragraph("NOT NULL, UNIQUE", table_cell), Paragraph("Unique tracking number provided by the courier", table_cell)],
        [Paragraph("shipping_status", table_cell), Paragraph("VARCHAR(50)", table_cell), Paragraph("NOT NULL", table_cell), Paragraph("Status of the shipment (Shipped, In Transit, Delivered)", table_cell)],
        [Paragraph("shipping_cost", table_cell), Paragraph("DECIMAL(10,2)", table_cell), Paragraph("NOT NULL", table_cell), Paragraph("Shipping cost calculated for the order", table_cell)],
        [Paragraph("created_at", table_cell), Paragraph("DATETIME", table_cell), Paragraph("AUTO-GENERATED", table_cell), Paragraph("Timestamp when shipping was initiated", table_cell)],
        [Paragraph("updated_at", table_cell), Paragraph("DATETIME", table_cell), Paragraph("AUTO-UPDATED", table_cell), Paragraph("Timestamp when shipping details were last updated", table_cell)],
    ]
    t_ship = Table(shipping_table_data, colWidths=[85, 80, 105, 234])
    t_ship.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, 0), primary_color),
        ('BOX', (0, 0), (-1, -1), 1, colors.HexColor("#cbd5e1")),
        ('INNERGRID', (0, 0), (-1, -1), 0.5, colors.HexColor("#e2e8f0")),
        ('ROWBACKGROUNDS', (0, 1), (-1, -1), [colors.white, colors.HexColor("#f8fafc")]),
        ('TOPPADDING', (0, 0), (-1, -1), 4),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 4),
        ('LEFTPADDING', (0, 0), (-1, -1), 6),
        ('RIGHTPADDING', (0, 0), (-1, -1), 6),
    ]))
    elements.append(t_ship)
    elements.append(Spacer(1, 10))

    elements.append(Paragraph("3. Key Functionalities", h1_style))
    elements.append(Paragraph("<b>3.1 Shipping Cost Calculation</b>", h2_style))
    elements.append(Paragraph(
        "Shipping costs are dynamically calculated based on weight, delivery location, and shipping method:<br/>"
        "&bull; <b>Weight Fee:</b> Base fee of $5.00 plus $2.00 per kg.<br/>"
        "&bull; <b>Delivery Location:</b> Local (1.0x, 1-2 days), Domestic (1.4x, 3-5 days), International (2.5x, 7-10 days).<br/>"
        "&bull; <b>Shipping Method:</b> Standard (1.0x), Express (1.5x), Overnight (2.2x).<br/>"
        "&bull; <b>Formula:</b> Total Cost = (Base Cost + Weight x $2.00) x Location Multiplier x Method Multiplier.<br/>"
        "An interactive cost calculator is provided in the admin UI and exposed via <code>POST /api/shipping/calculate-cost</code>.",
        body_style
    ))

    elements.append(Paragraph("<b>3.2 Shipping Dashboard</b>", h2_style))
    elements.append(Paragraph(
        "Admins can monitor all orders with corresponding shipping details: Shipping ID, Order ID, Customer Name, Address, "
        "Courier Service, Tracking Number, Status Badge, Cost, and Dispatch Date. "
        "Includes real-time metric counters for Total Shipments, Shipped, In Transit, and Delivered, along with interactive status filter buttons.",
        body_style
    ))

    elements.append(Paragraph("<b>3.3 Track Shipment (Customer Self-Service)</b>", h2_style))
    elements.append(Paragraph(
        "Customers and admins can look up any shipment status by entering its tracking number into the tracking search box or clicking "
        "the tracking number link on any table. The system renders an interactive milestone progress stepper showing: "
        "1. Order Confirmed &bull; 2. Shipped &bull; 3. In Transit &bull; 4. Delivered, with timestamps and destination address.",
        body_style
    ))

    elements.append(Paragraph("<b>3.4 Update Shipping Information</b>", h2_style))
    elements.append(Paragraph(
        "Admins can update shipping details once an order has been shipped, including re-assigning courier service (e.g. to Blue Dart or FedEx), "
        "updating tracking numbers, and changing shipment status (Shipped &rarr; In Transit &rarr; Delivered). "
        "Updating status to 'Delivered' automatically synchronizes the parent order's status to Delivered.",
        body_style
    ))
    elements.append(Spacer(1, 10))

    elements.append(Paragraph("4. How to Use the Screen", h1_style))
    elements.append(Paragraph(
        "<b>Admin Flow:</b><br/>"
        "1. Open the application and click the <b>Shipping Management</b> tab.<br/>"
        "2. In the <i>Calculate Cost &amp; Dispatch Order</i> form, select an active order.<br/>"
        "3. Choose courier service (Blue Dart, FedEx, DHL, Delhivery, Speed Post) and adjust weight, location, and method.<br/>"
        "4. Click <b>Create Shipment &amp; Dispatch</b>. The order transitions to 'Shipped' and appears in the dashboard.<br/>"
        "5. Click the <b>Pencil</b> icon on any row to edit courier details, tracking number, or advance the status to Delivered.<br/><br/>"
        "<b>Customer Tracking Flow:</b><br/>"
        "1. Enter the tracking number (e.g. <code>BD-83920145</code>) into the <i>Track Shipment</i> search box.<br/>"
        "2. Click <b>Track</b> to view live shipment progress, destination address, and milestone timeline.",
        body_style
    ))
    elements.append(Spacer(1, 10))

    elements.append(Paragraph("5. REST API Specifications", h1_style))
    api_data = [
        [Paragraph("HTTP Method", table_header), Paragraph("Endpoint", table_header), Paragraph("Description", table_header)],
        [Paragraph("GET", table_cell), Paragraph("/api/shipping", table_cell), Paragraph("List all shipments (supports ?status=Shipped/In Transit/Delivered)", table_cell)],
        [Paragraph("GET", table_cell), Paragraph("/api/shipping/stats", table_cell), Paragraph("Summary counters (Total, Shipped, In Transit, Delivered, Total Cost)", table_cell)],
        [Paragraph("GET", table_cell), Paragraph("/api/shipping/{id}", table_cell), Paragraph("Retrieve single shipping record by ID", table_cell)],
        [Paragraph("GET", table_cell), Paragraph("/api/shipping/order/{orderId}", table_cell), Paragraph("Retrieve shipping details for an order", table_cell)],
        [Paragraph("GET", table_cell), Paragraph("/api/shipping/track/{trackingNumber}", table_cell), Paragraph("Public shipment tracking endpoint with milestone timeline", table_cell)],
        [Paragraph("POST", table_cell), Paragraph("/api/shipping/calculate-cost", table_cell), Paragraph("Calculate shipping cost based on weight, location, and method", table_cell)],
        [Paragraph("POST", table_cell), Paragraph("/api/shipping", table_cell), Paragraph("Create shipment and update linked order status to Shipped", table_cell)],
        [Paragraph("PUT", table_cell), Paragraph("/api/shipping/{id}", table_cell), Paragraph("Update courier service, tracking number, or shipping status", table_cell)],
        [Paragraph("PUT", table_cell), Paragraph("/api/shipping/{id}/status", table_cell), Paragraph("Quick status update (Shipped, In Transit, Delivered)", table_cell)],
    ]
    t_api = Table(api_data, colWidths=[80, 190, 234])
    t_api.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, 0), primary_color),
        ('BOX', (0, 0), (-1, -1), 1, colors.HexColor("#cbd5e1")),
        ('INNERGRID', (0, 0), (-1, -1), 0.5, colors.HexColor("#e2e8f0")),
        ('ROWBACKGROUNDS', (0, 1), (-1, -1), [colors.white, colors.HexColor("#f8fafc")]),
        ('TOPPADDING', (0, 0), (-1, -1), 4),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 4),
        ('LEFTPADDING', (0, 0), (-1, -1), 6),
        ('RIGHTPADDING', (0, 0), (-1, -1), 6),
    ]))
    elements.append(t_api)
    elements.append(Spacer(1, 10))

    elements.append(Paragraph("6. Assignment Submission Deliverables", h1_style))
    sub_data = [
        [Paragraph("Assignment Requirement", table_header), Paragraph("Candidate Submission Details", table_header)],
        [Paragraph("<b>1. GitHub Repository Link</b>", table_cell), Paragraph("https://github.com/ansarizeenat/ecommerce-category-management", table_cell)],
        [Paragraph("<b>2. URL Hosted on Free Server</b>", table_cell), Paragraph("https://ecommerce-category-management.onrender.com", table_cell)],
        [Paragraph("<b>3. End User Documentation</b>", table_cell), Paragraph("Compiled in PDF: <code>Shipping_Management_Module_End_User_Documentation.pdf</code>", table_cell)],
    ]
    t_sub = Table(sub_data, colWidths=[180, 324])
    t_sub.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, 0), primary_color),
        ('BOX', (0, 0), (-1, -1), 1, colors.HexColor("#cbd5e1")),
        ('INNERGRID', (0, 0), (-1, -1), 0.5, colors.HexColor("#e2e8f0")),
        ('ROWBACKGROUNDS', (0, 1), (-1, -1), [colors.white, colors.HexColor("#f8fafc")]),
        ('TOPPADDING', (0, 0), (-1, -1), 5),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 5),
        ('LEFTPADDING', (0, 0), (-1, -1), 8),
        ('RIGHTPADDING', (0, 0), (-1, -1), 8),
    ]))
    elements.append(t_sub)

    doc.build(elements, canvasmaker=NumberedCanvas)
    print(f"Successfully generated PDF: {output_filename}")

if __name__ == "__main__":
    out_file = sys.argv[1] if len(sys.argv) > 1 else "Shipping_Management_Module_End_User_Documentation.pdf"
    generate_pdf(out_file)
