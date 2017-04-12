
public class Clock {
	private int numberOfProcesses;
	private  int[] timestamp;
	
	
	public void Clock(int pNum){
		//initialize clock vector with 
		//number of processes
		//3 processes = (0,0,0)
		numberOfProcesses = pNum;
		 int[] timestamp = new int[pNum];
		
		 for(int i=0; i<timestamp.length; ++i){
			 //fill timestamp vector with 0's
			 //this is the initial clock
			 timestamp[i] = 0;
		 }
	}
	
	public  Clock compareClocks(Clock c1, Clock c2){
		//compare the clocks of two clients
		//eg. P1|P2
		
		Clock update = new Clock();
		//create clock to return
		
		for(int i = 0; i<timestamp.length; ++i){
			//cycle through timestamp vector
			
			//fill update Clock with highest timestamps
			if(c1.timestamp[i]<c2.timestamp[i]){
				update.timestamp[i] = c2.timestamp[i];
			}
			else if(c1.timestamp[i]>c2.timestamp[i]){
				update.timestamp[i] = c1.timestamp[i];
			}
		}
		
		return update;
	}
	
	
	public void updateClock(int processID){
		//update process clock for event
		//if P2 recieves message, it's timestamp 
		//is incremented by one
		//eg. (0,0,++,0)
		
		timestamp[processID-1]++;
		
		//call this method after you compare the
		//two clocks between sender and reciever
		//or call this method just before sending 
		//a message to another client
	}
	
	public int[] getTimestamp(Clock clientNum){
		//You should create different instances 
		//of each client so that client has it's own Clock
		return clientNum.timestamp;
	}
}
