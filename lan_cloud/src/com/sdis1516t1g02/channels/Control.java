package com.sdis1516t1g02.channels;

import com.sdis1516t1g02.Server;
import com.sdis1516t1g02.protocols.Deletion;
import com.sdis1516t1g02.protocols.MessageType;
import com.sdis1516t1g02.protocols.Reclaim;

import java.awt.*;
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
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            handleReceivedPacket(mpacket);
                        } catch (ChannelException e) {
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

    //TODO resolver questão de onde devem ser resolvidas as excepções de mandar mensagens
    public void sendRemovedMessage(String fileId, int chunkNo){
        String header= buildHeader(MessageType.REMOVED.toString(), Server.VERSION, Server.getInstance().getId(),fileId,""+chunkNo);
        try {
            sendMessage(header);
        } catch (ChannelException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendStoredMessage(String fileId, int chunkNo){
        String header= buildHeader(MessageType.STORED.toString(), Server.VERSION, Server.getInstance().getId(),fileId,""+chunkNo);
        try {
            sendMessage(header);
        } catch (ChannelException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendDeletedMessage(String fileId, int chunkNo){
        String header= buildHeader(MessageType.DELETE.toString(), Server.VERSION, Server.getInstance().getId(),fileId,""+chunkNo);
        try {
            sendMessage(header);
        } catch (ChannelException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void handleMessage(String header, byte[] body) throws MessageException {
        String splitHeader[]=header.split("\\s+");
        String messageType = splitHeader[0];
        String version = splitHeader[1];
        String senderId = splitHeader[2];
        if(senderId.equals(Server.getInstance().getId()))
            return;
        if(!isValidVersionNumber(version))
            throw new MessageException(header, MessageException.ExceptionType.VERSION_INVALID);
        switch (MessageType.valueOf(messageType)){
            case DELETE:
                int expectedLength = 4;
                if(splitHeader.length < expectedLength)
                    throw new MessageException(header,MessageException.ExceptionType.INVALID_NUMBER_FIELDS);
                String fileId = splitHeader[3];
                if(!isValidFileId(fileId))
                    throw new MessageException(header, MessageException.ExceptionType.FILEID_INVALID_LENGTH);
                String args[]= new String[splitHeader.length-expectedLength];
                System.arraycopy(splitHeader,expectedLength,args,0,splitHeader.length-expectedLength);
                Deletion.deleteChunk(MessageType.DELETE,Double.valueOf(version),senderId,fileId,args);
                break;

            case STORED:    //A recepção da mensagem stored pertence ao protocolo BACKUP mas é tratado no protocol RECLAIM
            case REMOVED:
                expectedLength = 5;
                if(splitHeader.length < expectedLength)
                    throw new MessageException(header,MessageException.ExceptionType.INVALID_NUMBER_FIELDS);
                fileId = splitHeader[3];
                if(!isValidFileId(fileId))
                    throw new MessageException(header, MessageException.ExceptionType.FILEID_INVALID_LENGTH);
                String chunkNo = splitHeader[4];
                args= new String[splitHeader.length-expectedLength];
                System.arraycopy(splitHeader,expectedLength,args,0,splitHeader.length-expectedLength);
                Reclaim.updateNetworkCopiesOfChunk(MessageType.valueOf(messageType),Double.valueOf(version),senderId,fileId,Integer.valueOf(chunkNo),args);
                break;
            default:
                throw new MessageException(header, MessageException.ExceptionType.UNRECOGNIZED_MESSAGE_TYPE);
        }
    }
}
