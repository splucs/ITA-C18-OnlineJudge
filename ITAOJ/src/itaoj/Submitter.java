/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package itaoj;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 *
 * @author Lucas Franca
 */
public class Submitter implements Runnable {
    
    private Socket serverSocket;
    private DataOutputStream outToServer;
    private BufferedReader inFromServer;
    private Socket submitSocket;
    
    private String serverAddress;
    private int serverPort;
    private int submitPort;
    private String command;
    
    private int numLines;
    private String code;
    private String filePath;
    private int problemId;
    
    private String verdict;
    private int submissionId;
    
    public Submitter(String _serverAddress, int _serverPort, String _filePath, int _problemId) {
        serverAddress = _serverAddress;
        serverPort = _serverPort;
        problemId  =_problemId;
        filePath = _filePath;
    }

    private void readCode() throws Exception {
        
        //Read code from file, compute number of lines
        BufferedReader br = new BufferedReader(new FileReader(filePath));
        try {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            numLines = 1;

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
                numLines++;
            }
           code = sb.toString();
        } finally {
            br.close();
        }
    }

    public void run() {

        //reads code
        try {
            readCode();
            System.out.println("[SUBMITTER] Code read (" + numLines + " lines):\n" + code);
        } catch (Exception e) {
            System.out.println("[SUBMITTER] Exception: " + e.getMessage());
            e.printStackTrace();
            return;
        }
        
        //Process submit request communication
        try {
            
            //Connects socket
            System.out.println("[SUBMITTER] Connecting request submitter...");
            serverSocket = new Socket(serverAddress, serverPort);
            serverSocket.setSoTimeout(ITAOJ.SOCKET_TIMEOUT);
            
            //Setting up submit request communication
            outToServer = new DataOutputStream(serverSocket.getOutputStream());
            inFromServer = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
            
            //Sends SubReq
            System.out.println("[SUBMITTER] Attempting submit request");
            outToServer.writeBytes(ITAOJ.SUBMIT_REQ + '\n');
            
            //Expects SubAck and port
            command = inFromServer.readLine();
            System.out.println("[SUBMITTER] Received: " + command);
            if (!command.equals(ITAOJ.SUBMIT_ACK)) {
                throw new Exception("Expected command SubAck, received: " + command);
            }
            submitPort = Integer.parseInt(inFromServer.readLine());
            System.out.println("[SUBMITTER] Received: " + command + " " + submitPort);
            
            //Sends SubFin
            outToServer.writeBytes(ITAOJ.SUBMIT_FIN + '\n');
            
            //Close submit request communication
            outToServer.close();
            inFromServer.close();
            serverSocket.close();
            
        } catch (Exception e) {
            System.out.println("[SUBMITTER] Exception: " + e.getMessage());
            e.printStackTrace();
            return;
        }
        
        //Process code submit communication
        try {
            
            //Connects socket
            System.out.println("[SUBMITTER] Connecting code submitter...");
            submitSocket = new Socket(serverAddress, submitPort);
            submitSocket.setSoTimeout(ITAOJ.SOCKET_TIMEOUT);
            
            //Setting up submit request communication
            outToServer = new DataOutputStream(submitSocket.getOutputStream());
            inFromServer = new BufferedReader(new InputStreamReader(submitSocket.getInputStream()));
            
            //Sends numLines and code
            outToServer.writeBytes(ITAOJ.CODE_SUB + "\n" + problemId + "\n"+ numLines + "\n" + code + "\n");
            
            //Expects CodAck
            command = inFromServer.readLine();
            System.out.println("[SUBMITTER] Received: " + command);
            if (!command.equals(ITAOJ.CODE_ACK)) {
                throw new Exception("Expected command CodAck, received: " + command);
            }
            submissionId = Integer.parseInt(inFromServer.readLine());
            System.out.println("[SUBMITTER] Submission id: " + submissionId);
            
            //Expects RemReq
            submitSocket.setSoTimeout(ITAOJ.CORRECTION_TIMEOUT);
            command = inFromServer.readLine();
            System.out.println("[SUBMITTER] Received: " + command);
            if (!command.equals(ITAOJ.REMAINDER_REQ)) {
                throw new Exception("Expected command RemReq, received: " + command);
            }
            submitSocket.setSoTimeout(ITAOJ.SOCKET_TIMEOUT);
            
            //Sends RemAck
            outToServer.writeBytes(ITAOJ.REMAINDER_ACK + "\n");
            
            //Expects RemFin and verdict
            command = inFromServer.readLine();
            System.out.println("[SUBMITTER] Received: " + command);
            if (!command.equals(ITAOJ.REMAINDER_FIN)) {
                throw new Exception("Expected command RemFin, received: " + command);
            }
            verdict = inFromServer.readLine();
            
            //Close code submit request communication
            outToServer.close();
            inFromServer.close();
            submitSocket.close();
        } catch (Exception e) {
            System.out.println("[SUBMITTER] Exception: " + e.getMessage());
            e.printStackTrace();
            return;
        }
        
        //Print answer
        System.out.println("[SUBMITTER] Submitter done, verdict: " + verdict);
    }
    
}
