package com.campus.gui;

import com.campus.graph.Edge;
import com.campus.graph.Graph;
import com.campus.graph.Vertex;
import com.campus.utils.Constants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom Swing component that visualizes the campus graph.
 * Supports clicking to select source/destination, dragging to move buildings,
 * and double-clicking to add buildings.
 */
public class CampusMapPanel extends JPanel {
    private final Graph graph;
    private Vertex selectedSource = null;
    private Vertex selectedDest = null;
    private List<Vertex> shortestPath = new ArrayList<>();
    private List<Vertex> alternativePath = new ArrayList<>();

    private Vertex draggedVertex = null;
    private Point dragOffset = new Point();

    private final List<MapSelectionListener> listeners = new ArrayList<>();
    private boolean isAdminMode = false;

    public interface MapSelectionListener {
        void onSourceSelected(Vertex v);
        void onDestinationSelected(Vertex v);
        void onMapUpdated();
    }

    public CampusMapPanel(Graph graph) {
        this.graph = graph;
        setBackground(Constants.COLOR_BG);
        setDoubleBuffered(true);

        // Add mouse interactions
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Vertex clicked = getVertexAt(e.getPoint());
                if (clicked != null) {
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        if (isAdminMode) {
                            // Start dragging
                            draggedVertex = clicked;
                            dragOffset.x = e.getX() - clicked.getX();
                            dragOffset.y = e.getY() - clicked.getY();
                        } else {
                            // Select source
                            selectedSource = clicked;
                            notifySourceSelected(clicked);
                            repaint();
                        }
                    } else if (SwingUtilities.isRightMouseButton(e)) {
                        // Select destination
                        selectedDest = clicked;
                        notifyDestinationSelected(clicked);
                        repaint();
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (draggedVertex != null) {
                    // Update database or save data
                    notifyMapUpdated();
                    draggedVertex = null;
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (isAdminMode && draggedVertex != null) {
                    int x = e.getX() - dragOffset.x;
                    int y = e.getY() - dragOffset.y;
                    
                    // Constrain within panel boundaries
                    x = Math.max(25, Math.min(x, getWidth() - 25));
                    y = Math.max(25, Math.min(y, getHeight() - 25));

                    draggedVertex.setX(x);
                    draggedVertex.setY(y);
                    repaint();
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (isAdminMode && e.getClickCount() == 2) {
                    Vertex clicked = getVertexAt(e.getPoint());
                    if (clicked == null) {
                        // Double click empty space to add building
                        addNewBuildingDialog(e.getPoint());
                    }
                }
            }
        };

        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);
    }

    public void addMapSelectionListener(MapSelectionListener listener) {
        listeners.add(listener);
    }

    public void setAdminMode(boolean admin) {
        this.isAdminMode = admin;
    }

    public void setSelections(Vertex src, Vertex dest) {
        this.selectedSource = src;
        this.selectedDest = dest;
        repaint();
    }

    public void setPaths(List<Vertex> primary, List<Vertex> alternative) {
        this.shortestPath = primary != null ? primary : new ArrayList<>();
        this.alternativePath = alternative != null ? alternative : new ArrayList<>();
        repaint();
    }

    public void clearPaths() {
        this.shortestPath.clear();
        this.alternativePath.clear();
        repaint();
    }

    private Vertex getVertexAt(Point p) {
        double radius = 20;
        for (Vertex v : graph.getVertices()) {
            double dist = p.distance(v.getX(), v.getY());
            if (dist <= radius) {
                return v;
            }
        }
        return null;
    }

    private void notifySourceSelected(Vertex v) {
        for (MapSelectionListener listener : listeners) {
            listener.onSourceSelected(v);
        }
    }

    private void notifyDestinationSelected(Vertex v) {
        for (MapSelectionListener listener : listeners) {
            listener.onDestinationSelected(v);
        }
    }

    private void notifyMapUpdated() {
        for (MapSelectionListener listener : listeners) {
            listener.onMapUpdated();
        }
    }

