package org.example.hastaneotomasyonu.models;

public class Hasta implements Comparable<Hasta> {
    public int hastaNo;
    public String hastaAdi;
    public int hastaYasi;
    public String cinsiyet;
    public boolean mahkumlukDurumBilgisi;
    public int engellilikOrani;
    public String kanamaliHastaDurumBilgisi;
    public double hastaKayitSaati;
    public double muayeneSaati;
    public int muayeneSuresi;
    public int oncelikPuani;

    public Hasta(String hastaAdi, int hastaYasi, String cinsiyet, boolean mahkum, int engelli, String kanama, double kayitSaati) {
        this.hastaAdi = hastaAdi;
        this.hastaYasi = hastaYasi;
        this.cinsiyet = cinsiyet;
        this.mahkumlukDurumBilgisi = mahkum;
        this.engellilikOrani = engelli;
        this.kanamaliHastaDurumBilgisi = kanama;
        this.hastaKayitSaati = kayitSaati;
    }

    public void oncelikPuaniHesapla() {
        int yasPuani = 0;
        if (hastaYasi < 5) yasPuani = 20;
        else if (hastaYasi < 45) yasPuani = 0;
        else if (hastaYasi < 65) yasPuani = 15;
        else yasPuani = 25;

        int mahkumPuani = mahkumlukDurumBilgisi ? 50 : 0;
        int engelliPuani = engellilikOrani;
        int kanamaPuani = switch (kanamaliHastaDurumBilgisi.toLowerCase()) {
            case "kanama" -> 20;
            case "agirkanama" -> 50;
            default -> 0;
        };

        this.oncelikPuani = yasPuani + mahkumPuani + engelliPuani + kanamaPuani;
    }

    public void muayeneSuresiHesapla() {
        int sure = 10;
        if (hastaYasi >= 65) sure += 15;
        int engelliSure = engellilikOrani >= 80 ? 20 : engellilikOrani >= 50 ? 10 : 0;
        int kanamaSure = switch (kanamaliHastaDurumBilgisi.toLowerCase()) {
            case "kanama" -> 10;
            case "agirkanama" -> 20;
            default -> 0;
        };

        this.muayeneSuresi = sure + engelliSure + kanamaSure;
    }

    @Override
    public int compareTo(Hasta other) {
        return Integer.compare(other.oncelikPuani, this.oncelikPuani); // max-heap için
    }

    // --- GÖSTERİM İÇİN GEREKEN GETTER'LAR ---

    public String getHastaAdi() {
        return hastaAdi;
    }

    public int getOncelikPuani() {
        return oncelikPuani;
    }

    public double getMuayeneSaati() {
        return muayeneSaati;
    }

    public int getMuayeneSuresi() {
        return muayeneSuresi;
    }
}
