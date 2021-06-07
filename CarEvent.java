

import org.json.JSONException;
import org.json.JSONObject;
import java.util.List;
import org.json.JSONArray;
public class CarEvent
{
	protected static final String HEADER = "HEADER";
	protected static final String BODY = "BODY";
	protected JSONObject jsonObj = new JSONObject();
	
	public static void main(String args[])
	{
		JSONObject temp = new JSONObject();
		try
		{
			temp.put(HEADER, new JSONObject());

			JSONObject repli = new JSONObject(temp.toString());
			System.out.println(repli.toString());
		}
		catch(JSONException e)
		{
			System.err.println(e);
		}
		
		System.out.println(temp.toString());
		
	
	}
	
	
	
	
	public CarEvent()
	{
		try 
		{
			jsonObj.put(HEADER, new JSONObject());
			jsonObj.put(BODY, new JSONObject());
		} 
		catch (JSONException e) {
			System.err.println("error occurred while construcutor for carEvent");
			e.printStackTrace();
		}
		
	}
	
	public CarEvent(JSONObject msg)
	{
		this();
		try 
		{
			if((msg.getJSONObject(HEADER) != null) && (msg.getJSONObject(BODY) != null))
				jsonObj = msg;
		}
		catch(JSONException ex)
		{
			System.err.println(ex);
		}
	}
	
	public boolean isEmpty()
	{
		return jsonObj == null;
	}
	

	
	protected void put(String key, String value, String specifier)
	{
		JSONObject tempJson;
		try {
			tempJson = jsonObj.getJSONObject(specifier);
			tempJson.put(key, value);
		} catch (JSONException e) {
			System.err.println("error occurred while put" +specifier +"() \n");
			e.printStackTrace();
		}
		
	}
	
	public void putBody(String key, String value)
	{
		put(key, value, BODY);
	}
	
	public void putHeader(String key, String value)
	{
		put(key, value, HEADER);
	}
	
	protected String get(String key, String specifier)
	{
		try 
		{
			JSONObject temp =  (JSONObject) jsonObj.get(specifier);
			if(temp.has(key))
			{
				return temp.getString(key);
			}
		} 
		
		catch (JSONException e) 
		{
			System.err.println("error occurred while get" + specifier + "() \n");
			e.printStackTrace();
		}
		
		return null;
	}
	
	public String getHeader(String key)
	{
		return get(key, HEADER);
	}
	
	public String getBody(String key)
	{
		return get(key, BODY);
	}
	
	public String toString()
	{
		return jsonObj.toString();
	}
	
	protected JSONObject getJsonObject()
	{
		return jsonObj;
	}
}


