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
    public Hasta(Hasta other) {
        this.hastaAdi = other.hastaAdi;
        this.hastaYasi = other.hastaYasi;
        this.cinsiyet = other.cinsiyet;
        this.mahkumlukDurumBilgisi = other.mahkumlukDurumBilgisi;
        this.engellilikOrani = other.engellilikOrani;
        this.kanamaliHastaDurumBilgisi = other.kanamaliHastaDurumBilgisi;
        this.hastaKayitSaati = other.hastaKayitSaati;
        this.oncelikPuani = other.oncelikPuani;
        this.muayeneSuresi = other.muayeneSuresi;
        this.muayeneSaati = other.muayeneSaati;
    }

    public void oncelikPuaniHesapla() {

        int yasPuani;
        if (hastaYasi >= 0 && hastaYasi < 5) {
            yasPuani = 20;
        } else if (hastaYasi >= 5 && hastaYasi < 45) {
            yasPuani = 0;
        } else if (hastaYasi >= 45 && hastaYasi < 65) {
            yasPuani = 15;
        } else if (hastaYasi >= 65) {
            yasPuani = 25;
        } else {
            yasPuani = 0;
        }


        int mahkumPuani = mahkumlukDurumBilgisi ? 50 : 0;


        int engelliPuani = engellilikOrani / 4;


        int kanamaPuani = 0;
        if (kanamaliHastaDurumBilgisi != null) {
            switch (kanamaliHastaDurumBilgisi.toLowerCase()) {
                case "kanama":
                    kanamaPuani = 20;
                    break;
                case "agirkanama":
                    kanamaPuani = 50;
                    break;
                case "kanamayok":
                default:
                    kanamaPuani = 0;
            }
        }


        this.oncelikPuani = yasPuani + mahkumPuani + engelliPuani + kanamaPuani;
    }

    public void muayeneSuresiHesapla() {
        int sure = 10;
        if (hastaYasi < 65) sure += 15;
        int engelliSure = engellilikOrani /5;
        int kanamaSure = switch (kanamaliHastaDurumBilgisi.toLowerCase()) {
            case "kanama" -> 10;
            case "agirkanama" -> 20;
            default -> 0;
        };

        this.muayeneSuresi = sure + engelliSure + kanamaSure;
    }

    @Override
    public int compareTo(Hasta diger) {

        int puanKarsilastirma = Integer.compare(this.oncelikPuani, diger.oncelikPuani);
        if (puanKarsilastirma == 0) {
            return Double.compare(diger.hastaKayitSaati, this.hastaKayitSaati);
        }

        return puanKarsilastirma;
    }






    public String getHastaAdi() {
        return hastaAdi;
    }

    public int getOncelikPuani() {
        return oncelikPuani;
    }

    public double getMuayeneSaati() {
        return muayeneSaati;
    }
    public  void setHastaNo(int hastaNo){
        this.hastaNo = hastaNo;
    }
    public int getHastaNo(){
        return hastaNo;
    }

    public void setMuayeneSaati(double yeniMuayeneSaati) {

        if (yeniMuayeneSaati < hastaKayitSaati) {
            throw new IllegalArgumentException("Muayene saati (" + yeniMuayeneSaati + ") kayıt saatinden önce olamaz. Kayıt Saati: " + hastaKayitSaati);
        } else if (yeniMuayeneSaati<9.00) {
            this.muayeneSaati=(yeniMuayeneSaati%8.00)+9.00;

        }else{
            this.muayeneSaati = yeniMuayeneSaati;

        }

    }
    public double getHastaKayitSaati() {
        return this.hastaKayitSaati;
    }







    public int getMuayeneSuresi() {
        return muayeneSuresi;
    }
}
