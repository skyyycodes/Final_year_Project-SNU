package com.snu.project;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.net.URI;
import java.awt.Desktop;
import java.awt.image.BufferedImage; // For loading image
import javax.imageio.ImageIO;     // For ImageIO

public class GeminiLikeAppResizable extends JFrame {

    private JPanel sidebarPanel;
    private JPanel mainContentPanel;
    private CardLayout cardLayout;

    // Define colors for the light theme
    private final Color SIDEBAR_DEFAULT_COLOR = new Color(245, 245, 245);
    private final Color SIDEBAR_HOVER_COLOR = new Color(230, 230, 230);
    private final Color MAIN_CONTENT_COLOR = Color.WHITE;

    // Colors for neumorphic effect
    private final Color NEUMORPHIC_LIGHT_SHADOW = new Color(250, 250, 250, 150);
    private final Color NEUMORPHIC_DARK_SHADOW = new Color(0, 0, 0, 150);
    private final int NEUMORPHIC_SHADOW_OFFSET = 3;
    private final int NEUMORPHIC_CORNER_RADIUS = 15;

    // Animation duration and frame rate
    private final int ANIMATION_DURATION = 150;
    private final int FRAME_RATE = 30;

    public GeminiLikeAppResizable() {
        setTitle("Gemini-Like Application (Resizable Sidebar - Neumorphic Buttons)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        createSidebar();
        createMainContentArea();

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sidebarPanel, mainContentPanel);
        splitPane.setDividerLocation(200);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerSize(8);
        splitPane.setBorder(BorderFactory.createEmptyBorder());

        add(splitPane, BorderLayout.CENTER);

        setVisible(true);
    }

