package com.snu.project;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder; // Used for simple shadow (though custom painting is better for neumorphism)

public class GeminiLikeAppResizable extends JFrame {

    private JPanel sidebarPanel;
    private JPanel mainContentPanel;
    private CardLayout cardLayout; // For switching content

    // Define colors for the light theme
    private final Color SIDEBAR_DEFAULT_COLOR = new Color(245, 245, 245); // Changed to a very light grey/off-white
    private final Color SIDEBAR_HOVER_COLOR = new Color(230, 230, 230);   // Slightly darker light grey for hover
    private final Color MAIN_CONTENT_COLOR = Color.WHITE;                  // White for the middle screen

    // Colors for neumorphic effect
    private final Color NEUMORPHIC_LIGHT_SHADOW = new Color(250, 250, 250, 150); // Adjusted light shadow for new background
    private final Color NEUMORPHIC_DARK_SHADOW = new Color(0, 0, 0, 150);  // Black with transparency
    private final int NEUMORPHIC_SHADOW_OFFSET = 3; // Offset for the shadow effect
    private final int NEUMORPHIC_CORNER_RADIUS = 15; // Radius for rounded corners

    // Animation duration and frame rate
    private final int ANIMATION_DURATION = 150; // milliseconds
    private final int FRAME_RATE = 30; // frames per second

    public GeminiLikeAppResizable() {
        setTitle("Gemini-Like Application (Resizable Sidebar - Home Button Added)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700); // Slightly larger initial size
        setLocationRelativeTo(null); // Center the window

        // 1. Create the Sidebar
        createSidebar();

        // 2. Create the Main Content Area
        createMainContentArea();

        // 3. Use JSplitPane to combine sidebar and main content
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sidebarPanel, mainContentPanel);
        splitPane.setDividerLocation(200); // Initial divider location (sidebar width)
        splitPane.setOneTouchExpandable(true); // Small arrows to expand/collapse sidebar
        splitPane.setDividerSize(8); // Make the divider a bit wider for easier grabbing
        splitPane.setBorder(BorderFactory.createEmptyBorder()); // Remove default border

        add(splitPane, BorderLayout.CENTER); // Add the split pane to the frame

        setVisible(true);
    }

