package com.sdis1516t1g02.chunks;

import java.util.Hashtable;

/**
 * Created by Duarte on 22/03/2016.
 */
public class BackupFile {
    String fileId;
    Hashtable<Integer,Chunk> chunks;

    public BackupFile(String fileId) {
        this.fileId = fileId;
        this.chunks = new Hashtable<>();
    }

}
