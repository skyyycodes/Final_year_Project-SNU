import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class GeminiLikeAppResizable extends JFrame {

    private JPanel sidebarPanel;
    private JPanel mainContentPanel;
    private CardLayout cardLayout; // For switching content

    public GeminiLikeAppResizable() {
        setTitle("Gemini-Like Application (Resizable Sidebar)");
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
        sidebarPanel.setBackground(new Color(40, 44, 52)); // Dark background for sidebar
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS)); // Vertical stacking

        // Add padding at the top for visual appeal, or a logo/title
        sidebarPanel.add(Box.createVerticalStrut(10)); // Top padding

        // Add "New Chat" button/item at the top
        // For the "New Chat" button at the very top, you might want it separate from the scrollable list.
        JPanel newChatButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        newChatButtonPanel.setBackground(new Color(40, 44, 52));
        JButton newChatButton = new JButton("New Chat");
        newChatButton.setFocusPainted(false); // Remove focus border
        newChatButton.setBackground(new Color(0, 120, 215)); // A distinct color
        newChatButton.setForeground(Color.WHITE);
        newChatButton.setFont(new Font("Arial", Font.BOLD, 14));
        newChatButton.setPreferredSize(new Dimension(160, 35)); // Fixed size
        newChatButton.addActionListener(e -> {
            System.out.println("New Chat button clicked!");
            cardLayout.show(mainContentPanel, "New Chat");
        });
        newChatButtonPanel.add(newChatButton);
        sidebarPanel.add(newChatButtonPanel);
        sidebarPanel.add(Box.createVerticalStrut(20)); // Space after new chat button


        // Panel for main navigation items (Chat, Explore, Activity)
        JPanel navigationItemsPanel = new JPanel();
        navigationItemsPanel.setLayout(new BoxLayout(navigationItemsPanel, BoxLayout.Y_AXIS));
        navigationItemsPanel.setBackground(new Color(40, 44, 52));

        addSidebarItem(navigationItemsPanel, "Explore", "path/to/explore_icon.png");
        addSidebarItem(navigationItemsPanel, "Activity", "path/to/activity_icon.png");
        // Add more items as needed

        // Wrap the navigation items in a JScrollPane
        JScrollPane navigationScrollPane = new JScrollPane(navigationItemsPanel);
        navigationScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        navigationScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        navigationScrollPane.setBorder(BorderFactory.createEmptyBorder()); // No border
        navigationScrollPane.getVerticalScrollBar().setUnitIncrement(16); // Make scrolling smoother
        sidebarPanel.add(navigationScrollPane); // Add the scroll pane to the sidebar

        sidebarPanel.add(Box.createVerticalGlue()); // Push items to top

        // Add "Help & FAQ" and "Settings" at the bottom
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setBackground(new Color(40, 44, 52));
        addSidebarItem(bottomPanel, "Help & FAQ", "path/to/help_icon.png");
        addSidebarItem(bottomPanel, "Settings", "path/to/settings_icon.png");
        sidebarPanel.add(bottomPanel);
        sidebarPanel.add(Box.createVerticalStrut(10)); // Bottom padding
    }


    private void addSidebarItem(JPanel parentPanel, String text, String iconPath) {
        JPanel itemPanel = new JPanel();
        itemPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 10)); // Left alignment, padding
        itemPanel.setBackground(new Color(40, 44, 52));
        itemPanel.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Hand cursor on hover
        itemPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40)); // Prevent items from growing too tall horizontally

        // Add icon (if path is provided)
        if (iconPath != null && !iconPath.isEmpty()) {
            try {
                // Dummy path for icons, replace with actual paths
                ImageIcon icon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/placeholder.png")).getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));
                // If using actual paths:
                // ImageIcon icon = new ImageIcon(new ImageIcon(iconPath).getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));
                JLabel iconLabel = new JLabel(icon);
                itemPanel.add(iconLabel);
            } catch (Exception e) {
                System.err.println("Error loading icon or icon path is dummy: " + iconPath + " - Using placeholder if available.");
                // Fallback: If you have a default placeholder icon in your resources
                try {
                    ImageIcon placeholderIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/placeholder.png")).getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));
                    JLabel iconLabel = new JLabel(placeholderIcon);
                    itemPanel.add(iconLabel);
                } catch (Exception ex) {
                    System.err.println("Placeholder icon also not found: " + ex.getMessage());
                }
            }
        }

        // Add text
        JLabel textLabel = new JLabel(text);
        textLabel.setForeground(Color.WHITE);
        textLabel.setFont(new Font("Arial", Font.PLAIN, 15));
        itemPanel.add(textLabel);

        // Add mouse listener for hover effects and click actions
        itemPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent evt) {
                itemPanel.setBackground(new Color(60, 64, 72)); // Lighter on hover
            }

            @Override
            public void mouseExited(MouseEvent evt) {
                itemPanel.setBackground(new Color(40, 44, 52)); // Back to original
            }

            @Override
            public void mouseClicked(MouseEvent evt) {
                System.out.println("Clicked: " + text);
                cardLayout.show(mainContentPanel, text); // Switch card
            }
        });

        parentPanel.add(itemPanel);
    }


    private void createMainContentArea() {
        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);
        mainContentPanel.setBackground(new Color(55, 59, 66)); // A slightly lighter dark for content

        // Add different "cards" (JPanels) for each main section
        mainContentPanel.add(createContentPanel("Welcome to New Chat!"), "New Chat");
        mainContentPanel.add(createContentPanel("Explore various topics."), "Explore");
        mainContentPanel.add(createContentPanel("Your recent activities."), "Activity");
        mainContentPanel.add(createContentPanel("Help and frequently asked questions."), "Help & FAQ");
        mainContentPanel.add(createContentPanel("Application settings."), "Settings");
    }

    private JPanel createContentPanel(String contentText) {
        JPanel panel = new JPanel(new GridBagLayout()); // Use GridBagLayout to center text
        panel.setBackground(new Color(55, 59, 66));
        JLabel label = new JLabel(contentText);
        label.setForeground(Color.LIGHT_GRAY);
        label.setFont(new Font("Arial", Font.BOLD, 28)); // Slightly larger font for content
        panel.add(label);
        return panel;
    }

    public static void main(String[] args) {
        // Ensure GUI updates are done on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> new GeminiLikeAppResizable());
    }
}