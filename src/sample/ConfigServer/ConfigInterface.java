package sample.ConfigServer;

import sample.Node;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ConfigInterface extends Remote {

    List<Node> getHosts() throws RemoteException;

    int getTimeout() throws RemoteException;

}
