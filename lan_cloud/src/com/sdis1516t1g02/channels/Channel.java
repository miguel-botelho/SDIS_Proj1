package com.sdis1516t1g02.channels;

import com.sdis1516t1g02.Server;
import com.sun.xml.internal.ws.server.sei.SEIInvokerTube;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardCopyOption;
import java.util.Observable;

/**
 * Created by Duarte on 19/03/2016.
 */
public abstract class Channel extends Observable implements Runnable {
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
        String messageStr = new String(mpacket.getData(),0,mpacket.getLength(), Server.CHARSET);
        String splitMessage[] = messageStr.split("\\r\\n\\r\\n",2);

        //TODO resolver questao de como efectuar quando o header contem várias header lines
        String header = splitMessage[0];
        try {
            byte[] body = splitMessage[1].getBytes(Server.CHARSET);
            handleMessage(header,body);
        } catch (MessageException e) {
            e.printStackTrace();
            throw new ChannelException(e);
        }
    }

    public static boolean isValidVersionNumber(String versionNumber){
        return versionNumber.matches("\\d\\.\\d");
    }

    public static boolean isValidFileId(String fileId){
        return fileId.length() == 64;
    }

    protected abstract void handleMessage(String header, byte[] body) throws MessageException;

    protected int sendMessage(String message) throws ChannelException, IOException {
        byte[] buf = message.getBytes(Server.CHARSET);
        if (buf.length > Server.CONTROL_BUF_SIZE)
            throw new ChannelException("Message Size bigger than "+Server.CONTROL_BUF_SIZE+" bytes.");
        System.out.println("Sent Message: "+message);

        DatagramPacket datagramPacket = new DatagramPacket(buf,buf.length,multicastAddress,mport);
        MulticastSocket socket = new MulticastSocket();
        socket.send(datagramPacket);
        return buf.length;
    }

    protected static String buildMessage(String header, byte[] body){
        return header.concat(new String (body));
    }

    protected static String buildHeader(String... fields){
        String header="";
        for(String field : fields){
            header = header.concat(field+" ");
        }
        header = header.concat(CRLF+CRLF);
        return header;
    }
}
