package org.example.hastaneotomasyonu.Controller;

import com.sun.jna.platform.win32.*;
import com.sun.jna.platform.win32.COM.*;
import com.sun.jna.platform.win32.COM.util.Factory;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;

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
import org.example.hastaneotomasyonu.models.Hasta;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalTime;
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
                if (simuleEdilenZaman.get() >= (day + 1) * 1440) {
                    day = (int) (simuleEdilenZaman.get() / 1440);
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

        // Diğer sistem güncellemeleri
        processNewPatients(doubleToSaatDakika1(simuleEdilenZaman.get()));
        checkCurrentExamination(doubleToSaatDakika1(simuleEdilenZaman.get()));
        displayCurrentExaminationStatus(doubleToSaatDakika1(simuleEdilenZaman.get()));
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
    private Hasta lastAnnouncedPatient = null;
    private String lastAnnouncedStatus = null;

    private void displayCurrentExaminationStatus(double currentTime) {
        DecimalFormat df = new DecimalFormat("#.##", new DecimalFormatSymbols(Locale.US)); // Noktalı format

        // Muayenedeki hasta bilgileri
        if (muayenedekiHasta != null) {
            String cinsiyetIkonu = muayenedekiHasta.cinsiyet.equalsIgnoreCase("E") ? "👨" : "👩";
            lblMuayenedekiHasta.setText(cinsiyetIkonu + " " + muayenedekiHasta.hastaAdi);

            double baslangic = muayenedekiHasta.getMuayeneSaati() - (24 * day);
            lblBaslangic.setText(df.format(baslangic));
            lblbitissaati.setText(df.format(muayeneBitisSaati - (24 * day)));

            // ♿ Engellilik oranı
            if (muayenedekiHasta.engellilikOrani > 0) {
                lblMuayenedekiHasta.setText(lblMuayenedekiHasta.getText() + "  ♿ %" + muayenedekiHasta.engellilikOrani);
            }

            // 🩸 Kanama durumu
            if (muayenedekiHasta.kanamaliHastaDurumBilgisi != null &&
                    !muayenedekiHasta.kanamaliHastaDurumBilgisi.equalsIgnoreCase("kanamaYok")) {
                if (muayenedekiHasta.kanamaliHastaDurumBilgisi.equalsIgnoreCase("agirKanama")) {
                    lblMuayenedekiHasta.setText(lblMuayenedekiHasta.getText() + "  🆘 Ağır Kanama");
                } else {
                    lblMuayenedekiHasta.setText(lblMuayenedekiHasta.getText() + "  🩸 Kanama");
                }
            }
            if (shouldAnnouncePatient(muayenedekiHasta)) {
                String sesliBilgi = createAnnouncementText(muayenedekiHasta);
                speak(sesliBilgi);
                lastAnnouncedPatient = muayenedekiHasta;
                lastAnnouncedStatus = getPatientStatus(muayenedekiHasta);
            }

            System.out.println("🔵 Şu anda muayenede: " + muayenedekiHasta.hastaAdi);
            System.out.printf("   Başlangıç: %.2f, Bitiş: %.2f%n",
                    muayenedekiHasta.getMuayeneSaati(), muayeneBitisSaati);

            // Sıradaki hasta bilgisi
            if (!HastaHeap.bosMu()) {
                Hasta siradaki = HastaHeap.peek();
                String cinsiyetIkonuSiradaki = siradaki.cinsiyet.equalsIgnoreCase("E") ? "👨" : "👩";
                String siradakiHastaText = cinsiyetIkonuSiradaki + " " + siradaki.hastaAdi;

                // ♿ engellilik oranı
                if (siradaki.engellilikOrani > 0) {
                    siradakiHastaText += "  ♿ %" + siradaki.engellilikOrani;
                }

                // 🩸 Kanama durumu
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

            // ♿ engellilik oranı
            if (ilkHasta.engellilikOrani > 0) {
                ilkHastaText += "  ♿ %" + ilkHasta.engellilikOrani;
            }

            // 🩸 kanama
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

        // 🔽 Tüm sıradaki hastaları listele
        vboxHastaListesi.getChildren().removeIf(node -> node instanceof HBox); // Önceki kutuları temizle

        for (Hasta h : HastaHeap.getTumHastalar()) {
            String baslangic = df.format(h.getMuayeneSaati() - (24 * day));
            String bitis = df.format(saatTopla(h.getMuayeneSaati(), h.muayeneSuresi));

            HBox kutucuk = new HBox(10);
            kutucuk.setStyle("-fx-background-color: #ffe0b2; -fx-background-radius: 6; -fx-padding: 8;");
            kutucuk.setAlignment(Pos.CENTER_LEFT);

            String cinsiyetIkonu = h.cinsiyet.equalsIgnoreCase("E") ? "👨" : "👩";

            Label lblAd = new Label(cinsiyetIkonu + " " + h.hastaAdi);
            lblAd.setStyle("-fx-font-weight: bold; -fx-text-fill: #e65100;");

            Label lblSaat = new Label("🕒 " + baslangic + " - " + bitis);
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
    private final Object voiceLock = new Object();

    private void speak(String text) {
        new Thread(() -> {
            synchronized (voiceLock) {
                if (containsTurkishCharacters(text)) {
                    speakWithFreeTTS(text);
                } else {
                    speakWithFreeTTS(text);
                }
            }
        }).start();
    }



    private void speakWithFreeTTS(String text) {
        try {
            System.setProperty("freetts.voices", "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
            VoiceManager voiceManager = VoiceManager.getInstance();
            Voice voice = voiceManager.getVoice("kevin16");

            if (voice != null) {
                voice.allocate();
                voice.setRate(150);
                voice.speak(text);
                voice.deallocate();
            }
        } catch (Exception e) {
            System.err.println("[FreeTTS HATASI] " + e.getMessage());
        }
    }

    private boolean containsTurkishCharacters(String text) {
        return text.matches(".*[çÇğĞıİöÖşŞüÜ].*");
    }

    private boolean shouldAnnouncePatient(Hasta currentPatient) {
        if (currentPatient == null) return false;

        if (lastAnnouncedPatient == null || !lastAnnouncedPatient.equals(currentPatient)) {
            return true;
        }

        String currentStatus = getPatientStatus(currentPatient);
        boolean statusChanged = !currentStatus.equals(lastAnnouncedStatus);

        boolean criticalChange = checkCriticalChange(currentPatient);

        return statusChanged || criticalChange;
    }

    private boolean checkCriticalChange(Hasta currentPatient) {
        if (lastAnnouncedPatient.kanamaliHastaDurumBilgisi == null &&
                currentPatient.kanamaliHastaDurumBilgisi != null) {
            return true;
        }
        return lastAnnouncedPatient.kanamaliHastaDurumBilgisi != null &&
                currentPatient.kanamaliHastaDurumBilgisi != null &&
                !lastAnnouncedPatient.kanamaliHastaDurumBilgisi.equals(
                        currentPatient.kanamaliHastaDurumBilgisi);
    }

    private String getPatientStatus(Hasta hasta) {
        return String.format("%s_%d_%s",
                hasta.hastaAdi,
                hasta.engellilikOrani,
                hasta.kanamaliHastaDurumBilgisi != null ? hasta.kanamaliHastaDurumBilgisi : "kanamaYok");
    }

    private String createAnnouncementText(Hasta hasta) {
        StringBuilder sb = new StringBuilder();
        sb.append("Patient ").append(hasta.hastaAdi).append(" is currently being examined.");

        if (hasta.engellilikOrani > 0) {
            sb.append(" Disability rate: ").append((int) hasta.engellilikOrani).append(" percent.");
        }

        if (hasta.kanamaliHastaDurumBilgisi != null) {
            if (hasta.kanamaliHastaDurumBilgisi.equalsIgnoreCase("agirKanama")) {
                sb.append(" Emergency! Severe bleeding detected!");
            } else if (!hasta.kanamaliHastaDurumBilgisi.equalsIgnoreCase("kanamaYok")) {
                sb.append(" Warning! Bleeding condition present.");
            }
        }

        return sb.toString();
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
        lblHizGostergesi.setText(zamanHizi.get()*100+"x >>");
    }
    @FXML
    private void x2button(){
        zamanHizi.set(zamanHizi.get()*2);
        lblHizGostergesi.setText(zamanHizi.get()*100+"x >>");
    }

    @FXML
    private void stop(){
        zamanHizi.set(0);
        lblHizGostergesi.setText(zamanHizi.get()*100+"x >>");
    }

}