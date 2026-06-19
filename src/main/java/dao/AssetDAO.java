package dao;

import database.AzureDatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AssetDAO {

    public List<String[]> getAllAssets() throws SQLException {
        List<String[]> assets = new ArrayList<>();
        String sql = "SELECT a.AssetID, a.SerialNumber, a.Model, c.CategoryName, a.Status, " +
                "ISNULL(a.AssignedTo, 'Unassigned') AS AssignedTo " +
                "FROM dbo.Assets a " +
                "INNER JOIN dbo.AssetCategories c ON a.CategoryID = c.CategoryID " +
                "ORDER BY a.AssetID;";

        try (Connection conn = AzureDatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                assets.add(new String[]{
                        String.valueOf(rs.getInt("AssetID")),
                        rs.getString("SerialNumber"),
                        rs.getString("Model"),
                        rs.getString("CategoryName"),
                        rs.getString("Status"),
                        rs.getString("AssignedTo")
                });
            }
        }
        return assets;
    }

    public void addAsset(String serial, String model, int categoryId, String status, String assignedTo) throws SQLException {
        String sql = "INSERT INTO dbo.Assets (SerialNumber, Model, CategoryID, Status, AssignedTo) VALUES (?, ?, ?, ?, ?);";
        try (Connection conn = AzureDatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, serial);
            stmt.setString(2, model);
            stmt.setInt(3, categoryId);
            stmt.setString(4, status);
            stmt.setString(5, assignedTo.trim().isEmpty() ? null : assignedTo);
            stmt.executeUpdate();
        }
    }

    public void updateAsset(int assetId, String serial, String model, String status, String assignedTo) throws SQLException {
        String sql = "UPDATE dbo.Assets SET SerialNumber = ?, Model = ?, Status = ?, AssignedTo = ? WHERE AssetID = ?;";
        try (Connection conn = AzureDatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, serial);
            stmt.setString(2, model);
            stmt.setString(3, status);
            stmt.setString(4, assignedTo.trim().isEmpty() ? null : assignedTo);
            stmt.setInt(5, assetId);
            stmt.executeUpdate();
        }
    }

    public void deleteAsset(int assetId) throws SQLException {
        String sql = "DELETE FROM dbo.Assets WHERE AssetID = ?;";
        try (Connection conn = AzureDatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, assetId);
            stmt.executeUpdate();
        }
    }
}