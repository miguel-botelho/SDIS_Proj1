package com.sdis1516t1g02.protocols;

import com.sdis1516t1g02.Server;
import com.sdis1516t1g02.chunks.BackupFile;
import com.sdis1516t1g02.chunks.Chunk;

import java.util.Set;

/**
 * Created by Duarte on 19/03/2016.
 */
public class Deletion{

    public static void deleteChunk(MessageType messageType, double version, String senderId, String fileId, String[] args){
        if(version >= 1.0)
            Server.getInstance().getChunckManager().deleteFile(fileId);
    }

    public static boolean deleteFileByName(String filename){
        String fileId = Server.getInstance().getFileManager().getFileId(filename);
        if(fileId == null)
            return false;
        else
            return deleteFileById(fileId);
    }


    public static boolean deleteFileById(String fileId){
        BackupFile file = Server.getInstance().getChunckManager().getFiles().get(fileId);
        file.setAsDeleted();
        Server.getInstance().getMc().sendDeletedMessage(fileId);
        //Server.getInstance().getChunckManager().getStoredChunks().remove(fileId);
        return true;
    }
}
