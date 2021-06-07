

import org.json.*;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;


public class RelayEvent extends CarEvent
{
	protected static final String EVENTLIST = "EVENTLIST";
	protected static final String CLIENTID = "CLIENTID";
	protected static final String REQNO = "REQNO";
	protected static final String IPADDR = "IPADDR";
	protected static final String SERVERID = "SERVERID";
	protected static final String PORTNUM = "PORTNUMB";

	public RelayEvent()
	{
		super();
	}
	
	public RelayEvent(JSONObject temp)
	{
		super(temp);
	}
	
    boolean putCarEvents(List<CarEvent> carEvents)
    {
        JSONArray tempEvents = new JSONArray();
        
        for(CarEvent eve: carEvents)
        {
            tempEvents.put(eve.jsonObj);
        }
        
        try 
        {
			((JSONObject)jsonObj.get(BODY)).put(EVENTLIST, tempEvents);
		}
        catch (JSONException e)
        {
			System.err.println("error occurred during putCarEvents");
			e.printStackTrace();
			return false;
		}
        return true;
    }
    
    LinkedList<CarEvent> getCarEvents()
    {
    	LinkedList<CarEvent> retList = new LinkedList<CarEvent>();
    	
    	try 
    	{
    		JSONObject body = jsonObj.getJSONObject(BODY);
    		JSONArray tempArr = body.getJSONArray(EVENTLIST);
    		
    		CarEvent temp;
    		
    		for(int ind = 0; ind < tempArr.length(); ind++)
    		{
    			temp = new CarEvent(tempArr.getJSONObject(ind));
    			if(!temp.isEmpty())
    			{
    				retList.add(temp);
    			}
    		}
    	}
    	catch(JSONException e)
    	{
    		System.err.println("error occurred in getCarEvents()");
    		System.err.println(e);
    		
    	}
    	
    	return retList;
    }
    
    boolean putClientId(List<String> clientId)
    {
    	try 
    	{
    		JSONObject body = jsonObj.getJSONObject(BODY);
    		JSONArray tempArr  = new JSONArray();
    		
    		for(String temp: clientId)
    		{
    			if(temp != null)
    			{
    				tempArr.put(temp);
    			}
    			
    		}
    		
    		body.put(CLIENTID, tempArr);
    		return true;
 
    	}
    	catch(JSONException e)
    	{
    		System.err.println("error occurred in putClientId()");
    		System.err.println(e);
    		
    	}
    	
    	return false;
    }
    
    LinkedList<String> getClientId()
    {
    	LinkedList<String> retList = new LinkedList<String>();
    	
    	try 
    	{
    		
    		JSONObject body = jsonObj.getJSONObject(BODY);
    		JSONArray clientArr = body.getJSONArray(CLIENTID);
    		for(int ind = 0; ind < clientArr.length(); ind++)
    		{
    			try 
    			{
    				retList.add((String) clientArr.get(ind));
    			}
    			catch(ClassCastException ex)
    			{
    				System.err.println("failed to cast " + clientArr.getDouble(ind));
    			}
    		}
    		
    	}
    	
    	catch(JSONException ex)
    	{
    		System.err.println("error occurred in getClientId() " + jsonObj);
    		System.err.println(ex);
    	}
    	
    	return retList;
    }
    
    boolean putReqNo(int reqNo)
    {
    	try 
    	{
    		
    		JSONObject header = jsonObj.getJSONObject(HEADER);
    		header.put(REQNO, reqNo);
    		return true;
    	}
    	
    	catch(JSONException ex)
    	{
    		System.err.println("error occurred in putReqNo " + jsonObj);
    		System.err.println(ex);
    	}
    	return false;
    }
    
    int getReqNo()
    {
    	int reqNo = -1;
    	try 
    	{
    		JSONObject header = jsonObj.getJSONObject(HEADER);
    		reqNo = header.getInt(REQNO);
    		
    	}
    	catch(JSONException ex)
    	{
    		System.err.println("error occurred in getReqNo() " + jsonObj);
    		System.err.println(ex);
    	}
    	
    	return reqNo;
    }
    
    boolean putServerId(String serverId)
    {

    	try 
    	{
    		JSONObject header = jsonObj.getJSONObject(HEADER);
    		header.put(SERVERID, serverId);
    		return true;
 
    	}
    	catch(JSONException e)
    	{
    		System.err.println("error occurred in putServerId() " + jsonObj);
    		System.err.println(e);
    		
    	}
    	
    	return false;
    }
    
    String getServerId()
    {
    	String serverId = null;
    	
    	try 
    	{
    		JSONObject header = jsonObj.getJSONObject(HEADER);
    		serverId = header.getString(SERVERID);
 
    	}
    	catch(JSONException e)
    	{
    		System.err.println("error occurred in getServerId() " + jsonObj);
    		System.err.println(e);
    		
    	}
    	
    	return serverId;
    	
    }
    
    boolean putIpAddr(String ipAddr)
    {
    	try 
    	{
    		JSONObject body = jsonObj.getJSONObject(BODY);
    		body.put(IPADDR, ipAddr);
    		return true;
 
    	}
    	catch(JSONException e)
    	{
    		System.err.println("error occurred in putIpAddr() " + jsonObj);
    		System.err.println(e);
    		
    	}
    	
    	return false;
    }
    
    String getIpAddr()
    {
    	String ipAddr = null;
    	
    	try 
    	{
    		JSONObject body = jsonObj.getJSONObject(BODY);
    		ipAddr = body.getString(IPADDR);
 
    	}
    	catch(JSONException e)
    	{
    		System.err.println("error occurred in getIpAddr() " + jsonObj);
    		System.err.println(e);
    		
    	}
    	
    	return ipAddr;
    }
    
    boolean putPortNum(int portNum)
    {
    	try 
    	{
    		JSONObject body = jsonObj.getJSONObject(BODY);
    		body.put(PORTNUM, portNum);
    		return true;
 
    	}
    	catch(JSONException e)
    	{
    		System.err.println("error occurred in putPortNum() " + jsonObj);
    		System.err.println(e);
    		
    	}
    	
    	return false;
    }
    
    int getPortNum()
    {
    	int portNumb = -1;
    	try 
    	{
    		JSONObject body = jsonObj.getJSONObject(BODY);
    		portNumb = body.getInt(PORTNUM);
 
    	}
    	catch(JSONException e)
    	{
    		System.err.println("error occurred in getPortNum() " + jsonObj);
    		System.err.println(e);
    		
    	}
    	
    	return portNumb;
    }
    
}