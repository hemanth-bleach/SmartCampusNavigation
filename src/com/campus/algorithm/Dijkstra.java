package com.campus.algorithm;

import com.campus.graph.Edge;
import com.campus.graph.Graph;
import com.campus.graph.Vertex;

import java.util.*;

/**
 * Implements Dijkstra's algorithm for finding the shortest and alternative paths.
 */
public class Dijkstra {

    public static class PathResult {
        private List<Vertex> path;
        private double distance;
        private PathResult alternativeRoute;

        public PathResult(List<Vertex> path, double distance) {
            this.path = path;
            this.distance = distance;
            this.alternativeRoute = null;
        }

        public List<Vertex> getPath() {
            return path;
        }

        public double getDistance() {
            return distance;
        }

        public double getWalkingTimeSeconds() {
            // Speed = 1.4 m/s
            return distance / 1.4;
        }

        public String getFormattedTime() {
            double totalSeconds = getWalkingTimeSeconds();
            int minutes = (int) (totalSeconds / 60);
            int seconds = (int) (totalSeconds % 60);
            if (minutes > 0) {
                return minutes + " min " + seconds + " sec";
            } else {
                return seconds + " sec";
            }
        }

        public PathResult getAlternativeRoute() {
            return alternativeRoute;
        }

        public void setAlternativeRoute(PathResult alternativeRoute) {
            this.alternativeRoute = alternativeRoute;
        }

        @Override
        public String toString() {
            if (path == null || path.isEmpty()) {
                return "No path found.";
            }
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < path.size(); i++) {
                sb.append(path.get(i).getName());
                if (i < path.size() - 1) {
                    sb.append(" \u2193 "); // Down arrow
                }
            }
            sb.append("\nDistance: ").append(String.format("%.1f", distance)).append("m");
            sb.append("\nTime: ").append(getFormattedTime());
            return sb.toString();
        }
    }

    /**
     * Finds the shortest path between source and destination using Dijkstra's algorithm.
     * Blocked roads are ignored.
     */
    public static PathResult findShortestPath(Graph graph, String sourceId, String destId) {
        Vertex source = graph.getVertex(sourceId);
        Vertex dest = graph.getVertex(destId);
        if (source == null || dest == null) {
            return new PathResult(Collections.emptyList(), 0);
        }

        Map<String, Double> distances = new HashMap<>();
        Map<String, Vertex> predecessors = new HashMap<>();
        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingDouble(n -> n.distance));
        Set<String> visited = new HashSet<>();

        // Initialize distances
        for (Vertex v : graph.getVertices()) {
            distances.put(v.getId(), Double.MAX_VALUE);
        }
        distances.put(sourceId, 0.0);
        pq.add(new Node(sourceId, 0.0));

        while (!pq.isEmpty()) {
            Node current = pq.poll();
            String currentId = current.vertexId;

            if (visited.contains(currentId)) continue;
            visited.add(currentId);

            if (currentId.equals(destId)) break;

            for (Edge edge : graph.getNeighbours(currentId)) {
                if (edge.isBlocked()) continue; // Ignore blocked roads

                Vertex neighbor = edge.getDestination();
                double newDist = distances.get(currentId) + edge.getDistance();

                if (newDist < distances.get(neighbor.getId())) {
                    distances.put(neighbor.getId(), newDist);
                    predecessors.put(neighbor.getId(), graph.getVertex(currentId));
                    pq.add(new Node(neighbor.getId(), newDist));
                }
            }
        }

        // Reconstruct path
        if (distances.get(destId) == Double.MAX_VALUE) {
            return new PathResult(Collections.emptyList(), 0);
        }

        List<Vertex> path = new ArrayList<>();
        Vertex curr = dest;
        while (curr != null) {
            path.add(0, curr);
            curr = predecessors.get(curr.getId());
        }

        return new PathResult(path, distances.get(destId));
    }

    /**
     * Finds the primary shortest path AND calculates the second-best alternative path
     * by temporarily blocking roads in the primary path one at a time.
     */
    public static PathResult findRouteWithAlternative(Graph graph, String sourceId, String destId) {
        PathResult primary = findShortestPath(graph, sourceId, destId);
        if (primary.getPath().isEmpty()) {
            return primary;
        }

        List<Vertex> primaryPath = primary.getPath();
        PathResult bestAlternative = null;
        double minAltDistance = Double.MAX_VALUE;

        // Try blocking each road (edge) in the primary path one by one
        for (int i = 0; i < primaryPath.size() - 1; i++) {
            String u = primaryPath.get(i).getId();
            String v = primaryPath.get(i + 1).getId();

            // Temporarily block road u <-> v
            graph.blockRoad(u, v, true);

            // Re-run Dijkstra
            PathResult alt = findShortestPath(graph, sourceId, destId);
            if (!alt.getPath().isEmpty() && alt.getDistance() < minAltDistance) {
                // Ensure alternative path is not exactly identical to primary (though blocking an edge guarantees it deviates)
                minAltDistance = alt.getDistance();
                bestAlternative = alt;
            }

            // Restore road u <-> v
            graph.blockRoad(u, v, false);
        }

        if (bestAlternative != null) {
            primary.setAlternativeRoute(bestAlternative);
        }

        return primary;
    }

    private static class Node {
        String vertexId;
        double distance;

        Node(String vertexId, double distance) {
            this.vertexId = vertexId;
            this.distance = distance;
        }
    }
}
