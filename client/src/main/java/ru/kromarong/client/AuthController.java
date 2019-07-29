package ru.kromarong.client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import ru.kromarong.common.CMDList;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class AuthController implements Initializable {

    @FXML
    TextField tfLogin;

    @FXML
    PasswordField tfPassword;

    @FXML
    Button btLogin;

    private String reg = "Зарегистрироваться";

    public void loginBtn(ActionEvent actionEvent) {
        if (!tfLogin.getText().equals("") && !tfPassword.getText().equals("")) {
            if (btLogin.getText().equals(reg)) {
                Network.getInstance().sendCommand(CMDList.REGISTRATION + " " + tfLogin.getText() + " " + tfPassword.getText());
            } else {
                Network.getInstance().sendCommand(CMDList.AUTH + " " + tfLogin.getText() + " " + tfPassword.getText());
            }
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Thread t = new Thread(() -> {
            try{

            while (true) {
                boolean auth = Network.getInstance().isAuth();
                if (auth) {
                    System.out.println("trying set new scene");
                    changeScene();
                    break;
                } else {
                    if (Network.getInstance().isAuth_error()) {
                        showAuthErrorAlert();
                        Network.getInstance().setAuth_error(false);
                    }
                    if (Network.getInstance().isReg_error()){
                        showRegistrationErrorAlert();
                        Network.getInstance().setReg_error(false);
                    }
                }
            }} catch (IllegalStateException e){
                e.printStackTrace();
            }
        });
        t.setDaemon(true);
        t.start();
    }

    private void showAuthErrorAlert() {
        updateUI(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Неверный логин или пароль", ButtonType.OK);
            alert.setTitle("Ошибка аутентификации!");
            alert.setHeaderText("");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.get().getText().equals("OK")) {
                tfLogin.clear();
                tfPassword.clear();
            }
        });
    }

    private void showRegistrationErrorAlert() {
        updateUI(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Данный логин уже занят", ButtonType.OK);
            alert.setTitle("Ошибка регистрации!");
            alert.setHeaderText("");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.get().getText().equals("OK")) {
                tfLogin.clear();
                tfPassword.clear();
            }
        });
    }

    private void changeScene() {
        updateUI(() -> {
            Stage stage = (Stage) tfLogin.getScene().getWindow();
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/Main.fxml"));
            Parent root = null;
            try {
                root = fxmlLoader.load();
            } catch (IOException e) {
                e.printStackTrace();
            }
            stage.setScene(new Scene(root));
        });
    }

    private static void updateUI(Runnable r) {
        if (Platform.isFxApplicationThread()) {
            r.run();
        } else {
            Platform.runLater(r);
        }
    }

    public void registration(ActionEvent actionEvent) {
        updateUI(() -> {
            btLogin.setText(reg);
        });
    }
}
