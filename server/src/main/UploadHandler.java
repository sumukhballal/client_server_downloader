package main;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class UploadHandler extends Thread {

    int threadNumber;
    String fileName;
    int clientNumber;
    String filePath;
    final private Logger logger = new Logger();
    final DataOutputStream dataOutputStream;
    final DataInputStream dataInputStream;
    boolean serial;


    UploadHandler(boolean serial, int threadNumber, String fileName, String filePath, int clientNumber, DataInputStream dataInputStream, DataOutputStream dataOutputStream) {
        this.threadNumber=threadNumber;
        this.fileName=fileName;
        this.clientNumber=clientNumber;
        this.filePath=filePath+"/"+fileName;
        this.dataInputStream=dataInputStream;
        this.dataOutputStream=dataOutputStream;
        this.serial=serial;
    }

    @Override
    public void run() {

        if(serial) {
            uploadSerial();
        } else {
            uploadParallel();
        }
    }

    private void uploadParallel() {
        logger.log("Uploading file parallel "+fileName+" using thread "+threadNumber+" for client with id "+clientNumber);
        File file = new File(filePath);
        byte[] fileBytes = new byte[(int) file.length()];

        try {
            /* Load into buffered input stream */
            BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
            bufferedInputStream.read(fileBytes, 0, fileBytes.length);
            bufferedInputStream.close();
            /* Write to client socket */

            long startTime=System.nanoTime();
            dataOutputStream.writeUTF(Integer.toString(fileBytes.length));
            dataOutputStream.write(fileBytes, 0, fileBytes.length);
            dataOutputStream.flush();
            long elapsedTime=System.nanoTime() - startTime;
            logger.log("It took "+(elapsedTime)+" Nanoseconds to upload file "+fileName+" of size "+fileBytes.length+" to client "+clientNumber);
            /* Kill thread */
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            logger.log("Something went wrong uploading file "+fileName+" using thread "+threadNumber+" for client with id "+clientNumber);
            e.printStackTrace();
        }
    }

    private void uploadSerial() {
        logger.log("Uploading file Serially "+fileName+" using thread "+threadNumber+" for client with id "+clientNumber);
        File file = new File(filePath);
        byte[] fileBytes = new byte[(int) file.length()];

        try {
            /* Load into buffered input stream */
            BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
            bufferedInputStream.read(fileBytes, 0, fileBytes.length);
            bufferedInputStream.close();
            /* Write to client socket */

            long startTime=System.nanoTime();
            dataOutputStream.writeUTF(Integer.toString(fileBytes.length));
            dataOutputStream.write(fileBytes, 0, fileBytes.length);
            dataOutputStream.flush();
            long elapsedTime=System.nanoTime() - startTime;
            try {
                String md5Checksum = getMD5Checksum(filePath, MessageDigest.getInstance("MD5"));
                if(!md5Checksum.equals("")) {
                    dataOutputStream.writeUTF(md5Checksum);
                } else {
                    dataOutputStream.writeUTF("no_md5_checksum");
                }
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }

            logger.log("It took "+(elapsedTime)+" Nanoseconds to upload file "+fileName+" of size "+fileBytes.length+" to client "+clientNumber);
            /* Kill thread */
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            logger.log("Something went wrong uploading file "+fileName+" using thread "+threadNumber+" for client with id "+clientNumber);
            e.printStackTrace();
        }
    }

    private String getMD5Checksum(String filePath, MessageDigest messageDigest) {

        StringBuilder md5Hash = new StringBuilder();

        try(DigestInputStream dis = new DigestInputStream(new FileInputStream(filePath), messageDigest)) {

            while(dis.read() != -1) {
                messageDigest=dis.getMessageDigest();
            }
        } catch(IOException e) {
            logger.log("Something when wrong when calculating the md5 hash! ");
            e.printStackTrace();
        }

        for( byte b : messageDigest.digest()) {
            md5Hash.append(String.format("%02x",b));
        }

        return md5Hash.toString();
    }

}
