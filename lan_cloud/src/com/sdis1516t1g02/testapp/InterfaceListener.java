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

    /**
     * The port used by the RMI.
     */
    public final Integer PORT_RMI = 1099;

    /**
     * The id of the peer.
     */
    Integer id;

    /**
     * Creates a new Interface Listener.
     * @param id
     */
    public InterfaceListener(Integer id) {
        this.id = id;
    }

    /**
     * Returns the id of the peer.
     * @return id
     */
    public Integer getId() {
        return id;
    }

    /**
     * Sets the id of the peer.
     * @param id
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * The thread that runs the RMI, connects to it.
     */
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

    /**
     * The implementation of the backup by the interface.
     * @param filename the name of the file
     * @param repDegree the replication degree
     * @param enhancement checks the enhancement
     * @return message
     * @throws RemoteException
     */
    @Override
    public String backup(String filename, Integer repDegree, Boolean enhancement) throws RemoteException {
        if (enhancement)
            Server.getInstance().setEnhanceMode(true);
        try {
            if (Backup.backupFile(filename,repDegree))
                return "File " + filename + " backed up.";
            else return "Error backing up " + filename;
        } catch (FileNotFoundException e) {
            return "File does not exist.";
        }
    }

    /**
     * The implementation of the restore by the interface.
     * @param filename the name of the file
     * @param enhancement checks the enhancement
     * @return message
     * @throws RemoteException
     */
    @Override
    public String restore(String filename, Boolean enhancement) throws RemoteException {
        if (enhancement)
            Server.getInstance().setEnhanceMode(true);
        if (Restore.restoreFile(filename))
            return "File " + filename + "restored.";
        else return "Error restoring file " + filename;
    }

    /**
     * The implementation of the delete by the interface.
     * @param filename the name of the file
     * @param enhancement checks the enhancement
     * @return message
     * @throws RemoteException
     */
    @Override
    public String delete(String filename, Boolean enhancement) throws RemoteException {
        if (enhancement)
            Server.getInstance().setEnhanceMode(true);
        if (Deletion.deleteFileByName(filename))
            return "File " + filename + " deleted.";
        else return "Error deleting file " + filename;
    }

    /**
     * The implementation of the reclaim by the interface.
     * @param space the space to be reclaimed
     * @param enhancement checks the enhancement
     * @return message
     * @throws RemoteException
     */
    @Override
    public String reclaim(long space, Boolean enhancement) throws RemoteException {
        if (enhancement)
            Server.getInstance().setEnhanceMode(true);

        long resp = Reclaim.reclaimSpace(space);
        if (resp == 0)
            return "Error reclaiming space";
        else return "Space Reclaimed: " + resp;
    }
}
