package main;

/*
Author: Sumukh Ballal
Date: 02/21/2021

This is the class which handles server side logging

*/

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Logger {

    /* Provide logging access to clients */
    BufferedWriter bufferedWriter;

    public Logger(String logPath) throws IOException {
        bufferedWriter = new BufferedWriter(new FileWriter(new File(logPath), true));
    }

    public void log(String data, int type) throws IOException {

        StringBuilder result=new StringBuilder();

        if (type == 1) {
            result.append("INFO: ");
        } else if(type==2) {
            result.append("ERROR: ");
        } else if(type==3) {
            result.append("INPUT: ");
        } else if(type==4) {
            result.append("OUTPUT: \n");
        }

        result.append(data);
        if(type==3) {
            result.append("\n > ");
        }

        /* Write to disk */
        bufferedWriter.write(result.toString());
        System.out.println(result.toString());
    }

    /* Exit the logger */
    public void exit() throws IOException {
        bufferedWriter.close();
    }
}
