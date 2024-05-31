package org.ifandidesignbeurau.pa;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class UserFormController implements Initializable {
    Connection connection = DB.connectDB();

    @FXML private TextField txtUsername;
    @FXML private TextField txtPassword;
    @FXML private TextField txtEmail;
    @FXML private Label txtWarning;

    @FXML private Button btnBatal;

    @FXML private Button btnEdit;

    private static UserFormController instance;
    public UserFormController getInstance() {
        return instance;
    }


    private int setUID;
    public void setSetUID(int setUID) {
        this.setUID = setUID;
    }

    private String username;
    private String password;
    private String email;
    Stage MeStage;

    public void initialize(URL url, ResourceBundle resourceBundle) {
        instance = this;
        txtWarning.setVisible(false);

        btnEdit.setOnAction(actionEvent -> Edit());
        btnBatal.setOnAction(actionEvent -> Batal());
    }

    public void setUserForm(int uid, Stage stage) throws SQLException {
        ResultSet resultSet = connection.createStatement().executeQuery("SELECT username, password, email, izin FROM akun WHERE uid = " + uid);
        MeStage = stage;

        if (resultSet.next()){
            username = resultSet.getString("username");
            password =  resultSet.getString("password");
            email =  resultSet.getString("email");

            txtUsername.setText(username);
            txtPassword.setText(password);
            txtEmail.setText(email);
        }
    }

    private void Edit(){
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM akun WHERE username=? and email=?");
            statement.setString(1, txtUsername.getText());
            statement.setString(2, txtEmail.getText());
            ResultSet resultSet = statement.executeQuery();
            if (!resultSet.next()){
                statement = connection.prepareStatement("UPDATE akun SET username=?, password=?, email=? WHERE uid =" + setUID );
                statement.setString(1, txtUsername.getText().replaceAll("\\s", ""));
                statement.setString(2, txtPassword.getText().replaceAll("\\s", ""));
                statement.setString(3, txtEmail.getText().replaceAll("\\s", ""));
                statement.executeUpdate();

                NotifCustom.AlertNotifINFORMATION("Notifikasi", "Edit Data", "Data Berhasil Diubah!");
                MeStage.close();

                DashboardAdminController dashboardAdminController = new DashboardAdminController();
                dashboardAdminController.getInstance().getRefreshAdmin();
            }
            txtWarning.setVisible(true);
            PauseTransition visiblePause = new PauseTransition(Duration.seconds(3));
            visiblePause.setOnFinished(event -> txtWarning.setVisible(false));

            resultSet.close();
            visiblePause.play();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void Batal(){
        txtUsername.setText(username);
        txtPassword.setText(password);
        txtEmail.setText(email);
    }
}
