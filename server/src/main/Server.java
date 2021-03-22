package main;

/*
Author: Sumukh Ballal
Date: 02/21/2021

This is the main Server class which listens for clients and provides them service.
 */

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class Server {

    final public static Logger logger = new Logger();
    int numberOfClients=0;
    int numOfUploadThreads=10;
    String configFilePath=System.getProperty("user.dir")+"/src/resources/config.properties";
    HashMap<String, String> configHash;

    Server() {
        configHash=new HashMap<>();
        readConfigFile();
    }

    public static void main(String[] args) {

        logger.log("Starting server!");
        /* Create the socket for port specified in config file */
        Server server = new Server();
        ServerSocket socket=server.createSocket(Integer.parseInt(server.configHash.get("portNumber")));

        if(socket!=null) {
            int requestNumber=0;
            while(true) {
                try {
                    /* Accept a connection coming in and send it to the client handler */
                    Socket newSocket = socket.accept();
                    requestNumber+=1;

                    new RequestHandler(newSocket, requestNumber, server, new DataInputStream(newSocket.getInputStream()), new DataOutputStream(newSocket.getOutputStream())).start();
                    server.numberOfClients++;
                    logger.log("Accepted Client "+requestNumber+" ! Total clients! "+server.numberOfClients);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            logger.log("Failed to create Socket!");
        }
    }

    /* Function which reads config file */
    private void readConfigFile() {
        logger.log("Config file path! "+configFilePath);
        try {
            BufferedReader reader = new BufferedReader(new FileReader(configFilePath));
            String line=reader.readLine();
            while(line!=null) {
                String[] lineArray=line.split("=");
                configHash.put(lineArray[0], lineArray[1]);
                line=reader.readLine();
            }

        } catch (FileNotFoundException e) {
            logger.log("File not found! ");
            e.printStackTrace();
        } catch (IOException e) {
            logger.log("Unable to read file! ");
            e.printStackTrace();
        }
    }

    /* Generic function which creates the socket with some error handling */
    private ServerSocket createSocket(int port) {
        ServerSocket socket = null;
        if(port < 1000 || port > Integer.MAX_VALUE-1)
            return socket;

        logger.log("Creating socket binding with port "+port);
        try {
            socket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return socket;
    }
}
