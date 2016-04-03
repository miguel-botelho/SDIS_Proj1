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

    /**
     * An hashtable with the id of the files.
     */
    Hashtable<String, String> files = new Hashtable<>();

    /**
     * The file where the serializable is stored.
     */
    private final File confFile = new File("/conf/filesFile.ser");

    /**
     * Serializes the hashtable files.
     */
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

    /**
     * Deserializes the file and loads it into the hashtable.
     */
    public void deserialize() {
        try {
            FileInputStream fileIn = new FileInputStream(confFile);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            files = (Hashtable<String,String>) in.readObject();
            in.close();
            fileIn.close();
        }catch(FileNotFoundException e) {
            return;
        }catch(IOException i) {
            i.printStackTrace();
            return;
        }catch(ClassNotFoundException c) {
            System.out.println("Configuration file for FileManager not found");
            c.printStackTrace();
            return;
        }
    }

    /**
     * Returns the id's of the files.
     * @return files
     */
    public Hashtable<String, String> getFiles() {
        return files;
    }

    /**
     * Adds a fileId to the hashtable.
     * @param filename the name of the file
     * @param fileid the id of the file
     * @param file the file
     * @return the previous id of the file
     */
    public String addFile(String filename, String fileid, File file){
        String previousFileId = files.get(filename);
        files.put(filename, fileid);
        return previousFileId;
    }

    /**
     * Returns the id of the file.
     * @param filename the name of the file
     * @return the id of the file
     */
    public String getFileId(String filename){
        return files.get(filename);
    }

    /**
     * Returns the number of chunks.
     * @param file the file
     * @return size
     */
    public static int getNumberChunks(File file){
        long length = file.length();
        return (int) Math.ceil((double)length/Server.CHUNK_SIZE);
    }

    /**
     * An hex array.
     */
    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

    /**
     * Changes all of the bytes in a byte array to hexadecimal.
     * @param bytes
     * @return the bytes changed into hexadecimal
     */
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     * Hashes a file by checking the owner, the path, the date of the last modification and the size of the file and hashes it using SHA-256.
     * @param filename the name of the file
     * @return the id
     */
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
            return fileId;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

}
