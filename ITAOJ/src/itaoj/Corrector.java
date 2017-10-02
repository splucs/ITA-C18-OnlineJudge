/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package itaoj;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Lucas Franca
 */
public class Corrector implements Runnable{
    
    private Server server;
    private ServerSocket submitSocket;
    private int submitPort;
    private BufferedReader inFromClient;
    private DataOutputStream outToClient;
    
    private String CorrectorTag;
    private String command;
    
    private String code;
    private int submissionId;
    private int problemId;
    private int numLines;
    private String verdict;
    private String filePath;
    private String fileName;
    private String testPath;
    
    private String stdin;
    private String stdout;
    private String stderr;
    private int returnValue;
    private boolean timeLimitExceeded;
    
    public Corrector(Server _server, int _submitPort, int _submissionId) {
        server = _server;
        submitPort = _submitPort;
        submissionId = _submissionId;
        CorrectorTag = "[CORRECTOR " + submitPort + "]";
    }
    
    private String readTxt(String outPath) throws Exception {
        
        //Read a file
        BufferedReader br = new BufferedReader(new FileReader(outPath));
        String out = "";
        
        try {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                line = br.readLine();
                if (line != null) sb.append(System.lineSeparator());
            }
            out = sb.toString();
        } finally {
            br.close();
        }
        
        return out;
    }
    
    void ExecWindowsCommand(String cmd) throws Exception {
        //Run cmd
        Process process = Runtime.getRuntime().exec(cmd);

        //Get input and output streams
        BufferedWriter stdOutput = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
        if (stdin == null) stdin = "";
        stdOutput.write(stdin);
        stdOutput.write("\r\n");
        stdOutput.flush();
        BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
        BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        
        timeLimitExceeded = !process.waitFor(1000, TimeUnit.MILLISECONDS);
        if(!timeLimitExceeded) returnValue = process.exitValue();

        //Read standard output
        String s;
        stdout = "";
        while ((s = stdInput.readLine()) != null) {
            if (stderr.length() > 0) stdout = stdout + "\n";
            stdout = stdout + s;
        }

        // Read errors
        stderr = "";
        while ((s = stdError.readLine()) != null) {
            if (stderr.length() > 0) stderr = stderr + "\n";
            stderr = stderr + s;
        }
        
        //Close streams
        stdInput.close();
        stdError.close();
        stdOutput.close();
    }

    private void correct() throws Exception{
        
        //Get paths
        fileName = submitPort + "-" + submissionId;
        testPath = server.getDBPath() + problemId + "\\";
        filePath = server.getDBPath() + fileName;
        
        //Write code
        PrintWriter writer = new PrintWriter(filePath + ".cpp", "UTF-8");
        writer.print(code);
        writer.close();
        
        //Compile
        System.out.println(CorrectorTag + " Compiling code");
        ExecWindowsCommand("g++ \"" + filePath + ".cpp\"" + " -O2 -o \"" + filePath + '\"');
        if(stdout.length() > 3 || stderr.length() > 3) {
            verdict = "COMPILATION ERROR";
            return;
        }
        
        //Run tests
        int numTests = (new File(testPath).listFiles()).length / 2;
        System.out.println(CorrectorTag + " perfoming " + numTests + " tests on submission " + submissionId);
        for(int test = 1; test <= numTests; test++) {
            stdin = readTxt(testPath + "in" + test + ".txt");
            String correctOut = readTxt(testPath + "out" + test + ".txt");
            ExecWindowsCommand('\"' + filePath + ".exe\"");
            if (timeLimitExceeded) {
                verdict = "TIME LIMIT EXCEEDED";
                System.out.println(CorrectorTag + " TLE");
                return;
            }
            if (stderr.length() > 0 || returnValue != 0) {
                verdict = "RUNTIME ERROR";
                System.out.println(CorrectorTag + " RTE, stderr: " + stderr);
                return;
            }
            if (!stdout.equals(correctOut)) {
                verdict = "WRONG ANSWER";
                System.out.println(CorrectorTag + " WA, expected " + correctOut + ", found " + stdout);
                return;
            }
        }
        
        verdict = "ACCEPTED";
    }
    
    public void run() {
        //reserve the port
        server.reservePort(submitPort);
        
        //Update verdicts in server
        server.setSubmissionVerdict(submissionId, "NOT ANSWERED YET");
        
        try {
            //Initialize corrector socket
            submitSocket = new ServerSocket(submitPort);
            submitSocket.setSoTimeout(ITAOJ.SOCKET_TIMEOUT);
            
            //Awaiting request
            System.out.println(CorrectorTag + " Awaiting code request");
            Socket connectionSocket = submitSocket.accept();
            System.out.println(CorrectorTag + " Code request found");

            //Setting up submit request communication
            connectionSocket.setSoTimeout(ITAOJ.SOCKET_TIMEOUT);
            inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            outToClient = new DataOutputStream(connectionSocket.getOutputStream());

            //Expects CodSub
            command = inFromClient.readLine();
            System.out.println(CorrectorTag + " Received: " + command);
            if (!command.equals(ITAOJ.CODE_SUB)) {
                throw new Exception("Expected command CodSub, received: " + command);
            }

            //Wait for code
            problemId = Integer.parseInt(inFromClient.readLine());
            numLines = Integer.parseInt(inFromClient.readLine());
            code = "";
            for (int i = 0; i < numLines; i++) {
                code = code + inFromClient.readLine() + "\n";
            }
            System.out.println(CorrectorTag + " Received " + numLines + " lines for problem " + problemId);
            
            //Sends CodAck
            outToClient.writeBytes(ITAOJ.CODE_ACK + "\n" + submissionId + "\n");
            
            //Corrects the solution
            submitSocket.setSoTimeout(ITAOJ.CORRECTION_TIMEOUT);
            connectionSocket.setSoTimeout(ITAOJ.CORRECTION_TIMEOUT);
            correct();
            submitSocket.setSoTimeout(ITAOJ.SOCKET_TIMEOUT);
            connectionSocket.setSoTimeout(ITAOJ.SOCKET_TIMEOUT);
            
            //Sends RemReq
            outToClient.writeBytes(ITAOJ.REMAINDER_REQ + "\n");

            //Expects RemAck
            command = inFromClient.readLine();
            System.out.println(CorrectorTag + " Received: " + command);
            if (!command.equals(ITAOJ.REMAINDER_ACK)) {
                throw new Exception("Expected command RemAck, received: " + command);
            }
            
            //Sends RemFin and verdict
            outToClient.writeBytes(ITAOJ.REMAINDER_FIN + "\n" + verdict + "\n");
            
            //Close code submit request communication
            outToClient.close();
            inFromClient.close();
            connectionSocket.close();
            submitSocket.close();
            
        } catch (Exception e) {
            System.out.println(CorrectorTag + " Exception: " + e.getMessage());
            server.freePort(submitPort);
            return;
        }
        
        //Update verdicts in server
        server.setSubmissionVerdict(submissionId, verdict);
        
        //free the port
        server.freePort(submitPort);
    }
    
}
