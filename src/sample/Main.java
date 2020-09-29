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
    private static final String nextIp = "192.168.1.101";
    private final int priority = 1;
    private int coordinatorPriority;
    private static RMIInterface rmi = null;
    private List<String[]> inithosts;

    public static void main(String[] args) {

        Registry reg = null;

        try {
            reg = LocateRegistry.createRegistry(1099);
        } catch (Exception e) {
            System.out.println("ERROR: Could not create the registry.");
            e.printStackTrace();
        }

        serverObject = new Main();
        System.out.println("RMI server online");

        try {
            reg.rebind("server", UnicastRemoteObject.exportObject(serverObject, 0));
        } catch (Exception e) {
            System.out.println("ERROR: Failed to register the server object.");
            e.printStackTrace();
        }
    }

    @Override
    public void sendElect(List<String[]> host) throws InterruptedException, RemoteException, UnknownHostException, NotBoundException {
        receiveElect(host);
    }

    public void receiveElect(List<String[]> host) throws InterruptedException, RemoteException, UnknownHostException, NotBoundException {

        boolean elected = false;

        String message = "Otrzymano sygnał ELECT z hostami: \n\n";
        message += "Priorytet\tIP\n";

        for (String[] record : host) {
            if (record[0] == Integer.toString(priority)) elected = true;

            message += record[0] + "\t" + record[1] + "\n";

        }

        if (elected) {

            message += "\nSygnał przeszedł przez pierścień. Rozpoczęto elekcję.\n\n";

            message += "Wysyłam sygnał COORDINATOR o treści: \n\n";
            message += "Priorytet\tIP\n";

            for (String[] record : host) {
                if (record[0] == Integer.toString(priority)) message += record[0] + "\t" + record[1] + "\n";
            }

            message += "\ndo hosta " + nextIp + "\n";

            Registry registry = LocateRegistry.getRegistry(nextIp, 1099);
            rmi = (RMIInterface) registry.lookup("server");
            rmi.sendCoordinator(host);

        }
        else {

            message += "\nDodano dane węzła do wiadomości w celu jej dalszego przekazania.\n\n";

            List<String[]> newHost = host;
            newHost.add(new String[]{Integer.toString(priority), java.net.InetAddress.getLocalHost().getHostAddress()});
            rmi.sendElect(newHost);

            message += "Wysłano sygnał ELECT o treści: \n\n";
            message += "Priorytet\tIP\n";

            for (String[] record : newHost) {
                if (record[0] == Integer.toString(priority)) ;
                message += record[0] + "\t" + record[1] + "\n";
            }

            message += "\ndo hosta " + nextIp + "\n";

        }
    }

    @Override
    public void sendCoordinator(List<String[]> host) throws InterruptedException, RemoteException, UnknownHostException, NotBoundException {
        receiveCoordinator(host);
    }

    public void receiveCoordinator(List<String[]> host) throws InterruptedException, RemoteException, UnknownHostException, NotBoundException {

        int maxPriority = 0;
        String[] coordinatorRecord = new String[]{"", ""};

        String message = "Otrzymano sygnał COORDINATOR z hostami: \n\n";
        message += "Priorytet\tIP\n";

        for (String[] record : host) {

            message += record[0] + "\t" + record[1] + "\n";

            if (Integer.parseInt(record[0]) > maxPriority) {
                maxPriority = Integer.parseInt(record[0]);
            }
        }

        if (maxPriority != coordinatorPriority) {

            message += "\nZ rekordów wybrano koordynatora: " + coordinatorRecord[0] + "\t" + coordinatorRecord[1] + "\n";

            coordinatorPriority = maxPriority;

            message += "Wysyłam sygnał COORDINATOR o treści: \n\n";
            message += "Priorytet\tIP\n";

            for (String[] record : host) {

                message += record[0] + "\t" + record[1] + "\n";

            }

            message += "\ndo hosta " + nextIp + "\n";

            Registry registry = LocateRegistry.getRegistry(nextIp, 1099);
            rmi = (RMIInterface) registry.lookup("server");
            rmi.sendElect(host);

        }
        else {

            message += "\nWszystkie węzły przyjęły wybranego koordynatora: " + coordinatorRecord[0] + "\t" + coordinatorRecord[1] + "\n";

        }
    }
}
