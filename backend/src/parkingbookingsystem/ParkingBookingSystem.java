package parkingbookingsystem;

import javax.swing.SwingUtilities;

public class ParkingBookingSystem {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new BookingsFrame().setVisible(true);
        });
    }
}
