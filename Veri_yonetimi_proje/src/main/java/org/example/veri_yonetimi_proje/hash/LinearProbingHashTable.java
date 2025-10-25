package org.example.veri_yonetimi_proje.hash;

import org.example.veri_yonetimi_proje.model.Ogrenci;

public class LinearProbingHashTable implements HashTable {
    private Ogrenci[] table;
    private boolean[] tombstone; // true => silinmiş (boş ama zincir devam etmeli)
    private int M;

    public LinearProbingHashTable(int size) {
        this.M = size;
        table = new Ogrenci[M];
        tombstone = new boolean[M];
    }

    private int hash(int k) {
        return Math.abs(k) % M;
    }

    @Override
    public void insert(Ogrenci o) {
        int idx = hash(o.getOgrNo());
        int start = idx;
        while (table[idx] != null && tombstone[idx] == false) {
            idx = (idx + 1) % M;
            if (idx == start) throw new RuntimeException("Hash table full");
        }
        table[idx] = o;
        tombstone[idx] = false;
    }

    @Override
    public Ogrenci searchByOgrNo(int ogrNo) {
        int idx = hash(ogrNo);
        int start = idx;
        while (table[idx] != null || tombstone[idx]) {
            if (table[idx] != null && table[idx].getOgrNo() == ogrNo)
                return table[idx];
            idx = (idx + 1) % M;
            if (idx == start) break;
        }
        return null;
    }

    @Override
    public boolean delete(int ogrNo) {
        int idx = hash(ogrNo);
        int start = idx;
        while (table[idx] != null || tombstone[idx]) {
            if (table[idx] != null && table[idx].getOgrNo() == ogrNo) {
                table[idx] = null;
                tombstone[idx] = true;
                return true;
            }
            idx = (idx + 1) % M;
            if (idx == start) break;
        }
        return false;
    }

    @Override
    public void showHashTable() {
        for (int i = 0; i < M; i++) {
            System.out.printf("[%d] -> %s %s%n", i,
                    table[i] == null ? "EMPTY" : String.valueOf(table[i].getOgrNo()),
                    tombstone[i] ? "(TOMBSTONE)" : "");
        }
    }
}