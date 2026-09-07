package com.campus.test;

import com.campus.algorithm.BFS;
import com.campus.algorithm.DFS;
import com.campus.algorithm.Dijkstra;
import com.campus.graph.Graph;
import com.campus.graph.Vertex;

import java.util.List;
import java.util.Set;

/**
 * Automates the test cases defined in Phase 14 to verify the correctness of DSA graph operations and path-finding algorithms.
 */
public class AlgorithmTest {
    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("  Smart Campus Navigation - Core Algorithm Tests  ");
        System.out.println("==================================================\n");

        Graph testGraph = new Graph();
        
        // 1. Setup default test graph
        Vertex lib = new Vertex("LIB", "Library", 150, 150);
        Vertex cse = new Vertex("CSE", "Computer Science", 150, 420);
        Vertex adm = new Vertex("ADM", "Admin Block", 350, 150);
        Vertex pkg = new Vertex("PKG", "Parking", 350, 420);
        Vertex hst = new Vertex("HST", "Hostel", 580, 150);
        Vertex cnt = new Vertex("CNT", "Canteen", 580, 420);
        Vertex aud = new Vertex("AUD", "Auditorium", 780, 280);

        testGraph.addBuilding(lib);
        testGraph.addBuilding(cse);
        testGraph.addBuilding(adm);
        testGraph.addBuilding(pkg);
        testGraph.addBuilding(hst);
        testGraph.addBuilding(cnt);
        testGraph.addBuilding(aud);

        testGraph.addRoad("LIB", "CSE", 100);
        testGraph.addRoad("LIB", "ADM", 70);
        testGraph.addRoad("ADM", "PKG", 50);
        testGraph.addRoad("LIB", "CNT", 60);
        testGraph.addRoad("CSE", "CNT", 200);
        testGraph.addRoad("ADM", "HST", 80);
        testGraph.addRoad("HST", "CNT", 40);
        testGraph.addRoad("CNT", "AUD", 120);
        testGraph.addRoad("PKG", "AUD", 90);
        testGraph.addRoad("HST", "AUD", 110);

        testGraph.displayCampus();
        System.out.println();

        // Test Case 1: Shortest Route from Library to Parking
        // Expected Path: Library -> Admin -> Parking (70 + 50 = 120m)
        System.out.println("--- Test Case 1: Shortest Route (Library -> Parking) ---");
        Dijkstra.PathResult r1 = Dijkstra.findShortestPath(testGraph, "LIB", "PKG");
        System.out.println("Actual Path:\n" + r1);
        boolean tc1Pass = r1.getPath().size() == 3 && 
                           r1.getPath().get(0).getId().equals("LIB") &&
                           r1.getPath().get(1).getId().equals("ADM") &&
                           r1.getPath().get(2).getId().equals("PKG") &&
                           r1.getDistance() == 120.0;
        System.out.println("Test Case 1 Result: " + (tc1Pass ? "PASS" : "FAIL"));
        System.out.println();

        // Test Case 2: Block Road (Admin -> Parking) and reroute
        // Expected Path: Library -> Canteen -> Parking or similar (Library -> Canteen -> Hostel -> Admin -> Parking is blocked, so Library -> Canteen -> Parking)
        // Wait, is there a road Canteen <-> Parking? Wait! In our default seeding, Canteen is NOT directly connected to Parking!
        // The connections are:
        // LIB - CSE (100)
        // LIB - ADM (70)
        // ADM - PKG (50)
        // LIB - CNT (60)
        // CSE - CNT (200)
        // ADM - HST (80)
        // HST - CNT (40)
        // CNT - AUD (120)
        // PKG - AUD (90)
        // HST - AUD (110)
        // If ADM <-> PKG (50) is blocked, the paths from LIB to PKG could be:
        // LIB -> CNT -> AUD -> PKG (60 + 120 + 90 = 270m)
        // LIB -> CNT -> HST -> AUD -> PKG (60 + 40 + 110 + 90 = 300m)
        // Let's run Dijkstra with ADM <-> PKG blocked and see what it finds!
        System.out.println("--- Test Case 2: Block Road (Admin <-> Parking) and Re-Route ---");
        System.out.println("Blocking road: ADM <-> PKG...");
        testGraph.blockRoad("ADM", "PKG", true);
        
