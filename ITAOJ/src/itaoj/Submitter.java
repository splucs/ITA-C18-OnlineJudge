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
public class Submitter implements Runnable {
    
    private Socket clientSocket;
    private DataOutputStream outToServer;
    private BufferedReader inFromServer;
    
    public Submitter(String serverAddress, int serverPort) {
        try {
            clientSocket = new Socket(serverAddress, serverPort);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        System.out.println("[SUBMITTER] Submitter online");
    }
    
    public void run() {
        String modifiedSentence;
        try {
            System.out.println("[SUBMITTER] Attempting submit");
            outToServer = new DataOutputStream(clientSocket.getOutputStream());
            inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            outToServer.writeBytes(ITAOJ.SUBMIT_REQ + '\n');
            modifiedSentence = inFromServer.readLine();
            System.out.println("FROM SERVER: " + modifiedSentence);
            outToServer.writeBytes(ITAOJ.SUBMIT_FIN + '\n');
            clientSocket.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
    
}
