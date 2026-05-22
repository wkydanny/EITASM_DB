# Deploy_EITASM_in_Azure.ps1
# Automated Azure Cloud Clean-Reset and Deployment Script for EITASM

# Configuration Variables
$ServerInstance = "eitasm-srv-vanier.database.windows.net"
$DatabaseName = "EITASM_DB"
$AdminUser = "dbadmin"
$Password = "Vanier1234"

Write-Host "=========================================================" -ForegroundColor Cyan
Write-Host " RESETTING & DEPLOYING EITASM DATABASE ON AZURE CLOUD" -ForegroundColor Cyan
Write-Host "=========================================================" -ForegroundColor Cyan

# STEP 0: CLEAN SLATE RESET (Drop objects in strict reverse-dependency order)
Write-Host "Step 0: Dropping all existing database objects..." -ForegroundColor DarkYellow
$resetSql = @"
DROP VIEW IF EXISTS HR.vw_Public_Personnel;
DROP VIEW IF EXISTS SEC.vw_AssetCompliance;
DROP PROCEDURE IF EXISTS HR.sp_OnboardEmployee;
DROP TRIGGER IF EXISTS IT.tr_ValidateClearanceOnAssignment;
DROP TRIGGER IF EXISTS SEC.tr_UpdateAssetStatusOnIncident;
DROP TABLE IF EXISTS SEC.AuditLogs;
DROP TABLE IF EXISTS SEC.SecurityIncidents;
DROP TABLE IF EXISTS IT.LicenseAssignments;
DROP TABLE IF EXISTS IT.SoftwareLicenses;
DROP TABLE IF EXISTS IT.HardwareAssets;
DROP TABLE IF EXISTS IT.AssetTypes;
IF OBJECT_ID('HR.Departments', 'U') IS NOT NULL 
    ALTER TABLE HR.Departments DROP CONSTRAINT IF EXISTS FK_Departments_Employees;
DROP TABLE IF EXISTS HR.Employees;
DROP TABLE IF EXISTS HR.Departments;
DROP TABLE IF EXISTS SEC.SeverityLevels;
"@
Invoke-Sqlcmd -ServerInstance $ServerInstance -Database $DatabaseName -Username $AdminUser -Password $Password -Query $resetSql -ErrorAction SilentlyContinue

# STEP 1: PROVISION ISOLATED ARCHITECTURAL SCHEMAS (Run individually to satisfy batch constraints)
Write-Host "Step 1: Creating isolated architectural schemas..." -ForegroundColor Yellow
Invoke-Sqlcmd -ServerInstance $ServerInstance -Database $DatabaseName -Username $AdminUser -Password $Password -Query "IF NOT EXISTS (SELECT * FROM sys.schemas WHERE name = 'HR') EXEC('CREATE SCHEMA HR;');"
Invoke-Sqlcmd -ServerInstance $ServerInstance -Database $DatabaseName -Username $AdminUser -Password $Password -Query "IF NOT EXISTS (SELECT * FROM sys.schemas WHERE name = 'IT') EXEC('CREATE SCHEMA IT;');"
Invoke-Sqlcmd -ServerInstance $ServerInstance -Database $DatabaseName -Username $AdminUser -Password $Password -Query "IF NOT EXISTS (SELECT * FROM sys.schemas WHERE name = 'SEC') EXEC('CREATE SCHEMA SEC;');"

