package org.example.hastaneotomasyonu.Controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.hastaneotomasyonu.Algorithm.HastaHeap;
import org.example.hastaneotomasyonu.models.Hasta;

import java.util.List;

public class HastaGosterController {
    @FXML private TableView<Hasta> tableView;
    @FXML private TableColumn<Hasta, String> adiColumn;
    @FXML private TableColumn<Hasta, Integer> puanColumn;
    @FXML private TableColumn<Hasta, Double> saatColumn;
    @FXML private TableColumn<Hasta, Integer> sureColumn;

    @FXML
    public void initialize() {
        adiColumn.setCellValueFactory(new PropertyValueFactory<>("hastaAdi"));
        puanColumn.setCellValueFactory(new PropertyValueFactory<>("oncelikPuani"));
        saatColumn.setCellValueFactory(new PropertyValueFactory<>("muayeneSaati"));
        sureColumn.setCellValueFactory(new PropertyValueFactory<>("muayeneSuresi"));

        // Örnek veriler doğru constructor ile oluşturulmalı
        HastaHeap heap = HelloController.HastaHeap;

        // Heap içindeki tüm hastaları çek (liste olarak)
        List<Hasta> hastaListesi = List.of(heap.getTumHastalar());

        // ObservableList'e dönüştür
        ObservableList<Hasta> veri = FXCollections.observableArrayList(hastaListesi);

        // Tabloya veriyi bağla
        tableView.setItems(veri);
    }

}
