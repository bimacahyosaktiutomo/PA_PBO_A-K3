package org.ifandidesignbeurau.pa;

import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.ifandidesignbeurau.pa.Important.SesiUser;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class LoginController implements Initializable {
    @FXML private Button btnLogin;

    @FXML private PasswordField txtPassword;

    @FXML private Label txtWarning;

    @FXML private Hyperlink hyperRegister;

    @FXML private TextField txtUsername;

    static Stage loginStage;

    private final Connection connection = DB.connectDB();

    //ACTION
    public void btnLoginClicked(ActionEvent actionEvent) throws IOException {Login(actionEvent);}
    public void hyperRegisterClicked(ActionEvent actionEvent){openRegisterPopup(actionEvent);}

    public void initialize (URL location, ResourceBundle resource){
        txtWarning.setVisible(false);
    }

    //METHOD
    private void Login(ActionEvent actionEvent) throws IOException {
        String username = txtUsername.getText();
        String password = txtPassword.getText();
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM akun WHERE username=? AND password=?");
            statement.setString(1, username);
            statement.setString(2, password);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                SesiUser.LoginSession = 1;
                SesiUser.uid = resultSet.getInt("uid");
                SesiUser.username = resultSet.getString("username");
                SesiUser.email = resultSet.getString("email");
                SesiUser.izin = resultSet.getString("izin");

                NotifCustom.AlertNotifINFORMATION("Notifikasi", "Login", "Login Berhasil");

                Stage stage = NotifCustom.CloseMeStage(actionEvent);
                loginStage = stage;

                HelloApplication.primaryStage.close();
                Stage Home = new Stage();
                HelloApplication.HomeRefresh(Home);

            } else {
                txtWarning.setVisible(true);
                PauseTransition visiblePause = new PauseTransition(Duration.seconds(3));
                visiblePause.setOnFinished(event -> txtWarning.setVisible(false));
                visiblePause.play();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void openRegisterPopup(ActionEvent actionEvent) {
        NotifCustom.CloseMeStage(actionEvent);

        Popup REGpopup = new Popup();
        REGpopup.setAutoHide(true);
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("register.fxml"));
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
