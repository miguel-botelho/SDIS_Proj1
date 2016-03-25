package com.sdis1516t1g02.chunks;

/**
 * Created by Duarte on 22/03/2016.
 */
public class Chunk implements Comparable<Chunk>{

    public enum State{
        RECLAIMED,STORED,REMOVED, NETWORK, BACKUP
    }

    State state = State.NETWORK;
    int chunkNo;
    String chunkFileName;
    final int replicationDegree;
    int networkCopies = 0;
    BackupFile file;


    public Chunk(BackupFile file, int chunkNo, String filename, int replicationDegree) {
        this.file = file;
        this.chunkNo = chunkNo;
        this.chunkFileName = filename;
        this.replicationDegree = replicationDegree;
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

    public int getNetworkCopies() {
        return networkCopies;
    }

    public void incNetworkCopy(){
        this.networkCopies++;
    }

    public void decrNetworkCopy(){ this.networkCopies--; }

    public void setChunkAsReclaimed(){
        this.setState(State.RECLAIMED);
        this.decrNetworkCopy();
    }

    @Override
    public int compareTo(Chunk o) {
        return (this.networkCopies - this.replicationDegree) - (o.networkCopies-o.replicationDegree);
    }
}
