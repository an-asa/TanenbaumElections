package sample.RingServer;

import sample.ConfigServer.ConfigInterface;
import sample.Node;

import javax.swing.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class RingServer implements SignalInterface {

    private static RingServer serverObject = null;
    private static SignalInterface signalInterface = null;
    private static ConfigInterface configInterface = null;

    private static String configIp;

    private static Node currentNode;
    private static Node nextNode;
    private static int timeout;

    private static int coordinatorPriority;

    public static void main(String[] args) throws RemoteException, NotBoundException, UnknownHostException, InterruptedException {

        String configIp = (String)JOptionPane.showInputDialog(null,"IP serwera konfiguracji","Wskaż serwer konfiguracji",JOptionPane.PLAIN_MESSAGE);

        Object[] options = {"Nie",
                "Tak"};

        int selected = JOptionPane.showOptionDialog(null,
                "Rozpocząć elekcję po uruchomieniu? (Wybierz opcję, jeśli uruchomiono serwery pozostałych węzłów)",
                "Uruchom serwer",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]);

        Registry configRegistry = LocateRegistry.getRegistry(configIp, 1098);
        configInterface = (ConfigInterface) configRegistry.lookup("configServer");

        currentNode = findCurrentAndNext(configInterface.getHosts())[0];
        nextNode = findCurrentAndNext(configInterface.getHosts())[1];
        timeout = configInterface.getTimeout();

        Registry reg = null;

        try {
            reg = LocateRegistry.createRegistry(1099);
        } catch (Exception e) {
            System.out.println("ERROR: Could not create the registry.");
            e.printStackTrace();
        }

        serverObject = new RingServer();
        System.out.println("RMI server online at ");
        System.out.println(java.net.InetAddress.getLocalHost().getHostAddress());

        try {
            reg.rebind("signalServer", UnicastRemoteObject.exportObject(serverObject, 0));
        } catch (Exception e) {
            System.out.println("ERROR: Failed to register the server object.");
            e.printStackTrace();
        }

        if(selected == 1) {
            List<Node> host = new LinkedList<Node>();
            host.add(new Node(currentNode.priority, java.net.InetAddress.getLocalHost().getHostAddress()));

            Registry registry = LocateRegistry.getRegistry(nextNode.ip, 1099);
            signalInterface = (SignalInterface) registry.lookup("signalServer");
            signalInterface.sendElect(host);
        }
    }

    public static Node[] findCurrentAndNext(List<Node> host) throws UnknownHostException {
        Node currentNode = new Node(0,"");
        Node nextNode = new Node(0,"");
        for (Node record : host) {
            if (record.ip.equals(InetAddress.getLocalHost().getHostAddress())){
                currentNode = new Node(record.priority,record.ip);
                if(host.indexOf(record) + 1 < host.size()) {
                    nextNode = host.get(host.indexOf(record)+1);
                }else{
                    nextNode = host.get(0);
                }
            }
        }
        Node[] nodes = new Node[] {currentNode, nextNode};
        return nodes;
    }

    @Override
    public void sendElect(List<Node> host) throws InterruptedException, RemoteException, UnknownHostException, NotBoundException {
        receiveElect(host);
    }

    public void receiveElect(List<Node> host) throws InterruptedException, RemoteException, UnknownHostException, NotBoundException {

        boolean elected = false;

        String message = "Otrzymano sygnał ELECT z hostami: \n\n";
        message += "Priorytet\tIP\n";

        for (Node record : host) {
            if (record.priority==currentNode.priority) elected = true;

            message += record.priority + "\t" + record.ip + "\n";

        }

        if (elected) {

            message += "\nSygnał przeszedł przez pierścień. Rozpoczęto elekcję.\n\n";

            message += "Wysyłam sygnał COORDINATOR o treści: \n\n";
            message += "Priorytet, IP:\n";

            for (Node record : host) {
                message += record.priority + ", " + record.ip+ "\n";
            }

            message += "\ndo hosta " + nextNode.ip + ".\n";

            JOptionPane.showMessageDialog(null,
                    message,
                    "Sygnał ELECT",
                    JOptionPane.PLAIN_MESSAGE);

            Registry registry = LocateRegistry.getRegistry(nextNode.ip, 1099);
            signalInterface = (SignalInterface) registry.lookup("signalServer");
            signalInterface.sendCoordinator(host);

        }
        else {

            message += "\nDodano dane węzła do wiadomości w celu jej dalszego przekazania.\n\n";

            List<Node> newHost = host;
            newHost.add(new Node(currentNode.priority, java.net.InetAddress.getLocalHost().getHostAddress()));

            message += "Wysłano sygnał ELECT o treści: \n\n";
            message += "Priorytet, IP:\n";

            for (Node record : newHost) {
                message += record.priority + ", " + record.ip + "\n";
            }

            message += "\ndo hosta " + nextNode.ip + ".\n";

            JOptionPane.showMessageDialog(null,
                    message,
                    "Sygnał ELECT",
                    JOptionPane.PLAIN_MESSAGE);

            Registry registry = LocateRegistry.getRegistry(nextNode.ip, 1099);
            signalInterface = (SignalInterface) registry.lookup("signalServer");
            signalInterface.sendElect(newHost);
        }
    }

    @Override
    public void sendCoordinator(List<Node> host) throws InterruptedException, RemoteException, UnknownHostException, NotBoundException {
        receiveCoordinator(host);
    }

    public void receiveCoordinator(List<Node> host) throws InterruptedException, RemoteException, UnknownHostException, NotBoundException {

        Node coordinatorRecord = new Node(0,"");

        String message = "Otrzymano sygnał COORDINATOR z hostami: \n\n";
        message += "Priorytet, IP\n";

        for (Node record : host) {

            message += record.priority + ", " + record.ip + "\n";

            if (record.priority > coordinatorRecord.priority){
                coordinatorRecord.priority = record.priority;
                coordinatorRecord.ip = record.ip;
            }
        }

        if (coordinatorRecord.priority != coordinatorPriority) {

            message += "\nZ rekordów wybrano koordynatora: " + coordinatorRecord.priority + "\t" + coordinatorRecord.ip + "\n";

            coordinatorPriority = coordinatorRecord.priority;

            message += "Wysyłam sygnał COORDINATOR o treści: \n\n";
            message += "Priorytet, IP\n";

            for (Node record : host) {

                message += record.priority + ", " + record.ip + "\n";

            }

            message += "\ndo hosta " + nextNode.ip + "\n";

            JOptionPane.showMessageDialog(null,
                    message,
                    "Sygnał COORDINATOR",
                    JOptionPane.PLAIN_MESSAGE);

            Registry registry = LocateRegistry.getRegistry(nextNode.ip, 1099);
            signalInterface = (SignalInterface) registry.lookup("signalServer");
            signalInterface.sendCoordinator(host);

        }
        else {

            message += "\nWszystkie węzły przyjęły wybranego koordynatora: " + coordinatorRecord.priority + "\t" + coordinatorRecord.ip + "\n";

            JOptionPane.showMessageDialog(null,
                    message,
                    "Sygnał COORDINATOR",
                    JOptionPane.PLAIN_MESSAGE);

        }
    }
}
