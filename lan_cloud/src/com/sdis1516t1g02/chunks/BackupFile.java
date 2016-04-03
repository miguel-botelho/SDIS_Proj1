package com.sdis1516t1g02.chunks;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Set;

/**
 * Created by Duarte on 22/03/2016.
 */
public class BackupFile implements Serializable {

    /**
     * The id of the file.
     */
    String fileId;

    /**
     * An hashtable to store all of the chunks.
     */
    Hashtable<Integer,Chunk> chunks;

    /**
     * A boolean to know if the file was backed up.
     */
    boolean backedUp = false;

    /**
     * Creates a new BackupFile
     * @param fileId the id of the file
     */
    //TODO este construtor não está deprecated?
    public BackupFile(String fileId) {
        this.fileId = fileId;
        this.chunks = new Hashtable<>();
    }

    /**
     * Creates a new BackupFile
     * @param fileId the id of the file
     * @param backedUp a boolean to know if the file has been backed up
     */
    public BackupFile(String fileId, boolean backedUp) {
        this.fileId = fileId;
        this.chunks = new Hashtable<>();
    }

    /**
     * Returns the hashtable containing the chunks.
     * @return chunks
     */
    public Hashtable<Integer, Chunk> getChunksTable() {
        return chunks;
    }

    /**
     * Returns the id of the file.
     * @return fileId
     */
    public String getFileId() {
        return fileId;
    }

    /**
     * Returns the chunks in the form of an arrayList.
     * @return chunks
     */
    public ArrayList<Chunk> getChunks(){
        ArrayList<Chunk> retList = new ArrayList<>();

        Set<Integer> keys = chunks.keySet();
        for(Integer key : keys){
            retList.add(chunks.get(key));
        }

        return retList;
    }

    /**
     * Returns all of the chunks that were stored in the form of an arrayList.
     * @return chunks
     */
    public ArrayList<Chunk> getStoredChunks(){
        ArrayList<Chunk> retList = new ArrayList<>();

        Set<Integer> keys = chunks.keySet();
        for(Integer key : keys){
            Chunk chunk = chunks.get(key);
            if(chunk.getState().equals(Chunk.State.STORED))
                retList.add(chunk);
        }

        return retList;
    }

    /**
     * Sets the chunks as deleted.
     */
    public void setAsDeleted(){
        Set<Integer> keys = chunks.keySet();
        for(Integer key : keys){
            Chunk chunk = chunks.get(key);
            chunk.setState(Chunk.State.DELETED);
        }

    }
}
