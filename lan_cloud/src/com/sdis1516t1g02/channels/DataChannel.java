package com.sdis1516t1g02.channels;

import com.sdis1516t1g02.Server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;

/**
 * Created by Duarte on 19/03/2016.
 */
public abstract class DataChannel extends Channel {
    public DataChannel(InetAddress multicastAddress, int mport) throws IOException {
        super(multicastAddress,mport);
    }
    @Override
    public void run() {

        while (true){
            byte[] buf = new byte[Server.DATA_BUF_SIZE];

            DatagramPacket mpacket = new DatagramPacket(buf,buf.length);
            try {
                this.mSocket.receive(mpacket);
                this.handleMessage(mpacket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
