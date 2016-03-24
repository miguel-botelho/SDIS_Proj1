package com.sdis1516t1g02.channels;

import com.sdis1516t1g02.Server;
import com.sdis1516t1g02.protocols.Deletion;
import com.sdis1516t1g02.protocols.MessageType;

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



    @Override
    protected void handleMessage(String header, byte[] body) throws MessageException {
        String splitHeader[]=header.split("\\s+");
        String messageType = splitHeader[0];
        String version = splitHeader[1];
        String senderId = splitHeader[2];
        if(senderId == Server.getInstance().getId())
            return;
        switch (MessageType.valueOf(messageType)){
            case DELETE:
                String fileId = splitHeader[3];
                if(!isValidVersionNumber(version))
                    throw new MessageException(header, MessageException.ExceptionType.VERSION_INVALID);
                if(!isValidFileId(fileId))
                    throw new MessageException(header, MessageException.ExceptionType.FILEID_INVALID_LENGTH);

                String args[]= new String[splitHeader.length-4];
                System.arraycopy(splitHeader,3,args,0,splitHeader.length-1);
                Deletion delete = new Deletion(MessageType.DELETE,version,senderId,fileId,args);
                delete.deleteChunk();
                break;
            default:
                throw new MessageException(header, MessageException.ExceptionType.UNRECOGNIZED_MESSAGE_TYPE);
        }
    }
}
