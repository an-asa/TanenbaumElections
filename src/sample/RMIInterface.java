package sample;

import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface RMIInterface extends Remote {

    void sendElect(List<Node> host) throws RemoteException, InterruptedException, UnknownHostException, NotBoundException;

    void sendCoordinator(List<Node> host) throws RemoteException, InterruptedException, UnknownHostException, NotBoundException;

}
