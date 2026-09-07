package com.campus.gui;

import com.campus.algorithm.BFS;
import com.campus.algorithm.DFS;
import com.campus.algorithm.Dijkstra;
import com.campus.database.BuildingDAO;
import com.campus.database.RoadDAO;
import com.campus.graph.Edge;
import com.campus.graph.Graph;
import com.campus.graph.Vertex;
import com.campus.utils.Constants;
import com.campus.utils.Validation;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Main dashboard container featuring student controls, admin settings, and the live interactive map.
 */
public class HomeFrame extends JFrame implements CampusMapPanel.MapSelectionListener {
    private final Graph graph;
    private final String role;
    private final String username;

    private CampusMapPanel mapPanel;
    private JComboBox<Vertex> srcCombo;
    private JComboBox<Vertex> destCombo;
    private JTextArea routeOutputArea;

    // Admin Components
    private JTabbedPane adminTabs;
    
    // Building tab
    private UIComponents.ModernTextField newBuildIdField;
    private UIComponents.ModernTextField newBuildNameField;
    private JComboBox<Vertex> deleteBuildCombo;

    // Road tab
    private JComboBox<Vertex> roadSrcCombo;
    private JComboBox<Vertex> roadDestCombo;
    private UIComponents.ModernTextField roadDistanceField;
    private JComboBox<Vertex> deleteRoadSrcCombo;
    private JComboBox<Vertex> deleteRoadDestCombo;

    // Block tab
    private JComboBox<Vertex> blockRoadSrcCombo;
    private JComboBox<Vertex> blockRoadDestCombo;
    private JCheckBox blockToggleCheckbox;

    private boolean isUpdatingDropdowns = false;

    public HomeFrame(Graph graph, String role, String username) {
        this.graph = graph;
        this.role = role;
        this.username = username;

        setTitle("Smart Campus Navigation System - (" + role + ": " + username + ")");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);

