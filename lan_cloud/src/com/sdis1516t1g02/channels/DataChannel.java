package com.sdis1516t1g02.channels;

import com.sdis1516t1g02.Server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

/**
 * Created by Duarte on 19/03/2016.
 */
public abstract class DataChannel extends Channel {
    public DataChannel(InetAddress multicastAddress, int mport) throws IOException {
        super(multicastAddress,mport);
    }
    @Override
    public void run() {

        while (true) {
            byte[] buf = new byte[Server.DATA_BUF_SIZE];

            final DatagramPacket mpacket = new DatagramPacket(buf, buf.length);
            try {
                this.mSocket.receive(mpacket);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            handleReceivedPacket(mpacket);
                        }catch (ChannelException e) {
                            System.out.println(e.getMessage());
                            e.printStackTrace();
                        }

                    }
                }).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected int sendMessage(byte[] message) throws ChannelException, IOException {
        if (message.length > Server.DATA_BUF_SIZE)
            throw new ChannelException("Message Size bigger than "+Server.DATA_BUF_SIZE+" bytes.");

        DatagramPacket datagramPacket = new DatagramPacket(message,message.length,multicastAddress,mport);
        MulticastSocket socket = new MulticastSocket();
        socket.send(datagramPacket);
        return message.length;
    }
}
