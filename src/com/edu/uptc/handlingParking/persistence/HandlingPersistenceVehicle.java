package com.edu.uptc.handlingParking.persistence;

import com.edu.uptc.handlingParking.model.Vehicle;
import com.edu.uptc.handlingParking.enums.ETypeFileEnum;
import com.edu.uptc.handlingParking.interfaces.IActionsFile;
import com.edu.uptc.handlingParking.logic.Config;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.time.format.DateTimeFormatter;

public class HandlingPersistenceVehicle implements IActionsFile {

    private List<Vehicle> listVehicles;
    private Config config;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public HandlingPersistenceVehicle() {
        listVehicles = new ArrayList<>();
        this.config = new Config("data/appconfig.properties");
    }


    public Boolean addVehicle(Vehicle vehicle) {
        if (Objects.isNull(this.findVehicleByPlate(vehicle.getLicensePlate()))) {
            this.listVehicles.add(vehicle);
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    public Vehicle findVehicleByPlate(String plate) {
        for (Vehicle v : this.listVehicles) {
            if (v.getLicensePlate().equalsIgnoreCase(plate)) {
                return v;
            }
        }
        return null;
    }

    public void deleteVehicle(String plate) {
        this.listVehicles = this.listVehicles.stream()
                .filter(v -> !v.getLicensePlate().equalsIgnoreCase(plate))
                .collect(Collectors.toList());
    }

    public List<Vehicle> getListVehicles() {
        return listVehicles;
    }

    public void setListVehicles(List<Vehicle> listVehicles) {
        this.listVehicles = listVehicles;
    }


    @Override
    public void loadFile(ETypeFileEnum eTypeFileEnum) {

        if (eTypeFileEnum == ETypeFileEnum.JSON) {
            loadFileJSON();
        } else if (eTypeFileEnum == ETypeFileEnum.XML) {
            loadFileXML();
        } else if (eTypeFileEnum == ETypeFileEnum.CSV || eTypeFileEnum == ETypeFileEnum.PLAIN) {
            loadFilePlain(eTypeFileEnum == ETypeFileEnum.CSV ? "vehicles.csv" : "vehicles.txt");
        }
    }

    @Override
    public void dumpFile(ETypeFileEnum eTypeFile) {
        if (eTypeFile == ETypeFileEnum.PLAIN) {
            dumpFilePlain("vehicles.txt");
        } else if (eTypeFile == ETypeFileEnum.CSV) {
            dumpFilePlain("vehicles.csv");
        } else if (eTypeFile == ETypeFileEnum.JSON) {
            dumpFileJSON();
        } else if (eTypeFile == ETypeFileEnum.XML) {
            dumpFileXML();
        }
    }


    private void dumpFileXML() {
        String rutaArchivo = "data/vehicles.xml";
        List<String> records = new ArrayList<>();
        records.add("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        records.add("<vehicles>");
        for (Vehicle v : this.listVehicles) {
            records.add("  <vehicle>");
            records.add("    <licensePlate>" + escape(v.getLicensePlate()) + "</licensePlate>");
            records.add("    <typeVehicle>" + escape(v.getTypeVehicle()) + "</typeVehicle>");
            records.add("    <owner>" + escape(v.getOwner()) + "</owner>");
            records.add("    <model>" + escape(v.getModel()) + "</model>");
            records.add("    <color>" + escape(v.getColor()) + "</color>");
            records.add("    <pricePerHour>" + v.getPricePerHour() + "</pricePerHour>");
            records.add("  </vehicle>");
        }
        records.add("</vehicles>");
        this.writer(rutaArchivo, records);
    }

    private void loadFileXML() {
        List<String> lines = reader("data/vehicles.xml");
        if (lines.isEmpty()) return;

        Vehicle tmp = null;
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("<vehicle")) {
                tmp = new Vehicle();
            } else if (line.startsWith("<licensePlate>")) {
                tmp.setLicensePlate(unwrapTag(line, "licensePlate"));
            } else if (line.startsWith("<typeVehicle>")) {
                tmp.setTypeVehicle(unwrapTag(line, "typeVehicle"));
            } else if (line.startsWith("<owner>")) {
                tmp.setOwner(unwrapTag(line, "owner"));
            } else if (line.startsWith("<model>")) {
                tmp.setModel(unwrapTag(line, "model"));
            } else if (line.startsWith("<color>")) {
                tmp.setColor(unwrapTag(line, "color"));
            } else if (line.startsWith("<pricePerHour>")) {
                tmp.setPricePerHour(Double.parseDouble(unwrapTag(line, "pricePerHour")));
            } else if (line.startsWith("</vehicle")) {
                if (tmp != null) {
                    this.listVehicles.add(tmp);
                    tmp = null;
                }
            }
        }
    }


    private void dumpFileJSON() {
        String rutaArchivo = "data/vehicles.json";
        List<String> content = new ArrayList<>();
        content.add("[");
        int total = listVehicles.size();
        int contador = 0;

        for (Vehicle v : this.listVehicles) {
            StringBuilder json = new StringBuilder();
            json.append("{");
            json.append("\"licensePlate\":\"").append(escape(v.getLicensePlate())).append("\",");
            json.append("\"typeVehicle\":\"").append(escape(v.getTypeVehicle())).append("\",");
            json.append("\"owner\":\"").append(escape(v.getOwner())).append("\",");
            json.append("\"model\":\"").append(escape(v.getModel())).append("\",");
            json.append("\"color\":\"").append(escape(v.getColor())).append("\",");
            json.append("\"pricePerHour\":").append(v.getPricePerHour());
            json.append("}");
            contador++;
            if (contador < total) {
                json.append(",");
            }
            content.add(json.toString());
        }
        content.add("]");
        this.writer(rutaArchivo, content);
    }

    private void loadFileJSON() {
        List<String> lines = reader("data/vehicles.json");
        if (lines.isEmpty()) return;
        String all = String.join("", lines).trim();
        if (all.length() < 2) return;

        all = all.substring(all.indexOf('[') + 1, all.lastIndexOf(']')).trim();
        if (all.isEmpty()) return;
        String[] objects = all.split("\\},\\s*\\{");
        for (String obj : objects) {
            String o = obj.trim();
            if (!o.startsWith("{")) o = "{" + o;
            if (!o.endsWith("}")) o = o + "}";
            Map<String, String> map = parseSimpleJsonObject(o);
            Vehicle v = new Vehicle();
            v.setLicensePlate(map.getOrDefault("licensePlate", ""));
            v.setTypeVehicle(map.getOrDefault("typeVehicle", ""));
            v.setOwner(map.getOrDefault("owner", ""));
            v.setModel(map.getOrDefault("model", ""));
            v.setColor(map.getOrDefault("color", ""));
            v.setPricePerHour(Double.parseDouble(map.getOrDefault("pricePerHour", "0")));
            this.listVehicles.add(v);
        }
    }


    private void dumpFilePlain(String nameFile) {
        String rutaArchivo = "data/" + nameFile;
        List<String> records = new ArrayList<>();
        for (Vehicle v : this.listVehicles) {
            StringBuilder sb = new StringBuilder();
            sb.append(v.getLicensePlate()).append(";");
            sb.append(v.getTypeVehicle()).append(";");
            sb.append(v.getOwner()).append(";");
            sb.append(v.getModel()).append(";");
            sb.append(v.getColor()).append(";");
            sb.append(v.getPricePerHour());
            records.add(sb.toString());
        }
        this.writer(rutaArchivo, records);
    }

    private void loadFilePlain(String nameFile) {
        List<String> content = this.reader("data/" + nameFile);
        content.forEach(row -> {
            if (row.trim().isEmpty()) return;
            StringTokenizer tokens = new StringTokenizer(row, ";");
            try {
                String plate = tokens.nextToken();
                String type = tokens.nextToken();
                String owner = tokens.nextToken();
                String model = tokens.nextToken();
                String color = tokens.nextToken();
                double price = Double.parseDouble(tokens.nextToken());
                this.listVehicles.add(new Vehicle(plate, type, owner, model, color, price));
            } catch (Exception e) {

            }
        });
    }


    private void writer(String ruta, List<String> content) {

        File f = new File("data");
        if (!f.exists()) f.mkdirs();
        try (PrintWriter pw = new PrintWriter(new FileWriter(ruta))) {
            for (String line : content) {
                pw.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<String> reader(String ruta) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(ruta))) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {

        }
        return lines;
    }

    private static String unwrapTag(String line, String tag) {
        return line.replace("<" + tag + ">", "").replace("</" + tag + ">", "").trim();
    }

    private static String escape(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static Map<String, String> parseSimpleJsonObject(String obj) {
        Map<String, String> map = new HashMap<>();

        String inner = obj.trim();
        if (inner.startsWith("{")) inner = inner.substring(1);
        if (inner.endsWith("}")) inner = inner.substring(0, inner.length()-1);
        String[] parts = inner.split("\",\\s*\"");
        for (String p : parts) {
            p = p.trim();
            p = p.replaceAll("^\"|\"$", "");
            String[] kv = p.split("\":");
            if (kv.length < 2) continue;
            String key = kv[0].replaceAll("^\"|\"$", "").trim();
            String val = kv[1].trim();
            val = val.replaceAll("^\"|\"$", "");
            map.put(key, val);
        }
        return map;
    }
}
