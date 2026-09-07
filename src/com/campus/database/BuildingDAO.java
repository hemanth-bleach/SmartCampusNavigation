package com.campus.database;

import com.campus.graph.Vertex;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Buildings. Supports SQLite and file storage fallback.
 */
public class BuildingDAO {
    private static final String FILE_PATH = "database/buildings.txt";

    public static List<Vertex> getAllBuildings() {
        List<Vertex> list = new ArrayList<>();
        if (DBConnection.isSqliteActive()) {
            String sql = "SELECT id, name, x, y FROM Buildings";
            try (Connection conn = DBConnection.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    list.add(new Vertex(
                            rs.getString("id"),
                            rs.getString("name"),
                            rs.getInt("x"),
                            rs.getInt("y")
                    ));
                }
                return list;
            } catch (Exception e) {
                System.err.println("SQLite query failed in BuildingDAO. Falling back to files: " + e.getMessage());
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
                    list.add(new Vertex(
                            parts[0],
                            parts[1],
                            Integer.parseInt(parts[2]),
                            Integer.parseInt(parts[3])
                    ));
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading buildings file: " + e.getMessage());
        }
        return list;
    }

    public static void saveBuildings(List<Vertex> buildings) {
        if (DBConnection.isSqliteActive()) {
            try (Connection conn = DBConnection.getConnection();
                 Statement stmt = conn.createStatement()) {
                conn.setAutoCommit(false);
                stmt.execute("DELETE FROM Buildings");
                
                String sql = "INSERT INTO Buildings (id, name, x, y) VALUES (?, ?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    for (Vertex v : buildings) {
                        pstmt.setString(1, v.getId());
                        pstmt.setString(2, v.getName());
                        pstmt.setInt(3, v.getX());
                        pstmt.setInt(4, v.getY());
                        pstmt.addBatch();
                    }
                    pstmt.executeBatch();
                }
                conn.commit();
                return;
            } catch (Exception e) {
                System.err.println("SQLite update failed in BuildingDAO: " + e.getMessage());
            }
        }

        // File-based fallback
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (Vertex v : buildings) {
                writer.write(v.getId() + "," + v.getName() + "," + v.getX() + "," + v.getY());
                writer.newLine();
            }
        } catch (Exception e) {
            System.err.println("Error writing buildings file: " + e.getMessage());
        }
    }
}
