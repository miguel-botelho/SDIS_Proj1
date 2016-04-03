package com.sdis1516t1g02.chunks;

import com.sdis1516t1g02.Server;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Set;

/**
 * Created by Duarte on 21/03/2016.
 */
public class ChunkManager implements Serializable {

    /**
     * The folder where the chunks are stored.
     */
    public final static String FOLDER_PATH = "chuncks/";

    /**
     * The extension of the chunks.
     */
    public final static String CHUNK_EXTENSION=".chunck";

    /**
     * An hashtable that contains the backup files.
     */
    Hashtable<String,BackupFile> files;

    /**
     * The serializable file.
     */
    private final File file = new File("conf/filesChunk.ser");

    /**
     * Creates a new ChunkManager.
     */
    public ChunkManager(){
        files = new Hashtable<>();
    }

    /**
     * Creates a new name for the chunk.
     * @param fileId the id of the file.
     * @param chunkNo the number of the chunk.
     * @return the name of the chunk
     */
    public static String generateFilename(String fileId, int chunkNo){
        return fileId+"_"+chunkNo;
    }

    /**
     * Adds a chunk to the peer.
     * @param fileId the id of the file
     * @param chunkNo the number of the chunk
     * @param replicationDegree the replication degree of the file
     * @param data the chunk data
     * @param originalServerId the id of the peer that sent the chunk
     * @return true if it added the chunk, false if didn't
     * @throws ChunkException
     */
    public boolean addChunk(String fileId, int chunkNo, int replicationDegree, byte[] data, String originalServerId) throws ChunkException {
        if(!Server.getInstance().hasSpaceForChunk(data.length))
            throw new ChunkException("Not enough space for a new chunk. Available space: "+Server.getByteCount(Server.getInstance().getAvailableSpace(),true));
        BackupFile backupFile = files.get(fileId);
        if(backupFile == null){
            backupFile = new BackupFile(fileId);
            files.put(fileId, backupFile);
        }

        Chunk chunk = getNotStoredChunk(fileId, chunkNo, replicationDegree, backupFile, originalServerId);
        backupFile.getChunksTable().put(chunkNo,chunk);
        return writeChunk(data, chunk);
    }

    /**
     * Returns the chunk data.
     * @param fileId the id of the file
     * @param chunkNo the number of the chunk
     * @return chunk data
     * @throws ChunkException
     */
    public byte[] getChunkData(String fileId, int chunkNo) throws ChunkException {
        BackupFile backupFile = files.get(fileId);
        if(backupFile == null){
            throw new ChunkException("No information about file with fileId="+fileId);
        }
        Chunk chunk = backupFile.chunks.get(chunkNo);
        if(chunk == null || chunk.getState() != Chunk.State.STORED)
            throw new ChunkException("fileId= "+ fileId +" chunkNo="+chunkNo+" not stored");
        return readChunk(chunk);
    }

    /**
     * Deletes a file from the system.
     * @param fileId the id of the file
     * @return deleted space
     */
    public long deleteFile(String fileId){
        BackupFile backupFile = files.get(fileId);
        if(backupFile == null || backupFile.deleted){
            return -1;
        }
        long deletedSpace = 0;
        ArrayList<Chunk> chunks= backupFile.getStoredChunks();
        for(Chunk chunk : chunks){
            long deletedChunkSize = deleteChunk(chunk);
            if(deletedChunkSize>=0) {
                chunk.setState(Chunk.State.DELETED);
                chunk.remNetworkCopy(Server.getInstance().getId());
                deletedSpace += deletedChunkSize;
            }
        }
        backupFile.deleted = true;
        return deletedSpace;
    }

    /**
     * Returns the chunk that isn't stored.
     * @param fileId the id of the file
     * @param chunkNo the number of the chunk
     * @param replicationDegree the replication degree of the file
     * @param backupFile the backup file
     * @param originalServerId the id of the peer that sent the chunk
     * @return the chunk
     * @throws ChunkException
     */
    protected Chunk getNotStoredChunk(String fileId, int chunkNo, int replicationDegree, BackupFile backupFile, String originalServerId) throws ChunkException {
        Chunk chunk;
        chunk = backupFile.chunks.get(new Integer(chunkNo));
        if(chunk != null) {
            synchronized (chunk.state) {
                if (chunk.getState().equals(Chunk.State.STORED))
                    throw new ChunkException("Chunk is already stored");

            }
            chunk.setOriginalServerId(originalServerId);
            chunk.setReplicationDegree(replicationDegree);
        }else{
            chunk = new Chunk(backupFile, chunkNo,generateFilename(fileId,chunkNo), replicationDegree, originalServerId);
        }
        return chunk;
    }

