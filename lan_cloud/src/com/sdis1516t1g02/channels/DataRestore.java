package com.sdis1516t1g02.channels;

import com.sdis1516t1g02.Server;
import com.sdis1516t1g02.protocols.MessageType;

import java.io.IOException;
import java.net.InetAddress;

/**
 * Created by Duarte on 19/03/2016.
 */
public class DataRestore extends DataChannel {

    /**
     * Creates a new DataRestore channel.
     * @param multicastAddress the address the multicast socket will join
     * @param mport the port used to create the socket
     * @throws IOException
     */
    public DataRestore(InetAddress multicastAddress, int mport) throws IOException {
        super(multicastAddress, mport);
    }

    /**
     * Sends a CHUNK message.
     * @param fileId the id of the file
     * @param chunkNo the number of the chunk
     * @param data the body of the message (the chunk)
     */
    public void sendChunkMessage(String fileId,int chunkNo,byte data[]){
        String header = buildHeader(MessageType.CHUNK.toString(), Server.VERSION, Server.getInstance().getId(),fileId,chunkNo+"");
        byte[] message = buildMessage(header, data);
        try {
            sendMessage(message);
            System.out.println("Sent Restore Message: "+header.split("\\r\\n\\r\\n")[0] +" Body size: "+data.length);
        } catch (ChannelException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Splits the received header to retrieve all of the information. It then notifies the observers that a CHUNK message was received.
     * @param header the header of the message
     * @param body the body of the message
     * @throws MessageException
     */
    @Override
    protected void handleMessage(String header, byte[] body) throws MessageException {
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
}
