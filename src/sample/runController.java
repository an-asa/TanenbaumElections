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
    RMIInterface rmi = null;

    @Override
    public void sendElect(List<String[]> host) throws RemoteException, InterruptedException {
        receiveElect(host);
    }

    @Override
    public void sendCoordinator(List<String[]> host) throws RemoteException, InterruptedException {
        sendElect(host);
    }

    public void receiveElect(List<String[]> host) throws RemoteException, InterruptedException {

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
                    message += "do hosta " + ip + "\n";
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
                    message += "do hosta " + ip + "\n";
                    logEvent(message);
                } catch (RemoteException | UnknownHostException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public void receiveCoordinator(List<String[]> host) throws RemoteException, InterruptedException {

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
                    message += "do hosta " + ip + "\n";
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

    private void logEvent(String log) throws InterruptedException {
        String logText = ">" + log + "\n";
        System.out.println(logText);
        Thread.sleep(1000);
    }

    public void nodeInitialization(String prority, String ip) throws InterruptedException {
        logEvent("Zainicjalizowano węzeł o nr priorytetu " + prority + " o następniku " + ip);
        this.priority = prority;
        this.ip = ip;
    }

    public void electButtonClicked(ActionEvent actionEvent) throws RemoteException {
        List<String[]> host = new LinkedList<String[]>();
        host.add(new String[] {priority,ip});

        if (rmi != null) {
            try {
                String message = "Wysłano sygnał ELECT o treści: \n\n";
                message += "Priorytet\tIP\n";
                for (String[] record : host) {
                    if (record[0] == this.priority) ;
                    message += record[0] + "\t" + record[1] + "\n";
                }
                message += "do hosta " + ip + "\n";
                logEvent(message);
                rmi.sendElect(host);
            } catch (RemoteException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void initialize() {
        try {
            Registry registry = LocateRegistry.getRegistry(ip, 1099);
            this.rmi = (RMIInterface) registry.lookup("server");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
