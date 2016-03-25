package com.sdis1516t1g02;

import com.sdis1516t1g02.channels.Control;
import com.sdis1516t1g02.channels.DataBackup;
import com.sdis1516t1g02.channels.DataRestore;
import com.sdis1516t1g02.chunks.ChunkManager;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
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
    private final ChunkManager chunckManager;
    private String id;
    private Control mc;
    private DataBackup mdb;
    private DataRestore mdr;
    private Long availableSpace = (long) (1024 * 1024 * 1024); //1GB

    public static Server getInstance(){
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

        this.id = InetAddress.getLocalHost().getHostName();
        this.setMc(new Control(InetAddress.getByAddress(MC_ADDRESS.getBytes()),MC_PORT));
        this.setMdb(new DataBackup(InetAddress.getByAddress(MDB_ADDRESS.getBytes()), MDB_PORT));
        this.setMdr(new DataRestore(InetAddress.getByAddress(MDR_ADDRESS.getBytes()), MDR_PORT));

        new Thread(this.mc).start();
        new Thread(this.mdb).start();
        new Thread(this.mdr).start();

        this.chunckManager = new ChunkManager();

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
}
