package org.example.veri_yonetimi_proje.hash;

import org.example.veri_yonetimi_proje.model.Ogrenci;

public interface HashTable {
    void insert(Ogrenci o);
    Ogrenci searchByOgrNo(int ogrNo);
    boolean delete(int ogrNo); // tombstone veya remove
    void showHashTable(); // konsola veya dosyaya yaz
}