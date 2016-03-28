package com.sdis1516t1g02.protocols;

import com.sdis1516t1g02.FileManager;
import com.sdis1516t1g02.Server;
import com.sdis1516t1g02.chunks.Chunk;
import com.sdis1516t1g02.chunks.ChunkManager;

import java.io.File;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by Duarte on 28/03/2016.
 */
public class RestoreFile implements Observer{
    String filename;
    String fileId;
    File file;
    long fileSize;

    public RestoreFile(String filename){
        this.filename = filename;
        generateFields();
    }

    public void generateFields(){
        this.fileId = Server.getInstance().getFileManager().getFileId(filename);
        this.file = new File(filename);
        this.fileSize = file.length();
    }

    public boolean restore(){
        ChunkManager cm = Server.getInstance().getChunckManager();
        FileManager fm = Server.getInstance().getFileManager();
        if(!file.exists())
            return false;

        return true;
    }

    @Override
    public void update(Observable o, Object arg) {

    }
}
