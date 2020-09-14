package sample;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.net.UnknownHostException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import static javafx.stage.Modality.APPLICATION_MODAL;

public class setupController {

    public TextField PriorityInput;
    public TextField IPInput;
    public Button OKButton;

    public void OKButtonClick(ActionEvent actionEvent) {
        PriorityInput.setEditable(false);
        IPInput.setEditable(false);
        OKButton.setDisable(true);
        String priority = PriorityInput.getText();
        String ip = IPInput.getText();

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("runMenu.fxml"));
            Parent root = fxmlLoader.load();
            runController controller = fxmlLoader.getController();
            controller.nodeInitialization(priority, ip);
            Stage stage = new Stage();
            stage.initModality(APPLICATION_MODAL);
            stage.setOnCloseRequest(t -> {
                Platform.exit();
                System.exit(0);
            });
            stage.setTitle(java.net.InetAddress.getLocalHost().toString());
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @FXML
    public void initialize() throws UnknownHostException {
        IPInput.setText(java.net.InetAddress.getLocalHost().getHostAddress());
        PriorityInput.setText("1");
    }
}
