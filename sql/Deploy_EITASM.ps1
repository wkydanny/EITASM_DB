# Deploy_EITASM.ps1
# PowerShell Deployment Script for the EITASM Database System

# Configuration Variables 
$ServerInstance = "localhost" # Change SQL Server instance name if not local
$DatabaseName = "EITASM_DB"
$ProjectRoot = "C:\Users\wkyda\Documents\Vanier\SoftwareDevelopment\420-921-VA_Database\Project"

# Subdirectory Resolution
$ScriptDirectory = Join-Path $ProjectRoot "Scripts"
$BackupDirectory = Join-Path $ProjectRoot "Backups"

# Ensure directories exist locally before outputting configurations
if (!(Test-Path $ScriptDirectory)) { New-Item -ItemType Directory -Force -Path $ScriptDirectory | Out-Null }
if (!(Test-Path $BackupDirectory)) { New-Item -ItemType Directory -Force -Path $BackupDirectory | Out-Null }

$BackupFilePath = Join-Path $BackupDirectory "EITASM_Full_Backup.bak"
$BackupScriptPath = Join-Path $ScriptDirectory "Execute_Full_Backup.sql"

Write-Host "=========================================================" -ForegroundColor Cyan
Write-Host " INITIALIZING EITASM REPOSITORY LOCALHOST ENVIRONMENT" -ForegroundColor Cyan
Write-Host "=========================================================" -ForegroundColor Cyan

# 1. Drop Database if Exists and Create Fresh Instance
Write-Host "Step 1: Dropping old instances and re-mounting '$DatabaseName'..." -ForegroundColor Yellow
$initSql = @"
IF EXISTS (SELECT name FROM sys.databases WHERE name = N'$DatabaseName')
BEGIN
    ALTER DATABASE [$DatabaseName] SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE [$DatabaseName];
END
CREATE DATABASE [$DatabaseName];
"@
Invoke-Sqlcmd -ServerInstance $ServerInstance -Query $initSql -TrustServerCertificate

# 2. Deploy Schema, Tables, and Referential Constraints (3NF)
Write-Host "Step 2: Partitioning architectural schemas (HR, IT, SEC)..." -ForegroundColor Yellow
$tablesSql = @"
USE [$DatabaseName];
GO

CREATE SCHEMA HR;
GO
CREATE SCHEMA IT;
GO
CREATE SCHEMA SEC;
GO

CREATE TABLE SEC.SeverityLevels (
    SeverityLevelID INT IDENTITY(1,1) PRIMARY KEY,
    SeverityName VARCHAR(20) NOT NULL UNIQUE
);

CREATE TABLE HR.Departments (
    DepartmentID INT IDENTITY(1,1) PRIMARY KEY,
    DeptName VARCHAR(100) NOT NULL UNIQUE,
    ManagerID INT NULL
);

CREATE TABLE HR.Employees (
    EmployeeID INT IDENTITY(1,1) PRIMARY KEY,
    FirstName VARCHAR(50) NOT NULL,
    LastName VARCHAR(50) NOT NULL,
    DepartmentID INT NOT NULL,
    Email VARCHAR(255) NOT NULL UNIQUE,
    ClearanceLevel TINYINT NOT NULL,
    CONSTRAINT CK_ClearanceLevel CHECK (ClearanceLevel BETWEEN 1 AND 5),
    CONSTRAINT FK_Employees_Departments FOREIGN KEY (DepartmentID) REFERENCES HR.Departments(DepartmentID)
);

ALTER TABLE HR.Departments 
ADD CONSTRAINT FK_Departments_Employees FOREIGN KEY (ManagerID) REFERENCES HR.Employees(EmployeeID);

