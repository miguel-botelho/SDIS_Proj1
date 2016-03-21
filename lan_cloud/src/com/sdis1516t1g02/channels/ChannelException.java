package com.sdis1516t1g02.channels;

/**
 * Created by Duarte on 20/03/2016.
 */
public class ChannelException extends Exception {

    public ChannelException(String message) {
        super(message);
    }

    public ChannelException(Throwable cause) {
        super(cause);
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }
}
