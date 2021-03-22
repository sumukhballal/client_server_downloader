package test;

import main.Client;
import main.Logger;

import java.io.IOException;

public class WriteElapsedTime extends Thread {

    Client client;
    long elapsedTime;
    Logger logger;

    WriteElapsedTime(Client client, long elapsedTime, Logger logger) {
        this.client=client;
        this.elapsedTime=elapsedTime;
        this.logger=logger;
    }

    @Override
    public void run() {
        try {
            logger.log(" It took " + elapsedTime + " nanoseconds to download a file from 4 clients concurrently!",1 );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
