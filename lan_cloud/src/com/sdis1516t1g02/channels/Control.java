package com.sdis1516t1g02.channels;

import com.sdis1516t1g02.Server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;

/**
 * Created by Duarte on 19/03/2016.
 */
public class Control extends Channel {

    public Control(InetAddress multicastAddress, int mport) throws IOException {
        super(multicastAddress, mport);
    }

    @Override
    public synchronized void run() {

        while (true) {
            byte[] buf = new byte[Server.CONTROL_BUF_SIZE];
        }
    }

    @Override
    protected void handleMessage(String header, byte[] body) {
        this.updateLogger(header);
    }
}
