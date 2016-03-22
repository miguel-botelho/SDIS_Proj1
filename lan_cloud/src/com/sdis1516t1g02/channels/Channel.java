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
    protected final static char[] CRLF = {0xD,0xA};

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

    protected byte[] getHeaderLineFromSegment(byte[] message, int i) throws ChannelException {
        int init_i = i;
        for(;i < message.length; i=+2){
            if(i < message.length-1 && message[i] == CRLF[0] && message[i+1] == CRLF[i+1])  //FIM DA MENSAGEM
                break;
        }

        if(i >= message.length)
            throw new ChannelException("Header Line without end sequence <CRLF>");
        int lenght = i- init_i;
        byte[] headerLine = new byte[lenght];

        System.arraycopy(message,init_i,headerLine,0,lenght);
        return headerLine;
    }

    protected void handleReceivedPacket(DatagramPacket mpacket) throws ChannelException {
        byte[] message = mpacket.getData();

        int i;
        ArrayList<String> headerLines = new ArrayList<>();

        for ( i = 0; i < message.length; i+=2) {                 //PERCORRE A MENSAGEM DE CHAR EM CHAR
            if(i < message.length-1 && message[i] == CRLF[0] && message[i+1] == CRLF[i+1])  //FIM DA MENSAGEM
                break;
            else{
                byte[] headerLineBytes = getHeaderLineFromSegment(message, i);      //RETIRA HEADER LINE E AVANÇA PARA O PROXIMO HEADER LINE
                i += headerLineBytes.length;
                String headerl = new String(headerLineBytes);
                headerLines.add(headerl);
            }
        }

        //TODO resolver questao de como efectuar quando o header contem várias header lines
        String header = headerLines.get(0);
        int bodyLength = message.length - i;
        byte[] body = new byte[bodyLength];
        System.arraycopy(message,i,body,0,bodyLength);
        handleMessage(header,body);
    }
    protected abstract void handleMessage(String header, byte[] body);

    private void sendMessage(String message) throws ChannelException, IOException {
        if (message.getBytes().length > Server.CONTROL_BUF_SIZE)
            throw new ChannelException("Message Size bigger than "+Server.CONTROL_BUF_SIZE+" bytes.");

        byte[] buf = message.getBytes();
        DatagramPacket datagramPacket = new DatagramPacket(buf,buf.length,multicastAddress,mport);

        this.updateLogger(message);
        mSocket.send(datagramPacket);
    }
}
