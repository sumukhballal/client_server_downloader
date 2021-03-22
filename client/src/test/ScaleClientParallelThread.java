package test;

import main.Client;
import main.Logger;

import java.io.IOException;

import main.Client;
import main.Logger;

import java.io.IOException;

    public class ScaleClientParallelThread  extends  Thread {

        int clientNumber;
        Client client;
        String fileName;
        Logger logger;

        ScaleClientParallelThread(int clientNumber, Client client, String fileName, Logger logger) {
            this.clientNumber=clientNumber;
            this.client=client;
            this.fileName=fileName;
            this.logger=logger;
        }

        @Override
        public void run() {

            try {
                //Logger logger = client.getLogger();
                logger.log("Client: " + clientNumber + " : Running client " + clientNumber +" !", 4);
                logger.log("Client: " + clientNumber + " : Downloading file : "+fileName+" from Server! ", 4);
                client.downloadParallely(fileName);
                client.exit();
                Thread.currentThread().interrupt();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
