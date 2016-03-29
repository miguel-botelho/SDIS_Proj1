package com.sdis1516t1g02;

import com.sdis1516t1g02.channels.Control;
import com.sdis1516t1g02.channels.DataBackup;
import com.sdis1516t1g02.channels.DataRestore;
import com.sdis1516t1g02.chunks.ChunkManager;
import com.sdis1516t1g02.testapp.InterfaceListener;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.UUID;

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
    private int id;
    private Control mc;
    private DataBackup mdb;
    private DataRestore mdr;
    private InterfaceListener interfaceListener;
    private Long availableSpace = (long) (1024 * 1024 * 1024); //1GB


	private final static LoggerServer logger = new LoggerServer("logs/server.log");

    public static Server getInstance() {

        try{
            if(ourInstance == null)
                ourInstance  = new Server();
        } catch (IOException e) {
            System.out.println("Unable to start server!");
            e.printStackTrace();
        }
        return ourInstance;

    }

    private Server() throws IOException {

        this.id = new Random().nextInt(4000);
        this.setMc(new Control(InetAddress.getByName(MC_ADDRESS),MC_PORT));
		this.setMdb(new DataBackup(InetAddress.getByName(MDB_ADDRESS), MDB_PORT));
        this.setMdr(new DataRestore(InetAddress.getByName(MDR_ADDRESS), MDR_PORT));

		new Thread(this.mc).start();
        new Thread(this.mdb).start();
       	new Thread(this.mdr).start();
        new Thread(this.interfaceListener).start();
    }

    public Server(int serverId, String mcAddress, int mcPort, String mdbAddress, int mdbPort, String mdrAddress, int mdrPort) throws IOException {

        if(ourInstance != null){
            System.out.println("Unable to start another server");
            return;
        }
        this.id = serverId;
        this.setMc(new Control(InetAddress.getByName(mcAddress),mcPort));
        this.setMdb(new DataBackup(InetAddress.getByName(mdbAddress), mdbPort));
        this.setMdr(new DataRestore(InetAddress.getByName(mdrAddress), mdrPort));
        this.setInterfaceListener(new InterfaceListener(serverId));

        new Thread(this.mc).start();
        new Thread(this.mdb).start();
        new Thread(this.mdr).start();
        new Thread(this.interfaceListener).start();
        ourInstance = this;
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

    public InterfaceListener getInterfaceListener() {
        return interfaceListener;
    }

    public void setInterfaceListener(InterfaceListener interfaceListener) {
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
        return id+"";
    }

    public ChunkManager getChunckManager() {
        return chunckManager;
    }

    public FileManager getFileManager() {
        return fileManager;
    }
    
    public static void main(String[] args){
    	if(args.length < 7){
    		System.out.println("Illegal number of arguments. <SERVER_ID> <MC_ADDRESS> <MC_PORT> <MDB_ADDRESS> <MDB_PORT> <MDR_ADDRESS> <MDR_PORT>");
    		return;
    	}
    	int id = Integer.valueOf(args[0]);
    	String mcAddress = args[1];
    	int mcPort = Integer.valueOf(args[2]);
    	String mdbAddress = args[3];
    	int mdbPort = Integer.valueOf(args[4]);
    	String mdrAddress = args[5];
    	int mdrPort = Integer.valueOf(args[6]);
    	
    	try {
			Server server = new Server(id, mcAddress, mcPort, mdbAddress, mdbPort, mdrAddress, mdrPort);
			synchronized(server){
				while(true){
					server.wait();
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
