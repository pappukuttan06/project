// BookingPage.java
import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

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
    private JLabel priceLabel;

    private final SimpleDateFormat dbDateFormat =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    // MODIFIED: Static map to hold DB data
    private static Map<String, Integer> carPrices = new LinkedHashMap<>();
    
    // NEW: DAO instance to load data from DB
    private static final CarDAO carDAO = new CarDAO();


    public BookingPage() {
        setTitle("Booking");

        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        ImageIcon bgIcon = new ImageIcon("ddd1.jpeg"); 
        Image bgImage = bgIcon.getImage();

        JPanel background = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
            }
        };
        setContentPane(background); 

        JLabel title = new JLabel("Book your perfect car", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 32));
        title.setForeground(Color.WHITE);
        background.add(title, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        background.add(centerPanel, BorderLayout.CENTER);


        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false); 
        form.setBackground(new Color(255, 255, 255, 50)); 
        form.setBorder(BorderFactory.createEmptyBorder(16, 24, 24, 24));
        
        GridBagConstraints formConstraints = new GridBagConstraints();
        formConstraints.anchor = GridBagConstraints.EAST; 
        formConstraints.weightx = 1.0; 
        centerPanel.add(form, formConstraints);


        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(8, 8, 8, 8);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 0.5;

        // Row 0: Name, Email
        gc.gridx = 0; gc.gridy = 0;
        JLabel nameLbl = new JLabel("Name:");
        nameLbl.setForeground(Color.WHITE);
        form.add(nameLbl, gc);
        gc.gridx = 1;
        nameField = new JTextField(15);
        form.add(nameField, gc);

        gc.gridx = 2;
        JLabel emailLbl = new JLabel("Email:");
        emailLbl.setForeground(Color.WHITE);
        form.add(emailLbl, gc);
        gc.gridx = 3;
        emailField = new JTextField(15);
        form.add(emailField, gc);

        // Row 1: Phone
        gc.gridx = 0; gc.gridy = 1;
        JLabel phoneLbl = new JLabel("Phone No:");
        phoneLbl.setForeground(Color.WHITE);
        form.add(phoneLbl, gc);
        gc.gridx = 1; gc.gridwidth = 3;
        phoneField = new JTextField(30);
        form.add(phoneField, gc);
        gc.gridwidth = 1;

        // Row 2: Address
        gc.gridx = 0; gc.gridy = 2;
        JLabel addrLbl = new JLabel("Address:");
        addrLbl.setForeground(Color.WHITE);
        form.add(addrLbl, gc);
        gc.gridx = 1; gc.gridwidth = 3;
        addressArea = new JTextArea(3, 30);
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
        pickupLocField = new JTextField(15);
        form.add(pickupLocField, gc);

        gc.gridx = 2;
        JLabel dlocLbl = new JLabel("Drop-off Location:");
        dlocLbl.setForeground(Color.WHITE);
        form.add(dlocLbl, gc);
        gc.gridx = 3;
        dropoffLocField = new JTextField(15);
        form.add(dropoffLocField, gc);

        // Row 5: Car selection + Price
        gc.gridx = 0; gc.gridy = 5;
        JLabel carLbl = new JLabel("Select Car:");
        carLbl.setForeground(Color.WHITE);
        form.add(carLbl, gc);
        gc.gridx = 1;
        
        carCombo = new JComboBox<>();
        form.add(carCombo, gc);

        gc.gridx = 2; gc.gridwidth = 2;
        priceLabel = new JLabel("Price: $0", SwingConstants.LEFT);
        priceLabel.setFont(new Font("Arial", Font.BOLD, 14));
        priceLabel.setForeground(Color.WHITE);
        form.add(priceLabel, gc);
        gc.gridwidth = 1;

        // Bottom: Submit button
        gc.gridx = 0; gc.gridy = 6; gc.gridwidth = 4;
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setOpaque(false);
        JButton submitBtn = new JButton("Submit Booking");
        bottom.add(submitBtn);
        form.add(bottom, gc);
        gc.gridwidth = 1;


        // Listeners
        carCombo.addActionListener(e -> updateCarPreview());
        pickupDateSpinner.addChangeListener(e -> updateCarPreview());
        dropDateSpinner.addChangeListener(e -> updateCarPreview());
        submitBtn.addActionListener(e -> submitBooking());

        // FIX: Load data from the DB and populate the JComboBox
        loadCarPricesFromDB(); 
        repopulateCarCombo(); // Must call this to populate the dropdown
        updateCarPreview();
        
        this.setVisible(true);
    }
    
    /**
     * Loads car data from the database into the static carPrices map.
     * Called when the BookingPage opens and by AdminFrame after modifications.
     */
    public static void loadCarPricesFromDB() {
        carPrices = carDAO.loadAllCars();
        // The carDAO constructor ensures the table is created and populated with defaults
        // System.out.println("Car list reloaded from DB. Total cars: " + carPrices.size());
    }

    /**
     * Repopulates the JComboBox with the current cars from the static carPrices map.
     */
    private void repopulateCarCombo() {
        carCombo.removeAllItems();
        for (String carModel : carPrices.keySet()) {
            carCombo.addItem(carModel);
        }
        if (carCombo.getItemCount() > 0) {
            carCombo.setSelectedIndex(0);
        }
    }


    private void updateCarPreview() {
        if (carPrices.isEmpty() || carCombo.getSelectedItem() == null) {
            priceLabel.setText("Price: $0 (No Cars Available)");
            return;
        }
        
        String car = (String) carCombo.getSelectedItem();
        int dailyRent = carPrices.getOrDefault(car, 0);
        java.util.Date pickup = (java.util.Date) pickupDateSpinner.getValue();
        java.util.Date drop = (java.util.Date) dropDateSpinner.getValue();

        if (drop.before(pickup) || drop.equals(pickup)) {
            priceLabel.setText("Price: $0 (Invalid Dates)");
            return;
        }

        long diffMs = drop.getTime() - pickup.getTime();
        long diffDays = diffMs / (1000 * 60 * 60 * 24);
        long days = Math.max(1, diffDays);
        
        if (diffMs > 0 && diffDays == 0) {
            days = 1;
        }

        int totalPrice = dailyRent * (int) days;

        priceLabel.setText("Price: $" + totalPrice);
    }

    /**
     * Handles the booking submission, saves to DB, and immediately opens the PaymentPage.
     */
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
        
        int dailyRent = carPrices.getOrDefault(car, 0);
        
        if (!drop.after(pickup)) {
            JOptionPane.showMessageDialog(this,
                    "Drop date must be after pickup date.",
                    "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        long diffMs = drop.getTime() - pickup.getTime();
        long diffDays = diffMs / (1000 * 60 * 60 * 24);
        long days = Math.max(1, diffDays);
        
        if (diffMs > 0 && diffDays == 0) {
            days = 1;
        }

        int totalPrice = dailyRent * (int) days;


        if (name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please fill name, email and phone.",
                    "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        
        String sql = "INSERT INTO bookings "
                + "(name,email,phone,address,car,pickup_date,drop_date,pickup_location,dropoff_location,price) "
                + "VALUES (?,?,?,?,?,?,?,?,?,?)"; 
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
            ps.setInt(10, totalPrice); 

            ps.executeUpdate();
            System.out.println("Booking saved to DB. Opening Payment Page.");
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Database error: " + ex.getMessage(),
                    "DB Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // --- CHANGE START: Transition directly to Payment Page ---
        // Removed success JOptionPane

        // Instantiate PaymentPage with all required details, including price
        PaymentPage payment = new PaymentPage(
            name, email, phone, car, pickup, drop,
            pickupLoc, dropoffLoc, totalPrice
        );
        payment.setVisible(true);
        this.dispose(); // Close the BookingPage window
        // --- CHANGE END ---
    }

    // clearForm() is now unnecessary after submit as the page is disposed, 
    // but kept here for completeness in case it was used elsewhere.
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
        
        if (carCombo.getItemCount() > 0) {
            carCombo.setSelectedIndex(0);
        }
        updateCarPreview();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new BookingPage().setVisible(true));
    }
}
