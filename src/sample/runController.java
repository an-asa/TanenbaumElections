package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;

import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;

public class runController implements RMIInterface {
    public String ip;
    public String priority;
    public Button electButton;
    public TextArea logField;
    public String[] coordinator;

    @Override
    public void sendElect(List<String[]> host) throws RemoteException {

        electButton.setDisable(true);
        boolean elected = false;
        String message = "Otrzymano sygnał ELECT z hostami: \n\n";
        message += "Priorytet\tIP\n";

        for (String[] record : host) {
            if (record[0] == this.priority) elected = true;
            message += record[0] + "\t" + record[1] + "\n";
        }

        message += "\n";

        if (elected) {

            message += "Sygnał przeszedł przez pierścień. Rozpoczęto elekcję.";
            logEvent(message);
            RMIInterface rmi = null;

            try {
                Registry registry = LocateRegistry.getRegistry(ip, 1099);
                rmi = (RMIInterface) registry.lookup("server");
                System.out.println("Connected to Server");
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (rmi != null) {
                try {
                    List<String[]> newHost = new LinkedList<String[]>();
                    newHost.add(new String[]{priority, java.net.InetAddress.getLocalHost().toString()});
                    rmi.sendCoordinator(newHost);

                    message = null;
                    message += "Wysłano sygnał COORDINATOR o treści: \n\n";
                    message += "Priorytet\tIP\n";
                    for (String[] record : newHost) {
                        if (record[0] == this.priority)
                            message += record[0] + "\t" + record[1] + "\n";
                    }
                    message += "do hosta" + ip + "\n";
                    logEvent(message);
                } catch (RemoteException | UnknownHostException e) {
                    e.printStackTrace();
                }
            }

        } else {

            message += "Dodano dane węzła do wiadomości w celu jej dalszego przekazania.";
            logEvent(message);
            RMIInterface rmi = null;

            try {
                Registry registry = LocateRegistry.getRegistry(ip, 1099);
                rmi = (RMIInterface) registry.lookup("server");
                System.out.println("Connected to Server");
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (rmi != null) {
                try {
                    List<String[]> newHost = host;
                    newHost.add(new String[]{priority, java.net.InetAddress.getLocalHost().getHostAddress()});
                    rmi.sendElect(newHost);

                    message = null;
                    message += "Wysłano sygnał ELECT o treści: \n\n";
                    message += "Priorytet\tIP\n";
                    for (String[] record : newHost) {
                        if (record[0] == this.priority) ;
                        message += record[0] + "\t" + record[1] + "\n";
                    }
                    message += "do hosta" + ip + "\n";
                    logEvent(message);
                } catch (RemoteException | UnknownHostException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    @Override
    public void sendCoordinator(List<String[]> host) throws RemoteException {

        electButton.setDisable(true);
        int maxPriority = 0;
        String[] coordinatorRecord = new String[]{"", ""};
        String message = "Otrzymano sygnał COORDINATOR z hostami: \n\n";

        for (String[] record : host) {
            message += record[0] + "\t" + record[1] + "\n";
            if (Integer.parseInt(record[0]) > maxPriority) {
                maxPriority = Integer.parseInt(record[0]);
                coordinatorRecord = record;
            }
        }

        if (coordinatorRecord != this.coordinator) {
            message += "\nZ rekordów wybrano koordynatora: " + coordinatorRecord[0] + "\t" + coordinatorRecord[1] + "\n";
            logEvent(message);
            RMIInterface rmi = null;

            try {
                Registry registry = LocateRegistry.getRegistry(ip, 1099);
                rmi = (RMIInterface) registry.lookup("server");
                System.out.println("Connected to Server");
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (rmi != null) {
                try {
                    rmi.sendElect(host);

                    message = null;
                    message += "Wysłano sygnał COORDINATOR o treści: \n\n";
                    message += "Priorytet\tIP\n";
                    for (String[] record : host) {
                        if (record[0] == this.priority) ;
                        message += record[0] + "\t" + record[1] + "\n";
                    }
                    message += "do hosta" + ip + "\n";
                    logEvent(message);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        } else {
            message += "\nWszystkie węzły przyjęły wybranego koordynatora: " + coordinatorRecord[0] + "\t" + coordinatorRecord[1] + "\n";
            logEvent(message);
        }

    }

    private void logEvent(String log) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        String logText = "> [" + dtf.format(now) + "] " + log + "\n";
        logField.setText(logText);
    }

    public void nodeInitialization(String prority, String ip) {
        logEvent("Zainicjalizowano węzeł o nr priorytetu " + prority + " o następniku " + ip);
        this.priority = prority;
        this.ip = ip;
    }

    public void electButtonClicked(ActionEvent actionEvent) throws RemoteException {
        electButton.setDisable(true);
        RMIInterface rmi = null;
        List<String[]> host = new LinkedList<String[]>();
        host.add(new String[] {priority,ip});

        try {
            Registry registry = LocateRegistry.getRegistry(ip, 1099);
            rmi = (RMIInterface) registry.lookup("server");
            System.out.println("Connected to Server");
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (rmi != null) {
            try {
                rmi.sendElect(host);

                String message = "Wysłano sygnał ELECT o treści: \n\n";
                message += "Priorytet\tIP\n";
                for (String[] record : host) {
                    if (record[0] == this.priority) ;
                    message += record[0] + "\t" + record[1] + "\n";
                }
                message += "do hosta" + ip + "\n";
                logEvent(message);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void initialize() {
        Registry reg = null;
        try {
            reg = LocateRegistry.createRegistry(1099);
        } catch (Exception e) {
            System.out.println("ERROR: Could not create the registry.");
            e.printStackTrace();
        }
        runController serverObject = new runController();
        System.out.println("RMI server online");
        try {
            reg.rebind("server", UnicastRemoteObject.exportObject(serverObject, 0));
        } catch (Exception e) {
            System.out.println("ERROR: Failed to register the server object.");
            e.printStackTrace();
        }
    }
}
