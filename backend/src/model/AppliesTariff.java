package model;

import java.sql.Date;

public class AppliesTariff {

    private int lotId;
    private int tariffID;
    private Date effectiveDate;

    public AppliesTariff() {
    }

    public AppliesTariff(int lotId, int tariffID, Date effectiveDate) {
        this.lotId = lotId;
        this.tariffID = tariffID;
        this.effectiveDate = effectiveDate;
    }

    public int getLotId() {
        return lotId;
    }

    public void setLotId(int lotId) {
        this.lotId = lotId;
    }

    public int getTariffID() {
        return tariffID;
    }

    public void setTariffID(int tariffID) {
        this.tariffID = tariffID;
    }

    public Date getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(Date effectiveDate) {
        this.effectiveDate = effectiveDate;
    }
}
