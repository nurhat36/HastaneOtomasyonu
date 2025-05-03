package org.example.hastaneotomasyonu;

import javafx.animation.KeyFrame;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.animation.Timeline;
import javafx.util.Duration;
import org.example.hastaneotomasyonu.Algorithm.HastaHeap;
import org.example.hastaneotomasyonu.models.Hasta;
import org.example.hastaneotomasyonu.Services.ClockService;

import java.io.IOException;

public class HelloApplication extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        startClockUpdater();
        scene = new Scene(loadFXML("hello-view"));
        stage.setScene(scene);
        stage.sizeToScene();
        stage.show();
    }
    private void startClockUpdater() {
        Timeline clock = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            ClockService.updateTime();
            // İsteğe bağlı: GUI elemanlarını güncelleyin
        }));
        clock.setCycleCount(Timeline.INDEFINITE);
        clock.play();
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

        launch();
    }
}