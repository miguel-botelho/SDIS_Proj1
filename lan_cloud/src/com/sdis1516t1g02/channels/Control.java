package com.sdis1516t1g02.channels;

import com.sdis1516t1g02.Server;
import com.sdis1516t1g02.protocols.Deletion;
import com.sdis1516t1g02.protocols.MessageType;
import com.sdis1516t1g02.protocols.Reclaim;

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

    public void sendRemovedMessage(String fileId, int chunkNo){
        String header= buildHeader(MessageType.REMOVED.toString(), Server.VERSION, Server.getInstance().getId(),fileId,""+chunkNo);
        try {
            sendMessage(header);
        } catch (ChannelException e) {
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
        if(senderId == Server.getInstance().getId())
            return;
        if(!isValidVersionNumber(version))
            throw new MessageException(header, MessageException.ExceptionType.VERSION_INVALID);
        switch (MessageType.valueOf(messageType)){
            case DELETE:
                if(splitHeader.length < 4)
                    throw new MessageException(header,MessageException.ExceptionType.INVALID_NUMBER_FIELDS);
                String fileId = splitHeader[3];
                if(!isValidFileId(fileId))
                    throw new MessageException(header, MessageException.ExceptionType.FILEID_INVALID_LENGTH);
                String args[]= new String[splitHeader.length-4];
                System.arraycopy(splitHeader,3,args,0,splitHeader.length-1);
                Deletion delete = new Deletion(MessageType.DELETE,version,senderId,fileId,args);
                delete.deleteChunk();
                break;
            case STORED:    //A recepção da mensagem stored pertence ao protocolo
            case REMOVED:
                if(splitHeader.length < 5)
                    throw new MessageException(header,MessageException.ExceptionType.INVALID_NUMBER_FIELDS);
                fileId = splitHeader[3];
                if(!isValidFileId(fileId))
                    throw new MessageException(header, MessageException.ExceptionType.FILEID_INVALID_LENGTH);
                String chunkNo = splitHeader[4];
                args= new String[splitHeader.length-5];
                System.arraycopy(splitHeader,3,args,0,splitHeader.length-1);
                Reclaim.updateNetworkCopiesOfChunk(MessageType.valueOf(messageType),version,senderId,fileId,chunkNo,args);
                break;
            default:
                throw new MessageException(header, MessageException.ExceptionType.UNRECOGNIZED_MESSAGE_TYPE);
        }
    }
}
