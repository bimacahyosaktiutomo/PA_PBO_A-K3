package org.ifandidesignbeurau.pa;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class UserCardController implements Initializable {
    Connection connection = DB.connectDB();

    @FXML private Label txtUidDB;

    @FXML private Label txtUsernameDB;

    @FXML private Label txtPasswordDB;

    @FXML private Label txtEmailDB;

    @FXML private Label txtIzinDB;

    @FXML private Button btnEditDB;

    @FXML private CheckBox chkAkunCard;

    @FXML private ImageView imgProfile;

    private static UserCardController instance;

    public UserCardController getInstance() {
        return instance;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        instance = this;
    }

    public void setDataUserCard(Integer uid, String username, String password, String email, String izin, String gambar) {
        txtUidDB.setText(uid.toString());
        txtUsernameDB.setText(username);
        txtPasswordDB.setText(password);
        txtEmailDB.setText(email);
        txtIzinDB.setText(izin);
        if (izin.equals("admin")){
            chkAkunCard.setVisible(false);
            chkAkunCard.setDisable(true);
        }

        File file = new File(gambar);
        if (!gambar.isEmpty()){
            Image image = new Image(file.toURI().toString());
            imgProfile.setImage(image);
        }

        btnEditDB.setOnAction(actionEvent -> openEditPopup(uid, izin));
        chkAkunCard.setOnAction(actionEvent -> ChkBoxCeker(uid));
    }

    public void openEditPopup(int uid, String izin){
        Popup popup = new Popup();
        Stage stage = new Stage();
        popup.setAutoHide(true);
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("userForm.fxml"));
        try {
            if (!popup.isShowing() && !izin.equals("admin")){
                Scene scene = new Scene(fxmlLoader.load());
                UserFormController userFormController = fxmlLoader.getController();
                userFormController.setUserForm(uid, stage);
                userFormController.setSetUID(uid);
                stage.setScene(scene);
                stage.show();
            }
        } catch (IOException e ) {
            e.printStackTrace();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void ChkBoxCeker(int uid){
        DashboardAdminController dashboardAdminController = new DashboardAdminController();
        ArrayList<Integer> list = dashboardAdminController.getInstance().getList();
        if (chkAkunCard.isSelected()){
            if (!list.contains(uid) && uid != 1){
                list.add(uid);
            }
        }else if (!chkAkunCard.isSelected()){
            list.remove(Integer.valueOf(uid));
        }
    }
}
