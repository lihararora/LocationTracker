package com.example.locationtracker;

import java.util.UUID;

public class Beacon {
	public static String APP_UUID = "D4B19E36-DCBC-4E55-9742-9A228E007F06";
	
	private String uuid;
	private String mac;
	private int major;
	private int minor;
	private int rssi;
	private int count;
	private float distance;
	private int calibratedPower;
	
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
	public int getRssi() {
		return rssi;
	}
	public void setRssi(int rssi) {
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
	public float getDistance() {
		return distance;
	}
	public void setDistance(float distance) {
		this.distance = distance;
	}
}
