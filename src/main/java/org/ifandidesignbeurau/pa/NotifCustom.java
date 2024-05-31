package org.ifandidesignbeurau.pa;

import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.util.Optional;

public class NotifCustom extends Alat {
    public static void AlertNotifINFORMATION(String tittle, String header, String text) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, text, ButtonType.CLOSE);
        alert.setTitle(tittle);alert.setHeaderText(header);alert.showAndWait();
    }

    public static void AlertNotifWARNING(String tittle, String header, String text) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, text, ButtonType.CLOSE);
        alert.setTitle(tittle);alert.setHeaderText(header);alert.showAndWait();
    }

    public static boolean AlertNotifCONFIRM(String tittle, String header, String text) {
        Alert alert = new Alert(Alert.AlertType.WARNING, text,ButtonType.YES, ButtonType.NO);
        alert.setTitle(tittle);alert.setHeaderText(header);

        Optional<ButtonType> result = alert.showAndWait();

        return result.isPresent() && result.get() == ButtonType.YES;
    }

    public static void AlertNotif(String tittle, String header, String text) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, text, ButtonType.CLOSE);
        alert.setTitle(tittle);alert.setHeaderText(header);alert.showAndWait();
    }

    public static Stage CloseMeStage (ActionEvent actionEvent){
        Node source = (Node) actionEvent.getSource();
        Stage stage  = (Stage) source.getScene().getWindow();
        stage.close();
        return stage;
    }
}
