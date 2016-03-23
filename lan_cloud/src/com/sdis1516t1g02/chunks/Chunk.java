package com.sdis1516t1g02.chunks;

/**
 * Created by Duarte on 22/03/2016.
 */
public class Chunk {
    enum State{
        DELETED,STORED,REMOVED,UNKNOWN
    }

    State state = State.STORED;
    int chunkNo;
    String filename;

    public Chunk(int chunkNo,String filename) {
        this.chunkNo = chunkNo;
        this.filename = filename;
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
}
