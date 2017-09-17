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
    public static final String CODE_SUB = "CodSub";
    public static final String CODE_ACK = "CodAck";
    public static final String REMAINDER_REQ = "RemReq";
    public static final String REMAINDER_ACK = "RemAck";
    public static final String REMAINDER_FIN = "RemFin";
    public static final String REPORT_REQ = "RpoReq";
    public static final String REPORT_ACK = "RpoAck";
    public static final String REPORT_FIN = "RpoFin";
    
    public static void main(String[] args) {
        
        if (args.length < 3) {
            System.out.println("Please specify use:");
            System.out.println("server <IP> <PORT>");
            System.out.println("submit <IP> <PORT>");
            System.out.println("report <IP> <PORT>");
            return;
        }
        
        Thread thread;
        
        switch (args[0]) {
            case "server":
                thread = new Thread(new Server(args[1], Integer.parseInt(args[2])));
                break;
            case "submit":
                thread = new Thread(new Submitter(args[1], Integer.parseInt(args[2])));
                break;
            case "report":
                thread = new Thread(new ReportRequester(args[1], Integer.parseInt(args[2])));
                break;
            default: thread = null;
        }
        
        thread.start();
        
    }
    
}
