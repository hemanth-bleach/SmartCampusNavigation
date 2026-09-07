package com.campus.graph;

import java.io.Serializable;

/**
 * Represents a road (edge) in the campus map.
 */
public class Edge implements Serializable {
    private static final long serialVersionUID = 1L;

    private Vertex source;
    private Vertex destination;
    private double distance;
    private boolean isBlocked;

    public Edge(Vertex source, Vertex destination, double distance) {
        this.source = source;
        this.destination = destination;
        this.distance = distance;
        this.isBlocked = false;
    }

    public Edge(Vertex source, Vertex destination, double distance, boolean isBlocked) {
        this.source = source;
        this.destination = destination;
        this.distance = distance;
        this.isBlocked = isBlocked;
    }

    public Vertex getSource() {
        return source;
    }

    public void setSource(Vertex source) {
        this.source = source;
    }

    public Vertex getDestination() {
        return destination;
    }

    public void setDestination(Vertex destination) {
        this.destination = destination;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public boolean isBlocked() {
        return isBlocked;
    }

    public void setBlocked(boolean blocked) {
        isBlocked = blocked;
    }

    @Override
    public String toString() {
        return source.getName() + " -> " + destination.getName() + " (" + distance + "m)" + (isBlocked ? " [BLOCKED]" : "");
    }
}
