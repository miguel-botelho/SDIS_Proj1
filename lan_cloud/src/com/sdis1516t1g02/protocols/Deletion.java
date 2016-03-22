package com.sdis1516t1g02.protocols;

/**
 * Created by Duarte on 19/03/2016.
 */
public class Deletion{

    String senderId;
    double version;
    String fileId;
    MessageType action;
    String args[];

    public Deletion(MessageType action, String versionStr, String senderId, String fileId, String[] args) {
        this.args = args;
        this.senderId = senderId;
        this.version = Double.valueOf(versionStr);
        this.fileId = fileId;
        this.action = action;
    }

    public void deleteChunk(){
        //TODO Implementar o pedido de apagar quando o chunk manager estiver terminado
        if(version >= 1.0)
            /*Pedir ao chunck Manager que elimine todos os chunks com fileId*/;
    }
}
