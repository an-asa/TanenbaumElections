package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

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
            controller.nodeInitialization(priority,ip);
            Stage stage = new Stage();
            stage.setTitle("Konfiguracja węzła");
            stage.setScene(new Scene(root));
            stage.show();
        } catch(Exception e) {
            e.printStackTrace();
        }

    }
}
