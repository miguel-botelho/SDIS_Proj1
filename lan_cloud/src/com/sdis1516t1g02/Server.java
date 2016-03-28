package com.sdis1516t1g02;

import com.sdis1516t1g02.channels.Control;
import com.sdis1516t1g02.channels.DataBackup;
import com.sdis1516t1g02.channels.DataRestore;
import com.sdis1516t1g02.chunks.ChunkManager;
import com.sdis1516t1g02.testapp.Interface_Listener;

import java.io.IOException;
import java.net.InetAddress;

/**
 * Created by Duarte on 19/03/2016.
 */
public class Server {
    public final static int CHUNK_SIZE= 64*1000;
    public final static int CONTROL_BUF_SIZE= 256;
    public final static int DATA_BUF_SIZE= CHUNK_SIZE+CONTROL_BUF_SIZE;

    public final static String MC_ADDRESS = "224.0.0.128";
    public final static int MC_PORT = 4446;
    public final static String MDB_ADDRESS= "224.0.0.160";
    public final static int MDB_PORT = 4447;
    public final static String MDR_ADDRESS = "224.0.0.192";
    public final static int MDR_PORT = 4448;

    public final static String VERSION = "1.0";

    private static Server ourInstance;
    private final ChunkManager chunckManager = new ChunkManager();
    private final FileManager fileManager = new FileManager();
    private String id;
    private Control mc;
    private DataBackup mdb;
    private DataRestore mdr;
    private Interface_Listener interfaceListener;
    private Long availableSpace = (long) (1024 * 1024 * 1024); //1GB

	private final static LoggerServer logger = new LoggerServer("lan_cloud/logs/server.log");

    public static void main(String[] args) {
        if (args.length != 7) {
            System.out.println("Numero de argumentos errado.");
            return;
        }
        try {
            Server server = new Server(args[0], args[1], Integer.parseInt(args[2]), args[3], Integer.parseInt(args[4]), args[5], Integer.parseInt(args[6]));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Server getInstance() {
        synchronized (ourInstance){
            try{
                if(ourInstance == null)
                    ourInstance  = new Server();
            } catch (IOException e) {
                System.out.println("Unable to start server!");
                e.printStackTrace();
            }
            return ourInstance;
        }
    }

    @Deprecated
    private Server() throws IOException {

        this.id = InetAddress.getLocalHost().getHostName();
        this.setMc(new Control(InetAddress.getByName(MC_ADDRESS),MC_PORT));
		this.setMdb(new DataBackup(InetAddress.getByName(MDB_ADDRESS), MDB_PORT));
        this.setMdr(new DataRestore(InetAddress.getByName(MDR_ADDRESS), MDR_PORT));
        this.setInterfaceListener(new Interface_Listener(Integer.parseInt(this.id)));

		new Thread(this.mc).start();
        new Thread(this.mdb).start();
       	new Thread(this.mdr).start();
        new Thread(this.interfaceListener).start();
    }

    public Server(String id, String mcAddress, int mcPort, String mdbAddress, int mdbPort, String mdrAddress, int mdrPort) throws IOException {
        synchronized (ourInstance){
            if(ourInstance == null){
                System.out.println("Unable to start another server");
                return;
            }
            this.id = id;
            this.setMc(new Control(InetAddress.getByName(mcAddress),mcPort));
            this.setMdb(new DataBackup(InetAddress.getByName(mdbAddress), mdbPort));
            this.setMdr(new DataRestore(InetAddress.getByName(mdrAddress), mdrPort));
            this.setInterfaceListener(new Interface_Listener(Integer.parseInt(this.id)));

            new Thread(this.mc).start();
            new Thread(this.mdb).start();
            new Thread(this.mdr).start();
            new Thread(this.interfaceListener).start();
            ourInstance = this;
        }
    }

    public Control getMc() {
        return mc;
    }

    public void setMc(Control mc) {
        this.mc = mc;
    }

    public DataBackup getMdb() {
        return mdb;
    }

    public void setMdb(DataBackup mdb) {
        this.mdb = mdb;
    }

    public DataRestore getMdr() {
        return mdr;
    }

    public void setMdr(DataRestore mdr) {
        this.mdr = mdr;
    }

    public Interface_Listener getInterfaceListener() {
        return interfaceListener;
    }

    public void setInterfaceListener(Interface_Listener interfaceListener) {
        this.interfaceListener = interfaceListener;
    }

    public long getAvailableSpace() {
        return availableSpace;
    }

    public synchronized boolean hasSpaceForChunk(long chunkSize){
        synchronized (availableSpace){
            if (availableSpace >= chunkSize)
                return true;
            else
                return false;
        }

    }

    public synchronized boolean allocateSpace(long chunkSize){
        synchronized (availableSpace) {
            if (hasSpaceForChunk(chunkSize)) {
                availableSpace -= chunkSize;
                return true;
            } else
                return false;
        }
    }

    public synchronized void freeSpace(long size){
        synchronized (availableSpace){
            availableSpace += size;
        }
    }

    public static String getByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    public String getId() {
        return id;
    }

    public ChunkManager getChunckManager() {
        return chunckManager;
    }

    public FileManager getFileManager() {
        return fileManager;
    }
}
