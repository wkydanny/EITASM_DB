-- Seed_700_Employees.sql
-- Project: Enterprise IT Asset & Security Management (EITASM)
-- Objective: Seed exactly 700 distinct employee records via a clean, memory-isolated server loop.

USE [EITASM_DB];
GO

SET NOCOUNT ON;

-- Local temporary profile array
DECLARE @WorkerTable TABLE (
    RowID INT IDENTITY(1,1),
    FN VARCHAR(50), 
    LN VARCHAR(50), 
    DeptID INT, 
    Clvl INT
);

INSERT INTO @WorkerTable (FN, LN, DeptID, Clvl) VALUES
('Alex', 'Smith', 1, 1), ('Taylor', 'Jones', 2, 3), ('Jordan', 'Miller', 3, 5),
('Morgan', 'Davis', 4, 2), ('Jamie', 'Garcia', 1, 4), ('Casey', 'Rodriguez', 2, 2),
('Dana', 'Wilson', 3, 3), ('Chris', 'Martinez', 4, 1), ('Pat', 'Anderson', 1, 4),
('Kim', 'Taylor', 2, 5), ('Sam', 'Thomas', 3, 2), ('Robin', 'Hernandez', 4, 3),
('Skyler', 'Moore', 1, 1), ('Jesse', 'Martin', 2, 4), ('Drew', 'Jackson', 3, 5),
('Danny', 'Wong', 4, 2), ('Nafiseh', 'Rad', 1, 3), ('Kwok', 'Gilberto', 2, 4),
('Silviya', 'Paskaleva', 3, 5), ('Silvia', 'Lyubomirova', 4, 1);

DECLARE @Outer INT = 1;
DECLARE @Inner INT = 1;
DECLARE @MaxInner INT = (SELECT COUNT(*) FROM @WorkerTable);

WHILE @Outer <= 35
BEGIN
    SET @Inner = 1;
    WHILE @Inner <= @MaxInner
    BEGIN
        INSERT INTO HR.Employees (FirstName, LastName, DepartmentID, Email, ClearanceLevel)
        SELECT 
            FN, 
            LN, 
            DeptID, 
            LOWER(FN + '.' + LN + CAST(@Outer AS VARCHAR) + '@enterprise.com'), 
            Clvl
        FROM @WorkerTable 
        WHERE RowID = @Inner;
        
        SET @Inner = @Inner + 1;
    END;
    SET @Outer = @Outer + 1;
END;
GO
