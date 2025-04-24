package org.example.avlfx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    private AVLTree avlTree = new AVLTree();
    private AVLTreeVisualizer visualizer = new AVLTreeVisualizer();

    @Override
    public void start(Stage primaryStage) {
        VBox root = new VBox(10);
        TextField inputField = new TextField();
        inputField.setPromptText("Sayı girin...");
        Button btnEkle = new Button("Ekle");

        Pane treePane = new Pane();
        treePane.setMinHeight(400);

        btnEkle.setOnAction(e -> {
            try {
                int key = Integer.parseInt(inputField.getText());
                avlTree.insert(key);
                visualizer.drawTree(treePane, avlTree.root);
            } catch (NumberFormatException ex) {
                inputField.setText("Geçersiz sayı!");
            }
        });
        TextField deleteField = new TextField();
        deleteField.setPromptText("Silinecek sayıyı girin...");
        Button btnSil = new Button("Sil");

        btnSil.setOnAction(e -> {
            try {
                int key = Integer.parseInt(deleteField.getText());
                avlTree.delete(key);
                visualizer.drawTree(treePane, avlTree.root);
            } catch (NumberFormatException ex) {
                deleteField.setText("Geçersiz!");
            }
        });


        root.getChildren().addAll(inputField, btnEkle, deleteField, btnSil, treePane);

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("AVL Ağaç Görselleştirme");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}