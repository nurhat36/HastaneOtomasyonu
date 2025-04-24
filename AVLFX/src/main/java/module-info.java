module org.example.avlfx {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;

    opens org.example.avlfx to javafx.fxml;
    exports org.example.avlfx;
}