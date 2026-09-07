package com.campus.algorithm;

import com.campus.graph.Edge;
import com.campus.graph.Graph;
import com.campus.graph.Vertex;

import java.util.*;

/**
 * Breadth-First Search (BFS) implementation for checking connectivity and path existence.
 */
public class BFS {

    /**
     * Checks if a path exists between source and destination using BFS.
     * Takes blocked status of edges into account (i.e. cannot cross blocked roads).
     */
    public static boolean isReachable(Graph graph, String sourceId, String destId) {
        if (sourceId.equals(destId)) return true;

        Vertex source = graph.getVertex(sourceId);
        Vertex dest = graph.getVertex(destId);
        if (source == null || dest == null) return false;

        Queue<String> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();

        queue.add(sourceId);
        visited.add(sourceId);

        while (!queue.isEmpty()) {
            String currentId = queue.poll();

            if (currentId.equals(destId)) {
                return true;
            }

            for (Edge edge : graph.getNeighbours(currentId)) {
                if (edge.isBlocked()) continue; // Cannot traverse blocked roads

                String neighborId = edge.getDestination().getId();
                if (!visited.contains(neighborId)) {
                    visited.add(neighborId);
                    queue.add(neighborId);
                }
            }
        }

        return false;
    }

    /**
     * Finds the shortest path in terms of number of hops (edges) using BFS.
     * Useful for finding "least turn" or "nearest hops" paths.
     */
    public static List<Vertex> findShortestHopsPath(Graph graph, String sourceId, String destId) {
        Vertex source = graph.getVertex(sourceId);
        Vertex dest = graph.getVertex(destId);
        if (source == null || dest == null) return Collections.emptyList();

        Queue<String> queue = new LinkedList<>();
        Map<String, String> predecessors = new HashMap<>();
        Set<String> visited = new HashSet<>();

        queue.add(sourceId);
        visited.add(sourceId);

        boolean found = false;
        while (!queue.isEmpty()) {
            String currentId = queue.poll();

            if (currentId.equals(destId)) {
                found = true;
                break;
            }

            for (Edge edge : graph.getNeighbours(currentId)) {
                if (edge.isBlocked()) continue;

                String neighborId = edge.getDestination().getId();
                if (!visited.contains(neighborId)) {
                    visited.add(neighborId);
                    predecessors.put(neighborId, currentId);
                    queue.add(neighborId);
                }
            }
        }

        if (!found) return Collections.emptyList();

        List<Vertex> path = new ArrayList<>();
        String currId = destId;
        while (currId != null) {
            path.add(0, graph.getVertex(currId));
            currId = predecessors.get(currId);
        }
        return path;
    }
}
