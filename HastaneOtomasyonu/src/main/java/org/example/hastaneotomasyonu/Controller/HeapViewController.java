package org.example.hastaneotomasyonu.Controller;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.example.hastaneotomasyonu.Algorithm.HastaHeap;
import org.example.hastaneotomasyonu.models.Hasta;

public class HeapViewController {

    @FXML
    private Canvas canvas;

    private HastaHeap heap;

    public void setHeap(HastaHeap heap) {
        this.heap = heap;
        drawHeap();
    }

    private void drawHeap() {
        if (heap == null || heap.boyut() == 0) return;

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        drawNode(gc, heap.getRoot(), canvas.getWidth() / 2, 50, canvas.getWidth() / 4);
    }

    private void drawNode(GraphicsContext gc, HastaHeap.Node node, double x, double y, double offset) {
        if (node == null) return;

        Hasta hasta = node.getData();

        // Daire çiz
        gc.setFill(Color.LIGHTBLUE);
        gc.fillOval(x - 30, y - 30, 60, 60);
        gc.setStroke(Color.BLACK);
        gc.strokeOval(x - 30, y - 30, 60, 60);

        // Yazı ekle
        gc.setFill(Color.BLACK);
        gc.setFont(Font.font(12));
        String text = hasta.getOncelikPuani() + "\n" + hasta.hastaAdi+"\n"+hasta.muayeneSaati;
        gc.fillText(text, x - 25, y - 10);

        // Sol çocuk
        if (node.getLeft() != null) {
            double childX = x - offset;
            double childY = y + 80;
            gc.strokeLine(x, y + 30, childX, childY - 30);
            drawNode(gc, node.getLeft(), childX, childY, offset / 2);
        }

        // Sağ çocuk
        if (node.getRight() != null) {
            double childX = x + offset;
            double childY = y + 80;
            gc.strokeLine(x, y + 30, childX, childY - 30);
            drawNode(gc, node.getRight(), childX, childY, offset / 2);
        }
    }
}
