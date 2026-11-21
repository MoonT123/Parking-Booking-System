package model;

import java.math.BigDecimal;
import java.sql.Time;

public class Tariff {

    private int tariffID;
    private String dayOfWeek;
    private Time startHour;
    private Time endHour;
    private BigDecimal hourlyRate;

    public Tariff() {
    }

    public Tariff(int tariffID, String dayOfWeek,
                  Time startHour, Time endHour,
                  BigDecimal hourlyRate) {
        this.tariffID = tariffID;
        this.dayOfWeek = dayOfWeek;
        this.startHour = startHour;
        this.endHour = endHour;
        this.hourlyRate = hourlyRate;
    }

    public int getTariffID() {
        return tariffID;
    }

    public void setTariffID(int tariffID) {
        this.tariffID = tariffID;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public Time getStartHour() {
        return startHour;
    }

    public void setStartHour(Time startHour) {
        this.startHour = startHour;
    }

    public Time getEndHour() {
        return endHour;
    }

    public void setEndHour(Time endHour) {
        this.endHour = endHour;
    }

    public BigDecimal getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(BigDecimal hourlyRate) {
        this.hourlyRate = hourlyRate;
    }
}
