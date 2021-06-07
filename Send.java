

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
import java.util.concurrent.TimeUnit;

class Send extends Thread {
	private static final String CHARSET = "UTF-8";
		
			Socket socket;
			String outString;
			Networker networker;
			
			Send(Socket socket, Networker networker) {
				this.socket = socket;
				this.networker = networker;
			}
			
			@Override
			public void run() {
				System.out.println("Send 스레드 생성");
				BufferedWriter out;
				
				String msgToSend = null;
				
				try 
				{
					out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), CHARSET));
					
					
					
					
					while(socket.isConnected() && !networker.close) 
					{
						//링크리스트에 값이 있으면 실행
						//while(sendStrings.isEmpty());
						msgToSend = null;
						
						try 
						{
						  msgToSend = networker.sendStrings.poll(1, TimeUnit.SECONDS);
						  
						if(msgToSend == null)
							  continue;
						  if(socket.isConnected())
						  {  
							  out.write(msgToSend);
							  out.flush();
							  System.out.println("메세지를 서버에게 보냈습니다: " + msgToSend);
							  
						  }
						}
						catch(InterruptedException e)
						{
							System.out.println("error occurred during dequeue!");
							System.err.println(e);
						}
						
						
					}
				
				} 
				catch (Exception e) 
				{
					System.err.println("error in Send.run()");
					System.err.println(e);
					networker.close = true;
					
					if(msgToSend != null)          //보내는 도중에 끈겼으면 다시 보내야된다. 
						networker.send(msgToSend);
						
				}
				
				finally
				{
					try
					{
						socket.shutdownOutput();
					}
					catch(Exception ex)
					{
						System.err.println("error occurred during closing send socket!");
						System.err.println(ex);
					}
					
					networker.onTearedOff();
				
					
				}
			}
		}