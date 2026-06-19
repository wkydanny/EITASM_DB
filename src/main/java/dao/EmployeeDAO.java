package dao;

import database.AzureDatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmployeeDAO {

    // Retrieve all records for the dynamic search tab
    public List<String[]> getAllEmployeesSummary() throws SQLException {
        List<String[]> employees = new ArrayList<>();
        String sql = "SELECT e.EmployeeID, e.FirstName, e.LastName, e.PhoneNumber, e.Email, d.DeptName " +
                "FROM HR.Employees e " +
                "LEFT JOIN HR.Departments d ON e.DepartmentID = d.DepartmentID";

        try (Connection conn = AzureDatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                employees.add(new String[]{
                        String.valueOf(rs.getInt("EmployeeID")),
                        rs.getString("FirstName"),
                        rs.getString("LastName"),
                        rs.getString("PhoneNumber"),
                        rs.getString("Email"),
                        rs.getString("DeptName") != null ? rs.getString("DeptName") : "Unassigned"
                });
            }
        }
        return employees;
    }

    // Single record full PII details search fetcher
    public String[] getEmployeeCompleteDetails(int id) throws SQLException {
        String sql = "SELECT e.*, d.DeptName, u.Username, u.PasswordHash " +
                "FROM HR.Employees e " +
                "LEFT JOIN HR.Departments d ON e.DepartmentID = d.DepartmentID " +
                "LEFT JOIN SEC.UserCredentials u ON e.EmployeeID = u.EmployeeID " +
                "WHERE e.EmployeeID = ?";

        try (Connection conn = AzureDatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new String[]{
                            String.valueOf(rs.getInt("EmployeeID")), rs.getString("FirstName"), rs.getString("LastName"),
                            rs.getString("PhoneNumber"), rs.getString("Email"), rs.getString("CivilNumber"),
                            rs.getString("StreetName"), rs.getString("City"), rs.getString("ProvinceState"),
                            rs.getString("Country"), rs.getString("PostalZipCode"),
                            rs.getDate("DateOfBirth") != null ? rs.getDate("DateOfBirth").toString() : "",
                            rs.getString("DeptName") != null ? rs.getString("DeptName") : "",
                            String.valueOf(rs.getInt("UserSecurityLevel")),
                            rs.getString("Username") != null ? rs.getString("Username") : "",
                            rs.getString("PasswordHash") != null ? rs.getString("PasswordHash") : ""
                    };
                }
            }
        }
        return null;
    }

    // Update operational method wrapping relational tables
    public void updateEmployee(int id, String first, String last, String phone, String email, String civil,
                               String street, String city, String prov, String country, String postal,
                               String dob, int deptId, int securityLvl, String username, String password) throws SQLException {
        String updateEmp = "UPDATE HR.Employees SET FirstName=?, LastName=?, PhoneNumber=?, Email=?, CivilNumber=?, " +
                "StreetName=?, City=?, ProvinceState=?, Country=?, PostalZipCode=?, DateOfBirth=?, " +
                "UserSecurityLevel=?, DepartmentID=? WHERE EmployeeID=?";
        String updateCred = "UPDATE SEC.UserCredentials SET Username=?, PasswordHash=? WHERE EmployeeID=?";

        try (Connection conn = AzureDatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement stmt = conn.prepareStatement(updateEmp)) {
                    stmt.setString(1, first); stmt.setString(2, last); stmt.setString(3, phone); stmt.setString(4, email);
                    stmt.setString(5, civil); stmt.setString(6, street); stmt.setString(7, city); stmt.setString(8, prov);
                    stmt.setString(9, country); stmt.setString(10, postal);
                    if (dob == null || dob.isEmpty()) stmt.setNull(11, Types.DATE); else stmt.setDate(11, Date.valueOf(dob));
                    stmt.setInt(12, securityLvl);
                    if (deptId == 0) stmt.setNull(13, Types.INTEGER); else stmt.setInt(13, deptId);
                    stmt.setInt(14, id);
                    stmt.executeUpdate();
                }
                try (PreparedStatement stmt = conn.prepareStatement(updateCred)) {
                    stmt.setString(1, username);
                    stmt.setString(2, password);
                    stmt.setInt(3, id);
                    stmt.executeUpdate();
                }
                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        }
    }

    // Insert atomic transaction creation method
    public void createEmployee(String first, String last, String phone, String email, String civil,
                               String street, String city, String prov, String country, String postal,
                               String dob, int deptId, int securityLvl, String username, String password) throws SQLException {
        String insertEmp = "INSERT INTO HR.Employees (FirstName, LastName, PhoneNumber, Email, CivilNumber, StreetName, City, ProvinceState, Country, PostalZipCode, DateOfBirth, UserSecurityLevel, DepartmentID) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
        String insertCred = "INSERT INTO SEC.UserCredentials (EmployeeID, Username, PasswordHash) VALUES (?,?,?)";

        try (Connection conn = AzureDatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int newEmpId = 0;
                try (PreparedStatement stmt = conn.prepareStatement(insertEmp, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setString(1, first); stmt.setString(2, last); stmt.setString(3, phone); stmt.setString(4, email);
                    stmt.setString(5, civil); stmt.setString(6, street); stmt.setString(7, city); stmt.setString(8, prov);
                    stmt.setString(9, country); stmt.setString(10, postal);
                    if (dob == null || dob.isEmpty()) stmt.setNull(11, Types.DATE); else stmt.setDate(11, Date.valueOf(dob));
                    stmt.setInt(12, securityLvl);
                    if (deptId == 0) stmt.setNull(13, Types.INTEGER); else stmt.setInt(13, deptId);
                    stmt.executeUpdate();

                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (rs.next()) newEmpId = rs.getInt(1);
                    }
                }
                try (PreparedStatement stmt = conn.prepareStatement(insertCred)) {
                    stmt.setInt(1, newEmpId);
                    stmt.setString(2, username);
                    stmt.setString(3, password);
                    stmt.executeUpdate();
                }
                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        }
    }

    public void removeEmployee(int employeeId) throws SQLException {
        String sql = "DELETE FROM HR.Employees WHERE EmployeeID = ?";
        try (Connection conn = AzureDatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, employeeId);
            stmt.executeUpdate();
        }
    }

    public int getDeptIdByName(String name) throws SQLException {
        String sql = "SELECT DepartmentID FROM HR.Departments WHERE DeptName = ?";
        try (Connection conn = AzureDatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt("DepartmentID");
            }
        }
        return 0;
    }
}