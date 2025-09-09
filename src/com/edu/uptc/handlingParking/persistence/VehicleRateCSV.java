package com.edu.uptc.handlingParking.persistence;

import com.edu.uptc.handlingParking.model.VehicleRate;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VehicleRateCSV {
    private String path;

    public VehicleRateCSV(String path) {
        this.path = path;
    }

    public List<VehicleRate> loadRates() {
        List<VehicleRate> rates = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length == 2) {
                    String type = parts[0];
                    double price = Double.parseDouble(parts[1]);
                    rates.add(new VehicleRate(type, price));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rates;
    }
}
