package org.ifandidesignbeurau.pa;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.stage.*;
import org.ifandidesignbeurau.pa.Important.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class HelloController implements Initializable {
    @FXML private Button BtnShowAppetizer;

    @FXML private Button BtnShowDessert;

    @FXML private Button BtnShowMain;


    @FXML private ImageView imgProfileUser;
    public ImageView getImgProfileUser() {
        return imgProfileUser;
    }

    @FXML private Label txtNamaUSer;

    @FXML private HBox HboxBtnContainer;

    @FXML private HBox HboxResepBtn;

    @FXML private HBox HboxBookmarkBtn;

    @FXML private HBox HboxRankBtn;

    @FXML private HBox HboxDashboardBtn;

    @FXML private HBox HboxHomeBtn;

    @FXML private HBox HboxProfileBtn;

    @FXML private GridPane resepContainer;

    @FXML private VBox VboxLobbyContainer;

    @FXML private HBox HboxDisplayPageName;

    @FXML private ScrollPane LobbyContainer;

    @FXML public VBox MainContainer;

    @FXML private Button btnOpenLogin;

    @FXML private Label txtHomeBtn;

    @FXML private Label txtDisplayHome;

    @FXML private TextField txtSearch;

    @FXML private BorderPane MainBorderpane;

    static Stage PopLoginStage;

    Connection connection = DB.connectDB();

    private static HelloController instance;
    public HelloController getInstance(){
        return instance;
    }

    @Override
    public void initialize(URL location, ResourceBundle resource){
        instance = this;
        if (SesiUser.LoginSession == 1){
            txtNamaUSer.setText(SesiUser.username);

            try {
               ResultSet resultSet = connection.createStatement().executeQuery("SELECT gambar FROM akun WHERE uid=" + SesiUser.uid);
                if (resultSet.next()){
                    File file = new File(resultSet.getString("gambar"));
                    if (!resultSet.getString("gambar").isEmpty()){
                        Image image = new Image(file.toURI().toString());
                        imgProfileUser.setImage(image);
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            btnOpenLogin.setText("Logout");
            if (!SesiUser.izin.equals("admin")){
                HboxDashboardBtn.setVisible(false);HboxDashboardBtn.setDisable(true);
                HboxProfileBtn.setOnMouseClicked(mouseEvent -> openProfile());
            }

        }else {
            HboxResepBtn.setVisible(false);HboxDashboardBtn.setDisable(true);
            HboxDashboardBtn.setVisible(false);HboxDashboardBtn.setDisable(true);
            btnOpenLogin.setText("Login");
            txtNamaUSer.setText("Login");

            Popup OpenLoginPopup = new Popup();
            Stage popLoginStage = new Stage();
            OpenLoginPopup.setAutoHide(true);
            HboxProfileBtn.setOnMouseClicked(event -> {
                try {
                    openLoginPopup(OpenLoginPopup, popLoginStage);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        RefreshData();

        Popup OpenLoginPopup = new Popup();
        Stage popLoginStage = new Stage();
        OpenLoginPopup.setAutoHide(true);
        btnOpenLogin.setOnAction(event -> {
            try {
                openLoginPopup(OpenLoginPopup, popLoginStage);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        BtnShowAppetizer.setOnAction(actionEvent -> showBTN("Appetizer"));
        BtnShowMain.setOnAction(actionEvent -> showBTN("Main Course"));
        BtnShowDessert.setOnAction(actionEvent -> showBTN("Dessert"));

        HboxHomeBtn.setOnMouseClicked(mouseEvent -> openHome());
        HboxResepBtn.setOnMouseClicked(mouseEvent -> openUserDashboard());
        HboxDashboardBtn.setOnMouseClicked(mouseEvent -> openAdminDashboard());
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> Cari());
        txtHomeBtn.setOnMouseClicked(mouseEvent -> openHome());
        HboxBookmarkBtn.setOnMouseClicked(mouseEvent -> {
            if (SesiUser.LoginSession == 0){
                try {
                    openLoginPopup(OpenLoginPopup, popLoginStage);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }else {
                openTopBookmark("bookmark");
            }
        });
        HboxRankBtn.setOnMouseClicked(mouseEvent -> openTopBookmark("top"));
    }

    private void RefreshData() {
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
                    cardController.getBtnActionEdit().setVisible(false);
                    cardController.getBtnActionHapus().setVisible(false);
                    cardController.getBtnActionEdit().setDisable(true);
                    cardController.getBtnActionHapus().setDisable(true);

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

    private void openHome() {
        RefreshData();
        returnColor();
        HboxHomeBtn.setStyle("-fx-background-color: #4cbc27;");
        LobbyContainer.setContent(MainContainer);
        MainContainer.getChildren().clear();
        MainContainer.getChildren().add(VboxLobbyContainer);
        MainContainer.getChildren().addFirst(HboxDisplayPageName);
        txtDisplayHome.setText("Rekomendasi");
        HboxBtnContainer.setDisable(false);
        HboxBtnContainer.setVisible(true);
    }

    private void openProfile() {
        LobbyContainer.setContent(MainContainer);
        returnColor();
        try {
            FXMLLoader dashboard = new FXMLLoader();
            dashboard.setLocation(getClass().getResource("dashboardUser.fxml"));

            MainContainer.getChildren().clear();
            MainContainer.getChildren().addFirst(dashboard.load());

            FXMLLoader Profil = new FXMLLoader();
            Profil.setLocation(getClass().getResource("UserProfile.fxml"));

            MainContainer.getChildren().addFirst(Profil.load());


            ProfileController profileController = Profil.getController();
            profileController.setProfileData(SesiUser.uid);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private void openLoginPopup(Popup popup, Stage stage) throws IOException {
        if (btnOpenLogin.getText().equals("Logout")){
            SesiUser.LoginSession = 0;

            LoginController.loginStage.close();
            HelloApplication.primaryStage.close();
            HelloApplication.HomeRefresh(stage);
        }else {
//            PopLoginStage = stage;
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("login.fxml"));
            try {
                if (!popup.isShowing()){
                    Scene scene = new Scene(fxmlLoader.load());
                    stage.setScene(scene);
                    stage.show();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void getHelloLogin() throws IOException {
        Popup OpenLoginPopup = new Popup();
        Stage popLoginStage = new Stage();
        OpenLoginPopup.setAutoHide(true);
        openLoginPopup(OpenLoginPopup, popLoginStage);
    }

    public void openAdminDashboard() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("dashboardAdmin.fxml"));
//            VBox cardBox = fxmlLoader.load();

            returnColor();
            HboxDashboardBtn.setStyle("-fx-background-color: #4cbc27;");
//            MainContainer.getChildren().clear();
//            MainContainer.getChildren().addFirst(fxmlLoader.load());

            LobbyContainer.setContent(fxmlLoader.load());
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private void openUserDashboard() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("dashboardUser.fxml"));

            returnColor();
            HboxResepBtn.setStyle("-fx-background-color: #4cbc27;");
//            MainContainer.getChildren().clear();
//            MainContainer.getChildren().addFirst(fxmlLoader.load());

            LobbyContainer.setContent(fxmlLoader.load());
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private void openTopBookmark(String page) {
        LobbyContainer.setContent(MainContainer);
        MainContainer.getChildren().clear();
        MainContainer.getChildren().addFirst(VboxLobbyContainer);
        MainContainer.getChildren().addFirst(HboxDisplayPageName);
        resepContainer.getChildren().clear();
        HboxBtnContainer.setDisable(true);
        HboxBtnContainer.setVisible(false);

        returnColor();
        try {
            String query;
            if (page.equals("bookmark")){
                HboxBookmarkBtn.setStyle("-fx-background-color: #4cbc27;");

                txtDisplayHome.setText("Bookmark");
                query = "SELECT r.* FROM resep r RIGHT JOIN bookmark b ON r.idr = b.idr WHERE b.uid=" + SesiUser.uid;
            }else {
                HboxRankBtn.setStyle("-fx-background-color: #4cbc27;");

                txtDisplayHome.setText("Top Resep");
                query = "SELECT r.*, AVG(k.rating) AS rata_rata FROM resep r INNER JOIN komentar k ON r.idr = k.idr GROUP BY r.nama ORDER BY rata_rata DESC";
            }
            int column = 0;
            int row = 1;
            ResultSet resultSet = connection.createStatement().executeQuery(query);
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
                    cardController.getBtnActionEdit().setVisible(false);
                    cardController.getBtnActionHapus().setVisible(false);
                    cardController.getBtnActionEdit().setDisable(true);
                    cardController.getBtnActionHapus().setDisable(true);

                    if (column == 3){
                        column = 0;
                        ++row;
                    }

                    resepContainer.add(cardBox, column++, row);
                    GridPane.setMargin(cardBox, new Insets(10, 10, 15, 10));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


    }

    private void Cari() {
        resepContainer.getChildren().clear();
        int column = 0;
        int row = 1;
        String cari = txtSearch.getText();
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM resep WHERE nama LIKE ? AND status = 'selesai'");
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
                    cardController.getBtnActionEdit().setVisible(false);
                    cardController.getBtnActionHapus().setVisible(false);
                    cardController.getBtnActionEdit().setDisable(true);
                    cardController.getBtnActionHapus().setDisable(true);

                    if (column == 3){
                        column = 0;
                        ++row;
                    }

                    resepContainer.add(cardBox, column++, row);
                    GridPane.setMargin(cardBox, new Insets(10, 10, 10, 10));

                }catch (IOException e){
                    throw new RuntimeException(e);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void showBTN(String kategori){
        resepContainer.getChildren().clear();
        int column = 0;
        int row = 1;
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM resep WHERE kategori=? AND status = 'selesai'");
            statement.setString(1, kategori);
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
                    cardController.getBtnActionEdit().setVisible(false);
                    cardController.getBtnActionHapus().setVisible(false);
                    cardController.getBtnActionEdit().setDisable(true);
                    cardController.getBtnActionHapus().setDisable(true);

                    if (column == 3){
                        column = 0;
                        ++row;
                    }

                    resepContainer.add(cardBox, column++, row);
                    GridPane.setMargin(cardBox, new Insets(10, 10, 10, 10));

                }catch (IOException e){
                    throw new RuntimeException(e);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void returnColor() {
        HboxHomeBtn.setStyle("-fx-background-color: white;");
        HboxDashboardBtn.setStyle("-fx-background-color: white;");
        HboxResepBtn.setStyle("-fx-background-color: white;");
        HboxBookmarkBtn.setStyle("-fx-background-color: white;");
        HboxRankBtn.setStyle("-fx-background-color: white;");
    }
}