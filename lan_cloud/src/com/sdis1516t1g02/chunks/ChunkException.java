package com.sdis1516t1g02.chunks;

import com.sdis1516t1g02.channels.MessageException;

/**
 * Created by Duarte on 22/03/2016.
 */
public class ChunkException extends Exception {
    /**
     * Creates a new ChunkException.
     * @param message the message of the exception.
     */
    public ChunkException(String message) {
        super(message);
    }
}
