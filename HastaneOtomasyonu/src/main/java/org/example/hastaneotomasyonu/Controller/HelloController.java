package org.example.hastaneotomasyonu.Controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.hastaneotomasyonu.Algorithm.HastaHeap;
import org.example.hastaneotomasyonu.HelloApplication;
import org.example.hastaneotomasyonu.Services.GlobalHeapService;
import org.example.hastaneotomasyonu.models.Hasta;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import org.example.hastaneotomasyonu.Services.ClockService;

import static org.example.hastaneotomasyonu.HelloApplication.setRoot;

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
    public static HastaHeap HastaHeap;

    private double muayeneSaati = 9.00; // Başlangıç saati

    @FXML
    public void initialize() {
        HastaHeap = new HastaHeap(100);
        verileriHeapEkle();

        comboCinsiyet.getItems().addAll("Erkek", "Kadın", "Diğer");
        comboKanama.getItems().addAll("Yok", "Kanama", "AgirKanama");
        Timeline clock = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            ClockService.updateTime();
            lblSonuc.setText(ClockService.getCurrentTime());
        }));
        clock.setCycleCount(Timeline.INDEFINITE);
        clock.play();

    }
    @FXML
    private void heapGoster() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/hastaneotomasyonu/heap-view.fxml"));
        Parent root = loader.load();

        // HeapViewController'a eriş ve heap'i ver
        HeapViewController controller = loader.getController();
        controller.setHeap(GlobalHeapService.getHeap()); // heap burada global olarak saklanmalı

        Stage stage = new Stage();
        stage.setTitle("Hasta Heap Görselleştirici");
        stage.setScene(new Scene(root));
        stage.centerOnScreen();
        stage.show();
    }


    public void verileriHeapEkle() {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(getClass().getResource("/org/example/hastaneotomasyonu/Hasta.txt").openStream(), StandardCharsets.UTF_8))) {

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
                    double kayitSaati = Double.parseDouble(parcalar[7].trim().replace(",", "."));

                    Hasta hasta = new Hasta(ad, yas, cinsiyet, mahkum, engelli, kanama, kayitSaati);
                    hasta.oncelikPuaniHesapla();
                    hasta.muayeneSuresiHesapla();

                    hasta.setMuayeneSaati(muayeneSaati);
                    HastaHeap.ekle(hasta);
                    guncelleMuayeneSaati(hasta.muayeneSuresi);
                }
            }

            System.out.println("Başlangıç hastaları heap'e eklendi. Toplam hasta: " + HastaHeap.boyut());

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

            Hasta yeniHasta = new Hasta(ad, yas, cinsiyet, mahkum, engelli, kanama, saat);
            yeniHasta.oncelikPuaniHesapla();
            yeniHasta.muayeneSuresiHesapla();

            yeniHasta.setMuayeneSaati(muayeneSaati);
            HastaHeap.ekle(yeniHasta);
            guncelleMuayeneSaati(yeniHasta.muayeneSuresi);

            lblSonuc.setText("Hasta eklendi! Öncelik: " + yeniHasta.oncelikPuani +
                    ", Süre: " + yeniHasta.muayeneSuresi +
                    ", Muayene Saati: " + String.format("%.2f", yeniHasta.getMuayeneSaati()));
        } catch (Exception e) {
            lblSonuc.setText("Hata: " + e.getMessage());
        }
    }

    private void guncelleMuayeneSaati(int ekSure) {
        int saat = (int) muayeneSaati;
        int dakika = (int) Math.round((muayeneSaati - saat) * 100);

        int toplamDakika = dakika + ekSure;
        saat += toplamDakika / 60;
        dakika = toplamDakika % 60;

        muayeneSaati = saat + (dakika / 100.0);
    }

    @FXML
    private void tumHastalariGoster() throws IOException {
        setRoot("hastagoster");

    }
}
