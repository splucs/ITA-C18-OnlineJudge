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
    public static final int SOCKET_TIMEOUT = 5000;
    public static final int CORRECTION_TIMEOUT = 60000;

    public static void printWrongFormatMesage() {
        System.out.println("Please specify use:");
        System.out.println("server <ip> <port> <databasePath>");
        System.out.println("submit <ip> <port> <filePath> <problemId>");
        System.out.println("report <ip> <port> <submissionId>");
    }

    public static void main(String[] args) {
        
        if (args.length < 1) {
            printWrongFormatMesage();
            return;
        }
        
        Thread thread = null;
        
        switch (args[0]) {
            case "server":
                if (args.length < 3) {
                    printWrongFormatMesage();
                    return;
                }
                thread = new Thread(new Server(args[1], Integer.parseInt(args[2]), args[3]));
                break;
            case "submit":
                if (args.length < 4) {
                    printWrongFormatMesage();
                    return;
                }
                thread = new Thread(new Submitter(args[1], Integer.parseInt(args[2]), args[3], Integer.parseInt(args[4])));
                break;
            case "report":
                if (args.length < 3) {
                    printWrongFormatMesage();
                    return;
                }
                thread = new Thread(new ReportRequester(args[1], Integer.parseInt(args[2]), Integer.parseInt(args[3])));
                break;
            default:
                thread = null;
        }
        
        thread.start();
        
        //server localhost 2374 "..\DataBase"
        //submit localhost 2374 "..\dummy.cpp" 1001
        //report localhost 2374 0
        
        /*
        //  DEBUG
        //dummy submit
        Thread submit = new Thread(new Submitter(args[1], Integer.parseInt(args[2]), "..\\dummy.cpp", 1001));
        submit.start();
        
        try {
            Thread.sleep(SOCKET_TIMEOUT);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        
        //dummy report
        Thread report = new Thread(new ReportRequester(args[1], Integer.parseInt(args[2]), 0));
        report.start();*/
        
    }
    
}
