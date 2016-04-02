package com.sdis1516t1g02.protocols;

import com.sdis1516t1g02.FileManager;
import com.sdis1516t1g02.Server;
import com.sdis1516t1g02.channels.Channel;
import com.sdis1516t1g02.chunks.BackupFile;
import com.sdis1516t1g02.chunks.Chunk;
import com.sdis1516t1g02.chunks.ChunkException;
import com.sdis1516t1g02.chunks.ChunkManager;

import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Random;

/**
 * Created by Duarte on 19/03/2016.
 */
public class Backup{
    public static final int RESPONSE_MAX_DELAY = 400;       /*IN MILLISECONDS*/
    public static final int TIME_BETWEEN_SEND_CHUNK = 1000; /*IN MILLISECONDS*/
    public static final int TRIES = 5;

    public static boolean createAndSendChunk(BackupFile file, int chunkNo, int replicationDegree, byte[] data){
        Chunk chunk = new Chunk(file, chunkNo,replicationDegree);
        chunk.setState(Chunk.State.BACKUP);
        file.getChunksTable().put(chunkNo,chunk);
        String serverId = Server.getInstance().getId();
        return backupChunk(serverId, chunk, data);
    }

    private static boolean backupChunk(String serverId, Chunk chunk, byte[] data) {
        int timeInterval = TIME_BETWEEN_SEND_CHUNK;
        int chunkNo = chunk.getChunkNo();
        String fileId = chunk.getFile().getFileId();
        int replicationDegree = chunk.getReplicationDegree();
        int networkCopies = chunk.getNumNetworkCopies();
        int i = 0;
        while(networkCopies < replicationDegree && i < TRIES) {
            Server.getInstance().getMdb().sendBackupMessage(serverId,fileId, chunkNo, replicationDegree, data);

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


    public static boolean backupFile(String filename,int replicationDegree) throws FileNotFoundException {
        ChunkManager cm = Server.getInstance().getChunckManager();
        FileManager fm = Server.getInstance().getFileManager();

        String fileId = fm.generateFileId(filename);
        if(!Channel.isValidFileId(fileId)){
            System.out.println("Invalid FileId. Length:"+fileId.length());
            return false;
        }
        File file = new File(filename);

        if(!file.exists()){
            throw new FileNotFoundException();
        }
        String previousFileId = (fm.addFile(filename,fileId, file));

        if(previousFileId != null)
            Deletion.deleteFileById(fileId);

        BackupFile backupFile = new BackupFile(fileId,true);
        cm.getFiles().put(fileId,backupFile);


        return sendBackupFile(replicationDegree, backupFile, file);
    }

    protected static boolean sendBackupFile(int replicationDegree, BackupFile backupFile, File file) throws FileNotFoundException {
        int numChunks = FileManager.getNumberChunks(file);
        long fileSize = file.length();

        System.out.println("File size:" + Server.getByteCount(fileSize,true)+". numChunks: "+numChunks);

        try {
            FileInputStream in = new FileInputStream(file);
            try {
                for (int i = 0; i < numChunks; i++) {
                    if (fileSize == 0)
                        break;
                    byte[] tempData = new byte[Server.CHUNK_SIZE];
                    int bytesRead = in.read(tempData,0, Server.CHUNK_SIZE);
                    byte[] data = new byte[bytesRead];
                    System.arraycopy(tempData,0,data,0,bytesRead);
                    if(!createAndSendChunk(backupFile,i,replicationDegree,data))
                        return false;
                }
            } finally {
                in.close();
            }
        }catch (IOException e){
            if(e.getClass().equals(FileNotFoundException.class))
                throw (FileNotFoundException) e;
            e.printStackTrace();
        }
        return true;
    }

    public static boolean reSendChunk(Chunk chunk){
        try {
            byte[] data = Server.getInstance().getChunckManager().getChunkData(chunk.getFile().getFileId(), chunk.getChunkNo());
            return backupChunk(chunk.getOriginalServerId(),chunk,data);

        } catch (ChunkException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void receiveChunk(MessageType messageType, double version, String senderId, String fileId, int chunkNo, int replicationDegree, String[] args, byte[] data){
        ChunkManager cm = Server.getInstance().getChunckManager();

        Chunk chunk = cm.getChunk(fileId,chunkNo);
        if(version >=1.2){
            if (chunk != null) {
                if (chunk.isStored()) {
                    Server.getInstance().getMc().sendStoredMessage(fileId, chunkNo);
                    return;
                }
                if (chunk.isReclaimed())
                    return;
                chunk.setReplicationDegree(replicationDegree);
                chunk.setOriginalServerId(senderId);
                if(chunk.getNumNetworkCopies()- replicationDegree>=0)
                    return;
            }
            try {
                int delay = new Random().nextInt(RESPONSE_MAX_DELAY + 1);
                Thread.sleep(delay);
                Server.getInstance().getChunckManager().addChunk(fileId, chunkNo, replicationDegree, data, senderId);
                Server.getInstance().getMc().sendStoredMessage(fileId, chunkNo);
            } catch (ChunkException e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        } else if(version >=1.0) {
            if (chunk != null) {
                if (chunk.isStored()) {
                    Server.getInstance().getMc().sendStoredMessage(fileId, chunkNo);
                    return;
                }
                if (chunk.isReclaimed())
                    return;
                chunk.setReplicationDegree(replicationDegree);
                chunk.setOriginalServerId(senderId);
            }
            try {
                Server.getInstance().getChunckManager().addChunk(fileId, chunkNo, replicationDegree, data, senderId);
                int delay = new Random().nextInt(RESPONSE_MAX_DELAY + 1);
                Thread.sleep(delay);
                Server.getInstance().getMc().sendStoredMessage(fileId, chunkNo);
            } catch (ChunkException e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
