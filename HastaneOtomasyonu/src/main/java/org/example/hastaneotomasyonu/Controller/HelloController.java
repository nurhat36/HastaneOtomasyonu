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
import java.util.*;

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
        HastaHeap = new HastaHeap();
        verileriHazirla();

        comboCinsiyet.getItems().addAll("Erkek", "Kadın", "Diğer");
        comboKanama.getItems().addAll("Yok", "Kanama", "AgirKanama");

        Timeline clock = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            ClockService.updateTime();
            lblSonuc.setText(ClockService.getCurrentTime());

            double currentTime = getCurrentDoubleTime();

            Iterator<Hasta> iterator = bekleyenHastalar.iterator();
            System.out.println("hasta adi      " + "muayene saati        " + "hasta kaıt saati");

            while (iterator.hasNext()) {
                Hasta h = iterator.next();

                if (h.hastaKayitSaati >= 8.00 && h.hastaKayitSaati <= currentTime) {
                    h.oncelikPuaniHesapla();
                    h.muayeneSuresiHesapla();


                    // 🔄 Muayene saati hesaplama (önceki tüm hastaların bitiş zamanına göre)
                    double enSonBitisSaati = 0.0;

                    if (!HastaHeap.bosMu()) {
                        for (Hasta heapHastasi : HastaHeap.getTumHastalar()) {


                            double bitisSaati = saatTopla(heapHastasi.getMuayeneSaati() , heapHastasi.getMuayeneSuresi() );
                            if (bitisSaati > enSonBitisSaati) {
                                enSonBitisSaati = bitisSaati;
                            }
                        }
                        System.out.println(Math.max(h.hastaKayitSaati, enSonBitisSaati)+"  "+h.hastaKayitSaati+"  "+enSonBitisSaati);
                        h.setMuayeneSaati(Math.max(h.hastaKayitSaati, enSonBitisSaati));
                    } else {
                        h.setMuayeneSaati(h.hastaKayitSaati);
                    }

                    System.out.println(h.hastaAdi + "    " + h.muayeneSaati + "      " + h.hastaKayitSaati+"    " +h.muayeneSuresi);

                    HastaHeap.ekle(h);
                    guncelleMuayeneSaati(h.getMuayeneSuresi(), h.hastaKayitSaati);

                    iterator.remove();
                }
            }

            // Muayenesi biten hastayı çıkar
            if (!HastaHeap.bosMu()) {
                Hasta enOncelikli = HastaHeap.peek();
                double bitisZamani = saatTopla(enOncelikli.getMuayeneSaati(),enOncelikli.getMuayeneSuresi());
                if (currentTime >= bitisZamani) {
                    Hasta cikan = HastaHeap.cikar();
                    System.out.println("Muayenesi biten: " + cikan.hastaAdi);
                }
            }

            // Şu anda muayene olan hasta
            if (!HastaHeap.bosMu()) {
                Hasta suankiHasta = HastaHeap.peek();
                double baslangicSaati = suankiHasta.getMuayeneSaati();
                double bitisSaati = saatTopla(baslangicSaati, suankiHasta.getMuayeneSuresi());

                System.out.println("🔵 Şu anda muayenede: " + suankiHasta.hastaAdi+"   "+suankiHasta.hastaKayitSaati);
                System.out.printf("   Başlangıç Saati: %.2f%n", baslangicSaati);
                System.out.printf("   Bitiş Saati    : %.2f%n", bitisSaati);

                if (HastaHeap.boyut() > 1) {
                    Hasta siradakiHasta = HastaHeap.peekNext(); // bu metodu senin yazman gerekir
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
    private String doubleToSaatDakika(double zaman) {
        int saat = (int) zaman;
        int dakika = (int) ((zaman - saat) * 60);
        return String.format("%02d:%02d", saat, dakika);
    }
    public static double saatTopla(double saatDouble, int dakikaEkle) {
        int saat = (int) saatDouble;
        int dakika = (int) Math.round((saatDouble - saat) * 100); // 0.40 → 40 dakikaya çeviriyoruz

        dakika += dakikaEkle;

        saat += dakika / 60;
        dakika = dakika % 60;

        return saat + (dakika / 100.0); // 10 saat 0 dakika → 10.00
    }






    private double getCurrentDoubleTime() {
        LocalTime time = LocalTime.now();
        return time.getHour() + (time.getMinute() / 60.0);  // Dakikayı doğru şekilde 60'a bölüyoruz
    }


    private void guncelleMuayeneSaati(int ekSure, double hastaKayitSaati) {
        muayeneSaati=saatTopla(hastaKayitSaati, ekSure);

        System.out.println("Muayene saati: " + muayeneSaati);
    }






    public void verileriHazirla() {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(getClass().getResource("/org/example/hastaneotomasyonu/Hasta.txt").openStream(), StandardCharsets.UTF_8))) {

            String line;
            double previousMuayeneSaati = 0; // Önceki muayene saati
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

                    // Muayene saati hesaplanacak


                    // İlk hastadan sonraki hastalar için muayene saati belirleniyor


                    // Hasta oluşturuluyor
                    Hasta hasta = new Hasta(ad, yas, cinsiyet, mahkum, engelli, kanama, kayitSaati);

                    // Muayene saati hastaya atanıyor


                    // Listeye ekleniyor
                    bekleyenHastalar.add(hasta);

                    // Sonraki hastanın muayene saati için güncelleniyor

                }
            }

            // Kayıt saatlerine göre sıralama (küçükten büyüğe)
            Collections.sort(bekleyenHastalar, Comparator.comparingDouble(Hasta::getHastaKayitSaati));

            System.out.println("Bekleyen hastalar yüklendi ve sıralandı: " + bekleyenHastalar.size());

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
            double saat = Double.parseDouble(txtSaat.getText().replace(",", ".")); // virgül düzeltmesi

            // Yeni hasta oluşturuluyor
            Hasta yeniHasta = new Hasta(ad, yas, cinsiyet, mahkum, engelli, kanama.toLowerCase(), saat);
            yeniHasta.setMuayeneSaati(saat); // İlk muayene saati kayıta eşit

            // Eğer bekleyen listede hasta varsa, son muayene saatine göre ayarla
            if (!bekleyenHastalar.isEmpty()) {
                Hasta sonHasta = bekleyenHastalar.get(bekleyenHastalar.size() - 1);
                double sonHastaMuayeneSaati = sonHasta.getMuayeneSaati() + (sonHasta.getMuayeneSuresi() / 60.0);
                if (saat < sonHastaMuayeneSaati) {
                    yeniHasta.setMuayeneSaati(sonHastaMuayeneSaati); // Çakışma varsa ileri al
                }
            }

            bekleyenHastalar.add(yeniHasta);
            Collections.sort(bekleyenHastalar, Comparator.comparingDouble(Hasta::getHastaKayitSaati));

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Hasta Eklendi");
            alert.setHeaderText(null);
            alert.setContentText("Yeni hasta başarıyla eklendi.");
            alert.showAndWait();

            // Form alanlarını temizle
            txtAd.clear();
            txtYas.clear();
            comboCinsiyet.getSelectionModel().clearSelection();
            checkMahkum.setSelected(false);
            txtEngelli.clear();
            comboKanama.getSelectionModel().clearSelection();
            txtSaat.clear();

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Hata");
            alert.setHeaderText("Girdi Hatası");
            alert.setContentText("Lütfen tüm alanları doğru şekilde doldurun!");
            alert.showAndWait();
            e.printStackTrace();
        }
    }



    @FXML
    private void tumHastalariGoster() throws IOException {
        setRoot("hastagoster");
    }
}
