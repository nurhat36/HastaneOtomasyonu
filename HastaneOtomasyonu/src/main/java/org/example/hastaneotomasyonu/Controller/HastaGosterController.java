package org.example.hastaneotomasyonu.Controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Duration;
import org.example.hastaneotomasyonu.Algorithm.HastaHeap;
import org.example.hastaneotomasyonu.models.Hasta;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

public class HastaGosterController {
    @FXML private TableView<Hasta> tableView;
    @FXML private TableColumn<Hasta, String> adiColumn;
    @FXML private TableColumn<Hasta, Integer> puanColumn;
    @FXML private TableColumn<Hasta, Double> saatColumn;
    @FXML private TableColumn<Hasta, Integer> sureColumn;
    private Timeline tabloGuncellemeZamanlayici;

    private void baslatTabloGuncelleme() {
        tabloGuncellemeZamanlayici = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            guncelleTablo();
        }));
        tabloGuncellemeZamanlayici.setCycleCount(Timeline.INDEFINITE);
        tabloGuncellemeZamanlayici.play();
    }

    private void guncelleTablo() {
        HastaHeap heap = HelloController.HastaHeap;
        List<Hasta> hastaListesi = List.of(heap.getTumHastalar());
        ObservableList<Hasta> veri = FXCollections.observableArrayList(hastaListesi);
        tableView.setItems(veri);
    }

    @FXML
    public void initialize() {
        // Tablo stilini ayarla
        tableView.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 14px;");

        // Sütun başlıklarını özelleştir
        adiColumn.setText("Hasta Adı");
        adiColumn.setStyle("-fx-alignment: CENTER; -fx-font-weight: bold;");
        adiColumn.setCellValueFactory(new PropertyValueFactory<>("hastaAdi"));

        puanColumn.setText("Öncelik Puanı");
        puanColumn.setStyle("-fx-alignment: CENTER;");
        puanColumn.setCellValueFactory(new PropertyValueFactory<>("oncelikPuani"));

        // Saat sütunu için özel formatlama
        DecimalFormat df = new DecimalFormat("#.##", new DecimalFormatSymbols(Locale.US));
        saatColumn.setText("Muayene Saati");
        saatColumn.setStyle("-fx-alignment: CENTER;");
        saatColumn.setCellValueFactory(cellData -> {
            double saat = cellData.getValue().getMuayeneSaati();
            double gosterilecekSaat = saat > 24 ? saat - 24 : saat;
            double yuvarlanmisSaat = Double.parseDouble(df.format(gosterilecekSaat));
            return new javafx.beans.property.SimpleDoubleProperty(yuvarlanmisSaat).asObject();
        });

        // Saat sütunu için özel hücre fabrikası (00:00 formatında göstermek için)
        saatColumn.setCellFactory(column -> new javafx.scene.control.TableCell<Hasta, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    int saat = (int) Math.floor(item);
                    int dakika = (int) Math.round((item - saat) * 100);
                    setText(String.format("%02d:%02d", saat, dakika));

                    // Özel durumlara göre renklendirme
                    if (item >= 8 && item <= 17) {
                        setStyle("-fx-text-fill: #2e7d32; -fx-font-weight: bold;"); // Çalışma saatleri
                    } else {
                        setStyle("-fx-text-fill: #d32f2f; -fx-font-weight: bold;"); // Mesai dışı
                    }
                }
            }
        });

        sureColumn.setText("Muayene Süresi (dk)");
        sureColumn.setStyle("-fx-alignment: CENTER;");
        sureColumn.setCellValueFactory(new PropertyValueFactory<>("muayeneSuresi"));

        // Süre sütunu için özel hücre fabrikası
        sureColumn.setCellFactory(column -> new javafx.scene.control.TableCell<Hasta, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.toString());

                    // Uzun muayene sürelerini vurgula
                    if (item > 30) {
                        setStyle("-fx-text-fill: #d32f2f; -fx-font-weight: bold;");
                    } else if (item > 15) {
                        setStyle("-fx-text-fill: #ff8f00; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #2e7d32;");
                    }
                }
            }
        });

        // Öncelik puanına göre renklendirme
        puanColumn.setCellFactory(column -> new javafx.scene.control.TableCell<Hasta, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.toString());

                    // Acil durumları vurgula
                    if (item >= 90) {
                        setStyle("-fx-background-color: #ffcdd2; -fx-font-weight: bold; -fx-text-fill: #c62828;");
                    } else if (item >= 70) {
                        setStyle("-fx-background-color: #fff8e1; -fx-font-weight: bold; -fx-text-fill: #ff8f00;");
                    } else {
                        setStyle("-fx-text-fill: #1565c0;");
                    }
                }
            }
        });

        // Verileri yükle
        HastaHeap heap = HelloController.HastaHeap;
        List<Hasta> hastaListesi = List.of(heap.getTumHastalar());
        ObservableList<Hasta> veri = FXCollections.observableArrayList(hastaListesi);
        tableView.setItems(veri);

        // Otomatik güncellemeyi başlat
        baslatTabloGuncelleme();
    }

    public void durdurGuncelleme() {
        if (tabloGuncellemeZamanlayici != null) {
            tabloGuncellemeZamanlayici.stop();
        }
    }
}