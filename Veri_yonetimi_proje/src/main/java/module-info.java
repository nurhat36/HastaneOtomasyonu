module org.example.veri_yonetimi_proje {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;

    opens org.example.veri_yonetimi_proje to javafx.fxml;
    exports org.example.veri_yonetimi_proje;
}