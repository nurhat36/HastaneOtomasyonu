package org.example.hastaneotomasyonu.Controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.web.WebView;
import javafx.util.Duration;
import org.example.hastaneotomasyonu.Algorithm.HastaHeap;
import org.example.hastaneotomasyonu.models.Hasta;

public class HeapViewController {

    @FXML
    private Canvas canvas;
    private Pane infoPanel;
    private Label infoLabel;
    private boolean panelVisible = false;
    private WebView infoWebView;

    private HastaHeap heap;
    private static final double NODE_RADIUS = 35;  // Increased node size
    private static final double VERTICAL_SPACING = 100;  // Increased vertical spacing
    private static final double INITIAL_HORIZONTAL_OFFSET = 40;  // Increased initial spacing
    private static final double WIDTH_SCALE_FACTOR = 2.2;  // Controls how much nodes spread horizontally
    private Timeline heapGuncellemeZamanlayici;
    @FXML
    public void initialize() {
        // Create the info panel with WebView for rich text
        infoPanel = new Pane();
        infoPanel.setStyle("-fx-background-color: #ffffff; " +
                "-fx-border-color: #3498db; " +
                "-fx-border-width: 2; " +
                "-fx-border-radius: 8; " +
                "-fx-background-radius: 8; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 12, 0, 0, 3);");
        infoPanel.setVisible(false);
        infoPanel.setManaged(false);

        infoWebView = new WebView();
        infoWebView.setPrefSize(300, 250);
        infoWebView.setContextMenuEnabled(false);

        infoPanel.getChildren().add(infoWebView);

        if (canvas.getParent() instanceof Pane) {
            ((Pane)canvas.getParent()).getChildren().add(infoPanel);
        }

        canvas.setOnMouseMoved(this::handleMouseMove);
        canvas.setOnMouseExited(e -> hideInfoPanel());
    }
    private void handleMouseMove(MouseEvent event) {
        if (heap == null || heap.boyut() == 0) {
            hideInfoPanel();
            return;
        }

        HastaHeap.Node node = findNodeAtPosition(heap.getRoot(),
                canvas.getWidth() / 2, 60,
                INITIAL_HORIZONTAL_OFFSET * Math.pow(1.5, calculateDepth(heap.getRoot())-1),
                event.getX(), event.getY());

        if (node != null) {
            showInfoPanel(node.getData(), event.getX(), event.getY());
        } else {
            hideInfoPanel();
        }
    }

    private HastaHeap.Node findNodeAtPosition(HastaHeap.Node node, double nodeX, double nodeY,
                                              double horizontalOffset, double mouseX, double mouseY) {
        if (node == null) return null;

        // Check if mouse is inside this node
        if (mouseX >= nodeX - NODE_RADIUS && mouseX <= nodeX + NODE_RADIUS &&
                mouseY >= nodeY - NODE_RADIUS/2 && mouseY <= nodeY + NODE_RADIUS/2) {
            return node;
        }

        // Calculate dynamic offset for children
        double childOffset = horizontalOffset / WIDTH_SCALE_FACTOR;
        double childY = nodeY + VERTICAL_SPACING;

        // Check left child
        if (node.getLeft() != null) {
            double childX = nodeX - horizontalOffset;
            HastaHeap.Node found = findNodeAtPosition(node.getLeft(), childX, childY, childOffset, mouseX, mouseY);
            if (found != null) return found;
        }

        // Check right child
        if (node.getRight() != null) {
            double childX = nodeX + horizontalOffset;
            HastaHeap.Node found = findNodeAtPosition(node.getRight(), childX, childY, childOffset, mouseX, mouseY);
            if (found != null) return found;
        }

        return null;
    }

