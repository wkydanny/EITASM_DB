# To execute the seeding loop
Invoke-Sqlcmd -ServerInstance "localhost" -InputFile "C:\Users\wkyda\Documents\Vanier\SoftwareDevelopment\420-921-VA_Database\Project\Scripts\Seed_700_Employees.sql" -TrustServerCertificate

# To check the final record count (700 employees)
Invoke-Sqlcmd -ServerInstance "localhost" -Query "SELECT COUNT(*) AS TotalEmployees FROM EITASM_DB.HR.Employees;" -TrustServerCertificate