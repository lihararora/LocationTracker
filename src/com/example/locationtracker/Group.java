package com.example.locationtracker;

import java.util.ArrayList;
import java.util.List;

public class Group {

  public int minor;
  public double distance;
  public double rssi;
  public final List<String> children = new ArrayList<String>();
  
  public Group(int minor, double d, double e) 
  {
	super();
	this.minor = minor;
	this.distance = d;
	this.rssi = e;
  }
  
  public Group() 
  {
	super();
	this.minor = 0;
	this.distance = 0;
	this.rssi = 0;
  }
} 