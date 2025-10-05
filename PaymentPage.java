import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Date;

public class PaymentPage extends JFrame {

    private JComboBox<String> paymentMethod;
    private JPanel form;
    private JTextField cardNumber, cardName, expiry, cvv, upiId;

    // NEW: Fields to store booking details for the receipt
    private final String name, email, phone, car, pickupLoc, dropoffLoc;
    private final Date pickup, drop;
    private final int price; // <--- NEW FIELD

    public PaymentPage(String name, String email, String phone,
                       String car, Date pickup, Date drop,
                       String pickupLoc, String dropoffLoc, int price) { // <--- MODIFIED: Added price

        // NEW: Assign all parameters to fields
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.car = car;
        this.pickup = pickup;
        this.drop = drop;
        this.pickupLoc = pickupLoc;
        this.dropoffLoc = dropoffLoc;
        this.price = price; // <--- NEW: Store price

        setTitle("Payment Page");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // ---- Background Panel that auto-scales image ----
        ImageIcon bgIcon = new ImageIcon("payment.jpg"); // your image file
        Image bgImage = bgIcon.getImage();

        JPanel bgPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // draw scaled image to fit current size
                g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
            }
        };
        setContentPane(bgPanel);

        // ---- Right-side container (compact & padded) ----
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setOpaque(false);
        rightPanel.setPreferredSize(new Dimension(400, 600));
        rightPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // ---- Booking Summary ----
        JTextArea summary = new JTextArea();
        summary.setEditable(false);
        // MODIFIED: Updated summary to include price
        summary.setText("Booking Summary:\n\n"
                + "Name: " + name + "\n"
                + "Email: " + email + "\n"
                + "Phone: " + phone + "\n"
                + "Car: " + car + "\n"
                + "Pickup: " + pickup + "\n"
                + "Drop: " + drop + "\n"
                + "Pickup Location: " + pickupLoc + "\n"
                + "Drop-off Location: " + dropoffLoc + "\n"
                + "Total Price: $" + price + "\n"); // <--- NEW: Display price
        summary.setBorder(BorderFactory.createTitledBorder("Booking Details"));
        summary.setBackground(new Color(255,255,255,180));
        rightPanel.add(new JScrollPane(summary), BorderLayout.NORTH);

        // ---- Payment Form ----
        form = new JPanel(new CardLayout());
        form.setBorder(BorderFactory.createTitledBorder("Payment Information"));
        form.setOpaque(false);

        paymentMethod = new JComboBox<>(new String[]{"Debit Card", "Credit Card", "UPI"});
        
        // --- Payment Method Panel ---
        JPanel methodPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        methodPanel.setOpaque(false);
        
        // **MODIFICATION 1: Set "Select Payment Method" label to WHITE**
        JLabel selectMethodLabel = new JLabel("Select Payment Method:");
        selectMethodLabel.setForeground(Color.WHITE); 
        
        methodPanel.add(selectMethodLabel);
        methodPanel.add(paymentMethod);

        // --- Card Panel ---
        JPanel cardPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        cardPanel.setOpaque(false);
        cardNumber = new JTextField();
        cardName   = new JTextField();
        expiry     = new JTextField("MM/YY");
        cvv        = new JTextField();
        
        // **MODIFICATION 2: Set Card Panel Labels to WHITE**
        JLabel numLbl = new JLabel("Card Number:");
        numLbl.setForeground(Color.WHITE);
        cardPanel.add(numLbl);
        cardPanel.add(cardNumber);
        
        JLabel nameLbl = new JLabel("Cardholder Name:");
        nameLbl.setForeground(Color.WHITE);
        cardPanel.add(nameLbl);
        cardPanel.add(cardName);
        
        JLabel expiryLbl = new JLabel("Expiry Date:");
        expiryLbl.setForeground(Color.WHITE);
        cardPanel.add(expiryLbl);
        cardPanel.add(expiry);
        
        JLabel cvvLbl = new JLabel("CVV:");
        cvvLbl.setForeground(Color.WHITE);
        cardPanel.add(cvvLbl);
        cardPanel.add(cvv);

        // --- UPI Panel ---
        JPanel upiPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        upiPanel.setOpaque(false);
        upiId = new JTextField(15);
        
        // **MODIFICATION 3: Set UPI Panel Labels to WHITE**
        JLabel upiLbl = new JLabel("UPI ID:");
        upiLbl.setForeground(Color.WHITE);
        
        upiPanel.add(upiLbl);
        upiPanel.add(upiId);
        upiPanel.setPreferredSize(new Dimension(300, 60));

        form.add(cardPanel, "Card");
        form.add(upiPanel, "UPI");

        CardLayout cl = (CardLayout) form.getLayout();
        cl.show(form, "Card");

        paymentMethod.addActionListener(e -> {
            String method = (String) paymentMethod.getSelectedItem();
            if (method.equals("UPI")) {
                cl.show(form, "UPI");
            } else {
                cl.show(form, "Card");
            }
        });

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(methodPanel, BorderLayout.NORTH);
        centerPanel.add(form, BorderLayout.CENTER);
        rightPanel.add(centerPanel, BorderLayout.CENTER);

        // ---- Buttons ----
        JPanel buttons = new JPanel();
        buttons.setOpaque(false);
        JButton payBtn    = new JButton("Pay Now");
        JButton cancelBtn = new JButton("Cancel");
        buttons.add(payBtn);
        buttons.add(cancelBtn);
        rightPanel.add(buttons, BorderLayout.SOUTH);

        // Center the right panel vertically on the right
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.CENTER;
        wrapper.add(rightPanel, gbc);
        add(wrapper, BorderLayout.EAST);

        // ---- Button actions ----
        payBtn.addActionListener(e -> {
            String method = (String) paymentMethod.getSelectedItem();
            if (method.equals("UPI")) {
                if (upiId.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                            "Please enter UPI ID!",
                            "Validation", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                showOtpPanel("UPI", upiId.getText(), null, null, null, null);
            } else {
                if (cardNumber.getText().trim().isEmpty()
                        || cardName.getText().trim().isEmpty()
                        || expiry.getText().trim().isEmpty()
                        || cvv.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                            "Please fill all card details!",
                            "Validation", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                showOtpPanel(method, null,
                        cardNumber.getText(), cardName.getText(),
                        expiry.getText(), cvv.getText());
            }
        });

        cancelBtn.addActionListener(e -> this.dispose());
    }

    private void showOtpPanel(String method, String upi,
                              String cardNum, String cardName,
                              String expiry, String cvv) {
        JDialog otpDialog = new JDialog(this, "OTP Verification", true);
        otpDialog.setSize(300, 150);
        otpDialog.setLocationRelativeTo(this);
        otpDialog.setLayout(new GridLayout(2, 2, 10, 10));

        JTextField otpField = new JTextField();
        JButton verifyBtn = new JButton("Verify OTP");

        otpDialog.add(new JLabel("Enter OTP:"));
        otpDialog.add(otpField);
        otpDialog.add(new JLabel(""));
        otpDialog.add(verifyBtn);

        verifyBtn.addActionListener(ev -> {
            if (otpField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(otpDialog,
                        "Please enter OTP!",
                        "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (method.equals("UPI")) {
                insertPaymentUPI(upi);
            } else {
                insertPayment(cardNum, cardName, expiry, cvv);
            }

            // MODIFIED: Call ReceiptPage instead of showing a simple message
            new ReceiptPage(
                this.name, this.email, this.phone, 
                this.car, this.pickup, this.drop, 
                this.pickupLoc, this.dropoffLoc, this.price
            ).setVisible(true);

            otpDialog.dispose();
            this.dispose();
        });

        otpDialog.setVisible(true);
    }

    private void insertPayment(String cardNum, String cardHolder,
                               String expiry, String cvv) {
        String sql = "INSERT INTO mycards (card_number, card_name, expiry, cvv) VALUES (?,?,?,?)";
        // NOTE: This assumes you have a DBUtil.getConnection() class available
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cardNum);
            ps.setString(2, cardHolder);
            ps.setString(3, expiry);
            ps.setString(4, cvv);
            ps.executeUpdate();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Database error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void insertPaymentUPI(String upiId) {
        String sql = "INSERT INTO myupi (upi_id) VALUES (?)";
        // NOTE: This assumes you have a DBUtil.getConnection() class available
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, upiId);
            ps.executeUpdate();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Database error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() ->
            new PaymentPage(
                "John Doe",
                "john@example.com",
                "1234567890",
                "Audi A8",
                new Date(),
                new Date(System.currentTimeMillis() + 3600000),
                "Kochi",
                "Alappuzha",
                940 // Placeholder price for testing
            ).setVisible(true)
        );
    }
}