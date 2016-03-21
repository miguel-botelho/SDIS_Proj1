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
    protected void handleMessage(String[] header, byte[] body)  {
        String messageType = header[0];
        switch (messageType){
            case "DELETE":
                String version = header[1];
                String fileId = header[3];
                if(!isValidVersionNumber(version))
                    return;//TODO adicionar excepção
                if(!isValidFileId(fileId))
                    return; //TODO adicionar excepção

                String info[]= new String[header.length-1];
                System.arraycopy(header,1,info,0,header.length-1);
                Deletion.deleteChunk(info);
                break;
            default:
                //TODO Fazer log de tipo de mensagem não reconhecida
        }
    }
}
