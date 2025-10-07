import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

public class BookingDAO {

    private static final String TABLE_NAME = "bookings";

    public static final String[] COLUMN_NAMES = {
        "ID", "Name", "Email", "Phone", "Address", "Car", "Pickup Date",
        "Drop Date", "Pickup Location", "Drop-off Location", "Price ($)"
    };

    /**
     * Fetches all booking records, ordered by ID ascending.
     */
    public Vector<Vector<Object>> loadAllBookings() {
        Vector<Vector<Object>> bookingData = new Vector<>();
        
        String sql = "SELECT * FROM " + TABLE_NAME + " ORDER BY id ASC"; 
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("id")); 
                row.add(rs.getString("name"));
                row.add(rs.getString("email"));
                row.add(rs.getString("phone"));
                row.add(rs.getString("address"));
                row.add(rs.getString("car"));
                row.add(rs.getTimestamp("pickup_date").toString().substring(0, 16)); 
                row.add(rs.getTimestamp("drop_date").toString().substring(0, 16));
                row.add(rs.getString("pickup_location"));
                row.add(rs.getString("dropoff_location"));
                row.add(rs.getInt("price"));
                
                bookingData.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bookingData;
    }
    
    /**
     * Deletes a specific booking record by its ID.
     * @param bookingId The ID of the booking to delete.
     * @return true if the deletion was successful, false otherwise.
     */
    public boolean deleteBooking(int bookingId) {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            return ps.executeUpdate() > 0; // Returns true if one or more rows were deleted
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
