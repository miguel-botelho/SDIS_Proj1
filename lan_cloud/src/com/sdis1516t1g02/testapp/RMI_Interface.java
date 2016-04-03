package com.sdis1516t1g02.testapp;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by m_bot on 28/03/2016.
 */
public interface RMI_Interface extends Remote {

    /**
     * The implementation of the backup by the interface.
     * @param filename the name of the file
     * @param repDegree the replication degree of the file
     * @param enhancement checks the enhancement
     * @return message
     * @throws RemoteException
     */
    String backup(String filename, Integer repDegree, Boolean enhancement) throws RemoteException;

    /**
     * The implementation of the restore by the interface.
     * @param filename the name of the file
     * @param enhancement checks the enhancement
     * @return message
     * @throws RemoteException
     */
    String restore(String filename, Boolean enhancement) throws RemoteException;

    /**
     * The implementation of the delete by the interface.
     * @param filename the name of the file
     * @param enhancement checks the enhancement
     * @return message
     * @throws RemoteException
     */
    String delete(String filename, Boolean enhancement) throws RemoteException;

    /**
     * The implementation of the reclaim by the interface.
     * @param space the space to be reclaimed
     * @param enhancement checks the enhancement
     * @return message
     * @throws RemoteException
     */
    String reclaim(long space, Boolean enhancement) throws RemoteException;
}
