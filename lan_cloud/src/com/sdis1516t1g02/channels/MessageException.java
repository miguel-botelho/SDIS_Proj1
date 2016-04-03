package com.sdis1516t1g02.channels;

/**
 * Created by Duarte on 21/03/2016.
 */
public class MessageException extends Exception {

    /**
     * The enum for the MessageException.
     */
    public enum ExceptionType{
        UNRECOGNIZED_MESSAGE_TYPE,
        VERSION_INVALID,
        FILEID_INVALID_LENGTH,
        INVALID_NUMBER_FIELDS
    }

    /**
     * The type of the exception.
     */
    protected ExceptionType type;

    /**
     * Creates a new MessageException.
     * @param message the message that explains the exception
     * @param type its type
     */
    public MessageException(String message, ExceptionType type) {
        super(message);
        this.type = type;
    }

    /**
     * Returns the type of the exception.
     * @return type
     */
    public ExceptionType getType() {
        return type;
    }

    /**
     * Returns the message of the exception.
     * @return message
     */
    @Override
    public String getMessage() {
        switch(type){
            case UNRECOGNIZED_MESSAGE_TYPE:
                return "Unrecognized MessageType in message: " + super.getMessage();
            case VERSION_INVALID:
                return "Invalid Version argument in message: " + super.getMessage();
            case FILEID_INVALID_LENGTH:
                return "Argument FileId - invalid length in message: " + super.getMessage();
            case INVALID_NUMBER_FIELDS:
                return "Invalid number of fields in message: " + super.getMessage();
            default:
                return super.getMessage();
        }
    }
}
