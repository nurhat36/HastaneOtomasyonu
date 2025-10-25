package org.example.veri_yonetimi_proje.storage;

import org.example.veri_yonetimi_proje.model.Ogrenci;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileManager {
    private final File dataFile;

    public FileManager(String path) {
        this.dataFile = new File(path);
    }

    // Tüm kayıtları oku (ardışık tarama)
    public List<Ogrenci> readAll() throws IOException {
        List<Ogrenci> list = new ArrayList<>();
        if (!dataFile.exists()) return list;
        try (BufferedReader br = new BufferedReader(new FileReader(dataFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                if (line.startsWith("DELETED,")) continue; // tombstone
                Ogrenci o = Ogrenci.fromCsv(line);
                if (o != null) list.add(o);
            }
        }
        return list;
    }

    // Yeni kayıt ekle (append)
    public void append(Ogrenci o) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(dataFile, true))) {
            bw.write(o.toString());
            bw.newLine();
        }
    }

    // Tümünü tekrar yaz (güncelle/silme sonrası)
    public void overwriteAll(List<Ogrenci> all) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(dataFile, false))) {
            for (Ogrenci o : all) {
                bw.write(o.toString());
                bw.newLine();
            }
        }
    }
}