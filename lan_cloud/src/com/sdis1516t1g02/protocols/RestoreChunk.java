package com.sdis1516t1g02.protocols;

import com.sdis1516t1g02.Server;
import com.sdis1516t1g02.channels.MessageData;
import com.sdis1516t1g02.chunks.ChunkException;
import com.sdis1516t1g02.chunks.ChunkManager;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;

/**
 * Created by Duarte on 28/03/2016.
 */
public class RestoreChunk implements Observer {
    public static final int RETRIEVE_CHUNK_MAX_DELAY = 400; /*IN MILLISECONDS*/
    private static final int ADDRESS_INDEX = 0;
    private static final int PORT_INDEX = 1;

    private final MessageType messageType;
    private final double version;
    private final String serverId;
    private final String fileId;
    private final int chunkNo;
    private final String[] args;

    private Boolean alreadySent = false;
    private final Object alreadySentLock = new Object();


    public RestoreChunk(MessageType messageType, double version, String serverId, String fileId, int chunkNo, String[] args){
        this.messageType = messageType;
        this.version = version;
        this.serverId = serverId;
        this.fileId = fileId;
        this.chunkNo = chunkNo;
        this.args = args;
    }

    public void sendRequestedChunk(){
        Server.getInstance().getMdr().addObserver(this);
        ChunkManager cm = Server.getInstance().getChunckManager();
        if(version >= 1.3){
            try {
                String addressStr = this.args[ADDRESS_INDEX];
                InetAddress address = InetAddress.getByName(addressStr);
                int port = Integer.valueOf(this.args[PORT_INDEX]);
                byte[] data = cm.getChunkData(fileId,chunkNo);
                int delay = new Random().nextInt(RETRIEVE_CHUNK_MAX_DELAY+1);
                Thread.sleep(delay);
                synchronized (alreadySentLock){
                    if(alreadySent)
                        return;
                }
                Server.getInstance().getTcpChannel().sendChunkMessage(fileId,chunkNo,data,address,port);
                byte[] emptyData = new byte[0];
                Server.getInstance().getMdr().sendChunkMessage(fileId,chunkNo,emptyData);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }catch (ChunkException e) {
                e.printStackTrace();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }else if(version >= 1.0){
            try {
                byte[] data = cm.getChunkData(fileId,chunkNo);
                int delay = new Random().nextInt(RETRIEVE_CHUNK_MAX_DELAY+1);
                Thread.sleep(delay);
                synchronized (alreadySentLock){
                    if(alreadySent)
                        return;
                }
                Server.getInstance().getMdr().sendChunkMessage(fileId,chunkNo,data);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }catch (ChunkException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        MessageData messageData = (MessageData) arg;
        MessageType messageType = messageData.getMessageType();
        String fileId = messageData.getFileId();
        int chunkNo = messageData.getChunkNo();

        if(messageType.equals(MessageType.CHUNK) && fileId.equals(fileId) &&  chunkNo == this.chunkNo){
            synchronized (alreadySentLock){
                alreadySent = true;
            }
        }

    }
}
