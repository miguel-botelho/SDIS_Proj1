package com.sdis1516t1g02.channels;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;

/**
 * Created by Duarte on 19/03/2016.
 */
public class DataRestore extends DataChannel {
    public DataRestore(InetAddress multicastAddress, int mport) throws IOException {
        super(multicastAddress, mport);
    }

    @Override
    public void handleMessage(DatagramPacket mpacket) {

    }
}
