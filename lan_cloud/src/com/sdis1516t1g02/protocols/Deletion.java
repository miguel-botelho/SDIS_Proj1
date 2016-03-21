package com.sdis1516t1g02.protocols;

/**
 * Created by Duarte on 19/03/2016.
 */
public class Deletion implements Runnable{
    String info[];

    public Deletion(String[] info) {
        this.info = info;
    }

    public void deleteChunk(String info[]){
        String versionStr = info[0];
        double version = Double.valueOf(versionStr);
        String senderId = info[1];
        String fileId = info[2];

        //TODO Implementar o pedido de apagar quando o chunk manager estiver terminado
        if(version >= 1.0)
            /*Pedir ao chunck Manager que elimine todos os chunks com fileId*/;
    }

    @Override
    public void run() {
        deleteChunk(this.info);
    }
}