    private void createSidebar() {
        sidebarPanel = new JPanel();
        sidebarPanel.setBackground(SIDEBAR_DEFAULT_COLOR);
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));

        sidebarPanel.add(Box.createVerticalStrut(30));

        JPanel navigationItemsPanel = new JPanel();
        navigationItemsPanel.setLayout(new BoxLayout(navigationItemsPanel, BoxLayout.Y_AXIS));
        navigationItemsPanel.setBackground(SIDEBAR_DEFAULT_COLOR);

        addSidebarItem(navigationItemsPanel, "Home", "/icons/home_icon.png");
        addSidebarItem(navigationItemsPanel, "Image", "/icons/image_icon.png");
        addSidebarItem(navigationItemsPanel, "Audio", "/icons/audio_icon.png");
        addSidebarItem(navigationItemsPanel, "Video", "/icons/video_icon.png");
        addSidebarItem(navigationItemsPanel, "Text", "/icons/text_icon.png");

        JScrollPane navigationScrollPane = new JScrollPane(navigationItemsPanel);
        navigationScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        navigationScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        navigationScrollPane.setBorder(BorderFactory.createEmptyBorder());
        navigationScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        sidebarPanel.add(navigationScrollPane);

        sidebarPanel.add(Box.createVerticalGlue());

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setBackground(SIDEBAR_DEFAULT_COLOR);
        addSidebarItem(bottomPanel, "Information", "/icons/info_icon.png");
        sidebarPanel.add(bottomPanel);
        sidebarPanel.add(Box.createVerticalStrut(10));
    }


    private void addSidebarItem(JPanel parentPanel, String text, String iconPath) {
        RoundedPanel itemPanel = new RoundedPanel(NEUMORPHIC_CORNER_RADIUS);
        itemPanel.setBackground(SIDEBAR_DEFAULT_COLOR);
        itemPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        itemPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        itemPanel.setOpaque(false);

        itemPanel.setLayout(new GridBagLayout());

        JPanel iconTextPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        iconTextPanel.setOpaque(false);

        if (iconPath != null && !iconPath.isEmpty()) {
            try {
                ImageIcon icon = new ImageIcon(new ImageIcon(getClass().getResource(iconPath)).getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));
                JLabel iconLabel = new JLabel(icon);
                iconTextPanel.add(iconLabel);
            } catch (Exception e) {
                System.err.println("Error loading icon from resource: " + iconPath +
                        " - Please ensure the 'icons' folder is located under src/main/resources/ in your project. Error: " + e.getMessage());
                iconTextPanel.add(Box.createHorizontalStrut(20));
            }
        } else {
            iconTextPanel.add(Box.createHorizontalStrut(20));
        }

        JLabel textLabel = new JLabel(text);
        textLabel.setForeground(Color.BLACK);
        textLabel.setFont(new Font("Arial", Font.BOLD, 18));
        iconTextPanel.add(textLabel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        itemPanel.add(iconTextPanel, gbc);

        itemPanel.setBorder(new EmptyBorder(12, 15, 12, 15));

        final Color[] startColor = {SIDEBAR_DEFAULT_COLOR};
        final Color[] endColor = {SIDEBAR_HOVER_COLOR};
        final long[] startTime = {0};
        Timer timer = new Timer(1000 / FRAME_RATE, null);

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
                itemPanel.repaint();
            }
        });

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

    class RoundedPanel extends JPanel {
        private int cornerRadius;

        public RoundedPanel(int radius) {
            this.cornerRadius = radius;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();

            g2.setColor(NEUMORPHIC_DARK_SHADOW);
            g2.fillRoundRect(NEUMORPHIC_SHADOW_OFFSET, NEUMORPHIC_SHADOW_OFFSET,
                    width - NEUMORPHIC_SHADOW_OFFSET, height - NEUMORPHIC_SHADOW_OFFSET,
                    cornerRadius, cornerRadius);

            g2.setColor(NEUMORPHIC_LIGHT_SHADOW);
            g2.fillRoundRect(0, 0,
                    width - NEUMORPHIC_SHADOW_OFFSET, height - NEUMORPHIC_SHADOW_OFFSET,
                    cornerRadius, cornerRadius);

            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, width - NEUMORPHIC_SHADOW_OFFSET, height - NEUMORPHIC_SHADOW_OFFSET, cornerRadius, cornerRadius);

            g2.dispose();
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension superPref = super.getPreferredSize();
            return new Dimension(superPref.width + NEUMORPHIC_SHADOW_OFFSET, superPref.height + NEUMORPHIC_SHADOW_OFFSET);
        }
    }

    // Custom JPanel for background image
    class BackgroundPanel extends JPanel {
        private Image backgroundImage;

        public BackgroundPanel(String imagePath) {
            try {
                // Load image from resources
                backgroundImage = ImageIO.read(getClass().getResource(imagePath));
            } catch (Exception e) {
                System.err.println("Error loading background image: " + imagePath + " - " + e.getMessage());
                // Set to null or a default image if loading fails
                backgroundImage = null;
            }
            // Ensure child components are transparent so background shows through
            setOpaque(false); // Make this panel transparent to its parent (if it had one, but it draws its own background)
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (backgroundImage != null) {
                // Draw the image scaled to fill the panel
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            }
        }
    }


    private Color interpolateColor(Color color1, Color color2, float fraction) {
        int r = (int) (color1.getRed() + (color2.getRed() - color1.getRed()) * fraction);
        int g = (int) (color1.getGreen() + (color2.getGreen() - color1.getGreen()) * fraction);
        int b = (int) (color1.getBlue() + (color2.getBlue() - color1.getBlue()) * fraction);
        return new Color(r, g, b);
    }

    private void createMainContentArea() {
        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);
        // The mainContentPanel itself holds the homePanel, so its background isn't strictly needed for the image,
        // but it's good to keep it set for other cards.
        mainContentPanel.setBackground(MAIN_CONTENT_COLOR);

        mainContentPanel.add(createHomePagePanel(), "Home");
        mainContentPanel.add(createContentPanel("Displaying Image Content."), "Image");
        mainContentPanel.add(createContentPanel("Playing Audio Content."), "Audio");
        mainContentPanel.add(createContentPanel("Playing Video Content."), "Video");
        mainContentPanel.add(createContentPanel("Displaying Text Content."), "Text");
        mainContentPanel.add(createContentPanel("Information about the application."), "Information");
    }

    /**
     * Creates the custom Home page panel with a background image.
     * @return A JPanel representing the Home page.
     */
    private JPanel createHomePagePanel() {
        // Use the custom BackgroundPanel for the home page
        JPanel homePanel = new BackgroundPanel("/icons/homepage_background.jpg"); // Specify your image path here
        homePanel.setLayout(new GridBagLayout()); // Keep the GridBagLayout for component arrangement

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 5, 10, 5);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.CENTER;

        JLabel combinedLogoLabel = new JLabel();
        try {
            ImageIcon combinedIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/combined_university_logo.png")).getImage().getScaledInstance(250, 65, Image.SCALE_SMOOTH));
            combinedLogoLabel.setIcon(combinedIcon);
        } catch (Exception e) {
            System.err.println("Error loading combined logo from resource: /icons/combined_university_logo.png - " +
                    "Please ensure you have created this single image file and placed it in src/main/resources/icons/. Error: " + e.getMessage());
        }
        gbc.gridy = 0;
        gbc.insets = new Insets(20, 5, 10, 5);
        homePanel.add(combinedLogoLabel, gbc);

        JLabel mainTitleLabel = new JLabel("MULTIMEDIA ENCRYPTION AND COPYRIGHT");
        mainTitleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        mainTitleLabel.setForeground(Color.BLACK); // Adjust color if background image is dark
        mainTitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        // Ensure this label is transparent to show background image
        mainTitleLabel.setOpaque(false);
        gbc.gridy = 1;
        gbc.insets = new Insets(30, 5, 5, 5);
        homePanel.add(mainTitleLabel, gbc);

        JLabel mainTitleLabel2 = new JLabel("PROTECTION");
        mainTitleLabel2.setFont(new Font("Arial", Font.BOLD, 28));
        mainTitleLabel2.setForeground(Color.BLACK); // Adjust color if background image is dark
        mainTitleLabel2.setHorizontalAlignment(SwingConstants.CENTER);
        mainTitleLabel2.setOpaque(false);
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 5, 30, 5);
        homePanel.add(mainTitleLabel2, gbc);

        JLabel deptLabel = new JLabel("DEPARTMENT OF COMPUTER SCIENCE");
        deptLabel.setFont(new Font("Arial", Font.BOLD | Font.ITALIC, 20));
        deptLabel.setForeground(Color.BLACK); // Adjust color if background image is dark
        deptLabel.setHorizontalAlignment(SwingConstants.CENTER);
        deptLabel.setOpaque(false);
        gbc.gridy = 3;
        gbc.insets = new Insets(10, 5, 0, 5);
        homePanel.add(deptLabel, gbc);

        JLabel deptLabel2 = new JLabel("AND ENGINEERING");
        deptLabel2.setFont(new Font("Arial", Font.BOLD | Font.ITALIC, 20));
        deptLabel2.setForeground(Color.BLACK); // Adjust color if background image is dark
        deptLabel2.setHorizontalAlignment(SwingConstants.CENTER);
        deptLabel2.setOpaque(false);
        gbc.gridy = 4;
        gbc.insets = new Insets(0, 5, 40, 5);
        homePanel.add(deptLabel2, gbc);

        JLabel developedByLabel = new JLabel("DEVELOPED BY :");
        developedByLabel.setFont(new Font("Arial", Font.BOLD, 18));
        developedByLabel.setForeground(Color.BLACK); // Adjust color if background image is dark
        developedByLabel.setHorizontalAlignment(SwingConstants.CENTER);
        developedByLabel.setOpaque(false);
        gbc.gridy = 5;
        gbc.insets = new Insets(10, 5, 5, 5);
        homePanel.add(developedByLabel, gbc);

        String[] names = {
                "AKASH CHAKRABORTY",
                "SANJUKTA SARKAR",
                "ABHIRUP SAMADDER",
                "APARAJITO RAY CHAUDHURI"
        };
        String[] urls = {
                "https://www.linkedin.com/in/skyycodes/",
                "https://www.linkedin.com/in/sanjukta-sarkar-2a2533255/",
                "https://github.com/abhirup-coregit",
                "https://github.com/apara1807"
        };


        for (int i = 0; i < names.length; i++) {
            final String nameText = names[i];
            final String url = urls[i];

            JLabel nameLabel = new JLabel("<html><u>" + nameText + "</u></html>");
            nameLabel.setFont(new Font("Arial", Font.PLAIN, 16));
            nameLabel.setForeground(Color.BLUE); // Adjust color if background image is dark
            nameLabel.setHorizontalAlignment(SwingConstants.CENTER);
            nameLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
            nameLabel.setOpaque(false); // Ensure name labels are transparent

            nameLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    try {
                        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                            Desktop.getDesktop().browse(new URI(url));
                        } else {
                            System.err.println("Desktop or BROWSE action not supported on this system.");
                        }
                    } catch (Exception ex) {
                        System.err.println("Error opening URL: " + url + " - " + ex.getMessage());
                    }
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    nameLabel.setForeground(new Color(0, 0, 150));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    nameLabel.setForeground(Color.BLUE);
                }
            });

            gbc.gridy = 6 + i;
            gbc.insets = new Insets(2, 5, 2, 5);
            homePanel.add(nameLabel, gbc);
        }

        return homePanel;
    }

    private JPanel createContentPanel(String contentText) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(MAIN_CONTENT_COLOR);
        JLabel label = new JLabel(contentText);
        label.setForeground(Color.DARK_GRAY);
        label.setFont(new Font("Arial", Font.BOLD, 28));
        panel.add(label);
        return panel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GeminiLikeAppResizable());
    }
}