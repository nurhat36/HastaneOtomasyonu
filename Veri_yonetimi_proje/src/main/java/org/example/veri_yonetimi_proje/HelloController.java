package org.example.veri_yonetimi_proje;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import org.example.veri_yonetimi_proje.model.Ogrenci;
import org.example.veri_yonetimi_proje.storage.FileManager;

import java.io.IOException;
import java.util.List;

public class HelloController {

    @FXML private TableView<Ogrenci> tblOgrenciler;
    @FXML private TableColumn<Ogrenci, Integer> colNo;
    @FXML private TableColumn<Ogrenci, String> colAd;
    @FXML private TableColumn<Ogrenci, String> colSoyad;
    @FXML private TableColumn<Ogrenci, Float> colGano;
    @FXML private TableColumn<Ogrenci, Integer> colSinif;
    @FXML private TableColumn<Ogrenci, Integer> colBolumSira;
    @FXML private TableColumn<Ogrenci, Character> colCinsiyet;
    @FXML private TextField txtArama;

    private final ObservableList<Ogrenci> ogrenciListesi = FXCollections.observableArrayList();
    private final FileManager fileManager = new FileManager("ogrenciler.txt");

    @FXML
    public void initialize() {
        colNo.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getOgrNo()).asObject());
        colAd.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getIsim()));
        colSoyad.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getSoyad()));
        colGano.setCellValueFactory(cellData -> new javafx.beans.property.SimpleFloatProperty(cellData.getValue().getGano()).asObject());
        colSinif.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getSinif()).asObject());
        colBolumSira.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getBolumSira()).asObject());
        colCinsiyet.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getCinsiyet()));

        // Dosyadan verileri yükle
        try {
            List<Ogrenci> list = fileManager.readAll();
            ogrenciListesi.setAll(list);
            tblOgrenciler.setItems(ogrenciListesi);
        } catch (IOException e) {
            showAlert("Dosya Okuma Hatası", "ogrenciler.txt dosyası okunamadı!");
        }
    }

    @FXML
    private void onYeniOgrenciEkle() {
        Ogrenci yeni = new Ogrenci("Yeni", "Ogrenci", 10000 + ogrenciListesi.size(), 3.00f, 1, 1, 1, 'E');
        ogrenciListesi.add(yeni);
        tblOgrenciler.refresh();

        try {
            fileManager.append(yeni);
            showAlert("Başarılı", "Yeni öğrenci eklendi!");
        } catch (IOException e) {
            showAlert("Hata", "Dosyaya yazılamadı!");
        }
    }

    @FXML
    private void onOgrenciGuncelle() {
        Ogrenci secili = tblOgrenciler.getSelectionModel().getSelectedItem();
        if (secili == null) {
            showAlert("Uyarı", "Lütfen bir öğrenci seçin!");
            return;
        }

        secili.setGano(secili.getGano() + 0.1f);
        tblOgrenciler.refresh();
        try {
            fileManager.overwriteAll(ogrenciListesi);
            showAlert("Başarılı", "Öğrenci güncellendi!");
        } catch (IOException e) {
            showAlert("Hata", "Güncelleme başarısız!");
        }
    }

    @FXML
    private void onOgrenciSil() {
        Ogrenci secili = tblOgrenciler.getSelectionModel().getSelectedItem();
        if (secili == null) {
            showAlert("Uyarı", "Lütfen silinecek öğrenciyi seçin!");
            return;
        }

        ogrenciListesi.remove(secili);
        tblOgrenciler.refresh();
        try {
            fileManager.overwriteAll(ogrenciListesi);
            showAlert("Başarılı", "Öğrenci silindi!");
        } catch (IOException e) {
            showAlert("Hata", "Silme işlemi başarısız!");
        }
    }

    @FXML
    private void onTumListele() {
        try {
            ogrenciListesi.setAll(fileManager.readAll());
            tblOgrenciler.setItems(ogrenciListesi);
        } catch (IOException e) {
            showAlert("Hata", "Listeleme başarısız!");
        }
    }

    @FXML
    private void onNumarayaGoreAra() {
        String input = txtArama.getText().trim();
        if (input.isEmpty()) return;
        try {
            int no = Integer.parseInt(input);
            Ogrenci bulunan = ogrenciListesi.stream()
                    .filter(o -> o.getOgrNo() == no)
                    .findFirst().orElse(null);
            if (bulunan != null)
                tblOgrenciler.getSelectionModel().select(bulunan);
            else
                showAlert("Bulunamadı", "Bu numaraya ait öğrenci yok!");
        } catch (NumberFormatException e) {
            showAlert("Hata", "Geçerli bir numara girin!");
        }
    }

    @FXML
    private void onAdaGoreAra() {
        String ad = txtArama.getText().trim().toLowerCase();
        if (ad.isEmpty()) return;

        ObservableList<Ogrenci> filtreli = ogrenciListesi.filtered(o -> o.getIsim().toLowerCase().contains(ad));
        tblOgrenciler.setItems(filtreli);
    }

    @FXML
    private void onHashGoster() {
        showAlert("Bilgi", "Hash tablosu gösterimi bu ekranda ileride eklenecek!");
    }

    @FXML
    private void onAraTikla() {
        onNumarayaGoreAra(); // hızlı arama için
    }

    private void showAlert(String baslik, String mesaj) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(baslik);
        alert.setHeaderText(null);
        alert.setContentText(mesaj);
        alert.showAndWait();
    }
}
