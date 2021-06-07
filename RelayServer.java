

import java.util.Map;
import java.util.UUID;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.json.JSONException;
import org.json.JSONObject;

public class RelayServer implements NetworkerUser
{
	
	public static void main(String [] args)
	{
	    RelayServer relServer = new RelayServer();
	    relServer.startProcessingIncomingMsgs();
	}
	
	private static final String PROTOCOL_END = "\r\n"; //String that inidicates end of msg
	private static final String ID = "ID";
	
	private static final int PORTNUM = 6000; //포트넘버입니다. 바꾸시고 싶으면 바꾸세요.
	
	//reqNo for protocol. For Relay -> Server msg
	private static final int SEND_SET_SERVER_ID = 0;
	private static final int SEND_SEND_REQ = 1;
	private static final int SEND_RELAY_REDIRECTION = 2;
	
	//reqNo for protocol. For Server -> Relay msg
	private static final int RECIEVE_NEW_CLIENT = 0;
	private static final int RECIEVE_REMOVE_CLIENT = 1;
	private static final int RECIEVE_UNSENT_EVENTLIST = 2;
	private static final int RECIEVE_SERVER_ID_REQ = 3;
	
	//Data structures used to process incoming msgs.
	private HashMap<String, String> clientToServer;
	private HashMap<String, List<CarEvent>> clientToEventList;
	private HashMap<String, Networker> serverToNetworker;
	
	//blocking queues for incoming msgs and unsent msgs
	private LinkedBlockingQueue<RelayEvent> msgQueue;
	private LinkedBlockingQueue<RelayEvent> unsentMsgQueue;


	//mutex for serverToNetworker HashMap since it will be accessed by multiple threads.
	private Mutex mutForServerToNetworker;  
	
	//Threads that will accept new server connection
	
	public RelayServer()
	{
		clientToServer = new HashMap<String, String>();
		clientToEventList = new HashMap<String, List<CarEvent>>();
		serverToNetworker = new HashMap<String, Networker>();
		msgQueue = new LinkedBlockingQueue<RelayEvent>();
		mutForServerToNetworker = new Mutex(1);
		
		//pass serverSocket to different thread. The thread will run infinite loop to accept new server.
		try  
		{
			AcceptThread temp = new AcceptThread(new ServerSocket(PORTNUM));
			temp.start();
		}
		catch (IOException e) 
		{
			System.err.println("failed to create a socket on port" + PORTNUM);
			e.printStackTrace();
		}
	}
	
	//start taking incoming msgs and process them. It is blocking method.
	public void startProcessingIncomingMsgs()
	{
		System.out.println("start processing!");
		RelayEvent tempRelayEvent;
		
		while(true)
		{
			try 
			{
				tempRelayEvent = msgQueue.take();
				analyzeMsg(tempRelayEvent);
			} 
			catch (InterruptedException e) 
			{
				System.err.println("uninterruptted while polling for next relayEvent!");
				e.printStackTrace();
			}
		}
	}
	
	public void onRecieve(String msg, Networker networker)
	{
		RelayEvent temp = converToRelayEvent(msg);
		if(temp.getReqNo() == RECIEVE_SERVER_ID_REQ)   //if server requested serverId again
		{
			String msgToSend = sendInitMsg(networker.id);
			networker.send(msgToSend);
			return;
		}
		
		try 
		{
			msgQueue.put(temp);
			System.out.println("successfully queued " + msg);
		} 
		catch (InterruptedException e) {
			System.err.println("error occurred during putting msg in msgQueue!");
			System.err.println("msg is: " + msg);
			e.printStackTrace();
		}
	}
	
	public void onTearedOff(String serverId, List<String> unSentMsgs, Networker networker)
	{
		deleteFromServerToNetworker(serverId);
		
		for(String unSentMsg: unSentMsgs)
		{
			try 
			{
				unsentMsgQueue.put(converToRelayEvent(unSentMsg));
			}
			catch (InterruptedException e) 
			{
				System.err.println("error occurred while putting unsentMsg!");
				e.printStackTrace();
			}
		}
		
		//I did not write code for processing unsent RelayEvents.
		//For now, I just put them in a separate blocking queue.
		//I recommend you to process them while processing incoming msgs from servers.
	}
	
