package com.sdis1516t1g02.protocols;

import com.sdis1516t1g02.Server;
import com.sdis1516t1g02.chunks.ChunkException;

import java.util.Random;

/**
 * Created by Duarte on 19/03/2016.
 */
public class Backup{
    public static final int RESPONSE_DELAY = 400;
    public int sendChunk(String fileId, int chunkNo,int replicationDegree,byte[] data){
        return Server.getInstance().getMdb().sendBackupMessage(fileId,chunkNo,replicationDegree,data);
    }

    public static void receiveChunk(MessageType messageType, String versionStr, String senderId, String fileId, String chunkNoStr, String replicationDegreeStr, String[] args, byte[] data){
        double version = Double.valueOf(versionStr);
        int chunkNo = Integer.valueOf(chunkNoStr);
        int replicationDegree = Integer.valueOf(replicationDegreeStr);

        try {
            Server.getInstance().getChunckManager().addChunk(fileId,chunkNo,replicationDegree,data);
            int delay = new Random().nextInt(400+1);
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
