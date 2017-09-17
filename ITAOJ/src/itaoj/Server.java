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

/**
 *
 * @author Lucas Franca
 */
public class Server implements Runnable{
    
    private String clientSentence;
    private ServerSocket serverSocket;
    private BufferedReader inFromClient;
    private DataOutputStream outToClient;
    
    public Server(String serverAddress, int serverPort) {
        try {
            serverSocket = new ServerSocket(serverPort);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        System.out.println("[SERVER] Server online");
    }
    
    public void run() {
        try {
            while (true) {
                System.out.println("[SERVER] Awaiting connection");
                Socket connectionSocket = serverSocket.accept();
                inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                outToClient = new DataOutputStream(connectionSocket.getOutputStream());
                clientSentence = inFromClient.readLine();
                System.out.println("Received: " + clientSentence);
                outToClient.writeBytes(ITAOJ.SUBMIT_ACK + "\n");
                clientSentence = inFromClient.readLine();
                System.out.println("Received: " + clientSentence);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
