package com.sdis1516t1g02.channels;

import com.sdis1516t1g02.Server;
import com.sun.xml.internal.ws.server.sei.SEIInvokerTube;

import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
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

    public static String getHeader(byte[] data) {
        ByteArrayInputStream stream = new ByteArrayInputStream(data);
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(stream));
        String header;
        try {
            header = reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }

        return header;
    }

    public static byte[] getBody(byte[] data, int dataLength) {
        ByteArrayInputStream stream = new ByteArrayInputStream(data);
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

        String tempLine = null;
        int sumLength = 0;
        int numLines = 0;
        try {
            do {

                    tempLine = reader.readLine();

                    sumLength += tempLine.length();
                    numLines++;

            } while (!tempLine.isEmpty());
        } catch (IOException e) {
            e.printStackTrace();
        }
        int bodyStartIndex = sumLength + numLines * Channel.CRLF.getBytes().length;

        byte[] body = Arrays.copyOfRange(data, bodyStartIndex, dataLength);
        return body;
    }

    public static byte[] buildMessage(String header, byte[] body) {
        byte[] headerByteArray = header.getBytes();
        byte[] message = new byte[headerByteArray.length + body.length];
        System.arraycopy(headerByteArray,0,message,0,headerByteArray.length);
        System.arraycopy(body,0,message,headerByteArray.length,body.length);
        return message;
    }

    protected void handleReceivedPacket(DatagramPacket mpacket) throws ChannelException {
        byte[] data = mpacket.getData();
        String header = getHeader(data);
        byte[] body = getBody(data, mpacket.getLength());

        //TODO resolver questao de como efectuar quando o header contem várias header lines
        try {
            handleMessage(header,body);
            Server.getInstance().saveConfigs();
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

    protected int sendMessage(byte[] message) throws ChannelException, IOException {
        if (message.length > Server.CONTROL_BUF_SIZE)
            throw new ChannelException("Message Size bigger than "+Server.CONTROL_BUF_SIZE+" bytes.");
        System.out.println("Sent Message: "+message);

        DatagramPacket datagramPacket = new DatagramPacket(message,message.length,multicastAddress,mport);
        MulticastSocket socket = new MulticastSocket();
        socket.send(datagramPacket);
        return message.length;
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
