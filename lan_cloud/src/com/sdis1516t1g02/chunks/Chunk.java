package com.sdis1516t1g02.chunks;

import com.sdis1516t1g02.Server;

import java.io.Serializable;
import java.util.HashSet;

/**
 * Created by Duarte on 22/03/2016.
 */
public class Chunk implements Comparable<Chunk>, Serializable {

    /**
     * The enum for the state of the chunk.
     */
    public enum State{
        RECLAIMED,STORED, DELETED, NETWORK, BACKUP
    }

    /**
     * The state of the chunk.
     */
    State state = State.NETWORK;

    /**
     * The HashSet to keep track of the copies in the network.
     */
    HashSet<String> networkCopies = new HashSet();

    /**
     * A lock for the network copies.
     */
    final Integer networkCopiesLock = new Integer(0);

    /**
     * A lock for the state.
     */
    final Integer stateLock = new Integer(0);

    /**
     * The backup file.
     */
    BackupFile file;

    /**
     * The original peer that sent the chunk.
     */
    String originalServerId;

    /**
     * The number of the chunk.
     */
    int chunkNo;

    /**
     * The name of the chunk.
     */
    String chunkFileName;

    /**
     * The replication degree of the chunk.
     */
    int replicationDegree;

    /**
     * Creates a new chunk.
     * @param file the file that originated the chunk
     * @param chunkNo the number of the chunk
     * @param filename the name of the chunk
     * @param replicationDegree the replication degree of the chunk
     * @param originalServerId the id of the peer that sent the chunk
     */
    public Chunk(BackupFile file, int chunkNo, String filename, int replicationDegree, String originalServerId) {
        this.file = file;
        this.chunkNo = chunkNo;
        this.chunkFileName = filename;
        this.replicationDegree = replicationDegree;
        this.originalServerId = originalServerId;
    }

    /**
     * Creates a new Chunk.
     * @param file the file that originated the chunk.
     * @param chunkNo the number of the chunk.
     * @param filename the name of the chunk.
     */
    //TODO isto não está deprecated também?
    public Chunk(BackupFile file, int chunkNo, String filename) {
        this.file = file;
        this.chunkNo = chunkNo;
        this.chunkFileName = filename;
        this.replicationDegree = replicationDegree;
    }

    /**
     * Creates a new Chunk.
     * @param file the file that originated the chunk.
     * @param chunkNo the number of the chunk.
     * @param replicationDegree the replication degree of the chunk.
     */
    //TODO isto não está deprecated também?
    public Chunk(BackupFile file,int chunkNo,int replicationDegree){
        this.file = file;
        this.chunkNo = chunkNo;
        this.replicationDegree = replicationDegree;
        this.chunkFileName = null;
    }

    /**
     * Returns the number of the chunk.
     * @return chunkNo
     */
    public int getChunkNo() {
        return chunkNo;
    }

    /**
     * Returns the state of the chunk.
     * @return state
     */
    public State getState() {
        return state;
    }

    /**
     * Sets the state of the chunk.
     * @param state
     */
    public void setState(State state) {
        synchronized (stateLock){
            this.state = state;
        }
    }

    /**
     * Returns the name of the chunk.
     * @return chunkFileName
     */
    public String getChunkFileName() {
        return chunkFileName;
    }

    /**
     * Returns the replication degree of the chunk.
     * @return replicationDegree
     */
    public int getReplicationDegree() {
        return replicationDegree;
    }

    /**
     * Sets the replication degree of the chunk.
     * @param replicationDegree
     */
    public void setReplicationDegree(int replicationDegree) {
        this.replicationDegree = replicationDegree;
    }

    /**
     * Returns the copies of the network.
     * @return networkCopies
     */
    public HashSet<String> getNetworkCopies() {
        return networkCopies;
    }

    /**
     * Adds a network copy to the hashset.
     * @param serverId
     */
    public void addNetworkCopy(String serverId){
        synchronized (networkCopiesLock){
            this.networkCopies.add(serverId);
        }
    }

    /**
     * Removes a network copy given the id of the peer.
     * @param serverId the id of the peer
     */
    public void remNetworkCopy(String serverId){
        synchronized (networkCopiesLock){
            if(this.networkCopies.contains(serverId))
                this.networkCopies.remove(serverId);
        }
    }

    /**
     * Returns the number of network copies.
     * @return size of network copies
     */
    public int getNumNetworkCopies(){
        synchronized (networkCopiesLock){
            return this.networkCopies.size();
        }
    }

    /**
     * Sets Chunk as reclaimed.
     */
    public void setChunkAsReclaimed(){
        this.setState(State.RECLAIMED);
        this.remNetworkCopy(Server.getInstance().getId());
    }

    /**
     * Returns the BackupFile.
     * @return file
     */
    public BackupFile getFile() {
        return file;
    }

    /**
     * Checks if the chunk is stored.
     * @return true if it is stored, false if it isnt'
     */
    public boolean isStored(){
        synchronized (stateLock){
            return state.equals(State.STORED);
        }
    }

    /**
     * Compares two chunks through their replication degree and the number of network copies.
     * @param o the chunk
     * @return > 0 if bigger, < 0 if smaller
     */
    @Override
    public int compareTo(Chunk o) {
        if((this.networkCopies.size() - this.replicationDegree) - (o.networkCopies.size()-o.replicationDegree)==0){
            return o.chunkNo - this.chunkNo;
        }
        return (this.networkCopies.size() - this.replicationDegree) - (o.networkCopies.size()-o.replicationDegree);    }

    /**
     * Returns the id of the peer that stored the chunk.
     * @return originalServerId
     */
    public String getOriginalServerId() {
        return originalServerId;
    }

    /**
     * Sets the id of the peer that stored the chunk.
     * @param originalServerId
     */
    public void setOriginalServerId(String originalServerId) {
        this.originalServerId = originalServerId;
    }

    /**
     * Checks if the chunk is reclaimed
     * @return true if it is, false if it isnt'
     */
    public boolean isReclaimed(){
        synchronized (stateLock){
            return state.equals(State.RECLAIMED);
        }
    }

    /**
     * Checks if the chunk needs to be resent.
     * @return true if the number of network copies are less than the replication degree
     */
    public boolean needsResend(){
        return this.getNumNetworkCopies()-this.replicationDegree<0;
    }
}
