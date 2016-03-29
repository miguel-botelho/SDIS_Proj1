package com.sdis1516t1g02.protocols;

/**
 * Created by Duarte on 19/03/2016.
 */
public class Restore {

    public static void sendRequestedChunk(MessageType messageType, double version, String serverId, String fileId, int chunkNo, String[] args){
        RestoreChunk attend = new RestoreChunk(messageType,version,serverId,fileId,chunkNo, args);
        attend.sendRequestedChunk();
    }


    public static boolean restoreFile(String filename){
        RestoreFile restoreFile = new RestoreFile(filename);
        return restoreFile.restore();
    }


}
