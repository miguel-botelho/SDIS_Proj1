package com.sdis1516t1g02.channels;

import com.sdis1516t1g02.protocols.MessageType;

/**
 * Created by duarte on 30-03-2016.
 */
public class MessageData {
    /**
     * The type of the message (an enum).
     */
    MessageType messageType;

    /**
     * The version of the message.
     */
    double version;

    /**
     * The id of the peer.
     */
    String senderId;

    /**
     * The id of the file.
     */
    String fileId;

    /** The number of the chunk.
     *
     */
    int chunkNo;

    /**
     * The body of the message.
     */
    byte[] body;

    /**
     * Creates a new MessageData.
     * @param messageType the type of the message
     * @param version the version of the message
     * @param senderId the id of the peer
     * @param fileId the id of the file
     * @param chunkNo the number of the chunk
     * @param body the body of the message
     */
    public MessageData(MessageType messageType, double version, String senderId, String fileId, int chunkNo, byte[] body) {
        this.messageType = messageType;
        this.version = version;
        this.senderId = senderId;
        this.fileId = fileId;
        this.chunkNo = chunkNo;
        this.body = body;
    }

    /**
     * Returns the type of the message
     * @return messageType
     */
    public MessageType getMessageType() {
        return messageType;
    }

    /**
     * Sets the type of the message
     * @param messageType
     */
    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    /**
     * Returns the version of the message
     * @return version
     */
    public double getVersion() {
        return version;
    }

    /**
     * Sets the version of the message
     * @param version
     */
    public void setVersion(double version) {
        this.version = version;
    }

    /**
     * Returns the id of the peer.
     * @return senderId
     */
    public String getSenderId() {
        return senderId;
    }

    /**
     * Sets the id of peer.
     * @param senderId
     */
    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    /**
     * Returns the id of the file.
     * @return fileId
     */
    public String getFileId() {
        return fileId;
    }

    /**
     * Sets the id of the file.
     * @param fileId
     */
    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    /**
     * Returns the number of the chunk.
     * @return chunkNo
     */
    public int getChunkNo() {
        return chunkNo;
    }

    /**
     * Sets the number of the chunk.
     * @param chunkNo
     */
    public void setChunkNo(int chunkNo) {
        this.chunkNo = chunkNo;
    }

    /**
     * Returns the body of the message.
     * @return body
     */
    public byte[] getBody() {
        return body;
    }

    /**
     * Sets the body of the message.
     * @param body
     */
    public void setBody(byte[] body) {
        this.body = body;
    }
}
