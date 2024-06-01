package org.ifandidesignbeurau.pa;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.ifandidesignbeurau.pa.Important.SesiUser;

import javafx.scene.image.Image;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FilenameUtils;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.util.ResourceBundle;
import java.util.UUID;

import static java.lang.Math.round;

public class ResepFormController implements Initializable {

    @FXML private SVGPath bintang1;
    @FXML private SVGPath bintang2;
    @FXML private SVGPath bintang3;
    @FXML private SVGPath bintang4;
    @FXML private SVGPath bintang5;

    @FXML private Button btnBatal;
    @FXML private Button btnLater;
    @FXML private Button btnSelesai;
    @FXML private ImageView imgThumbnail;
    @FXML private TextArea txtAlat;
    @FXML private TextArea txtAuthor;
    @FXML private TextArea txtBahan;
    @FXML private TextArea txtDeskripsi;
    @FXML private TextArea txtInstruksi;
    @FXML private TextArea txtNamaResep;
    @FXML private ChoiceBox<String> cbKategori;

    private int setIDResep;
    public void setSetIDResep(int setIDResep) {
        this.setIDResep = setIDResep;
    }

    Connection connection = DB.connectDB();

    private static ResepFormController instance;
    public ResepFormController getInstance() {
        return instance;
    }
    File currentFile;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        instance = this;

        txtAuthor.setDisable(true);
        cbKategori.getItems().addAll("Appetizer", "Main Course", "Dessert");
        cbKategori.setValue("Main Course");

        Platform.runLater(() -> {
            if (setIDResep > 0) {
                btnSelesai.setOnAction(actionEvent -> Edit("selesai", actionEvent, setIDResep));
                btnLater.setOnAction(actionEvent -> Edit("belum", actionEvent, setIDResep));
            } else {
                btnSelesai.setOnAction(actionEvent -> Selesai("selesai", actionEvent));
                btnLater.setOnAction(actionEvent -> Selesai("belum", actionEvent));
            }

        });

