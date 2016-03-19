package com.sdis1516t1g02;

/**
 * Created by Duarte on 19/03/2016.
 */
public class Server {
    public final static int CONTROL_BUF_SIZE= 64;
    public final static int DATA_BUF_SIZE= 64*1000+CONTROL_BUF_SIZE;

    private static Server ourInstance = new Server();


    public static Server getInstance() {
        return ourInstance;
    }

    private Server() {
    }
}
