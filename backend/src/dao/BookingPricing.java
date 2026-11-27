package dao;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import model.Booking;

final class BookingPricing {
    private record Rate(LocalDate effective, String day, long start, long end, BigDecimal hourly) {
        boolean applies(LocalDate date) {
            return !effective.isAfter(date)
                    && (day.equals("ALL") || day.equals(date.getDayOfWeek().name()));
        }
    }

    static BigDecimal total(Connection connection, Booking booking) throws SQLException {
        LocalDateTime start = booking.getStartTime().toLocalDateTime();
        LocalDateTime end = booking.getEndTime().toLocalDateTime();
        List<Rate> rates = new ArrayList<>();
        String sql = """
                SELECT a.effective_date, t.day_of_week, TIME_TO_SEC(t.start_hour),
                       TIME_TO_SEC(t.end_hour), t.hourly_rate
                FROM APPLIESTARIF a JOIN TARIFF t ON t.Tariff_ID = a.Tariff_ID
                WHERE a.lot_id = ? AND a.effective_date <= ?
                ORDER BY (t.day_of_week = 'ALL'), a.effective_date DESC, t.Tariff_ID DESC
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, booking.getLotId());
            statement.setObject(2, end.toLocalDate());
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    rates.add(new Rate(rows.getObject(1, LocalDate.class), rows.getString(2),
                            rows.getLong(3), rows.getLong(4), rows.getBigDecimal(5)));
                }
            }
        }

        BigDecimal weightedNanos = BigDecimal.ZERO;
        LocalDateTime cursor = start;
        while (cursor.isBefore(end)) {
            LocalDate date = cursor.toLocalDate();
            LocalDateTime midnight = date.atStartOfDay();
            LocalDateTime nextMidnight = date.plusDays(1).atStartOfDay();
            LocalDateTime dayEnd = end.isBefore(nextMidnight) ? end : nextMidnight;
            List<Rate> applicable = rates.stream().filter(rate -> rate.applies(date)).toList();
            TreeSet<LocalDateTime> boundaries = new TreeSet<>();
            boundaries.add(dayEnd);
            for (Rate rate : applicable) {
                LocalDateTime from = midnight.plusSeconds(rate.start());
                LocalDateTime to = midnight.plusSeconds(rate.end());
                if (from.isAfter(cursor) && from.isBefore(dayEnd)) {
                    boundaries.add(from);
                }
                if (to.isAfter(cursor) && to.isBefore(dayEnd)) {
                    boundaries.add(to);
                }
            }
            for (LocalDateTime boundary : boundaries) {
                final long second = cursor.toLocalTime().toSecondOfDay();
                Rate rate = applicable.stream()
                        .filter(candidate -> candidate.start() <= second && second < candidate.end())
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException(
                                "No applicable tariff on " + date + " at second " + second));
                long nanos = Duration.between(cursor, boundary).toNanos();
                weightedNanos = weightedNanos.add(rate.hourly().multiply(BigDecimal.valueOf(nanos)));
                cursor = boundary;
            }
        }
        return weightedNanos.divide(BigDecimal.valueOf(Duration.ofHours(1).toNanos()), 2,
                RoundingMode.HALF_UP);
    }
}
