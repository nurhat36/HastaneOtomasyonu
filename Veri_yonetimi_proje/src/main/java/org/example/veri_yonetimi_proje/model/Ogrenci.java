package org.example.veri_yonetimi_proje.model;



public class Ogrenci {
    private String isim;
    private String soyad;
    private int ogrNo;
    private float gano;
    private int bolumSira;
    private int sinifSira;
    private int sinif;
    private char cinsiyet; // 'E' veya 'K'

    public Ogrenci() {}

    public Ogrenci(String isim, String soyad, int ogrNo, float gano,
                   int bolumSira, int sinifSira, int sinif, char cinsiyet) {
        this.isim = isim;
        this.soyad = soyad;
        this.ogrNo = ogrNo;
        this.gano = gano;
        this.bolumSira = bolumSira;
        this.sinifSira = sinifSira;
        this.sinif = sinif;
        this.cinsiyet = cinsiyet;
    }

    // getters / setters
    public String getIsim() { return isim; }
    public void setIsim(String isim) { this.isim = isim; }
    public String getSoyad() { return soyad; }
    public void setSoyad(String soyad) { this.soyad = soyad; }
    public int getOgrNo() { return ogrNo; }
    public void setOgrNo(int ogrNo) { this.ogrNo = ogrNo; }
    public float getGano() { return gano; }
    public void setGano(float gano) { this.gano = gano; }
    public int getBolumSira() { return bolumSira; }
    public void setBolumSira(int bolumSira) { this.bolumSira = bolumSira; }
    public int getSinifSira() { return sinifSira; }
    public void setSinifSira(int sinifSira) { this.sinifSira = sinifSira; }
    public int getSinif() { return sinif; }
    public void setSinif(int sinif) { this.sinif = sinif; }
    public char getCinsiyet() { return cinsiyet; }
    public void setCinsiyet(char cinsiyet) { this.cinsiyet = cinsiyet; }

    @Override
    public String toString() {
        // CSV benzeri format: isim,soyad,ogrNo,gano,bolumSira,sinifSira,sinif,cinsiyet
        return String.format("%s,%s,%d,%.2f,%d,%d,%d,%c",
                isim, soyad, ogrNo, gano, bolumSira, sinifSira, sinif, cinsiyet);
    }

    public static Ogrenci fromCsv(String line) {
        String[] p = line.split(",");
        if (p.length < 8) return null;
        return new Ogrenci(
                p[0], p[1], Integer.parseInt(p[2]), Float.parseFloat(p[3]),
                Integer.parseInt(p[4]), Integer.parseInt(p[5]), Integer.parseInt(p[6]),
                p[7].charAt(0)
        );
    }
}

