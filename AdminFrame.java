// AdminFrame.java
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class AdminFrame extends JFrame {

    private final CarRentalUI parentUI; 
    private JTable carTable;
    private DefaultTableModel carTableModel;
    private JTable bookingTable;
    private DefaultTableModel bookingTableModel;
    
    // DAOs (Assuming CarDAO and BookingDAO are in the project)
    private final CarDAO carDAO = new CarDAO(); 
    private final BookingDAO bookingDAO = new BookingDAO(); 

    private static final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 36);
    private static final Font HEADER_FONT = new Font("SansSerif", Font.BOLD, 16);
    private static final Font BUTTON_FONT = new Font("SansSerif", Font.BOLD, 15);
    private static final Color BG_COLOR = new Color(30, 30, 30);
    private static final Color TEXT_COLOR = Color.WHITE;

    public AdminFrame(CarRentalUI parentUI) {
        this.parentUI = parentUI;
        setTitle("Admin Car Management & Bookings");
        setSize(1200, 700); 
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); 
        setLocationRelativeTo(null);
        
        setupUI();
        
        loadCarData(); 
        loadBookingData();
        
        setVisible(true);
    }

    private void setupUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BG_COLOR);
        
        JLabel title = new JLabel("Admin Dashboard 🛠️", SwingConstants.CENTER);
        title.setFont(TITLE_FONT);
        title.setForeground(TEXT_COLOR);
        title.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        mainPanel.add(title, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(HEADER_FONT);
        
        tabbedPane.addTab("Car Inventory", createCarManagementPanel());
        tabbedPane.addTab("View Bookings", createBookingPanel()); 
        
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 10));
        footerPanel.setBackground(new Color(20, 20, 20));
        JButton logoutBtn = createStyledButton("Admin Log Out");
        logoutBtn.addActionListener(e -> {
            // Placeholder for admin logout logic
            // parentUI.logoutAdmin(); 
            dispose(); 
        });
        footerPanel.add(logoutBtn);
        mainPanel.add(footerPanel, BorderLayout.SOUTH);
        
        setContentPane(mainPanel);
    }
    
    // --- Car Management Panel ---
    private JPanel createCarManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(40, 40, 40)); 
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        String[] columnNames = {"Car Model", "Daily Rent ($)"};
        carTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 1; // Only price is editable
            }
        };
        carTable = new JTable(carTableModel);
        carTable.setFont(new Font("SansSerif", Font.PLAIN, 14));
        carTable.getTableHeader().setFont(HEADER_FONT);
        carTable.setRowHeight(25);
        
        JScrollPane scrollPane = new JScrollPane(carTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        actionPanel.setOpaque(false);

        JButton addBtn = createStyledButton("Add New Car");
        addBtn.addActionListener(e -> addNewCar());
        
        JButton updateBtn = createStyledButton("Save Price Change (in table)");
        updateBtn.addActionListener(e -> updateCarPrice());

        JButton deleteCarBtn = createStyledButton("Delete Car");
        deleteCarBtn.addActionListener(e -> deleteCar());
        
        actionPanel.add(addBtn);
        actionPanel.add(updateBtn);
        actionPanel.add(deleteCarBtn);
        
        panel.add(actionPanel, BorderLayout.SOUTH);
        return panel;
    }
    
    private void loadCarData() {
        carTableModel.setRowCount(0); 
        Map<String, Integer> cars = carDAO.loadAllCars();
        List<Map.Entry<String, Integer>> list = new ArrayList<>(cars.entrySet());
        list.sort(Comparator.comparing(Map.Entry::getKey)); 

        for (Map.Entry<String, Integer> entry : list) {
            carTableModel.addRow(new Object[]{entry.getKey(), entry.getValue()});
        }
    }
    
    private void addNewCar() {
        String name = JOptionPane.showInputDialog(this, "Enter New Car Model Name:");
        if (name == null || name.trim().isEmpty()) return;
        
        String priceStr = JOptionPane.showInputDialog(this, "Enter Daily Rent for " + name + ":");
        if (priceStr == null || priceStr.trim().isEmpty()) return;

        try {
            int price = Integer.parseInt(priceStr.trim());
            if (carDAO.addCar(name, price)) {
                loadCarData(); 
                // Assumes BookingPage.loadCarPricesFromDB() is available
                // BookingPage.loadCarPricesFromDB(); 
                JOptionPane.showMessageDialog(this, "Car added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                 JOptionPane.showMessageDialog(this, "Failed to add car. It may already exist.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid price entered. Price must be a number.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateCarPrice() {
        int selectedRow = carTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select the car row you edited, then click this button.", "Selection Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String carName = (String) carTableModel.getValueAt(selectedRow, 0);
        Object priceObj = carTableModel.getValueAt(selectedRow, 1);
        
        try {
            int newPrice = Integer.parseInt(priceObj.toString().trim());
            
            if (carDAO.updateCarPrice(carName, newPrice)) {
                loadCarData(); 
                // BookingPage.loadCarPricesFromDB(); 
                JOptionPane.showMessageDialog(this, "Price for " + carName + " updated to $" + newPrice + ".", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update price in DB.", "Error", JOptionPane.ERROR_MESSAGE);
                loadCarData(); 
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid price in the selected row's price column. Please ensure it is a number.", "Error", JOptionPane.ERROR_MESSAGE);
            loadCarData(); 
        }
    }

    private void deleteCar() {
        int selectedRow = carTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a car to delete.", "Selection Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String carName = (String) carTableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to delete " + carName + "? This car will be permanently removed.", 
                "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (carDAO.deleteCar(carName)) {
                loadCarData(); 
                // BookingPage.loadCarPricesFromDB();
                JOptionPane.showMessageDialog(this, carName + " deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete car from DB.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    // --- Booking View Panel ---
    private JPanel createBookingPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(40, 40, 40)); 
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        bookingTableModel = new DefaultTableModel(BookingDAO.COLUMN_NAMES, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; 
            }
        };
        
        bookingTable = new JTable(bookingTableModel);
        bookingTable.setFont(new Font("SansSerif", Font.PLAIN, 12));
        bookingTable.getTableHeader().setFont(HEADER_FONT);
        bookingTable.setRowHeight(20);
        bookingTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        
        JScrollPane scrollPane = new JScrollPane(bookingTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Action Panel for Booking Controls
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        actionPanel.setOpaque(false);
        
        JButton refreshBtn = createStyledButton("Refresh Bookings");
        refreshBtn.addActionListener(e -> loadBookingData());
        
        // NEW: Delete Booking Button
        JButton deleteBookingBtn = createStyledButton("Delete Selected Booking");
        deleteBookingBtn.setBackground(new Color(200, 50, 50)); // Red background
        deleteBookingBtn.setForeground(Color.WHITE);
        deleteBookingBtn.addActionListener(e -> deleteBookingDetails());
        
        actionPanel.add(deleteBookingBtn);
        actionPanel.add(refreshBtn);
        
        panel.add(actionPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void loadBookingData() {
        bookingTableModel.setRowCount(0); 
        
        Vector<Vector<Object>> bookings = bookingDAO.loadAllBookings();
        
        for (Vector<Object> row : bookings) {
            bookingTableModel.addRow(row);
        }
    }
    
    /**
     * Deletes the selected booking row using the Booking ID (first column).
     */
    private void deleteBookingDetails() {
        int selectedRow = bookingTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a booking to delete.", "Selection Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // The Booking ID is in the first column (index 0)
        int bookingId = (int) bookingTableModel.getValueAt(selectedRow, 0); 
        String customerName = (String) bookingTableModel.getValueAt(selectedRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to permanently delete the booking for " + customerName + " (ID: " + bookingId + ")?", 
                "Confirm Delete Booking", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (bookingDAO.deleteBooking(bookingId)) {
                loadBookingData(); // Refresh the table after deletion
                JOptionPane.showMessageDialog(this, "Booking ID " + bookingId + " deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete booking from DB.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(BUTTON_FONT);
        btn.setForeground(Color.BLACK);
        btn.setBackground(Color.WHITE);
        btn.setFocusPainted(false);
        return btn;
    }
}
