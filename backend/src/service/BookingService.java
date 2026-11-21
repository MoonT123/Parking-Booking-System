
package service;

import dao.BookingDAO;
import model.Booking;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
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

    // 2. CALCULATE BOOKING AMOUNT
    public BigDecimal calculateAmount(
            Timestamp startTime,
            Timestamp endTime,
            BigDecimal hourlyRate) {

        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException(
                    "Booking time cannot be null.");
        }

        if (!endTime.after(startTime)) {
            throw new IllegalArgumentException(
                    "Invalid booking time.");
        }

        if (hourlyRate == null
                || hourlyRate.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Invalid hourly rate.");
        }

        long minutes = Duration.between(
                startTime.toLocalDateTime(),
                endTime.toLocalDateTime()
        ).toMinutes();

        BigDecimal hours = BigDecimal.valueOf(minutes)
                .divide(
                        BigDecimal.valueOf(60),
                        4,
                        RoundingMode.HALF_UP
                );

        return hours.multiply(hourlyRate)
                .setScale(2, RoundingMode.HALF_UP);
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
    // 4. CREATE BOOKING

    public boolean createBooking(
            Booking booking,
            BigDecimal hourlyRate) throws SQLException {

        validateBooking(booking);

        if (!checkAvailability(booking)) {
            throw new IllegalArgumentException(
                    "This parking bay is already booked "
                    + "during the selected time.");
        }

        BigDecimal amount = calculateAmount(
                booking.getStartTime(),
                booking.getEndTime(),
                hourlyRate
        );

        booking.setTotalAmount(amount);
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
    // 9. UPDATE BOOKING
    public boolean updateBooking(
            Booking booking,
            BigDecimal hourlyRate) throws SQLException {

        validateBooking(booking);

        if (booking.getBookingID() <= 0) {
            throw new IllegalArgumentException(
                    "Invalid Booking ID.");
        }

        Booking existing = bookingDAO.getBookingById(
                booking.getBookingID());

        if (existing == null) {
            throw new IllegalArgumentException(
                    "Booking does not exist.");
        }

        if ("CANCELLED".equalsIgnoreCase(
                existing.getBookingStatus())
                || "COMPLETED".equalsIgnoreCase(
                existing.getBookingStatus())) {

            throw new IllegalArgumentException(
                    "Cannot update a cancelled or completed booking.");
        }

        if (!checkAvailability(booking)) {
            throw new IllegalArgumentException(
                    "Parking bay is not available.");
        }

        booking.setTotalAmount(calculateAmount(
                booking.getStartTime(),
                booking.getEndTime(),
                hourlyRate
        ));
        if (booking.getBookingStatus() == null
        || booking.getBookingStatus().trim().isEmpty()) {
    booking.setBookingStatus(existing.getBookingStatus());
}
        return bookingDAO.updateBooking(booking);
    }
    // 10. CANCEL BOOKING
    public boolean cancelBooking(int bookingID)
            throws SQLException {

        Booking booking = bookingDAO.getBookingById(bookingID);

        if (booking == null) {
            throw new IllegalArgumentException(
                    "Booking does not exist.");
        }

        String status = booking.getBookingStatus();

        if ("CANCELLED".equalsIgnoreCase(status)
                || "COMPLETED".equalsIgnoreCase(status)
                || "EXPIRED".equalsIgnoreCase(status)) {

            throw new IllegalArgumentException(
                    "This booking cannot be cancelled.");
        }

        return bookingDAO.updateBookingStatus(
                bookingID, "CANCELLED");
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
                bookingID, status);
    }
    // 12. VALIDATE STATUS
    private void validateStatus(String status) {

        if (status == null) {
            throw new IllegalArgumentException(
                    "Booking status cannot be empty.");
        }

        String value = status.toUpperCase();

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
