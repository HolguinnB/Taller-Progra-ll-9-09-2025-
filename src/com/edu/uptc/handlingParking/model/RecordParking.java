package com.edu.uptc.handlingParking.model;

import java.time.LocalDateTime;

public class RecordParking {
    private String licensePlate;
    private LocalDateTime entryTime;
    private LocalDateTime departureTime;
    private double total;

    public RecordParking() {}

    public RecordParking(String licensePlate, LocalDateTime entryTime, LocalDateTime departureTime, double total) {
        this.licensePlate = licensePlate;
        this.entryTime = entryTime;
        this.departureTime = departureTime;
        this.total = total;
    }

    public String getLicensePlate() { return licensePlate; }
    public void setLicensePlate(String licensePlate) { this.licensePlate = licensePlate; }

    public LocalDateTime getEntryTime() { return entryTime; }
    public void setEntryTime(LocalDateTime entryTime) { this.entryTime = entryTime; }

    public LocalDateTime getDepartureTime() { return departureTime; }
    public void setDepartureTime(LocalDateTime departureTime) { this.departureTime = departureTime; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    @Override
    public String toString() {
        return "Record [" +
                "Placa='" + licensePlate + '\'' +
                ", Entrada=" + entryTime +
                ", Salida=" + departureTime +
                ", Total=" + total +
                ']';
    }

}