CREATE TABLE IT.AssetTypes (
    AssetTypeID INT IDENTITY(1,1) PRIMARY KEY,
    TypeName VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IT.HardwareAssets (
    AssetID INT IDENTITY(1,1) PRIMARY KEY,
    SerialNumber VARCHAR(50) NOT NULL UNIQUE,
    Model VARCHAR(100) NOT NULL,
    AssetTypeID INT NOT NULL,
    PurchaseDate DATE NOT NULL,
    SecurityRequirement TINYINT NOT NULL,
    CurrentStatus VARCHAR(30) DEFAULT 'Available',
    AssignedToEmployeeID INT NULL,
    CONSTRAINT CK_SecurityRequirement CHECK (SecurityRequirement BETWEEN 1 AND 5),
    CONSTRAINT FK_HardwareAssets_AssetTypes FOREIGN KEY (AssetTypeID) REFERENCES IT.AssetTypes(AssetTypeID),
    CONSTRAINT FK_HardwareAssets_Employees FOREIGN KEY (AssignedToEmployeeID) REFERENCES HR.Employees(EmployeeID) ON DELETE SET NULL
);

CREATE TABLE IT.SoftwareLicenses (
    LicenseID INT IDENTITY(1,1) PRIMARY KEY,
    SoftwareName VARCHAR(100) NOT NULL,
    Version VARCHAR(30) NOT NULL,
    TotalSeats INT NOT NULL,
    ExpiryDate DATE NOT NULL
);

CREATE TABLE IT.LicenseAssignments (
    AssignmentID INT IDENTITY(1,1) PRIMARY KEY,
    LicenseID INT NOT NULL,
    EmployeeID INT NOT NULL,
    InstallDate DATE DEFAULT GETDATE(),
    CONSTRAINT FK_LicenseAssignments_Licenses FOREIGN KEY (LicenseID) REFERENCES IT.SoftwareLicenses(LicenseID) ON DELETE CASCADE,
    CONSTRAINT FK_LicenseAssignments_Employees FOREIGN KEY (EmployeeID) REFERENCES HR.Employees(EmployeeID) ON DELETE CASCADE
);

CREATE TABLE SEC.SecurityIncidents (
    IncidentID INT IDENTITY(1,1) PRIMARY KEY,
    AssetID INT NOT NULL,
    ReporterID INT NOT NULL,
    SeverityLevelID INT NOT NULL,
    Description VARCHAR(MAX) NOT NULL,
    DateReported DATETIME DEFAULT GETDATE(),
    ResolutionStatus VARCHAR(30) DEFAULT 'Open',
    CONSTRAINT FK_SecurityIncidents_Assets FOREIGN KEY (AssetID) REFERENCES IT.HardwareAssets(AssetID),
    CONSTRAINT FK_SecurityIncidents_Employees FOREIGN KEY (ReporterID) REFERENCES HR.Employees(EmployeeID)
);

CREATE TABLE SEC.AuditLogs (
    LogID INT IDENTITY(1,1) PRIMARY KEY,
    ActionDescription VARCHAR(MAX) NOT NULL,
    Timestamp DATETIME DEFAULT GETDATE()
);
"@
Invoke-Sqlcmd -ServerInstance $ServerInstance -Query $tablesSql -TrustServerCertificate

# 3. Deploy Views for Confidentiality and Role-Based Access Control
Write-Host "Step 3: Generating decoupled views for data masking..." -ForegroundColor Yellow
$viewsSql = @"
USE [$DatabaseName];
GO

CREATE VIEW HR.vw_Public_Personnel AS
SELECT EmployeeID, FirstName, LastName, DepartmentID, Email
FROM HR.Employees;
GO

CREATE VIEW SEC.vw_AssetCompliance AS
SELECT 
    a.AssetID, a.SerialNumber, a.Model, a.CurrentStatus, a.SecurityRequirement,
    e.FirstName + ' ' + e.LastName AS AssignedEmployee,
    e.ClearanceLevel,
    COUNT(i.IncidentID) AS TotalIncidents
FROM IT.HardwareAssets a
LEFT JOIN HR.Employees e ON a.AssignedToEmployeeID = e.EmployeeID
LEFT JOIN SEC.SecurityIncidents i ON a.AssetID = i.AssetID
GROUP BY a.AssetID, a.SerialNumber, a.Model, a.CurrentStatus, a.SecurityRequirement, e.FirstName, e.LastName, e.ClearanceLevel;
GO
"@
Invoke-Sqlcmd -ServerInstance $ServerInstance -Query $viewsSql -TrustServerCertificate

# 4. Deploy Advanced Logic: Security Triggers and Automation Procedures
Write-Host "Step 4: Compiling triggers and multi-table transactions..." -ForegroundColor Yellow
$logicSql = @"
USE [$DatabaseName];
GO

CREATE TRIGGER IT.tr_ValidateClearanceOnAssignment
ON IT.HardwareAssets
FOR UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    IF UPDATE(AssignedToEmployeeID)
    BEGIN
        IF EXISTS (
            SELECT 1 
            FROM inserted i
            JOIN HR.Employees e ON i.AssignedToEmployeeID = e.EmployeeID
            WHERE e.ClearanceLevel < i.SecurityRequirement
        )
        BEGIN
            RAISERROR ('Security Violation: Target employee has an insufficient Security Clearance level for this hardware asset.', 16, 1);
            ROLLBACK TRANSACTION;
        END
    END
END;
GO

CREATE TRIGGER SEC.tr_UpdateAssetStatusOnIncident
ON SEC.SecurityIncidents
AFTER INSERT
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE IT.HardwareAssets
    SET CurrentStatus = 'Under Investigation'
    WHERE AssetID IN (SELECT AssetID FROM inserted);
    
    INSERT INTO SEC.AuditLogs (ActionDescription)
    SELECT 'Asset system automatically flagged asset ID ' + CAST(AssetID AS VARCHAR) + ' to Under Investigation due to new logged incident.'
    FROM inserted;
END;
GO

CREATE PROCEDURE HR.sp_OnboardEmployee
    @FirstName VARCHAR(50),
    @LastName VARCHAR(50),
    @DepartmentID INT,
    @Email VARCHAR(255),
    @ClearanceLevel TINYINT,
    @PreferredAssetTypeID INT
AS
BEGIN
    SET NOCOUNT ON;
    BEGIN TRANSACTION;
    BEGIN TRY
        INSERT INTO HR.Employees (FirstName, LastName, DepartmentID, Email, ClearanceLevel)
        VALUES (@FirstName, @LastName, @DepartmentID, @Email, @ClearanceLevel);
        
        DECLARE @NewEmployeeID INT = SCOPE_IDENTITY();
        DECLARE @TargetAssetID INT = NULL;

        SELECT TOP 1 @TargetAssetID = AssetID
        FROM IT.HardwareAssets
        WHERE CurrentStatus = 'Available' 
          AND AssetTypeID = @PreferredAssetTypeID 
          AND SecurityRequirement <= @ClearanceLevel
        ORDER BY PurchaseDate ASC;

        IF @TargetAssetID IS NOT NULL
        BEGIN
            UPDATE IT.HardwareAssets
            SET AssignedToEmployeeID = @NewEmployeeID,
                CurrentStatus = 'Assigned'
            WHERE AssetID = @TargetAssetID;
        END

        COMMIT TRANSACTION;
        PRINT 'Employee profile successfully added and asset initialized.';
    END TRY
    BEGIN CATCH
        ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO
"@
Invoke-Sqlcmd -ServerInstance $ServerInstance -Query $logicSql -TrustServerCertificate

# 5. Populate Core Configuration & Seed Data Lookup Structures
Write-Host "Step 5: Seeding fundamental lookup structures..." -ForegroundColor Yellow
$seedSql = @"
USE [$DatabaseName];
GO
INSERT INTO SEC.SeverityLevels (SeverityName) VALUES ('Low'), ('Medium'), ('High'), ('Critical');
INSERT INTO IT.AssetTypes (TypeName) VALUES ('Workstation'), ('Server'), ('Mobile Device'), ('Network Equipment');
INSERT INTO HR.Departments (DeptName) VALUES ('Human Resources'), ('Information Technology'), ('Cyber Security'), ('Finance');
"@
Invoke-Sqlcmd -ServerInstance $ServerInstance -Query $seedSql -TrustServerCertificate

# 6. Generate Maintenance Backup Script Deliverable
Write-Host "Step 6: Writing administrative disaster recovery deliverables..." -ForegroundColor Yellow
$backupScriptContent = @"
-- Automated Full Backup Generation Script
BACKUP DATABASE [$DatabaseName]
TO DISK = N'$BackupFilePath'
WITH NOFORMAT, INIT, 
NAME = N'$DatabaseName-Full Local Workspace Backup', 
SKIP, NOREWIND, NOUNLOAD, STATS = 10;
GO
"@
Out-File -FilePath $BackupScriptPath -InputObject $backupScriptContent -Encoding utf8

Write-Host "=========================================================" -ForegroundColor Green
Write-Host " LOCAL DEPLOYMENT COMPLETED SUCCESSFULLY" -ForegroundColor Green
Write-Host " Schema scripts generated inside your project workspace." -ForegroundColor Green
Write-Host " Ready to link tracking details to remote Git repositories." -ForegroundColor Green
Write-Host "=========================================================" -ForegroundColor Green