    private void showInfoPanel(Hasta hasta, double x, double y) {
        // HTML template with icons (using Font Awesome icons via CDN)
        String htmlTemplate = """
        <html>
        <head>
            <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
            <style>
                body {
                    font-family: 'Segoe UI', Arial, sans-serif;
                    background-color: #f8f9fa;
                    padding: 12px;
                    color: #333;
                    font-size: 13px;
                }
                .info-item {
                    margin-bottom: 8px;
                    display: flex;
                    align-items: center;
                }
                .icon {
                    width: 20px;
                    text-align: center;
                    margin-right: 10px;
                    color: #3498db;
                }
                .header {
                    font-weight: bold;
                    color: #2c3e50;
                    margin-bottom: 12px;
                    font-size: 16px;
                    border-bottom: 1px solid #eee;
                    padding-bottom: 6px;
                }
                .priority {
                    display: inline-block;
                    padding: 2px 8px;
                    border-radius: 12px;
                    background-color: %priorityColor%;
                    color: white;
                    font-weight: bold;
                    font-size: 12px;
                }
            </style>
        </head>
        <body>
            <div class="header">
                <i class="fas fa-user-circle"></i> Hasta Bilgileri
            </div>
            
            <div class="info-item">
                <div class="icon"><i class="fas fa-id-card"></i></div>
                <div><b>Adı:</b> %name%</div>
            </div>
            
            <div class="info-item">
                <div class="icon"><i class="fas fa-birthday-cake"></i></div>
                <div><b>Yaş:</b> %age%</div>
            </div>
            
            <div class="info-item">
                <div class="icon"><i class="fas fa-venus-mars"></i></div>
                <div><b>Cinsiyet:</b> %gender%</div>
            </div>
            
            <div class="info-item">
                <div class="icon"><i class="fas fa-handcuffs"></i></div>
                <div><b>Mahkum:</b> %prisoner%</div>
            </div>
            
            <div class="info-item">
                <div class="icon"><i class="fas fa-wheelchair"></i></div>
                <div><b>Engellilik:</b> %% %disability%</div>
            </div>
            
            <div class="info-item">
                <div class="icon"><i class="fas fa-tint"></i></div>
                <div><b>Kanama Durumu:</b> %bleeding%</div>
            </div>
            
            <div class="info-item">
                <div class="icon"><i class="fas fa-clock"></i></div>
                <div><b>Kayıt Saati:</b> %registerTime%</div>
            </div>
            
            <div class="info-item">
                <div class="icon"><i class="fas fa-stethoscope"></i></div>
                <div><b>Muayene Saati:</b> %examTime%</div>
            </div>
            
            <div class="info-item">
                <div class="icon"><i class="fas fa-stopwatch"></i></div>
                <div><b>Muayene Süresi:</b> %duration% dk</div>
            </div>
            
            <div class="info-item">
                <div class="icon"><i class="fas fa-exclamation-triangle"></i></div>
                <div><b>Öncelik Puanı:</b> <span class="priority">%priority%</span></div>
            </div>
        </body>
        </html>
        """;

        // Get color based on priority
        String priorityColor = getPriorityColorHex(hasta.oncelikPuani);

        // Replace placeholders with actual data
        String htmlContent = htmlTemplate
                .replace("%name%", escapeHtml(hasta.hastaAdi))
                .replace("%age%", String.valueOf(hasta.hastaYasi))
                .replace("%gender%", escapeHtml(hasta.cinsiyet))
                .replace("%prisoner%", hasta.mahkumlukDurumBilgisi ? "Evet" : "Hayır")
                .replace("%disability%", String.valueOf(hasta.engellilikOrani))
                .replace("%bleeding%", escapeHtml(hasta.kanamaliHastaDurumBilgisi))
                .replace("%registerTime%", String.format("%.2f", hasta.hastaKayitSaati))
                .replace("%examTime%", String.format("%.2f", hasta.muayeneSaati))
                .replace("%duration%", String.valueOf(hasta.muayeneSuresi))
                .replace("%priority%", String.valueOf(hasta.oncelikPuani))
                .replace("%priorityColor%", priorityColor);

        infoWebView.getEngine().loadContent(htmlContent);
        infoPanel.setVisible(true);
        infoPanel.setManaged(true);

        // Position the panel
        double panelWidth = 320;
        double panelHeight = 280;
        double panelX = x + 25;
        double panelY = y - panelHeight/2;

        // Adjust position if it would go off-screen
        if (panelX + panelWidth > canvas.getWidth()) {
            panelX = x - panelWidth - 25;
        }
        if (panelY + panelHeight > canvas.getHeight()) {
            panelY = canvas.getHeight() - panelHeight - 15;
        }
        if (panelY < 15) {
            panelY = 15;
        }

        infoPanel.setLayoutX(panelX);
        infoPanel.setLayoutY(panelY);
        infoPanel.setPrefSize(panelWidth, panelHeight);
        panelVisible = true;

        refresh();
    }

    private String escapeHtml(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private String getPriorityColorHex(int priority) {
        // Convert your existing color to hex
        Color color = getPriorityColor(priority);
        return String.format("#%02X%02X%02X",
                (int)(color.getRed() * 255),
                (int)(color.getGreen() * 255),
                (int)(color.getBlue() * 255));
    }

    private void hideInfoPanel() {
        infoPanel.setVisible(false);
        infoPanel.setManaged(false);
        panelVisible = false;
    }

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
                "Puan: " + hasta.getOncelikPuani()

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
        hideInfoPanel();
    }

}