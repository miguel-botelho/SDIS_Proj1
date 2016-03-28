package com.sdis1516t1g02.chunks;

import com.sdis1516t1g02.Server;

import java.io.Serializable;
import java.util.ArrayList;

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
    final int replicationDegree;
    ArrayList<String> networkCopies = new ArrayList<>();
    BackupFile file;


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
        synchronized (this.state){
            this.state = state;
        }
    }

    public String getChunkFileName() {
        return chunkFileName;
    }

    public int getReplicationDegree() {
        return replicationDegree;
    }

    public ArrayList<String> getNetworkCopies() {
        return networkCopies;
    }

    public void addNetworkCopy(String serverId){
        synchronized (networkCopies){
            if(!this.networkCopies.contains(serverId))
                this.networkCopies.add(serverId);
        }
    }

    public void remNetworkCopy(String serverId){
        synchronized (networkCopies){
            if(this.networkCopies.contains(serverId))
                this.networkCopies.remove(serverId);
        }
    }

    public int getNumNetworkCopies(){
        synchronized (networkCopies){
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
        synchronized (state){
            return state.equals(State.STORED);
        }
    }

    @Override
    public int compareTo(Chunk o) {
        return (this.networkCopies.size() - this.replicationDegree) - (o.networkCopies.size()-o.replicationDegree);
    }
}
