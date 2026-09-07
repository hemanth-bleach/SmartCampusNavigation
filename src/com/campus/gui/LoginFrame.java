package com.campus.gui;

import com.campus.graph.Graph;
import com.campus.utils.Constants;
import com.campus.utils.Validation;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Modern login screen for the Smart Campus Navigation System.
 */
public class LoginFrame extends JFrame {
    private final Graph graph;
    private final JComboBox<String> roleCombo;
    private final UIComponents.ModernTextField userField;
    private final UIComponents.ModernPasswordField passField;

    public LoginFrame(Graph graph) {
        this.graph = graph;

        setTitle("Smart Campus Navigation - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(450, 520);
        setLocationRelativeTo(null);
        setResizable(false);

        // Core container
        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(Constants.COLOR_BG);
        rootPanel.setBorder(new EmptyBorder(30, 40, 30, 40));

        // Header Panel
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        headerPanel.setOpaque(false);
        JLabel titleLabel = new JLabel("Smart Campus Navigation", JLabel.CENTER);
        titleLabel.setFont(Constants.FONT_TITLE);
        titleLabel.setForeground(Constants.COLOR_TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("DSA Navigation & Mapping System", JLabel.CENTER);
        subtitleLabel.setFont(Constants.FONT_SMALL);
        subtitleLabel.setForeground(Constants.COLOR_TEAL);

        headerPanel.add(titleLabel);
        headerPanel.add(subtitleLabel);
        rootPanel.add(headerPanel, BorderLayout.NORTH);

        // Form Card Panel
        UIComponents.ModernPanel formPanel = new UIComponents.ModernPanel(16, Constants.COLOR_SIDEBAR);
        formPanel.setLayout(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(25, 25, 25, 25));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.gridx = 0;

        // Role Selection
        JLabel roleLabel = new JLabel("I am a:");
        roleLabel.setFont(Constants.FONT_BODY);
        roleLabel.setForeground(Constants.COLOR_TEXT_SECONDARY);
        gbc.gridy = 0;
        formPanel.add(roleLabel, gbc);

        roleCombo = new JComboBox<>(new String[]{"Student", "Admin"});
        roleCombo.setFont(Constants.FONT_BODY);
        roleCombo.setBackground(Constants.COLOR_BG);
        roleCombo.setForeground(Constants.COLOR_TEXT_PRIMARY);
        roleCombo.setBorder(BorderFactory.createLineBorder(new Color(100, 116, 139, 120), 1));
        gbc.gridy = 1;
        formPanel.add(roleCombo, gbc);

        // Username Field
        JLabel userLabel = new JLabel("Username:");
        userLabel.setFont(Constants.FONT_BODY);
        userLabel.setForeground(Constants.COLOR_TEXT_SECONDARY);
        gbc.gridy = 2;
        formPanel.add(userLabel, gbc);

        userField = new UIComponents.ModernTextField("student", 20);
        gbc.gridy = 3;
        formPanel.add(userField, gbc);

        // Password Field
        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(Constants.FONT_BODY);
        passLabel.setForeground(Constants.COLOR_TEXT_SECONDARY);
        gbc.gridy = 4;
        formPanel.add(passLabel, gbc);

        passField = new UIComponents.ModernPasswordField("student123", 20);
        gbc.gridy = 5;
        formPanel.add(passField, gbc);

        // Quick tip label
        JLabel tipLabel = new JLabel("Default logins: student/student123 or admin/admin123", JLabel.CENTER);
        tipLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        tipLabel.setForeground(Constants.COLOR_TEXT_SECONDARY);
        gbc.gridy = 6;
        gbc.insets = new Insets(15, 0, 0, 0);
        formPanel.add(tipLabel, gbc);

        rootPanel.add(formPanel, BorderLayout.CENTER);

        // Buttons Panel (Bottom)
        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        btnPanel.setOpaque(false);
        btnPanel.setBorder(new EmptyBorder(25, 0, 5, 0));

        UIComponents.ModernButton loginBtn = new UIComponents.ModernButton("Login");
        loginBtn.setPreferredSize(new Dimension(0, 42));
        loginBtn.addActionListener(this::handleLogin);

        UIComponents.ModernButton exitBtn = new UIComponents.ModernButton("Exit");
        exitBtn.setColors(Constants.COLOR_CARD, Constants.COLOR_CARD.brighter());
        exitBtn.addActionListener(e -> System.exit(0));

        btnPanel.add(exitBtn);
        btnPanel.add(loginBtn);
        rootPanel.add(btnPanel, BorderLayout.SOUTH);

        add(rootPanel);
        
        // Auto-focus username
        SwingUtilities.invokeLater(userField::requestFocusInWindow);
    }

    private void handleLogin(ActionEvent e) {
        String username = userField.getCleanText();
        String password = passField.getCleanPassword();
        String selectedRole = (String) roleCombo.getSelectedItem();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                    "Please fill in all credentials.", 
                    "Missing Information", 
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (Validation.validateLogin(username, password, selectedRole)) {
            // Close login, open Home dashboard
            this.dispose();
            SwingUtilities.invokeLater(() -> {
                HomeFrame homeFrame = new HomeFrame(graph, selectedRole, username);
                homeFrame.setVisible(true);
            });
        } else {
            JOptionPane.showMessageDialog(this, 
                    "Invalid Username or Password for role: " + selectedRole, 
                    "Authentication Failed", 
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
