package com.sdis1516t1g02.chunks;

/**
 * Created by Duarte on 22/03/2016.
 */
public class Chunk {

    enum State{
        RECLAIMED,STORED,REMOVED, NETWORK
    }

    State state = State.NETWORK;
    int chunkNo;
    String filename;
    final int replicationDegree;
    int networkCopies = 0;

    public Chunk(int chunkNo,String filename, int replicationDegree) {
        this.chunkNo = chunkNo;
        this.filename = filename;
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

    public String getFilename() {
        return filename;
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
}
