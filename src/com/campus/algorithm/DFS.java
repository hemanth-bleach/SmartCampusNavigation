package com.campus.algorithm;

import com.campus.graph.Edge;
import com.campus.graph.Graph;
import com.campus.graph.Vertex;

import java.util.*;

/**
 * Depth-First Search (DFS) implementation for map connectivity analysis.
 */
public class DFS {

    /**
     * Traverses the graph from the starting vertex using DFS, returning vertices in visited order.
     * Takes road blocked status into account.
     */
    public static List<Vertex> getReachableBuildings(Graph graph, String startId) {
        Vertex start = graph.getVertex(startId);
        if (start == null) return Collections.emptyList();

        List<Vertex> visitedOrder = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        
        dfsHelper(graph, startId, visited, visitedOrder);
        
        return visitedOrder;
    }

    private static void dfsHelper(Graph graph, String currentId, Set<String> visited, List<Vertex> visitedOrder) {
        visited.add(currentId);
        visitedOrder.add(graph.getVertex(currentId));

        // Sort neighbors by name to have deterministic traversal (optional, but good for testing)
        List<Edge> neighbors = new ArrayList<>(graph.getNeighbours(currentId));
        neighbors.sort(Comparator.comparing(e -> e.getDestination().getName()));

        for (Edge edge : neighbors) {
            if (edge.isBlocked()) continue; // Skip blocked roads

            String neighborId = edge.getDestination().getId();
            if (!visited.contains(neighborId)) {
                dfsHelper(graph, neighborId, visited, visitedOrder);
            }
        }
    }

    /**
     * Identifies all disconnected components of the campus graph (e.g. islands of buildings).
     */
    public static List<Set<Vertex>> getConnectedComponents(Graph graph) {
        List<Set<Vertex>> components = new ArrayList<>();
        Set<String> visited = new HashSet<>();

        for (Vertex vertex : graph.getVertices()) {
            if (!visited.contains(vertex.getId())) {
                Set<Vertex> component = new HashSet<>();
                List<Vertex> visitedOrder = new ArrayList<>();
                
                dfsHelper(graph, vertex.getId(), visited, visitedOrder);
                
                component.addAll(visitedOrder);
                components.add(component);
            }
        }

        return components;
    }
}
