package com.example.locationtracker;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.DropBoxManager.Entry;
import android.widget.TextView;

public class Utils {
	public static String APP_UUID = "D4B19E36-DCBC-4E55-9742-9A228E007F06";
	public static String API_URL = "http://location.isi.jhu.edu/api/";
	public static final int GET_REQUEST = 1;
	public static final int POST_REQUEST = 2;
	
	public static Byte findMostCommon(ArrayList<Byte> list) {
	    Collections.sort(list);
	    Byte mostCommon = 0;
	    Byte last = null;
	    int mostCount = 0;
	    int lastCount = 0;
	    for (Byte x : list) {
	        if (x.equals(last)) {
	            lastCount++;
	        } else if (lastCount > mostCount) {
	            mostCount = lastCount;
	            mostCommon = last;
	        }
	        last = x;
	    }
	    return mostCommon;
	}
	
	public static void calculateRSSI(ArrayList<Beacon> beacons) {
		for(Beacon beacon:beacons)
		{
			//byte rssi = findMostCommon(beacon.getReceivedRSSIs());
			ArrayList<Byte> rssis = beacon.getReceivedRSSIs();
			if(rssis.size()>0)
			{
				Collections.sort(rssis);
				beacon.setRssi(rssis.get((int)(rssis.size()/2)));
			}
			else
			{
				beacon.setRssi(rssis.get(0));
			}
		}
			
	}
	
	public static void calculateDistance(ArrayList<Beacon> beacons)
	{
		for(Beacon beacon:beacons)
		{
			double ratio = beacon.getRssi()*1.0/beacon.getCalibratedPower();
			if (ratio < 1.0)
			{
			    beacon.setDistance((float)Math.pow(ratio,10));
			}
			else
			{
			    double accuracy =  (0.89976)*Math.pow(ratio,7.7095) + 0.111;    
			    beacon.setDistance((float)accuracy);
			}
		}
	}
	
	public static boolean sendData(ArrayList<Beacon> beacons, String token)
	{
		boolean success = false;
		
		String data = null;
		
		JSONArray ja = new JSONArray();
		
		for(Beacon b: beacons)
		{
			try{
				JSONObject jo = new JSONObject();
				jo.put("major",b.getMajor());
				jo.put("minor",b.getMinor());
				jo.put("mac",b.getMac());
				jo.put("rssi",b.getRssi());
				jo.put("distance",b.getDistance());
				ja.put(jo);
			}
			catch(JSONException e)
			{
				e.printStackTrace();
			}
		}
		
		data = ja.toString();
		
		HttpConnection con = null;
		try {
			con = new HttpConnection(Utils.API_URL+"post", HttpConnection.POST_REQUEST);
			con.POST(data, token, false);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		con.close();
		
		
		
		return success;
	}
	
	static final char[] hexArray = "0123456789ABCDEF".toCharArray();
	private static String bytesToHex(byte[] bytes) {
	    char[] hexChars = new char[bytes.length * 2];
	    for ( int j = 0; j < bytes.length; j++ ) {
	        int v = bytes[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(hexChars);
	}
	
	public static void updateBeaconList(ArrayList<Beacon> beacons, String address, int rssi, byte[] scanRecord)
	{
		int startByte = 2;
	    boolean patternFound = false;
	    while (startByte <= 5) {
	        if (    ((int) scanRecord[startByte + 2] & 0xff) == 0x02 && //Identifies an iBeacon
	                ((int) scanRecord[startByte + 3] & 0xff) == 0x15) { //Identifies correct data length
	            patternFound = true;
	            break;
	        }
	        startByte++;
	    }
		
	    if (patternFound) {
	        boolean beaconFound = false;
	        Beacon b = null;
	        for(Beacon beacon:beacons)
	        {
	        	if(address.equals(beacon.getMac()))
	        	{
	        		beaconFound = true;
	        		b = beacon;
	        		break;
	        	}
	        }
	        
	        if(beaconFound)
	        {
	        	b.getReceivedRSSIs().add(Byte.valueOf((byte) rssi));
	        }
	        else
	        {
	        	byte[] uuidBytes = new byte[16];
		        System.arraycopy(scanRecord, startByte+4, uuidBytes, 0, 16);
	        	String hexString = bytesToHex(uuidBytes);
		        String uuid =  hexString.substring(0,8) + "-" + 
		                hexString.substring(8,12) + "-" + 
		                hexString.substring(12,16) + "-" + 
		                hexString.substring(16,20) + "-" + 
		                hexString.substring(20,32);
		        int major = (scanRecord[startByte+20] & 0xff) * 0x100 + (scanRecord[startByte+21] & 0xff);
		        int minor = (scanRecord[startByte+22] & 0xff) * 0x100 + (scanRecord[startByte+23] & 0xff);
		        
		        if(uuid.equals(APP_UUID))
		        {
			        b = new Beacon();
			        b.setUuid(uuid);
			        b.setMac(address);
			        b.setMajor(major);
			        b.setMinor(minor);
			        b.getReceivedRSSIs().add(Byte.valueOf((byte) rssi));
			        b.setCalibratedPower(scanRecord[startByte+24]);
			        b.setScanRecord(scanRecord);
			        beacons.add(b);
		        }
	        }
	    }
	}
}
