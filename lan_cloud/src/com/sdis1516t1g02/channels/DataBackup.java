package com.sdis1516t1g02.channels;

import com.sdis1516t1g02.Server;
import com.sdis1516t1g02.protocols.Backup;
import com.sdis1516t1g02.protocols.MessageType;

import java.io.IOException;
import java.net.InetAddress;

/**
 * Created by Duarte on 19/03/2016.
 */
public class DataBackup extends DataChannel{
    public DataBackup(InetAddress multicastAddress, int mport) throws IOException {
        super(multicastAddress, mport);
    }

    public int sendBackupMessage(String fileId, int chunkNo, int replicationDegree, byte[] data){
        String header = buildHeader(MessageType.PUTCHUNK.toString(), Server.VERSION, Server.getInstance().getId(),fileId, ""+chunkNo, ""+replicationDegree);
        String message = header.concat(new String(data));
        int size = -1;
        try {
            size = sendMessage(message);
        } catch (ChannelException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return size;
    }

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
                Backup.receiveChunk(MessageType.valueOf(messageType),version,senderId,fileId,chunkNo,replicationDegree,args,body);
                break;
            default:
                throw new MessageException(header, MessageException.ExceptionType.UNRECOGNIZED_MESSAGE_TYPE);
        }
    }
}
