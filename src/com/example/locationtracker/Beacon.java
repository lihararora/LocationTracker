package com.example.locationtracker;

import java.util.UUID;

public class Beacon {
	
	private String uuid;
	private String mac;
	private int major;
	private int minor;
	private double rssi;
	private int count;
	private double distance;
	private int calibratedPower;
	private byte[] scanRecord;
	
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
	public double getRssi() {
		return rssi;
	}
	public void setRssi(double rssi) {
		this.rssi = rssi;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
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
}
