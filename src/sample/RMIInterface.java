package sample;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface RMIInterface extends Remote {

    public void sendElect(List<String[]> host) throws RemoteException, InterruptedException;

    public void sendCoordinator(List<String[]> host) throws RemoteException, InterruptedException;

}
