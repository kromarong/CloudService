package ru.kromarong.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Client extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception{
        Network.getInstance().start();
        FXMLLoader fxmlLoader1 = new FXMLLoader(getClass().getResource("/Auth.fxml"));
        Parent auth = fxmlLoader1.load();
        primaryStage.setTitle("Cloud service");
        Scene scene = new Scene(auth);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        Network.getInstance().stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
