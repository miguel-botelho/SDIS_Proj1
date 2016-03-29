package com.sdis1516t1g02.channels;

import com.sdis1516t1g02.Server;
import com.sdis1516t1g02.protocols.MessageType;

import java.io.IOException;
import java.net.InetAddress;

/**
 * Created by Duarte on 19/03/2016.
 */
public class DataRestore extends DataChannel {
    public DataRestore(InetAddress multicastAddress, int mport) throws IOException {
        super(multicastAddress, mport);
    }

    public void sendChunkMessage(String fileId,int chunkNo,byte data[]){
        String header = buildHeader(MessageType.PUTCHUNK.toString(), Server.VERSION, Server.getInstance().getId(),fileId,chunkNo+"");
        String message = buildMessage(header, data);
        try {
            sendMessage(message);
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
        if(senderId.equals( Server.getInstance().getId()))
            return;
        if(!isValidVersionNumber(version))
            throw new MessageException(header, MessageException.ExceptionType.VERSION_INVALID);
        switch (MessageType.valueOf(messageType)){
            case CHUNK:
                int expectedLength = 5;
                if(splitHeader.length < expectedLength)
                    throw new MessageException(header,MessageException.ExceptionType.INVALID_NUMBER_FIELDS);
                String fileId = splitHeader[3];
                if(!isValidFileId(fileId))
                    throw new MessageException(header, MessageException.ExceptionType.FILEID_INVALID_LENGTH);
                String chunkNo = splitHeader[4];
                String[] args= new String[splitHeader.length+1];
                System.arraycopy(splitHeader,0,args,0,splitHeader.length);
                args[splitHeader.length] = new String(body);
                setChanged();
                notifyObservers(args);
                break;
            default:
                throw new MessageException(header, MessageException.ExceptionType.UNRECOGNIZED_MESSAGE_TYPE);
        }

    }
}
