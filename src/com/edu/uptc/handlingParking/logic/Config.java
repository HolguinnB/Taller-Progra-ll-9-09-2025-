package com.edu.uptc.handlingParking.logic;

import java.io.FileInputStream;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.io.IOException;
import java.util.Properties;

public class Config {

    public static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private int maxCapacity;

    public Config(String path) {
        loadConfig(path);
    }

    private void loadConfig(String path) {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(path)) {
            props.load(fis);
            this.maxCapacity = Integer.parseInt(props.getProperty("capacity", "20")); // defecto 20
        } catch (IOException e) {
            e.printStackTrace();
            this.maxCapacity = 10;
        }
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }
}
