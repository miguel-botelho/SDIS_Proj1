package com.sdis1516t1g02.protocols;

import com.sdis1516t1g02.Server;
import com.sdis1516t1g02.chunks.BackupFile;
import com.sdis1516t1g02.chunks.Chunk;

import java.util.Set;

/**
 * Created by Duarte on 19/03/2016.
 */
public class Deletion{

    /**
     * Deletes a chunk from the system.
     * @param messageType the type of the message
     * @param version the version of the message
     * @param senderId the id of the peer that sent the message
     * @param fileId the id of the file
     * @param args
     */
    public static void deleteChunk(MessageType messageType, double version, String senderId, String fileId, String[] args){
        if(version >= 1.0)
            Server.getInstance().getChunckManager().deleteFile(fileId);
    }

    /**
     * Deletes a file given his name.
     * @param filename the name of the file
     * @return deleteFileById
     */
    public static boolean deleteFileByName(String filename){
        String fileId = Server.getInstance().getFileManager().getFileId(filename);
        if(fileId == null)
            return false;
        else
            return deleteFileById(fileId);
    }

    /**
     * Deletes a file given his id.
     * @param fileId the id of the file
     * @return true
     */
    public static boolean deleteFileById(String fileId){
        BackupFile file = Server.getInstance().getChunckManager().getFiles().get(fileId);
        file.setAsDeleted();
        Server.getInstance().getMc().sendDeletedMessage(fileId);
        //Server.getInstance().getChunckManager().getStoredChunks().remove(fileId);
        return true;
    }
}
