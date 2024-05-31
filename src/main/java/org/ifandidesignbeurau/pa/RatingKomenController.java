package org.ifandidesignbeurau.pa;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;

import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class RatingKomenController implements Initializable {
    @FXML private SVGPath bintang1;

    @FXML private SVGPath bintang2;

    @FXML private SVGPath bintang3;

    @FXML private SVGPath bintang4;

    @FXML private SVGPath bintang5;

    @FXML private ImageView imgPP;

    @FXML private Text txtKomentar;

    @FXML private Label txtNama;

    Connection connection = DB.connectDB();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    public void setRatingKomen(int idr, String komen, int star, int uid) {
        txtKomentar.setText(komen);

        SVGPath[] starColor = {bintang1, bintang2, bintang3, bintang4, bintang5};
        for (int i = 0; i < starColor.length; i++) {
            if (i < star) {
                starColor[i].setFill(Color.web("#FCC419"));
            } else {
                starColor[i].setFill(Color.BLACK);
            }
        }

        try {
            ResultSet userSet = connection.createStatement().executeQuery("SELECT username, gambar FROM akun WHERE uid=" + uid);
            if (userSet.next()){
                txtNama.setText(userSet.getString("username"));

                File file = new File(userSet.getString("gambar"));
                if (!userSet.getString("gambar").isEmpty()){
                    Image image = new Image(file.toURI().toString());
                    imgPP.setImage(image);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
