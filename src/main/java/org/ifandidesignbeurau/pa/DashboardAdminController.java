package org.ifandidesignbeurau.pa;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class DashboardAdminController implements Initializable{
    @FXML private Tab tabAkun;

    @FXML private VBox VboxUserContainerAkun;

    @FXML private GridPane resepContainer;

    @FXML private Tab tabResep;

    @FXML private TextField txtSearchData;

    @FXML private Button btnCari;

    @FXML private Button btnHapus;

    private static DashboardAdminController instance;
    public DashboardAdminController getInstance() {
        return instance;
    }

    Connection connection = DB.connectDB();

    ArrayList<Integer> list = new ArrayList<>();
    public ArrayList<Integer> getList() {
        return list;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        instance = this;
        RefreshData();
        RefreshDataResep();

//        btnCari.setOnAction(actionEvent -> SearchData());
        txtSearchData.textProperty().addListener((observable, oldValue, newValue) -> {SearchData();});

        btnHapus.setOnAction(actionEvent -> Hapus());
    }

    private void RefreshData() {
        VboxUserContainerAkun.getChildren().clear();
        try {
            ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM akun");

            while (resultSet.next()){
                try {
                    int uid  = resultSet.getInt("uid");
                    String username = resultSet.getString("username");
                    String password = resultSet.getString("password");
                    String email = resultSet.getString("email");
                    String izin = resultSet.getString("izin");
                    String gambar = resultSet.getString("gambar");

                    FXMLLoader fxmlLoader = new FXMLLoader();
                    fxmlLoader.setLocation(getClass().getResource("userCard.fxml"));
                    VboxUserContainerAkun.getChildren().add(fxmlLoader.load());
                    VboxUserContainerAkun.getChildren().add(new Separator());

                    UserCardController userCardController = fxmlLoader.getController();
                    userCardController.setDataUserCard(uid, username, password, email, izin, gambar);

                }catch (IOException e){
                    throw new RuntimeException(e);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void RefreshDataResep(){
        resepContainer.getChildren().clear();
        int column = 0;
        int row = 1;
        try {
            ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM resep WHERE status = 'selesai'");
            while (resultSet.next()) {
                try {
                    int idr = resultSet.getInt("idr");
                    String namaResep = resultSet.getString("nama");
                    String gambar = resultSet.getString("gambar");

                    FXMLLoader fxmlLoader = new FXMLLoader();
                    fxmlLoader.setLocation(getClass().getResource("card.fxml"));
                    VBox cardBox =  fxmlLoader.load();
                    CardController cardController = fxmlLoader.getController();
                    cardController.setData(namaResep, idr, gambar);
                    cardController.setMode("admin");
                    cardController.getBtnActionEdit().setVisible(false);
                    cardController.getBtnActionEdit().setDisable(true);

                    if (column == 3){
                        column = 0;
                        ++row;
                    }

                    resepContainer.add(cardBox, column++, row);
                    GridPane.setMargin(cardBox, new Insets(10, 10, 10, 10));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void getRefreshAdmin() {
        RefreshData();
    }

    public void getRefreshResep() {
        RefreshDataResep();
    }

    private void SearchData() {
        VboxUserContainerAkun.getChildren().clear();
        resepContainer.getChildren().clear();
        String cari = txtSearchData.getText();

        searchUsers(cari);
        searchRecipes(cari);
    }

    private void searchUsers(String query) {
        String sql = "SELECT * FROM akun WHERE username LIKE ? OR uid LIKE ? OR email LIKE ? OR izin LIKE ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 1; i <= 4; i++) {
                statement.setString(i, "%" + query + "%");
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    int uid  = resultSet.getInt("uid");
                    String username = resultSet.getString("username");
                    String password = resultSet.getString("password");
                    String email = resultSet.getString("email");
                    String izin = resultSet.getString("izin");
                    String gambar = resultSet.getString("gambar");

                    FXMLLoader fxmlLoader = new FXMLLoader();
                    fxmlLoader.setLocation(getClass().getResource("userCard.fxml"));
                    VboxUserContainerAkun.getChildren().add(fxmlLoader.load());

                    UserCardController userCardController = fxmlLoader.getController();
                    userCardController.setDataUserCard(uid, username, password, email, izin, gambar);
                }
            } catch (IOException e) {
                throw new RuntimeException("Error loading user card FXML", e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error during user search", e);
        }
    }

    private void searchRecipes(String query) {
        String sql = "SELECT * FROM resep WHERE nama LIKE ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, "%" + query + "%");

            try (ResultSet resultSet = statement.executeQuery()) {
                int column = 0;
                int row = 1;

                while (resultSet.next()) {
                    int idr = resultSet.getInt("idr");
                    String namaResep = resultSet.getString("nama");
                    String gambar = resultSet.getString("gambar");

                    FXMLLoader fxmlLoader = new FXMLLoader();
                    fxmlLoader.setLocation(getClass().getResource("card.fxml"));
                    VBox cardBox =  fxmlLoader.load();
                    CardController cardController = fxmlLoader.getController();
                    cardController.setData(namaResep, idr, gambar);

                    if (column == 3){
                        column = 0;
                        ++row;
                    }

                    resepContainer.add(cardBox, column++, row);
                    GridPane.setMargin(cardBox, new Insets(10, 10, 10, 10));
                }
            } catch (IOException e) {
                throw new RuntimeException("Error loading recipe card FXML", e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error during recipe search", e);
        }
    }


    private void Hapus() {
        // REMINDER BUAT GW : SET AUTO INCREMENT ID KE VALUE TERBESAR SETELAH DIHAPUS
        if (NotifCustom.AlertNotifCONFIRM("Notifikasi", "Hapus data", "Apakah anda yakin ingin mengapus user berikut, uid :  " + list)){
            for (int uid : list){
                try {
                    ResultSet resultSet = connection.createStatement().executeQuery("SELECT gambar FROM akun WHERE uid=" + uid);
                    if (resultSet.next() && !resultSet.getString("gambar").isEmpty()){
                        Path path = Path.of(resultSet.getString("gambar")).toAbsolutePath();
                        Files.deleteIfExists(path);
                    }
                    resultSet.close();


                    ResultSet WipeImage = connection.createStatement().executeQuery("SELECT gambar FROM resep WHERE id_user=" + uid);
                    while (WipeImage.next() && !WipeImage.getString("gambar").isEmpty()){
                        Path path = Path.of(WipeImage.getString("gambar")).toAbsolutePath();
                        Files.deleteIfExists(path);
                    }
                    WipeImage.close();

                    PreparedStatement statement = connection.prepareStatement("DELETE FROM akun WHERE uid=?");
                    statement.setInt(1, uid);
                    statement.executeUpdate();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            RefreshData();
        }
    }
}
