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
    
    private Socket clientSocket;
    
    public ReportRequester(String serverAddress, int serverPort) {
        
        try {
            clientSocket = new Socket(serverAddress, serverPort);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
    
    public void run() {
        
    }
}
