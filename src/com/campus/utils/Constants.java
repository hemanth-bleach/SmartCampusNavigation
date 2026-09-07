package com.campus.utils;

import java.awt.Color;
import java.awt.Font;

/**
 * Global application constants.
 */
public class Constants {
    
    // Algorithm Speed
    public static final double WALKING_SPEED_MPS = 1.4; // Average walking speed: 1.4 m/s

    // File Storage Paths
    public static final String DB_FOLDER = "database";
    public static final String DB_FILE_PATH = "database/campus.db";
    public static final String JSON_FILE_PATH = "database/campus_data.json";

    // Standard Credentials
    public static final String ADMIN_USER = "admin";
    public static final String ADMIN_PASS = "admin123";
    public static final String STUDENT_USER = "student";
    public static final String STUDENT_PASS = "student123";

    // Design Color Palette (Modern Dark Mode / Glassmorphism theme)
    public static final Color COLOR_BG = new Color(30, 41, 59);         // Slate 800
    public static final Color COLOR_SIDEBAR = new Color(15, 23, 42);    // Slate 900
    public static final Color COLOR_CARD = new Color(51, 65, 85);       // Slate 700
    public static final Color COLOR_TEXT_PRIMARY = new Color(248, 250, 252); // Slate 50
    public static final Color COLOR_TEXT_SECONDARY = new Color(148, 163, 184); // Slate 400
    
    // UI Accents
    public static final Color COLOR_TEAL = new Color(20, 184, 166);     // Teal 500
    public static final Color COLOR_GOLD = new Color(245, 158, 11);     // Amber 500
    public static final Color COLOR_RED = new Color(239, 68, 68);       // Red 500
    public static final Color COLOR_GREEN = new Color(34, 197, 94);     // Green 500

    // Fonts
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 12);
}
