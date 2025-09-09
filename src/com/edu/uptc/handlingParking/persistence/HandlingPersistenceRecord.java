package com.edu.uptc.handlingParking.persistence;

import com.edu.uptc.handlingParking.model.RecordParking;
import com.edu.uptc.handlingParking.enums.ETypeFileEnum;
import com.edu.uptc.handlingParking.interfaces.IActionsFile;
import com.edu.uptc.handlingParking.logic.Config;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class HandlingPersistenceRecord implements IActionsFile {

    private List<RecordParking> listRecords;
    private Config config;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public HandlingPersistenceRecord() {
        listRecords = new ArrayList<>();
        this.config = new Config("data/appconfig.properties");
    }

    
    public Boolean addRecord(RecordParking record) {
        this.listRecords.add(record);
        return Boolean.TRUE;
    }

    public RecordParking findRecordByPlate(String plate) {
        for (RecordParking r : this.listRecords) {
            if (r.getLicensePlate().equalsIgnoreCase(plate)) {
                return r;
            }
        }
        return null;
    }

    public void deleteRecord(String plate) {
        this.listRecords = this.listRecords.stream()
                .filter(r -> !r.getLicensePlate().equalsIgnoreCase(plate))
                .collect(Collectors.toList());
    }

    public List<RecordParking> getListRecords() {
        return listRecords;
    }

    public void setListRecords(List<RecordParking> listRecords) {
        this.listRecords = listRecords;
    }

    
    @Override
    public void loadFile(ETypeFileEnum eTypeFileEnum) {
        if (eTypeFileEnum == ETypeFileEnum.JSON) {
            loadFileJSON();
        } else if (eTypeFileEnum == ETypeFileEnum.XML) {
            loadFileXML();
        } else if (eTypeFileEnum == ETypeFileEnum.CSV || eTypeFileEnum == ETypeFileEnum.PLAIN) {
            loadFilePlain(eTypeFileEnum == ETypeFileEnum.CSV ? "records.csv" : "records.txt");
        }
    }

    @Override
    public void dumpFile(ETypeFileEnum eTypeFile) {
        if (eTypeFile == ETypeFileEnum.PLAIN) {
            dumpFilePlain("records.txt");
        } else if (eTypeFile == ETypeFileEnum.CSV) {
            dumpFilePlain("records.csv");
        } else if (eTypeFile == ETypeFileEnum.JSON) {
            dumpFileJSON();
        } else if (eTypeFile == ETypeFileEnum.XML) {
            dumpFileXML();
        }
    }

    
    private void dumpFileXML() {
        String rutaArchivo = "data/records.xml";
        List<String> records = new ArrayList<>();
        records.add("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        records.add("<records>");
        for (RecordParking r : this.listRecords) {
            records.add("  <record>");
            records.add("    <licensePlate>" + escape(r.getLicensePlate()) + "</licensePlate>");
            records.add("    <entryTime>" + r.getEntryTime().format(formatter) + "</entryTime>");
            records.add("    <departureTime>" + r.getDepartureTime().format(formatter) + "</departureTime>");
            records.add("    <total>" + r.getTotal() + "</total>");
            records.add("  </record>");
        }
        records.add("</records>");
        this.writer(rutaArchivo, records);
    }

    private void loadFileXML() {
        List<String> lines = reader("data/records.xml");
        if (lines.isEmpty()) return;
        RecordParking tmp = null;
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("<record")) {
                tmp = new RecordParking();
            } else if (line.startsWith("<licensePlate>")) {
                tmp.setLicensePlate(unwrapTag(line, "licensePlate"));
            } else if (line.startsWith("<entryTime>")) {
                tmp.setEntryTime(LocalDate.parse(unwrapTag(line, "entryTime"), formatter));
            } else if (line.startsWith("<departureTime>")) {
                tmp.setDepartureTime(LocalDate.parse(unwrapTag(line, "departureTime"), formatter));
            } else if (line.startsWith("<total>")) {
                tmp.setTotal(Double.parseDouble(unwrapTag(line, "total")));
            } else if (line.startsWith("</record")) {
                if (tmp != null) {
                    this.listRecords.add(tmp);
                    tmp = null;
                }
            }
        }
    }

    
    private void dumpFileJSON() {
        String rutaArchivo = "data/records.json";
        List<String> content = new ArrayList<>();
        content.add("[");
        int total = listRecords.size();
        int contador = 0;

        for (RecordParking r : this.listRecords) {
            StringBuilder json = new StringBuilder();
            json.append("{");
            json.append("\"licensePlate\":\"").append(escape(r.getLicensePlate())).append("\",");
            json.append("\"entryTime\":\"").append(r.getEntryTime().format(formatter)).append("\",");
            json.append("\"departureTime\":\"").append(r.getDepartureTime().format(formatter)).append("\",");
            json.append("\"total\":").append(r.getTotal());
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
        List<String> lines = reader("data/records.json");
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
            String plate = map.getOrDefault("licensePlate", "");
            LocalDate entry = LocalDate.parse(map.getOrDefault("entryTime", "01/01/1970"), formatter);
            LocalDate departure = LocalDate.parse(map.getOrDefault("departureTime", "01/01/1970"), formatter);
            double total = Double.parseDouble(map.getOrDefault("total", "0"));
            this.listRecords.add(new RecordParking(plate, entry, departure, total));
        }
    }


    private void dumpFilePlain(String nameFile) {
        String rutaArchivo = "data/" + nameFile;
        List<String> records = new ArrayList<>();
        for (RecordParking r : this.listRecords) {
            StringBuilder sb = new StringBuilder();
            sb.append(r.getLicensePlate()).append(";");
            sb.append(r.getEntryTime().format(formatter)).append(";");
            sb.append(r.getDepartureTime().format(formatter)).append(";");
            sb.append(r.getTotal());
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
                LocalDate entry = LocalDate.parse(tokens.nextToken(), formatter);
                LocalDate departure = LocalDate.parse(tokens.nextToken(), formatter);
                double total = Double.parseDouble(tokens.nextToken());
                this.listRecords.add(new RecordParking(plate, entry, departure, total));
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
