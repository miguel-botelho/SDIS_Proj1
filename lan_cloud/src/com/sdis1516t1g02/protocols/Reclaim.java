package com.sdis1516t1g02.protocols;

import com.sdis1516t1g02.Server;
import com.sdis1516t1g02.chunks.Chunk;
import com.sdis1516t1g02.chunks.ChunkException;
import com.sdis1516t1g02.chunks.ChunkManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static com.sdis1516t1g02.protocols.MessageType.REMOVED;
import static com.sdis1516t1g02.protocols.MessageType.STORED;

/**
 * Created by Duarte on 19/03/2016.
 */
public class Reclaim {

    public static long reclaimSpace(long space){
        ChunkManager cm = Server.getInstance().getChunckManager();
        ArrayList<Chunk> chunks = cm.getStoredChunks();
        Collections.sort(chunks, new Comparator<Chunk>() {
            @Override
            public int compare(Chunk o1, Chunk o2) {
                return o2.compareTo(o1);
            }
        });

        long reclaimedSpace = 0;
        int i = 0;
        while(reclaimedSpace < space || i < chunks.size()){
            Chunk chunk = chunks.get(i);
            long deletedSpace = cm.deleteChunk(chunk);
            if(deletedSpace > 0){
                chunk.setChunkAsReclaimed();
                reclaimedSpace += deletedSpace;
            }
            Server.getInstance().getMc().sendRemovedMessage(chunk.getFile().getFileId(),chunk.getChunkNo());
            i++;
        }

        return reclaimedSpace;
    }

    public static void updateNetworkCopiesOfChunk(MessageType messageType, double version, String senderId, String fileId, int chunkNo, String[] args){
        if(version >= 1.0){
            try {
                Chunk chunk = Server.getInstance().getChunckManager().getChunk(fileId,chunkNo);
                if(chunk == null)
                    return;
                if(messageType.equals(REMOVED))
                    chunk.remNetworkCopy(senderId);
                else if(messageType.equals(STORED))
                    chunk.addNetworkCopy(senderId);
            } catch (ChunkException e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
