-- =========================================================
-- 1. IMPLEMENT REQUIREMENT: 1 SQL VIEW
-- Combines HR tables to output actual names instead of IDs
-- =========================================================
CREATE OR ALTER VIEW HR.vw_EmployeeDetails AS
SELECT 
    e.EmployeeID,
    e.FirstName,
    e.LastName,
    ISNULL(d.DeptName, 'Unassigned') AS Department,
    e.Email,
    CASE 
        WHEN e.ClearanceLevel = 5 THEN 'System Administrator'
        WHEN e.ClearanceLevel = 4 THEN 'Security Analyst'
        WHEN e.ClearanceLevel = 3 THEN 'IT Technician'
        ELSE 'Standard Staff'
    END AS Role
FROM HR.Employees e
LEFT JOIN HR.Departments d ON e.DepartmentID = d.DepartmentID;
GO

-- =========================================================
-- 2. IMPLEMENT REQUIREMENT: 1 CUSTOM SQL FUNCTION
-- Processes incident description fields into a clean UI format
-- =========================================================
CREATE OR ALTER FUNCTION SEC.fn_FormatIncidentDisplay (@Description VARCHAR(MAX))
RETURNS VARCHAR(MAX)
AS
BEGIN
    RETURN 'INCIDENT ALERT: ' + UPPER(ISNULL(@Description, 'No description provided.'));
END;
GO

-- =========================================================
-- 3. IMPLEMENT REQUIREMENT: STORED PROCEDURES FOR CRUD
-- =========================================================

-- Stored Procedure to READ Employees via our View
CREATE OR ALTER PROCEDURE HR.sp_GetAllEmployees
AS
BEGIN
    SET NOCOUNT ON;
    SELECT EmployeeID, FirstName, LastName, Department, Email, Role 
    FROM HR.vw_EmployeeDetails;
END;
GO

-- Stored Procedure to CREATE (Insert) an Employee
CREATE OR ALTER PROCEDURE HR.sp_AddEmployee
    @FirstName VARCHAR(50),
    @LastName VARCHAR(50),
    @Email VARCHAR(255)
AS
BEGIN
    SET NOCOUNT ON;
    INSERT INTO HR.Employees (FirstName, LastName, DepartmentID, Email, ClearanceLevel)
    VALUES (@FirstName, @LastName, 1, @Email, 1); -- Defaults to Dept 1 and Clearance 1
END;
GO

-- Stored Procedure to DELETE an Employee
CREATE OR ALTER PROCEDURE HR.sp_RemoveEmployee
    @EmployeeID INT
AS
BEGIN
    SET NOCOUNT ON;
    DELETE FROM HR.Employees WHERE EmployeeID = @EmployeeID;
END;
GO

-- Stored Procedure to READ Security Incidents using our custom Function
CREATE OR ALTER PROCEDURE SEC.sp_GetAllIncidents
AS
BEGIN
    SET NOCOUNT ON;
    SELECT 
        i.IncidentID,
        SEC.fn_FormatIncidentDisplay(i.Description) AS ProcessedDescription,
        ISNULL(s.SeverityName, 'UNKNOWN') AS SeverityLevel,
        i.ResolutionStatus,
        ISNULL(i.DateReported, CURRENT_TIMESTAMP) AS Timestamp
    FROM SEC.SecurityIncidents i
    LEFT JOIN SEC.SeverityLevels s ON i.SeverityLevelID = s.SeverityLevelID;
END;
GO

-- Stored Procedure to UPDATE an Incident's status
CREATE OR ALTER PROCEDURE SEC.sp_UpdateIncidentStatus
    @IncidentID INT,
    @Status VARCHAR(30)
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE SEC.SecurityIncidents 
    SET ResolutionStatus = @Status, DateReported = CURRENT_TIMESTAMP 
    WHERE IncidentID = @IncidentID;
END;
GO