package com.sdis1516t1g02.channels;

import java.io.IOException;
import java.net.InetAddress;

/**
 * Created by Duarte on 19/03/2016.
 */
public class DataBackup extends DataChannel{
    public DataBackup(InetAddress multicastAddress, int mport) throws IOException {
        super(multicastAddress, mport);
    }


    @Override
    protected void handleMessage(String header, byte[] body) {

    }
}
