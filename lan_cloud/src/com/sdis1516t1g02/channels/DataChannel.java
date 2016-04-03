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

    /**
     * Creates a new DataChannel.
     * @param multicastAddress the address the multicast socket will join
     * @param mport the port used to create the socket
     * @throws IOException
     */
    public DataChannel(InetAddress multicastAddress, int mport) throws IOException {
        super(multicastAddress,mport);
    }

    /**
     * The thread for the DataChannel, that will be inherited by the Restore and Backup Channel.
     */
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

    /**
     * Sends a message to the multicast socket.
     * @param message the message to be sent
     * @return the length of the message that was sent
     * @throws ChannelException
     * @throws IOException
     */
    protected int sendMessage(byte[] message) throws ChannelException, IOException {
        if (message.length > Server.DATA_BUF_SIZE)
            throw new ChannelException("Message Size bigger than "+Server.DATA_BUF_SIZE+" bytes.");

        DatagramPacket datagramPacket = new DatagramPacket(message,message.length,multicastAddress,mport);
        MulticastSocket socket = new MulticastSocket();
        socket.send(datagramPacket);
        return message.length;
    }
}
