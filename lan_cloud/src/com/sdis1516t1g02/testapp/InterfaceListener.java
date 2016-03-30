package com.sdis1516t1g02.testapp;

import com.sdis1516t1g02.Server;
import com.sdis1516t1g02.protocols.Backup;
import com.sdis1516t1g02.protocols.Deletion;
import com.sdis1516t1g02.protocols.Reclaim;
import com.sdis1516t1g02.protocols.Restore;

import java.io.FileNotFoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/**
 * Created by m_bot on 28/03/2016.
 */
public class InterfaceListener implements RMI_Interface, Runnable{

    public final Integer PORT_RMI = 1099;

    Integer id;

    public InterfaceListener(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public void run() {
        try {
            LocateRegistry.createRegistry(PORT_RMI);

            RMI_Interface rmiInterface = (RMI_Interface) UnicastRemoteObject.exportObject(Server.getInstance().getInterfaceListener(), 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind(Server.getInstance().getInterfaceListener().getId().toString(), rmiInterface);

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
                if (Backup.backupFile(filename,repDegree))
                    return "File " + filename + " backed up.";
                else return "Error backing up " + filename;
            } catch (FileNotFoundException e) {
                return "File does not exist.";
            }
        }
    }

    @Override
    public String restore(String filename, Boolean enhancement) throws RemoteException {
        if (enhancement)
            return null;
        else
            return String.valueOf(Restore.restoreFile(filename));
    }

    @Override
    public String delete(String filename, Boolean enhancement) throws RemoteException {
        if (enhancement)
            return null;
        else {
            if (Deletion.deleteFileByName(filename))
                return "File " + filename + " deleted.";
            else return "Error deleting file " + filename;
        }
    }

    @Override
    public String reclaim(long space, Boolean enhancement) throws RemoteException {
        long resp = 0;
        if (enhancement)
            return null;
        else resp = Reclaim.reclaimSpace(space);

        if (resp == 0)
            return "Error reclaiming space";
        else return "Space Reclaimed: " + resp;
    }
}
