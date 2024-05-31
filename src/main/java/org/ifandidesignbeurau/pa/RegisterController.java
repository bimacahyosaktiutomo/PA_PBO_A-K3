package org.ifandidesignbeurau.pa;

import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class RegisterController implements Initializable {
    @FXML private Button btnRegister;

    @FXML private TextField txtEmailRegis;

    @FXML private PasswordField txtPasswordRegis;

    @FXML private TextField txtUsernameRegis;

    @FXML private Hyperlink hyperLogin;

    @FXML private Label txtWarning;

    private final Connection connection = DB.connectDB();

    public void initialize (URL location, ResourceBundle resource){
        txtWarning.setVisible(false);
    }

    // ACTION
    public void btnRegisterClicked(ActionEvent actionEvent) {Register(actionEvent);}
    public void HyperLoginClicked(ActionEvent actionEvent) {openLoginPopup(actionEvent);}

    // METHOD
    private void Register(ActionEvent actionEvent) {
        String username = txtUsernameRegis.getText().replaceAll("\\s", "");;
        String password = txtPasswordRegis.getText();
        String email = txtEmailRegis.getText().replaceAll("\\s", "");
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM akun WHERE username=? and email=?");
            statement.setString(1, username);
            statement.setString(2, email);
            ResultSet resultSet = statement.executeQuery();
            if (!resultSet.next()){
                statement = connection.prepareStatement("INSERT INTO akun values (NULL, ?, ?, ?, ?, '')");
                statement.setString(1, username);
                statement.setString(2, password);
                statement.setString(3, email);
                statement.setString(4, "user");
                statement.executeUpdate();

                NotifCustom.AlertNotifINFORMATION("Notifikasi", "Registrasi", "Registrasi Berhasil");

                Node source = (Node) actionEvent.getSource();
                Stage stage  = (Stage) source.getScene().getWindow();
                stage.close();
            }
            txtWarning.setVisible(true);
            PauseTransition visiblePause = new PauseTransition(Duration.seconds(3));
            visiblePause.setOnFinished(event -> txtWarning.setVisible(false));

            resultSet.close();
            visiblePause.play();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    private void openLoginPopup(ActionEvent actionEvent){
        Node source = (Node) actionEvent.getSource();
        Stage stage  = (Stage) source.getScene().getWindow();
        stage.close();

        Popup Logpopup = new Popup();
        Logpopup.setAutoHide(true);
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("login.fxml"));
        try {
            Scene scene = new Scene(fxmlLoader.load());
            Stage POPstage = new Stage();
            POPstage.setScene(scene);
            POPstage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
