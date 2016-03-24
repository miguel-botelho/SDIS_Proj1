package com.sdis1516t1g02;

import com.sdis1516t1g02.channels.Control;
import com.sdis1516t1g02.channels.DataBackup;
import com.sdis1516t1g02.channels.DataRestore;

import java.io.IOException;
import java.net.InetAddress;

/**
 * Created by Duarte on 19/03/2016.
 */
public class Server {
    public final static int CHUNK_SIZE= 64*1000;
    public final static int CONTROL_BUF_SIZE= 64;
    public final static int DATA_BUF_SIZE= CHUNK_SIZE+CONTROL_BUF_SIZE;

    public final static String MC_ADDRESS = "224.0.0.128";
    public final static int MC_PORT = 4446;
    public final static String MDB_ADDRESS= "224.0.0.160";
    public final static int MDB_PORT = 4447;
    public final static String MDR_ADDRESS = "224.0.0.192";
    public final static int MDR_PORT = 4448;

    private Control mc;
    private DataBackup mdb;
    private DataRestore mdr;

    public static Server getInstance() {
        return ourInstance;
    }

    private final static LoggerServer logger = new LoggerServer("lan_cloud/logs/server.log");

    private static Server ourInstance = new Server();

    private Server() {
        try {
            this.setMc(new Control(InetAddress.getByName(MC_ADDRESS),MC_PORT));
            this.setMdb(new DataBackup(InetAddress.getByName(MDB_ADDRESS), MDB_PORT));
            this.setMdr(new DataRestore(InetAddress.getByName(MDR_ADDRESS), MDR_PORT));

            new Thread(this.mc).start();
            new Thread(this.mdb).start();
            new Thread(this.mdr).start();

            this.logger.updateLogger(mc, true, "msgTeste");

        } catch (IOException e) {
            e.printStackTrace();
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
}
