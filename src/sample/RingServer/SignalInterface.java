package sample.RingServer;

import sample.Node;

import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.PriorityQueue;

public interface SignalInterface extends Remote {

    void sendElect(List<Node> host) throws RemoteException, InterruptedException, UnknownHostException, NotBoundException;

    void sendCoordinator(List<Node> host) throws RemoteException, InterruptedException, UnknownHostException, NotBoundException;

}
