package com.sdis1516t1g02.channels;

import com.sdis1516t1g02.Server;
import com.sdis1516t1g02.protocols.Backup;
import com.sdis1516t1g02.protocols.MessageType;
import com.sun.media.jfxmedia.track.Track;

import java.io.IOException;
import java.net.InetAddress;

/**
 * Created by Duarte on 19/03/2016.
 */
public class DataBackup extends DataChannel{

    /**
     * Creates a new DataBackup channel.
     * @param multicastAddress the address the multicast socket will join
     * @param mport the port used to create the socket
     * @throws IOException
     */
    public DataBackup(InetAddress multicastAddress, int mport) throws IOException {
        super(multicastAddress, mport);
    }

    /**
     * Calls the other sendBackupMessage
     * @param fileId the id of the file
     * @param chunkNo the number of the chunk
     * @param replicationDegree the replication degree of the file
     * @param data the body of the message
     * @return sendBackupMessage
     */
    public int sendBackupMessage(String fileId, int chunkNo, int replicationDegree, byte[] data){
        return sendBackupMessage(Server.getInstance().getId(),fileId,chunkNo,replicationDegree,data);
    }

    /**
     * Builds a message and calls sendMessage with it.
     * @param senderId the id of the peer that sent the message
     * @param fileId the id of the file
     * @param chunkNo the number of the chunk
     * @param replicationDegree the replication degree of the file
     * @param data the body (the chunk)
     * @return the size of the whole message
     */
    public int sendBackupMessage(String senderId,String fileId, int chunkNo, int replicationDegree, byte[] data){
        String header = buildHeader(MessageType.PUTCHUNK.toString(), Server.VERSION, senderId,fileId, ""+chunkNo, ""+replicationDegree);

        byte[] message = Channel.buildMessage(header,data);
        int size = -1;
        try {
            size = sendMessage(message);
            System.out.println("Sent Backup Message: "+header.split("\\r\\n\\r\\n")[0] +" Body size: "+data.length);
        } catch (ChannelException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return size;
    }

    /**
     * It handles a message received by the DataBackup channel. Splits the header to retrieve all of the information.
     * It then calls either the receiveChunk (for the PUTCHUNK message)
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
            case PUTCHUNK:
                int expectedLength = 6;
                if(splitHeader.length < expectedLength)
                    throw new MessageException(header,MessageException.ExceptionType.INVALID_NUMBER_FIELDS);
                String fileId = splitHeader[3];
                if(!isValidFileId(fileId))
                    throw new MessageException(header, MessageException.ExceptionType.FILEID_INVALID_LENGTH);
                String chunkNo = splitHeader[4];
                String replicationDegree = splitHeader[5];
                String[] args= new String[splitHeader.length-expectedLength];
                System.arraycopy(splitHeader,expectedLength,args,0,splitHeader.length-expectedLength);
                Backup.receiveChunk(MessageType.valueOf(messageType),Double.valueOf(version),senderId,fileId,Integer.valueOf(chunkNo),Integer.valueOf(replicationDegree),args,body);
                break;
            default:
                throw new MessageException(header, MessageException.ExceptionType.UNRECOGNIZED_MESSAGE_TYPE);
        }
    }
}
