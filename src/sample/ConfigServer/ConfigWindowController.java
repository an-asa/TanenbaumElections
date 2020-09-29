package sample.ConfigServer;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import sample.Node;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class ConfigWindowController implements ConfigInterface {

    private static ConfigWindowController serverObject = null;
    public Button fileChooserButton;
    public Button launchButton;
    public TextField timeoutTextField;
    public File chosenfile = null;
    public int timeout = 0;
    public PriorityQueue<Node> hostlist = new PriorityQueue<Node>(Comparator.comparingInt(a -> a.priority));

    public void fileChooserButton_onClicked(ActionEvent actionEvent) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Wybierz plik z hostami");
        chosenfile = chooser.showOpenDialog(null);

        if (chosenfile != null) {
            fileChooserButton.setText(chosenfile.getName());
        }

    }

    public void launchButton_onClicked(ActionEvent actionEvent) throws IOException {
        timeoutTextField.setDisable(true);
        fileChooserButton.setDisable(true);
        launchButton.setDisable(true);

        BufferedReader br = Files.newBufferedReader(Paths.get(chosenfile.getAbsolutePath()), StandardCharsets.US_ASCII);

        String line = br.readLine();

        while (line != null) {
            String[] attributes = line.split(",");
            hostlist.add(new Node(Integer.parseInt(attributes[0]),attributes[1]));
            line = br.readLine();
        }

        timeout = Integer.parseInt(timeoutTextField.getText());

        Registry reg = null;

        try {
            reg = LocateRegistry.createRegistry(1098);
        } catch (Exception e) {
            System.out.println("ERROR: Could not create the registry.");
            e.printStackTrace();
        }

        serverObject = new ConfigWindowController();
        System.out.println("RMI config server online at ");
        System.out.println(java.net.InetAddress.getLocalHost().getHostAddress() + "\n");
        System.out.println("Wczytane węzły:");
        System.out.println("Priorytet\tIP");
        for (Node record : hostlist) {
            System.out.println(record.priority + "\t" + record.ip);
        }
        System.out.println("\nCzas oczekiwania węzła: " + timeout + "ms");

        try {
            reg.rebind("configServer", UnicastRemoteObject.exportObject(serverObject, 0));
        } catch (Exception e) {
            System.out.println("ERROR: Failed to register the server object.");
            e.printStackTrace();
        }
    }

    @Override
    public PriorityQueue<Node> getHosts() throws RemoteException {
        return hostlist;
    }

    @Override
    public int getTimeout() throws RemoteException {
        return timeout;
    }
}
