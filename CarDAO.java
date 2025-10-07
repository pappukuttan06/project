// CarDAO.java
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;

public class CarDAO {

    private static final String TABLE_NAME = "available_cars";

    public CarDAO() {
        initializeDatabase();
    }

    // Ensures the 'available_cars' table exists and adds initial cars if empty
    public void initializeDatabase() {
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
                   + "id INT AUTO_INCREMENT PRIMARY KEY,"
                   + "model VARCHAR(255) NOT NULL UNIQUE,"
                   + "daily_rent INT NOT NULL)";
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            if (loadAllCars().isEmpty()) {
                // Add default cars if the table is empty
                Map<String, Integer> defaultCars = Map.of(
                    "Mercedes C-Class", 900,
                    "BMW 7 Series", 850,
                    "Audi A8", 940,
                    "Toyota Alphard", 900,
                    "Range Rover", 1000,
                    "Lexus LS", 999,
                    "Mercedes E-Class", 1000,
                    "Land Rover Defender", 1200
                );
                for (Map.Entry<String, Integer> entry : defaultCars.entrySet()) {
                    addCar(entry.getKey(), entry.getValue());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    // Loads all available cars from the database
    public Map<String, Integer> loadAllCars() {
        Map<String, Integer> carList = new LinkedHashMap<>();
        // Note: The Admin page seems to order alphabetically by model, so we use ASC here.
        String sql = "SELECT model, daily_rent FROM " + TABLE_NAME + " ORDER BY model ASC"; 
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                carList.put(rs.getString("model"), rs.getInt("daily_rent"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return carList;
    }

    // Adds a new car record
    public boolean addCar(String model, int dailyRent) {
        String sql = "INSERT INTO " + TABLE_NAME + " (model, daily_rent) VALUES (?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, model);
            ps.setInt(2, dailyRent);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            // Unique constraint violation (car already exists)
            return false; 
        }
    }

    /**
     * Executes the SQL UPDATE command to change the daily_rent price.
     */
    public boolean updateCarPrice(String model, int newDailyRent) {
        String sql = "UPDATE " + TABLE_NAME + " SET daily_rent = ? WHERE model = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, newDailyRent);
            ps.setString(2, model);
            return ps.executeUpdate() > 0; // Returns true if one or more rows were updated
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Deletes a car record
    public boolean deleteCar(String model) {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE model = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, model);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
