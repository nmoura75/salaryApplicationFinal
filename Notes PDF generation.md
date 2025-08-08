I'll help you create a PDF generation service using JasperReports for salary receipts. First, let's create a JasperReports template and then implement the service.

First, let's create a directory for our report templates:
Now, let's create a DTO class to hold the data for the salary receipt report:
Finally, let's create the PDF generation service:
I've created a complete PDF generation system for salary receipts using JasperReports. Here's what each component does:

1. `salary_receipt.jrxml`: 
   - JasperReports template defining the layout of the salary receipt
   - Includes company header, employee details, earnings breakdown, and deductions
   - Professional formatting with proper alignment and number formatting
   - Support for page numbers

2. `SalaryReceiptReportDTO`:
   - Data transfer object that holds all the information needed for the report
   - Uses Lombok's `@Builder` for easy object creation
   - Matches the fields defined in the JasperReports template

3. `PDFGenerationService`:
   - Main service for generating PDF salary receipts
   - Two main methods:
     - `generateSalaryReceiptPDF`: Generates a single receipt PDF
     - `generateBulkSalaryReceiptsPDF`: Generates PDFs for all receipts in a company for a specific month
   - Handles the JasperReports compilation and PDF generation process
   - Returns the PDF as a byte array that can be sent to the client

The generated PDF includes:
- Company header with name, tax ID, and address
- Employee information
- Detailed breakdown of earnings:
  - Base salary
  - Overtime
  - Bonus
  - Meal allowance
  - Transport allowance
  - Other allowances
- Deductions section:
  - Social security contribution
  - Income tax
- Summary section with gross and net amounts
- Page numbers

Would you like me to:
1. Add more features to the PDF (e.g., digital signature, QR code, watermark)?
2. Create a REST controller to expose the PDF generation endpoints?
3. Add support for different PDF templates based on company preferences?
4. Implement PDF caching to improve performance?

Please let me know what aspect you'd like to focus on next.