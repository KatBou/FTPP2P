/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.*;

import java.net.*;
import java.sql.Timestamp;

//import echoserver.EchoServer;

 /*
 * @author jZimmerman
 */

public class controller {

	static boolean runner=true;
	
	//****************************//
	public static PrintWriter writer;
	
	 public static void runnersetter(boolean run){
			runner=run;
	 }
	 public static boolean runnergetter(){
			return runner;
	}

	public static void main(String[] args) throws IOException 
	{ 
		
		//***************************//
		int i=0;
	    try {
		writer = new PrintWriter(new FileOutputStream("ControllerLog.txt",true));
   	 	java.util.Date date= new java.util.Date();
        System.out.println("Controller started at: " + date); 
        //****************************//
		 
		//Socket Port--Changeable
		int SOCKET_PORT=51032;
		//Opens Server Socket
		ServerSocket sSocket= new ServerSocket(SOCKET_PORT);
		
		while(runner){
			Socket socket= sSocket.accept();
			ClientThread cT = new ClientThread(socket);
			new Thread(cT).start();	
			
			//******************************//
			if(i==0){
            	System.out.print(new Timestamp(date.getTime()));
             	System.out.println(": Client connected");
             	writer.print(new Timestamp(date.getTime()));
             	writer.println(": Client connected.");
             	writer.flush();
             //	writer.close();
             	i++;
            }
		}
			
		} catch(IOException exception) {
	        System.out.println("Error: " + exception);
	    }
	    //*****************************//
	}

}
class ClientThread implements Runnable
{
	public static PrintWriter writer;
	//Set Number of Peers + 1 ---Changeable
	public static int nPeers=11;
	
    Socket threadSocket;
    //Keep Active Until thread Reaches nPeers
    boolean runner=true;
    
    //This constructor will be passed the socket
    public ClientThread(Socket socket)
    {
        //Here we set the socket to a local variable so we can use it later
        threadSocket = socket;
    }
	@Override
	public void run() {
		
		try{
		//Create output stream
		//*******//
		writer = new PrintWriter(new FileOutputStream("ControllerLog.txt",true));
		java.util.Date date= new java.util.Date();
		//******//
        PrintWriter output = new PrintWriter(threadSocket.getOutputStream(), true);  
		boolean runner=true;
			while(runner){
				//Once threadCount = nPeers Send Start Message
				if(java.lang.Thread.activeCount()==nPeers){
					System.out.println("Enter with thread counter with threadcount: " + java.lang.Thread.activeCount());
					output.println("start ");
					writer.print(new Timestamp(date.getTime()));
	             	writer.println(": Enter with thread counter with threadcount: " + java.lang.Thread.activeCount());
	             	writer.flush();
					runner=false;
					threadSocket.close();
					output.close();
					
					System.out.println("Exit with thread counter with threadcount: " + java.lang.Thread.activeCount());
					//No clue what this does but it somehow keeps activeCount correct so Controller Can always run.
					writer.print(new Timestamp(date.getTime()));
	             	writer.println(": Exit with thread counter with threadcount: " + java.lang.Thread.activeCount());
	             	writer.flush();
					java.lang.Thread.getAllStackTraces();
					
				}
			}
		}catch(IOException exception){
			System.out.println("Error: " + exception);
		}
			
	}
	
}
		
	
