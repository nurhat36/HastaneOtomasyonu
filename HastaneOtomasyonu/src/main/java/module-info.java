module org.example.hastaneotomasyonu {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires freetts;

    opens org.example.hastaneotomasyonu to javafx.fxml;
    exports org.example.hastaneotomasyonu;
    exports org.example.hastaneotomasyonu.Controller;
    exports org.example.hastaneotomasyonu.models;
    opens org.example.hastaneotomasyonu.Controller to javafx.fxml;
    opens org.example.hastaneotomasyonu.models to javafx.base;
}