# STEP 2: DEPLOY RELATIONAL TABLES AND CONSTRAINTS (3NF)
Write-Host "Step 2: Structuring core relational tables..." -ForegroundColor Yellow
$tablesSql = @"
CREATE TABLE SEC.SeverityLevels (SeverityLevelID INT IDENTITY(1,1) PRIMARY KEY, SeverityName VARCHAR(20) NOT NULL UNIQUE);
CREATE TABLE HR.Departments (DepartmentID INT IDENTITY(1,1) PRIMARY KEY, DeptName VARCHAR(100) NOT NULL UNIQUE, ManagerID INT NULL);
CREATE TABLE HR.Employees (EmployeeID INT IDENTITY(1,1) PRIMARY KEY, FirstName VARCHAR(50) NOT NULL, LastName VARCHAR(50) NOT NULL, DepartmentID INT NOT NULL, Email VARCHAR(255) NOT NULL UNIQUE, ClearanceLevel TINYINT NOT NULL, CONSTRAINT CK_ClearanceLevel CHECK (ClearanceLevel BETWEEN 1 AND 5), CONSTRAINT FK_Employees_Departments FOREIGN KEY (DepartmentID) REFERENCES HR.Departments(DepartmentID));
ALTER TABLE HR.Departments ADD CONSTRAINT FK_Departments_Employees FOREIGN KEY (ManagerID) REFERENCES HR.Employees(EmployeeID);
CREATE TABLE IT.AssetTypes (AssetTypeID INT IDENTITY(1,1) PRIMARY KEY, TypeName VARCHAR(50) NOT NULL UNIQUE);
CREATE TABLE IT.HardwareAssets (AssetID INT IDENTITY(1,1) PRIMARY KEY, SerialNumber VARCHAR(50) NOT NULL UNIQUE, Model VARCHAR(100) NOT NULL, AssetTypeID INT NOT NULL, PurchaseDate DATE NOT NULL, SecurityRequirement TINYINT NOT NULL, CurrentStatus VARCHAR(30) DEFAULT 'Available', AssignedToEmployeeID INT NULL, CONSTRAINT CK_SecurityRequirement CHECK (SecurityRequirement BETWEEN 1 AND 5), CONSTRAINT FK_HardwareAssets_AssetTypes FOREIGN KEY (AssetTypeID) REFERENCES IT.AssetTypes(AssetTypeID), CONSTRAINT FK_HardwareAssets_Employees FOREIGN KEY (AssignedToEmployeeID) REFERENCES HR.Employees(EmployeeID) ON DELETE SET NULL);
CREATE TABLE IT.SoftwareLicenses (LicenseID INT IDENTITY(1,1) PRIMARY KEY, SoftwareName VARCHAR(100) NOT NULL, Version VARCHAR(30) NOT NULL, TotalSeats INT NOT NULL, ExpiryDate DATE NOT NULL);
CREATE TABLE IT.LicenseAssignments (AssignmentID INT IDENTITY(1,1) PRIMARY KEY, LicenseID INT NOT NULL, EmployeeID INT NOT NULL, InstallDate DATE DEFAULT GETDATE(), CONSTRAINT FK_LicenseAssignments_Licenses FOREIGN KEY (LicenseID) REFERENCES IT.SoftwareLicenses(LicenseID) ON DELETE CASCADE, CONSTRAINT FK_LicenseAssignments_Employees FOREIGN KEY (EmployeeID) REFERENCES HR.Employees(EmployeeID) ON DELETE CASCADE);
CREATE TABLE SEC.SecurityIncidents (IncidentID INT IDENTITY(1,1) PRIMARY KEY, AssetID INT NOT NULL, ReporterID INT NOT NULL, SeverityLevelID INT NOT NULL, Description VARCHAR(MAX) NOT NULL, DateReported DATETIME DEFAULT GETDATE(), ResolutionStatus VARCHAR(30) DEFAULT 'Open', CONSTRAINT FK_SecurityIncidents_Assets FOREIGN KEY (AssetID) REFERENCES IT.HardwareAssets(AssetID), CONSTRAINT FK_SecurityIncidents_Employees FOREIGN KEY (ReporterID) REFERENCES HR.Employees(EmployeeID));
CREATE TABLE SEC.AuditLogs (LogID INT IDENTITY(1,1) PRIMARY KEY, ActionDescription VARCHAR(MAX) NOT NULL, Timestamp DATETIME DEFAULT GETDATE());
"@
Invoke-Sqlcmd -ServerInstance $ServerInstance -Database $DatabaseName -Username $AdminUser -Password $Password -Query $tablesSql

# STEP 3: DEPLOY REPORTING VIEWS
Write-Host "Step 3: Generating reporting views..." -ForegroundColor Yellow
$view1 = "CREATE VIEW HR.vw_Public_Personnel AS SELECT EmployeeID, FirstName, LastName, DepartmentID, Email FROM HR.Employees;"
$view2 = "CREATE VIEW SEC.vw_AssetCompliance AS SELECT a.AssetID, a.SerialNumber, a.Model, a.CurrentStatus, a.SecurityRequirement, e.FirstName + ' ' + e.LastName AS AssignedEmployee, e.ClearanceLevel, COUNT(i.IncidentID) AS TotalIncidents FROM IT.HardwareAssets a LEFT JOIN HR.Employees e ON a.AssignedToEmployeeID = e.EmployeeID LEFT JOIN SEC.SecurityIncidents i ON a.AssetID = i.AssetID GROUP BY a.AssetID, a.SerialNumber, a.Model, a.CurrentStatus, a.SecurityRequirement, e.FirstName, e.LastName, e.ClearanceLevel;"
Invoke-Sqlcmd -ServerInstance $ServerInstance -Database $DatabaseName -Username $AdminUser -Password $Password -Query $view1
Invoke-Sqlcmd -ServerInstance $ServerInstance -Database $DatabaseName -Username $AdminUser -Password $Password -Query $view2

