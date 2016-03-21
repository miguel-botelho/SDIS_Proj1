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
    public void run() {
        while (true){
            byte[] buf = new byte[Server.CONTROL_BUF_SIZE];
            DatagramPacket mpacket = new DatagramPacket(buf, buf.length);

            try {
                this.mSocket.receive(mpacket);
                this.handleReceivedPacket(mpacket);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ChannelException e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }

        }
    }

    @Override
    protected void handleMessage(String header, byte[] body) {

    }
}
