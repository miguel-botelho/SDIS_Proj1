package com.sdis1516t1g02.protocols;

import com.sdis1516t1g02.Server;

/**
 * Created by Duarte on 19/03/2016.
 */
public class Deletion{

    public static void deleteChunk(MessageType messageType, double version, String senderId, String fileId, String[] args){
        if(version >= 1.0)
            Server.getInstance().getChunckManager().deleteFile(fileId);
    }
}
