package sample.ConfigServer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import sample.Node;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.List;

public class ConfigServer extends Application{

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("ConfigWindow.fxml"));

        Scene scene = new Scene(root);

        primaryStage.setTitle("Serwer konfiguracyjny");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
