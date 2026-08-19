# EITASM – Enterprise IT Asset & Security Management

EITASM is an IT asset and security management application designed to help organizations manage employees, departments, hardware assets, software licenses, license assignments, security incidents, audit logs, and system information from a centralized interface.

The project provides a structured web-based interface for managing IT resources and monitoring security-related information.

---

## Features

The EITASM application includes the following main features:

- User registration
- User login and authentication
- Protected application pages
- Dashboard with system statistics
- Employee management
- Department management
- Hardware asset management
- Asset type management
- Software license management
- License assignment tracking
- Security incident tracking
- Audit log monitoring
- Reports and system statistics
- Application settings
- Search functionality
- Responsive navigation

---

## Main Application Modules

### Dashboard

The Dashboard provides an overview of the IT environment, including:

- Total employees
- Hardware assets
- Software licenses
- Open security incidents
- Quick statistics
- Asset distribution chart
- Monthly security incident chart
- Recent activity
- Quick actions
- Calendar
- Announcements

### Employees

Displays and manages employee records.

### Departments

Displays company departments, managers, locations, employee counts, and department status.

### Hardware Assets

Displays and manages organizational hardware assets.

### Asset Types

Organizes hardware into categories such as:

- Laptop
- Desktop
- Printer
- Network
- Server

### Software Licenses

Displays and manages software license information.

### License Assignments

Tracks software licenses assigned to employees.

### Security Incidents

Tracks reported security incidents including:

- Incident type
- Severity
- Status
- Reporter
- Date reported

### Audit Logs

Displays user and system activity records.

### Reports

Provides system statistics and visual reports for:

- Employees
- Hardware assets
- Software licenses
- Security incidents

### Settings

Allows users to configure application preferences including:

- Organization name
- Time zone
- Date format
- Notification preferences

---

## Technologies Used

### Frontend

- HTML5
- CSS3
- Bootstrap 5
- JavaScript
- JSON

### Backend / Project Environment

- Java
- Maven

### Development Tools

- Visual Studio Code
- Git
- GitHub
- Live Server
- Google Chrome

---

## Project Structure

```text
EITASM_DB/
│
├── doc/
│   ├── requirements.md
│   ├── test-plan.md
│   ├── use-case-diagram.png
│   └── project documentation
│
├── ERD/
│   └── database design files
│
├── frontend/
│   ├── css/
│   ├── data/
│   ├── js/
│   └── pages/
│
├── sql/
│   └── SQL scripts
│
├── src/
│   └── Java source code
│
├── .gitignore
├── pom.xml
└── README.md

Frontend Data

The frontend uses JSON data files to provide application data for several modules.

Examples include:

Employees
Departments
Hardware assets
Asset types
Software licenses
License assignments
Security incidents
Audit logs

JavaScript is used to load and display the data dynamically in the application interface.

Authentication

EITASM includes a frontend authentication system.

Users can:

Create an account.
Sign in using their credentials.
Access protected application pages after authentication.

Authentication information is stored in browser storage for the frontend implementation.

Protected pages verify the current user before allowing access.

Running the Frontend

The frontend can be run using the VS Code Live Server extension.

Steps
Clone or download the project.
Open the project folder in Visual Studio Code.
Open the frontend login page.
Start Live Server.
Register a user account if necessary.
Sign in.
Access the EITASM Dashboard.

Example local development address:

http://127.0.0.1:5500/frontend/pages/login.html

The exact port may vary depending on the local development environment.

Testing

Manual functional testing is documented in:

doc/test-plan.md

Testing covers major functionality including:

Login
Registration
Form validation
Authentication protection
Dashboard
Data management pages
Search functionality
Reports
Settings
Navigation
Requirements Documentation

Functional and non-functional requirements are documented in:

doc/requirements.md

The requirements documentation also includes the system Use Case Diagram.

Use Case Diagram

The EITASM Use Case Diagram is located at:

doc/use-case-diagram.png

It represents the main interactions between authenticated users and the application modules.

Version Control

The project uses Git and GitHub for version control and team collaboration.

Git is used to track source code changes throughout the development process.

Project Status

The EITASM project includes the primary user interface, application modules, frontend data handling, authentication, requirements documentation, system diagrams, and testing documentation.

The project is currently being prepared for final review and submission.

Project

EITASM
Enterprise IT Asset & Security Management