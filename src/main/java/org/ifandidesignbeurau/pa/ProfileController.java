package org.ifandidesignbeurau.pa;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.UUID;

public class ProfileController implements Initializable {
    @FXML private Button btnUbahEmail;

    @FXML private Button btnUbahPassword;

    @FXML private Button btnUbahUsername;

    @FXML private Button btnUbahGambar;

    @FXML private ImageView imgPP;

    @FXML private TextField txtEmail;

    @FXML private TextField txtPassword;

    @FXML private TextField txtUsername;

    @FXML private Label txtWarning;

    Connection connection = DB.connectDB();

    private int UserID;
    private String username;
    private String password;
    private String email;
    private File currentFile;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        txtWarning.setVisible(false);
    }

    private void getImage() {
        FileChooser fileChooser = new FileChooser();

        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("(*.JPG *.PNG *.jpg *.png)", "*.JPG", "jpg files (*.jpg)", "*.jpg", "PNG files (*.PNG)", "*.PNG", "png files (*.png)", "*.png");
        fileChooser.getExtensionFilters().add(extFilter);

        File file = fileChooser.showOpenDialog(null);

        if (file != null) {
            Image image = new Image(file.toURI().toString());
            imgPP.setImage(image);
            currentFile = file;
        }
    }

    public void setProfileData(int uid) {
        try {
            ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM akun WHERE uid = " + uid);
            if (resultSet.next()){
                username = resultSet.getString("username");
                password =  resultSet.getString("password");
                email =  resultSet.getString("email");

                txtUsername.setText(username);
                txtPassword.setText(password);
                txtEmail.setText(email);

                File file = new File(resultSet.getString("gambar"));
                if (!resultSet.getString("gambar").isEmpty()){
                    currentFile = file;
                    Image image = new Image(file.toURI().toString());
                    imgPP.setImage(image);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        btnUbahUsername.setOnAction(actionEvent -> UbahUsername(uid));
        btnUbahEmail.setOnAction(actionEvent -> UbahEmail(uid));
        btnUbahPassword.setOnAction(actionEvent -> UbahPassword(uid));

        imgPP.setOnMouseClicked(mouseEvent -> getImage());
        btnUbahGambar.setOnAction(actionEvent -> UbahGambar(uid));
    }

    private void UbahUsername(int uid) {
        try {
            PreparedStatement statement = connection.prepareStatement("UPDATE akun SET username=? WHERE uid=" + uid);
            ResultSet resultSet = connection.createStatement().executeQuery("SELECT username FROM akun WHERE uid=" + uid);
            if (!resultSet.next()){
                statement.setString(1, txtUsername.getText());
                statement.executeUpdate();
                NotifCustom.AlertNotifINFORMATION("Informasi", "Edit Username", "Username berhasil diubah");
            }else {
                txtWarning.setText("Username sudah ada!");
                txtWarning.setVisible(true);
                PauseTransition visiblePause = new PauseTransition(Duration.seconds(3));
                visiblePause.setOnFinished(event -> txtWarning.setVisible(false));

                resultSet.close();
                visiblePause.play();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void UbahPassword(int uid) {
        try {
            PreparedStatement statement = connection.prepareStatement("UPDATE akun SET password=? WHERE uid=" + uid);
            statement.setString(1, txtPassword.getText());
            statement.executeUpdate();
            NotifCustom.AlertNotifINFORMATION("Informasi", "Edit Password", "Password berhasil diubah");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void UbahEmail(int uid) {
        try {
            PreparedStatement statement = connection.prepareStatement("UPDATE akun SET email=? WHERE uid=" + uid);
            ResultSet resultSet = connection.createStatement().executeQuery("SELECT email FROM akun WHERE uid=" + uid);
            if (!resultSet.next()){
                statement.setString(1, txtEmail.getText());
                statement.executeUpdate();
                NotifCustom.AlertNotifINFORMATION("Informasi", "Edit Email", "Email berhasil diubah");
            }else {
                txtWarning.setText("Email sudah ada!");
                txtWarning.setVisible(true);
                PauseTransition visiblePause = new PauseTransition(Duration.seconds(3));
                visiblePause.setOnFinished(event -> txtWarning.setVisible(false));

                resultSet.close();
                visiblePause.play();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void UbahGambar(int uid) {
        try {
            PreparedStatement statement = connection.prepareStatement("UPDATE akun SET gambar=? WHERE uid=" + uid);
            Path destinationDir = Path.of("src/main/resources/img/fotoUser");
            try {
                ResultSet resultSet = connection.createStatement().executeQuery("SELECT gambar FROM akun WHERE uid=" + uid);
                if (resultSet.next()) {
                    if(!resultSet.getString("gambar").isEmpty()) {
                        Path path = Path.of(resultSet.getString("gambar")).toAbsolutePath();
                        if (currentFile != null) {
                            String fileName = username + "_" + UUID.randomUUID() + "_" + uid + "." + FilenameUtils.getExtension(currentFile.getName());
                            Path destinationFile = destinationDir.toAbsolutePath().resolve(fileName);
                            Files.copy(currentFile.toPath(), destinationFile, StandardCopyOption.REPLACE_EXISTING);
                            Files.deleteIfExists(path);
                            statement.setString(1, destinationDir.resolve(fileName).toString());

                            HelloController helloController = new HelloController();
                            Image image = new Image(currentFile.toURI().toString());
                            helloController.getInstance().getImgProfileUser().setImage(image);
                        }else {
                            statement.setString(1, "");
                        }
                    }else {
                        if (currentFile != null) {
                            String fileName = username + "_" + UUID.randomUUID() + "_" + uid + "." + FilenameUtils.getExtension(currentFile.getName());
                            Path destinationFile = destinationDir.toAbsolutePath().resolve(fileName);
                            Files.copy(currentFile.toPath(), destinationFile, StandardCopyOption.REPLACE_EXISTING);
                            statement.setString(1, destinationDir.resolve(fileName).toString());

                            HelloController helloController = new HelloController();
                            Image image = new Image(currentFile.toURI().toString());
                            helloController.getInstance().getImgProfileUser().setImage(image);
                        }else {
                            statement.setString(1, "");
                        }
                    }
                    statement.executeUpdate();
                    NotifCustom.AlertNotifINFORMATION("Informasi", "Edit Gambar", "Gambar berhasil diubah");
                }
            } catch (IOException | SQLException ex) {
                ex.printStackTrace();
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
