package org.example.hastaneotomasyonu.Controller;
import com.sun.jna.platform.win32.COM.util.Factory;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.hastaneotomasyonu.Algorithm.HastaHeap;
import org.example.hastaneotomasyonu.Services.ClockService;
import org.example.hastaneotomasyonu.Services.GlobalHeapService;
import org.example.hastaneotomasyonu.Services.VoiceAnnouncementService;
import org.example.hastaneotomasyonu.models.Hasta;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;
import static org.example.hastaneotomasyonu.HelloApplication.setRoot;

public class HelloController {
    @FXML
    private ListView<String> listSiradakiHastalar;

    @FXML
    private TextField txtAd;
    @FXML
    private TextField txtYas;
    @FXML
    private VBox vboxHastaListesi;
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
    private DoubleProperty simuleEdilenZaman = new SimpleDoubleProperty(8); // toplam dakika
    private DoubleProperty zamanHizi = new SimpleDoubleProperty(0.02); // 1x, 2x, 5x hız gibi
    private Timeline simulationTimeline;
    private Factory COMUtils;

    private double getCurrentDoubleTime() {
        return doubleToSaatDakika1(simuleEdilenZaman.get());
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
        startSimulationTimer();
    }

    private AnimationTimer simulationTimer;
    private long lastUpdateTime = 0;

    // Zaman kontrol değişkenleri



