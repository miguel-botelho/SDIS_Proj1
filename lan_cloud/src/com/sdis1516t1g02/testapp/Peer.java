package com.sdis1516t1g02.testapp;

import com.sdis1516t1g02.protocols.Backup;
import com.sdis1516t1g02.protocols.Deletion;
import com.sdis1516t1g02.protocols.Reclaim;

import java.io.FileNotFoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/**
 * Created by m_bot on 28/03/2016.
 */
public class Peer implements RMI_Interface, Runnable{

    int id;

    public Peer(int id) {
        this.id = id;
    }

    final static String REMOTE = "061195";

    @Override
    public void run() {
        try {
            Peer peer = new Peer();
            LocateRegistry.createRegistry(1099);

            RMI_Interface rmiInterface = (RMI_Interface) UnicastRemoteObject.exportObject(peer, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind(REMOTE, rmiInterface);

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String backup(String filename, Integer repDegree, Boolean enhancement) throws RemoteException {
        if (enhancement)
            return null;
        else {
            try {
                Backup.backupFile(filename); // FALTA ENVIAR A repDegree
            } catch (FileNotFoundException e) {
                return "File does not exist.";
            }
        }
        return "File " + filename + " backed up.";
    }

    @Override
    public String restore(String filename, Boolean enhancement) throws RemoteException {
        return null;
    }

    @Override
    public String delete(String filename, Boolean enhancement) throws RemoteException {

        if (enhancement)
            return null;
        else Deletion.deleteFileByName(filename);

        return "File " + filename + " deleted.";
    }

    @Override
    public String reclaim(long space, Boolean enhancement) throws RemoteException {
        long resp = 0;
        if (enhancement)
            return null;
        else resp = Reclaim.reclaimSpace(space);

        return "Space Reclaimed: " + resp;
    }
}
