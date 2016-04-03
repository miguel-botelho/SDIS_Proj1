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

    /**
     * Deletes a chunk from the system.
     * @param messageType the type of the message
     * @param version the version of the message
     * @param senderId the id of the peer that sent the message
     * @param fileId the id of the file
     * @param args
     */
    public static void deleteChunk(MessageType messageType, double version, String senderId, String fileId, String[] args){
        if(version >= 1.3){
            Server.getInstance().getChunckManager().deleteFile(fileId);
            Server.getInstance().getMc().sendConfirmDeleteMessage(fileId);
        }else if(version >= 1.0)
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

    public static void handleDeletedConfirmation(MessageType messageType, double version, String senderId, String fileId, String[] args){
        if(version >= 1.3){
            BackupFile  file = Server.getInstance().getChunckManager().getFiles().get(fileId);
            if(file == null || !file.isBackedUp()){
                return;
            }
            file.removeNetworkCopy(senderId);
        }
    }

    /**
     * Deletes a file given his id.
     * @param fileId the id of the file
     * @return true
     */
    public static boolean deleteFileById(String fileId){
        BackupFile file = Server.getInstance().getChunckManager().getFiles().get(fileId);
        if(file == null)
            return false;
        file.setAsDeleted();
        Server.getInstance().getMc().sendDeletedMessage(fileId);
        //Server.getInstance().getChunckManager().getStoredChunks().remove(fileId);
        if(Server.getVERSION() >= 1.3) {
            long delay = INIT_DELAY;
            while (!file.areBackupsDeleted()) {
                try {
                    Thread.sleep(delay);
                    Server.getInstance().getMc().sendDeletedMessage(fileId);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (delay < MAX_DELAY)
                    delay *= 2;
            }
        }

        Server.getInstance().saveConfigs();
        return true;
    }
}
