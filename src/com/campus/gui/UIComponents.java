package com.campus.gui;

import com.campus.utils.Constants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Reusable modern-styled Swing components for a premium look.
 */
public class UIComponents {

    /**
     * A beautiful rounded button with hover animations.
     */
    public static class ModernButton extends JButton {
        private Color normalColor = Constants.COLOR_TEAL;
        private Color hoverColor = new Color(45, 212, 191); // Teal 400
        private boolean isHovered = false;

        public ModernButton(String text) {
            super(text);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setForeground(Color.WHITE);
            setFont(new Font("Segoe UI", Font.BOLD, 13));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    isHovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    repaint();
                }
            });
        }

        public void setColors(Color normal, Color hover) {
            this.normalColor = normal;
            this.hoverColor = hover;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            if (isEnabled()) {
                g2.setColor(isHovered ? hoverColor : normalColor);
            } else {
                g2.setColor(new Color(100, 116, 139, 100)); // Disabled greyed-out Slate
            }
            
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);

            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(getText())) / 2;
            int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
            g2.setColor(isEnabled() ? getForeground() : Constants.COLOR_TEXT_SECONDARY);
            g2.setFont(getFont());
            g2.drawString(getText(), x, y);
            g2.dispose();
        }
    }

    /**
     * A rounded Panel card representation.
     */
    public static class ModernPanel extends JPanel {
        private int cornerRadius = 12;
        private Color backgroundColor = Constants.COLOR_CARD;

        public ModernPanel() {
            setOpaque(false);
            setBackground(Constants.COLOR_CARD);
        }

        public ModernPanel(int radius, Color bg) {
            setOpaque(false);
            this.cornerRadius = radius;
            this.backgroundColor = bg;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(backgroundColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);
            g2.dispose();
        }
    }

    /**
     * A rounded text field with placeholders.
     */
    public static class ModernTextField extends JTextField {
        private final String placeholder;
        private boolean isPlaceholderActive = true;

        public ModernTextField(String placeholder, int columns) {
            super(columns);
            this.placeholder = placeholder;
            setOpaque(false);
            setForeground(Constants.COLOR_TEXT_SECONDARY);
            setText(placeholder);
            setFont(Constants.FONT_BODY);
            setBorder(new EmptyBorder(8, 12, 8, 12));

            addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    if (getText().equals(placeholder)) {
                        setText("");
                        setForeground(Constants.COLOR_TEXT_PRIMARY);
                        isPlaceholderActive = false;
                    }
                    repaint();
                }

                @Override
                public void focusLost(FocusEvent e) {
                    if (getText().isEmpty()) {
                        setText(placeholder);
                        setForeground(Constants.COLOR_TEXT_SECONDARY);
                        isPlaceholderActive = true;
                    }
                    repaint();
                }
            });
        }

        public String getCleanText() {
            return isPlaceholderActive ? "" : getText().trim();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Draw background
            g2.setColor(Constants.COLOR_BG);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            
            // Draw border
            if (isFocusOwner()) {
                g2.setColor(Constants.COLOR_TEAL);
                g2.setStroke(new BasicStroke(1.5f));
            } else {
                g2.setColor(new Color(100, 116, 139, 120)); // Slate border
                g2.setStroke(new BasicStroke(1.0f));
            }
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
            g2.dispose();
            
            super.paintComponent(g);
        }
    }

    /**
     * A rounded password field.
     */
    public static class ModernPasswordField extends JPasswordField {
        private final String placeholder;
        private boolean isPlaceholderActive = true;

        public ModernPasswordField(String placeholder, int columns) {
            super(columns);
            this.placeholder = placeholder;
            setOpaque(false);
            setForeground(Constants.COLOR_TEXT_SECONDARY);
            setText(placeholder);
            setEchoChar((char) 0); // Show text for placeholder
            setFont(Constants.FONT_BODY);
            setBorder(new EmptyBorder(8, 12, 8, 12));

            addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    if (new String(getPassword()).equals(placeholder)) {
                        setText("");
                        setForeground(Constants.COLOR_TEXT_PRIMARY);
                        setEchoChar('\u2022'); // bullet
                        isPlaceholderActive = false;
                    }
                    repaint();
                }

                @Override
                public void focusLost(FocusEvent e) {
                    if (new String(getPassword()).isEmpty()) {
                        setText(placeholder);
                        setForeground(Constants.COLOR_TEXT_SECONDARY);
                        setEchoChar((char) 0);
                        isPlaceholderActive = true;
                    }
                    repaint();
                }
            });
        }

        public String getCleanPassword() {
            return isPlaceholderActive ? "" : new String(getPassword()).trim();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Draw background
            g2.setColor(Constants.COLOR_BG);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            
            // Draw border
            if (isFocusOwner()) {
                g2.setColor(Constants.COLOR_TEAL);
                g2.setStroke(new BasicStroke(1.5f));
            } else {
                g2.setColor(new Color(100, 116, 139, 120)); // Slate border
                g2.setStroke(new BasicStroke(1.0f));
            }
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
            g2.dispose();
            
            super.paintComponent(g);
        }
    }
}
