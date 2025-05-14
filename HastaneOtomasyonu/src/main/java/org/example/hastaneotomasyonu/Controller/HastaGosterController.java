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
        adiColumn.setCellValueFactory(new PropertyValueFactory<>("hastaAdi"));
        puanColumn.setCellValueFactory(new PropertyValueFactory<>("oncelikPuani"));

        // Noktalı formatlama için
        DecimalFormat df = new DecimalFormat("#.##", new DecimalFormatSymbols(Locale.US));

        saatColumn.setCellValueFactory(cellData -> {
            double saat = cellData.getValue().getMuayeneSaati();
            double gosterilecekSaat = saat > 24 ? saat - 24 : saat;
            double yuvarlanmisSaat = Double.parseDouble(df.format(gosterilecekSaat));
            return new javafx.beans.property.SimpleDoubleProperty(yuvarlanmisSaat).asObject();
        });

        sureColumn.setCellValueFactory(new PropertyValueFactory<>("muayeneSuresi"));

        HastaHeap heap = HelloController.HastaHeap;
        List<Hasta> hastaListesi = List.of(heap.getTumHastalar());
        ObservableList<Hasta> veri = FXCollections.observableArrayList(hastaListesi);
        tableView.setItems(veri);
        baslatTabloGuncelleme();

    }
    public void durdurGuncelleme() {
        if (tabloGuncellemeZamanlayici != null) {
            tabloGuncellemeZamanlayici.stop();
        }
    }



}
