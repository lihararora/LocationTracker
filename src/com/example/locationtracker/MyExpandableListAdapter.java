package com.example.locationtracker;

import java.util.ArrayList;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;
import android.widget.CheckedTextView;
import android.widget.Toast;

public class MyExpandableListAdapter extends BaseExpandableListAdapter {

  private ArrayList<Group> groups;
  public LayoutInflater inflater;
  public Activity activity;

  public MyExpandableListAdapter(Activity act, ArrayList<Group> groups2) {
    activity = act;
    this.groups = groups2;
    inflater = act.getLayoutInflater();
  }

  @Override
  public Object getChild(int groupPosition, int childPosition) {
    return groups.get(groupPosition).children.get(childPosition);
  }

  @Override
  public long getChildId(int groupPosition, int childPosition) {
    return 0;
  }

  @Override
  public View getChildView(int groupPosition, final int childPosition,
      boolean isLastChild, View convertView, ViewGroup parent) {
    final String children = (String) getChild(groupPosition, childPosition);
    TextView text = null;
    if (convertView == null) {
      convertView = inflater.inflate(R.layout.listrow_details, null);
    }
    text = (TextView) convertView.findViewById(R.id.CalibrateDescription);
    text.setText(children);
    return convertView;
  }

  @Override
  public int getChildrenCount(int groupPosition) {
    return groups.get(groupPosition).children.size();
  }

  @Override
  public Object getGroup(int groupPosition) {
	  try
	  {
		  return groups.get(groupPosition);
	  }
	  catch(Exception e)
	  {
		  e.printStackTrace();
		  return new Group();
	  }
  }

  @Override
  public int getGroupCount() {
    return groups.size();
  }

  @Override
  public void onGroupCollapsed(int groupPosition) {
    super.onGroupCollapsed(groupPosition);
  }

  @Override
  public void onGroupExpanded(int groupPosition) {
    super.onGroupExpanded(groupPosition);
  }

  @Override
  public long getGroupId(int groupPosition) {
    return 0;
  }

  @Override
  public View getGroupView(int groupPosition, boolean isExpanded,
	      View convertView, ViewGroup parent) {
	    if (convertView == null) {
	      convertView = inflater.inflate(R.layout.listrow_group, null);
	    }
	    Group group = (Group) getGroup(groupPosition);
        
        TextView tv2 = (TextView) convertView
                .findViewById(R.id.textView2);
        tv2.setText(Integer.toString(group.minor));
        
        TextView tv3 = (TextView) convertView
                .findViewById(R.id.RSSI1);
        tv3.setText(Double.toString(Math.round(group.distance*10000)/10000.0d));
        
        TextView tv5 = (TextView) convertView
                .findViewById(R.id.RSSI2);
        tv5.setText(Integer.toString((int)Math.round(group.rssi)));
	    return convertView;
	  }

  @Override
  public boolean hasStableIds() {
    return false;
  }

  @Override
  public boolean isChildSelectable(int groupPosition, int childPosition) {
    return false;
  }

	public void setGroups(ArrayList<Group> groups) {
		this.groups = groups;
	}
 
} 