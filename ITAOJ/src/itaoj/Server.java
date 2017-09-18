/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package itaoj;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 *
 * @author Lucas Franca
 */
public class Server implements Runnable{
    
    private String command;
    private ServerSocket serverSocket;
    private BufferedReader inFromClient;
    private DataOutputStream outToClient;
    private String serverAddress;
    private int serverPort;
    
    private boolean[] available;
    private final int MAX_PORT = 10000;
    private final int MIN_PORT = 1024;
    private Map<Integer, String> verdicts;
    private int nextSubmission;
    private String databasePath;
    
    public Server(String _serverAddress, int _serverPort, String _databasePath) {
        serverAddress = _serverAddress;
        serverPort = _serverPort;
        databasePath = _databasePath;
        if (!databasePath.endsWith("\\")) databasePath = databasePath + "\\";
        available = new boolean[MAX_PORT+1];
        for(int i=0; i<=MAX_PORT; i++) available[i] = true;
        available[serverPort] = false;
        verdicts = new HashMap<>();
        nextSubmission = 0;
    }
    
    public String getDBPath() {
        return databasePath;
    }
    
    public String getServerAddress() {
        return serverAddress;
    }
    
    public int getServerPort() {
        return serverPort;
    }
    
    public int getNextSubmission() {
        nextSubmission++;
        return nextSubmission - 1;
    }
    
    public void setSubmissionVerdict(int submissionId, String verdict) {
        verdicts.put(submissionId, verdict);
    }
    
    private int getAvailablePort() {
        
        //Returns ans available port greater than 1023
        Random rn = new Random();
        int range, port;
        
        do {
            range = MAX_PORT - MIN_PORT + 1;
            port = MIN_PORT + (Math.abs(rn.nextInt()) % range);
        } while(!available[port]);
        
        return port;
    }
    
    public void reservePort(int port) {
        System.out.println("[SERVER] Submit port reserved: " + port);
        available[port] = false;
    }
    
    public void freePort(int port) {
        System.out.println("[SERVER] Submit port freed: " + port);
        available[port] = true;
    }
    
    private void processSubmit() throws Exception{
        
        //Sends reply with a new port a port
        int submitPort = getAvailablePort();
        outToClient.writeBytes(ITAOJ.SUBMIT_ACK + "\n" + submitPort + "\n");

        //Expects SubFin
        command = inFromClient.readLine();
        System.out.println("[SERVER] Received: " + command);
        if (!command.equals(ITAOJ.SUBMIT_FIN)) {
            throw new Exception("Expected command SubFin, received: " + command);
        }

        //Run corrector
        Thread corrector = new Thread(new Corrector(this, submitPort, getNextSubmission()));
        corrector.start();
    }
    
    private void processReport() throws Exception{
        
        //Expects submission id
        int submissionId = Integer.parseInt(inFromClient.readLine());
        System.out.println("[SERVER] Received: " + submissionId);
        
        //Gets verdict
        String verdict;
        if (verdicts.containsKey(submissionId)) {
            verdict = verdicts.get(submissionId);
        }
        else {
            verdict = "SUBMISSION NOT FOUND";
        }
        
        //Send verdict
        outToClient.writeBytes(ITAOJ.REPORT_ACK + "\n" + verdict + "\n");

        //Expects RpoFin
        command = inFromClient.readLine();
        System.out.println("[SERVER] Received: " + command);
        if (!command.equals(ITAOJ.REPORT_FIN)) {
            throw new Exception("Expected command RpoFin, received: " + command);
        }
    }
    
    public void run() {
        
        //Initialize server socket
        try {
            serverSocket = new ServerSocket(serverPort);
        } catch (Exception e) {
            System.out.println("[SERVER] Exception: " + e.getMessage());
        }
        System.out.println("[SERVER] Server online");
        
        while (true) {
            try {
                
                //Awaiting request
                System.out.println("[SERVER] Awaiting submit request");
                Socket connectionSocket = serverSocket.accept();
                System.out.println("[SERVER] Request found");
                
                //Setting up submit request communication
                connectionSocket.setSoTimeout(ITAOJ.SOCKET_TIMEOUT);
                inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                outToClient = new DataOutputStream(connectionSocket.getOutputStream());
                
                //Wait for command
                command = inFromClient.readLine();
                System.out.println("[SERVER] Received: " + command);
                if (command.equals(ITAOJ.SUBMIT_REQ)) {
                    processSubmit();
                }
                else if (command.equals(ITAOJ.REPORT_REQ)) {
                    processReport();
                }
                else {
                    throw new Exception("Received unknown command: " + command);
                }
                
                //Close stuff
                inFromClient.close();
                outToClient.close();
                connectionSocket.close();
                
            } catch (Exception e) {
                System.out.println("[SERVER] Exception: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
