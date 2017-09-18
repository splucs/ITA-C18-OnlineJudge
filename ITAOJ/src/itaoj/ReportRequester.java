/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package itaoj;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 *
 * @author Lucas Franca
 */
public class ReportRequester implements Runnable{
    
    private Socket serverSocket;
    private String serverAddress;
    private DataOutputStream outToServer;
    private BufferedReader inFromServer;
    private int serverPort;
    
    private int submissionId;
    private String command;
    private String verdict;
    
    public ReportRequester(String _serverAddress, int _serverPort, int _submissionId) {
        serverAddress = _serverAddress;
        serverPort = _serverPort;
        submissionId = _submissionId;
    }
    
    public void run() {
        
        try {
            
            //Connects socket
            System.out.println("[REPORTER] Connecting request submitter...");
            serverSocket = new Socket(serverAddress, serverPort);
            serverSocket.setSoTimeout(ITAOJ.SOCKET_TIMEOUT);
            
            //Setting up submit request communication
            outToServer = new DataOutputStream(serverSocket.getOutputStream());
            inFromServer = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
            
            //Sends RpoReq
            System.out.println("[REPORTER] Attempting report request");
            outToServer.writeBytes(ITAOJ.REPORT_REQ + '\n' + submissionId + '\n');
            
            //Expects RpoAck and verdict
            command = inFromServer.readLine();
            System.out.println("[REPORTER] Received: " + command);
            if (!command.equals(ITAOJ.REPORT_ACK)) {
                throw new Exception("Expected command RpoAck, received: " + command);
            }
            verdict = inFromServer.readLine();
            
            //Sends RpoFin
            outToServer.writeBytes(ITAOJ.REPORT_FIN + '\n');
            
            //Close submit request communication
            outToServer.close();
            inFromServer.close();
            serverSocket.close();
            
        } catch (Exception e) {
            System.out.println("[REPORTER] Exception: " + e.getMessage());
            e.printStackTrace();
            return;
        }
        
        //Print answer
        System.out.println("[REPORTER] Reporter done, verdict: " + verdict);
          
    }
}
