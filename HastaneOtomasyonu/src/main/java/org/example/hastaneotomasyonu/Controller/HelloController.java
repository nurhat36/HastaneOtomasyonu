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
    private Label lblMuayenedekiHasta;
    @FXML
    private Label lblSiradakiHasta;
    @FXML
    private Label lblBaslangic;
    @FXML
    private Label lblTahminiBaslangic;
    @FXML
    private Label lblbitissaati;
    @FXML
    private Label lbltahminibitis;
    @FXML
    private Label lblHizGostergesi;
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
    public static int day=0;

    public static HastaHeap HastaHeap;
    private List<Hasta> bekleyenHastalar = new ArrayList<>();
    private double muayeneSaati = 9.00;
    private double baslangicSaati = 9.00;

    private Hasta muayenedekiHasta = null;
    public static double muayeneBitisSaati = 0.0;
    private double simuleEdilenZaman = 8.00; // Sabah 8:00'de başlar
    private double zamanHizi = 1.0; // 1.0 = gerçek zaman, 2.0 = 2x hızında vs.
    private Timeline simulationTimeline;

    private double getCurrentDoubleTime() {
        return doubleToSaatDakika1(simuleEdilenZaman);
    }

    @FXML
    public void initialize() {
        HastaHeap = new HastaHeap();
        verileriHazirla();

        comboCinsiyet.getItems().addAll("Erkek", "Kadın", "Diğer");
        comboKanama.getItems().addAll("Yok", "Kanama", "AgirKanama");

        double currentTime = getCurrentDoubleTime();
        processPastPatients(currentTime);

        setupDynamicTimeline();
        startSimulationTimeline();
    }
    private void startSimulationTimeline() {
        simulationTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            // Zamanı ilerlet (her saniyede 1 dakika ileri gidecek şekilde)
            simuleEdilenZaman += (1.0 / 60.0) * zamanHizi;
            if(doubleToSaatDakika1(simuleEdilenZaman)>24){
                day = (int) (doubleToSaatDakika1(simuleEdilenZaman)/24);
            }


            // 24 saatlik döngü sağla


            // Sistem güncellemelerini yap
            updateSystem();
        }));
        simulationTimeline.setCycleCount(Timeline.INDEFINITE);
        simulationTimeline.play();
    }

    private void updateSystem() {

        lblSonuc.setText("Simüle Edilen Zaman: " + (doubleToSaatDakika1(simuleEdilenZaman)-(24*day)));


        System.out.println(doubleToSaatDakika1(simuleEdilenZaman));

        // Diğer sistem güncellemeleri
        processNewPatients(doubleToSaatDakika1(simuleEdilenZaman));
        checkCurrentExamination(doubleToSaatDakika1(simuleEdilenZaman));
        displayCurrentExaminationStatus(doubleToSaatDakika1(simuleEdilenZaman));
    }

    private void setupDynamicTimeline() {
        Timeline clock = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            ClockService.updateTime();
            lblSonuc.setText(ClockService.getCurrentTime());

            double currentTime = getCurrentDoubleTime();

            // 1. Muayenedeki hastanın durumunu kontrol et
            checkCurrentExamination(currentTime);

            // 2. Yeni hastaları işle (muayenede hasta yoksa veya muayene bittiyse)
            if (muayenedekiHasta == null || currentTime >= muayeneBitisSaati) {
                processNewPatients(currentTime);

            }

            // 3. Muayene için yeni hasta al
            if (muayenedekiHasta == null && !HastaHeap.bosMu()) {
                startNewExamination(currentTime);

            }

            // 4. Durum bilgilerini göster
            displayCurrentExaminationStatus(currentTime);
        }));

        clock.setCycleCount(Timeline.INDEFINITE);
        clock.play();
    }

    private void checkCurrentExamination(double currentTime) {
        if (muayenedekiHasta != null && currentTime >= muayeneBitisSaati) {
            System.out.println("✅ Muayenesi biten: " + muayenedekiHasta.hastaAdi +
                    " (Bitiş: " + muayeneBitisSaati + ")");
            muayenedekiHasta = null;
        }
    }



    private void processNewPatients(double currentTime) {
        Iterator<Hasta> iterator = bekleyenHastalar.iterator();
        while (iterator.hasNext()) {
            Hasta h = iterator.next();
            if (h.hastaKayitSaati <= currentTime) {
                h.oncelikPuaniHesapla();
                h.muayeneSuresiHesapla();

                // Muayene saati = max(kayıt saati, son muayene bitiş saati)
                double sonBitis = (muayenedekiHasta != null) ? muayeneBitisSaati :
                        calculateLastFinishTime();
                h.setMuayeneSaati(Math.max(h.hastaKayitSaati, sonBitis));
                System.out.println("son bitiş saati: " + sonBitis+"  hasta kayıt saati "+h.hastaKayitSaati);
                System.out.println(h.muayeneSaati);



                HastaHeap.ekle(h);
                iterator.remove();
            }
        }
    }

    private void displayCurrentExaminationStatus(double currentTime) {
        if (muayenedekiHasta != null) {
            lblMuayenedekiHasta.setText(muayenedekiHasta.hastaAdi);
            lblBaslangic.setText(String.valueOf((muayenedekiHasta.getMuayeneSaati()-(24*day))));
            lblbitissaati.setText(String.valueOf(muayeneBitisSaati));
            System.out.println("🔵 Şu anda muayenede: " + muayenedekiHasta.hastaAdi);
            System.out.printf("   Başlangıç: %.2f, Bitiş: %.2f%n",
                    muayenedekiHasta.getMuayeneSaati(), muayeneBitisSaati);

            // Sıradaki hasta bilgisi
            if (!HastaHeap.bosMu()) {
                Hasta siradaki = HastaHeap.peek();
                lblSiradakiHasta.setText(siradaki.hastaAdi);
                lblTahminiBaslangic.setText(String.valueOf((siradaki.getMuayeneSaati()-(24*day))));
                lbltahminibitis.setText(String.valueOf(saatTopla(siradaki.getMuayeneSaati(),siradaki.muayeneSuresi)));
                System.out.println("🟡 Sıradaki: " + siradaki.hastaAdi +
                        " (Başlangıç: " + siradaki.getMuayeneSaati() + ")");
            }
        } else if (!HastaHeap.bosMu()) {
            Hasta ilkHasta = HastaHeap.peek();
            lblSiradakiHasta.setText(ilkHasta.hastaAdi);
            lblTahminiBaslangic.setText(String.valueOf((ilkHasta.getMuayeneSaati()-(24*day))));
            lbltahminibitis.setText(String.valueOf(saatTopla(ilkHasta.getMuayeneSaati(),ilkHasta.muayeneSuresi)));
            System.out.println("⏳ Bekleyen hasta: " + ilkHasta.hastaAdi +
                    " (Muayene Başlangıç: " + ilkHasta.getMuayeneSaati() + ")");
        }
    }

    private void processPastPatients(double currentTime) {
        Iterator<Hasta> iterator = bekleyenHastalar.iterator();
        double enSonBitisSaati = 0.0;

        while (iterator.hasNext()) {
            Hasta h = iterator.next();

            if (h.hastaKayitSaati >= 8.00 && h.hastaKayitSaati <= currentTime) {
                processPatient(h, currentTime);
                iterator.remove();
            }
        }
    }



    private void processPatient(Hasta h, double currentTime) {
        h.oncelikPuaniHesapla();
        h.muayeneSuresiHesapla();

        // Muayene başlangıç saatini doğru şekilde hesapla
        double sonBitisSaati;
        if (muayenedekiHasta != null) {
            sonBitisSaati = muayeneBitisSaati;
        } else {
            sonBitisSaati = calculateLastFinishTime();
        }
        System.out.println("son bitiş saati: " + sonBitisSaati+"  hasta kayıt saati "+h.hastaKayitSaati);

        h.setMuayeneSaati(Math.max(h.hastaKayitSaati, sonBitisSaati));
        HastaHeap.ekle(h);
    }

    private void startNewExamination(double currentTime) {
        if (!HastaHeap.bosMu()) {
            Hasta yeniMuayeneHasta = HastaHeap.cikar();

            // Hasta muayene zamanı geldiyse
            if (yeniMuayeneHasta.getMuayeneSaati() <= currentTime) {
                muayenedekiHasta = yeniMuayeneHasta;
                muayeneBitisSaati = saatTopla(muayenedekiHasta.getMuayeneSaati(),
                        muayenedekiHasta.getMuayeneSuresi());

                System.out.println("🏥 Muayeneye alınan: " + muayenedekiHasta.hastaAdi +
                        " (Başlangıç: " + muayenedekiHasta.getMuayeneSaati() +
                        ", Bitiş: " + muayeneBitisSaati + ")");

                // Sıradaki hastanın başlangıç saatini güncelle
                if (!HastaHeap.bosMu()) {
                    Hasta siradaki = HastaHeap.peek();
                    double yeniBaslangic = muayeneBitisSaati;
                    siradaki.setMuayeneSaati(yeniBaslangic);
                }
            } else {
                // Henüz zamanı gelmedi, heap'e geri ekle
                HastaHeap.ekle(yeniMuayeneHasta);
            }
        }
    }

    private double calculateLastFinishTime() {
        double enSonBitisSaati = baslangicSaati;

        if (!HastaHeap.bosMu()) {
            for (Hasta heapHastasi : HastaHeap.getTumHastalar()) {
                double bitisSaati = saatTopla(heapHastasi.getMuayeneSaati(), heapHastasi.getMuayeneSuresi());
                if (bitisSaati > enSonBitisSaati) {
                    enSonBitisSaati = bitisSaati;
                }
            }
        }

        return enSonBitisSaati;
    }

    private void processFinishedExaminations(double currentTime) {
        // Muayenesi bitmiş hastaları çıkar
        while (!HastaHeap.bosMu()) {
            Hasta enOncelikli = HastaHeap.peek();
            double bitisZamani = saatTopla(enOncelikli.getMuayeneSaati(), enOncelikli.getMuayeneSuresi());

            // Eğer hasta henüz muayeneye başlamadıysa döngüyü kır
            if (currentTime < enOncelikli.getMuayeneSaati()) {
                break;
            }

            // Eğer hasta muayenesini bitirdiyse çıkar
            if (currentTime >= bitisZamani) {
                Hasta cikan = HastaHeap.cikar();
                System.out.println("✅ Muayenesi biten: " + cikan.hastaAdi +
                        " (Bitiş: " + bitisZamani + ")");
            }
            // Eğer hasta hala muayenedeyse
            else if (currentTime >= enOncelikli.getMuayeneSaati() && currentTime < bitisZamani) {
                System.out.println("⚠️ Muayenedeki hasta: " + enOncelikli.hastaAdi +
                        " (Başlangıç: " + enOncelikli.getMuayeneSaati() +
                        ", Bitiş: " + bitisZamani + ")");
                break; // Muayenedeki hasta bitene kadar diğerlerine dokunma
            }
        }
    }
    private double doubleToSaatDakika1(double zaman) {
        int saat = (int) zaman;
        int dakika = (int) ((zaman - saat) * 60);
        return saat + dakika / 100.0;
    }




    private String doubleToSaatDakika(double zaman) {
        int saat = (int) zaman;
        int dakika = (int) ((zaman - saat) * 60);
        return String.format("%02d:%02d", saat, dakika);
    }

    public static double saatTopla(double saatDouble, int dakikaEkle) {
        int saat = (int) saatDouble;
        int dakika = (int) Math.round((saatDouble - saat) * 100);

        dakika += dakikaEkle;

        saat += dakika / 60;
        dakika = dakika % 60;

        return saat + (dakika / 100.0);
    }



    private void guncelleMuayeneSaati(int ekSure, double hastaKayitSaati) {
        muayeneSaati = saatTopla(hastaKayitSaati, ekSure);
        System.out.println("Muayene saati: " + muayeneSaati);
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
            double saat = Double.parseDouble(txtSaat.getText().replace(",", "."));

            Hasta yeniHasta = new Hasta(ad, yas, cinsiyet, mahkum, engelli, kanama.toLowerCase(), saat);
            yeniHasta.oncelikPuaniHesapla();
            yeniHasta.muayeneSuresiHesapla();

            double enSonBitisSaati = calculateLastFinishTime();
            yeniHasta.setMuayeneSaati(Math.max(saat, enSonBitisSaati));

            bekleyenHastalar.add(yeniHasta);
            Collections.sort(bekleyenHastalar, Comparator.comparingDouble(Hasta::getHastaKayitSaati));

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Hasta Eklendi");
            alert.setHeaderText(null);
            alert.setContentText("Yeni hasta başarıyla eklendi.");
            alert.showAndWait();

            // Formu temizle
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
    @FXML
    private void x1button(){
        zamanHizi=1;
        lblHizGostergesi.setText(zamanHizi+">>");
    }
    @FXML
    private void x2button(){
        zamanHizi=2;
        lblHizGostergesi.setText(zamanHizi+">>");
    }
    @FXML
    private void x3button(){
        zamanHizi=3;
        lblHizGostergesi.setText(zamanHizi+">>");
    }
    @FXML
    private void x5button(){
        zamanHizi=5;
        lblHizGostergesi.setText(zamanHizi+">>");
    }
    @FXML
    private void stop(){
        zamanHizi=0;
        lblHizGostergesi.setText(zamanHizi+">>");
    }

}