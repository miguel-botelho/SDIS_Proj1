package com.sdis1516t1g02.channels;

import com.sdis1516t1g02.Server;
import com.sdis1516t1g02.protocols.Deletion;
import com.sdis1516t1g02.protocols.MessageType;
import com.sdis1516t1g02.protocols.Reclaim;
import com.sdis1516t1g02.protocols.Restore;

import java.awt.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;

/**
 * Created by Duarte on 19/03/2016.
 */
public class Control extends Channel {

    /**
     * Creates a new Control channel.
     * @param multicastAddress the address the multicast socket will join
     * @param mport the port used to create the socket
     * @throws IOException
     */
    public Control(InetAddress multicastAddress, int mport) throws IOException {
        super(multicastAddress, mport);
    }

    /**
     * The thread for the Control Channel. It's constantly running and receiving messages.
     */
    @Override
    public void run() {

        while (true){
            byte[] buf = new byte[Server.CONTROL_BUF_SIZE];
            final DatagramPacket mpacket = new DatagramPacket(buf, buf.length);

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

    /**
     * Sends a REMOVED message.
     * @param fileId the id of the file that was deleted
     * @param chunkNo the number of the chunk that was deleted
     */
    public void sendRemovedMessage(String fileId, int chunkNo){
        String header= buildHeader(MessageType.REMOVED.toString(), ""+Server.getVERSION(), Server.getInstance().getId(),fileId,""+chunkNo);
        try {
            sendMessage(header.getBytes());
        } catch (ChannelException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends a STORED message.
     * @param fileId the id of the file that was stored.
     * @param chunkNo the number of the chunk that was stored.
     */
    public void sendStoredMessage(String fileId, int chunkNo){
        String header= buildHeader(MessageType.STORED.toString(), ""+Server.getVERSION(), Server.getInstance().getId(),fileId,""+chunkNo);
        try {
            sendMessage(header.getBytes());
        } catch (ChannelException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends a DELETE message.
     * @param fileId the id of the file to be deleted.
     */
    public void sendDeletedMessage(String fileId){
        String header= buildHeader(MessageType.DELETE.toString(), ""+Server.getVERSION(), Server.getInstance().getId(),fileId);
        try {
            sendMessage(header.getBytes());
        } catch (ChannelException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends a GETCHUNK message
     * @param fileId the id of the file to be retrieved
     * @param chunkNo the number of the chunk to be retrieved
     */
    public void sendGetChunkMessage(String fileId, int chunkNo){
        String header= buildHeader(MessageType.GETCHUNK.toString(), ""+Server.getVERSION(), Server.getInstance().getId(),fileId,""+chunkNo);
        try {
            sendMessage(header.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ChannelException e) {
            e.printStackTrace();
        }
    }

    public void sendConfirmDeleteMessage(String fileId){
        String header=buildHeader(MessageType.CONFIRM_DELETED.toString(),""+Server.getVERSION(),Server.getInstance().getId(),fileId);
        try{
            sendMessage(header.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ChannelException e) {
            e.printStackTrace();
        }
    }

    /**
     * It handles a message received by the Control channel. Splits the header to retrieve all of the information.
     * It then calls either the deleteChunk (for the DELETED message), updateNetworkCopiesOfChunk (for the REMOVED message) and sendRequestedChunk (for the GETCHUNK message)
     * @param header the header of the received message
     * @param body the body of the received message
     * @throws MessageException
     */
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
        System.out.println("Received message: "+header+" Body: "+body.length);

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
            case GETCHUNK:
                expectedLength = 5;
                if(splitHeader.length < expectedLength)
                    throw new MessageException(header,MessageException.ExceptionType.INVALID_NUMBER_FIELDS);
                fileId = splitHeader[3];
                if(!isValidFileId(fileId))
                    throw new MessageException(header, MessageException.ExceptionType.FILEID_INVALID_LENGTH);
                chunkNo = splitHeader[4];
                args= new String[splitHeader.length-expectedLength];
                System.arraycopy(splitHeader,expectedLength,args,0,splitHeader.length-expectedLength);
                Restore.sendRequestedChunk(MessageType.valueOf(messageType),Double.valueOf(version),senderId,fileId,Integer.valueOf(chunkNo),args);
                break;
            case CONFIRM_DELETED:
                expectedLength = 4;
                if(splitHeader.length < expectedLength)
                    throw new MessageException(header,MessageException.ExceptionType.INVALID_NUMBER_FIELDS);
                fileId = splitHeader[3];
                if(!isValidFileId(fileId))
                    throw new MessageException(header, MessageException.ExceptionType.FILEID_INVALID_LENGTH);
                args= new String[splitHeader.length-expectedLength];
                System.arraycopy(splitHeader,expectedLength,args,0,splitHeader.length-expectedLength);
                Deletion.handleDeletedConfirmation(MessageType.valueOf(messageType),Double.valueOf(version),senderId,fileId,args);
                break;
            default:
                throw new MessageException(header, MessageException.ExceptionType.UNRECOGNIZED_MESSAGE_TYPE);
        }
    }
}
