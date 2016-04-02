package com.sdis1516t1g02.channels;

/**
 * Created by Duarte on 20/03/2016.
 */
public class ChannelException extends Exception {

    /**
     * Creates a new Channel Exception, based on a string.
     * @param message
     */
    public ChannelException(String message) {
        super(message);
    }

    /**
     * Creates a new Channel Exception, based on the cause (a Throwable).
     * @param cause
     */
    public ChannelException(Throwable cause) {
        super(cause);
    }

    /**
     * Returns the message of the exception.
     * @return message
     */
    @Override
    public String getMessage() {
        return super.getMessage();
    }
}
