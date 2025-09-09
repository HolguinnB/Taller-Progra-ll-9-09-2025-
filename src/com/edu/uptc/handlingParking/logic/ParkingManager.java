package com.edu.uptc.handlingParking.logic;

import com.edu.uptc.handlingParking.model.*;
import com.edu.uptc.handlingParking.persistence.*;
import com.edu.uptc.handlingParking.enums.ETypeFileEnum;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class ParkingManager {

    private VehicleRateCSV rateCSV;
    private UserSerialization userSerialization;
    private HandlingPersistenceVehicle vehiclePersistence;
    private HandlingPersistenceRecord recordPersistence;
    private Config config;

       public ParkingManager(String ratesPath, String usersPath, String configPath) {
        this.rateCSV = new VehicleRateCSV(ratesPath);
        this.userSerialization = new UserSerialization(usersPath);
        this.vehiclePersistence = new HandlingPersistenceVehicle();
        this.recordPersistence = new HandlingPersistenceRecord();
        this.config = new Config(configPath);

      
        this.vehiclePersistence.loadFile(ETypeFileEnum.XML);   
        this.vehiclePersistence.loadFile(ETypeFileEnum.JSON);  
        this.recordPersistence.loadFile(ETypeFileEnum.JSON);   
        this.recordPersistence.loadFile(ETypeFileEnum.XML);   
    }

    // login
    public boolean login(String username, String password) {
        return userSerialization.loadUsers()
                .stream()
                .anyMatch(u -> u.getUserName().equals(username) && u.getPassword().equals(password));
    }

   
    public void addVehicle(Vehicle vehicle) throws Exception {
        List<Vehicle> vehicles = vehiclePersistence.getListVehicles();

        // Validar capacidad
        if (vehicles.size() >= config.getMaxCapacity()) {
            throw new Exception("No hay cupos disponibles en el parqueadero.");
        }
        vehicle.setPricePerHour(getPriceForType(vehicle.getTypeVehicle())); 
        vehiclePersistence.addVehicle(vehicle);

        // Validar placa repetida
        if (vehiclePersistence.findVehicleByPlate(vehicle.getLicensePlate()) != null) {
            throw new Exception("Ya existe un vehículo con esa placa.");
        }

        vehiclePersistence.addVehicle(vehicle);
        vehiclePersistence.dumpFile(ETypeFileEnum.XML);  
        vehiclePersistence.dumpFile(ETypeFileEnum.JSON); 
    }

    public List<Vehicle> getVehicles() {
        return vehiclePersistence.getListVehicles();
    }

    public void updateVehicle(Vehicle updated) {
        vehiclePersistence.deleteVehicle(updated.getLicensePlate());
        vehiclePersistence.addVehicle(updated);
        vehiclePersistence.dumpFile(ETypeFileEnum.XML);
        vehiclePersistence.dumpFile(ETypeFileEnum.JSON);
    }

    public void deleteVehicle(String licensePlate) {
        vehiclePersistence.deleteVehicle(licensePlate);
        vehiclePersistence.dumpFile(ETypeFileEnum.XML);
        vehiclePersistence.dumpFile(ETypeFileEnum.JSON);
    }


    public void addRecord(String licensePlate, LocalDate entry, LocalDate departure) throws Exception {
        Vehicle vehicle = vehiclePersistence.findVehicleByPlate(licensePlate);
        if (vehicle == null) {
            throw new Exception("Vehículo no encontrado.");
        }

        long hours = ChronoUnit.HOURS.between(entry.atStartOfDay(), departure.atStartOfDay());
        if (hours <= 0) hours = 1; // como minimo 1 hora

        double total = vehicle.getPricePerHour() * hours;

        recordPersistence.addRecord(new RecordParking(licensePlate, entry, departure, total));
        recordPersistence.dumpFile(ETypeFileEnum.JSON);
        recordPersistence.dumpFile(ETypeFileEnum.XML);
    }

    public List<RecordParking> getRecords() {
        return recordPersistence.getListRecords();
    }

    public void updateRecord(RecordParking updated) {
        recordPersistence.deleteRecord(updated.getLicensePlate());
        recordPersistence.addRecord(updated);
        recordPersistence.dumpFile(ETypeFileEnum.JSON);
        recordPersistence.dumpFile(ETypeFileEnum.XML);
    }

    public void deleteRecord(String licensePlate) {
        recordPersistence.deleteRecord(licensePlate);
        recordPersistence.dumpFile(ETypeFileEnum.JSON);
        recordPersistence.dumpFile(ETypeFileEnum.XML);
    }

    
    public long countVehiclesByDate(LocalDate date) {
        return recordPersistence.getListRecords()
                .stream()
                .filter(r -> r.getEntryTime().equals(date))
                .count();
    }

    public double totalRevenueByDate(LocalDate date) {
        return recordPersistence.getListRecords()
                .stream()
                .filter(r -> r.getEntryTime().equals(date))
                .mapToDouble(RecordParking::getTotal)
                .sum();
    }


    public List<User> getUsers() {
    return userSerialization.loadUsers();
}


    public void saveUsers(List<User> users) {
    userSerialization.saveUsers(users);
}

    public double getPriceForType(String type) {
        return rateCSV.loadRates()
                      .stream()
                      .filter(r -> r.getTypeVehicle().equalsIgnoreCase(type))
                      .mapToDouble(VehicleRate::getPrice)
                      .findFirst()
                      .orElse(0.0);
    }

    
    
}