        // Main layout
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Constants.COLOR_BG);

        // 1. Header Banner
        JPanel bannerPanel = new JPanel(new BorderLayout());
        bannerPanel.setBackground(Constants.COLOR_SIDEBAR);
        bannerPanel.setBorder(new EmptyBorder(12, 20, 12, 20));

        JLabel titleLabel = new JLabel("Smart Campus Navigation Panel", JLabel.LEFT);
        titleLabel.setFont(Constants.FONT_TITLE);
        titleLabel.setForeground(Constants.COLOR_TEXT_PRIMARY);

        JPanel userInfoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        userInfoPanel.setOpaque(false);
        JLabel userLabel = new JLabel("Logged in as: " + username + " (" + role + ")");
        userLabel.setFont(Constants.FONT_BODY);
        userLabel.setForeground(Constants.COLOR_TEAL);

        UIComponents.ModernButton logoutBtn = new UIComponents.ModernButton("Logout");
        logoutBtn.setColors(Constants.COLOR_RED, Constants.COLOR_RED.brighter());
        logoutBtn.setPreferredSize(new Dimension(80, 28));
        logoutBtn.addActionListener(this::handleLogout);

        userInfoPanel.add(userLabel);
        userInfoPanel.add(logoutBtn);

        bannerPanel.add(titleLabel, BorderLayout.WEST);
        bannerPanel.add(userInfoPanel, BorderLayout.EAST);
        mainPanel.add(bannerPanel, BorderLayout.NORTH);

        // 2. Map Panel (Right side)
        mapPanel = new CampusMapPanel(graph);
        mapPanel.addMapSelectionListener(this);
        if ("admin".equalsIgnoreCase(role)) {
            mapPanel.setAdminMode(true);
        }

        // Wrap map in a nice panel with title
        JPanel mapContainer = new JPanel(new BorderLayout());
        mapContainer.setBackground(Constants.COLOR_BG);
        mapContainer.setBorder(new EmptyBorder(15, 10, 15, 15));
        
        UIComponents.ModernPanel mapCard = new UIComponents.ModernPanel(12, Constants.COLOR_SIDEBAR);
        mapCard.setLayout(new BorderLayout());
        mapCard.setBorder(new EmptyBorder(5, 5, 5, 5));
        mapCard.add(mapPanel, BorderLayout.CENTER);
        
        mapContainer.add(mapCard, BorderLayout.CENTER);
        mainPanel.add(mapContainer, BorderLayout.CENTER);

        // 3. Sidebar (Left side for controls)
        JPanel sidebarContainer = new JPanel(new BorderLayout());
        sidebarContainer.setBackground(Constants.COLOR_BG);
        sidebarContainer.setBorder(new EmptyBorder(15, 15, 15, 10));
        sidebarContainer.setPreferredSize(new Dimension(360, 0));

        UIComponents.ModernPanel sidebarCard = new UIComponents.ModernPanel(12, Constants.COLOR_SIDEBAR);
        sidebarCard.setLayout(new BorderLayout());
        sidebarCard.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Create student dashboard
        JPanel studentPanel = createStudentPanel();
        
        if ("admin".equalsIgnoreCase(role)) {
            // If admin, show both Student Routing Panel and Admin Control panel in tabs
            JTabbedPane sidebarTabs = new JTabbedPane();
            sidebarTabs.setFont(Constants.FONT_BODY);
            sidebarTabs.addTab("Navigation", studentPanel);
            
            JPanel adminPanel = createAdminPanel();
            sidebarTabs.addTab("Admin Tools", adminPanel);
            
            sidebarCard.add(sidebarTabs, BorderLayout.CENTER);
        } else {
            sidebarCard.add(studentPanel, BorderLayout.CENTER);
        }

        sidebarContainer.add(sidebarCard, BorderLayout.CENTER);
        mainPanel.add(sidebarContainer, BorderLayout.WEST);

        add(mainPanel);

        // Initialize selections
        populateDropdowns();
        triggerRouting();
    }

    private JPanel createStudentPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        // Dropdowns Group
        JLabel srcLabel = new JLabel("Start Location:");
        srcLabel.setFont(Constants.FONT_BODY);
        srcLabel.setForeground(Constants.COLOR_TEXT_SECONDARY);
        srcLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        srcCombo = new JComboBox<>();
        srcCombo.setFont(Constants.FONT_BODY);
        srcCombo.setMaximumSize(new Dimension(320, 35));
        srcCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        srcCombo.addActionListener(e -> {
            if (!isUpdatingDropdowns) {
                Vertex v = (Vertex) srcCombo.getSelectedItem();
                mapPanel.setSelections(v, (Vertex) destCombo.getSelectedItem());
                triggerRouting();
            }
        });

        JLabel destLabel = new JLabel("Destination:");
        destLabel.setFont(Constants.FONT_BODY);
        destLabel.setForeground(Constants.COLOR_TEXT_SECONDARY);
        destLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        destCombo = new JComboBox<>();
        destCombo.setFont(Constants.FONT_BODY);
        destCombo.setMaximumSize(new Dimension(320, 35));
        destCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        destCombo.addActionListener(e -> {
            if (!isUpdatingDropdowns) {
                Vertex v = (Vertex) destCombo.getSelectedItem();
                mapPanel.setSelections((Vertex) srcCombo.getSelectedItem(), v);
                triggerRouting();
            }
        });

        panel.add(srcLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(srcCombo);
        panel.add(Box.createRigidArea(new Dimension(0, 12)));
        panel.add(destLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(destCombo);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Verification Algorithms Buttons (BFS / DFS)
        JPanel searchBtnGrid = new JPanel(new GridLayout(2, 2, 8, 8));
        searchBtnGrid.setOpaque(false);
        searchBtnGrid.setMaximumSize(new Dimension(320, 70));
        searchBtnGrid.setAlignmentX(Component.LEFT_ALIGNMENT);

        UIComponents.ModernButton btnConnectivity = new UIComponents.ModernButton("Check Route (BFS)");
        btnConnectivity.setColors(Constants.COLOR_CARD, Constants.COLOR_CARD.brighter());
        btnConnectivity.addActionListener(this::handleConnectivityCheck);

        UIComponents.ModernButton btnDFSExplore = new UIComponents.ModernButton("Reachable (DFS)");
        btnDFSExplore.setColors(Constants.COLOR_CARD, Constants.COLOR_CARD.brighter());
        btnDFSExplore.addActionListener(this::handleDFSExplore);

        searchBtnGrid.add(btnConnectivity);
        searchBtnGrid.add(btnDFSExplore);
        panel.add(searchBtnGrid);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Route Result Panel
        JLabel outputTitle = new JLabel("Route Summary:");
        outputTitle.setFont(Constants.FONT_SUBTITLE);
        outputTitle.setForeground(Constants.COLOR_TEAL);
        outputTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        routeOutputArea = new JTextArea();
        routeOutputArea.setEditable(false);
        routeOutputArea.setFont(Constants.FONT_BODY);
        routeOutputArea.setBackground(Constants.COLOR_BG);
        routeOutputArea.setForeground(Constants.COLOR_TEXT_PRIMARY);
        routeOutputArea.setLineWrap(true);
        routeOutputArea.setWrapStyleWord(true);
        routeOutputArea.setMargin(new Insets(10, 10, 10, 10));

        JScrollPane scroll = new JScrollPane(routeOutputArea);
        scroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(100, 116, 139, 120), 1));
        scroll.setPreferredSize(new Dimension(320, 240));

        panel.add(outputTitle);
        panel.add(Box.createRigidArea(new Dimension(0, 8)));
        panel.add(scroll);

        return panel;
    }

    private JPanel createAdminPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        adminTabs = new JTabbedPane();
        adminTabs.setFont(Constants.FONT_SMALL);

        // 1. Buildings tab
        JPanel buildPanel = new JPanel();
        buildPanel.setLayout(new BoxLayout(buildPanel, BoxLayout.Y_AXIS));
        buildPanel.setBackground(Constants.COLOR_SIDEBAR);
        buildPanel.setBorder(new EmptyBorder(12, 12, 12, 12));

        buildPanel.add(createSectionHeader("ADD BUILDING"));
        newBuildIdField = new UIComponents.ModernTextField("Building ID (e.g. CSE)", 20);
        newBuildIdField.setMaximumSize(new Dimension(300, 32));
        buildPanel.add(newBuildIdField);
        buildPanel.add(Box.createRigidArea(new Dimension(0, 8)));

        newBuildNameField = new UIComponents.ModernTextField("Building Name (e.g. Computer Science)", 20);
        newBuildNameField.setMaximumSize(new Dimension(300, 32));
        buildPanel.add(newBuildNameField);
        buildPanel.add(Box.createRigidArea(new Dimension(0, 12)));

        UIComponents.ModernButton addBuildBtn = new UIComponents.ModernButton("Add Building");
        addBuildBtn.setMaximumSize(new Dimension(300, 32));
        addBuildBtn.addActionListener(this::handleAddBuilding);
        buildPanel.add(addBuildBtn);
        buildPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        buildPanel.add(createSectionHeader("DELETE BUILDING"));
        deleteBuildCombo = new JComboBox<>();
        deleteBuildCombo.setMaximumSize(new Dimension(300, 32));
        buildPanel.add(deleteBuildCombo);
        buildPanel.add(Box.createRigidArea(new Dimension(0, 12)));

        UIComponents.ModernButton deleteBuildBtn = new UIComponents.ModernButton("Delete Building");
        deleteBuildBtn.setColors(Constants.COLOR_RED, Constants.COLOR_RED.brighter());
        deleteBuildBtn.setMaximumSize(new Dimension(300, 32));
        deleteBuildBtn.addActionListener(this::handleDeleteBuilding);
        buildPanel.add(deleteBuildBtn);

        adminTabs.addTab("Buildings", buildPanel);

        // 2. Roads Tab
        JPanel roadPanel = new JPanel();
        roadPanel.setLayout(new BoxLayout(roadPanel, BoxLayout.Y_AXIS));
        roadPanel.setBackground(Constants.COLOR_SIDEBAR);
        roadPanel.setBorder(new EmptyBorder(12, 12, 12, 12));

        roadPanel.add(createSectionHeader("ADD / UPDATE ROAD"));
        roadSrcCombo = new JComboBox<>();
        roadSrcCombo.setMaximumSize(new Dimension(300, 32));
        roadPanel.add(roadSrcCombo);
        roadPanel.add(Box.createRigidArea(new Dimension(0, 8)));

        roadDestCombo = new JComboBox<>();
        roadDestCombo.setMaximumSize(new Dimension(300, 32));
        roadPanel.add(roadDestCombo);
        roadPanel.add(Box.createRigidArea(new Dimension(0, 8)));

        roadDistanceField = new UIComponents.ModernTextField("Distance in meters (e.g. 120)", 20);
        roadDistanceField.setMaximumSize(new Dimension(300, 32));
        roadPanel.add(roadDistanceField);
        roadPanel.add(Box.createRigidArea(new Dimension(0, 12)));

        UIComponents.ModernButton addRoadBtn = new UIComponents.ModernButton("Add / Update Road");
        addRoadBtn.setMaximumSize(new Dimension(300, 32));
        addRoadBtn.addActionListener(this::handleAddRoad);
        roadPanel.add(addRoadBtn);
        roadPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        roadPanel.add(createSectionHeader("DELETE ROAD"));
        deleteRoadSrcCombo = new JComboBox<>();
        deleteRoadSrcCombo.setMaximumSize(new Dimension(300, 32));
        roadPanel.add(deleteRoadSrcCombo);
        roadPanel.add(Box.createRigidArea(new Dimension(0, 8)));

        deleteRoadDestCombo = new JComboBox<>();
        deleteRoadDestCombo.setMaximumSize(new Dimension(300, 32));
        roadPanel.add(deleteRoadDestCombo);
        roadPanel.add(Box.createRigidArea(new Dimension(0, 12)));

        UIComponents.ModernButton deleteRoadBtn = new UIComponents.ModernButton("Delete Road");
        deleteRoadBtn.setColors(Constants.COLOR_RED, Constants.COLOR_RED.brighter());
        deleteRoadBtn.setMaximumSize(new Dimension(300, 32));
        deleteRoadBtn.addActionListener(this::handleDeleteRoad);
        roadPanel.add(deleteRoadBtn);

        adminTabs.addTab("Roads", roadPanel);

        // 3. Block / Open Road Tab
        JPanel blockPanel = new JPanel();
        blockPanel.setLayout(new BoxLayout(blockPanel, BoxLayout.Y_AXIS));
        blockPanel.setBackground(Constants.COLOR_SIDEBAR);
        blockPanel.setBorder(new EmptyBorder(12, 12, 12, 12));

        blockPanel.add(createSectionHeader("BLOCK STATUS"));
        blockRoadSrcCombo = new JComboBox<>();
        blockRoadSrcCombo.setMaximumSize(new Dimension(300, 32));
        blockPanel.add(blockRoadSrcCombo);
        blockPanel.add(Box.createRigidArea(new Dimension(0, 8)));

        blockRoadDestCombo = new JComboBox<>();
        blockRoadDestCombo.setMaximumSize(new Dimension(300, 32));
        blockPanel.add(blockRoadDestCombo);
        blockPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        blockToggleCheckbox = new JCheckBox("Road Blocked");
        blockToggleCheckbox.setFont(Constants.FONT_BODY);
        blockToggleCheckbox.setForeground(Constants.COLOR_TEXT_PRIMARY);
        blockToggleCheckbox.setOpaque(false);
        blockToggleCheckbox.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Update checkbox status when road is selected
        blockRoadSrcCombo.addActionListener(e -> updateBlockCheckbox());
        blockRoadDestCombo.addActionListener(e -> updateBlockCheckbox());

        blockPanel.add(blockToggleCheckbox);
        blockPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        UIComponents.ModernButton updateBlockBtn = new UIComponents.ModernButton("Update Block Status");
        updateBlockBtn.setMaximumSize(new Dimension(300, 32));
        updateBlockBtn.addActionListener(this::handleUpdateBlockRoad);
        blockPanel.add(updateBlockBtn);

        adminTabs.addTab("Blocking", blockPanel);

        panel.add(adminTabs, BorderLayout.CENTER);
        return panel;
    }

    private JLabel createSectionHeader(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(Constants.COLOR_TEAL);
        label.setBorder(new EmptyBorder(5, 0, 8, 0));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private void updateBlockCheckbox() {
        Vertex u = (Vertex) blockRoadSrcCombo.getSelectedItem();
        Vertex v = (Vertex) blockRoadDestCombo.getSelectedItem();
        if (u == null || v == null || u.equals(v)) {
            blockToggleCheckbox.setSelected(false);
            return;
        }

        boolean blocked = false;
        for (Edge edge : graph.getNeighbours(u.getId())) {
            if (edge.getDestination().getId().equals(v.getId())) {
                blocked = edge.isBlocked();
                break;
            }
        }
        blockToggleCheckbox.setSelected(blocked);
    }

    /**
     * Populate all dropdown selectors inside the application with the list of vertices.
     */
    private void populateDropdowns() {
        isUpdatingDropdowns = true;

        List<Vertex> list = new ArrayList<>(graph.getVertices());
        list.sort(Comparator.comparing(Vertex::getName));

        // Save active selections
        Vertex selectedSrc = (Vertex) srcCombo.getSelectedItem();
        Vertex selectedDst = (Vertex) destCombo.getSelectedItem();

        // Refresh dropdowns
        srcCombo.removeAllItems();
        destCombo.removeAllItems();

        for (Vertex v : list) {
            srcCombo.addItem(v);
            destCombo.addItem(v);
        }

        // Restore selections if valid, else pick defaults
        if (selectedSrc != null && list.contains(selectedSrc)) {
            srcCombo.setSelectedItem(selectedSrc);
        } else if (list.size() > 0) {
            srcCombo.setSelectedIndex(0);
        }

        if (selectedDst != null && list.contains(selectedDst)) {
            destCombo.setSelectedItem(selectedDst);
        } else if (list.size() > 1) {
            destCombo.setSelectedIndex(1);
        } else if (list.size() > 0) {
            destCombo.setSelectedIndex(0);
        }

        // Update Admin Combos if active
        if ("admin".equalsIgnoreCase(role)) {
            // Delete building combo
            deleteBuildCombo.removeAllItems();
            
            // Add/Delete road combos
            roadSrcCombo.removeAllItems();
            roadDestCombo.removeAllItems();
            deleteRoadSrcCombo.removeAllItems();
            deleteRoadDestCombo.removeAllItems();
            blockRoadSrcCombo.removeAllItems();
            blockRoadDestCombo.removeAllItems();

            for (Vertex v : list) {
                deleteBuildCombo.addItem(v);
                roadSrcCombo.addItem(v);
                roadDestCombo.addItem(v);
                deleteRoadSrcCombo.addItem(v);
                deleteRoadDestCombo.addItem(v);
                blockRoadSrcCombo.addItem(v);
                blockRoadDestCombo.addItem(v);
            }
        }

        // Update MapPanel selections
        mapPanel.setSelections((Vertex) srcCombo.getSelectedItem(), (Vertex) destCombo.getSelectedItem());

        isUpdatingDropdowns = false;
    }

    private void triggerRouting() {
        Vertex src = (Vertex) srcCombo.getSelectedItem();
        Vertex dest = (Vertex) destCombo.getSelectedItem();

        if (src == null || dest == null) {
            routeOutputArea.setText("No locations defined.");
            mapPanel.clearPaths();
            return;
        }

        if (src.equals(dest)) {
            routeOutputArea.setText("Source and destination are the same building:\n" + src.getName());
            mapPanel.clearPaths();
            return;
        }

        // Run Dijkstra
        Dijkstra.PathResult result = Dijkstra.findRouteWithAlternative(graph, src.getId(), dest.getId());

        if (result.getPath().isEmpty()) {
            routeOutputArea.setText("NO ROUTE FOUND\n\nThere is no open pathway between " + src.getName() + " and " + dest.getName() + ".");
            mapPanel.clearPaths();
            return;
        }

        // Format Report
        StringBuilder sb = new StringBuilder();
        sb.append("=== SHORTEST ROUTE ===\n");
        sb.append(result.toString());

        // Alternative path
        Dijkstra.PathResult alt = result.getAlternativeRoute();
        List<Vertex> altPath = Collections.emptyList();
        if (alt != null) {
            altPath = alt.getPath();
            sb.append("\n\n=== SECOND BEST ROUTE ===\n");
            sb.append(alt.toString());
        } else {
            sb.append("\n\n=== SECOND BEST ROUTE ===\nNo alternative route available.");
        }

        routeOutputArea.setText(sb.toString());
        routeOutputArea.setCaretPosition(0);

        // Highlight paths in MapPanel
        mapPanel.setPaths(result.getPath(), altPath);
    }

    private void persistData() {
        // Save buildings
        List<Vertex> buildings = new ArrayList<>(graph.getVertices());
        BuildingDAO.saveBuildings(buildings);

        // Save unique roads (undirected)
        List<RoadDAO.RoadRecord> roads = new ArrayList<>();
        for (String srcId : graph.getAdjacencyList().keySet()) {
            for (Edge edge : graph.getNeighbours(srcId)) {
                String destId = edge.getDestination().getId();
                // Avoid storing duplicate undirected roads (src < dest to do single insert)
                if (srcId.compareTo(destId) < 0) {
                    roads.add(new RoadDAO.RoadRecord(srcId, destId, edge.getDistance(), edge.isBlocked()));
                }
            }
        }
        RoadDAO.saveRoads(roads);
    }

    // Callbacks from MapPanel
    @Override
    public void onSourceSelected(Vertex v) {
        if (v == null) return;
        isUpdatingDropdowns = true;
        srcCombo.setSelectedItem(v);
        isUpdatingDropdowns = false;
        triggerRouting();
    }

    @Override
    public void onDestinationSelected(Vertex v) {
        if (v == null) return;
        isUpdatingDropdowns = true;
        destCombo.setSelectedItem(v);
        isUpdatingDropdowns = false;
        triggerRouting();
    }

    @Override
    public void onMapUpdated() {
        // Automatically save edits from node drags or interactive map additions
        persistData();
        populateDropdowns();
        triggerRouting();
    }

    // Handlers
    private void handleLogout(ActionEvent e) {
        this.dispose();
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame(graph);
            loginFrame.setVisible(true);
        });
    }

    private void handleConnectivityCheck(ActionEvent e) {
        Vertex src = (Vertex) srcCombo.getSelectedItem();
        Vertex dest = (Vertex) destCombo.getSelectedItem();

        if (src == null || dest == null) return;

        boolean reachable = BFS.isReachable(graph, src.getId(), dest.getId());
        
        if (reachable) {
            List<Vertex> hopPath = BFS.findShortestHopsPath(graph, src.getId(), dest.getId());
            StringBuilder sb = new StringBuilder();
            sb.append("A route exists between these locations!\n\n");
            sb.append("Shortest in terms of roads/hops (").append(hopPath.size() - 1).append(" turns):\n");
            for (int i = 0; i < hopPath.size(); i++) {
                sb.append(hopPath.get(i).getName());
                if (i < hopPath.size() - 1) sb.append(" -> ");
            }
            JOptionPane.showMessageDialog(this, sb.toString(), "Route Connectivity Valid", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, 
                    "No route exists between " + src.getName() + " and " + dest.getName() + ".\nEnsure no critical roads are blocked.", 
                    "Disconnected Buildings", 
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleDFSExplore(ActionEvent e) {
        Vertex src = (Vertex) srcCombo.getSelectedItem();
        if (src == null) return;

        List<Vertex> reachable = DFS.getReachableBuildings(graph, src.getId());
        StringBuilder sb = new StringBuilder();
        sb.append("Reachable buildings from ").append(src.getName()).append(" (DFS traversal):\n\n");
        for (Vertex v : reachable) {
            sb.append("- ").append(v.getName()).append(" (").append(v.getId()).append(")\n");
        }

        // Show connected components count
        List<java.util.Set<Vertex>> components = DFS.getConnectedComponents(graph);
        sb.append("\nTotal connected components on campus map: ").append(components.size());

        JOptionPane.showMessageDialog(this, sb.toString(), "DFS Map Traversal", JOptionPane.INFORMATION_MESSAGE);
    }

    // Admin Actions
    private void handleAddBuilding(ActionEvent e) {
        String id = newBuildIdField.getCleanText().toUpperCase();
        String name = newBuildNameField.getCleanText();

        if (!Validation.isValidBuildingId(id)) {
            JOptionPane.showMessageDialog(this, "Invalid Building ID. Use alphanumeric, dashes, and underscores only.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!Validation.isValidBuildingName(name)) {
            JOptionPane.showMessageDialog(this, "Invalid Building Name. Must be at least 2 characters.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (graph.getVertex(id) != null) {
            JOptionPane.showMessageDialog(this, "Building with ID " + id + " already exists.", "Entity Collision", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Place near center of visual map initially
        Vertex v = new Vertex(id, name, 400 + (int)(Math.random() * 80), 250 + (int)(Math.random() * 80));
        graph.addBuilding(v);
        
        // Persist and rebuild dropdowns
        persistData();
        populateDropdowns();
        triggerRouting();

        newBuildIdField.setText("Building ID (e.g. CSE)");
        newBuildNameField.setText("Building Name (e.g. Computer Science)");
        JOptionPane.showMessageDialog(this, "Building " + name + " added! You can drag it on the map.", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void handleDeleteBuilding(ActionEvent e) {
        Vertex selected = (Vertex) deleteBuildCombo.getSelectedItem();
        if (selected == null) return;

        int result = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to delete building: " + selected.getName() + "?\nThis will remove all roads connected to it.", 
                "Confirm Deletion", 
                JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            graph.removeBuilding(selected.getId());
            persistData();
            populateDropdowns();
            triggerRouting();
            JOptionPane.showMessageDialog(this, "Building deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void handleAddRoad(ActionEvent e) {
        Vertex src = (Vertex) roadSrcCombo.getSelectedItem();
        Vertex dest = (Vertex) roadDestCombo.getSelectedItem();
        String distStr = roadDistanceField.getCleanText();

        if (src == null || dest == null) return;

        if (src.equals(dest)) {
            JOptionPane.showMessageDialog(this, "Source and destination cannot be the same building.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!Validation.isValidDistance(distStr)) {
            JOptionPane.showMessageDialog(this, "Invalid Distance. Must be a positive numeric value.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        double distance = Double.parseDouble(distStr);
        graph.addRoad(src.getId(), dest.getId(), distance);
        
        persistData();
        populateDropdowns();
        triggerRouting();

        roadDistanceField.setText("Distance in meters (e.g. 120)");
        JOptionPane.showMessageDialog(this, "Road connection established: " + src.getName() + " <-> " + dest.getName() + " (" + distance + "m)", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void handleDeleteRoad(ActionEvent e) {
        Vertex src = (Vertex) deleteRoadSrcCombo.getSelectedItem();
        Vertex dest = (Vertex) deleteRoadDestCombo.getSelectedItem();

        if (src == null || dest == null) return;

        if (src.equals(dest)) {
            JOptionPane.showMessageDialog(this, "Select two different buildings.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Check if road exists
        boolean exists = false;
        for (Edge edge : graph.getNeighbours(src.getId())) {
            if (edge.getDestination().getId().equals(dest.getId())) {
                exists = true;
                break;
            }
        }

        if (!exists) {
            JOptionPane.showMessageDialog(this, "No road exists between " + src.getName() + " and " + dest.getName(), "Road Not Found", JOptionPane.WARNING_MESSAGE);
            return;
        }

        graph.removeRoad(src.getId(), dest.getId());
        persistData();
        populateDropdowns();
        triggerRouting();
        JOptionPane.showMessageDialog(this, "Road removed successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void handleUpdateBlockRoad(ActionEvent e) {
        Vertex src = (Vertex) blockRoadSrcCombo.getSelectedItem();
        Vertex dest = (Vertex) blockRoadDestCombo.getSelectedItem();
        boolean block = blockToggleCheckbox.isSelected();

        if (src == null || dest == null) return;

        if (src.equals(dest)) {
            JOptionPane.showMessageDialog(this, "Select two different buildings.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Check if road exists
        boolean exists = false;
        for (Edge edge : graph.getNeighbours(src.getId())) {
            if (edge.getDestination().getId().equals(dest.getId())) {
                exists = true;
                break;
            }
        }

        if (!exists) {
            JOptionPane.showMessageDialog(this, "No road exists between " + src.getName() + " and " + dest.getName(), "Road Not Found", JOptionPane.WARNING_MESSAGE);
            return;
        }

        graph.blockRoad(src.getId(), dest.getId(), block);
        persistData();
        populateDropdowns();
        triggerRouting();
        JOptionPane.showMessageDialog(this, "Road block status updated: " + (block ? "BLOCKED" : "OPEN"), "Success", JOptionPane.INFORMATION_MESSAGE);
    }
}
