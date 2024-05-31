package org.ifandidesignbeurau.pa;

import javafx.fxml.*;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Popup;
import javafx.stage.Stage;
import org.ifandidesignbeurau.pa.Important.SesiUser;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class DashboardUserController implements Initializable {
    @FXML private GridPane GPresep;

    @FXML private Button btnTambah;

    @FXML private Button btnHapus;

    @FXML private TextField txtSearchData;

    private static DashboardUserController instance;
    public DashboardUserController getInstance() {
        return instance;
    }

    Connection connection = DB.connectDB();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        instance = this;
        RefreshData();

        btnTambah.setOnAction(actionEvent -> openFormResep());
        txtSearchData.textProperty().addListener((observable, oldValue, newValue) -> {SearchData();});
    }

    private void RefreshData() {
        GPresep.getChildren().clear();
        int column = 0;
        int row = 1;
        try {
            ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM resep WHERE id_user=" + SesiUser.uid);
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
                    cardController.setMode("user");

                    if (column == 3){
                        column = 0;
                        ++row;
                    }

                    GPresep.add(cardBox, column++, row);
                    GridPane.setMargin(cardBox, new Insets(10, 10, 10, 10));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void getRefreshData() {
        RefreshData();
    }

    private void SearchData () {
        GPresep.getChildren().clear();
        int column = 0;
        int row = 1;
        String cari = txtSearchData.getText();
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM resep WHERE nama LIKE = ? AND id_user=" + SesiUser.uid);
            statement.setString(1, "%" + cari + "%");
            ResultSet resultSet = statement.executeQuery();
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

                    if (column == 3){
                        column = 0;
                        ++row;
                    }

                    GPresep.add(cardBox, column++, row);
                    GridPane.setMargin(cardBox, new Insets(10, 10, 10, 10));

                }catch (IOException e){
                    throw new RuntimeException(e);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void openFormResep() {
        Popup popup = new Popup();
        Stage stage = new Stage();
        popup.setAutoHide(true);
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("ResepForm.fxml"));
        try {
            if (!popup.isShowing()){
                Scene scene = new Scene(fxmlLoader.load());
                ResepFormController resepFormController = new ResepFormController();
                resepFormController.setSetIDResep(0);
                stage.setScene(scene);
                stage.setMinWidth(605);
                stage.setMinHeight(643);
                stage.show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

