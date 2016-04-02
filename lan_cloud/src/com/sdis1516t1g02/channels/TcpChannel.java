package com.sdis1516t1g02.channels;

import com.sdis1516t1g02.Server;
import com.sdis1516t1g02.protocols.MessageType;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Observable;

import static com.sdis1516t1g02.channels.Channel.*;

/**
 * Created by duarte on 02-04-2016.
 */
public class TcpChannel extends Observable implements Runnable {

    private ServerSocket serverSocket;
    private int port;

    public TcpChannel() {
        try {
            this.setServerSocket(new ServerSocket());
            this.setPort(getServerSocket().getLocalPort());
        } catch (IOException e) {
            this.setServerSocket(null);
            this.setPort(0);
            e.printStackTrace();
        }
    }

    protected void handleReceivedPacket(Socket socket) throws ChannelException {
        try {
            byte[] data = new byte[socket.getReceiveBufferSize()];
            socket.getInputStream().read(data);
            String header = getHeader(data);
            byte[] body = getBody(data, socket.getReceiveBufferSize());

            handleMessage(header,body);
            Server.getInstance().saveConfigs();
        } catch (MessageException e) {
            e.printStackTrace();
            throw new ChannelException(e);
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleMessage(String header, byte[] body) throws MessageException {
        String splitHeader[]=header.split("\\s+");
        String messageType = splitHeader[0];
        String version = splitHeader[1];
        String senderId = splitHeader[2];
        if(senderId.equals( Server.getInstance().getId()))
            return;
        if(!isValidVersionNumber(version))
            throw new MessageException(header, MessageException.ExceptionType.VERSION_INVALID);
        System.out.println("Received message: "+header+" Body: "+body.length);
        switch (MessageType.valueOf(messageType)){
            case CHUNK:
                int expectedLength = 5;
                if(splitHeader.length < expectedLength)
                    throw new MessageException(header,MessageException.ExceptionType.INVALID_NUMBER_FIELDS);
                String fileId = splitHeader[3];
                if(!isValidFileId(fileId))
                    throw new MessageException(header, MessageException.ExceptionType.FILEID_INVALID_LENGTH);
                String chunkNo = splitHeader[4];
                MessageData messageData = new MessageData(MessageType.valueOf(messageType), Double.valueOf(version),senderId,fileId, Integer.valueOf(chunkNo),body);
                setChanged();
                notifyObservers(messageData);
                break;
            default:
                throw new MessageException(header, MessageException.ExceptionType.UNRECOGNIZED_MESSAGE_TYPE);
        }

    }



    @Override
    public void run() {
        Socket socket;
        while(true){
            try {
                socket = getServerSocket().accept();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            handleReceivedPacket(socket);
                        } catch (ChannelException e) {
                            e.printStackTrace();
                        }
                    }
                }).run();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    public void setServerSocket(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
