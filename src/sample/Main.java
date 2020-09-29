package sample;

import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedList;
import java.util.List;

public class Main implements RMIInterface {

    private static Main serverObject = null;
    private static RMIInterface rmi = null;

    private static final String nextIp = "192.168.1.100";
    private final int priority = 1;

    private int coordinatorPriority;

    public static void main(String[] args) throws RemoteException, NotBoundException, UnknownHostException, InterruptedException {

        Registry reg = null;

        try {
            reg = LocateRegistry.createRegistry(1099);
        } catch (Exception e) {
            System.out.println("ERROR: Could not create the registry.");
            e.printStackTrace();
        }

        serverObject = new Main();
        System.out.println("RMI server online at ");
        System.out.println(java.net.InetAddress.getLocalHost().getHostAddress());

        try {
            reg.rebind("server", UnicastRemoteObject.exportObject(serverObject, 0));
        } catch (Exception e) {
            System.out.println("ERROR: Failed to register the server object.");
            e.printStackTrace();
        }

        List<Node> host = new LinkedList<>();
        host.add(new Node(1, "192.168.1.101"));

        Registry registry = LocateRegistry.getRegistry(nextIp, 1099);
        rmi = (RMIInterface) registry.lookup("server");
        rmi.sendElect(host);
    }

    @Override
    public void sendElect(List<Node> host) throws InterruptedException, RemoteException, UnknownHostException, NotBoundException {
        receiveElect(host);
    }

    public void receiveElect(List<Node> host) throws InterruptedException, RemoteException, UnknownHostException, NotBoundException {

        boolean elected = false;

        String message = "Otrzymano sygnał ELECT z hostami: \n\n";
        message += "Priorytet\tIP\n";

        System.out.print(message);
        message="";

        for (Node record : host) {
            if (record.priority==priority) elected = true;

            message += record.priority + "\t" + record.ip + "\n";

        }

        System.out.print(message);
        message="";

        if (elected) {

            message += "\nSygnał przeszedł przez pierścień. Rozpoczęto elekcję.\n\n";

            message += "Wysyłam sygnał COORDINATOR o treści: \n\n";
            message += "Priorytet\tIP\n";

            for (Node record : host) {
                message += record.priority + "\t" + record.ip+ "\n";
            }

            message += "\ndo hosta " + nextIp + "\n";
            System.out.println(message);

            Thread.sleep(2000);

            Registry registry = LocateRegistry.getRegistry(nextIp, 1099);
            rmi = (RMIInterface) registry.lookup("server");
            rmi.sendCoordinator(host);

        }
        else {

            message += "\nDodano dane węzła do wiadomości w celu jej dalszego przekazania.\n\n";

            List<Node> newHost = host;
            newHost.add(new Node(priority, java.net.InetAddress.getLocalHost().getHostAddress()));

            message += "Wysłano sygnał ELECT o treści: \n\n";
            message += "Priorytet\tIP\n";

            for (Node record : newHost) {
                message += record.priority + "\t" + record.ip + "\n";
            }

            message += "\ndo hosta " + nextIp + "\n";
            System.out.println(message);

            Thread.sleep(2000);

            Registry registry = LocateRegistry.getRegistry(nextIp, 1099);
            rmi = (RMIInterface) registry.lookup("server");
            rmi.sendElect(newHost);
        }
    }

    @Override
    public void sendCoordinator(List<Node> host) throws InterruptedException, RemoteException, UnknownHostException, NotBoundException {
        receiveCoordinator(host);
    }

    public void receiveCoordinator(List<Node> host) throws InterruptedException, RemoteException, UnknownHostException, NotBoundException {

        Node coordinatorRecord = new Node(0,"");

        String message = "Otrzymano sygnał COORDINATOR z hostami: \n\n";
        message += "Priorytet\tIP\n";

        for (Node record : host) {

            message += record.priority + "\t" + record.ip + "\n";

            if (record.priority > coordinatorRecord.priority){
                coordinatorRecord.priority = record.priority;
                coordinatorRecord.ip = record.ip;
            }
        }

        if (coordinatorRecord.priority != coordinatorPriority) {

            message += "\nZ rekordów wybrano koordynatora: " + coordinatorRecord.priority + "\t" + coordinatorRecord.ip + "\n";

            coordinatorPriority = coordinatorRecord.priority;

            message += "Wysyłam sygnał COORDINATOR o treści: \n\n";
            message += "Priorytet\tIP\n";

            for (Node record : host) {

                message += record.priority + "\t" + record.ip + "\n";

            }

            message += "\ndo hosta " + nextIp + "\n";
            System.out.println(message);

            Thread.sleep(2000);
            Registry registry = LocateRegistry.getRegistry(nextIp, 1099);
            rmi = (RMIInterface) registry.lookup("server");
            rmi.sendCoordinator(host);

        }
        else {

            message += "\nWszystkie węzły przyjęły wybranego koordynatora: " + coordinatorRecord.priority + "\t" + coordinatorRecord.ip + "\n";
            System.out.println(message);

        }
    }
}
