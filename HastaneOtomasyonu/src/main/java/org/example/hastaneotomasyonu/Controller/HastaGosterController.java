package org.example.hastaneotomasyonu.Controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.hastaneotomasyonu.Algorithm.HastaHeap;
import org.example.hastaneotomasyonu.models.Hasta;

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
        HastaHeap heap = HelloController.HastaHeap; // bu satırı kendi yapına göre güncelle

        // Hasta[] -> ObservableList'e dönüşüm
        ObservableList<Hasta> veri = FXCollections.observableArrayList(heap.getTumHastalar());

        // Tabloya veriyi bağla
        tableView.setItems(veri);
    }

}
