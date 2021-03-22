package main;

/*
Author: Sumukh Ballal
Date: 02/21/2021

This is the class which handles incoming requests and can be thought of as the thread which handles this client

*/

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.RandomAccess;

public class RequestHandler extends  Thread {

    private Socket socket;
    private int requestNumber;
    private Server server;
    final private Logger logger = new Logger();
    final String fileDirectoryRootPath;
    final DataOutputStream dataOutputStream;
    final DataInputStream dataInputStream;

    /* Constructor */
    RequestHandler(Socket socket, int requestNumber, Server server, DataInputStream dataInputStream, DataOutputStream dataOutputStream) {
        this.socket=socket;
        this.server=server;
        this.requestNumber=requestNumber;
        this.dataInputStream=dataInputStream;
        this.dataOutputStream=dataOutputStream;
        this.fileDirectoryRootPath=server.configHash.get("fileDirectoryPath");
    }

    /* Override thread run to let thread implementation handle OS calls */


    /* Commands supported
    *  get_files_list
    * create file
    * download
    *  exit
    * */

    @Override
    public void run() {
        if (socket != null) {
            try {

                while(true) {

                    String command=dataInputStream.readUTF();

                    if (command.equals("get_files_list")) {
                        String fileList = listFiles(new File(fileDirectoryRootPath));
                        if (fileList != null) {
                            dataOutputStream.writeUTF(fileList);
                        } else {
                            dataOutputStream.writeUTF("no_files_exist");
                        }
                    }

                    if(command.equals("download_serial")) {
                        startUploadThread();
                    }

                    if(command.equals("download_parallel")) {
                        startUploadThreadParallel();
                    }

                    if(command.equals("create_file")) {
                        createFile();
                    }

                    if(command.equals("exit")) {
                        exit(dataInputStream, dataOutputStream);
                        break;
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
                /* Reduce the total number of clients connected to server*/
                server.numberOfClients--;
            }
        } else {
            logger.log("Socket is null! ");
        }
    }

    private void createFile() throws IOException {

        String fileName=dataInputStream.readUTF();
        String fileSize=dataInputStream.readUTF();
        logger.log("Client "+requestNumber+" has requested to create file "+fileName+" with filesize "+fileSize+" !");
        File file = new File(fileDirectoryRootPath+"/"+fileName);
        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
        randomAccessFile.setLength(Integer.parseInt(fileSize));
        randomAccessFile.close();
        dataOutputStream.writeUTF("created");
        logger.log("Client "+requestNumber+" : File "+fileName+" has been created!");
    }

    private void startUploadThreadParallel() {

        try {
            /* Take input file name */

            String fileName = dataInputStream.readUTF();
            if(checkIfFileExists(fileDirectoryRootPath+"/"+fileName)) {
                logger.log("Starting upload thread to upload file  "+fileName);
                Thread current=new UploadHandler(false, 1, fileName, fileDirectoryRootPath, requestNumber, dataInputStream, dataOutputStream);

                current.start();
                current.join();
            } else {
                dataOutputStream.writeUTF("does_not_exist");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /* This function is the boilerplate to create upload threads for uploading files to client */
    private void startUploadThread() {

        try {
            /* Take input file name */

            String fileName = dataInputStream.readUTF();
            if(checkIfFileExists(fileDirectoryRootPath+"/"+fileName)) {
                logger.log("Starting upload thread to upload file  "+fileName);
                Thread current=new UploadHandler(true, 1, fileName, fileDirectoryRootPath, requestNumber, dataInputStream, dataOutputStream);

                current.start();
                current.join();
            } else {
                dataOutputStream.writeUTF("does_not_exist");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean checkIfFileExists(String filename) {
        File file = new File(filename);
        return file.exists();
    }

    private void exit(DataInputStream dataInputStream, DataOutputStream dataOutputStream) {
        try {
            dataInputStream.close();
            dataOutputStream.close();
            Thread.currentThread().interrupt();
            socket.close();
            server.numberOfClients--;
            logger.log("Exiting client "+requestNumber+" ! Total clients! "+server.numberOfClients);
        } catch (Exception e) {
            e.printStackTrace();
            logger.log("Cannot close socket! ");
        }
    }


    private String listFiles(File FlistFiles
) {
        /* Get all files */
        try {
            logger.log("Listing file in directory " + folder.getCanonicalPath() + " for client "+requestNumber);
        } catch (IOException e) {
            e.printStackTrace();
        }
        StringBuilder result = new StringBuilder();
        int index=0;
        for(File file : folder.listFiles()) {
            result.append((++index)+".) "+file.getName()+" \n");
        }
        result.append("\n");

        return result.toString();
    }
}
