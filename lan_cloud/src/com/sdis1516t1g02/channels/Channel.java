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

    /**
     * CRLF macro
     */
    protected final static String CRLF = "\r\n";

    /**
     * The multicast socket
     */
    MulticastSocket mSocket;

    /**
     * The multicast address
     */
    InetAddress multicastAddress;

    /**
     * The port used by the socket
     */
    int mport;

    /**
     * Creates a new Channel
     * @param multicastAddress the address the multicast socket will join
     * @param mport the port used to create the socket
     */
    public Channel(InetAddress multicastAddress, int mport) throws IOException {
        this.mSocket = new MulticastSocket(mport);
        this.mSocket.joinGroup(multicastAddress);
        this.mSocket.setTimeToLive(1);

        this.multicastAddress = multicastAddress;
        this.mport = mport;
    }

    /**
     * Extracts the header from a message.
     * @param data the message
     * @return the header
     */
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

    /**
     * Returns the body of the message, given its length
     * @param data the message
     * @param dataLength the length of the message
     * @return the body
     */
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

    /**
     * Creates a new message given an header and a body
     * @param header the header of the message to be sent
     * @param body the body of the message to be sent
     * @return the message to be sent
     */
    public static byte[] buildMessage(String header, byte[] body) {
        byte[] headerByteArray = header.getBytes();
        byte[] message = new byte[headerByteArray.length + body.length];
        System.arraycopy(headerByteArray,0,message,0,headerByteArray.length);
        System.arraycopy(body,0,message,headerByteArray.length,body.length);
        return message;
    }

    /**
     * Receives a packet and calls the handleMessage function, after extracting the header and the body
     * @param mpacket the received packet
     * @throws ChannelException
     */
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

    /**
     * Checks if the version is valid
     * @param versionNumber the version of the message sent
     * @return true if is valid, false if it isn't
     */
    public static boolean isValidVersionNumber(String versionNumber){
        return versionNumber.matches("\\d\\.\\d");
    }

    /**
     * Checks if the id received is valid
     * @param fileId the id of the file
     * @return true if its length is 64, false if it isn't
     */
    public static boolean isValidFileId(String fileId){
        return fileId.length() == 64;
    }

    /**
     * Handles a received message. Implemented on the child classes (Control, DataBackup and DataRestore)
     * @param header the header of the message
     * @param body the body of the message
     * @throws MessageException
     */
    protected abstract void handleMessage(String header, byte[] body) throws MessageException;

    /**
     * Sends a message to the multicast socket
     * @param message the message to be sent
     * @return the length of the message
     * @throws ChannelException
     * @throws IOException
     */
    protected int sendMessage(byte[] message) throws ChannelException, IOException {
        if (message.length > Server.CONTROL_BUF_SIZE)
            throw new ChannelException("Message Size bigger than "+Server.CONTROL_BUF_SIZE+" bytes.");
        System.out.println("Sent Message: "+ message);

        DatagramPacket datagramPacket = new DatagramPacket(message,message.length,multicastAddress,mport);
        MulticastSocket socket = new MulticastSocket();
        socket.send(datagramPacket);
        return message.length;
    }

    /**
     * Builds an header for a message, given the fields to be sent.
     * @param fields the fields to be contained in the header
     * @return the header
     */
    protected static String buildHeader(String... fields){
        String header="";
        for(String field : fields){
            header = header.concat(field+" ");
        }
        header = header.concat(CRLF+CRLF);
        return header;
    }
}
