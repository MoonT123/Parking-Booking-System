package parkingbookingsystem;

import dao.BookingDAO;
import java.sql.Timestamp;
import java.math.BigDecimal;
import service.BookingService;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import model.Booking;

import javax.swing.JOptionPane;


public class BookingsFrame extends JFrame {

    private final BookingDAO dao = new BookingDAO();
    
private final BookingService bookingService = new BookingService();

    private final JTextField bookingIdField = new JTextField(8);

    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{
                "Booking ID", "Start Time", "End Time", "Total Amount",
                "Status", "Created At", "User ID", "Vehicle ID",
                "Lot ID", "Bay Number"
            }, 0);

    private final JTable table = new JTable(model);

    public BookingsFrame() {
        super("Parking Booking System - Bookings");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1100, 500);

        table.setDefaultEditor(Object.class, null);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);

        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(event -> loadBookings());
        bookingIdField.addActionListener(event -> loadBookings());

        JButton detailsButton = new JButton("View Details");
        detailsButton.addActionListener(event -> showBookingDetails());
        
JButton createButton = new JButton("Create Booking");

createButton.addActionListener(event -> createNewBooking());

JButton updateButton = new JButton("Update Booking");

updateButton.addActionListener(event -> updateSelectedBooking());

JButton cancelButton = new JButton("Cancel Booking");

cancelButton.addActionListener(event -> cancelSelectedBooking());


        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbar.add(new JLabel("Booking ID:"));
        toolbar.add(bookingIdField);
        toolbar.add(searchButton);
        toolbar.add(detailsButton);
        
    
        toolbar.add(createButton);
        toolbar.add(updateButton);
        toolbar.add(cancelButton);

        

        add(toolbar, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        loadBookings();
    }

    private void loadBookings() {
        String input = bookingIdField.getText().trim();
        Integer bookingId = null;

        if (!input.isEmpty()) {
            try {
                bookingId = Integer.valueOf(input);
                if (bookingId <= 0) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                        "Enter a positive Booking ID or leave the field blank.");
                return;
            }
        }

        List<Booking> bookings;

        try {
            if (bookingId == null) {
                bookings = dao.getAllBookings();
                bookings.sort(Comparator.comparingInt(Booking::getBookingID));
            } else {
                Booking booking = dao.getBookingById(bookingId);
                bookings = booking == null ? List.of() : List.of(booking);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Could not load bookings: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        model.setRowCount(0);

        for (Booking booking : bookings) {
            model.addRow(new Object[]{
                booking.getBookingID(),
                booking.getStartTime(),
                booking.getEndTime(),
                booking.getTotalAmount(),
                booking.getBookingStatus(),
                booking.getCreatedAt(),
                booking.getUserID(),
                booking.getVehicleID(),
                booking.getLotId(),
                booking.getBayNumber()
            });
        }

        if (bookingId != null && bookings.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No booking found with ID " + bookingId + ".");
        }
    }

    private void showBookingDetails() {
        int selectedRow = table.getSelectedRow();

        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Select a booking first.");
            return;
        }

        int row = table.convertRowIndexToModel(selectedRow);
        StringBuilder details = new StringBuilder();

        for (int column = 0; column < model.getColumnCount(); column++) {
            details.append(model.getColumnName(column))
                    .append(": ")
                    .append(model.getValueAt(row, column))
                    .append('\n');
        }

        JOptionPane.showMessageDialog(this, details.toString(),
                "Booking Details", JOptionPane.INFORMATION_MESSAGE);
    }

