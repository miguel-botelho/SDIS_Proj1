package com.sdis1516t1g02;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Hashtable;

/**
 * Created by Duarte on 27/03/2016.
 */
public class FileManager {
    Hashtable<String, String> files = new Hashtable<>();


    public String addFile(String filename, String fileid){
        String previousFileId = files.get(filename);
        files.put(filename, fileid);
        return previousFileId;
    }

    public String getFileId(String filename){
        return files.get(filename);
    }

    public static int getNumberChunks(File file){
        long length = file.length();
        return (int) Math.ceil((double)length/Server.CHUNK_SIZE);
    }

    public static String generateFileId(String filename) {
        try {
            File file = new File(filename);

            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
            sdf.format(file.lastModified());
            Path path = Paths.get(file.getAbsolutePath());
            FileOwnerAttributeView attr = Files.getFileAttributeView(path, FileOwnerAttributeView.class);

            String name = file.getName();
            String date = sdf.toString();
            String owner = attr.getOwner().getName();
            long size = file.length();

            String metadata = name + date + owner + size;

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] digestedMessage = digest.digest(metadata.getBytes(StandardCharsets.UTF_8));
            String fileId = new String(digestedMessage);
            return fileId;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }
}
