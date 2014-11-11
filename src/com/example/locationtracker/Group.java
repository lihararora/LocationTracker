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
} 