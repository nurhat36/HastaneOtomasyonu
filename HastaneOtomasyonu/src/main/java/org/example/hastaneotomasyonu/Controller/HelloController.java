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
import org.example.hastaneotomasyonu.Services.ClockService;
import org.example.hastaneotomasyonu.Services.GlobalHeapService;
import org.example.hastaneotomasyonu.models.Hasta;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
    private List<Hasta> bekleyenHastalar = new ArrayList<>();
    private double muayeneSaati = 9.00;
    private double baslangicSaati = 9.00; // Her zaman 9:00'da başlasın

    @FXML
    public void initialize() {
        HastaHeap = new HastaHeap(100);
        verileriHazirla();

        comboCinsiyet.getItems().addAll("Erkek", "Kadın", "Diğer");
        comboKanama.getItems().addAll("Yok", "Kanama", "AgirKanama");

        Timeline clock = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            ClockService.updateTime();
            lblSonuc.setText(ClockService.getCurrentTime());

            double currentTime = getCurrentDoubleTime();

            // Uygulama açıldığında mevcut saati alıyoruz
            Iterator<Hasta> iterator = bekleyenHastalar.iterator();
            while (iterator.hasNext()) {
                Hasta h = iterator.next();
                // Sadece 9:00'dan sonra ve geçerli zamandaki hastaları işliyoruz
                if (h.hastaKayitSaati >= baslangicSaati && h.hastaKayitSaati <= currentTime) {
                    h.oncelikPuaniHesapla();
                    h.muayeneSuresiHesapla();
                    h.setMuayeneSaati(muayeneSaati);
                    HastaHeap.ekle(h);
                    guncelleMuayeneSaati(h.getMuayeneSuresi());
                    iterator.remove();
                }
            }

            // Muayene süresi biten hastayı çıkar
            if (!HastaHeap.bosMu()) {
                Hasta enOncelikli = HastaHeap.peek();
                double bitisZamani = enOncelikli.getMuayeneSaati() + (enOncelikli.getMuayeneSuresi() / 60.0);
                if (currentTime >= bitisZamani) {
                    Hasta cikan = HastaHeap.cikar();
                    System.out.println("Muayenesi biten: " + cikan.hastaAdi);
                }
            }

            if (!HastaHeap.bosMu()) {
                Hasta suankiHasta = HastaHeap.peek();
                double baslangicSaati = suankiHasta.getMuayeneSaati();
                double bitisSaati = baslangicSaati + (suankiHasta.getMuayeneSuresi() / 60.0);

                System.out.println("🔵 Şu anda muayenede: " + suankiHasta.hastaAdi);
                System.out.printf("   Başlangıç Saati: %.2f%n", baslangicSaati);
                System.out.printf("   Bitiş Saati    : %.2f%n", bitisSaati);

                // Sıradaki hastayı tahmin et
                if (HastaHeap.boyut() > 1) {
                    Hasta siradakiHasta = HastaHeap.peekNext(); // bu metodu ekleyeceğiz
                    double siradakiBaslangic = bitisSaati;
                    double siradakiBitis = siradakiBaslangic + (siradakiHasta.getMuayeneSuresi() / 60.0);

                    System.out.println("🟡 Sıradaki hasta: " + siradakiHasta.hastaAdi);
                    System.out.printf("   Tahmini Başlangıç: %.2f%n", siradakiBaslangic);
                    System.out.printf("   Tahmini Bitiş    : %.2f%n", siradakiBitis);
                } else {
                    System.out.println("🟢 Sıradaki hasta yok.");
                }
            }

        }));
        clock.setCycleCount(Timeline.INDEFINITE);
        clock.play();
    }

    private double getCurrentDoubleTime() {
        LocalTime time = LocalTime.now();
        return time.getHour() + (time.getMinute() / 60.0);  // Dakikayı doğru şekilde 60'a bölüyoruz
    }


    private void guncelleMuayeneSaati(int ekSure) {
        // Mevcut saati saat ve dakika olarak ayırıyoruz
        int saat = (int) muayeneSaati;
        int dakika = (int) Math.round((muayeneSaati - saat) * 60);  // ondalıklı kısmı dakikaya çeviriyoruz

        // Muayene süresi ekleyerek yeni saati hesaplıyoruz
        int toplamDakika = dakika + ekSure;
        saat += toplamDakika / 60;  // Saat hesaplaması
        dakika = toplamDakika % 60;  // Dakika hesaplaması

        // Yeni saati double formatında güncelliyoruz
        muayeneSaati = saat + (dakika / 60.0);  // Saat + dakika / 60 şeklinde
    }


    public void verileriHazirla() {
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
                        bekleyenHastalar.add(hasta);

                }
            }

            System.out.println("Bekleyen hastalar yüklendi: " + bekleyenHastalar.size());

        } catch (Exception e) {
            System.err.println("Veriler okunurken hata oluştu: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void heapGoster() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/hastaneotomasyonu/heap-view.fxml"));
        Parent root = loader.load();

        HeapViewController controller = loader.getController();
        controller.setHeap(GlobalHeapService.getHeap());

        Stage stage = new Stage();
        stage.setTitle("Hasta Heap Görselleştirici");
        stage.setScene(new Scene(root));
        stage.centerOnScreen();
        stage.show();
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

            // Yeni hastayı eklerken, önceki hastanın muayene saati dikkate alınmalı
            if (!bekleyenHastalar.isEmpty()) {
                Hasta sonHasta = bekleyenHastalar.get(bekleyenHastalar.size() - 1);
                saat = sonHasta.getMuayeneSaati() + (sonHasta.getMuayeneSuresi() / 60.0); // Bir önceki hastanın bitiş saati
            }

            Hasta yeniHasta = new Hasta(ad, yas, cinsiyet, mahkum, engelli, kanama, saat);
            bekleyenHastalar.add(yeniHasta);

            lblSonuc.setText("Hasta eklendi. Kayıt saati geldiğinde heap'e alınacak.");

        } catch (Exception e) {
            lblSonuc.setText("Hata: " + e.getMessage());
        }
    }

    @FXML
    private void tumHastalariGoster() throws IOException {
        setRoot("hastagoster");
    }
}
