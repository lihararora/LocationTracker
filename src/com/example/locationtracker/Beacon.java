package com.example.locationtracker;

import java.util.ArrayList;
import java.util.UUID;

public class Beacon implements Comparable {
	
	private String uuid;
	private String mac;
	private int major;
	private int minor;
	private ArrayList<Byte> receivedRSSIs = new ArrayList<Byte>();
	private byte rssi;
	private double distance;
	private int calibratedPower;
	private byte[] scanRecord;
	
	 @Override
    public int compareTo(Object newBeacon) {
        double newDistance=((Beacon)newBeacon).getDistance();
        /* For Ascending order*/
        return (int)(this.distance-newDistance);

    }
	
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public String getMac() {
		return mac;
	}
	public void setMac(String mac) {
		this.mac = mac;
	}
	public int getMajor() {
		return major;
	}
	public void setMajor(int major) {
		this.major = major;
	}
	public int getMinor() {
		return minor;
	}
	public void setMinor(int minor) {
		this.minor = minor;
	}
	public byte getRssi() {
		return rssi;
	}
	public int getCalibratedPower() {
		return calibratedPower;
	}
	public void setCalibratedPower(int calibratedPower) {
		this.calibratedPower = calibratedPower;
	}
	public double getDistance() {
		return distance;
	}
	public void setDistance(double distance) {
		this.distance = distance;
	}
	public byte[] getScanRecord() {
		return scanRecord;
	}
	public void setScanRecord(byte[] scanRecord) {
		this.scanRecord = scanRecord;
	}
	public ArrayList<Byte> getReceivedRSSIs() {
		return receivedRSSIs;
	}

	public void setReceivedRSSIs(ArrayList<Byte> receivedRSSIs) {
		this.receivedRSSIs = receivedRSSIs;
	}

	public void setRssi(byte rssi) {
		this.rssi = rssi;
	}
}
