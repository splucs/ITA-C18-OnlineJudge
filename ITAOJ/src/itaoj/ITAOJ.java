/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package itaoj;

/**
 *
 * @author Lucas Franca
 */
public class ITAOJ {
    
    public static final String SUBMIT_REQ = "SubReq";
    public static final String SUBMIT_ACK = "SubAck";
    public static final String SUBMIT_FIN = "SubFin";
    
    public static void main(String[] args) {
        
        Thread server = new Thread(new Server("192.168.0.22", 2374));
        Thread submitter = new Thread(new Submitter("192.168.0.22", 2374));
        
        server.start();
        submitter.start();
        
    }
    
}