	//Following methods are for making msgs to send to servers.
	private String sendInitMsg(String serverId)
	{
		RelayEvent relEvent = new RelayEvent();
		relEvent.putReqNo(SEND_SET_SERVER_ID);
		relEvent.putServerId(serverId);
		
		return convertToString(relEvent);
	}
	
	private String sendRequestMsg(List<CarEvent> carEvents)
	{
		RelayEvent relEvent = new RelayEvent();
		relEvent.putReqNo(SEND_SEND_REQ);
		relEvent.putCarEvents(carEvents);
		return convertToString(relEvent);
	}
					
	//make servers to connect to another relay server just in case.	
	private String sendRelayRedirection(String ipAddr, int portNumb)
	{
		RelayEvent relEvent = new RelayEvent();
		relEvent.putReqNo(SEND_RELAY_REDIRECTION);
		relEvent.putIpAddr(ipAddr);
		relEvent.putPortNum(portNumb);
		
		return convertToString(relEvent);
	}
	
	//Method to process message recieved from servers.
	private void analyzeMsg(RelayEvent aEvent) // 새로운 종류의 ReqNo가 있으면 여기서 추가하시면 됩니다. 
	{
		System.out.println("started to analyzeMsg: " + aEvent);
		if(aEvent == null)
			return;
		switch(aEvent.getReqNo())
		{
		case RECIEVE_NEW_CLIENT:   //server got new clients connected
			System.out.println("receive new client request!");
			processAddedClients(aEvent.getServerId(), aEvent.getClientId());
			break;
		case RECIEVE_REMOVE_CLIENT: // server got some clients lost
			System.out.println("recieve removed client");
			processRemovedClients(aEvent.getClientId());
			break;
		case RECIEVE_UNSENT_EVENTLIST: //server passed unsent CarEvents
			System.out.println("receive unsent eventlist");
			processUnSentEvent(aEvent.getCarEvents());
			break;
		}	
	}
	
	//Following methods process msgs based on reqNo.
	private void processUnSentEvent(List<CarEvent> carEvents)
	{
		String tempClientId;
		String tempServerId;
		String tempMsgToSend;
		LinkedList<CarEvent> tempCarEventListToSend = new LinkedList<CarEvent>();
		
		for(CarEvent event: carEvents)
		{
			tempClientId = event.getHeader(ID);
			if(clientToServer.containsKey(tempClientId)) //there is a server connected to this client
			{
				tempServerId = clientToServer.get(tempClientId);
				if(serverToNetworker.containsKey(tempServerId))
				{
					tempCarEventListToSend.add(event);
					tempMsgToSend = sendRequestMsg(tempCarEventListToSend);
					tempCarEventListToSend.clear();  // clear LinkedList for next!
					
					if(serverToNetworker.get(tempServerId).send(tempMsgToSend));
					{
						System.out.println("sent msg: " + tempMsgToSend + " to " + tempServerId);
						continue; //if it was successful to send, move on
								  //if above condition fails, it means that socket is disconnected
								  //but networker did not call onTearedOff() yet
					}
					
				}
				
				clientToServer.remove(tempClientId); //server is not connected to client anymore, so delete
			}
			
			if(!clientToEventList.containsKey(tempClientId))
			{
				clientToEventList.put(tempClientId, new LinkedList<CarEvent>());
			}
			
			clientToEventList.get(tempClientId).add(event);
			System.out.println("current clientToEventList for key " + tempClientId);
			
			for(CarEvent temp: clientToEventList.get(tempClientId))
			{
				System.out.println(temp);
			}
			
		}
	}
	
