package com.sdis1516t1g02.protocols;

import com.sdis1516t1g02.FileManager;
import com.sdis1516t1g02.Server;
import com.sdis1516t1g02.channels.Channel;
import com.sdis1516t1g02.chunks.BackupFile;
import com.sdis1516t1g02.chunks.Chunk;
import com.sdis1516t1g02.chunks.ChunkException;
import com.sdis1516t1g02.chunks.ChunkManager;

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
        int timeInterval = TIME_BETWEEN_SEND_CHUNK;
        int networkCopies = chunk.getNumNetworkCopies();
        int i = 0;
        while(networkCopies < replicationDegree && i < TRIES) {
            Server.getInstance().getMdb().sendBackupMessage(file.getFileId(), chunkNo, replicationDegree, data);

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
                java.nio.channels.FileLock lock = null;
                    Reader reader = new InputStreamReader(in);
                    for (int i = 0; i < numChunks; i++) {
                        if (fileSize == 0)
                            break;
                        char cbuf[] = new char[Server.CHUNK_SIZE/2];
                        int bytesRead = reader.read(cbuf);
                        String dataStr = new String(cbuf,0,bytesRead);
                        byte[] data = dataStr.getBytes(Server.CHARSET);
                        System.out.println("Going to send chunk: "+i+". Body length:"+data.length);
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

    public static void receiveChunk(MessageType messageType, double version, String senderId, String fileId, int chunkNo, int replicationDegree, String[] args, byte[] data){
        ChunkManager cm = Server.getInstance().getChunckManager();

        Chunk chunk = cm.getChunk(fileId,chunkNo);
        if(chunk != null){
            if(chunk.isStored()){
                Server.getInstance().getMc().sendStoredMessage(fileId,chunkNo);
                return;
            }
            if(chunk.isReclaimed())
                return;
        }
        try {
            Server.getInstance().getChunckManager().addChunk(fileId,chunkNo,replicationDegree,data, senderId);
            int delay = new Random().nextInt(RESPONSE_MAX_DELAY+1);
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