    private void createSidebar() {
        sidebarPanel = new JPanel();
        sidebarPanel.setBackground(SIDEBAR_DEFAULT_COLOR); // Set sidebar default background
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS)); // Vertical stacking

        // Add padding at the top for visual appeal, or a logo/title
        sidebarPanel.add(Box.createVerticalStrut(30)); // Top padding


        // Panel for main navigation items (Home, Image, Audio, Video, Text)
        JPanel navigationItemsPanel = new JPanel();
        navigationItemsPanel.setLayout(new BoxLayout(navigationItemsPanel, BoxLayout.Y_AXIS));
        navigationItemsPanel.setBackground(SIDEBAR_DEFAULT_COLOR); // Set navigation items panel background

        // Add navigation items (including the new Home button)
        addSidebarItem(navigationItemsPanel, "Home", "/icons/home_icon.png"); // New Home button
        addSidebarItem(navigationItemsPanel, "Image", "/icons/image_icon.png");
        addSidebarItem(navigationItemsPanel, "Audio", "/icons/audio_icon.png");
        addSidebarItem(navigationItemsPanel, "Video", "/icons/video_icon.png");
        addSidebarItem(navigationItemsPanel, "Text", "/icons/text_icon.png");

        // Wrap the navigation items in a JScrollPane to handle many items
        JScrollPane navigationScrollPane = new JScrollPane(navigationItemsPanel);
        navigationScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        navigationScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        navigationScrollPane.setBorder(BorderFactory.createEmptyBorder()); // No border for the scroll pane
        navigationScrollPane.getVerticalScrollBar().setUnitIncrement(16); // Make scrolling smoother
        sidebarPanel.add(navigationScrollPane); // Add the scroll pane to the sidebar

        sidebarPanel.add(Box.createVerticalGlue()); // Push items to top, filling available space

        // Add "Information" button at the bottom
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setBackground(SIDEBAR_DEFAULT_COLOR); // Set bottom panel background
        addSidebarItem(bottomPanel, "Information", "/icons/info_icon.png");
        sidebarPanel.add(bottomPanel);
        sidebarPanel.add(Box.createVerticalStrut(10)); // Bottom padding
    }


    /**
     * Helper method to create and add a sidebar item (JPanel with icon and text).
     * Now uses a custom RoundedPanel for neumorphic effect.
     * @param parentPanel The JPanel to which this item will be added (e.g., navigationItemsPanel or bottomPanel).
     * @param text The text label for the sidebar item.
     * @param iconPath The path to the icon image resource (e.g., "/icons/my_icon.png").
     */
    private void addSidebarItem(JPanel parentPanel, String text, String iconPath) {
        // Use a custom RoundedPanel for the button item
        RoundedPanel itemPanel = new RoundedPanel(NEUMORPHIC_CORNER_RADIUS);
        itemPanel.setBackground(SIDEBAR_DEFAULT_COLOR);
        itemPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        itemPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50)); // Increased height for spacing and shadow
        itemPanel.setOpaque(false); // Make it non-opaque so custom paintComponent handles background

        // Set the layout for the icon and text within the RoundedPanel
        itemPanel.setLayout(new GridBagLayout()); // Use GridBagLayout to center the icon-text unit

        // Create a new panel to hold the icon and text together as a single unit
        JPanel iconTextPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0)); // Centered flow, 10px gap between icon and text
        iconTextPanel.setOpaque(false); // Make it transparent so itemPanel's background shows through

        // Add icon to iconTextPanel
        if (iconPath != null && !iconPath.isEmpty()) {
            try {
                ImageIcon icon = new ImageIcon(new ImageIcon(getClass().getResource(iconPath)).getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));
                JLabel iconLabel = new JLabel(icon);
                iconTextPanel.add(iconLabel); // Add icon to the FlowLayout panel
            } catch (Exception e) {
                System.err.println("Error loading icon from resource: " + iconPath +
                        " - Please ensure the 'icons' folder is located under src/main/resources/ in your project. Error: " + e.getMessage());
                iconTextPanel.add(Box.createHorizontalStrut(20)); // Strut for icon space
            }
        } else {
            iconTextPanel.add(Box.createHorizontalStrut(20)); // Strut for icon space
        }

        // Add text label to iconTextPanel
        JLabel textLabel = new JLabel(text);
        textLabel.setForeground(Color.BLACK); // Changed text color to black for white sidebar
        textLabel.setFont(new Font("Arial", Font.BOLD, 18));
        iconTextPanel.add(textLabel); // Add text to the FlowLayout panel

        // Add the iconTextPanel to the itemPanel, centered using GridBagLayout
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0; // Allow it to expand horizontally
        gbc.anchor = GridBagConstraints.CENTER; // Center the iconTextPanel horizontally
        itemPanel.add(iconTextPanel, gbc);

        // Add spacing between buttons by adding an EmptyBorder to the itemPanel
        // Increased top/bottom padding for more spacing between buttons
        itemPanel.setBorder(new EmptyBorder(12, 15, 12, 15)); // Increased top/bottom padding to 12


        // Animation variables (for smooth color transition on hover)
        final Color[] startColor = {SIDEBAR_DEFAULT_COLOR};
        final Color[] endColor = {SIDEBAR_HOVER_COLOR};
        final long[] startTime = {0};
        Timer timer = new Timer(1000 / FRAME_RATE, null);

        // ActionListener for the timer to perform color interpolation
        timer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                long currentTime = System.currentTimeMillis();
                long elapsed = currentTime - startTime[0];

                if (elapsed >= ANIMATION_DURATION) {
                    itemPanel.setBackground(endColor[0]);
                    ((Timer) e.getSource()).stop();
                } else {
                    float fraction = (float) elapsed / ANIMATION_DURATION;
                    Color interpolatedColor = interpolateColor(startColor[0], endColor[0], fraction);
                    itemPanel.setBackground(interpolatedColor);
                }
                itemPanel.repaint(); // Repaint to show color change
            }
        });

        // MouseListener for hover effects and click actions
        itemPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent evt) {
                if (timer.isRunning()) {
                    timer.stop();
                }
                startColor[0] = itemPanel.getBackground();
                endColor[0] = SIDEBAR_HOVER_COLOR;
                startTime[0] = System.currentTimeMillis();
                timer.start();
            }

            @Override
            public void mouseExited(MouseEvent evt) {
                if (timer.isRunning()) {
                    timer.stop();
                }
                startColor[0] = itemPanel.getBackground();
                endColor[0] = SIDEBAR_DEFAULT_COLOR;
                startTime[0] = System.currentTimeMillis();
                timer.start();
            }

            @Override
            public void mouseClicked(MouseEvent evt) {
                System.out.println("Clicked: " + text);
                cardLayout.show(mainContentPanel, text);
            }
        });

        parentPanel.add(itemPanel);
    }

    /**
     * Custom JPanel for drawing rounded rectangles and simulating neumorphic shadows.
     */
    class RoundedPanel extends JPanel {
        private int cornerRadius;

        public RoundedPanel(int radius) {
            this.cornerRadius = radius;
            setOpaque(false); // Make the panel transparent so we can paint our own shape
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();

            // Draw dark shadow (bottom-right)
            g2.setColor(NEUMORPHIC_DARK_SHADOW); // Now black with transparency
            g2.fillRoundRect(NEUMORPHIC_SHADOW_OFFSET, NEUMORPHIC_SHADOW_OFFSET,
                    width - NEUMORPHIC_SHADOW_OFFSET, height - NEUMORPHIC_SHADOW_OFFSET,
                    cornerRadius, cornerRadius);

            // Draw light shadow (top-left)
            g2.setColor(NEUMORPHIC_LIGHT_SHADOW);
            g2.fillRoundRect(0, 0,
                    width - NEUMORPHIC_SHADOW_OFFSET, height - NEUMORPHIC_SHADOW_OFFSET,
                    cornerRadius, cornerRadius);

            // Draw the main rounded rectangle background
            g2.setColor(getBackground()); // Use the panel's current background color
            g2.fillRoundRect(0, 0, width - NEUMORPHIC_SHADOW_OFFSET, height - NEUMORPHIC_SHADOW_OFFSET, cornerRadius, cornerRadius);

            g2.dispose();
        }

        @Override
        public Dimension getPreferredSize() {
            // Adjust preferred size to account for shadows
            Dimension superPref = super.getPreferredSize();
            return new Dimension(superPref.width + NEUMORPHIC_SHADOW_OFFSET, superPref.height + NEUMORPHIC_SHADOW_OFFSET);
        }
    }


    /**
     * Interpolates between two colors based on a given fraction.
     * @param color1 The starting color.
     * @param color2 The ending color.
     * @param fraction The interpolation fraction (0.0 to 1.0).
     * @return The interpolated color.
     */
    private Color interpolateColor(Color color1, Color color2, float fraction) {
        int r = (int) (color1.getRed() + (color2.getRed() - color1.getRed()) * fraction);
        int g = (int) (color1.getGreen() + (color2.getGreen() - color1.getGreen()) * fraction);
        int b = (int) (color1.getBlue() + (color2.getBlue() - color1.getBlue()) * fraction);
        return new Color(r, g, b);
    }


    private void createMainContentArea() {
        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);
        mainContentPanel.setBackground(MAIN_CONTENT_COLOR); // Set main content background to white

        // Add different "cards" (JPanels) for each main section that can be displayed
        mainContentPanel.add(createContentPanel("Welcome to Home!"), "Home"); // Content for Home
        mainContentPanel.add(createContentPanel("Displaying Image Content."), "Image"); // Content for Image
        mainContentPanel.add(createContentPanel("Playing Audio Content."), "Audio");     // Content for Audio
        mainContentPanel.add(createContentPanel("Playing Video Content."), "Video");     // Content for Video
        mainContentPanel.add(createContentPanel("Displaying Text Content."), "Text");     // Content for Text
        mainContentPanel.add(createContentPanel("Information about the application."), "Information");
    }

    /**
     * Helper method to create a simple content panel with centered text.
     * @param contentText The text to display in the content panel.
     * @return A JPanel representing a content view.
     */
    private JPanel createContentPanel(String contentText) {
        JPanel panel = new JPanel(new GridBagLayout()); // Use GridBagLayout to center text
        panel.setBackground(MAIN_CONTENT_COLOR); // Set content panel background to white
        JLabel label = new JLabel(contentText);
        label.setForeground(Color.DARK_GRAY); // Text color
        label.setFont(new Font("Arial", Font.BOLD, 28)); // Font style and size
        panel.add(label); // Add the label to the panel
        return panel;
    }

    public static void main(String[] args) {
        // Ensure GUI updates are done on the Event Dispatch Thread for thread safety
        SwingUtilities.invokeLater(() -> new GeminiLikeAppResizable());
    }
}
