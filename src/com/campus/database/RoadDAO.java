package com.campus.database;

import com.campus.graph.Vertex;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object for Roads. Supports SQLite and file storage fallback.
 */
public class RoadDAO {
    private static final String FILE_PATH = "database/roads.txt";

    public static class RoadRecord {
        public String sourceId;
        public String destId;
        public double distance;
        public boolean isBlocked;

        public RoadRecord(String sourceId, String destId, double distance, boolean isBlocked) {
            this.sourceId = sourceId;
            this.destId = destId;
            this.distance = distance;
            this.isBlocked = isBlocked;
        }
    }

    public static List<RoadRecord> getAllRoads() {
        List<RoadRecord> list = new ArrayList<>();
        if (DBConnection.isSqliteActive()) {
            String sql = "SELECT source, destination, distance, status FROM Roads";
            try (Connection conn = DBConnection.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    list.add(new RoadRecord(
                            rs.getString("source"),
                            rs.getString("destination"),
                            rs.getDouble("distance"),
                            "Blocked".equalsIgnoreCase(rs.getString("status"))
                    ));
                }
                return list;
            } catch (Exception e) {
                System.err.println("SQLite query failed in RoadDAO. Falling back to files: " + e.getMessage());
            }
        }

        // File-based fallback
        File file = new File(FILE_PATH);
        if (!file.exists()) return list;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    list.add(new RoadRecord(
                            parts[0],
                            parts[1],
                            Double.parseDouble(parts[2]),
                            Boolean.parseBoolean(parts[3])
                    ));
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading roads file: " + e.getMessage());
        }
        return list;
    }

    public static void saveRoads(List<RoadRecord> roads) {
        if (DBConnection.isSqliteActive()) {
            try (Connection conn = DBConnection.getConnection();
                 Statement stmt = conn.createStatement()) {
                conn.setAutoCommit(false);
                stmt.execute("DELETE FROM Roads");

                String sql = "INSERT INTO Roads (source, destination, distance, status) VALUES (?, ?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    for (RoadRecord r : roads) {
                        pstmt.setString(1, r.sourceId);
                        pstmt.setString(2, r.destId);
                        pstmt.setDouble(3, r.distance);
                        pstmt.setString(4, r.isBlocked ? "Blocked" : "Open");
                        pstmt.addBatch();
                    }
                    pstmt.executeBatch();
                }
                conn.commit();
                return;
            } catch (Exception e) {
                System.err.println("SQLite update failed in RoadDAO: " + e.getMessage());
            }
        }

        // File-based fallback
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (RoadRecord r : roads) {
                writer.write(r.sourceId + "," + r.destId + "," + r.distance + "," + r.isBlocked);
                writer.newLine();
            }
        } catch (Exception e) {
            System.err.println("Error writing roads file: " + e.getMessage());
        }
    }
}
