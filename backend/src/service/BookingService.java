package service;

import dao.BookingDAO;
import model.Booking;

import java.sql.SQLException;
import java.util.List;

public class BookingService {

    private final BookingDAO bookingDAO;

    public BookingService() {
        this.bookingDAO = new BookingDAO();
    }

    // 1. VALIDATE BOOKING
    private void validateBooking(Booking booking)
            throws IllegalArgumentException {

        if (booking == null) {
            throw new IllegalArgumentException(
                    "Booking cannot be null.");
        }

        if (booking.getUserID() <= 0) {
            throw new IllegalArgumentException(
                    "Invalid User ID.");
        }

        if (booking.getVehicleID() <= 0) {
            throw new IllegalArgumentException(
                    "Invalid Vehicle ID.");
        }

        if (booking.getLotId() <= 0) {
            throw new IllegalArgumentException(
                    "Invalid Parking Lot ID.");
        }

        if (booking.getBayNumber() == null
                || booking.getBayNumber().trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Parking bay number cannot be empty.");
        }

        if (booking.getStartTime() == null
                || booking.getEndTime() == null) {
            throw new IllegalArgumentException(
                    "Booking time cannot be empty.");
        }

        if (!booking.getEndTime().after(
                booking.getStartTime())) {
            throw new IllegalArgumentException(
                    "End time must be after start time.");
        }
    }

    // 3. CHECK BOOKING AVAILABILITY
    public boolean checkAvailability(Booking booking)
            throws SQLException {

        validateBooking(booking);

        return !bookingDAO.hasOverlappingBooking(
                booking.getLotId(),
                booking.getBayNumber(),
                booking.getStartTime(),
                booking.getEndTime(),
                booking.getBookingID()
        );
    }

    public boolean createBooking(Booking booking) throws SQLException {
        validateBooking(booking);
        booking.setBookingStatus("PENDING");
        return bookingDAO.insertBooking(booking);
    }

    // 5. GET ALL BOOKINGS
    public List<Booking> getAllBookings()
            throws SQLException {

        return bookingDAO.getAllBookings();
    }

    // 6. GET BOOKING BY ID
    public Booking getBookingById(int bookingID)
            throws SQLException {

        if (bookingID <= 0) {
            throw new IllegalArgumentException(
                    "Invalid Booking ID.");
        }

        return bookingDAO.getBookingById(bookingID);
    }

    // 7. GET BOOKINGS BY USER
    public List<Booking> getBookingsByUserId(int userID)
            throws SQLException {

        if (userID <= 0) {
            throw new IllegalArgumentException(
                    "Invalid User ID.");
        }

        return bookingDAO.getBookingsByUserId(userID);
    }

    // 8. GET BOOKINGS BY STATUS
    public List<Booking> getBookingsByStatus(String status)
            throws SQLException {

        validateStatus(status);

        return bookingDAO.getBookingsByStatus(status);
    }

    public boolean updateBooking(Booking booking) throws SQLException {
        validateBooking(booking);
        if (booking.getBookingID() <= 0) {
            throw new IllegalArgumentException("Invalid booking ID.");
        }
        return bookingDAO.updateBooking(booking);
    }

    public boolean cancelBooking(int bookingID) throws SQLException {
        if (bookingID <= 0) {
            throw new IllegalArgumentException("Invalid booking ID.");
        }
        return bookingDAO.cancelBooking(bookingID);
    }

    // 11. UPDATE BOOKING STATUS
    public boolean updateBookingStatus(
            int bookingID,
            String status) throws SQLException {

        if (bookingDAO.getBookingById(bookingID) == null) {
            throw new IllegalArgumentException(
                    "Booking does not exist.");
        }

        validateStatus(status);

        return bookingDAO.updateBookingStatus(
                bookingID, status.toUpperCase(java.util.Locale.ROOT));
    }

    // 12. VALIDATE STATUS
    private void validateStatus(String status) {

        if (status == null) {
            throw new IllegalArgumentException(
                    "Booking status cannot be empty.");
        }

        String value = status.toUpperCase(java.util.Locale.ROOT);

        if (!value.equals("PENDING")
                && !value.equals("CONFIRMED")
                && !value.equals("COMPLETED")
                && !value.equals("CANCELLED")
                && !value.equals("EXPIRED")) {

            throw new IllegalArgumentException(
                    "Invalid booking status.");
        }
    }
}
