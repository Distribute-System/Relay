

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.Semaphore;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.*;
import java.util.concurrent.LinkedBlockingQueue;

class Networker 
{
		Socket receiverSocket;
		String id;
		volatile boolean close;
		NetworkerUser user;
		LinkedBlockingQueue<String> sendStrings;
		String endString; 
		
		public Networker(Socket socket, NetworkerUser use, String endString) 
		{
			// TODO Auto-generated constructor stub
			receiverSocket = socket;
			this.user = use;
			this.endString = endString;
			this.sendStrings = new LinkedBlockingQueue<String>();
			close = false;
			//클라이언트 측 MiddleWare로부터 요청 듣기 
			OnReceive onRecieve = new OnReceive(receiverSocket, this , endString);
			//클라이언트 측 MiddleWare로 값 보내기 
			Send send = new Send(receiverSocket, this);
			
			onRecieve.start();
			send.start();
		}
		

		
		
		void onTearedOff() 
		{
			LinkedList<String> aList = new LinkedList<String>();
			String temp =  null;
			
			System.err.println("on teared off called!");
			while(!(sendStrings.isEmpty()))
			{
				try {
					temp = sendStrings.take();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				aList.add(temp);
			}
			
			user.onTearedOff(id, aList, this);
		}
		
		
		void setId(String id){
			this.id = id;
		}
		
		void onRecieve(String string) 
		{
			if(user != null) 
			{
				user.onRecieve(string, this);
			} 
		}
		
		boolean send(String eventString)
		{
			try 
			{
				sendStrings.put(eventString);
			} 
			catch (InterruptedException e) 
			{
				System.out.println("error occurred while putting! " + eventString);
				e.printStackTrace();
				return false;
			}
			System.out.println("success to add sendString");
			return true;
		}
		
		void close()
		{
			close = true;
		}
		
	}