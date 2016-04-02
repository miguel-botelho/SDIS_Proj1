package com.sdis1516t1g02.protocols;

/**
 * Created by Duarte on 19/03/2016.
 */
public class Restore {

    /**
     * Sends a chunk that was requested.
     * @param messageType the type of the message
     * @param version the version of the message
     * @param serverId the id of the peer that sent the message
     * @param fileId the id of the file
     * @param chunkNo the number of the chunk
     * @param args
     */
    public static void sendRequestedChunk(MessageType messageType, double version, String serverId, String fileId, int chunkNo, String[] args){
        RestoreChunk attend = new RestoreChunk(messageType,version,serverId,fileId,chunkNo, args);
        attend.sendRequestedChunk();
    }

    /**
     * Restores a whole file.
     * @param filename the name of the file
     * @return restore
     */
    public static boolean restoreFile(String filename){
        RestoreFile restoreFile = new RestoreFile(filename);
        return restoreFile.restore();
    }


}
