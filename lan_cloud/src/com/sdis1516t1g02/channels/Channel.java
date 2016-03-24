package com.sdis1516t1g02.channels;

import com.sdis1516t1g02.Server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;

/**
 * Created by Duarte on 19/03/2016.
 */
public abstract class Channel implements Runnable {
    protected final static String CRLF = "\r\n";

    MulticastSocket mSocket;
    InetAddress multicastAddress;
    int mport;


    public Channel(InetAddress multicastAddress, int mport) throws IOException {
        this.mSocket = new MulticastSocket(mport);
        this.mSocket.joinGroup(multicastAddress);
        this.mSocket.setTimeToLive(1);

        this.multicastAddress = multicastAddress;
        this.mport = mport;
    }

    protected void handleReceivedPacket(DatagramPacket mpacket) throws ChannelException {
        byte[] message = mpacket.getData();

        String messageStr = new String(message,0,mpacket.getLength());
        String splitMessage[] = messageStr.split("\\r\\n\\r\\n");

        //TODO resolver questao de como efectuar quando o header contem várias header lines
        String header = splitMessage[0];
        byte[] body = new byte[0];
        for(int i = 1; i < splitMessage.length; i++){
            byte[] tempBody = splitMessage[i].getBytes();
            byte[] previousBody = body.clone();
            body = new byte[previousBody.length+tempBody.length];
            System.arraycopy(previousBody,0,body,0,previousBody.length);
            System.arraycopy(tempBody,0,body,previousBody.length,tempBody.length);
        }
        try {
            handleMessage(header,body);
        } catch (MessageException e) {
            e.printStackTrace();
            throw new ChannelException(e);
        }
    }

    public boolean isValidVersionNumber(String versionNumber){
        return versionNumber.matches("\\d\\.\\d");
    }

    public boolean isValidFileId(String fileId){
        return fileId.length() == 64;
    }

    protected abstract void handleMessage(String header, byte[] body) throws MessageException;

    protected void sendMessage(String message) throws ChannelException, IOException {
        if (message.getBytes().length > Server.CONTROL_BUF_SIZE)
            throw new ChannelException("Message Size bigger than "+Server.CONTROL_BUF_SIZE+" bytes.");

        byte[] buf = message.getBytes();
        DatagramPacket datagramPacket = new DatagramPacket(buf,buf.length,multicastAddress,mport);

        mSocket.send(datagramPacket);
    }
}