private void cancelSelectedBooking() {
    int selectedRow = table.getSelectedRow();

    if (selectedRow < 0) {
        JOptionPane.showMessageDialog(
                this, "Please select a booking first.");
        return;
    }

    int row = table.convertRowIndexToModel(selectedRow);
    int bookingId = (int) model.getValueAt(row, 0);

    int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to cancel Booking ID " + bookingId + "?",
            "Confirm Cancellation",
            JOptionPane.YES_NO_OPTION
    );

    if (confirm != JOptionPane.YES_OPTION) {
        return;
    }

    try {
        boolean success = bookingService.cancelBooking(bookingId);

        if (success) {
            JOptionPane.showMessageDialog(
                    this, "Booking cancelled successfully!");
            loadBookings();
        } else {
            JOptionPane.showMessageDialog(
                    this, "Could not cancel booking.");
        }
    } catch (SQLException | IllegalArgumentException ex) {
        JOptionPane.showMessageDialog(
                this,
                ex.getMessage(),
                "Cancellation Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}

private void createNewBooking() {

    JTextField userField = new JTextField();
    JTextField vehicleField = new JTextField();
    JTextField lotField = new JTextField();
    JTextField bayField = new JTextField();
    JTextField startField = new JTextField();
    JTextField endField = new JTextField();
    JTextField rateField = new JTextField();

    JPanel form = new JPanel(
            new java.awt.GridLayout(0, 2, 10, 10));

    form.add(new JLabel("User ID:"));
    form.add(userField);

    form.add(new JLabel("Vehicle ID:"));
    form.add(vehicleField);

    form.add(new JLabel("Lot ID:"));
    form.add(lotField);

    form.add(new JLabel("Bay Number:"));
    form.add(bayField);

    form.add(new JLabel("Start Time (yyyy-MM-dd HH:mm:ss):"));
    form.add(startField);

    form.add(new JLabel("End Time (yyyy-MM-dd HH:mm:ss):"));
    form.add(endField);

    form.add(new JLabel("Hourly Rate:"));
    form.add(rateField);

    int result = JOptionPane.showConfirmDialog(
            this,
            form,
            "Create New Booking",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
    );

    if (result != JOptionPane.OK_OPTION) {
        return;
    }

    try {
        Booking booking = new Booking();

        booking.setUserID(
                Integer.parseInt(userField.getText().trim()));

        booking.setVehicleID(
                Integer.parseInt(vehicleField.getText().trim()));

        booking.setLotId(
                Integer.parseInt(lotField.getText().trim()));

        booking.setBayNumber(
                bayField.getText().trim());

        booking.setStartTime(
                Timestamp.valueOf(startField.getText().trim()));

        booking.setEndTime(
                Timestamp.valueOf(endField.getText().trim()));

        BigDecimal hourlyRate = new BigDecimal(
                rateField.getText().trim());

        if (booking.getUserID() <= 0
                || booking.getVehicleID() <= 0
                || booking.getLotId() <= 0
                || hourlyRate.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "IDs must be positive and hourly rate cannot be negative.");
        }

        boolean success = bookingService.createBooking(
                booking, hourlyRate);

        if (success) {
            JOptionPane.showMessageDialog(
                    this, "Booking created successfully!");
            bookingIdField.setText("");
            loadBookings();
        } else {
            JOptionPane.showMessageDialog(
                    this, "Could not create booking.");
        }

    } catch (Exception ex) {
        JOptionPane.showMessageDialog(
                this,
                "Error: " + ex.getMessage(),
                "Create Booking Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}

private void updateSelectedBooking() {
    int selectedRow = table.getSelectedRow();

    if (selectedRow < 0) {
        JOptionPane.showMessageDialog(
                this, "Please select a booking first.");
        return;
    }

    int row = table.convertRowIndexToModel(selectedRow);
    int bookingId = (int) model.getValueAt(row, 0);

    try {
        Booking booking = bookingService.getBookingById(bookingId);

        if (booking == null) {
            JOptionPane.showMessageDialog(
                    this, "Booking not found.");
            return;
        }

        String status = booking.getBookingStatus();

        if ("CANCELLED".equalsIgnoreCase(status)
                || "COMPLETED".equalsIgnoreCase(status)) {
            JOptionPane.showMessageDialog(
                    this, "This booking cannot be updated.");
            return;
        }

        JTextField userField = new JTextField(
                String.valueOf(booking.getUserID()));
        JTextField vehicleField = new JTextField(
                String.valueOf(booking.getVehicleID()));
        JTextField lotField = new JTextField(
                String.valueOf(booking.getLotId()));
        JTextField bayField = new JTextField(
                booking.getBayNumber());
        JTextField startField = new JTextField(
                booking.getStartTime().toString());
        JTextField endField = new JTextField(
                booking.getEndTime().toString());
        JTextField rateField = new JTextField();

        JPanel form = new JPanel(
                new java.awt.GridLayout(0, 2, 10, 10));

        form.add(new JLabel("User ID:"));
        form.add(userField);
        form.add(new JLabel("Vehicle ID:"));
        form.add(vehicleField);
        form.add(new JLabel("Lot ID:"));
        form.add(lotField);
        form.add(new JLabel("Bay Number:"));
        form.add(bayField);
        form.add(new JLabel("Start Time (yyyy-MM-dd HH:mm:ss):"));
        form.add(startField);
        form.add(new JLabel("End Time (yyyy-MM-dd HH:mm:ss):"));
        form.add(endField);
        form.add(new JLabel("Hourly Rate:"));
        form.add(rateField);

        int result = JOptionPane.showConfirmDialog(
                this, form,
                "Update Booking ID " + bookingId,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        booking.setUserID(
                Integer.parseInt(userField.getText().trim()));
        booking.setVehicleID(
                Integer.parseInt(vehicleField.getText().trim()));
        booking.setLotId(
                Integer.parseInt(lotField.getText().trim()));
        booking.setBayNumber(bayField.getText().trim());
        booking.setStartTime(
                Timestamp.valueOf(startField.getText().trim()));
        booking.setEndTime(
                Timestamp.valueOf(endField.getText().trim()));

        BigDecimal hourlyRate = new BigDecimal(
                rateField.getText().trim());

        if (hourlyRate.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Hourly rate cannot be negative.");
        }

        boolean success = bookingService.updateBooking(
                booking, hourlyRate);

        if (success) {
            JOptionPane.showMessageDialog(
                    this, "Booking updated successfully!");
            bookingIdField.setText("");
            loadBookings();
        } else {
            JOptionPane.showMessageDialog(
                    this, "Could not update booking.");
        }

    } catch (SQLException | IllegalArgumentException ex) {
        JOptionPane.showMessageDialog(
                this,
                "Error: " + ex.getMessage(),
                "Update Booking Error",
                JOptionPane.ERROR_MESSAGE);
    }
}

}