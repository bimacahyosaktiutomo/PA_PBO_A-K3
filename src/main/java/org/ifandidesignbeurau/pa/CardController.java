package org.ifandidesignbeurau.pa;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.Popup;
import javafx.stage.Stage;
import org.ifandidesignbeurau.pa.Important.SesiUser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;

import static java.lang.Math.round;

public class CardController {
    @FXML private VBox VBox;

    @FXML private HBox Bookmark;

    @FXML private SVGPath SVGBookmark;

    @FXML private SVGPath bintang1;

    @FXML private SVGPath bintang2;

    @FXML private SVGPath bintang3;

    @FXML private SVGPath bintang4;

    @FXML private SVGPath bintang5;

    @FXML private Button btnActionEdit;

    @FXML private Button btnActionHapus;

    @FXML private ImageView imgPP;

    @FXML private Hyperlink namaResep;

    @FXML private ImageView resepImage;

    @FXML private Label txtUsername;

    public Button getBtnActionEdit() {
        return btnActionEdit;
    }

    public Button getBtnActionHapus() {
        return btnActionHapus;
    }

    Connection connection = DB.connectDB();

    private String Mode;
    public void setMode(String mode) {
        Mode = mode;
    }
    private static CardController instance;

    public CardController getInstance() {
        return instance;
    }

    public void setData(String nama, int idr, String gambar){
        instance = this;
        try {

            if (!gambar.trim().isEmpty()){
                File file = new File(gambar);
                Image image = new Image(file.toURI().toString());
                resepImage.setImage(image);
            }

            namaResep.setText(nama);

            ResultSet resultSet = connection.createStatement().executeQuery("SELECT AVG(k.rating) AS star, r.rating, u.username, u.gambar FROM resep r RIGHT JOIN akun u ON r.id_user = u.uid LEFT JOIN komentar k ON r.idr = k.idr WHERE r.idr= " + idr +" GROUP BY r.idr;");
            if (resultSet.next()){
                txtUsername.setText(resultSet.getString("username"));

                File file = new File(resultSet.getString("gambar"));
                if (!resultSet.getString("gambar").isEmpty()){
                    Image image = new Image(file.toURI().toString());
                    imgPP.setImage(image);
                }

                SVGPath[] starColor = {bintang1, bintang2, bintang3, bintang4, bintang5};
                double star = resultSet.getDouble("star");
                for (int i = 0; i < starColor.length; i++) {
                    if (i < round(star)) {
                        starColor[i].setFill(Color.web("#FCC419"));
                    } else {
                        starColor[i].setFill(Color.BLACK);
                    }
                }
            }

            ResultSet BookmarkSet = connection.createStatement().executeQuery("SELECT * FROM bookmark WHERE idr=" + idr +" AND uid=" + SesiUser.uid);
            if (BookmarkSet.next()){
                SVGBookmark.setContent("M19 24l-7-6-7 6v-24h14v24z");
                SVGBookmark.setFill(Color.web("#fcc419"));
                SVGBookmark.setStroke(Color.web("#fcc419"));
            }

            if (SesiUser.LoginSession == 1){
                Bookmark.setOnMouseClicked(mouseEvent -> Bookmark(idr));
            }else {
                Bookmark.setOnMouseClicked(mouseEvent -> {
                    HelloController helloController = new HelloController();
                    try {
                        helloController.getInstance().getHelloLogin();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        btnActionEdit.setOnAction(actionEvent -> openEdit(idr));
        btnActionHapus.setOnAction(actionEvent -> Hapus(idr, nama));
        namaResep.setOnAction(actionEvent -> openDisplay(idr));


    }

    private void openDisplay(int idr){
        try {
            System.out.println(idr);
            FXMLLoader Resep = new FXMLLoader(getClass().getResource("ResepForm.fxml"));
            FXMLLoader Komentar = new FXMLLoader(getClass().getResource("Komentar.fxml"));

            HelloController helloController = new HelloController();
            helloController.getInstance().MainContainer.getChildren().clear();

            helloController.getInstance().MainContainer.getChildren().addFirst(Komentar.load());
            helloController.getInstance().MainContainer.getChildren().addFirst(Resep.load());

            ResepFormController resepFormController = Resep.getController();
            resepFormController.setResepForm(idr);
            resepFormController.DisableInput();

            KomenController komenController = Komentar.getController();
            komenController.setIdResep(idr);
            if (SesiUser.LoginSession == 0){
                komenController.DisableInput();
            }
        } catch (IOException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void Bookmark(int idr) {
        try {
            ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM bookmark WHERE idr=" + idr + " AND uid=" + SesiUser.uid);
            if (!resultSet.next()){
                SVGBookmark.setContent("M19 24l-7-6-7 6v-24h14v24z");
                SVGBookmark.setFill(Color.web("#fcc419"));
                SVGBookmark.setStroke(Color.web("#fcc419"));

                PreparedStatement statement = connection.prepareStatement("INSERT INTO bookmark VALUES(?, ?)");
                statement.setInt(1, idr);
                statement.setInt(2, SesiUser.uid);
                statement.executeUpdate();
            }else {
                SVGBookmark.setContent("M5 0v24l7-6 7 6v-24h-14zm1 1h12v20.827l-6-5.144-6 5.144v-20.827z");
                SVGBookmark.setFill(Color.web("#fcc419"));
                SVGBookmark.setStroke(Color.web("#fcc419"));

                PreparedStatement statement = connection.prepareStatement("DELETE FROM bookmark WHERE idr=? AND uid=?");
                statement.setInt(1, idr);
                statement.setInt(2, SesiUser.uid);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void openEdit(int idr) {
        Popup popup = new Popup();
        Stage stage = new Stage();
        popup.setAutoHide(true);
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("ResepForm.fxml"));
        try {
            if (!popup.isShowing()){
                Scene scene = new Scene(fxmlLoader.load());
                ResepFormController resepFormController = fxmlLoader.getController();
                resepFormController.setSetIDResep(idr);
                resepFormController.setResepForm(idr);
                stage.setScene(scene);
                stage.setMinWidth(605);
                stage.setMinHeight(643);
                stage.show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void Hapus(int idr, String namaResep){
        if (NotifCustom.AlertNotifCONFIRM("Notifikasi", "Hapus data", "Apakah anda yakin ingin mengapus user berikut, idr :  " + namaResep)){
            try {
                ResultSet resultSet = connection.createStatement().executeQuery("SELECT gambar FROM resep WHERE idr=" + idr);
                if (resultSet.next() && !resultSet.getString("gambar").isEmpty()){
                    Path path = Path.of(resultSet.getString("gambar")).toAbsolutePath();
                    Files.deleteIfExists(path);
                }
                    PreparedStatement statement = connection.prepareStatement("DELETE FROM resep WHERE idr=" + idr);
                    statement.executeUpdate();

                if (Mode.equals("user")){
                    DashboardUserController dashboardUserController = new DashboardUserController();
                    dashboardUserController.getInstance().getRefreshData();
                }else {
                    DashboardAdminController dashboardAdminController = new DashboardAdminController();
                    dashboardAdminController.getInstance().getRefreshResep();
                }

                NotifCustom.AlertNotif("Delete", "Hapus Resep", "Resep Berhasil Dihapus");
            } catch (SQLException | IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
