package com.sdis1516t1g02;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import java.io.File;
/**
 * Created by m_bot on 24/03/2016.
 */
public class LoggerServer {

    Logger logger = Logger.getLogger("MyLog");
    FileHandler fh;

    public LoggerServer(String file) {
        try {
        	File f = new File(file);
            if(!f.exists()) {
                f.getParentFile().mkdirs();
                f.createNewFile();
            }
            
            fh = new FileHandler(file);
            
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);

            // the following statement is used to log any messages
            logger.log(Level.INFO, "Initiating Server.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateLogger(Object obj, Boolean type, String msg) {

        if (type) { //se type == true, entao é sent
            logger.log(Level.INFO, "Channel: " + obj.getClass().getName() + ", Type: Sent, Message: " + msg);
        }
        else logger.log(Level.INFO, "Channel: " + obj.getClass().getName() + ", Type: Received, Message: " + msg);

    }

}
