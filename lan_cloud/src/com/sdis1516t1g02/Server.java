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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.UUID;

/**
 * Created by Duarte on 19/03/2016.
 */
public class Server {

    /**
     * The maximum size of any chunk.
     */
    public final static int CHUNK_SIZE= 64*1000;

    /**
     * The size of the control buffer.
     */
    public final static int CONTROL_BUF_SIZE= 256;

    /**
     * The size of the data buffer.
     */
    public final static int DATA_BUF_SIZE= CHUNK_SIZE+CONTROL_BUF_SIZE;

    /**
     * The address of the MC Channel.
     */
    public final static String MC_ADDRESS = "224.0.0.128";

    /**
     * The port of the MC Channel.
     */
    public final static int MC_PORT = 4446;

    /**
     * The address of the MDB Channel.
     */
    public final static String MDB_ADDRESS= "224.0.0.160";

    /**
     * The port of the MDB Channel.
     */
    public final static int MDB_PORT = 4447;

    /**
     * The address of the MDR Channel.
     */
    public final static String MDR_ADDRESS = "224.0.0.192";

    /**
     * The port of the MDR Channel.
     */
    public final static int MDR_PORT = 4448;

    /**
     * The Standard Charset ASCII.
     */
    public final static Charset CHARSET= StandardCharsets.US_ASCII;

    /**
     * The default version.
     */
    public static String VERSION = "1.0";

    /**
     * The Maximum version.
     */
    private final static String MAX_VERSION ="1.3";

    /**
     * The instance for the singleton.
     */
    private static Server ourInstance;

    /**
     * The chunk manager.
     */
    private final ChunkManager chunckManager = new ChunkManager();

    /**
     * The file manager.
     */
    private final FileManager fileManager = new FileManager();

    /**
     * The id of the peer.
     */
    private int id;

    /**
     * The control channel.
     */
    private Control mc;

    /**
     * The data backup channel.
     */
    private DataBackup mdb;

    /**
     * The data restore channel.
     */
    private DataRestore mdr;

    /**
     * The interface listener for the TestApp.
     */
    private InterfaceListener interfaceListener;

    /**
     * Space available.
     */
    private Long availableSpace = (long) (1024 * 1024 * 1024); //1GB

    /**
     * The logger.
     */
    private final static LoggerServer logger = new LoggerServer("logs/server.log");

    /**
     * Returns the instance of the Server.
     * @return
     */
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

    /**
     * Creates a new Server.
     * @throws IOException
     */
    @Deprecated
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

    /**
     * Creates a new Server.
     * @param serverId the id of the peer
     * @param mcAddress the address of the mc channel
     * @param mcPort the port of the mc channel
     * @param mdbAddress the address of the mdb channel
     * @param mdbPort the port of the mdb channel
     * @param mdrAddress the address of the mdr channel
     * @param mdrPort the port of the mdr channel
     * @throws IOException
     */
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

    /**
     * Returns the mc channel.
     * @return mc
     */
    public Control getMc() {
        return mc;
    }

    /**
     * Sets the mc channel.
     * @param mc
     */
    public void setMc(Control mc) {
        this.mc = mc;
    }

    /**
     * Returns the mdb channel.
     * @return mdb
     */
    public DataBackup getMdb() {
        return mdb;
    }

    /**
     * Sets the mdb channel.
     * @param mdb
     */
    public void setMdb(DataBackup mdb) {
        this.mdb = mdb;
    }

    /**
     * Returns the mdr channel.
     * @return mdr
     */
    public DataRestore getMdr() {
        return mdr;
    }

    /**
     * Sets the mdr channel.
     * @param mdr
     */
    public void setMdr(DataRestore mdr) {
        this.mdr = mdr;
    }

    /**
     * Returns the interface listener.
     * @return interfaceListener
     */
    public InterfaceListener getInterfaceListener() {
        return interfaceListener;
    }

    /**
     * Sets the interface listener.
     * @param interfaceListener
     */
    public void setInterfaceListener(InterfaceListener interfaceListener) {
        this.interfaceListener = interfaceListener;
    }

    /**
     * Returns the space available.
     * @return availableSpace
     */
    public long getAvailableSpace() {
        return availableSpace;
    }

    /**
     * Checks if there is enough space available to write a chunk.
     * @param chunkSize the size of the chunk
     * @return true if is is, false if it doesn't
     */
    public synchronized boolean hasSpaceForChunk(long chunkSize){
        synchronized (availableSpace){
            if (availableSpace >= chunkSize)
                return true;
            else
                return false;
        }
    }

    /**
     * Allocates space for a chunk.
     * @param chunkSize the size of the chunk
     * @return true if can, false if it can't
     */
    public synchronized boolean allocateSpace(long chunkSize){
        synchronized (availableSpace) {
            if (hasSpaceForChunk(chunkSize)) {
                availableSpace -= chunkSize;
                return true;
            } else
                return false;
        }
    }

    /**
     * Frees space.
     * @param size space that was freed.
     */
    public synchronized void freeSpace(long size){
        synchronized (availableSpace){
            availableSpace += size;
        }
    }

    /**
     * Gets the byte count.
     * @param bytes
     * @param si
     * @return
     */
    public static String getByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    /**
     * Returns the id of the peeer.
     * @return id
     */
    public String getId() {
        return id+"";
    }

    /**
     * Returns the chunk manager.
     * @return chunckManager
     */
    public ChunkManager getChunckManager() {
        return chunckManager;
    }

    /**
     * Returns the file manager.
     * @return fileManager
     */
    public FileManager getFileManager() {
        return fileManager;
    }

    /**
     * The main for the peer. It creates a new Server which creates a new thread for each channel.
     * @param args the arguments given by the user.
     */
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
            getInstance().loadConfigs();
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

    /**
     * Loads the serializables.
     */
    private void loadConfigs(){
        this.chunckManager.deserialize();
        this.fileManager.deserialize();
    }

    /**
     * Saves the serializables.
     */
    public void saveConfigs() {
        this.chunckManager.serialize();
        this.fileManager.serialize();
    }

    /**
     * Returns the current version.
     * @return version
     */
    public static double getVERSION() {
        return Double.valueOf(VERSION);
    }

    /**
     * Sets the program to run in enhancement mode.
     * @param enhancement
     */
    public static void setEnhanceMode(boolean enhancement){
        if(enhancement){
            VERSION = MAX_VERSION;
        }
    }
}
