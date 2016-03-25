package com.sdis1516t1g02.chunks;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Set;

/**
 * Created by Duarte on 22/03/2016.
 */
public class BackupFile {
    String fileId;
    Hashtable<Integer,Chunk> chunks;
    boolean backedUp = false;
    public BackupFile(String fileId) {
        this.fileId = fileId;
        this.chunks = new Hashtable<>();
    }

    public BackupFile(String fileId, boolean backedUp) {
        this.fileId = fileId;
        this.chunks = new Hashtable<>();
    }

    public Hashtable<Integer, Chunk> getChunksTable() {
        return chunks;
    }

    public String getFileId() {
        return fileId;
    }
    public ArrayList<Chunk> getChunks(){
        ArrayList<Chunk> retList = new ArrayList<>();

        Set<Integer> keys = chunks.keySet();
        for(Integer key : keys){
            retList.add(chunks.get(key));
        }

        return retList;
    }

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
}
