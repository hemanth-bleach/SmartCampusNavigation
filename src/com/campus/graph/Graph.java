package com.campus.graph;

import java.io.Serializable;
import java.util.*;

/**
 * Represents the campus graph.
 */
public class Graph implements Serializable {
    private static final long serialVersionUID = 1L;

    private Map<String, Vertex> vertices;
    private Map<String, List<Edge>> adjacencyList;

    public Graph() {
        this.vertices = new HashMap<>();
        this.adjacencyList = new HashMap<>();
    }

    public void addBuilding(Vertex v) {
        if (v == null || v.getId() == null) return;
        vertices.put(v.getId(), v);
        adjacencyList.putIfAbsent(v.getId(), new ArrayList<>());
    }

    public void removeBuilding(String id) {
        if (!vertices.containsKey(id)) return;
        
        // Remove all roads connected to this building
        vertices.remove(id);
        adjacencyList.remove(id);
        
        for (List<Edge> edges : adjacencyList.values()) {
            edges.removeIf(edge -> edge.getDestination().getId().equals(id));
        }
    }

    public void addRoad(String srcId, String destId, double dist) {
        Vertex src = vertices.get(srcId);
        Vertex dest = vertices.get(destId);
        if (src == null || dest == null) return;

        // Since campus roads are two-way, add edges in both directions
        // Remove existing roads between these two first to avoid duplicates
        removeRoad(srcId, destId);

        adjacencyList.get(srcId).add(new Edge(src, dest, dist));
        adjacencyList.get(destId).add(new Edge(dest, src, dist));
    }

    public void removeRoad(String srcId, String destId) {
        if (adjacencyList.containsKey(srcId)) {
            adjacencyList.get(srcId).removeIf(edge -> edge.getDestination().getId().equals(destId));
        }
        if (adjacencyList.containsKey(destId)) {
            adjacencyList.get(destId).removeIf(edge -> edge.getDestination().getId().equals(srcId));
        }
    }

    public void blockRoad(String srcId, String destId, boolean block) {
        if (adjacencyList.containsKey(srcId)) {
            for (Edge edge : adjacencyList.get(srcId)) {
                if (edge.getDestination().getId().equals(destId)) {
                    edge.setBlocked(block);
                }
            }
        }
        if (adjacencyList.containsKey(destId)) {
            for (Edge edge : adjacencyList.get(destId)) {
                if (edge.getDestination().getId().equals(srcId)) {
                    edge.setBlocked(block);
                }
            }
        }
    }

    public List<Edge> getNeighbours(String id) {
        return adjacencyList.getOrDefault(id, new ArrayList<>());
    }

    public Collection<Vertex> getVertices() {
        return vertices.values();
    }

    public Vertex getVertex(String id) {
        return vertices.get(id);
    }

    public Vertex getVertexByName(String name) {
        for (Vertex v : vertices.values()) {
            if (v.getName().equalsIgnoreCase(name)) {
                return v;
            }
        }
        return null;
    }

    public Map<String, List<Edge>> getAdjacencyList() {
        return adjacencyList;
    }

    public void displayCampus() {
        System.out.println("Campus Buildings & Connections:");
        for (Vertex v : vertices.values()) {
            System.out.print(v.getName() + " (" + v.getId() + ") -> ");
            List<Edge> edges = adjacencyList.get(v.getId());
            if (edges != null) {
                for (Edge e : edges) {
                    System.out.print(e.getDestination().getName() + " (" + e.getDistance() + "m" + (e.isBlocked() ? ", BLOCKED" : "") + ") | ");
                }
            }
            System.out.println();
        }
    }
}
