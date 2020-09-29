package sample.ConfigServer;

import sample.Node;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.PriorityQueue;

public interface ConfigInterface extends Remote {

    PriorityQueue<Node> getHosts() throws RemoteException;

    int getTimeout() throws RemoteException;

}