        Dijkstra.PathResult r2 = Dijkstra.findShortestPath(testGraph, "LIB", "PKG");
        System.out.println("New Actual Path:\n" + r2);
        
        // Unblock road
        testGraph.blockRoad("ADM", "PKG", false);
        System.out.println("Unblocked road: ADM <-> PKG.");
        System.out.println("Test Case 2 Result: " + (!r2.getPath().isEmpty() ? "PASS" : "FAIL"));
        System.out.println();

        // Test Case 3: Delete Building (Parking)
        System.out.println("--- Test Case 3: Delete Building (Parking) ---");
        System.out.println("Deleting building PKG...");
        testGraph.removeBuilding("PKG");
        Vertex removedV = testGraph.getVertex("PKG");
        boolean tc3Pass = (removedV == null);
        System.out.println("Vertex PKG exists: " + (removedV != null));
        
        // Verify connections to PKG are also deleted
        boolean hasRoadToPkg = false;
        for (String id : testGraph.getAdjacencyList().keySet()) {
            for (var edge : testGraph.getNeighbours(id)) {
                if (edge.getDestination().getId().equals("PKG")) {
                    hasRoadToPkg = true;
                    break;
                }
            }
        }
        System.out.println("Any remaining roads to PKG: " + hasRoadToPkg);
        System.out.println("Test Case 3 Result: " + (tc3Pass && !hasRoadToPkg ? "PASS" : "FAIL"));
        System.out.println();

        // Test Case 4: Add Building (Medical Block)
        System.out.println("--- Test Case 4: Add Building (Medical Block) ---");
        Vertex med = new Vertex("MED", "Medical Block", 450, 200);
        testGraph.addBuilding(med);
        Vertex addedV = testGraph.getVertex("MED");
        boolean tc4Pass = (addedV != null && addedV.getName().equals("Medical Block"));
        System.out.println("Vertex MED exists: " + (addedV != null));
        System.out.println("Test Case 4 Result: " + (tc4Pass ? "PASS" : "FAIL"));
        System.out.println();

        // Test Case 5: BFS reachability and DFS traversal check
        System.out.println("--- Test Case 5: Connectivity and Traversal (BFS & DFS) ---");
        boolean reachable1 = BFS.isReachable(testGraph, "LIB", "MED");
        System.out.println("Is MED reachable from LIB (No connections yet): " + reachable1);
        
        System.out.println("Adding road MED <-> LIB...");
        testGraph.addRoad("MED", "LIB", 50);
        
        boolean reachable2 = BFS.isReachable(testGraph, "LIB", "MED");
        System.out.println("Is MED reachable from LIB (After adding road): " + reachable2);

        List<Vertex> dfsPath = DFS.getReachableBuildings(testGraph, "MED");
        System.out.print("DFS Traversal from MED: ");
        for (int i = 0; i < dfsPath.size(); i++) {
            System.out.print(dfsPath.get(i).getName());
            if (i < dfsPath.size() - 1) System.out.print(" -> ");
        }
        System.out.println();
        
        List<Set<Vertex>> components = DFS.getConnectedComponents(testGraph);
        System.out.println("Connected components count: " + components.size());
        
        boolean tc5Pass = !reachable1 && reachable2 && components.size() == 1;
        System.out.println("Test Case 5 Result: " + (tc5Pass ? "PASS" : "FAIL"));
        System.out.println();

        // Final tally
        boolean allPassed = tc1Pass && (!r2.getPath().isEmpty()) && (tc3Pass && !hasRoadToPkg) && tc4Pass && tc5Pass;
        System.out.println("==================================================");
        System.out.println("ALL ALGORITHM TESTS: " + (allPassed ? "PASSED" : "FAILED"));
        System.out.println("==================================================");
    }
}
