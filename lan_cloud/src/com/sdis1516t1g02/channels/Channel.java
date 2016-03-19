package com.sdis1516t1g02.channels;

import com.sdis1516t1g02.Server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

/**
 * Created by Duarte on 19/03/2016.
 */
public abstract class Channel implements Runnable {
    MulticastSocket mSocket;
    InetAddress multicastAddress;
    int mport;

    public abstract void handleMessage(DatagramPacket mpacket);

    public Channel(InetAddress multicastAddress, int mport) throws IOException {
        this.mSocket = new MulticastSocket(mport);

        this.multicastAddress = multicastAddress;
        this.mport = mport;
    }
}
