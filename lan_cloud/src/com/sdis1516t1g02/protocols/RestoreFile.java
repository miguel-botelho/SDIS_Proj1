package com.sdis1516t1g02.protocols;

import com.sdis1516t1g02.FileManager;
import com.sdis1516t1g02.Server;
import com.sdis1516t1g02.channels.Control;
import com.sdis1516t1g02.chunks.BackupFile;
import com.sdis1516t1g02.chunks.ChunkManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Duarte on 28/03/2016.
 */
public class RestoreFile implements Observer{
    String filename;
    String fileId;
    File file;
    long fileSize;
    RandomAccessFile randomAccessFile;
    private final Object fileLock = new Object();
    ArrayList<Boolean> receivedChunks;
    ArrayList<Object> locks;

    final Lock lock = new ReentrantLock();
    final Condition notFull  = lock.newCondition();

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
        Control mc = Server.getInstance().getMc();

        BackupFile backupFile = cm.getFiles().get(filename);
        if(backupFile == null)
            return false;

        int numChunks = backupFile.getChunks().size();
        initLists(numChunks);

        try {
            randomAccessFile = new RandomAccessFile(file,"rw");
            Server.getInstance().getMdb().addObserver(this);

            for (int i = 0; i < numChunks; i++) {
                mc.sendGetChunkMessage(fileId,i);
            }
            lock.lock();
            try {
                for (int i = 0; i < 2*numChunks; i++) {
                    if(!hasReceivedAllChunks())
                        notFull.await(1, TimeUnit.SECONDS);
                }

            }finally {
                lock.unlock();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return true;
    }

    protected synchronized boolean hasReceivedAllChunks(){
        for(Boolean bool : receivedChunks){
            if(bool.equals(Boolean.FALSE))
                return false;
        }
        return true;
    }

    protected void writeChunk(int chunkNo, byte[] data){
        synchronized (fileLock){
            long position = chunkNo*Server.CHUNK_SIZE;
            try {
                randomAccessFile.seek(position);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void initLists(int numChunks) {
        receivedChunks = new ArrayList<>(numChunks);
        Collections.fill(receivedChunks,Boolean.FALSE);
        locks = new ArrayList<>(numChunks);
        Collections.fill(locks, new Object());
    }

    @Override
    public void update(Observable o, Object arg) {
        String[] messageInfo = (String[]) arg;
        MessageType messageType =MessageType.valueOf(messageInfo[0]);
        double version = Double.valueOf(messageInfo[1]);
        String senderId = messageInfo[2];
        String fileId = messageInfo[3];
        int chunkNo = Integer.valueOf(messageInfo[4]);


        if(version >= 1.0){
            if(fileId.equals(this.fileId)) {
                synchronized (this.locks.get(chunkNo)){
                    if(this.receivedChunks.get(chunkNo))
                        return;
                    byte[] data = messageInfo[5].getBytes();
                    writeChunk(chunkNo,data);
                    this.receivedChunks.set(chunkNo,Boolean.TRUE);
                }

                lock.lock();
                try{
                    notFull.signal();
                }finally {
                    lock.unlock();
                }
            }else{
                return;
            }
        }
    }
}
