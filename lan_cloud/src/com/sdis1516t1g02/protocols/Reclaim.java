package com.sdis1516t1g02.protocols;

import com.sdis1516t1g02.Server;
import com.sdis1516t1g02.chunks.Chunk;
import com.sdis1516t1g02.chunks.ChunkException;

import static com.sdis1516t1g02.protocols.MessageType.REMOVED;
import static com.sdis1516t1g02.protocols.MessageType.STORED;

/**
 * Created by Duarte on 19/03/2016.
 */
public class Reclaim {

    public static boolean reclaimSpace(double space/*in bytes*/){

        return false;
    }

    public static void updateNetworkCopiesOfChunk(MessageType messageType, String versionStr, String senderId, String fileId, String chunkNoStr,String[] args){
        double version = Double.valueOf(versionStr);
        int chunkNo = Integer.valueOf(chunkNoStr);
        if(version >= 1.0){
            try {
                Chunk chunk = Server.getInstance().getChunckManager().getChunk(fileId,chunkNo);
                if(messageType == REMOVED)
                    chunk.decrNetworkCopy();
                else if(messageType == STORED)
                    chunk.incNetworkCopy();
            } catch (ChunkException e) {
                e.printStackTrace();
            }
        }
    }
}
