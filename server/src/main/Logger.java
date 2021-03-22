package main;

/*
Author: Sumukh Ballal
Date: 02/21/2021

This is the class which handles server side logging

*/

public class Logger {

    /* Provide logging access to clients */
    public void log(String data) {
        System.out.println("INFO: "+data);
        /* Write to file mechanism as well needs to be done */
        writeToLog(data);
    }

    /* Internal Log which writes to disk */
    private void writeToLog(String data) {

    }
}
