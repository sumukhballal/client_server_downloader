package test;

import main.Client;
import main.Logger;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.HashMap;


class EvaluationTests {

    public Logger logger;
    private int FIXED_NUMBER_OF_FILES=10;
    private int FIXED_REPETITIONS=2;
    String configFilePath=System.getProperty("user.dir")+"/src/resources/config.properties";
    HashMap<String, String> configHash = new HashMap<>();

    public EvaluationTests() {
        try {
            readConfigFile();
            logger = new Logger(configHash.get("logPath"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void transferOneFileTest() throws IOException, InterruptedException {

       Client client = new Client();
       client.setLogger(logger);

        /* Download 1 file serially */
        logger.log("Download 1 files [ test1.txt ] and check integrity of file! \n",1);
        client.initializeSocket(7777);
        client.downloadSerially("test1.txt");
        client.exit();
    }

    @Test
    public void scaleToFourTest() {

        try {
            Client[] clients = new Client[4];
            String[] fileNames = {"test1.txt", "test2.txt", "test3.txt", "test4.txt"};

            for (int i = 0; i < 4; i++) {
                clients[i] = new Client();
                clients[i].setLogger(logger);
                clients[i].initializeSocket(7777);
            }

            /* Download 4 different files serially */
            /* We will run each of the above clients in parallel using threads */

            logger.log("Scale to four and download 4 files [ test1.txt, test2.txt, test3.txt, test4.txt ] and check integrity of file! ", 1);
            Thread[] threads = new Thread[4];

            long startTime = System.nanoTime();
            for (int i = 0; i < 4; i++) {
                threads[i] = new ScaleClientThread(i + 1, clients[i], fileNames[i], logger);
                threads[i].start();
            }

            for (int i = 0; i < 4; i++) {
                threads[i].join();
            }

            long elapsedTime = System.nanoTime() - startTime;
            logger.log("It took "+elapsedTime+" to run 4 clients concurrentlly! ",1);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void transferSpeedClientConcurrent() throws IOException, InterruptedException {


        /* Concurrent clients ranging from 2 to 16 */
        int[] concurrentClients = {2, 4, 8, 16};

            String fileNames = "test1.txt,test2.txt,test3.txt,test4.txt";

            int N=16;


                Client[] clients = new Client[N];

                for (int i = 0; i < N; i++) {
                    clients[i] = new Client();
                    clients[i].setLogger(logger);
                    clients[i].initializeSocket(7777);
                }

                logger.log("Scaled to " + N + " clients and downloading 10 files concurrently and checking integrity of file! ", 1);

                Thread[] threads = new Thread[N];
                long startTime = System.nanoTime();
                for (int i = 0; i < N; i++) {
                    threads[i] = new ScaleClientParallelThread(i + 1, clients[i], fileNames, logger);
                    threads[i].start();
                }

                for (int i = 0; i < 4; i++) {
                    threads[i].join();
                }

                long elapsedTime = System.nanoTime() - startTime;
                logger.log("It took " + elapsedTime + " nanoseconds to download a file from " + N + " clients concurrently! ", 1);
                logger.exit();

        }

        @Test
        public void createFileTest() throws IOException, InterruptedException {
            Client createFileClient = new Client();
            createFileClient.setLogger(logger);
            createFileClient.initializeSocket(7777);

            String[] fileSizes = {"128", "512", "2000", "8000", "32000"};
            for (String size : fileSizes) {
                /* First we create 10 of these files on server */
                for (int i = 1; i < 11; i++) {
                    String filename="eval4test" + i + "_" + size + ".txt";
                    logger.log("Creating file "+filename+" of size "+size,1);
                    createFileClient.createFileOnServer(filename, size);
                }
            }
                createFileClient.exit();
        }

    @Test
    public void transferSpeedFileSizeConcurrent() throws IOException, InterruptedException {


            /* We then download them from 4 Parallel clients each downloading 10 files parallely for a total of 2 times */

        //String[] fileSizes = {"128", "512", "2000", "8000", "32000"};
        Client[] clients = new Client[4];

        String fileSize="32000";
        for (int i = 0; i < 4; i++) {
            clients[i] = new Client();
            clients[i].setLogger(logger);
            clients[i].initializeSocket(7777);
        }


            logger.log("Scaled to 4 clients and downloading 10 files concurrently and checking integrity of file for size " + fileSize, 1);

            Thread[] threads = new Thread[10];
            long startTime = System.nanoTime();
            for (int i = 0; i < 4; i++) {

                StringBuilder filenames = new StringBuilder();

                for (int j = 1; j < 11; j++) {
                    filenames.append("eval4test" + j + "_" +fileSize+".txt");
                    filenames.append(",");
                }

                threads[i] = new ScaleClientParallelThread(i + 1, clients[i], filenames.toString() , logger);
                threads[i].start();
            }

            for (int i = 0; i < 4; i++) {
                threads[i].join();
            }

            long elapsedTime = System.nanoTime() - startTime;
            logger.log("It took " + elapsedTime + " nanoseconds to download 10 files of size " + fileSize + " with 4 clients and 10 threads concurrently! ", 1);
            logger.exit();
        }

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



}