	private void processAddedClients(String serverId, List<String> clientIds)
	{
		System.out.println("new client added from: " + serverId );
		String tempMsgToSend;
		String tempClientId;
		Networker networker = serverToNetworker.get(serverId);
		
		if(networker == null)
		{
			System.out.println("Networker for serverId " + serverId + " is null");
			return;
		}
		
		
		for(int ind = 0; ind < clientIds.size(); ind++)
		{
			tempClientId = clientIds.get(ind);
			clientToServer.put(tempClientId, serverId);
			
			if(clientToEventList.containsKey(tempClientId))
			{
				
				tempMsgToSend = sendRequestMsg(clientToEventList.get(tempClientId));
				
				if(!networker.send(tempMsgToSend))
				{
					removeFromClientToServer(clientIds, ind); //remove added clientIds in clientToServer														  //since server is no longer connected!
					return;
				}
				
				System.out.println("succeeded to send msg: " + tempMsgToSend + " to serverId");
				clientToEventList.remove(tempClientId);
				 									//it means that connection is teared off
													//but networker did not call onTearedOff() yet
			}
			
		}
	}
	
	private void removeFromClientToServer(List<String> clientIds, int endInd)
	{	
		for(int ind = 0; ind <= endInd; endInd++)
		{
			
			clientToServer.remove(clientIds.get(ind));
		}
	}
	
	private void processRemovedClients(List<String> clientIds)
	{
		for(String clientId: clientIds)
		{
			clientToServer.remove(clientId);
		}
	}
	
	//other private methods used by RelayServer internally
	private String convertToString(CarEvent aEvent)
	{
		String temp = aEvent.toString();
		if(temp != null)
			temp = temp + PROTOCOL_END;
		
		return temp;
	}
	
	private RelayEvent converToRelayEvent(String msg)
	{
		RelayEvent relEvent = null;
		
		try
		{
			msg = msg.substring(0, msg.length() - PROTOCOL_END.length());
			JSONObject tempJsonObj = new JSONObject(msg);
			relEvent = new RelayEvent(tempJsonObj);
			if(relEvent.isEmpty())
				relEvent = null;
		}
		catch(JSONException e)
		{
			System.err.println("error occurred during converToRelayEvent! ");
			System.err.println(e);
		}
		
		return relEvent;
	}
	
	
	private <T> UUID getUniqueUUID(Map<String, T> aMap) 
	{
		UUID temp = UUID.randomUUID();
		
		while(aMap.containsKey(temp.toString()))
		{
			temp = UUID.randomUUID();
		}
		
		return temp;
	}
	
	private boolean addToServerToNetworker(String serverId, Networker networker)
	{
		try 
		{
			mutForServerToNetworker.acquire();
			serverToNetworker.put(serverId, networker);
			mutForServerToNetworker.release();
		} 
		catch (InterruptedException e) 
		{
			System.err.println("unable to acquire mutex in addToServerToNetworker, serverId: " + serverId);
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	private boolean deleteFromServerToNetworker(String serverId)
	{
		try 
		{
			mutForServerToNetworker.acquire();
			serverToNetworker.remove(serverId);
			mutForServerToNetworker.release();
		} 
		catch (InterruptedException e) 
		{
			System.err.println("unable to acquire mutex in deleteFromServerToNetworker, serverId: " + serverId);
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	class AcceptThread extends Thread
	{
		ServerSocket serverSocket;
		
		public AcceptThread(ServerSocket serverSocket) {
			this.serverSocket = serverSocket;
		}
		
		
		public void run()
		{
			Socket socket = null; 
			try {
				
			System.out.println("start accepting");
			
			while(true) 
			{
				socket = serverSocket.accept();
				
				
				String temp = getUniqueUUID(serverToNetworker).toString();
				Networker tempNetworker = new Networker(socket, RelayServer.this , PROTOCOL_END);
		
				while(!addToServerToNetworker(temp, tempNetworker));
		
				System.out.println(serverToNetworker.get(temp));
	
				tempNetworker.setId(temp);
				tempNetworker.send(sendInitMsg(temp));
			
				
			}
		} catch (IOException e)
		{
			System.err.println("Error occurred in creating a connection socket");
		}
			
		}

		
	}
}
