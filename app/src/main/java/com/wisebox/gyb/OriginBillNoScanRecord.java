package com.wisebox.gyb;

public class OriginBillNoScanRecord {
    private String BillNo;
    private String OriginBillNo;
    private String ScannedByCode;
    private String ScannedByName;
    private String ScannedAt;

    public String getBillNo() {
        return BillNo;
    }

    public void setBillNo(String billNo) {
        BillNo = billNo;
    }

    public String getOriginBillNo() {
        return OriginBillNo;
    }

    public void setOriginBillNo(String originBillNo) {
        OriginBillNo = originBillNo;
    }

    public String getScannedByCode() {
        return ScannedByCode;
    }

    public void setScannedByCode(String scannedByCode) {
        ScannedByCode = scannedByCode;
    }

    public String getScannedByName() {
        return ScannedByName;
    }

    public void setScannedByName(String scannedByName) {
        ScannedByName = scannedByName;
    }

    public String getScannedAt() {
        return ScannedAt;
    }

    public void setScannedAt(String scannedAt) {
        ScannedAt = scannedAt;
    }
}