        imgThumbnail.setOnMouseClicked(mouseEvent -> getImage());
        btnBatal.setOnAction(actionEvent -> Batal());
    }

    private void getImage() {
        FileChooser fileChooser = new FileChooser();

        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("(*.JPG *.PNG *.jpg *.png)", "*.JPG", "jpg files (*.jpg)", "*.jpg", "PNG files (*.PNG)", "*.PNG", "png files (*.png)", "*.png");
        fileChooser.getExtensionFilters().add(extFilter);

        File file = fileChooser.showOpenDialog(null);

        if (file != null) {
            Image image = new Image(file.toURI().toString());
            imgThumbnail.setImage(image);
            currentFile = file;
        }
    }

    public void setResepForm(int idr) throws SQLException  {
        ResultSet resultSet = connection.createStatement().executeQuery("SELECT r.* , u.username FROM resep r INNER JOIN akun u ON r.id_user = u.uid WHERE idr = " + idr);
        if (resultSet.next()){
            txtNamaResep.setText(resultSet.getString("nama"));
            cbKategori.setValue(resultSet.getString("kategori"));
            txtDeskripsi.setText(resultSet.getString("deskripsi"));
            txtAlat.setText(resultSet.getString("alat"));
            txtBahan.setText(resultSet.getString("bahan"));
            txtInstruksi.setText(resultSet.getString("instruksi"));
            txtAuthor.setText(resultSet.getString("username"));

            File file = new File(resultSet.getString("gambar"));
            if (!resultSet.getString("gambar").isEmpty()){
                Image image = new Image(file.toURI().toString());
                currentFile = file;
                imgThumbnail.setImage(image);
            }
            resultSet.close();
        }

        try {
            resultSet = connection.createStatement().executeQuery("SELECT AVG(rating) FROM komentar WHERE idr=" + idr);
            if (resultSet.next()){
                SVGPath[] starColor = {bintang1, bintang2, bintang3, bintang4, bintang5};
                double star = resultSet.getDouble("AVG(rating)");
                for (int i = 0; i < starColor.length; i++) {
                    if (i < round(star)) {
                        starColor[i].setFill(Color.web("#FCC419"));
                    } else {
                        starColor[i].setFill(Color.BLACK);
                    }
                }
                resultSet.close();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void DisableInput() {
        txtNamaResep.setDisable(true);
        txtDeskripsi.setDisable(true);
        txtAuthor.setDisable(true);
        txtBahan.setDisable(true);
        txtInstruksi.setDisable(true);
        txtAlat.setDisable(true);
        cbKategori.setDisable(true);
        txtAuthor.setDisable(true);

        txtNamaResep.setStyle("-fx-opacity: 1.0;");
        txtDeskripsi.setStyle("-fx-opacity: 1.0;");
        txtAuthor.setStyle("-fx-opacity: 1.0;");
        txtBahan.setStyle("-fx-opacity: 1.0;");
        txtInstruksi.setStyle("-fx-opacity: 1.0;");
        txtAlat.setStyle("-fx-opacity: 1.0;");
        cbKategori.setStyle("-fx-opacity: 1;-fx-background-color: white;-fx-border-color: grey;-fx-border-width: 0.3px;");

        btnSelesai.setDisable(true);btnSelesai.setVisible(false);
        btnLater.setDisable(true);btnLater.setVisible(false);
        btnBatal.setDisable(true);btnBatal.setVisible(false);
        imgThumbnail.setDisable(true);
    }

    private void Selesai(String status, ActionEvent actionEvent) {
        try{
            PreparedStatement statement = connection.prepareStatement("INSERT INTO resep VALUES (NULL, ?,?,?,?,?,?,?,?,?,0)");
            statement.setString(1, txtNamaResep.getText());
            statement.setString(2, cbKategori.getValue());
            statement.setString(3, txtDeskripsi.getText());
            statement.setString(4, txtAlat.getText());
            statement.setString(5, txtBahan.getText());
            statement.setString(6, txtInstruksi.getText());
            if (status.equals("selesai")) {
                if (!txtNamaResep.getText().trim().isEmpty() && !txtAlat.getText().trim().isEmpty() && !txtDeskripsi.getText().trim().isEmpty()
                    && !txtBahan.getText().trim().isEmpty() && !txtInstruksi.getText().trim().isEmpty() && currentFile != null){

                    statement.setString(7, "selesai");
                    String fileName = txtNamaResep.getText() + "_" + UUID.randomUUID() + "_" + SesiUser.uid + "." + FilenameUtils.getExtension(currentFile.getName());
                    Path destinationDir= Path.of("src/main/resources/img/resep");
                    Path destinationFile = destinationDir.toAbsolutePath().resolve(fileName);
                    statement.setString(8, destinationDir.resolve(fileName).toString());
                    try {
                        Files.copy(currentFile.toPath(), destinationFile, StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }

                    statement.setInt(9, SesiUser.uid);
                    statement.executeUpdate();

                    NotifCustom.AlertNotifINFORMATION("Informasi", "Tambah Resep", "Berhasil Menambah Resep");
                    NotifCustom.CloseMeStage(actionEvent);

                    DashboardUserController dashboardUserController = new DashboardUserController();
                    dashboardUserController.getInstance().getRefreshData();
                }else {
                    NotifCustom.AlertNotif("Warning", "Data", "Data Belum Lengkap");
                }
            } else {
                if (txtAlat.getText().isEmpty()) {
                    txtAlat.setText("");
                }
                if (txtAuthor.getText().isEmpty()) {
                    txtAuthor.setText("");
                }
                if (txtBahan.getText().isEmpty()) {
                    txtBahan.setText("");
                }
                if (txtDeskripsi.getText().isEmpty()) {
                    txtDeskripsi.setText("");
                }
                if (txtInstruksi.getText().isEmpty()) {
                    txtInstruksi.setText("");
                }
                if (txtNamaResep.getText().isEmpty()) {
                    txtNamaResep.setText("");
                }
                    statement.setString(7, "belum");
                    if (currentFile != null){
                        String fileName = txtNamaResep.getText() + "_" + UUID.randomUUID() + "_" + SesiUser.uid + "." + FilenameUtils.getExtension(currentFile.getName());
                        Path destinationDir= Path.of("src/main/resources/img/resep");
                        Path destinationFile = destinationDir.toAbsolutePath().resolve(fileName);
                        statement.setString(8, destinationDir.resolve(fileName).toString());
                        try {
                            Files.copy(currentFile.toPath(), destinationFile, StandardCopyOption.REPLACE_EXISTING);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }else {
                        statement.setString(8, "".trim());
                    }
                    statement.setInt(9, SesiUser.uid);
                    statement.executeUpdate();

                    NotifCustom.CloseMeStage(actionEvent);

                    DashboardUserController dashboardUserController = new DashboardUserController();
                    dashboardUserController.getInstance().getRefreshData();
                }
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    private void Edit(String status, ActionEvent actionEvent, int idr) {
        try{
            PreparedStatement statement = connection.prepareStatement("UPDATE resep SET nama=?, kategori=?, deskripsi=?, alat=?, bahan=?, instruksi=?, status=?, gambar=? WHERE idr=" + idr);
            statement.setString(1, txtNamaResep.getText());
            statement.setString(2, cbKategori.getValue());
            statement.setString(3, txtDeskripsi.getText());
            statement.setString(4, txtAlat.getText());
            statement.setString(5, txtBahan.getText());
            statement.setString(6, txtInstruksi.getText());
            if (status.equals("selesai")) {
                if (!txtNamaResep.getText().trim().isEmpty() && !txtAlat.getText().trim().isEmpty() && !txtDeskripsi.getText().trim().isEmpty()
                        && !txtBahan.getText().trim().isEmpty() && !txtInstruksi.getText().trim().isEmpty() && currentFile != null) {
                    statement.setString(7, "selesai");
                    Path destinationDir = Path.of("src/main/resources/img/resep");

                    try {
                        ResultSet resultSet = connection.createStatement().executeQuery("SELECT gambar FROM resep WHERE idr=" + idr);
                        if (resultSet.next()) {
                            if(!resultSet.getString("gambar").isEmpty()) {
                                Path path = Path.of(resultSet.getString("gambar")).toAbsolutePath();
                                if (currentFile != null) {
                                    String fileName = txtNamaResep.getText() + "_" + UUID.randomUUID() + "_" + SesiUser.uid + "." + FilenameUtils.getExtension(currentFile.getName());
                                    Path destinationFile = destinationDir.toAbsolutePath().resolve(fileName);
                                    Files.copy(currentFile.toPath(), destinationFile, StandardCopyOption.REPLACE_EXISTING);
                                    Files.deleteIfExists(path);
                                    statement.setString(8, destinationDir.resolve(fileName).toString());
                                }else {
                                    statement.setString(8, "");
                                }
                            }else {
                                if (currentFile != null) {
                                    String fileName = txtNamaResep.getText() + "_" + UUID.randomUUID() + "_" + SesiUser.uid + "." + FilenameUtils.getExtension(currentFile.getName());
                                    Path destinationFile = destinationDir.toAbsolutePath().resolve(fileName);
                                    Files.copy(currentFile.toPath(), destinationFile, StandardCopyOption.REPLACE_EXISTING);
                                    statement.setString(8, destinationDir.resolve(fileName).toString());
                                }else {
                                    statement.setString(8, "");
                                }
                            }
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }

                    statement.executeUpdate();

                    NotifCustom.AlertNotifINFORMATION("Informasi", "Edit Resep", "Berhasil Mengubah Resep");
                    NotifCustom.CloseMeStage(actionEvent);

                    DashboardUserController dashboardUserController = new DashboardUserController();
                    dashboardUserController.getInstance().getRefreshData();
                }else {
                    NotifCustom.AlertNotif("Warning", "Data", "Data Belum Lengkap");
                }
            } else {
                if (txtAlat.getText().isEmpty()) {
                    txtAlat.setText("");
                }
                if (txtAuthor.getText().isEmpty()) {
                    txtAuthor.setText("");
                }
                if (txtBahan.getText().isEmpty()) {
                    txtBahan.setText("");
                }
                if (txtDeskripsi.getText().isEmpty()) {
                    txtDeskripsi.setText("");
                }
                if (txtInstruksi.getText().isEmpty()) {
                    txtInstruksi.setText("");
                }
                if (txtNamaResep.getText().isEmpty()) {
                    txtNamaResep.setText("");
                }
                statement.setString(7, "belum");
                Path destinationDir = Path.of("src/main/resources/img/resep").toAbsolutePath();

                try {
                    ResultSet resultSet = connection.createStatement().executeQuery("SELECT gambar FROM resep WHERE idr=" + idr);
                    if (resultSet.next()) {
                        if(!resultSet.getString("gambar").isEmpty()) {
                            Path path = Path.of(resultSet.getString("gambar")).toAbsolutePath();
                            if (currentFile != null) {
                                String fileName = txtNamaResep.getText() + "_" + UUID.randomUUID() + "_" + SesiUser.uid + "." + FilenameUtils.getExtension(currentFile.getName());
                                Path destinationFile = destinationDir.toAbsolutePath().resolve(fileName);
                                Files.copy(currentFile.toPath(), destinationFile, StandardCopyOption.REPLACE_EXISTING);
                                Files.deleteIfExists(path);
                                statement.setString(8, destinationDir.resolve(fileName).toString());
                            }else {
                                statement.setString(8, "");
                            }
                        }else {
                            if (currentFile != null) {
                                String fileName = txtNamaResep.getText() + "_" + UUID.randomUUID() + "_" + SesiUser.uid + "." + FilenameUtils.getExtension(currentFile.getName());
                                Path destinationFile = destinationDir.toAbsolutePath().resolve(fileName);
                                Files.copy(currentFile.toPath(), destinationFile, StandardCopyOption.REPLACE_EXISTING);
                                statement.setString(8, destinationDir.resolve(fileName).toString());
                            }else {
                                statement.setString(8, "");
                            }
                        }
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                    statement.executeUpdate();

                    NotifCustom.CloseMeStage(actionEvent);
                    DashboardUserController dashboardUserController = new DashboardUserController();
                    dashboardUserController.getInstance().getRefreshData();
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    private void Batal() {
        Stage stage = (Stage) btnBatal.getScene().getWindow();
        stage.close();
    }
}
