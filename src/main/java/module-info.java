module org.ifandidesignbeurau.pa {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires com.dlsc.formsfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires java.sql;
    requires jdk.compiler;
    requires mysql.connector.j;
    requires java.desktop;
    requires org.apache.commons.io;
    requires MaterialFX;

    opens org.ifandidesignbeurau.pa to javafx.fxml;
    exports org.ifandidesignbeurau.pa;
}