package org.ifandidesignbeurau.pa;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import org.ifandidesignbeurau.pa.Important.SesiUser;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class KomenController implements Initializable {

    @FXML private VBox KomenContainer;

    @FXML private SVGPath bintang1;

    @FXML private SVGPath bintang2;

    @FXML private SVGPath bintang3;

    @FXML private SVGPath bintang4;

    @FXML private SVGPath bintang5;

    @FXML private Button btnSubmit;

    @FXML private TextArea txtKomentar;

    Connection connection = DB.connectDB();

    private int NilaiBintang;

    private int idResep;
    public void setIdResep(int idResep) {
        this.idResep = idResep;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform.runLater(() -> RefreshKomen(idResep));
        Alat.addTextAreaLimiter(txtKomentar, 100);

        btnSubmit.setOnAction(actionEvent -> Submit());

        SVGPath[] starColor = {bintang1, bintang2, bintang3, bintang4, bintang5};
        for (int i = 0; i < starColor.length; i++) {
            int value = i;
            starColor[i].setOnMouseClicked(mouseEvent -> setNilaiBintang(value + 1));
        }
    }

    private void Submit(){
        if (SesiUser.LoginSession != 0){
            try {
                ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM komentar WHERE uid=" + SesiUser.uid + " AND idr=" + idResep);
                if (!resultSet.next()){
                    if (!txtKomentar.getText().trim().isEmpty() && NilaiBintang > 0) {
                        PreparedStatement statement = connection.prepareStatement("INSERT INTO komentar VALUES (NULL, ?,?,?,?)");
                        statement.setInt(1, NilaiBintang);
                        statement.setString(2, txtKomentar.getText());
                        statement.setInt(3, idResep);
                        statement.setInt(4, SesiUser.uid);
                        statement.executeUpdate();

                        RefreshKomen(idResep);
                        txtKomentar.clear();
                        setNilaiBintang(0);
                    }else {
                        NotifCustom.AlertNotif("Notifikasi", "Input Belum Lengkap", "Komentar atau Rating masih kosong");
                    }
                }else {
                    if (!txtKomentar.getText().trim().isEmpty() && NilaiBintang > 0) {
                        PreparedStatement statement = connection.prepareStatement("UPDATE komentar SET rating=?, komen=? WHERE uid=" + SesiUser.uid);
                        statement.setInt(1, NilaiBintang);
                        statement.setString(2, txtKomentar.getText());
                        statement.executeUpdate();

                        PreparedStatement AddRating = connection.prepareStatement("UPDATE resep SET rating=rating + ? WHERE idr=" + idResep);
                        AddRating.setInt(1, NilaiBintang);
                        AddRating.executeUpdate();

                        RefreshKomen(idResep);
                        txtKomentar.clear();
                        setNilaiBintang(0);
                    }else {
                        NotifCustom.AlertNotif("Notifikasi", "Input Belum Lengkap", "Komentar atau Rating masih kosong");
                    }
                }
            }catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }else {
            NotifCustom.AlertNotif("Notifikasi", "Login Warning", "Login sebelum memberikan komentar");
        }
    }

    private void setNilaiBintang(int star){
        NilaiBintang = 0;
        SVGPath[] starColor = {bintang1, bintang2, bintang3, bintang4, bintang5};
        for (int i = 0; i < starColor.length; i++) {
            if (i < star) {
                starColor[i].setFill(Color.web("#FCC419"));
                NilaiBintang++;
            } else {
                starColor[i].setFill(Color.BLACK);
            }
        }
    }

    private void RefreshKomen(int idr) {
        KomenContainer.getChildren().clear();
        try {
            ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM komentar WHERE idr=" + idr);
            while (resultSet.next()) {
                String komen = resultSet.getString("komen");
                int star = resultSet.getInt("rating");
                int uid = resultSet.getInt("uid");

                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("ratingKomen.fxml"));
                KomenContainer.getChildren().add(fxmlLoader.load());

                RatingKomenController ratingKomenController = fxmlLoader.getController();
                ratingKomenController.setRatingKomen(idr, komen, star, uid);
            }
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void DisableInput() {
        txtKomentar.setDisable(true);
        bintang1.setDisable(true);
        bintang2.setDisable(true);
        bintang3.setDisable(true);
        bintang4.setDisable(true);
        bintang5.setDisable(true);
    }
}
