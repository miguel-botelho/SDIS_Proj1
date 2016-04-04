package com.sdis1516t1g02.protocols;

import com.sdis1516t1g02.FileManager;
import com.sdis1516t1g02.Server;
import com.sdis1516t1g02.channels.Control;
import com.sdis1516t1g02.channels.DataRestore;
import com.sdis1516t1g02.channels.MessageData;
import com.sdis1516t1g02.chunks.BackupFile;
import com.sdis1516t1g02.chunks.ChunkManager;

import javax.swing.plaf.synth.SynthTextAreaUI;
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

    /**
     * The name of the file.
     */
    String filename;

    /**
     * The id of the file.
     */
    String fileId;

    /**
     * The file.
     */
    File file;

    /**
     * The size of the file.
     */
    long fileSize;

    /**
     * The lock on the file.
     */
    private final Object fileLock = new Object();

    /**
     * The received chunks.
     */
    final ArrayList<Boolean> receivedChunks = new ArrayList<>();

    /**
     * The locks on the chunks.
     */
    final ArrayList<Object> locks = new ArrayList<>();

    /**
     * The lock.
     */
    final Lock lock = new ReentrantLock();

    /**
     * The condition for not being full.
     */
    final Condition notFull  = lock.newCondition();

    /**
     * Creates a new RestoreFile.
     * @param filename the name of the file
     */
    public RestoreFile(String filename){
        this.filename = filename;
        generateFields();
    }

    /**
     * Generates all of the fields of the class.
     */
    public void generateFields(){
        this.fileId = Server.getInstance().getFileManager().getFileId(filename);
        this.file = new File(filename);
        this.fileSize = file.length();
    }

    /**
     * Restores a whole file and requests the chunks that are on the network and adds an observer.
     * @return true if restored, false if it didn't
     */
    public boolean restore(){
        ChunkManager cm = Server.getInstance().getChunckManager();
        FileManager fm = Server.getInstance().getFileManager();
        Control mc = Server.getInstance().getMc();
        String fileId = fm.getFileId(filename);
        BackupFile backupFile = cm.getFiles().get(fileId);
        if(backupFile == null)
            return false;

        int numChunks = backupFile.getChunks().size();
        initLists(numChunks);

        if(!file.exists()){
            if(file.getParentFile() !=null && !file.getParentFile().exists())
                file.getParentFile().mkdirs();
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            Server.getInstance().getMdr().addObserver(this);
            if(Server.getVERSION() >=1.3){
                Server.getInstance().getTcpChannel().addObserver(this);
            }

            for (int i = 0; i < numChunks; i++) {
                System.out.println("Requesting chunkNo: "+i);
                mc.sendGetChunkMessage(fileId,i);
            }


            int i = 0;
            lock.lock();
            try {
                for (i = 0; i < 2*numChunks; i++) {
                    if(!hasReceivedAllChunks())
                        notFull.await(1, TimeUnit.SECONDS);
                    else{
                        break;
                    }
                }

            }finally {
                lock.unlock();
            }
            if(!hasReceivedAllChunks())
                return false;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return true;
    }

    /**
     * Checks if it has received all of the chunks of the file.
     * @return true if it received, false if it didn't
     */
    protected boolean hasReceivedAllChunks(){
        for(Boolean bool : receivedChunks){
            if(bool.equals(Boolean.FALSE))
                return false;
        }
        return true;
    }

    protected void setChunkAsReceived(int chunkNo){
        receivedChunks.set(chunkNo,Boolean.TRUE);
    }

    protected boolean isChunkReceived(int chunkNo){
        return this.receivedChunks.get(chunkNo);
    }

    /**
     * Writes a chunk into the file.
     * @param chunkNo the number of the chunk
     * @param data the data of the chunk
     */
    protected void writeChunk(int chunkNo, byte[] data){
        synchronized (fileLock){
            long position = chunkNo*Server.CHUNK_SIZE;
            try {
                RandomAccessFile randomAccessFile = new RandomAccessFile(file,"rw");
                randomAccessFile.seek(position);
                randomAccessFile.write(data);
                randomAccessFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Initializes all of the ArrayLists.
     * @param numChunks the number of chunks
     */
    private void initLists(int numChunks) {
        receivedChunks.addAll(Collections.nCopies(numChunks,Boolean.FALSE));
        locks.addAll(Collections.nCopies(numChunks,new Object()));
    }

    /**
     *
     * @param o
     * @param arg
     */
    @Override
    public void update(Observable o, Object arg) {
        MessageData messageData = (MessageData) arg;
        MessageType messageType =messageData.getMessageType();
        double version = messageData.getVersion();
        String senderId = messageData.getSenderId();
        String fileId = messageData.getFileId();
        int chunkNo = messageData.getChunkNo();
        byte[] data = messageData.getBody();

        if(version >= 1.3){
            if(data.length > 0){
                handleReceivedChunk(fileId,chunkNo,data);
            }
        }else if(version >= 1.0){
            handleReceivedChunk(fileId, chunkNo, data);
        }
    }

    private void handleReceivedChunk(String fileId, int chunkNo, byte[] data) {
        if(fileId.equals(this.fileId)) {
            synchronized (this.locks.get(chunkNo)){
                if(isChunkReceived(chunkNo))
                    return;
                writeChunk(chunkNo,data);
                setChunkAsReceived(chunkNo);
                lock.lock();
                try{
                    notFull.signal();
                }finally {
                    lock.unlock();
                }
            }
        }else{
            return;
        }
    }
}
