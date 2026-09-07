package com.campus.utils;

/**
 * Validation utility methods.
 */
public class Validation {

    public static boolean isValidBuildingId(String id) {
        return id != null && !id.trim().isEmpty() && id.matches("^[a-zA-Z0-9_-]+$");
    }

    public static boolean isValidBuildingName(String name) {
        return name != null && !name.trim().isEmpty() && name.length() >= 2;
    }

    public static boolean isValidDistance(String distStr) {
        try {
            double d = Double.parseDouble(distStr);
            return d > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isValidCoordinates(String xStr, String yStr) {
        try {
            int x = Integer.parseInt(xStr);
            int y = Integer.parseInt(yStr);
            return x >= 0 && y >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean validateLogin(String username, String password, String role) {
        if (username == null || password == null) return false;
        if ("admin".equalsIgnoreCase(role)) {
            return username.equals(Constants.ADMIN_USER) && password.equals(Constants.ADMIN_PASS);
        } else {
            return username.equals(Constants.STUDENT_USER) && password.equals(Constants.STUDENT_PASS);
        }
    }
}
