import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ReceiptPage extends JFrame {

    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("dd-MM-yyyy HH:mm");

    // Fields used by the helper method
    private JPanel receiptPanel;
    private GridBagConstraints gbc;
    private int y = 0; // Initialize 'y' as a class field to be mutable and accessible

    public ReceiptPage(String name, String email, String phone,
                       String car, Date pickup, Date drop,
                       String pickupLoc, String dropoffLoc, int price) {
        
        setTitle("Booking Receipt");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Header
        JPanel header = new JPanel();
        header.setBackground(new Color(40, 40, 40));
        JLabel title = new JLabel("✅ BOOKING CONFIRMED - RECEIPT", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(Color.WHITE);
        header.add(title);
        add(header, BorderLayout.NORTH);

        // Receipt Content Panel setup
        this.receiptPanel = new JPanel(new GridBagLayout()); // Assign to field
        receiptPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        receiptPanel.setBackground(Color.WHITE);
        
        this.gbc = new GridBagConstraints(); // Assign to field
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Reset row counter
        this.y = 0; 
        
        // --- Details ---
        
        // Customer Details
        addDetail("Customer Name:", name);
        addDetail("Email:", email);
        addDetail("Phone:", phone);
        
        // Separator
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 2;
        receiptPanel.add(new JSeparator(SwingConstants.HORIZONTAL), gbc);
        y++;

        // Booking Details (Car, Dates, Locations)
        addDetail("Car Model:", car);
        addDetail("Pickup Date:", dateFormat.format(pickup));
        addDetail("Drop Date:", dateFormat.format(drop));
        addDetail("Pickup Location:", pickupLoc);
        addDetail("Drop-off Location:", dropoffLoc);
        
        // Separator
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 2;
        receiptPanel.add(new JSeparator(SwingConstants.HORIZONTAL), gbc);
        y++;

        // Price
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.EAST;
        JLabel priceLbl = new JLabel("TOTAL AMOUNT PAID: $" + price, SwingConstants.RIGHT);
        priceLbl.setFont(new Font("SansSerif", Font.BOLD, 26));
        priceLbl.setForeground(new Color(0, 150, 0)); // Green color for price
        priceLbl.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        receiptPanel.add(priceLbl, gbc);
        y++;

        // Fill remaining space
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 2; gbc.weighty = 1.0;
        receiptPanel.add(Box.createVerticalGlue(), gbc);
        
        add(receiptPanel, BorderLayout.CENTER);

        // Footer
        JPanel footer = new JPanel();
        JButton closeBtn = new JButton("Close Receipt");
        closeBtn.addActionListener(e -> dispose());
        footer.add(closeBtn);
        add(footer, BorderLayout.SOUTH);
    }
    
    // CORRECTED: Private helper method replaces the anonymous object
    private void addDetail(String label, String value) {
        gbc.gridx = 0; gbc.gridy = y; gbc.weightx = 0.3;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 14));
        receiptPanel.add(lbl, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        JLabel val = new JLabel(value);
        val.setFont(new Font("SansSerif", Font.PLAIN, 14));
        receiptPanel.add(val, gbc);
        y++; // 'y' is now an instance field and can be mutated
    }

    public static void main(String[] args) {
        // Example usage for testing
        SwingUtilities.invokeLater(() ->
            new ReceiptPage(
                "Customer Name", "customer@email.com", "9876543210",
                "Range Rover", new Date(), 
                new Date(System.currentTimeMillis() + 86400000 * 3), // 3 days later
                "Kochi", "Alappuzha", 3000
            ).setVisible(true)
        );
    }
}
