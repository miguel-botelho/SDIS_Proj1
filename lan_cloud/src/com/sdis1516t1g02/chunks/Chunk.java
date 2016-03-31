package com.sdis1516t1g02.chunks;

import com.sdis1516t1g02.Server;

import java.io.Serializable;
import java.util.HashSet;

/**
 * Created by Duarte on 22/03/2016.
 */
public class Chunk implements Comparable<Chunk>, Serializable {


    public enum State{
        RECLAIMED,STORED, DELETED, NETWORK, BACKUP
    }

    State state = State.NETWORK;
    int chunkNo;
    String chunkFileName;
    int replicationDegree;
    HashSet<String> networkCopies = new HashSet();
    Object networkCopiesLock = new Object();
    Object stateLock = new Object();
    BackupFile file;
    String originalServerId;


    public Chunk(BackupFile file, int chunkNo, String filename, int replicationDegree, String originalServerId) {
        this.file = file;
        this.chunkNo = chunkNo;
        this.chunkFileName = filename;
        this.replicationDegree = replicationDegree;
        this.originalServerId = originalServerId;
    }

    public Chunk(BackupFile file, int chunkNo, String filename, int replicationDegree) {
        this.file = file;
        this.chunkNo = chunkNo;
        this.chunkFileName = filename;
        this.replicationDegree = replicationDegree;
    }

    public Chunk(BackupFile file,int chunkNo,int replicationDegree){
        this.file = file;
        this.chunkNo = chunkNo;
        this.replicationDegree = replicationDegree;
        this.chunkFileName = null;
    }

    public int getChunkNo() {
        return chunkNo;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        synchronized (stateLock){
            this.state = state;
        }
    }

    public String getChunkFileName() {
        return chunkFileName;
    }

    public int getReplicationDegree() {
        return replicationDegree;
    }

    public void setReplicationDegree(int replicationDegree) {
        this.replicationDegree = replicationDegree;
    }

    public HashSet<String> getNetworkCopies() {
        return networkCopies;
    }

    public void addNetworkCopy(String serverId){
        synchronized (networkCopiesLock){
            this.networkCopies.add(serverId);
        }
    }

    public void remNetworkCopy(String serverId){
        synchronized (networkCopiesLock){
            if(this.networkCopies.contains(serverId))
                this.networkCopies.remove(serverId);
        }
    }

    public int getNumNetworkCopies(){
        synchronized (networkCopiesLock){
            return this.networkCopies.size();
        }
    }

    public void setChunkAsReclaimed(){
        this.setState(State.RECLAIMED);
        this.remNetworkCopy(Server.getInstance().getId());
    }

    public BackupFile getFile() {
        return file;
    }

    public boolean isStored(){
        synchronized (stateLock){
            return state.equals(State.STORED);
        }
    }

    @Override
    public int compareTo(Chunk o) {
        return (this.networkCopies.size() - this.replicationDegree) - (o.networkCopies.size()-o.replicationDegree);
    }

    public String getOriginalServerId() {
        return originalServerId;
    }

    public void setOriginalServerId(String originalServerId) {
        this.originalServerId = originalServerId;
    }

    public boolean isReclaimed(){
        synchronized (stateLock){
            return state.equals(State.RECLAIMED);
        }
    }
}
