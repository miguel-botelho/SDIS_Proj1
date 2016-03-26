package com.sdis1516t1g02.protocols;

import com.sdis1516t1g02.Server;
import com.sdis1516t1g02.chunks.BackupFile;
import com.sdis1516t1g02.chunks.Chunk;
import com.sdis1516t1g02.chunks.ChunkException;

import java.util.Random;

/**
 * Created by Duarte on 19/03/2016.
 */
public class Backup{
    public static final int RESPONSE_MAX_DELAY = 400;       /*IN MILLISECONDS*/
    public static final int TIME_BETWEEN_SEND_CHUNK = 1000; /*IN MILLISECONDS*/
    public static final int TRIES = 5;
    public boolean createAndSendChunk(BackupFile file, int chunkNo, int replicationDegree, byte[] data){
        Chunk chunk = new Chunk(file, chunkNo,replicationDegree);
        chunk.setState(Chunk.State.BACKUP);
        file.getChunks().add(chunk);
        int timeInterval = TIME_BETWEEN_SEND_CHUNK;
        int networkCopies = chunk.getNumNetworkCopies();
        int i = 0;
        while(networkCopies < replicationDegree && i < TRIES) {
            Server.getInstance().getMdb().sendBackupMessage(file.getFileId(), chunkNo, replicationDegree, data);

            try {
                Thread.sleep(timeInterval);
                networkCopies = chunk.getNumNetworkCopies();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            timeInterval *=2;
            i++;
        }

        if(i == TRIES)
            return false;
        else
            return true;
    }

    public static void receiveChunk(MessageType messageType, double version, String senderId, String fileId, int chunkNo, int replicationDegree, String[] args, byte[] data){

        try {
            if(Server.getInstance().getChunckManager().getChunk(fileId,chunkNo).isStored()){
                Server.getInstance().getMc().sendStoredMessage(fileId,chunkNo);
                return;
            }

            Server.getInstance().getChunckManager().addChunk(fileId,chunkNo,replicationDegree,data);
            int delay = new Random().nextInt(RESPONSE_MAX_DELAY+1);
            Thread.sleep(delay);
            Server.getInstance().getMc().sendStoredMessage(fileId,chunkNo);
        } catch (ChunkException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
