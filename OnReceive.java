

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.Semaphore;



import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import com.sun.jdi.event.Event;

class OnReceive extends Thread {
	
			private static final String CHARSET = "UTF-8";
			Socket socket;
			Networker networker;
			String endString;
			
			public OnReceive(Socket socket, Networker networker, String endString) 
			{
				this.socket = socket;
				this.networker = networker;
				this.endString = endString;
			}
			
			
			
			@Override
			public void run() {
				System.out.println("OnReceive 스레드 생성");
				BufferedReader in;
				
				try
				{
					int lastCount = 0;
					in = new BufferedReader(new InputStreamReader(socket.getInputStream(), CHARSET));
					while(lastCount > -1 && !networker.close) 
					{	
						
						
						char [] lastStr = new char[endString.length()];
						StringBuilder strBuilder = new StringBuilder(50);
						
						for( int ind = 0; ind < lastStr.length; ind++)
						{
							in.read(lastStr, ind, 1);
						}
						
						strBuilder.append(lastStr);
						int len = lastStr.length;
						
						while(!compareCharArrToString(lastStr)) 
						{
							shiftLeft(lastStr, len);
							lastCount = in.read(lastStr, len - 1 , 1);
							
							if(lastCount < 0)
								break;
							
							strBuilder.append(lastStr[len - 1]);
						}
						
						
						if(lastCount > -1 && !networker.close)
							networker.onRecieve(strBuilder.toString());
					}
				} 
				catch (Exception e)
				{
					// TODO: handle exception
					System.err.println("error in OnReceive run()");
					System.err.println(e);
					
					
				}
				finally
				{
					try
					{
						networker.close = true; 
						System.out.println("on Recieve: networker.close = " + networker.close);
						socket.shutdownInput();
						System.out.println("succeed in shutdownInput!");
					}
					catch(Exception ex)
					{
						System.err.println("error occurred while closing recieving socket!");
						System.err.println(ex);
					}
				}
			}
			
			private boolean compareCharArrToString(char [] arr)
			{
				if(arr.length != endString.length())
					return false;
				for(int i = 0; i < arr.length; i++)
				{
					if(arr[i] != endString.charAt(i))
						return false;
				}
				
				return true;
			}
			
			private void shiftLeft(char [] a, int lenToShift) {
			    for(int i = 1; i < lenToShift; i++ ) {
			    	a[i-1] = a[i];
			    }
			}
		}