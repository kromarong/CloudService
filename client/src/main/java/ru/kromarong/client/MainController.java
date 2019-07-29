package ru.kromarong.client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import ru.kromarong.common.CMDList;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML
    ListView<String> clientFilesList;

    @FXML
    ListView<String> serverFilesList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        MainHandler.setController(this);
        updateLocalFilesList();
        Network.getInstance().sendCommand(CMDList.REQUEST_FILES_LIST);
    }

    public void pressOnDownloadBtn(ActionEvent actionEvent) {
        if (serverFilesList.getSelectionModel().getSelectedItem() != null) {
            Network.getInstance().sendCommand(CMDList.DOWNLOAD + " \u0000" + (serverFilesList.getSelectionModel().getSelectedItem()));
        }
    }

    public void pressOnUploadBtn(ActionEvent actionEvent) {
        if (clientFilesList.getSelectionModel().getSelectedItem() != null) {
            String filename = clientFilesList.getSelectionModel().getSelectedItem();
            String path = "client_storage/" + clientFilesList.getSelectionModel().getSelectedItem();
            Network.getInstance().sendFile(filename, path);
        }
    }

    public void updateLocalFilesList() {
        updateUI(() -> {
            try {
                clientFilesList.getItems().clear();
                Files.list(Paths.get("client_storage")).map(p -> p.getFileName().toString()).forEach(o -> clientFilesList.getItems().add(o));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }


    public void refreshServerFilesList(List<String> filesName) {
        System.out.println("пытаемся обновить список файлов на сервере");
        updateUI(() -> {
            serverFilesList.getItems().clear();
            for (String value : filesName) {
                serverFilesList.getItems().add(value);
            }
            System.out.println(filesName);
        });
    }

    private static void updateUI(Runnable r) {
        if (Platform.isFxApplicationThread()) {
            r.run();
        } else {
            Platform.runLater(r);
        }
    }

    public void btnDeleteFileOnClient(ActionEvent actionEvent) throws IOException {
        Files.delete(Paths.get("client_storage/" + clientFilesList.getSelectionModel().getSelectedItem()));
        updateLocalFilesList();
    }

    public void btnDeleteFileOnServer(ActionEvent actionEvent) {
        Network.getInstance().sendCommand(CMDList.DELETE + " " + "\u0000" + serverFilesList.getSelectionModel().getSelectedItem() );
    }
}
