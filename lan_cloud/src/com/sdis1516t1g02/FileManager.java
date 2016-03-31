package com.sdis1516t1g02;

import java.io.*;
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
public class FileManager implements Serializable {
    Hashtable<String, String> files = new Hashtable<>();
    private final File confFile = new File("/conf/filesFile.ser");

    public void serialize() {
        try {
            FileOutputStream fileOut = null;
            ObjectOutputStream out = null;


            if(!confFile.exists()){
                if(confFile.getParentFile()!=null && !confFile.getParentFile().exists())
                    confFile.getParentFile().mkdirs();
                confFile.createNewFile();
            }
            fileOut = new FileOutputStream(confFile);
            out = new ObjectOutputStream(fileOut);
            out.writeObject(files);
            out.close();
            fileOut.close();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deserialize() {
        try {
            FileInputStream fileIn = new FileInputStream(confFile);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            files = (Hashtable<String,String>) in.readObject();
            in.close();
            fileIn.close();
        }catch(IOException i) {
            i.printStackTrace();
            return;
        }catch(ClassNotFoundException c) {
            System.out.println("Configuration file for FileManager not found");
            c.printStackTrace();
            return;
        }
    }

    public Hashtable<String, String> getFiles() {
        return files;
    }

    public String addFile(String filename, String fileid, File file){
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

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
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
            String fileId = bytesToHex(digestedMessage);
            System.out.println("FileId: "+fileId);
            return fileId;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

}
