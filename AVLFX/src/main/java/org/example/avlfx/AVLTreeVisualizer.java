package org.example.avlfx;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class AVLTreeVisualizer {
    private static final int NODE_RADIUS = 20;
    private static final int H_GAP = 40;
    private static final int V_GAP = 50;

    public void drawTree(Pane pane, AVLNode root) {
        pane.getChildren().clear();
        drawNode(pane, root, 400, 40, 200);
    }

    private void drawNode(Pane pane, AVLNode node, double x, double y, double gap) {
        if (node == null) return;

        if (node.left != null) {
            double childX = x - gap;
            double childY = y + V_GAP;
            Line line = new Line(x, y, childX, childY);
            pane.getChildren().add(line);
            drawNode(pane, node.left, childX, childY, gap / 1.5);
        }

        if (node.right != null) {
            double childX = x + gap;
            double childY = y + V_GAP;
            Line line = new Line(x, y, childX, childY);
            pane.getChildren().add(line);
            drawNode(pane, node.right, childX, childY, gap / 1.5);
        }

        Circle circle = new Circle(x, y, NODE_RADIUS);
        circle.setFill(Color.LIGHTBLUE);
        circle.setStroke(Color.DARKBLUE);
        animateNodeAppear(circle);

        Text text = new Text(x - 6, y + 4, String.valueOf(node.key));
        pane.getChildren().addAll(circle, text);
    }

    private void animateNodeAppear(Node node) {
        FadeTransition ft = new FadeTransition(Duration.millis(500), node);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.play();
    }

    public void animateNodeDisappear(Node node, Pane pane) {
        FadeTransition ft = new FadeTransition(Duration.millis(500), node);
        ft.setFromValue(1.0);
        ft.setToValue(0.0);
        ft.setOnFinished(e -> pane.getChildren().remove(node));
        ft.play();
    }

    public void animateMove(Node node, double newX, double newY) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(500), node);
        tt.setToX(newX);
        tt.setToY(newY);
        tt.play();
    }
}