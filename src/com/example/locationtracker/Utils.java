package com.example.locationtracker;

import java.util.ArrayList;
import java.util.Arrays;

public class Utils {
	public static boolean sendData(ArrayList<Beacon> beacons)
	{
		boolean success = false;
		
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
	        	int count = b.getCount();
	        	b.setRssi(((b.getRssi()*count) + rssi)/(count+1));
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
		        
		        if(uuid.equals(Beacon.APP_UUID))
		        {
			        b = new Beacon();
			        b.setUuid(uuid);
			        b.setMac(address);
			        b.setMajor(major);
			        b.setMinor(minor);
			        b.setRssi(rssi);
			        b.setCount(1);
			        b.setCalibratedPower(scanRecord[startByte+24]);
			        beacons.add(b);
		        }
	        }
	    }
	}
}