    private void startSimulationTimer() {
        if (simulationTimer != null) {
            simulationTimer.stop();
        }

        simulationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (lastUpdateTime == 0) {
                    lastUpdateTime = now;
                    return;
                }

                long elapsedNanos = now - lastUpdateTime;
                double elapsedSeconds = elapsedNanos / 1_000_000_000.0;

                // Zamanı hızlandırılmış şekilde arttır
                double dakikaArtisi = elapsedSeconds * zamanHizi.get();
                simuleEdilenZaman.set(simuleEdilenZaman.get() + dakikaArtisi);

                // Gün hesaplama (her 1440 dakika = 24 saat)
                if (simuleEdilenZaman.get() >= (day + 1) * 24) {
                    day = (int) (simuleEdilenZaman.get() / 24);
                }

                updateSystem(); // sistem güncellemelerini çağır

                lastUpdateTime = now;
            }
        };

        simulationTimer.start();
    }




    private void updateSystem() {

        DecimalFormat df = new DecimalFormat("#.##", new DecimalFormatSymbols(Locale.US));
        double sonuc = doubleToSaatDakika1(simuleEdilenZaman.get()) - (24 * day);
        lblSonuc.setText("Simüle Edilen Zaman: " + df.format(sonuc));



        System.out.println(doubleToSaatDakika1(simuleEdilenZaman.get()));


        processNewPatients(doubleToSaatDakika1(simuleEdilenZaman.get()));
        checkCurrentExamination(doubleToSaatDakika1(simuleEdilenZaman.get()));
        displayCurrentExaminationStatus(doubleToSaatDakika1(simuleEdilenZaman.get()));
    }

    private void setupDynamicTimeline() {
        Timeline clock = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            ClockService.updateTime();
            lblSonuc.setText(ClockService.getCurrentTime());

            double currentTime = getCurrentDoubleTime();


            checkCurrentExamination(currentTime);


            if (muayenedekiHasta == null || currentTime >= muayeneBitisSaati) {
                processNewPatients(currentTime);

            }


            if (muayenedekiHasta == null && !HastaHeap.bosMu()) {
                startNewExamination(currentTime);

            }


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


    private int toplamsure = 0;

    private void processNewPatients(double currentTime) {
        List<Hasta> islenecekHastalar = new ArrayList<>();

        for (Hasta h : bekleyenHastalar) {
            if (h.hastaKayitSaati <= currentTime) {
                h.oncelikPuaniHesapla();
                h.muayeneSuresiHesapla();
                toplamsure += 1;
                h.setHastaNo(toplamsure);

                double sonBitis = (muayenedekiHasta != null) ? muayeneBitisSaati :
                        calculateLastFinishTime();
                h.setMuayeneSaati(Math.max(h.hastaKayitSaati, sonBitis));

                System.out.println("son bitiş saati: " + sonBitis + "  hasta kayıt saati " + h.hastaKayitSaati);
                System.out.println(h.muayeneSaati);

                HastaHeap.ekle(h);
                islenecekHastalar.add(h);
            }
        }

        // Tüm işlenen hastaları ana listeden çıkar
        bekleyenHastalar.removeAll(islenecekHastalar);
    }

    private final VoiceAnnouncementService announcementService = new VoiceAnnouncementService();

    private void displayCurrentExaminationStatus(double currentTime) {
        DecimalFormat df = new DecimalFormat("#.##", new DecimalFormatSymbols(Locale.US)); // Noktalı format


        if (muayenedekiHasta != null) {
            String cinsiyetIkonu = muayenedekiHasta.cinsiyet.equalsIgnoreCase("E") ? "👨" : "👩";
            lblMuayenedekiHasta.setText("No:"+muayenedekiHasta.hastaNo+"  "+cinsiyetIkonu + " " + muayenedekiHasta.hastaAdi);

            double baslangic = muayenedekiHasta.getMuayeneSaati() - (24 * day);
            lblBaslangic.setText(df.format(baslangic));
            lblbitissaati.setText(df.format(muayeneBitisSaati - (24 * day)));


            if (muayenedekiHasta.engellilikOrani > 0) {
                lblMuayenedekiHasta.setText(lblMuayenedekiHasta.getText() + "  ♿ %" + muayenedekiHasta.engellilikOrani);
            }


            if (muayenedekiHasta.kanamaliHastaDurumBilgisi != null &&
                    !muayenedekiHasta.kanamaliHastaDurumBilgisi.equalsIgnoreCase("kanamaYok")) {
                if (muayenedekiHasta.kanamaliHastaDurumBilgisi.equalsIgnoreCase("agirKanama")) {
                    lblMuayenedekiHasta.setText(lblMuayenedekiHasta.getText() + "  🆘 Ağır Kanama");
                } else {
                    lblMuayenedekiHasta.setText(lblMuayenedekiHasta.getText() + "  🩸 Kanama");
                }
            }
            announcementService.announcePatient(muayenedekiHasta);

            System.out.println("🔵 Şu anda muayenede: " + muayenedekiHasta.hastaAdi);
            System.out.printf("   Başlangıç: %.2f, Bitiş: %.2f%n",
                    muayenedekiHasta.getMuayeneSaati(), muayeneBitisSaati);


            if (!HastaHeap.bosMu()) {
                Hasta siradaki = HastaHeap.peek();
                String cinsiyetIkonuSiradaki = siradaki.cinsiyet.equalsIgnoreCase("E") ? "👨" : "👩";
                String siradakiHastaText = "No:"+siradaki.hastaNo+"  "+cinsiyetIkonuSiradaki + " " + siradaki.hastaAdi;


                if (siradaki.engellilikOrani > 0) {
                    siradakiHastaText += "  ♿ %" + siradaki.engellilikOrani;
                }


                if (siradaki.kanamaliHastaDurumBilgisi != null &&
                        !siradaki.kanamaliHastaDurumBilgisi.equalsIgnoreCase("kanamaYok")) {
                    if (siradaki.kanamaliHastaDurumBilgisi.equalsIgnoreCase("agirKanama")) {
                        siradakiHastaText += "  🆘 Ağır Kanama";
                    } else {
                        siradakiHastaText += "  🩸 Kanama";
                    }
                }

                lblSiradakiHasta.setText(siradakiHastaText);

                double tahminiBaslangic = siradaki.getMuayeneSaati() - (24 * day);
                double tahminiBitis = saatTopla(siradaki.getMuayeneSaati(), siradaki.muayeneSuresi) - (24 * day);

                lblTahminiBaslangic.setText(df.format(tahminiBaslangic));
                lbltahminibitis.setText(df.format(tahminiBitis));


                System.out.println("🟡 Sıradaki: " + siradaki.hastaAdi +
                        " (Başlangıç: " + siradaki.getMuayeneSaati() + ")");
            }
        } else if (!HastaHeap.bosMu()) {
            Hasta ilkHasta = HastaHeap.peek();
            String cinsiyetIkonu = ilkHasta.cinsiyet.equalsIgnoreCase("E") ? "👨" : "👩";
            String ilkHastaText = cinsiyetIkonu + " " + ilkHasta.hastaAdi;


            if (ilkHasta.engellilikOrani > 0) {
                ilkHastaText += "  ♿ %" + ilkHasta.engellilikOrani;
            }


            if (ilkHasta.kanamaliHastaDurumBilgisi != null &&
                    !ilkHasta.kanamaliHastaDurumBilgisi.equalsIgnoreCase("kanamaYok")) {
                if (ilkHasta.kanamaliHastaDurumBilgisi.equalsIgnoreCase("agirKanama")) {
                    ilkHastaText += "  🆘 Ağır Kanama";
                } else {
                    ilkHastaText += "  🩸 Kanama";
                }
            }

            lblSiradakiHasta.setText(ilkHastaText);

            double tahminiBaslangic = ilkHasta.getMuayeneSaati() - (24 * day);
            double tahminiBitis = saatTopla(ilkHasta.getMuayeneSaati(), ilkHasta.muayeneSuresi) - (24 * day);

            lblTahminiBaslangic.setText(df.format(tahminiBaslangic));
            lbltahminibitis.setText(df.format(tahminiBitis));

            System.out.println("⏳ Bekleyen hasta: " + ilkHasta.hastaAdi +
                    " (Muayene Başlangıç: " + ilkHasta.getMuayeneSaati() + ")");
        }


        vboxHastaListesi.getChildren().removeIf(node -> node instanceof HBox); // Önceki kutuları temizle

        for (Hasta h : HastaHeap.getTumHastalar()) {
            String baslangic = df.format(h.getMuayeneSaati() );
            String bitis = df.format(saatTopla(h.getMuayeneSaati(), h.muayeneSuresi));
            if(h.getMuayeneSaati()>24){
                baslangic = df.format(h.getMuayeneSaati()-24 );
                bitis = df.format(saatTopla(h.getMuayeneSaati(), h.muayeneSuresi)-24);
            }

            HBox kutucuk = new HBox(10);
            kutucuk.setStyle("-fx-background-color: #ffe0b2; -fx-background-radius: 6; -fx-padding: 8;");
            kutucuk.setAlignment(Pos.CENTER_LEFT);

            String cinsiyetIkonu = h.cinsiyet.equalsIgnoreCase("E") ? "👨" : "👩";

            Label lblAd = new Label("No:"+h.hastaNo+"  "+cinsiyetIkonu + " " + h.hastaAdi);
            lblAd.setStyle("-fx-font-weight: bold; -fx-text-fill: #e65100;");

            Label lblSaat = new Label("🕒 " + baslangic + " - " + bitis+ " - day" +day);
            lblSaat.setStyle("-fx-text-fill: #6d4c41;");

            kutucuk.getChildren().addAll(lblAd, lblSaat);

            if (h.engellilikOrani > 0) {
                Label lblEngelli = new Label("♿ %" + h.engellilikOrani);
                lblEngelli.setStyle("-fx-text-fill: #4e342e;");
                kutucuk.getChildren().add(lblEngelli);
            }

            if (h.kanamaliHastaDurumBilgisi != null &&
                    !h.kanamaliHastaDurumBilgisi.equalsIgnoreCase("kanamaYok")) {
                Label lblKanama;

                if (h.kanamaliHastaDurumBilgisi.equalsIgnoreCase("agirKanama")) {
                    lblKanama = new Label("🆘 Ağır Kanama");
                    lblKanama.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                } else {
                    lblKanama = new Label("🩸 Kanama");
                    lblKanama.setStyle("-fx-text-fill: red; -fx-font-weight: bold; -fx-font-size: 14;");

                }
                kutucuk.getChildren().add(lblKanama);
            }

            vboxHastaListesi.getChildren().add(kutucuk);
        }
    }




    private void processPastPatients(double currentTime) {
        List<Hasta> silinecekler = new ArrayList<>();

        for (Hasta h : bekleyenHastalar) {
            if (h.hastaKayitSaati >= 8.00 && h.hastaKayitSaati <= currentTime) {
                processPatient(h, currentTime);
                silinecekler.add(h);
            }
        }

        bekleyenHastalar.removeAll(silinecekler);
    }




    private void processPatient(Hasta h, double currentTime) {
        h.oncelikPuaniHesapla();
        h.muayeneSuresiHesapla();


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


            if (yeniMuayeneHasta.getMuayeneSaati() <= currentTime) {
                muayenedekiHasta = yeniMuayeneHasta;
                muayeneBitisSaati = saatTopla(muayenedekiHasta.getMuayeneSaati(),
                        muayenedekiHasta.getMuayeneSuresi());

                System.out.println("🏥 Muayeneye alınan: " + muayenedekiHasta.hastaAdi +
                        " (Başlangıç: " + muayenedekiHasta.getMuayeneSaati() +
                        ", Bitiş: " + muayeneBitisSaati + ")");


                if (!HastaHeap.bosMu()) {
                    Hasta siradaki = HastaHeap.peek();
                    double yeniBaslangic = muayeneBitisSaati;
                    siradaki.setMuayeneSaati(yeniBaslangic);
                }
            } else {

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

        while (!HastaHeap.bosMu()) {
            Hasta enOncelikli = HastaHeap.peek();
            double bitisZamani = saatTopla(enOncelikli.getMuayeneSaati(), enOncelikli.getMuayeneSuresi());


            if (currentTime < enOncelikli.getMuayeneSaati()) {
                break;
            }


            if (currentTime >= bitisZamani) {
                Hasta cikan = HastaHeap.cikar();
                System.out.println("✅ Muayenesi biten: " + cikan.hastaAdi +
                        " (Bitiş: " + bitisZamani + ")");
            }

            else if (currentTime >= enOncelikli.getMuayeneSaati() && currentTime < bitisZamani) {
                System.out.println("⚠️ Muayenedeki hasta: " + enOncelikli.hastaAdi +
                        " (Başlangıç: " + enOncelikli.getMuayeneSaati() +
                        ", Bitiş: " + bitisZamani + ")");
                break;
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
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/hastaneotomasyonu/view/heap-view.fxml"));
        Parent root = loader.load();

        HeapViewController controller = loader.getController();
        controller.setHeap(GlobalHeapService.getHeap());

        Stage stage = new Stage();
        stage.setTitle("Hasta Heap Görselleştirici");
        stage.setScene(new Scene(root));
        stage.centerOnScreen();
        stage.setOnCloseRequest(e -> controller.durdurGuncelleme()); // 👈 Sayfa kapanınca durdur
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
        zamanHizi.set(zamanHizi.get()/2);
        lblHizGostergesi.setText(zamanHizi.get()*50+"x ⏩");
    }
    @FXML
    private void x2button(){
        zamanHizi.set(zamanHizi.get()*2);
        lblHizGostergesi.setText(zamanHizi.get()*50+"x ⏩");
    }

    @FXML
    private void stop(){
        if(zamanHizi.get()==0){
            zamanHizi.set(0.02);
        }else{
            zamanHizi.set(0);
        }

        lblHizGostergesi.setText(zamanHizi.get()*100+"x ⏩");
    }

}