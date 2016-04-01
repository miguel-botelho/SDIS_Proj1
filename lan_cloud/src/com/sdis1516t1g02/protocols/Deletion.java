package com.sdis1516t1g02.protocols;

import com.sdis1516t1g02.Server;
import com.sdis1516t1g02.chunks.BackupFile;
import com.sdis1516t1g02.chunks.Chunk;

import java.util.Set;

/**
 * Created by Duarte on 19/03/2016.
 */
public class Deletion{
    private static long INIT_DELAY = 1000;  /*IN MILLISECONDS*/
    private static long MAX_DELAY = 64000; /*1 HOUR*/

    public static void deleteChunk(MessageType messageType, double version, String senderId, String fileId, String[] args){
        if(version >= 1.3){
            Server.getInstance().getChunckManager().deleteFile(fileId);
            Server.getInstance().getMc().sendConfirmDeleteMessage(fileId);
        }else if(version >= 1.0)
            Server.getInstance().getChunckManager().deleteFile(fileId);
    }

    public static boolean deleteFileByName(String filename){
        String fileId = Server.getInstance().getFileManager().getFileId(filename);
        if(fileId == null)
            return false;
        else
            return deleteFileById(fileId);
    }

    public static void handleDeletedConfirmation(MessageType messageType, double version, String senderId, String fileId, String[] args){
        if(version >= 1.3){
            BackupFile  file = Server.getInstance().getChunckManager().getFiles().get(fileId);
            if(file == null || !file.isBackedUp()){
                return;
            }
            file.removeNetworkCopy(senderId);
        }
    }

    public static boolean deleteFileById(String fileId){
        BackupFile file = Server.getInstance().getChunckManager().getFiles().get(fileId);
        file.setAsDeleted();
        Server.getInstance().getMc().sendDeletedMessage(fileId);
        //Server.getInstance().getChunckManager().getStoredChunks().remove(fileId);
        long delay = INIT_DELAY;
        while (!file.areBackupsDeleted()){
            try {
                Thread.sleep(delay);
                Server.getInstance().getMc().sendDeletedMessage(fileId);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(delay < MAX_DELAY)
                delay *= 2;
        }

        return true;
    }
}