    /**
     * Writes a chunk in the system.
     * @param data the data of the chunk
     * @param chunk the chunk
     * @return true if it wrote, false if it didn't
     * @throws ChunkException
     */
    protected boolean writeChunk(byte[] data, Chunk chunk) throws ChunkException {
        File chunkFile = new File(FOLDER_PATH+chunk.chunkFileName +CHUNK_EXTENSION);
        if(!Server.getInstance().allocateSpace(data.length))
            throw new ChunkException("Not enough space for a new chunk. Available space: "+Server.getByteCount(Server.getInstance().getAvailableSpace(),true));
        try {
            if(chunkFile.getParentFile()!=null&&!chunkFile.getParentFile().exists())
                chunkFile.getParentFile().mkdirs();
            if(chunkFile.exists()){
                throw new ChunkException("Chunk file was already stored but there was no record");
            }
            chunkFile.createNewFile();

            FileOutputStream out = new FileOutputStream(chunkFile);
            try {
                java.nio.channels.FileLock lock = out.getChannel().lock();
                try {
                    out.write(data);
                } finally {
                    lock.release();
                }
            } finally {
                out.close();
                chunk.setState(Chunk.State.STORED);
                chunk.addNetworkCopy(Server.getInstance().getId());
                return true;
            }
        } catch (IOException e) {
            Server.getInstance().freeSpace(data.length);
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Reads a chunk from the system.
     * @param chunk the chunk
     * @return data chunk
     * @throws ChunkException
     */
    protected byte[] readChunk(Chunk chunk) throws ChunkException {
        String path = FOLDER_PATH+chunk.chunkFileName +CHUNK_EXTENSION;
        File chunkFile = new File(path);
        try {
            if(!chunkFile.exists())
                throw new ChunkException("Chunk file doesn't exist! Chunk-"+chunk.chunkNo +" Path-"+path);
            FileInputStream in = new FileInputStream(chunkFile);
            byte tempData[]= new byte[Server.CHUNK_SIZE];
            byte[] data = null;
            try {
                int bytesRead = in.read(tempData,0,Server.CHUNK_SIZE);
                data = new byte[bytesRead];
                System.arraycopy(tempData,0,data,0,bytesRead);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            in.close();
            return data;
        } catch (IOException e) {
            e.printStackTrace();
        }
        throw new ChunkException("Unknown error reading from file!");
    }

    /**
     * Deletes a chunk from the system.
     * @param chunk the chunk
     * @return the size of the chunk
     */
    public long deleteChunk(Chunk chunk) {
        Path path = Paths.get(FOLDER_PATH,chunk.chunkFileName+CHUNK_EXTENSION);
        File chunkFile = new File(FOLDER_PATH+chunk.chunkFileName+CHUNK_EXTENSION);
        long size = chunkFile.length();
        int i;
        for (i = 0; i < 5 ; i++) {
            try{
                try {
                    Files.delete(path);
                    Server.getInstance().freeSpace(size);
                    chunk.setState(Chunk.State.DELETED);
                    break;
                } catch (NoSuchFileException x) {
                    System.err.format("%s: no such" + " file or directory%n", path);
                    throw x;
                } catch (DirectoryNotEmptyException x) {
                    System.err.format("%s not empty%n", path);
                    throw x;
                } catch (IOException x) {
                    // File permission problems are caught here.
                    System.err.println(x);
                    throw x;
                }
            }catch (Exception x){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }
        }
        if (i >= 5)
            return -1;

        return size;
    }

    /**
     * Return the hashtable that contains the backup files
     * @return files
     */
    public Hashtable<String, BackupFile> getFiles() {
        return files;
    }

    /**
     * Returns a chunk from the hashtable, given the id of the file and the number of the chunk.
     * @param fileId the id of the file
     * @param chunkNo the number of the chunk
     * @return chunk
     */
    public Chunk getChunk(String fileId, int chunkNo){
        BackupFile backupFile = files.get(fileId);
        if(backupFile == null){
            return null;
        }
        return backupFile.chunks.get(chunkNo);
    }

    /**
     * Returns all of the chunks that have the state set to STORED.
     * @return the chunks
     */
    public ArrayList<Chunk> getStoredChunks(){
        ArrayList<Chunk> chunksList = new ArrayList<>();
        Set<String> keys = files.keySet();

        for(String key : keys){
            chunksList.addAll(files.get(key).getStoredChunks());
        }
        return chunksList;
    }

    /**
     * Serialize the files hashtable.
     */
    public void serialize() {
        try {
            FileOutputStream fileOut = null;
            ObjectOutputStream out = null;

            if(!file.exists()){
                if(file.getParentFile()!=null && !file.getParentFile().exists())
                    file.getParentFile().mkdirs();
                file.createNewFile();
            }

            fileOut = new FileOutputStream(file);
            out = new ObjectOutputStream(fileOut);
            out.writeObject(files);
            out.close();
            fileOut.close();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Deserialize the files hashtable.
     */
    public void deserialize() {
        try {
            FileInputStream fileIn = new FileInputStream(file);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            files = (Hashtable<String,BackupFile>) in.readObject();
            in.close();
            fileIn.close();
        }catch(FileNotFoundException e){
            return;
        }catch(IOException i) {
            i.printStackTrace();
            return;
        }catch(ClassNotFoundException c) {
            System.out.println("Configuration file for ChunkManager not found");
            c.printStackTrace();
            return;
        }
    }
}
