-- Automated Full Backup Generation Script
BACKUP DATABASE [EITASM_DB]
TO DISK = N'C:\Users\wkyda\Documents\Vanier\SoftwareDevelopment\420-921-VA_Database\Project\Backups\EITASM_Full_Backup.bak'
WITH NOFORMAT, INIT, 
NAME = N'EITASM_DB-Full Local Workspace Backup', 
SKIP, NOREWIND, NOUNLOAD, STATS = 10;
GO
