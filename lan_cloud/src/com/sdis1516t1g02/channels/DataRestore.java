package com.sdis1516t1g02.channels;

import com.sdis1516t1g02.Server;
import com.sdis1516t1g02.protocols.MessageType;
import javafx.beans.Observable;

import javax.sound.midi.SysexMessage;
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
    protected void handleMessage(String header, byte[] body) {

    }
}
