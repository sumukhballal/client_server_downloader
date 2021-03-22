package main;

import java.io.*;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/* This class is a one to one mapping to download a file */
public class DownloadHandler extends Thread {

    String fileName;
    String filePath;
    int fileSize;
    DataInputStream dataInputStream;
    boolean checkIntegrity=false;
    /* Hard limit on 10 retries before giving up */
    int retryLimit=10;
    int retryCount=0;
    int threadNumber;
    boolean serial;
    Logger logger;

    public long getSerialDownloadTime() {
        return serialDownloadTime;
    }

    public void setSerialDownloadTime(long serialDownloadTime) {
        this.serialDownloadTime = serialDownloadTime;
    }

    public long getSerialIntegrityCheckTime() {
        return serialIntegrityCheckTime;
    }

    public void setSerialIntegrityCheckTime(long serialIntegrityCheckTime) {
        this.serialIntegrityCheckTime = serialIntegrityCheckTime;
    }

    long serialDownloadTime=0;
    long serialIntegrityCheckTime=0;


    final int NUMBER_THREADS_WRITE_PARALLEL=10;

    DownloadHandler(boolean serial, String fileName, String filePath, int fileSize, DataInputStream dataInputStream, int threadNumber, Logger logger) {
        this.fileName=fileName;
        this.filePath=filePath+"/"+fileName;
        this.fileSize=fileSize;
        this.dataInputStream=dataInputStream;
        this.threadNumber=threadNumber;
        this.serial=serial;
        this.logger=logger;
    }


    @Override
    public void run() {

        try {
            if (serial) {
                downloadSerial();
            } else {
                downloadParallel();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void downloadParallel() throws IOException {
        logger.log("Thread: "+threadNumber+" : Downloading file in parallel : " + fileName + " to directory " + filePath + " of size " + fileSize, 1);
        File file = new File(filePath);
        byte[] fileBytes = new byte[fileSize];

        while(!checkIntegrity && retryCount<retryLimit) {
            try {
                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file));
                int bytesRead = dataInputStream.read(fileBytes, 0, fileBytes.length);
                bufferedOutputStream.write(fileBytes, 0, bytesRead);
                bufferedOutputStream.close();
                logger.log("The MD5 Hash matches the server! Integrity check satisfied!",1);
                checkIntegrity=true;
                Thread.currentThread().interrupt();
                retryCount++;
                /* Need to retry the request as the file's md5 checkssum does not match */
            } catch (IOException e) {
                logger.log("Cannot download file due to errors. Retrying", 2);
                retryCount++;
                e.printStackTrace();
            }
        }
    }

    private void downloadSerial() throws IOException {
        logger.log("Downloading file serially : " + fileName + " to directory " + filePath + " of size " + fileSize, 1);
        File file = new File(filePath);
        byte[] fileBytes = new byte[fileSize];

        while(!checkIntegrity && retryCount<retryLimit) {
            try {
                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file));

                long startTime=System.nanoTime();
                int bytesRead = dataInputStream.read(fileBytes, 0, fileBytes.length);
                bufferedOutputStream.write(fileBytes, 0, bytesRead);
                bufferedOutputStream.close();
                long elapsedTime=System.nanoTime() - startTime;
                logger.log("It took "+(elapsedTime)+" Nanoseconds to download the file "+fileName+" of size "+fileBytes.length+" to client!",1);
                startTime=System.nanoTime();
                checkIntegrity=checkIntegrityOfFile(dataInputStream);
                elapsedTime=System.nanoTime() - startTime;
                logger.log("It took "+(elapsedTime)+" Nanoseconds to check integrity of the file "+fileName+" of size "+fileBytes.length+" to client!",1);

                if (checkIntegrity) {
                    logger.log("The MD5 Hash matches the server! Integrity check satisfied!",1);
                    Thread.currentThread().interrupt();
                    break;
                }
                retryCount++;
                /* Need to retry the request as the file's md5 checkssum does not match */
            } catch (IOException | NoSuchAlgorithmException e) {
                logger.log("Cannot download file due to errors. Retrying", 2);
                retryCount++;
                e.printStackTrace();
            }
        }
    }

    private boolean checkIntegrityOfFile(DataInputStream dataInputStream) throws NoSuchAlgorithmException,IOException {

        String clientCheckSum=getMD5Checksum(MessageDigest.getInstance("MD5"));
        String serverCheckSum = dataInputStream.readUTF();

        if(serverCheckSum.equals(clientCheckSum)) {
            return true;
        } else if(serverCheckSum.equals("no_md5_checksum")) {
            logger.log("Server did not send a checksum for file "+fileName,2);
        }

        return false;
    }

    private String getMD5Checksum(MessageDigest messageDigest) throws IOException {

        StringBuilder md5Hash = new StringBuilder();

        try(DigestInputStream dis = new DigestInputStream(new FileInputStream(filePath), messageDigest)) {

            while(dis.read() != -1) {
                messageDigest=dis.getMessageDigest();
            }
        } catch(IOException e) {
            logger.log("Something when wrong when calculating the md5 hash! ",2);
            e.printStackTrace();
        }

        for( byte b : messageDigest.digest()) {
            md5Hash.append(String.format("%02x",b));
        }

        return md5Hash.toString();
    }
}
