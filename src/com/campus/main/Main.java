package com.campus.main;

import com.campus.database.BuildingDAO;
import com.campus.database.RoadDAO;
import com.campus.graph.Graph;
import com.campus.graph.Vertex;
import com.campus.gui.LoginFrame;

import javax.swing.*;
import java.io.File;
import java.util.List;

/**
 * Main application entry point. Loads data, seeds defaults on first run, and launches GUI.
 */
public class Main {
    public static void main(String[] args) {
        // Set modern look and feel if system supports it, otherwise default
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Keep default Swing L&F
        }

        // Initialize Graph
        Graph graph = new Graph();

        // Load data from DB / files
        List<Vertex> buildings = BuildingDAO.getAllBuildings();
        List<RoadDAO.RoadRecord> roads = RoadDAO.getAllRoads();

        if (buildings.isEmpty()) {
            System.out.println("First-time run detected. Seeding default campus map data...");
            seedDefaultData(graph);
            
            // Save seeded data
            persistSeededData(graph);
        } else {
            System.out.println("Loading campus map (" + buildings.size() + " buildings, " + roads.size() + " roads)...");
            
            // Populate graph with loaded buildings
            for (Vertex v : buildings) {
                graph.addBuilding(v);
            }
            
            // Populate graph with loaded roads
            for (RoadDAO.RoadRecord r : roads) {
                graph.addRoad(r.sourceId, r.destId, r.distance);
                if (r.isBlocked) {
                    graph.blockRoad(r.sourceId, r.destId, true);
                }
            }
        }

        // Launch Login Dashboard
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame(graph);
            loginFrame.setVisible(true);
        });
    }

    private static void seedDefaultData(Graph graph) {
        // Default Vertices (Buildings) with visual coordinates
        Vertex lib = new Vertex("LIB", "Library", 150, 150);
        Vertex cse = new Vertex("CSE", "Computer Science", 150, 420);
        Vertex adm = new Vertex("ADM", "Admin Block", 350, 150);
        Vertex pkg = new Vertex("PKG", "Parking", 350, 420);
        Vertex hst = new Vertex("HST", "Hostel", 580, 150);
        Vertex cnt = new Vertex("CNT", "Canteen", 580, 420);
        Vertex aud = new Vertex("AUD", "Auditorium", 780, 280);

        graph.addBuilding(lib);
        graph.addBuilding(cse);
        graph.addBuilding(adm);
        graph.addBuilding(pkg);
        graph.addBuilding(hst);
        graph.addBuilding(cnt);
        graph.addBuilding(aud);

        // Default Edges (Roads)
        graph.addRoad("LIB", "CSE", 100);
        graph.addRoad("LIB", "ADM", 70);
        graph.addRoad("ADM", "PKG", 50);
        graph.addRoad("LIB", "CNT", 60);
        graph.addRoad("CSE", "CNT", 200);
        graph.addRoad("ADM", "HST", 80);
        graph.addRoad("HST", "CNT", 40);
        graph.addRoad("CNT", "AUD", 120);
        graph.addRoad("PKG", "AUD", 90);
        graph.addRoad("HST", "AUD", 110);
    }

    private static void persistSeededData(Graph graph) {
        // Ensure parent database folder exists
        File dbFolder = new File("database");
        if (!dbFolder.exists()) {
            dbFolder.mkdirs();
        }

        // Save buildings
        java.util.List<Vertex> buildings = new java.util.ArrayList<>(graph.getVertices());
        BuildingDAO.saveBuildings(buildings);

        // Save unique roads
        java.util.List<RoadDAO.RoadRecord> roads = new java.util.ArrayList<>();
        for (String srcId : graph.getAdjacencyList().keySet()) {
            for (com.campus.graph.Edge edge : graph.getNeighbours(srcId)) {
                String destId = edge.getDestination().getId();
                if (srcId.compareTo(destId) < 0) {
                    roads.add(new RoadDAO.RoadRecord(srcId, destId, edge.getDistance(), edge.isBlocked()));
                }
            }
        }
        RoadDAO.saveRoads(roads);
    }
}
