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
                this.handleReceivedPacket(mpacket);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ChannelException e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void sendMessage(String message) throws ChannelException, IOException {
        if (message.getBytes().length > Server.DATA_BUF_SIZE)
            throw new ChannelException("Message Size bigger than "+Server.DATA_BUF_SIZE+" bytes.");

        byte[] buf = message.getBytes();
        DatagramPacket datagramPacket = new DatagramPacket(buf,buf.length,multicastAddress,mport);

        mSocket.send(datagramPacket);
    }
}
