package dao;

import database.AzureDatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SecurityDAO {

    public List<String[]> getAllAssets() throws SQLException {
        List<String[]> assets = new ArrayList<>();
        String sql = "SELECT AssetID, HostName, PurchaseDate, CurrentStatus, AssignedToEmployeeID, SecurityRequirement FROM IT.HardwareAssets";
        try (Connection conn = AzureDatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                assets.add(new String[]{
                        String.valueOf(rs.getInt("AssetID")),
                        rs.getString("HostName"),
                        rs.getDate("PurchaseDate") != null ? rs.getDate("PurchaseDate").toString() : "",
                        rs.getString("CurrentStatus"),
                        rs.getObject("AssignedToEmployeeID") != null ? String.valueOf(rs.getInt("AssignedToEmployeeID")) : "",
                        String.valueOf(rs.getInt("SecurityRequirement"))
                });
            }
        }
        return assets;
    }

    public void addAsset(String hostname, String purchaseDate, String status, Integer empId, int secReq) throws SQLException {
        String sql = "INSERT INTO IT.HardwareAssets (HostName, PurchaseDate, CurrentStatus, AssignedToEmployeeID, SecurityRequirement) VALUES (?,?,?,?,?)";
        try (Connection conn = AzureDatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, hostname);
            if (purchaseDate == null || purchaseDate.isEmpty()) stmt.setNull(2, Types.DATE); else stmt.setDate(2, Date.valueOf(purchaseDate));
            stmt.setString(3, status);
            if (empId == null) stmt.setNull(4, Types.INTEGER); else stmt.setInt(4, empId);
            stmt.setInt(5, secReq);
            stmt.executeUpdate();
        }
    }

    public void updateAsset(int assetId, String hostname, String purchaseDate, String status, Integer empId, int secReq) throws SQLException {
        String sql = "UPDATE IT.HardwareAssets SET HostName=?, PurchaseDate=?, CurrentStatus=?, AssignedToEmployeeID=?, SecurityRequirement=? WHERE AssetID=?";
        try (Connection conn = AzureDatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, hostname);
            if (purchaseDate == null || purchaseDate.isEmpty()) stmt.setNull(2, Types.DATE); else stmt.setDate(2, Date.valueOf(purchaseDate));
            stmt.setString(3, status);
            if (empId == null) stmt.setNull(4, Types.INTEGER); else stmt.setInt(4, empId);
            stmt.setInt(5, secReq);
            stmt.setInt(6, assetId);
            stmt.executeUpdate();
        }
    }

    public void deleteAsset(int assetId) throws SQLException {
        String sql = "DELETE FROM IT.HardwareAssets WHERE AssetID = ?";
        try (Connection conn = AzureDatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, assetId);
            stmt.executeUpdate();
        }
    }
}