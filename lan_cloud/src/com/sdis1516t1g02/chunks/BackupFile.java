package com.sdis1516t1g02.chunks;

import java.util.Hashtable;

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

    public Hashtable<Integer, Chunk> getChunks() {
        return chunks;
    }

    public String getFileId() {
        return fileId;
    }
}
