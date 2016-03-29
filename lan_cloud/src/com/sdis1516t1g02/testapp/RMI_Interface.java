package com.sdis1516t1g02.testapp;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by m_bot on 28/03/2016.
 */
public interface RMI_Interface extends Remote {

    String backup(String filename, Integer repDegree, Boolean enhancement) throws RemoteException;

    String restore(String filename, Boolean enhancement) throws RemoteException;

    String delete(String filename, Boolean enhancement) throws RemoteException;

    String reclaim(long space, Boolean enhancement) throws RemoteException;
}