    private void addNewBuildingDialog(Point p) {
        JTextField idField = new JTextField(10);
        JTextField nameField = new JTextField(20);

        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        panel.add(new JLabel("Building ID:"));
        panel.add(idField);
        panel.add(new JLabel("Building Name:"));
        panel.add(nameField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Add New Building",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String id = idField.getText().trim();
            String name = nameField.getText().trim();

            if (id.isEmpty() || name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Fields cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (graph.getVertex(id) != null) {
                JOptionPane.showMessageDialog(this, "Building ID already exists.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Vertex newV = new Vertex(id, name, p.x, p.y);
            graph.addBuilding(newV);
            notifyMapUpdated();
            repaint();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        
        // Turn on anti-aliasing for smooth circles and text
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Draw grid lines (subtle dark design)
        g2.setColor(new Color(40, 50, 70, 50));
        for (int i = 0; i < getWidth(); i += 40) {
            g2.drawLine(i, 0, i, getHeight());
        }
        for (int i = 0; i < getHeight(); i += 40) {
            g2.drawLine(0, i, getWidth(), i);
        }

        // Draw normal & blocked edges (roads)
        for (String srcId : graph.getAdjacencyList().keySet()) {
            Vertex src = graph.getVertex(srcId);
            if (src == null) continue;
            for (Edge edge : graph.getNeighbours(srcId)) {
                Vertex dest = edge.getDestination();
                if (dest == null) continue;

                // To avoid drawing bidirected roads twice, draw only if source ID is lexicographically less
                if (src.getId().compareTo(dest.getId()) < 0) {
                    drawEdge(g2, src, dest, edge.isBlocked(), false, false);
                }
            }
        }

        // Draw alternative path overlay if present (drawn in secondary path highlight)
        if (!alternativePath.isEmpty()) {
            for (int i = 0; i < alternativePath.size() - 1; i++) {
                Vertex src = alternativePath.get(i);
                Vertex dest = alternativePath.get(i + 1);
                drawEdge(g2, src, dest, false, false, true);
            }
        }

        // Draw shortest path overlay
        if (!shortestPath.isEmpty()) {
            for (int i = 0; i < shortestPath.size() - 1; i++) {
                Vertex src = shortestPath.get(i);
                Vertex dest = shortestPath.get(i + 1);
                drawEdge(g2, src, dest, false, true, false);
            }
        }

        // Draw vertices (buildings)
        for (Vertex v : graph.getVertices()) {
            drawVertex(g2, v);
        }

        // Draw Legend in corner
        drawLegend(g2);
    }

    private void drawEdge(Graphics2D g2, Vertex src, Vertex dest, boolean isBlocked, boolean isPrimaryPath, boolean isAlternativePath) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        if (isPrimaryPath) {
            g2.setColor(Constants.COLOR_GOLD);
            g2.setStroke(new BasicStroke(5.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        } else if (isAlternativePath) {
            g2.setColor(new Color(20, 184, 166, 180)); // Cyan/Teal transparent overlay
            g2.setStroke(new BasicStroke(3.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, new float[]{8.0f, 4.0f}, 0.0f));
        } else if (isBlocked) {
            g2.setColor(Constants.COLOR_RED);
            g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, new float[]{5.0f, 5.0f}, 0.0f));
        } else {
            g2.setColor(new Color(100, 116, 139)); // Slate 500
            g2.setStroke(new BasicStroke(2.0f));
        }

        g2.draw(new Line2D.Double(src.getX(), src.getY(), dest.getX(), dest.getY()));

        // Draw distance label
        double midX = (src.getX() + dest.getX()) / 2.0;
        double midY = (src.getY() + dest.getY()) / 2.0;
        
        // Calculate distance
        double distance = Math.hypot(src.getX() - dest.getX(), src.getY() - dest.getY());
        // Find actual distance from graph to display
        double displayDist = 0;
        for (Edge edge : graph.getNeighbours(src.getId())) {
            if (edge.getDestination().getId().equals(dest.getId())) {
                displayDist = edge.getDistance();
                break;
            }
        }
        
        String distText = String.format("%.0fm", displayDist);
        if (isBlocked) distText = "BLOCKED";

        // Paint background box for text readability
        g2.setFont(Constants.FONT_SMALL);
        FontMetrics fm = g2.getFontMetrics();
        int width = fm.stringWidth(distText) + 6;
        int height = fm.getHeight() + 2;
        
        g2.setColor(Constants.COLOR_SIDEBAR);
        g2.fillRoundRect((int) (midX - width / 2.0), (int) (midY - height / 2.0), width, height, 4, 4);

        if (isBlocked) {
            g2.setColor(Constants.COLOR_RED);
        } else if (isPrimaryPath) {
            g2.setColor(Constants.COLOR_GOLD);
        } else if (isAlternativePath) {
            g2.setColor(Constants.COLOR_TEAL);
        } else {
            g2.setColor(Constants.COLOR_TEXT_SECONDARY);
        }
        g2.drawString(distText, (float) (midX - fm.stringWidth(distText) / 2.0), (float) (midY + fm.getAscent() / 2.0 - 2));
    }

    private void drawVertex(Graphics2D g2, Vertex v) {
        int r = 16; // Node radius

        // Set background color of node
        if (v.equals(selectedSource)) {
            g2.setColor(Constants.COLOR_GREEN); // Source Node is Green
        } else if (v.equals(selectedDest)) {
            g2.setColor(Constants.COLOR_RED); // Destination Node is Red
        } else if (shortestPath.contains(v)) {
            g2.setColor(Constants.COLOR_GOLD); // Shortest path nodes are highlighted Gold
        } else {
            g2.setColor(Constants.COLOR_TEAL); // Default Node color is Teal
        }

        // Draw shadow
        g2.setColor(new Color(0, 0, 0, 100));
        g2.fill(new Ellipse2D.Double(v.getX() - r + 2, v.getY() - r + 3, r * 2, r * 2));

        // Draw node body
        if (v.equals(selectedSource)) {
            g2.setColor(Constants.COLOR_GREEN);
        } else if (v.equals(selectedDest)) {
            g2.setColor(Constants.COLOR_RED);
        } else if (shortestPath.contains(v)) {
            g2.setColor(Constants.COLOR_GOLD);
        } else {
            g2.setColor(Constants.COLOR_CARD);
        }
        g2.fill(new Ellipse2D.Double(v.getX() - r, v.getY() - r, r * 2, r * 2));

        // Draw node border
        if (v.equals(selectedSource)) {
            g2.setColor(new Color(240, 253, 244)); // White-Green glow
            g2.setStroke(new BasicStroke(3.0f));
        } else if (v.equals(selectedDest)) {
            g2.setColor(new Color(254, 242, 242)); // White-Red glow
            g2.setStroke(new BasicStroke(3.0f));
        } else if (shortestPath.contains(v)) {
            g2.setColor(new Color(255, 255, 255));
            g2.setStroke(new BasicStroke(2.0f));
        } else {
            g2.setColor(Constants.COLOR_TEAL);
            g2.setStroke(new BasicStroke(2.0f));
        }
        g2.draw(new Ellipse2D.Double(v.getX() - r, v.getY() - r, r * 2, r * 2));

        // Draw text inside or below node
        g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
        String name = v.getName();
        FontMetrics fm = g2.getFontMetrics();
        int nameW = fm.stringWidth(name);

        // Draw pill background for label to be highly readable
        g2.setColor(new Color(15, 23, 42, 200)); // semi-transparent Slate 900
        g2.fillRoundRect(v.getX() - nameW / 2 - 4, v.getY() + r + 3, nameW + 8, fm.getHeight() + 2, 6, 6);

        g2.setColor(Constants.COLOR_TEXT_PRIMARY);
        g2.drawString(name, v.getX() - nameW / 2, v.getY() + r + fm.getAscent() + 3);
    }

    private void drawLegend(Graphics2D g2) {
        int x = 20;
        int y = getHeight() - 110;
        int w = 180;
        int h = 95;

        // Draw legend background
        g2.setColor(new Color(15, 23, 42, 220));
        g2.fillRoundRect(x, y, w, h, 8, 8);
        g2.setColor(new Color(100, 116, 139, 100));
        g2.setStroke(new BasicStroke(1.0f));
        g2.drawRoundRect(x, y, w, h, 8, 8);

        g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
        g2.setColor(Constants.COLOR_TEXT_PRIMARY);
        g2.drawString("LEGEND", x + 10, y + 18);

        g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));

        // Source
        g2.setColor(Constants.COLOR_GREEN);
        g2.fillOval(x + 10, y + 28, 8, 8);
        g2.setColor(Constants.COLOR_TEXT_SECONDARY);
        g2.drawString("Source Building (Left Click)", x + 25, y + 35);

        // Destination
        g2.setColor(Constants.COLOR_RED);
        g2.fillOval(x + 10, y + 43, 8, 8);
        g2.setColor(Constants.COLOR_TEXT_SECONDARY);
        g2.drawString("Dest Building (Right Click)", x + 25, y + 50);

        // Shortest Route
        g2.setColor(Constants.COLOR_GOLD);
        g2.setStroke(new BasicStroke(3.0f));
        g2.drawLine(x + 10, y + 63, x + 20, y + 63);
        g2.setColor(Constants.COLOR_TEXT_SECONDARY);
        g2.drawString("Shortest Route", x + 25, y + 67);

        // Alt Route
        g2.setColor(Constants.COLOR_TEAL);
        g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0f, new float[]{3.0f, 3.0f}, 0.0f));
        g2.drawLine(x + 10, y + 78, x + 20, y + 78);
        g2.setColor(Constants.COLOR_TEXT_SECONDARY);
        g2.drawString("Alternative Route", x + 25, y + 82);
    }
}
