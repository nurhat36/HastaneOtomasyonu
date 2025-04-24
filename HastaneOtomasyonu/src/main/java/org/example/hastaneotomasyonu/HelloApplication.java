package org.example.hastaneotomasyonu;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.example.hastaneotomasyonu.Algorithm.HastaHeap;
import org.example.hastaneotomasyonu.models.Hasta;

import java.io.IOException;

public class HelloApplication extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        scene = new Scene(loadFXML("hello-view"));
        stage.setScene(scene);
        stage.sizeToScene();
        stage.show();
    }

    public static void setRoot(String fxml) throws IOException {
        // FXML yükleniyor
        Parent rootNode = loadFXML(fxml);

        // Yeni bir StackPane oluştur
        StackPane root = new StackPane(rootNode);

        // Yeni bir Stage oluştur (Açılan pencere)
        Stage newStage = new Stage();
        newStage.setTitle(fxml);
        newStage.setScene(new Scene(root)); // Yeni sahneyi oluştur ve ata

        // Stage'i ekrana ortala ve göster
        newStage.centerOnScreen();
        newStage.show();
    }


    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("/org/example/hastaneotomasyonu/"+fxml + ".fxml"));
        return fxmlLoader.load();
    }


    public static void main(String[] args) {
        Hasta hasta1 = new Hasta("Ali", 70, "Erkek", false, 10, "yok", 8.30);
        hasta1.oncelikPuaniHesapla();

        Hasta hasta2 = new Hasta("Ayşe", 30, "Kadın", false, 0, "kanama", 8.45);
        hasta2.oncelikPuaniHesapla();

        Hasta hasta3 = new Hasta("Mehmet", 50, "Erkek", true, 0, "agirkanama", 8.15);
        hasta3.oncelikPuaniHesapla();

        HastaHeap heap = new HastaHeap(10);
        heap.ekle(hasta1);
        heap.ekle(hasta2);
        heap.ekle(hasta3);

        System.out.println("Hastalar öncelik sırasına göre çıkarılıyor:");
        while (!heap.bosMu()) {
            Hasta h = heap.cikar();
            System.out.println(h.getHastaAdi() + " - Öncelik: " + h.getOncelikPuani());
        }
        launch();
    }
}