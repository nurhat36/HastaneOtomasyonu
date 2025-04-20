package org.example.hastaneotomasyonu.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.hastaneotomasyonu.Algorithm.HastaHeap;
import org.example.hastaneotomasyonu.models.Hasta;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class HelloController {

    @FXML
    private TextField txtAd;
    @FXML
    private TextField txtYas;
    @FXML
    private ComboBox<String> comboCinsiyet;
    @FXML
    private CheckBox checkMahkum;
    @FXML
    private TextField txtEngelli;
    @FXML
    private ComboBox<String> comboKanama;
    @FXML
    private TextField txtSaat;
    @FXML
    private Label lblSonuc;
    HastaHeap HastaHeap;

    @FXML
    public void initialize() {
        HastaHeap = new HastaHeap(100); // kapasiteyi ihtiyacınıza göre ayarlayabilirsiniz
        verileriHeapEkle(HastaHeap);

        // İlk hastayı çıkaralım
        while (!HastaHeap.bosMu()) {
            Hasta hasta = HastaHeap.cikar();
            System.out.println("Muayene edilecek: " + hasta.hastaAdi + " - Öncelik: " + hasta.oncelikPuani);
        }
        // ComboBox seçenekleri ekleniyor
        comboCinsiyet.getItems().addAll("Erkek", "Kadın", "Diğer");
        comboKanama.getItems().addAll("Yok", "Kanama", "AgirKanama");
    }
    public void verileriHeapEkle(HastaHeap heap) {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(Objects.requireNonNull(getClass().getResource("Hasta.txt")).openStream(), StandardCharsets.UTF_8))) {

            String line;
            while ((line = br.readLine()) != null) {
                String[] parcalar = line.split(",");
                if (parcalar.length >= 8) {
                    String ad = parcalar[1].trim();
                    int yas = Integer.parseInt(parcalar[2].trim());
                    String cinsiyet = parcalar[3].trim();
                    boolean mahkum = Boolean.parseBoolean(parcalar[4].trim());
                    int engelli = Integer.parseInt(parcalar[5].trim());
                    String kanama = parcalar[6].trim().toLowerCase();
                    double kayitSaati = Double.parseDouble(parcalar[7].trim().replace(".", ","));

                    Hasta hasta = new Hasta(ad, yas, cinsiyet, mahkum, engelli, kanama, kayitSaati);
                    hasta.oncelikPuaniHesapla();
                    hasta.muayeneSuresiHesapla();

                    heap.ekle(hasta);
                }
            }

            System.out.println("Başlangıç hastaları heap'e eklendi. Toplam hasta: " + heap.boyut());

        } catch (Exception e) {
            System.err.println("Veriler okunurken hata oluştu: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void hastaEkle() {
        try {
            String ad = txtAd.getText();
            int yas = Integer.parseInt(txtYas.getText());
            String cinsiyet = comboCinsiyet.getValue();
            boolean mahkum = checkMahkum.isSelected();
            int engelli = Integer.parseInt(txtEngelli.getText());
            String kanama = comboKanama.getValue();
            double saat = Double.parseDouble(txtSaat.getText());

            // Hasta nesnesi oluştur
            org.example.hastaneotomasyonu.models.Hasta yeniHasta =
                    new org.example.hastaneotomasyonu.models.Hasta(ad, yas, cinsiyet, mahkum, engelli, kanama, saat);
            yeniHasta.oncelikPuaniHesapla();
            yeniHasta.muayeneSuresiHesapla();

            // Sonucu ekrana yazdır
            lblSonuc.setText("Hasta eklendi! Öncelik: " + yeniHasta.oncelikPuani + ", Süre: " + yeniHasta.muayeneSuresi);
        } catch (Exception e) {
            lblSonuc.setText("Hata: " + e.getMessage());
        }
    }
    @FXML
    private void tumHastalariGoster() {
        StringBuilder sb = new StringBuilder();

        // Geçici bir heap kopyasıyla çalışalım, orijinali bozulmasın
        HastaHeap geciciHeap = new HastaHeap(HastaHeap.boyut());
        for (int i = 0; i < HastaHeap.boyut(); i++) {
            geciciHeap.ekle(HastaHeap.heap[i]);
        }

        while (!geciciHeap.bosMu()) {
            Hasta hasta = geciciHeap.cikar();
            sb.append(hasta.hastaAdi)
                    .append(" - Öncelik: ")
                    .append(hasta.oncelikPuani)
                    .append("\n");
        }

        lblSonuc.setText(sb.toString());
    }

}
