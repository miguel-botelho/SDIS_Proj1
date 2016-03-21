package com.sdis1516t1g02.channels;

import com.sdis1516t1g02.Server;
import com.sdis1516t1g02.protocols.Deletion;

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
                this.handleReceivedPacket(mpacket);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ChannelException e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }

        }
    }



    @Override
    protected void handleMessage(String header, byte[] body) throws MessageException {
        String splitHeader[]=header.split("\\s+");
        String messageType = splitHeader[0];
        switch (messageType){
            case "DELETE":
                String version = splitHeader[1];
                String fileId = splitHeader[3];
                if(!isValidVersionNumber(version))
                    throw new MessageException(header, MessageException.ExceptionType.VERSION_INVALID);
                if(!isValidFileId(fileId))
                    throw new MessageException(header, MessageException.ExceptionType.FILEID_INVALID_LENGTH);

                String info[]= new String[splitHeader.length-1];
                System.arraycopy(splitHeader,1,info,0,splitHeader.length-1);
                new Thread(new Deletion(info)).start();
                break;
            default:
                throw new MessageException(header, MessageException.ExceptionType.UNRECOGNIZED_MESSAGE_TYPE);
        }
    }
}
