# Product Inventory Management Backend

This project is a Spring Boot backend application developed as part of the **Full-Stack Developer Technical Test**.

The application allows users to upload **Excel (.xlsx)** and **CSV (.csv)** files containing product inventory data, performs validation and processing, and exposes APIs for listing and summarizing the data.

---

## Features

- Upload product inventory using Excel or CSV files
- Row-level validation with skipped invalid rows
- Duplicate detection using (Product SKU + Purchase Date)
- Pagination and sorting support
- Inventory summary calculation
- Swagger UI for API testing
- Centralized exception handling
- Fully Dockerized using Docker & Docker Compose
- Unit tests included

---

## Supported File Format

### Required Columns

| Column Name     | Type     |
|-----------------|----------|
| Product SKU     | Text     |
| Product Name    | Text     |
| Category        | Text     |
| Purchase Date   | Date (yyyy-MM-dd) |
| Unit Price      | Currency |
| Quantity        | Number   |

---

## Validations Implemented

- Product SKU, Name, Category → mandatory
- Purchase Date → not null, not future date
- Unit Price → must be greater than 0
- Quantity → must be greater than 0
- Excel formula cells → not allowed
- Duplicate check → SKU + Purchase Date

Invalid rows are skipped and returned in the response summary.

---

##  API Endpoints

### Upload Excel / CSV

POST /api/products/upload
Consumes: multipart/form-data


### Get Products (Pagination + Sorting)


GET /api/products?page=0&size=10&sortBy=unitPrice&direction=asc


### Get Inventory Summary


GET /api/products/summary


---

## Sample Upload Response

```json
{
  "success": true,
  "message": "Excel processed successfully",
  "data": {
    "totalRows": 20,
    "successCount": 15,
    "failedCount": 5,
    "errors": [
      "Row 6: Product SKU is mandatory",
      "Row 12: Unit Price must be greater than zero"
    ]
  }
}
