package main;
/*
Author: Sumukh Ballal
Date: 02/21/2021

A Generic main.Client

*/

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Scanner;

public class Client {

    Logger logger;
    String configFilePath=System.getProperty("user.dir")+"/src/resources/config.properties";
    HashMap<String, String> configHash;
    DataInputStream dataInputStream;
    DataOutputStream dataOutputStream;
    Socket socket;


    public Logger getLogger() {
        return logger;
    }

    public Client() throws IOException {
        configHash=new HashMap<>();
        readConfigFile();
    }

    public static void main(String[] args) throws IOException {
        Client client = new Client();
        client.setLogger(new Logger(client.configHash.get("logPath")));
        int serverPort=Integer.parseInt(client.configHash.get("portNumber"));
        client.initializeSocket(serverPort);
        client.doWork(System.in);
    }

    public void initializeSocket(int serverPort) throws IOException {
        InetAddress serverIp=InetAddress.getByName(configHash.get("serverIp"));
        socket = new Socket(serverIp, serverPort);
        logger.log("Connected to server "+serverIp+" on port !"+serverPort,1);
        dataOutputStream = new DataOutputStream(socket.getOutputStream());
        dataInputStream = new DataInputStream(socket.getInputStream());
    }

    public void setLogger(Logger logger) throws IOException {
        this.logger=logger;
    }

    /* Easy to test */
    public void doWork(InputStream inputStream) throws  IOException {
        logger.log("Started Client process! ",1);
        Scanner scanner = new Scanner(inputStream);

        /* Connect to server */

        while(true) {
            logger.log("\n Please use numbers to signify what you intend to do?\n" +
                    "1.) List all Files \n" +
                    "2.) Download File \n" +
                    "3.) Create a File on Server! \n" +
                    "4.) Exit ", 3);

            Integer input=Integer.parseInt(scanner.next());

            switch (input) {
                case 1:
                    listAllFiles();
                    break;
                case 2:
                    downloadFiles(scanner);
                    break;
                case 3:
                    logger.log("Enter File name! ",3);
                    String fileName=scanner.next();
                    logger.log("Enter File Size",3);
                    String fileSize=scanner.next();
                    createFileOnServer(fileName, fileSize);
                    break;
                case 4:
                    exit();
                    break;
                default:
                    logger.log("This is not a valid option! ",2);
                    break;
            }
        }
    }

    public boolean createFileOnServer(String fileName, String fileSize) throws  IOException {

        dataOutputStream.writeUTF("create_file");
        dataOutputStream.writeUTF(fileName);
        dataOutputStream.writeUTF(fileSize);
        if(dataInputStream.readUTF().equals("created")) {
            logger.log("File "+fileName+" of size "+fileSize+" has been created on server! ", 1);
            return true;
        }

        logger.log("File "+fileName+" of size "+fileSize+" has NOT been created on server! ", 2);
        return false;
    }

    /* Overloaded for tests */
    public void exit() throws IOException {
        try {
            dataOutputStream.writeUTF("exit");
            dataOutputStream.close();
            dataInputStream.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.log("Exiting client! ",1);
        System.exit(0);
    }

    public void exit(Scanner scanner) throws IOException {
        try {
            dataOutputStream.writeUTF("exit");
            dataOutputStream.close();
            dataInputStream.close();
            socket.close();
            scanner.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.log("Exiting client! ",1);
    }

    public void listAllFiles() throws IOException {
        if(dataInputStream==null || dataOutputStream==null) {
            logger.log("Terminating client! Something is not right! ",2);
            System.exit(0);
        }

        /* Get files from server */
        try {
            /* Send action to server */
            logger.log("Listing all files! ",1);
            dataOutputStream.writeUTF("get_files_list");
            String response=dataInputStream.readUTF();
            if(response.equals("no_files_exist")) {
                logger.log("No files exist on server!",1);
            } else {
                logger.log(response,4);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void downloadFiles(Scanner scanner) throws IOException {
        if(scanner==null) {
            logger.log("Terminating client! Something is not right! ",2);
            System.exit(0);
        }

        logger.log("Do you want to download one file or multiple files? > \n 1.) One File \n 2.) Multiple Files ",3);
        String input=scanner.next();

        switch (input) {
            case "1":
                logger.log("Enter the file name! ",3);
                String fileName=scanner.next();
                downloadSerially(fileName);
                break;
            case "2":
                logger.log("Enter the file names separated by (,) ! ",3);
                String fileNames=scanner.next();
                logger.log("Do you want to download them \n 1.) Serially \n 2.) Parallely?",3);
                String choice=scanner.next();
                if(choice.equals("1")){
                    downloadSerially(fileNames);
                } else if(choice.equals("2")) {
                    downloadParallely(fileNames);
                } else {

                    logger.log("Invalid choice! Please choose 1 or 2. ",2);
                }
                break;
            default:
                logger.log("Not a valid input! Please input 1 or 2 !",2);
                break;
        }
    }

    /* Function which reads config file */
    private void readConfigFile() throws IOException {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(configFilePath));
            String line=reader.readLine();
            while(line!=null) {
                String[] lineArray=line.split("=");
                configHash.put(lineArray[0], lineArray[1]);
                line=reader.readLine();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void downloadSerially(String fileNames) throws  IOException {

        String[] fileNamesArray=fileNames.split(",");
        logger.log("Serial download of "+fileNamesArray.length+" files!",1);

        long startTime=System.nanoTime();
        for(String fileName : fileNamesArray) {
            fileName=fileName.trim();
            try {
                dataOutputStream.writeUTF("download_serial");
                dataOutputStream.writeUTF(fileName);
                String response = dataInputStream.readUTF();

                if (response.equals("does_not_exist")) {
                    logger.log("This file " + fileName + " does not exist on the server! ", 2);
                } else {
                    int fileSize = Integer.parseInt(response);
                    String filePath = configHash.get("fileDirectoryPath");
                    Thread current=new DownloadHandler(true, fileName, filePath, fileSize, dataInputStream,1, logger);
                    /* Serially */
                    current.start();
                    current.join();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        long elapsedTime=System.nanoTime() - startTime;
        logger.log("It took "+(elapsedTime)+" Nanoseconds to download serially "+fileNamesArray.length+" files to client!",1);
    }


    public void downloadParallely(String fileNames) throws IOException {
        String[] fileNamesArray=fileNames.split(",");
        logger.log("Parallel download of "+fileNamesArray.length+" files!",1);

        long startTime=System.nanoTime();

        for(String fileName : fileNamesArray) {
            fileName=fileName.trim();
            try {
                dataOutputStream.writeUTF("download_parallel");
                dataOutputStream.writeUTF(fileName);
                String response = dataInputStream.readUTF();

                logger.log(response,1);

                if (response.equals("does_not_exist")) {
                    logger.log("This file " + fileName + " does not exist on the server! ", 2);
                } else {
                    int fileSize = Integer.parseInt(response);
                    String filePath = configHash.get("fileDirectoryPath");
                    Thread current=new DownloadHandler(false, fileName, filePath, fileSize, dataInputStream,1, logger);
                    /* Parallel */
                    current.start();
                    current.join();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        long elapsedTime=System.nanoTime() - startTime;
        logger.log("It took "+(elapsedTime)+" Nanoseconds to download parallely "+fileNamesArray.length+" files to client!",1);
    }
}
