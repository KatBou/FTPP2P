/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.*;

import java.net.*;
import java.nio.file.Files;
import java.util.Scanner;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
/**
 *
 * @author jZimmerman
 */
public class ClientOne {
	static boolean runner=true;
	public static PrintWriter writer;//log
	static int cNum=0;
	static int pNum=0;
	 public static void runnersetter(boolean run){
			runner=run;
	 }
	 public static boolean runnergetter(){
			return runner;
	}
	
	public static void main(String[] args) throws IOException 
	{ 
		//start of log file
		 writer = new PrintWriter(new FileOutputStream("ClientLog.txt",true));
    	 java.util.Date date= new java.util.Date();
    	 System.out.println("ClientOne started at: " + date);
    	 
        Scanner sc = new Scanner(System.in);
		//Debugger Info
		Boolean Debug=true;
		
		//Controller Connection Information
		Socket contSocket = null;
        String contip = new String ("127.0.0.1");
        int contport = 51032;
        BufferedReader in = null;
        
        //Client Number Get
        System.out.println("What is your client Number?");
        cNum=sc.nextInt();
        System.out.println("How Many clients total will be joining?");
        pNum=sc.nextInt();
        //Socket Port
		int SOCKET_PORT=contport+cNum;//!!!!!CHANGES PER CLIENT!!!!!<contport+client Number>
		System.out.println("Client" + cNum + "started at: " + date);
        writer.print(new Timestamp(date.getTime()));
     	writer.println(" Client" + cNum + "started at: " + date);
     	writer.flush();
		
		//Opens Server Socket
		ServerSocket sSocket= new ServerSocket(SOCKET_PORT);
        
        //Connects to controller and provides listener
        contSocket = new Socket(contip,contport);
        in = new BufferedReader(new InputStreamReader(contSocket.getInputStream()));
        
        //WAIT() for Start Command
        while(!in.readLine().contains("start")){
        	
        }
        
        //Close Socket and listener
        contSocket.close();
        in.close();
        
        //Start Command Received From Controller
        //initialize clock
		Clock clock= new Clock();
		clock.startClock(10);
		
        if(Debug){
        System.out.println("Controller Sent Start Message");
        System.out.println(clock.toString());
        writer.print(new Timestamp(date.getTime()));
     	writer.println(": Controller Sent Start Message. "+clock.toString());
     	writer.flush();
        }
        
        //Server-Client Initialization
        listener ear = new listener(sSocket, clock,cNum);
        speaker mouth = new speaker(contip, contport, clock,cNum,pNum);
        //Server-Client Threads started
        ear.start();
        mouth.start();

	}
}class listener extends Thread{
	public static PrintWriter writer;//log
	
	//Variables passed from Main
	ServerSocket sSocket;
	Clock sClock= new Clock();
	int cNum;
	public listener(ServerSocket Socket, Clock clock,int cNum)
	{
		this.cNum=cNum;
		sClock=clock;
		sSocket = Socket;
	}public void run(){
		Boolean runner=true;
		
	 //CONNECT TO OTHER PEERS
		while(runner){
			try{
				writer = new PrintWriter(new FileOutputStream("ClientLog.txt",true));
				java.util.Date date= new java.util.Date();
				Socket socket= sSocket.accept();
				ClientThread cT = new ClientThread(socket,sClock,cNum);
				new Thread(cT).start();	
				writer.print(new Timestamp(date.getTime()));
		     	writer.println(": New peer Connected. "+sClock.toString());
		     	writer.flush();
			}catch(IOException exception){
				System.out.println("Error: " + exception);
			}
		}
	}
	
}class ClientThread implements Runnable
{
	public static PrintWriter writer;//log
    Socket threadSocket;
    boolean runner=true;
    Clock sClock= new Clock();
    //Gathers userInput for chatting to other Peers
    String userInput;
    //Other Users clock
    Clock oClock= new Clock();
    int cNum=0;
    //This constructor will be passed the socket
    public ClientThread(Socket socket,Clock clock,int cNum)
    {
        //Here we set the socket to a local variable so we can use it later
        this.cNum=cNum;
    	threadSocket = socket;
        sClock=clock;
        oClock = sClock;
    }
	@Override
	public void run() {
		
		try{
			writer = new PrintWriter(new FileOutputStream("ClientLog.txt",true));
			java.util.Date date= new java.util.Date();
			
		//Create output-input streams
		ObjectOutputStream output = new ObjectOutputStream(threadSocket.getOutputStream());  
        ObjectInputStream input = new ObjectInputStream(threadSocket.getInputStream());

        //Thread Runs Until Run Value == false
        boolean run=true;
        while (run) {
     	
        	//This will wait until an object is received.
        	//Message(Command, Filename, clock))
        	Object[] message= (Object[])input.readObject();
           userInput=(String) message[2];
           userInput=userInput.substring(1,userInput.length()-1);
           writer.print(new Timestamp(date.getTime()));
        	writer.println(": Waiting for object. "+sClock.toString());
        	writer.flush();
          // System.out.println(userInput);
           
           //Assigns Other persons clock
           String[] numbers = userInput.split("\\s*,\\s*");
           for(int i=0;i<numbers.length-1;i++){
        	   oClock.setTimestamp(i,Integer.parseInt(numbers[i]));
           }
           
           //NEED TO COMPARE CLOCKS
           synchronized(sClock){   
          sClock.compareClocks(oClock, sClock);    
          sClock.updateClock(cNum);//CHANGE PER CLIENT
           }
            writer.print(new Timestamp(date.getTime()));
       		writer.println(": Clocks synchronized. "+sClock.toString());
       		writer.flush();
           
           //assign message to File name
           String fileName=(String)message[1];
           fileName=fileName.trim().toLowerCase();
            writer.print(new Timestamp(date.getTime()));
       		writer.println(": Message assigned to file. "+sClock.toString());
       		writer.flush();
           //Check For file
           System.out.println(fileName);
           writer.print(new Timestamp(date.getTime()));
           writer.println(": Checking for file. "+sClock.toString());
           writer.flush();
	    	File f = new File("."); // current directory
	    	boolean hasIT=true;
			File[] files = f.listFiles();
			String s="";
			for (File file : files) {
				s=s+file.getCanonicalPath()+" ";
				s=s.toLowerCase();	
				}
			if (!(s.contains(fileName))) {
				hasIT=false;
				synchronized(sClock){
			          sClock.compareClocks(oClock, sClock);    
			          sClock.updateClock(cNum);//CHANGE PER CLIENT
				}
				threadSocket.close();
			}else if(hasIT==true){
				//UPDATE PER CLIENT
				synchronized(sClock){
					sClock.updateClock(cNum);//CHANGE PER CLIENT
				}
				Object[] response= new Object[]{"yes",fileName,sClock.toString()};
				//sleep random time
				Thread.sleep((long)(Math.random() * 100));
				output.writeObject(response);
				output.flush();
				writer.print(new Timestamp(date.getTime()));
	        	writer.println(": Peer has file. "+sClock.toString());
	        	writer.flush();
					//REQUEST TO DOWNLOAD
					Object[] fDownload= (Object[])input.readObject();
		           userInput=(String) fDownload[2];
		           userInput=userInput.substring(1,userInput.length()-1);
		           writer.print(new Timestamp(date.getTime()));
		        	writer.println(": Requesting download. "+sClock.toString());
		        	writer.flush();
		          // System.out.println(userInput);
		           
		           //Assigns Other persons clock
		           String[] fNumbers = userInput.split("\\s*,\\s*");
		           for(int i=0;i<fNumbers.length-1;i++){
		        	   oClock.setTimestamp(i,Integer.parseInt(fNumbers[i]));
		           }
		           
		           //NEED TO COMPARE CLOCKS
		           synchronized(sClock){   
		          sClock.compareClocks(oClock, sClock);    
		          sClock.updateClock(cNum);//CHANGE PER CLIENT
		           }
		           writer.print(new Timestamp(date.getTime()));
		        	writer.println(": Clocks synchronized. "+sClock.toString());
		        	writer.flush();
		           File file = new File(fileName);
		           
			       // Get length of file in bytes
		           long fSize = file.length();
		           Object[] res= new Object[]{"ack",fSize,sClock.toString()};
		          
		           //Send Back acknowledgement
		           System.out.println("file Size: "+res[1]);
		           output.writeObject(res);
		           writer.print(new Timestamp(date.getTime()));
		        	writer.println(": Send reponse. "+sClock.toString());
		        	writer.flush();
		           
		           //ACTUAL DOWNLOAD
		           Object[] finish=(Object[])input.readObject();
		           userInput=(String) finish[2];
		           userInput=userInput.substring(1,userInput.length()-1);
		           writer.print(new Timestamp(date.getTime()));
		        	writer.println(": File Downloaded. "+sClock.toString());
		        	writer.flush();
		          // System.out.println(userInput);
		           
		           //Assigns Other persons clock
		           String[] fs = userInput.split("\\s*,\\s*");
		           for(int i=0;i<fs.length-1;i++){
		        	   oClock.setTimestamp(i,Integer.parseInt(fs[i]));
		           }
		           output.writeObject(res);
		           //NEED TO COMPARE CLOCKS
		           synchronized(sClock){   
		          sClock.compareClocks(oClock, sClock);    
		          sClock.updateClock(cNum);//CHANGE PER CLIENT
		           }
		           res=new Object[]{"ack",fSize,sClock.toString()};
		           output.writeObject(res);
		           writer.print(new Timestamp(date.getTime()));
		        	writer.println(": Clocks synchronized. "+sClock.toString());
		        	writer.flush();
		        	
		           System.out.println("start: "+finish[3]);
		           writer.print(new Timestamp(date.getTime()));
		        	writer.println(": start:"+finish[3]+ ". "+sClock.toString());
		        	writer.flush();
		           int count=0;
        	       byte[] buffer = new byte[Integer.parseInt(res[1].toString())];
        	       System.out.println("File size sent "+finish[1]);
        	        writer.print(new Timestamp(date.getTime()));
               		writer.println(": File size sent"+finish[1]+" . "+sClock.toString());
               		writer.flush();
		          
        	      OutputStream out = threadSocket.getOutputStream();
         	      BufferedInputStream ins = new BufferedInputStream(new FileInputStream(file));
         	      int done=0;
         	      count = ins.read(buffer);
         	      if(Integer.parseInt(finish[3].toString())==0){
         	    	 done = Integer.parseInt(res[1].toString())-Integer.parseInt(finish[3].toString());
         	      }else{
         	    	  done=Integer.parseInt(res[1].toString())-Integer.parseInt(finish[3].toString());
         	      }
         	      System.out.println(Integer.parseInt(res[1].toString())-Integer.parseInt(finish[3].toString()));
         	      out.write(buffer, Integer.parseInt(finish[3].toString()), done);
         	      ins.close();
         	      threadSocket.close();
			}
        }
		}catch(IOException exception){
		} catch (ClassNotFoundException e) {
		} catch (InterruptedException e) {
		}
			
	}
	
}class speaker extends Thread{
	public static PrintWriter writer;//log
	
	//Variables passed from Main
	int pNum=0;
	int cNum=0;
	String eip;
	int eport;
	Clock sClock= new Clock();
	boolean waiting=false;
	Object[] message;
	Clock oClock= sClock;
	String fileName;
	boolean p1=true;
	boolean p2=true;
	boolean p3=true;
	boolean p4=true;
	boolean p5=true;
	boolean p6=true;
	boolean p7=true;
	boolean p8=true;
	boolean p9=true;
	boolean p10=true;
	Socket peer1,peer2,peer3,peer4,peer5,peer6,peer7,peer8,peer9,peer10;
	ObjectOutputStream out1,out2,out3,out4,out5,out6,out7,out8,out9,out10;
	ObjectInputStream in1,in2,in3,in4,in5,in6,in7,in8,in9,in10;
	static int pCount=5;//NUMBER OF PEERS-1;
	public speaker(String ip, int port, Clock clock,int cNum,int pNum){
		this.pNum=pNum;
		this.cNum=cNum;
		eip = ip;
		eport = port;
		sClock = clock;
		oClock = sClock;
	}public void run(){
		switch (cNum) {
			case 1:  p1 = false;
               		break;
			case 2: p2 = false;
					break;
			case 3: p3 = false;
					break;
			case 4: p4 = false;
					break;
			case 5:  p5 = false;
					break;
			case 6:  p6 = false;
					break;
			case 7:  p7 = false;
					break;
			case 8:  p8 = false;
					break;
			case 9:  p9 = false;
					break;
			case 10: p10 = false;
			break;
			default: break;
  }
		switch(pNum){
		case 1: p1=false; p2=false; p3=false; p4=false; p5=false; p6=false; p7=false; p8=false; p9=false;p10=false; break;
		case 2: p3=false; p4=false; p5=false; p6=false; p7=false; p8=false; p9=false;p10=false; break;
		case 3: p4=false; p5=false; p6=false; p7=false; p8=false; p9=false;p10=false; break;
		case 4: p5=false; p6=false; p7=false; p8=false; p9=false;p10=false; break;
		case 5: p6=false; p7=false; p8=false; p9=false;p10=false; break;
		case 6: p7=false; p8=false; p9=false;p10=false; break;
		case 7: p8=false; p9=false;p10=false; break;
		case 8: p9=false;p10=false; break;
		case 9: p10=false; break;
		default: break;
		}
		BufferedReader stdIn = new BufferedReader(
                new InputStreamReader(System.in));
		String userInput;
		
		try {	
			writer = new PrintWriter(new FileOutputStream("ClientLog.txt",true));
			java.util.Date date= new java.util.Date();
		//	Connect to all PCs
			if(p1){
				peer1 = new Socket(eip,eport+1);//!!!!!CHANGES PER CLIENT!!!!!<peer[ALLOTHERCLIENTS]> = <contport+[other client number]
				out1 = new ObjectOutputStream(peer1.getOutputStream());
				in1 = new ObjectInputStream(peer1.getInputStream());
			}if(p2){
				peer2 = new Socket(eip,eport+2);//!!!!!CHANGES PER CLIENT!!!!!<peer[ALLOTHERCLIENTS]> = <contport+[other client number]
				out2 = new ObjectOutputStream(peer2.getOutputStream());
				in2 = new ObjectInputStream(peer2.getInputStream());
			}if(p3){
				peer3 = new Socket(eip,eport+3);//!!!!!CHANGES PER CLIENT!!!!!<peer[ALLOTHERCLIENTS]> = <contport+[other client number]
				out3 = new ObjectOutputStream(peer3.getOutputStream());
				in3 = new ObjectInputStream(peer3.getInputStream());	
			}if(p4){
				peer4 = new Socket(eip,eport+4);//!!!!!CHANGES PER CLIENT!!!!!<peer[ALLOTHERCLIENTS]> = <contport+[other client number]
				out4 = new ObjectOutputStream(peer4.getOutputStream());
				in4 = new ObjectInputStream(peer4.getInputStream());	
			}if(p5){
				peer5 = new Socket(eip,eport+5);//!!!!!CHANGES PER CLIENT!!!!!<peer[ALLOTHERCLIENTS]> = <contport+[other client number]
				out5 = new ObjectOutputStream(peer5.getOutputStream());
				in5 = new ObjectInputStream(peer5.getInputStream());
			}if(p6){
				peer6 = new Socket(eip,eport+6);//!!!!!CHANGES PER CLIENT!!!!!<peer[ALLOTHERCLIENTS]> = <contport+[other client number]
			    out6 = new ObjectOutputStream(peer6.getOutputStream());
				in6 = new ObjectInputStream(peer6.getInputStream());	
			}if(p7){
				peer7 = new Socket(eip,eport+7);//!!!!!CHANGES PER CLIENT!!!!!<peer[ALLOTHERCLIENTS]> = <contport+[other client number]
				out7 = new ObjectOutputStream(peer7.getOutputStream());
				in7 = new ObjectInputStream(peer7.getInputStream());
			}if(p8){
				peer8 = new Socket(eip,eport+8);//!!!!!CHANGES PER CLIENT!!!!!<peer[ALLOTHERCLIENTS]> = <contport+[other client number]
				out8 = new ObjectOutputStream(peer8.getOutputStream());
				in8 = new ObjectInputStream(peer8.getInputStream());
			}if(p9){
				peer9 = new Socket(eip,eport+9);//!!!!!CHANGES PER CLIENT!!!!!<peer[ALLOTHERCLIENTS]> = <contport+[other client number]
				out9 = new ObjectOutputStream(peer9.getOutputStream());
				in9 = new ObjectInputStream(peer9.getInputStream());
			}if(p10){
				peer10 = new Socket(eip,eport+10);//!!!!!CHANGES PER CLIENT!!!!!<peer[ALLOTHERCLIENTS]> = <contport+[other client number]
				out10 = new ObjectOutputStream(peer10.getOutputStream());
				in10 = new ObjectInputStream(peer10.getInputStream());	
			}
			
			boolean open=true;
			
			
			System.out.println("Commands");
			System.out.println("1. download <filename>");
			
			
			while (open&&(userInput = stdIn.readLine()) !=  null  ) {
				//Commands for user to type to other users
				
				writer.print(new Timestamp(date.getTime()));
		 	    writer.println(" user input: "+userInput+". -"+sClock.toString());
		 	    writer.flush();
				
				//USER CHOOSES TO DOWNLOAD
				if(userInput.toLowerCase().startsWith("download")){
					
		 	    	fileName=userInput.substring(8, userInput.length());
		 	    	fileName=fileName.trim().toLowerCase();
		 	    	//UPDATES CLOCK BEFORE SENDING OuT MESSAGE
		 	    	synchronized(sClock){
		 	    		sClock.updateClock(cNum);	 	    		
		 	    	}
		 	    	//Keeps everything separated, into own elements
		 	        message= new Object[]{"download",fileName,sClock.toString()};
		 	    		 	  
		 	    	
		 	    	writer.print(new Timestamp(date.getTime()));
			 	    writer.println(": Download request sent---Clock updated. "+sClock.toString());
			 	    writer.flush();
		 	    	
		 	    	//tells program to read next line.
		 	    	waiting=true;
				}
				
				if(waiting){
					writer.print(new Timestamp(date.getTime()));
			 	    writer.println(": Waiting... "+sClock.toString());
			 	    writer.flush();
					int fSize=0;
					long endTime = System.currentTimeMillis() + 5000;
					while(System.currentTimeMillis() < endTime);
					
					//Peer 1
					if(p1){
						out1.writeObject(message);
						p1=peer(peer1,in1,out1,userInput,sClock,oClock,p1);	
						message= new Object[]{"download",fileName,sClock.toString()};
						}
					//PEER 2
					if(p2){
						out2.writeObject(message);
						p2=peer(peer2,in2,out2,userInput,sClock,oClock,p2);	
						message= new Object[]{"download",fileName,sClock.toString()};
						}
					//PEER 3
					if(p3){
						out3.writeObject(message);
						p3=peer(peer3,in3,out3,userInput,sClock,oClock,p3);
						message= new Object[]{"download",fileName,sClock.toString()};
						}
					//PEER 4
					if(p4){
						out4.writeObject(message);
						p4=peer(peer4,in4,out4,userInput,sClock,oClock,p4);
						message= new Object[]{"download",fileName,sClock.toString()};
						}
					//PEER 5
					if(p5){
						out5.writeObject(message);
						p5=peer(peer5,in5,out5,userInput,sClock,oClock,p5);
						message= new Object[]{"download",fileName,sClock.toString()};
						}
					//PEER 6
					if(p6){
						out6.writeObject(message);
						p6=peer(peer6,in6,out6,userInput,sClock,oClock,p6);
						message= new Object[]{"download",fileName,sClock.toString()};
						}
					//PEER 7
					if(p6){
						out7.writeObject(message);
						p7=peer(peer7,in7,out7,userInput,sClock,oClock,p7);
						message= new Object[]{"download",fileName,sClock.toString()};
						}
					//PEER 8
					if(p8){
						out8.writeObject(message);
						p8=peer(peer8,in8,out8,userInput,sClock,oClock,p8);
						message= new Object[]{"download",fileName,sClock.toString()};
						}
					//PEER 9
					if(p9){
						out9.writeObject(message);
						p9=peer(peer9,in9,out9,userInput,sClock,oClock,p9);
						message= new Object[]{"download",fileName,sClock.toString()};
						}
					//PEER 10
					if(p10){
						out10.writeObject(message);
						p10=peer(peer10,in10,out10,userInput,sClock,oClock,p10);
						message= new Object[]{"download",fileName,sClock.toString()};
						}//Send Another final download request
					synchronized(sClock){
						sClock.updateClock(cNum);
					}
					if(p2){
						Object[] ack=(Object[]) fpeer(sClock,oClock,fileName,peer2,2,eip,eport,in2,out2);
						if(fSize<Integer.parseInt(ack[1].toString())){
							fSize=Integer.parseInt(ack[1].toString());
						}
						}
					if(p3){
						Object[]ack=(Object[]) fpeer(sClock,oClock,fileName,peer3,3,eip,eport,in3,out3);
						if(fSize<Integer.parseInt(ack[1].toString())){
							fSize=Integer.parseInt(ack[1].toString());
						}
						}
					if(p4){
						Object[] ack=(Object[]) fpeer(sClock,oClock,fileName,peer4,4,eip,eport,in4,out4);
						if(fSize<Integer.parseInt(ack[1].toString())){
							fSize=Integer.parseInt(ack[1].toString());
						}
						}
					if(p5){
						Object[]ack=(Object[]) fpeer(sClock,oClock,fileName,peer5,5,eip,eport,in5,out5);
						if(fSize<Integer.parseInt(ack[1].toString())){
							fSize=Integer.parseInt(ack[1].toString());
						}
						}
					if(p6){
						Object[] ack=(Object[]) fpeer(sClock,oClock,fileName,peer6,6,eip,eport,in6,out6);
						if(fSize<Integer.parseInt(ack[1].toString())){
							fSize=Integer.parseInt(ack[1].toString());
						}
						}
					if(p7){
						Object[]ack=(Object[]) fpeer(sClock,oClock,fileName,peer7,7,eip,eport,in7,out7);
						if(fSize<Integer.parseInt(ack[1].toString())){
							fSize=Integer.parseInt(ack[1].toString());
						}
						}
					if(p8){
						Object[] ack=(Object[]) fpeer(sClock,oClock,fileName,peer8,8,eip,eport,in8,out8);
						if(fSize<Integer.parseInt(ack[1].toString())){
							fSize=Integer.parseInt(ack[1].toString());
						}
						}
					if(p9){
						Object[]ack=(Object[]) fpeer(sClock,oClock,fileName,peer9,9,eip,eport,in9,out9);
						if(fSize<Integer.parseInt(ack[1].toString())){
							fSize=Integer.parseInt(ack[1].toString());
						}
						}
					if(p10){
						Object[]ack=(Object[]) fpeer(sClock,oClock,fileName,peer10,10,eip,eport,in10,out10);
						if(fSize<Integer.parseInt(ack[1].toString())){
							fSize=Integer.parseInt(ack[1].toString());
						}
						}
					
					int start=0;
					int partNum=0;
							System.out.println("fsize= "+fSize);
			        	   int size=fSize/pCount;
			        	   writer.print(new Timestamp(date.getTime()));
					 	   writer.println(": Send final download request. "+sClock.toString());
					 	   writer.flush();
					 	   synchronized(sClock){
					 		   sClock.updateClock(cNum);
					 	   }
			        	   if(p2){
								Object[] m=new Object[]{"d",size,sClock.toString(),start};
								//SEND OUT FINAL Request for Download
								//CHANGE PER USER
								writer.print(new Timestamp(date.getTime()));
							 	writer.println(": Final download request sent to peer "+peer2.toString()+ " -"+sClock.toString());
							 	writer.flush();
								out2.writeObject(m);
								byte[] buffer = new byte[1024];
					 	    	int count=-1;
					 	    	InputStream ins =peer2.getInputStream();
					 	    	count=ins.read(buffer);
					 	    	System.out.println("count: "+count);
					 	    	if(count>0){
					 	    		writer.print(new Timestamp(date.getTime()));
								 	writer.println(": fileName: "+fileName+" . "+sClock.toString());
								 	writer.flush();
					 	    		System.out.println("fileName: "+fileName);
					 	    		FileOutputStream fos = new FileOutputStream(fileName.substring(0, fileName.length()-4)+partNum+".txt");
					 	    		partNum++;
					 	    		while((count  >= 0)){
					 	    			fos.write(buffer);
				 	    	    		fos.flush();
				 	    	    		count=ins.read(buffer);
					 	    		}
					 	    		peer2.close();
					 	 	    	fos.close();
					 	 	    	peer2 = new Socket(eip,eport+2);
					 	 	    	out2 = new ObjectOutputStream(peer2.getOutputStream());
					 	            in2 = new ObjectInputStream(peer2.getInputStream());
					 	            synchronized(sClock){
					 	            	sClock.updateClock(2);
					 	            }
					 	    	}
					 	    start=size+1;	
			        	   }if(p3){
			        		
								Object[] m=new Object[]{"d",size,sClock.toString(),start};
								
								//SEND OUT FINAL Request for Download
								//CHANGE PER USER
								out3.writeObject(m);
								byte[] buffer = new byte[1024];
					 	    	int count=-1;
					 	    	InputStream ins =peer3.getInputStream();
					 	    	count=ins.read(buffer);
					 	    	if(count>0){  
					 	    		System.out.println("count: "+count);
					 	    		FileOutputStream fos = new FileOutputStream(fileName.substring(0, fileName.length()-4)+partNum+".txt");
					 	    		partNum++;
					 	    		while((count  >= 0)){
					 	    			fos.write(buffer);
				 	    	    		fos.flush();
				 	    	    		count=ins.read(buffer);
					 	    		}
					 	    		peer3.close();
					 	 	    	fos.close();
					 	 	    	peer3 = new Socket(eip,eport+3);
					 	 	    	out3 = new ObjectOutputStream(peer3.getOutputStream());
					 	            in3 = new ObjectInputStream(peer3.getInputStream());
					 	           synchronized(sClock){
					 	            	sClock.updateClock(3);
					 	            }
					 	    	}
					 	    	start=size+1;
			        	   }
			        	   if(p4){
								Object[] m=new Object[]{"d",size,sClock.toString(),start};
								//SEND OUT FINAL Request for Download
								//CHANGE PER USER
								writer.print(new Timestamp(date.getTime()));
							 	writer.println(": Final download request sent to peer "+peer4.toString()+ " -"+sClock.toString());
							 	writer.flush();
								out4.writeObject(m);
								byte[] buffer = new byte[1024];
					 	    	int count=-1;
					 	    	InputStream ins =peer4.getInputStream();
					 	    	count=ins.read(buffer);
					 	    	System.out.println("count: "+count);
					 	    	if(count>0){
					 	    		writer.print(new Timestamp(date.getTime()));
								 	writer.println(": fileName: "+fileName+" . "+sClock.toString());
								 	writer.flush();
					 	    		System.out.println("fileName: "+fileName);
					 	    		FileOutputStream fos = new FileOutputStream(fileName.substring(0, fileName.length()-4)+partNum+".txt");
					 	    		partNum++;
					 	    		while((count  >= 0)){
					 	    			fos.write(buffer);
				 	    	    		fos.flush();
				 	    	    		count=ins.read(buffer);
					 	    		}
					 	    		peer4.close();
					 	 	    	fos.close();
					 	 	    	peer4 = new Socket(eip,eport+4);
					 	 	    	out4 = new ObjectOutputStream(peer4.getOutputStream());
					 	            in4 = new ObjectInputStream(peer4.getInputStream());
					 	            synchronized(sClock){
					 	            	sClock.updateClock(4);
					 	            }
					 	    	}
					 	    start=size+1;	
			        	   }
			        	   if(p5){
								Object[] m=new Object[]{"d",size,sClock.toString(),start};
								//SEND OUT FINAL Request for Download
								//CHANGE PER USER
								writer.print(new Timestamp(date.getTime()));
							 	writer.println(": Final download request sent to peer "+peer5.toString()+ " -"+sClock.toString());
							 	writer.flush();
								out5.writeObject(m);
								byte[] buffer = new byte[1024];
					 	    	int count=-1;
					 	    	InputStream ins =peer5.getInputStream();
					 	    	count=ins.read(buffer);
					 	    	System.out.println("count: "+count);
					 	    	if(count>0){
					 	    		writer.print(new Timestamp(date.getTime()));
								 	writer.println(": fileName: "+fileName+" . "+sClock.toString());
								 	writer.flush();
					 	    		System.out.println("fileName: "+fileName);
					 	    		FileOutputStream fos = new FileOutputStream(fileName.substring(0, fileName.length()-4)+partNum+".txt");
					 	    		partNum++;
					 	    		while((count  >= 0)){
					 	    			fos.write(buffer);
				 	    	    		fos.flush();
				 	    	    		count=ins.read(buffer);
					 	    		}
					 	    		peer5.close();
					 	 	    	fos.close();
					 	 	    	peer5 = new Socket(eip,eport+5);
					 	 	    	out5 = new ObjectOutputStream(peer5.getOutputStream());
					 	            in5 = new ObjectInputStream(peer5.getInputStream());
					 	            synchronized(sClock){
					 	            	sClock.updateClock(5);
					 	            }
					 	    	}
					 	    start=size+1;	
			        	   }
			        	   if(p6){
								Object[] m=new Object[]{"d",size,sClock.toString(),start};
								//SEND OUT FINAL Request for Download
								//CHANGE PER USER
								writer.print(new Timestamp(date.getTime()));
							 	writer.println(": Final download request sent to peer "+peer6.toString()+ " -"+sClock.toString());
							 	writer.flush();
								out6.writeObject(m);
								byte[] buffer = new byte[1024];
					 	    	int count=-1;
					 	    	InputStream ins =peer6.getInputStream();
					 	    	count=ins.read(buffer);
					 	    	System.out.println("count: "+count);
					 	    	if(count>0){
					 	    		writer.print(new Timestamp(date.getTime()));
								 	writer.println(": fileName: "+fileName+" . "+sClock.toString());
								 	writer.flush();
					 	    		System.out.println("fileName: "+fileName);
					 	    		FileOutputStream fos = new FileOutputStream(fileName.substring(0, fileName.length()-4)+partNum+".txt");
					 	    		partNum++;
					 	    		while((count  >= 0)){
					 	    			fos.write(buffer);
				 	    	    		fos.flush();
				 	    	    		count=ins.read(buffer);
					 	    		}
					 	    		peer6.close();
					 	 	    	fos.close();
					 	 	    	peer6 = new Socket(eip,eport+6);
					 	 	    	out6 = new ObjectOutputStream(peer6.getOutputStream());
					 	            in6 = new ObjectInputStream(peer6.getInputStream());
					 	            synchronized(sClock){
					 	            	sClock.updateClock(6);
					 	            }
					 	    	}
					 	    start=size+1;	
			        	   }
			        	   if(p7){
								Object[] m=new Object[]{"d",size,sClock.toString(),start};
								//SEND OUT FINAL Request for Download
								//CHANGE PER USER
								writer.print(new Timestamp(date.getTime()));
							 	writer.println(": Final download request sent to peer "+peer7.toString()+ " -"+sClock.toString());
							 	writer.flush();
								out7.writeObject(m);
								byte[] buffer = new byte[1024];
					 	    	int count=-1;
					 	    	InputStream ins =peer7.getInputStream();
					 	    	count=ins.read(buffer);
					 	    	System.out.println("count: "+count);
					 	    	if(count>0){
					 	    		writer.print(new Timestamp(date.getTime()));
								 	writer.println(": fileName: "+fileName+" . "+sClock.toString());
								 	writer.flush();
					 	    		System.out.println("fileName: "+fileName);
					 	    		FileOutputStream fos = new FileOutputStream(fileName.substring(0, fileName.length()-4)+partNum+".txt");
					 	    		partNum++;
					 	    		while((count  >= 0)){
					 	    			fos.write(buffer);
				 	    	    		fos.flush();
				 	    	    		count=ins.read(buffer);
					 	    		}
					 	    		peer7.close();
					 	 	    	fos.close();
					 	 	    	peer7 = new Socket(eip,eport+7);
					 	 	    	out7 = new ObjectOutputStream(peer7.getOutputStream());
					 	            in7 = new ObjectInputStream(peer7.getInputStream());
					 	            synchronized(sClock){
					 	            	sClock.updateClock(7);
					 	            }
					 	    	}
					 	    start=size+1;	
			        	   }
			        	   if(p8){
								Object[] m=new Object[]{"d",size,sClock.toString(),start};
								//SEND OUT FINAL Request for Download
								//CHANGE PER USER
								writer.print(new Timestamp(date.getTime()));
							 	writer.println(": Final download request sent to peer "+peer8.toString()+ " -"+sClock.toString());
							 	writer.flush();
								out8.writeObject(m);
								byte[] buffer = new byte[1024];
					 	    	int count=-1;
					 	    	InputStream ins =peer8.getInputStream();
					 	    	count=ins.read(buffer);
					 	    	System.out.println("count: "+count);
					 	    	if(count>0){
					 	    		writer.print(new Timestamp(date.getTime()));
								 	writer.println(": fileName: "+fileName+" . "+sClock.toString());
								 	writer.flush();
					 	    		System.out.println("fileName: "+fileName);
					 	    		FileOutputStream fos = new FileOutputStream(fileName.substring(0, fileName.length()-4)+partNum+".txt");
					 	    		partNum++;
					 	    		while((count  >= 0)){
					 	    			fos.write(buffer);
				 	    	    		fos.flush();
				 	    	    		count=ins.read(buffer);
					 	    		}
					 	    		peer8.close();
					 	 	    	fos.close();
					 	 	    	peer8 = new Socket(eip,eport+2);
					 	 	    	out8 = new ObjectOutputStream(peer8.getOutputStream());
					 	            in8 = new ObjectInputStream(peer8.getInputStream());
					 	            synchronized(sClock){
					 	            	sClock.updateClock(8);
					 	            }
					 	    	}
					 	    start=size+1;	
			        	   }
			        	   if(p9){
								Object[] m=new Object[]{"d",size,sClock.toString(),start};
								//SEND OUT FINAL Request for Download
								//CHANGE PER USER
								writer.print(new Timestamp(date.getTime()));
							 	writer.println(": Final download request sent to peer "+peer9.toString()+ " -"+sClock.toString());
							 	writer.flush();
								out9.writeObject(m);
								byte[] buffer = new byte[1024];
					 	    	int count=-1;
					 	    	InputStream ins =peer9.getInputStream();
					 	    	count=ins.read(buffer);
					 	    	System.out.println("count: "+count);
					 	    	if(count>0){
					 	    		writer.print(new Timestamp(date.getTime()));
								 	writer.println(": fileName: "+fileName+" . "+sClock.toString());
								 	writer.flush();
					 	    		System.out.println("fileName: "+fileName);
					 	    		FileOutputStream fos = new FileOutputStream(fileName.substring(0, fileName.length()-4)+partNum+".txt");
					 	    		partNum++;
					 	    		while((count  >= 0)){
					 	    			fos.write(buffer);
				 	    	    		fos.flush();
				 	    	    		count=ins.read(buffer);
					 	    		}
					 	    		peer9.close();
					 	 	    	fos.close();
					 	 	    	peer9 = new Socket(eip,eport+9);
					 	 	    	out9 = new ObjectOutputStream(peer9.getOutputStream());
					 	            in9 = new ObjectInputStream(peer9.getInputStream());
					 	            synchronized(sClock){
					 	            	sClock.updateClock(9);
					 	            }
					 	    	}
					 	    start=size+1;	
			        	   }
			        	   if(p10){
			        		   Object[] m=new Object[]{"d",size,sClock.toString(),start};
								//SEND OUT FINAL Request for Download
								//CHANGE PER USER
								writer.print(new Timestamp(date.getTime()));
							 	writer.println(": Final download request sent to peer "+peer10.toString()+ " -"+sClock.toString());
							 	writer.flush();
								out10.writeObject(m);
								byte[] buffer = new byte[1024];
					 	    	int count=-1;
					 	    	InputStream ins =peer10.getInputStream();
					 	    	count=ins.read(buffer);
					 	    	System.out.println("count: "+count);
					 	    	if(count>0){
					 	    		writer.print(new Timestamp(date.getTime()));
								 	writer.println(": fileName: "+fileName+" . "+sClock.toString());
								 	writer.flush();
					 	    		System.out.println("fileName: "+fileName);
					 	    		FileOutputStream fos = new FileOutputStream(fileName.substring(0, fileName.length()-4)+partNum+".txt");
					 	    		partNum++;
					 	    		while((count  >= 0)){
					 	    			fos.write(buffer);
				 	    	    		fos.flush();
				 	    	    		count=ins.read(buffer);
					 	    		}
					 	    		peer10.close();
					 	 	    	fos.close();
					 	 	    	peer10 = new Socket(eip,eport+2);
					 	 	    	out10 = new ObjectOutputStream(peer10.getOutputStream());
					 	            in10 = new ObjectInputStream(peer10.getInputStream());
					 	            synchronized(sClock){
					 	            	sClock.updateClock(10);
					 	            }
					 	    	}
					 	    start=size+1;	
			        	   }

			        	   BufferedOutputStream outs = new BufferedOutputStream(new FileOutputStream(fileName));
			        	   for(int y=0;y<pCount;y++){
			
			        		   String s=fileName.substring(0, fileName.length()-4)+y+".txt";
	
			      	    	BufferedInputStream ins = new BufferedInputStream(new FileInputStream(s));

			      	    	int count= 0;
			      	    	byte[] buffer= new byte[1024];
			      	    	
			      	    	count=ins.read(buffer);
			      	    	
			      	    	outs.write(buffer);
			      	    	outs.flush();
			        	   }
			      	    	
			        	   	
				}
				writer.print(new Timestamp(date.getTime()));
			 	writer.println(": Final Clock: "+ sClock.toString());
			 	writer.flush();
					

			       
			          
				}
				 
			
			
		} catch (UnknownHostException e1) {

		} catch (IOException e1) {

		} catch (ClassNotFoundException e) {

		} 	
	}
	public static Object fpeer(Clock sClock,Clock oClock,String fileName,Socket peer,int pNum,String eip,int eport,ObjectInputStream in,ObjectOutputStream out) throws IOException, ClassNotFoundException{
		
		writer = new PrintWriter(new FileOutputStream("ClientLog.txt",true));
		java.util.Date date= new java.util.Date();
		
		Object[] message=new Object[]{"fDownload",fileName,sClock.toString()};
		out.writeObject(message);
		
		Object[] ack=(Object[])in.readObject();
		  String userInputs=(String) ack[2];
           userInputs=userInputs.substring(1,userInputs.length()-1);
           //Assigns Other persons clock
           String[] fNumbers = userInputs.split("\\s*,\\s*");
           for(int i=0;i<fNumbers.length;i++){
        	   oClock.setTimestamp(i,Integer.parseInt(fNumbers[i]));
           }         
          
           //NEED TO COMPARE CLOCKS
           synchronized(sClock){
          sClock.compareClocks(sClock, oClock);
          sClock.updateClock(1);//CHANGE PER CLIENT
           }
          // ack=(Object[])in.readObject();
           writer.print(new Timestamp(date.getTime()));
	 	    writer.println(":Clocks synchronized with peer "+peer.toString() + "after file size received -"+sClock.toString());
	 	    writer.flush();
         return ack;
	}
	public static boolean peer(Socket peer,ObjectInputStream in,ObjectOutputStream out,String userInput,Clock sClock,Clock oClock,boolean p) throws IOException {
		try{
			writer = new PrintWriter(new FileOutputStream("ClientLog.txt",true));
			java.util.Date date= new java.util.Date();
			
			   Object[] message=(Object[])in.readObject();
			   
	           userInput=(String) message[2];
	           userInput=userInput.substring(1,userInput.length()-1);
	           String[] numbers = userInput.split("\\s*,\\s*");
	           for(int i=0;i<numbers.length;i++){
	        	   oClock.setTimestamp(i,Integer.parseInt(numbers[i]));
	           }
	           synchronized(sClock){
	          sClock.compareClocks(sClock, oClock);    
	           }
	           writer.print(new Timestamp(date.getTime()));
		 	    writer.println(":Clocks synchronized with peer "+peer.toString() + "for initial request -"+sClock.toString());
		 	    writer.flush();
			p=true;
		}catch(EOFException e){
			pCount--;
			p=false;
			peer.close();	
			in.close();
			out.close();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return p;
		}
}
