import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * BookingPage - Booking form JFrame
 * Saves each booking into MySQL table: bookings
 */
public class BookingPage extends JFrame {

    private JTextField nameField, emailField, phoneField;
    private JTextArea addressArea;
    private JSpinner pickupDateSpinner, dropDateSpinner;
    private JTextField pickupLocField, dropoffLocField;
    private JComboBox<String> carCombo;
    private JPanel carPreviewPanel;
    private JLabel priceLabel;   // to show price

    private final SimpleDateFormat dbDateFormat =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    // Car daily rents
    private final Map<String, Integer> carPrices = Map.of(
            "Mercedes C-Class", 900,
            "BMW 7 Series", 850,
            "Audi A8", 940,
            "Toyota Alphard", 900,
            "Range Rover", 1000,
            "Lexus LS", 999,
            "Mercedes E-Class", 1000,
            "Land Rover Defender", 1200
    );

    public BookingPage() {
        setTitle("Booking");

        // ---- Full screen ----
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // ---- Background Wallpaper (auto-fit to screen) ----
        ImageIcon bgIcon = new ImageIcon("ddd.jpeg"); // put your wallpaper here
        Image bgImage = bgIcon.getImage();

        JPanel background = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
            }
        };
        setContentPane(background);  // set wallpaper as base

        // ----- Header Title -----
        JLabel title = new JLabel("Book your perfect car", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 32));
        title.setForeground(Color.WHITE);
        background.add(title, BorderLayout.NORTH);

        // ----- Form area -----
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false); // transparent to show wallpaper
        form.setBorder(BorderFactory.createEmptyBorder(16, 24, 24, 24));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(8, 8, 8, 8);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;

        // Row 0: Name, Email
        gc.gridx = 0; gc.gridy = 0;
        JLabel nameLbl = new JLabel("Name:");
        nameLbl.setForeground(Color.WHITE);
        form.add(nameLbl, gc);
        gc.gridx = 1; gc.weightx = 0.5;
        nameField = new JTextField();
        form.add(nameField, gc);

        gc.gridx = 2;
        JLabel emailLbl = new JLabel("Email:");
        emailLbl.setForeground(Color.WHITE);
        form.add(emailLbl, gc);
        gc.gridx = 3;
        emailField = new JTextField();
        form.add(emailField, gc);

        // Row 1: Phone
        gc.gridx = 0; gc.gridy = 1;
        JLabel phoneLbl = new JLabel("Phone No:");
        phoneLbl.setForeground(Color.WHITE);
        form.add(phoneLbl, gc);
        gc.gridx = 1; gc.gridwidth = 3;
        phoneField = new JTextField();
        form.add(phoneField, gc);
        gc.gridwidth = 1;

        // Row 2: Address
        gc.gridx = 0; gc.gridy = 2;
        JLabel addrLbl = new JLabel("Address:");
        addrLbl.setForeground(Color.WHITE);
        form.add(addrLbl, gc);
        gc.gridx = 1; gc.gridwidth = 3;
        addressArea = new JTextArea(3, 20);
        JScrollPane sp = new JScrollPane(addressArea);
        form.add(sp, gc);
        gc.gridwidth = 1;

        // Row 3: Pickup Date & Drop Date
        java.util.Date now = new java.util.Date();

        SpinnerDateModel pickModel =
                new SpinnerDateModel(now, null, null, Calendar.MINUTE);
        pickupDateSpinner = new JSpinner(pickModel);
        pickupDateSpinner.setEditor(
                new JSpinner.DateEditor(pickupDateSpinner, "yyyy-MM-dd HH:mm"));

        SpinnerDateModel dropModel = new SpinnerDateModel(
                new java.util.Date(now.getTime() + 3600 * 1000),
                null, null, Calendar.MINUTE);
        dropDateSpinner = new JSpinner(dropModel);
        dropDateSpinner.setEditor(
                new JSpinner.DateEditor(dropDateSpinner, "yyyy-MM-dd HH:mm"));

        gc.gridx = 0; gc.gridy = 3;
        JLabel pickLbl = new JLabel("Pickup Date:");
        pickLbl.setForeground(Color.WHITE);
        form.add(pickLbl, gc);
        gc.gridx = 1;
        form.add(pickupDateSpinner, gc);

        gc.gridx = 2;
        JLabel dropLbl = new JLabel("Drop Date:");
        dropLbl.setForeground(Color.WHITE);
        form.add(dropLbl, gc);
        gc.gridx = 3;
        form.add(dropDateSpinner, gc);

        // Row 4: Pickup/Dropoff Location
        gc.gridx = 0; gc.gridy = 4;
        JLabel plocLbl = new JLabel("Pickup Location:");
        plocLbl.setForeground(Color.WHITE);
        form.add(plocLbl, gc);
        gc.gridx = 1;
        pickupLocField = new JTextField();
        form.add(pickupLocField, gc);

        gc.gridx = 2;
        JLabel dlocLbl = new JLabel("Drop-off Location:");
        dlocLbl.setForeground(Color.WHITE);
        form.add(dlocLbl, gc);
        gc.gridx = 3;
        dropoffLocField = new JTextField();
        form.add(dropoffLocField, gc);

        // Row 5: Car selection + preview
        gc.gridx = 0; gc.gridy = 5;
        JLabel carLbl = new JLabel("Select Car:");
        carLbl.setForeground(Color.WHITE);
        form.add(carLbl, gc);
        gc.gridx = 1;
        String[] cars = {"Mercedes C-Class", "BMW 7 Series", "Audi A8",
                "Toyota Alphard", "Range Rover", "Lexus LS", "Mercedes E-Class", "Land Rover Defender"};
        carCombo = new JComboBox<>(cars);
        form.add(carCombo, gc);

        gc.gridx = 2; gc.gridwidth = 2;
        carPreviewPanel = new JPanel();
        carPreviewPanel.setPreferredSize(new Dimension(400, 250));
        carPreviewPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        carPreviewPanel.setLayout(new BorderLayout());
        carPreviewPanel.setOpaque(false);

        // Price label under car preview
        priceLabel = new JLabel("Price: $0", SwingConstants.CENTER);
        priceLabel.setFont(new Font("Arial", Font.BOLD, 14));
        priceLabel.setForeground(Color.WHITE);

        carPreviewPanel.add(priceLabel, BorderLayout.SOUTH);
        form.add(carPreviewPanel, gc);
        gc.gridwidth = 1;

        // Bottom: Submit button
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setOpaque(false);
        JButton submitBtn = new JButton("Submit Booking");
        bottom.add(submitBtn);

        background.add(form, BorderLayout.CENTER);
        background.add(bottom, BorderLayout.SOUTH);

        // Listeners
        carCombo.addActionListener(e -> updateCarPreview());
        pickupDateSpinner.addChangeListener(e -> updateCarPreview());
        dropDateSpinner.addChangeListener(e -> updateCarPreview());
        submitBtn.addActionListener(e -> submitBooking());

        updateCarPreview();
        
        // FIX: Make the frame visible when instantiated by CarRentalUI
        this.setVisible(true);
    }

    // Show preview + price
    private void updateCarPreview() {
        String car = (String) carCombo.getSelectedItem();

        // Car images
        Map<String, String> map = new HashMap<>();
        map.put("Mercedes C-Class", "images/mercedes.jpg");
        map.put("BMW 7 Series", "images/7series.jpg");
        map.put("Audi A8", "images/audi.jpg");
        map.put("Toyota Alphard", "images/toyota.jpg");
        map.put("Range Rover", "images/range.jpg");
        map.put("Lexus LS", "images/lexus.jpg");
        map.put("Mercedes E-Class", "images/Eclass.jpg");
        map.put("Land Rover Defender", "images/Land Rover Defender.jpg");

        String fn = map.getOrDefault(car, null);

        carPreviewPanel.removeAll();
        carPreviewPanel.setLayout(new BorderLayout());

        if (fn != null) {
            ImageIcon icon = new ImageIcon(fn);
            if (icon.getIconWidth() > 0) {
                int boxW = 400, boxH = 200;
                int imgW = icon.getIconWidth();
                int imgH = icon.getIconHeight();
                double scale = Math.min((double) boxW / imgW, (double) boxH / imgH);

                int newW = (int) (imgW * scale);
                int newH = (int) (imgH * scale);

                Image img = icon.getImage().getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
                JLabel imgLabel = new JLabel(new ImageIcon(img), SwingConstants.CENTER);

                JLabel nameLabel = new JLabel(car, SwingConstants.CENTER);
                nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
                nameLabel.setForeground(Color.WHITE);

                carPreviewPanel.add(nameLabel, BorderLayout.NORTH);
                carPreviewPanel.add(imgLabel, BorderLayout.CENTER);
            }
        }

        int dailyRent = carPrices.getOrDefault(car, 0);
        java.util.Date pickup = (java.util.Date) pickupDateSpinner.getValue();
        java.util.Date drop = (java.util.Date) dropDateSpinner.getValue();

        long diff = Math.max(1,
                (drop.getTime() - pickup.getTime()) / (1000 * 60 * 60 * 24));
        int totalPrice = dailyRent * (int) diff;

        priceLabel.setText("Price: $" + totalPrice);
        carPreviewPanel.add(priceLabel, BorderLayout.SOUTH);

        carPreviewPanel.revalidate();
        carPreviewPanel.repaint();
    }

    // Save booking to DB and open PaymentPage
    private void submitBooking() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String address = addressArea.getText().trim();
        java.util.Date pickup = (java.util.Date) pickupDateSpinner.getValue();
        java.util.Date drop = (java.util.Date) dropDateSpinner.getValue();
        String pickupLoc = pickupLocField.getText().trim();
        String dropoffLoc = dropoffLocField.getText().trim();
        String car = (String) carCombo.getSelectedItem();
        
        // 1. Calculate the price
        int dailyRent = carPrices.getOrDefault(car, 0);
        long diff = Math.max(1,
                (drop.getTime() - pickup.getTime()) / (1000 * 60 * 60 * 24));
        int totalPrice = dailyRent * (int) diff;


        if (name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please fill name, email and phone.",
                    "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!drop.after(pickup)) {
            JOptionPane.showMessageDialog(this,
                    "Drop date must be after pickup date.",
                    "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // 2. Update SQL query to include 'price' column (total 10 columns)
        // NOTE: Ensure your MySQL table 'bookings' has a 'price' column of type INT or DECIMAL.
        String sql = "INSERT INTO bookings "
                + "(name,email,phone,address,car,pickup_date,drop_date,pickup_location,dropoff_location,price) "
                + "VALUES (?,?,?,?,?,?,?,?,?,?)"; // 10 placeholders
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, phone);
            ps.setString(4, address);
            ps.setString(5, car);
            ps.setTimestamp(6, new java.sql.Timestamp(pickup.getTime()));
            ps.setTimestamp(7, new java.sql.Timestamp(drop.getTime()));
            ps.setString(8, pickupLoc);
            ps.setString(9, dropoffLoc);
            // 3. Bind the calculated price to the 10th placeholder
            ps.setInt(10, totalPrice); 

            ps.executeUpdate();
            System.out.println("Booking saved to DB");
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Database error: " + ex.getMessage(),
                    "DB Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(this,
                "Booking saved successfully!",
                "Success", JOptionPane.INFORMATION_MESSAGE);

        // MODIFIED: Added totalPrice to the PaymentPage constructor call
        PaymentPage payment = new PaymentPage(
                name, email, phone, car, pickup, drop,
                pickupLoc, dropoffLoc, totalPrice // <--- NEW ARGUMENT
        );
        payment.setVisible(true);
        this.dispose();
        clearForm();
    }

    private void clearForm() {
        nameField.setText("");
        emailField.setText("");
        phoneField.setText("");
        addressArea.setText("");
        pickupDateSpinner.setValue(new java.util.Date());
        dropDateSpinner.setValue(
                new java.util.Date(System.currentTimeMillis() + 3600 * 1000));
        pickupLocField.setText("");
        dropoffLocField.setText("");
        carCombo.setSelectedIndex(0);
        updateCarPreview();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new BookingPage().setVisible(true));
    }
}