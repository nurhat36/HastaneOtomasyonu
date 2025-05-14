package org.example.hastaneotomasyonu.Controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;
import org.example.hastaneotomasyonu.Algorithm.HastaHeap;
import org.example.hastaneotomasyonu.models.Hasta;

public class HeapViewController {

    @FXML
    private Canvas canvas;

    private HastaHeap heap;
    private static final double NODE_RADIUS = 35;  // Increased node size
    private static final double VERTICAL_SPACING = 100;  // Increased vertical spacing
    private static final double INITIAL_HORIZONTAL_OFFSET = 40;  // Increased initial spacing
    private static final double WIDTH_SCALE_FACTOR = 2.2;  // Controls how much nodes spread horizontally
    private Timeline heapGuncellemeZamanlayici;

    public void baslatHeapGuncelleme() {
        if (heapGuncellemeZamanlayici != null) {
            heapGuncellemeZamanlayici.stop();
        }

        heapGuncellemeZamanlayici = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            refresh(); // her saniye tekrar çiz
        }));
        heapGuncellemeZamanlayici.setCycleCount(Timeline.INDEFINITE);
        heapGuncellemeZamanlayici.play();
    }

    public void setHeap(HastaHeap heap) {
        this.heap = heap;
        drawHeap();              // ilk yüklemede bir kez çiz
        baslatHeapGuncelleme();  // sonra periyodik olarak yenile
    }


    private void drawHeap() {
        if (heap == null || heap.boyut() == 0) {
            clearCanvas();
            return;
        }

        clearCanvas();
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Calculate the maximum depth to adjust spacing
        int depth = calculateDepth(heap.getRoot());
        double dynamicOffset = INITIAL_HORIZONTAL_OFFSET * Math.pow(1.5, depth-1);

        drawNode(gc, heap.getRoot(), canvas.getWidth() / 2, 60, dynamicOffset, depth);
    }

    private int calculateDepth(HastaHeap.Node node) {
        if (node == null) return 0;
        return 1 + Math.max(calculateDepth(node.getLeft()), calculateDepth(node.getRight()));
    }

    private void clearCanvas() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    private void drawNode(GraphicsContext gc, HastaHeap.Node node,
                          double x, double y, double horizontalOffset, int depth) {
        if (node == null) return;

        Hasta hasta = node.getData();

        // Draw node (ellipse shape for better text fit)
        gc.setFill(getPriorityColor(hasta.getOncelikPuani()));
        gc.fillRoundRect(x - NODE_RADIUS, y - NODE_RADIUS/2,
                NODE_RADIUS*2, NODE_RADIUS, 20, 20);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1.5);
        gc.strokeRoundRect(x - NODE_RADIUS, y - NODE_RADIUS/2,
                NODE_RADIUS*2, NODE_RADIUS, 20, 20);

        // Draw node information with better formatting
        gc.setFill(Color.BLACK);
        gc.setFont(Font.font(12));

        String[] lines = {
                hasta.hastaAdi,
                "Puan: " + hasta.getOncelikPuani(),
                String.format("Saat: %04.2f", hasta.muayeneSaati)
        };

        // Center text vertically in the node
        double textY = y - NODE_RADIUS/2 + 15;
        for (String line : lines) {
            // Center text horizontally
            double textWidth = getTextWidth(gc, line);
            gc.fillText(line, x - textWidth/2, textY);
            textY += 15;
        }

        // Calculate dynamic offset for children based on depth
        double childOffset = horizontalOffset / WIDTH_SCALE_FACTOR;

        // Draw left child
        if (node.getLeft() != null) {
            double childX = x - horizontalOffset;
            double childY = y + VERTICAL_SPACING;
            drawConnection(gc, x, y + NODE_RADIUS/2, childX, childY - NODE_RADIUS/2);
            drawNode(gc, node.getLeft(), childX, childY, childOffset, depth-1);
        }

        // Draw right child
        if (node.getRight() != null) {
            double childX = x + horizontalOffset;
            double childY = y + VERTICAL_SPACING;
            drawConnection(gc, x, y + NODE_RADIUS/2, childX, childY - NODE_RADIUS/2);
            drawNode(gc, node.getRight(), childX, childY, childOffset, depth-1);
        }
    }

    private double getTextWidth(GraphicsContext gc, String text) {
        javafx.scene.text.Font font = gc.getFont();
        javafx.scene.text.Text helper = new javafx.scene.text.Text(text);
        helper.setFont(font);
        return helper.getLayoutBounds().getWidth();
    }

    private void drawConnection(GraphicsContext gc, double x1, double y1, double x2, double y2) {
        gc.setStroke(Color.DARKGRAY);
        gc.setLineWidth(1.5);
        gc.strokeLine(x1, y1, x2, y2);
    }

    private Color getPriorityColor(int priority) {
        // More distinct color variation based on priority
        double hue = 200 - (priority * 0.5); // Blue to green gradient
        hue = Math.max(120, Math.min(240, hue)); // Keep in blue-green range
        double saturation = 0.7;
        double brightness = 0.8 - (priority / 300.0);
        brightness = Math.max(0.5, Math.min(0.9, brightness));

        return Color.hsb(hue, saturation, brightness);
    }

    public void refresh() {
        drawHeap();
    }

    public void durdurGuncelleme() {
        if (heapGuncellemeZamanlayici != null) {
            heapGuncellemeZamanlayici.stop();
        }
    }

}