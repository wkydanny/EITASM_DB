# EITASM – Test Plan

## 1. Purpose

This test plan verifies that the main functional requirements of the EITASM application work correctly.

Testing focuses on authentication, navigation, data display, search functionality, form validation, and access control.

---

## 2. Test Environment

- Application: EITASM
- Environment: Local development environment
- Browser: Google Chrome
- Development Server: VS Code Live Server
- Frontend: HTML, CSS, Bootstrap, JavaScript
- Data Source: JSON files
- Version Control: Git and GitHub

---

## 3. Test Cases

### TC-01 – Login with Valid Credentials

**Objective:**  
Verify that a user can sign in using valid credentials.

**Steps:**

1. Open `login.html`.
2. Enter a valid email address.
3. Enter a valid password.
4. Click **Sign In**.

**Expected Result:**  
The user is redirected to the Dashboard.

**Status:** PASS

---

### TC-02 – Invalid Email Validation

**Objective:**  
Verify that the login form rejects an invalid email format.

**Steps:**

1. Open the Login page.
2. Enter an invalid email such as `abc`.
3. Enter a password.
4. Click **Sign In**.

**Expected Result:**  
An email validation message is displayed.

**Status:** PASS

---

### TC-03 – Password Validation

**Objective:**  
Verify that passwords shorter than the minimum required length are rejected.

**Steps:**

1. Enter a valid email address.
2. Enter a password shorter than 6 characters.
3. Click **Sign In**.

**Expected Result:**  
The system displays a password validation message.

**Status:** PASS

---

### TC-04 – User Registration

**Objective:**  
Verify that a new user can create an account.

**Steps:**

1. Open the registration page.
2. Enter the required user information.
3. Enter a valid email address.
4. Enter a valid password.
5. Submit the registration form.

**Expected Result:**  
The user account is created and can be used for authentication.

**Status:** PASS

---

### TC-05 – Authentication Protection

**Objective:**  
Verify that protected pages cannot be accessed without authentication.

**Steps:**

1. Remove the current user information from browser storage.
2. Attempt to open `dashboard.html` directly.

**Expected Result:**  
The user is redirected to `login.html`.

**Status:** PASS

---

### TC-06 – Dashboard Data

**Objective:**  
Verify that the Dashboard displays application statistics.

**Steps:**

1. Sign in.
2. Open the Dashboard.
3. Review the summary cards.

**Expected Result:**  
Employee, hardware asset, software license, and incident information is displayed correctly.

**Status:** PASS

---

### TC-07 – Employee Records

**Objective:**  
Verify that employee records are displayed correctly.

**Steps:**

1. Open the Employees page.
2. Review the employee table.

**Expected Result:**  
Employee records are displayed with the required information.

**Status:** PASS

---

### TC-08 – Department Records

**Objective:**  
Verify that department information is displayed correctly.

**Steps:**

1. Open the Departments page.
2. Review the department table.

**Expected Result:**  
Department ID, name, manager, location, employee count, and status are displayed.

**Status:** PASS

---

### TC-09 – Hardware Assets

**Objective:**  
Verify that hardware asset information is displayed.

**Steps:**

1. Open the Hardware Assets page.
2. Review the asset records.

**Expected Result:**  
Hardware asset information is displayed correctly.

**Status:** PASS

---

### TC-10 – Asset Types

**Objective:**  
Verify that hardware asset categories are displayed.

**Steps:**

1. Open the Asset Types page.
2. Review the asset type table.

**Expected Result:**  
Asset type ID, name, description, and asset count are displayed.

**Status:** PASS

---

### TC-11 – Software Licenses

**Objective:**  
Verify that software license information is displayed correctly.

**Steps:**

1. Open the Software Licenses page.
2. Review the license records.

**Expected Result:**  
Software license information is displayed correctly.

**Status:** PASS

---

### TC-12 – License Assignments

**Objective:**  
Verify that license assignments are displayed.

**Steps:**

1. Open the License Assignments page.
2. Review the assignment table.

**Expected Result:**  
License and employee assignment information is displayed correctly.

**Status:** PASS

---

### TC-13 – Security Incidents

**Objective:**  
Verify that security incident records are displayed.

**Steps:**

1. Open the Security Incidents page.
2. Review the incident table.

**Expected Result:**  
Incident information including severity, status, reported user, and date is displayed.

**Status:** PASS

---

### TC-14 – Audit Logs

**Objective:**  
Verify that audit log records are displayed.

**Steps:**

1. Open the Audit Logs page.
2. Review the log table.

**Expected Result:**  
System and user activity records are displayed correctly.

**Status:** PASS

---

### TC-15 – Search Functionality

**Objective:**  
Verify that users can search records.

**Steps:**

1. Open a data management page.
2. Enter a search term in the search field.

**Expected Result:**  
Only records matching the search term are displayed.

**Status:** PASS

---

### TC-16 – Reports

**Objective:**  
Verify that system statistics and reports are displayed.

**Steps:**

1. Open the Reports page.
2. Review the summary cards.
3. Review the charts.
4. Review the System Summary table.

**Expected Result:**  
System statistics, asset information, and security incident information are displayed correctly.

**Status:** PASS

---

### TC-17 – Settings

**Objective:**  
Verify that application preferences can be configured.

**Steps:**

1. Open the Settings page.
2. Modify an application preference.
3. Save the settings.
4. Reload the page.

**Expected Result:**  
The selected preferences are preserved.

**Status:** PASS

---

### TC-18 – Navigation

**Objective:**  
Verify that navigation between application modules works correctly.

**Steps:**

1. Sign in.
2. Use the sidebar to navigate through the application pages.

**Expected Result:**  
Each navigation link opens the correct page.

**Status:** PASS

---

## 4. Test Summary

The main EITASM application functionality was tested using manual functional testing.

The tested areas include:

- User authentication
- Registration
- Form validation
- Access control
- Dashboard
- Employee management
- Department management
- Hardware assets
- Asset types
- Software licenses
- License assignments
- Security incidents
- Audit logs
- Reports
- Settings
- Search functionality
- Application navigation

All listed test cases produced the expected results during testing.

---

## 5. Test Result

**Overall Result: PASS**

The EITASM application satisfies the tested functional requirements and is ready for final project review.