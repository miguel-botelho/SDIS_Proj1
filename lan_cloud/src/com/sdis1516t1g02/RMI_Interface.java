package com.sdis1516t1g02;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by m_bot on 28/03/2016.
 */
public interface RMI_Interface extends Remote {

    String backup(String file, Integer repDegree) throws RemoteException;

    String restore(String file) throws RemoteException;

    String delete(String file) throws RemoteException;

    String remove(String file) throws RemoteException;
}
