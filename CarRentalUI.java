import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CarRentalUI extends JFrame {

    // Global components and state
    private boolean isLoggedIn = false;
    private JButton bookNow;
    private JButton logInActionBtn, signUpActionBtn; // Buttons in the welcome panel
    private JButton logoutLink; // Button in the navigation bar
    
    private JPanel mainContentCardPanel;
    private CardLayout cardLayout;
    private static final int FORM_PADDING = 100;
    
    // REPLACED: use database instead of in-memory map
    private final UserDAO userDAO = new UserDAO();

    public CarRentalUI() {
        // Initialize DB (create table if not exists)
        userDAO.initializeDatabase();

        setTitle("Car Rental");
        setSize(1100, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // ===== Top Navigation Bar =====
        JPanel navBar = new JPanel(new BorderLayout());
        navBar.setBackground(Color.BLACK);
        navBar.setPreferredSize(new Dimension(getWidth(), 60));

        // Left Logo
        JLabel logo = new JLabel("🚗 Car Rental");
        logo.setForeground(Color.WHITE);
        logo.setFont(new Font("SansSerif", Font.BOLD, 18));
        logo.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 10));
        navBar.add(logo, BorderLayout.WEST);

        // Center Menu (empty but kept for structure)
        JPanel menuPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 10)); 
        menuPanel.setBackground(Color.BLACK);
        navBar.add(menuPanel, BorderLayout.CENTER);

        // Right Panel: Log Out
        logoutLink = new JButton("Log Out"); 
        logoutLink.setFocusPainted(false);
        logoutLink.setForeground(Color.WHITE);
        logoutLink.setBackground(Color.BLACK);
        logoutLink.setFont(new Font("SansSerif", Font.PLAIN, 16));
        logoutLink.setBorder(null);
        logoutLink.addActionListener(e -> logoutUser());
        
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        rightPanel.setOpaque(false);
        rightPanel.add(logoutLink);
        navBar.add(rightPanel, BorderLayout.EAST);
        
        add(navBar, BorderLayout.NORTH);

        // ===== Dynamic Content Area (CardLayout) =====
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                ImageIcon bg = new ImageIcon("ddd.jpeg"); 
                if (bg.getImageLoadStatus() != MediaTracker.COMPLETE) {
                    g.setColor(new Color(30, 30, 30)); 
                    g.fillRect(0, 0, getWidth(), getHeight());
                } else {
                    g.drawImage(bg.getImage(), 0, 0, getWidth(), getHeight(), this);
                }
            }
        };
        mainPanel.setLayout(new BorderLayout());

        cardLayout = new CardLayout();
        mainContentCardPanel = new JPanel(cardLayout);
        mainContentCardPanel.setOpaque(false);

        mainContentCardPanel.add(createWelcomePanel(), "WELCOME");
        mainContentCardPanel.add(createLoginForm(), "LOGIN");
        mainContentCardPanel.add(createSignupForm(), "SIGNUP");
        
        JPanel contentWrapper = new JPanel(new GridBagLayout());
        contentWrapper.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        contentWrapper.add(mainContentCardPanel, gbc);

        mainPanel.add(contentWrapper, BorderLayout.WEST);
        add(mainPanel, BorderLayout.CENTER);

        updateButtonVisibility();
        showView("WELCOME");
        setVisible(true);
    }

    private JPanel createWelcomePanel() {
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false); 
        contentPanel.setBorder(BorderFactory.createEmptyBorder(FORM_PADDING, FORM_PADDING, 0, 0)); 

        JLabel title = new JLabel("Car rental.");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 42));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(title);

        JLabel subtitle = new JLabel("<html>A car rental, hire car, or car hire agency is a company<br>" +
                "that rents automobiles for short periods of time, generally<br>" +
                "ranging from a few hours to a few weeks.</html>");
        subtitle.setForeground(Color.LIGHT_GRAY);
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitle.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(subtitle);

        JPanel actionBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0)); 
        actionBtns.setOpaque(false);
        actionBtns.setAlignmentX(Component.LEFT_ALIGNMENT);

        Font actionFont = new Font("SansSerif", Font.BOLD, 15);
        
        bookNow = new JButton("Book Now");
        bookNow.setBackground(Color.WHITE);
        bookNow.setForeground(Color.BLACK);
        bookNow.setFocusPainted(false);
        bookNow.setFont(actionFont);
        bookNow.addActionListener(e -> {
             new BookingPage(); 
             dispose(); 
        });

        logInActionBtn = new JButton("Log In");
        logInActionBtn.setBackground(new Color(0, 0, 0, 0));
        logInActionBtn.setForeground(Color.WHITE);
        logInActionBtn.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));
        logInActionBtn.setFocusPainted(false);
        logInActionBtn.setFont(actionFont);
        logInActionBtn.addActionListener(e -> showView("LOGIN"));
        
        signUpActionBtn = new JButton("Sign Up");
        signUpActionBtn.setBackground(new Color(0, 0, 0, 0));
        signUpActionBtn.setForeground(Color.WHITE);
        signUpActionBtn.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));
        signUpActionBtn.setFocusPainted(false);
        signUpActionBtn.setFont(actionFont);
        signUpActionBtn.addActionListener(e -> showView("SIGNUP"));
        
        actionBtns.add(bookNow);
        actionBtns.add(logInActionBtn);
        actionBtns.add(signUpActionBtn);
        
        contentPanel.add(actionBtns);
        return contentPanel;
    }
    
    private JPanel createLoginForm() {
        JPanel formPanel = createBaseFormPanel("User Login");

        JTextField usernameField = new JTextField(15);
        JPasswordField passwordField = new JPasswordField(15);
        JButton submitBtn = new JButton("Log In");

        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setOpaque(false);
        GridBagConstraints gbc = createGBC();

        gbc.gridy = 0; inputPanel.add(createLabel("Username:"), gbc);
        gbc.gridy = 1; inputPanel.add(usernameField, gbc);

        gbc.gridy = 2; inputPanel.add(createLabel("Password:"), gbc);
        gbc.gridy = 3; inputPanel.add(passwordField, gbc);

        gbc.gridy = 4; gbc.anchor = GridBagConstraints.CENTER;
        submitBtn.addActionListener(e -> loginUser(usernameField.getText(), new String(passwordField.getPassword())));
        inputPanel.add(submitBtn, gbc);
        
        JButton backBtn = new JButton("← Back");
        backBtn.setBackground(new Color(0, 0, 0, 0));
        backBtn.setForeground(Color.WHITE);
        backBtn.setBorder(null);
        backBtn.addActionListener(e -> showView("WELCOME"));
        gbc.gridy = 5; gbc.anchor = GridBagConstraints.CENTER;
        inputPanel.add(backBtn, gbc);

        formPanel.add(inputPanel, BorderLayout.CENTER);
        return formPanel;
    }

    private JPanel createSignupForm() {
        JPanel formPanel = createBaseFormPanel("New User Sign Up");

        JTextField usernameField = new JTextField(15);
        JPasswordField passwordField = new JPasswordField(15);
        JPasswordField confirmPasswordField = new JPasswordField(15);
        JButton submitBtn = new JButton("Sign Up");

        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setOpaque(false);
        GridBagConstraints gbc = createGBC();

        gbc.gridy = 0; inputPanel.add(createLabel("Username:"), gbc);
        gbc.gridy = 1; inputPanel.add(usernameField, gbc);

        gbc.gridy = 2; inputPanel.add(createLabel("Password:"), gbc);
        gbc.gridy = 3; inputPanel.add(passwordField, gbc);

        gbc.gridy = 4; inputPanel.add(createLabel("Confirm Password:"), gbc);
        gbc.gridy = 5; inputPanel.add(confirmPasswordField, gbc);

        gbc.gridy = 6; gbc.anchor = GridBagConstraints.CENTER;
        submitBtn.addActionListener(e -> signupUser(usernameField.getText(), new String(passwordField.getPassword()), new String(confirmPasswordField.getPassword())));
        inputPanel.add(submitBtn, gbc);
        
        JButton backBtn = new JButton("← Back");
        backBtn.setBackground(new Color(0, 0, 0, 0));
        backBtn.setForeground(Color.WHITE);
        backBtn.setBorder(null);
        backBtn.addActionListener(e -> showView("WELCOME"));
        gbc.gridy = 7; gbc.anchor = GridBagConstraints.CENTER;
        inputPanel.add(backBtn, gbc);

        formPanel.add(inputPanel, BorderLayout.CENTER);
        return formPanel;
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("SansSerif", Font.PLAIN, 14));
        label.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }
    
    private JPanel createBaseFormPanel(String titleText) {
        JPanel formPanel = new JPanel(new BorderLayout());
        formPanel.setOpaque(false);
        formPanel.setBorder(BorderFactory.createEmptyBorder(FORM_PADDING, FORM_PADDING, 0, 0));

        JLabel title = new JLabel(titleText);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 36));
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        formPanel.add(title, BorderLayout.NORTH);
        
        return formPanel;
    }
    
    private GridBagConstraints createGBC() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 0, 5, 0);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        return gbc;
    }

    private void showView(String viewName) {
        cardLayout.show(mainContentCardPanel, viewName);
    }

    private void updateButtonVisibility() {
        if (bookNow != null) {
            bookNow.setVisible(isLoggedIn);
        }
        if (logInActionBtn != null) {
            logInActionBtn.setVisible(!isLoggedIn);
        }
        if (signUpActionBtn != null) {
            signUpActionBtn.setVisible(!isLoggedIn);
        }
        if (logoutLink != null) {
            logoutLink.setVisible(isLoggedIn);
        }
    }

    private void loginUser(String username, String password) {
        if (userDAO.loginUser(username, password)) {
            isLoggedIn = true;
            JOptionPane.showMessageDialog(this, "Login Successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
            updateButtonVisibility();
            showView("WELCOME");
        } else {
            JOptionPane.showMessageDialog(this, "Invalid Username or Password", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void signupUser(String username, String password, String confirmPassword) {
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username and Password cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (userDAO.registerUser(username, password)) {
            JOptionPane.showMessageDialog(this, "Sign Up Successful! Please log in.", "Success", JOptionPane.INFORMATION_MESSAGE);
            showView("LOGIN");
        } else {
            JOptionPane.showMessageDialog(this, "Username already exists or DB error.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void logoutUser() {
        isLoggedIn = false;
        JOptionPane.showMessageDialog(this, "You have been logged out.", "Logout", JOptionPane.INFORMATION_MESSAGE);
        updateButtonVisibility();
        showView("WELCOME");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(CarRentalUI::new);
    }
}