# STEP 4: DEPLOY PROGRAMMABLE TRIGGERS AND PROCEDURES
Write-Host "Step 4: Compiling programmable triggers and procedures..." -ForegroundColor Yellow
$trigger1 = "CREATE TRIGGER IT.tr_ValidateClearanceOnAssignment ON IT.HardwareAssets FOR UPDATE AS BEGIN SET NOCOUNT ON; IF UPDATE(AssignedToEmployeeID) BEGIN IF EXISTS (SELECT 1 FROM inserted i JOIN HR.Employees e ON i.AssignedToEmployeeID = e.EmployeeID WHERE e.ClearanceLevel < i.SecurityRequirement) BEGIN RAISERROR ('Security Violation: Target employee has an insufficient Security Clearance level for this hardware asset.', 16, 1); ROLLBACK TRANSACTION; END END END;"
$trigger2 = "CREATE TRIGGER SEC.tr_UpdateAssetStatusOnIncident ON SEC.SecurityIncidents AFTER INSERT AS BEGIN SET NOCOUNT ON; UPDATE IT.HardwareAssets SET CurrentStatus = 'Under Investigation' WHERE AssetID IN (SELECT AssetID FROM inserted); INSERT INTO SEC.AuditLogs (ActionDescription) SELECT 'Asset system automatically flagged asset ID ' + CAST(AssetID AS VARCHAR) + ' to Under Investigation due to new logged incident.' FROM inserted; END;"
$procedure1 = @"
CREATE PROCEDURE HR.sp_OnboardEmployee @FirstName VARCHAR(50), @LastName VARCHAR(50), @DepartmentID INT, @Email VARCHAR(255), @ClearanceLevel TINYINT, @PreferredAssetTypeID INT AS BEGIN SET NOCOUNT ON; BEGIN TRANSACTION; BEGIN TRY INSERT INTO HR.Employees (FirstName, LastName, DepartmentID, Email, ClearanceLevel) VALUES (@FirstName, @LastName, @DepartmentID, @Email, @ClearanceLevel); DECLARE @NewEmployeeID INT = SCOPE_IDENTITY(); DECLARE @TargetAssetID INT = NULL; SELECT TOP 1 @TargetAssetID = AssetID FROM IT.HardwareAssets WHERE CurrentStatus = 'Available' AND AssetTypeID = @PreferredAssetTypeID AND SecurityRequirement <= @ClearanceLevel ORDER BY PurchaseDate ASC; IF @TargetAssetID IS NOT NULL BEGIN UPDATE IT.HardwareAssets SET AssignedToEmployeeID = @NewEmployeeID, CurrentStatus = 'Assigned' WHERE AssetID = @TargetAssetID; END COMMIT TRANSACTION; PRINT 'Employee profile successfully added and asset initialized.'; END TRY BEGIN CATCH ROLLBACK TRANSACTION; THROW; END CATCH END;
"@
Invoke-Sqlcmd -ServerInstance $ServerInstance -Database $DatabaseName -Username $AdminUser -Password $Password -Query $trigger1
Invoke-Sqlcmd -ServerInstance $ServerInstance -Database $DatabaseName -Username $AdminUser -Password $Password -Query $trigger2
Invoke-Sqlcmd -ServerInstance $ServerInstance -Database $DatabaseName -Username $AdminUser -Password $Password -Query $procedure1

# STEP 5: SEED FUNDAMENTAL LOOKUP DATA
Write-Host "Step 5: Seeding fundamental lookup data..." -ForegroundColor Yellow
$seedSql = @"
INSERT INTO SEC.SeverityLevels (SeverityName) VALUES ('Low'), ('Medium'), ('High'), ('Critical');
INSERT INTO IT.AssetTypes (TypeName) VALUES ('Workstation'), ('Server'), ('Mobile Device'), ('Network Equipment');
INSERT INTO HR.Departments (DeptName) VALUES ('Human Resources'), ('Information Technology'), ('Cyber Security'), ('Finance');
"@
Invoke-Sqlcmd -ServerInstance $ServerInstance -Database $DatabaseName -Username $AdminUser -Password $Password -Query $seedSql

Write-Host "=========================================================" -ForegroundColor Green
Write-Host " AZURE DATABASE RESET AND DEPLOYMENT SUCCESSFUL!" -ForegroundColor Green
Write-Host "=========================================================" -ForegroundColor Green

# STEP 6: Execute Seed File
Write-Host "Step 6: Seeding 700 employee records..." -ForegroundColor Yellow
Invoke-Sqlcmd -ServerInstance $ServerInstance -Database $DatabaseName -Username $AdminUser -Password $Password -InputFile "Seed_700_Employees.sql"
Write-Host "Employee seeding complete." -ForegroundColor Green