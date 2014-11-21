package com.example.locationtracker;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Formatter;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends Activity {

	private BluetoothAdapter mBluetoothAdapter;
	private ArrayList<Beacon> beacons = null;
	private ExpandableListView listView;
	private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private String[] menuItems = {"Scan Beacons", "Calibrate", "Log Out"};
	private MyExpandableListAdapter adapter;
	private String token;
	private CharSequence mTitle = "Location Tracker: Scan";
	private String[] mNavigationDrawerItemTitles ={"Scan Beacons", "Calibrate Beacons", "Location Tracker: Log Out"};
	private int currentCalibratingBeacon = 0;
	Timer mTimer;
    
 // Stops scanning after 10 seconds.
    private static final long REFRESH_TIME = 5000;
    private static final int REQUEST_ENABLE_BT = 1;
    
    ArrayList<Group> groups = new ArrayList<Group>();
    
    public class ScanFragment extends Fragment {
    	 
        public ScanFragment() {
        }
     
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
     
            View rootView = inflater.inflate(R.layout.fragment_scan, container, false);
            
            return rootView;
        }
        
        @Override
        public void onViewCreated(View view, Bundle savedInstanceState)
        {
        	MainActivity.this.listView = (ExpandableListView) view.findViewById(R.id.listView);
        	MainActivity.this.adapter = new MyExpandableListAdapter(this.getActivity(),groups);
    		listView.setAdapter(adapter);
    		MainActivity.this.runNotifyThread();
    		
    		MainActivity.this.scanLeDevice(true);
        }
     
    }
    
    public class CalibrateFragment extends Fragment {
   	 
        public CalibrateFragment() {
        }
     
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
     
            View rootView = inflater.inflate(R.layout.fragment_calibrate, container, false);
            
            MainActivity.this.scanLeDevice(false);
            
            return rootView;
        }
        
        @Override
        public void onViewCreated(View view, Bundle savedInstanceState)
        {
        	Spinner spinner = (Spinner) view.findViewById(R.id.spinner1);
        	spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

				@Override
				public void onItemSelected(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					// TODO Auto-generated method stub
					if(arg2 > 0)
					{
						Button step1 = (Button) MainActivity.this.findViewById(R.id.btnStep1);
						step1.setVisibility(Button.VISIBLE);
						MainActivity.this.currentCalibratingBeacon = arg2;
						
						step1.setOnClickListener(new View.OnClickListener() {
							
							@Override
							public void onClick(View v) {
								// Calculate RSSI at 1 m
								
								TextView rssi1 = (TextView) MainActivity.this.findViewById(R.id.RSSI1);
								rssi1.setText("-xx");
								
								Button step2 = (Button) MainActivity.this.findViewById(R.id.btnStep2);
								step2.setVisibility(Button.VISIBLE);
								
								Button step1 = (Button) MainActivity.this.findViewById(R.id.btnStep1);
								step1.setVisibility(Button.GONE);
								
								step2.setOnClickListener(new View.OnClickListener() {
									
									@Override
									public void onClick(View v) {
										// Calculate RSSI at 2 m
										
										TextView rssi2 = (TextView) MainActivity.this.findViewById(R.id.RSSI2);
										rssi2.setText("-xx");
										
										Button step3 = (Button) MainActivity.this.findViewById(R.id.btnStep3);
										step3.setVisibility(Button.VISIBLE);
										
										Button step2 = (Button) MainActivity.this.findViewById(R.id.btnStep2);
										step2.setVisibility(Button.GONE);
										
										step3.setOnClickListener(new View.OnClickListener() {
											
											@Override
											public void onClick(View v) {
												// Update server
												
												
												Spinner spinner = (Spinner) MainActivity.this.findViewById(R.id.spinner1);
												spinner.setSelection(0);
												
												TextView rssi1 = (TextView) MainActivity.this.findViewById(R.id.RSSI1);
												rssi1.setText("-");
												
												TextView rssi2 = (TextView) MainActivity.this.findViewById(R.id.RSSI2);
												rssi2.setText("-");
												
												Button step3 = (Button) MainActivity.this.findViewById(R.id.btnStep3);
												step3.setVisibility(Button.GONE);
												
												Toast toast = Toast.makeText(v.getContext(), "Calibration Done!", Toast.LENGTH_SHORT);
							        			toast.show();
											}
										});
									}
								});
							}
						});
					}
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
					// TODO Auto-generated method stub
					
				}
        		
			});
        }
    }
    
    /** Swaps fragments in the main content view */
    private void selectItem(int position) {
        // Create a new fragment and specify the planet to show based on position
    	Fragment fragment = null;
        
        switch (position) {
        case 0:
            fragment = new ScanFragment();
            break;
        case 1:
            fragment = new CalibrateFragment();
            break;
            
        case 2:
        	final SharedPreferences sp = this.getSharedPreferences("com.example.locationtracker", Context.MODE_PRIVATE);
    	    final String tokenKey = "com.example.locationtracker.token";
    	    sp.edit().remove(tokenKey).commit();
    	    Intent intent = new Intent (getApplicationContext(), LoginActivity.class);
		    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		    startActivity(intent);
		    finish(); // Call once you redirect to another activity
        	break;
     
        default:
            break;
        }
        
        if (fragment != null) {
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
     
            mDrawerList.setItemChecked(position, true);
            mDrawerList.setSelection(position);
            getActionBar().setTitle(mNavigationDrawerItemTitles[position]);
            mDrawerLayout.closeDrawer(mDrawerList);        
            
        } else {
            Log.e("MainActivity", "Error in creating fragment");
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }
    
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            selectItem(position);
        }
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      
      final SharedPreferences sp = this.getSharedPreferences("com.example.locationtracker", Context.MODE_PRIVATE);
	  final String tokenKey = "com.example.locationtracker.token";
	  token = sp.getString(tokenKey, null);
	  
	  if(token == null)
	  {
		  Intent intent = new Intent (getApplicationContext(), LoginActivity.class);
		  intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		  startActivity(intent);
		  finish(); // Call once you redirect to another activity
	  }
      
      setContentView(R.layout.activity_main);
      
      
      mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
      mDrawerList = (ListView) findViewById(R.id.left_drawer);

      // Set the adapter for the list view
      
      
      mDrawerList.setAdapter(new ArrayAdapter<String>(this,
             R.layout.drawer_list_item, menuItems));
      
      mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
      
      
      
      // Set the list's click listener
      //mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
      
      final BluetoothManager bluetoothManager =
		        (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();
		
		if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
		    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}
		
		beacons = new ArrayList<Beacon>();
		
		selectItem(0);
    }

    public void createData() {
    	ArrayList<Group> g = new ArrayList<Group>();
    	Collections.sort(beacons);
    	for (Beacon b : beacons) 
    	{  
    		Formatter formatter = new Formatter();
    		ArrayList<String> subDetails = new ArrayList<String>();
    		subDetails.add("MAC: "+b.getMac());
    		subDetails.add("Major: "+b.getMajor()+" Minor: "+b.getMinor());
    		byte[] byteArray = b.getScanRecord();
    		for (byte by : byteArray) {
    		    formatter.format(" %02x", by);
    		}
    		subDetails.add("Scan Record: "+formatter.toString());
    		
    		Group group = new Group(b.getMinor(), b.getDistance(), b.getRssi());
    		group.children.addAll(subDetails);
    		g.add(group);
    	}
    	groups = g;
	    try
		{
			runNotifyThread();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main, menu);
	    return super.onCreateOptionsMenu(menu);
	}


	private boolean isScanning = true;
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		switch (item.getItemId()) {
		    case R.id.pause_scan:
		    	if(isScanning)
		        {
		        	scanLeDevice(false);
		            item.setIcon(R.drawable.ic_play);
		            item.setTitle("Play");
		            isScanning = false;
		        }
		        else
		        {
		        	scanLeDevice(true);
		            item.setIcon(R.drawable.ic_pause);
		            item.setTitle("Pause");
		            isScanning = true;
		        }
		        return true;
		    }
	    return super.onOptionsItemSelected(item);
	}
	
	private void scanLeDevice(final boolean enable) {
        if (enable) {
        	mTimer = new Timer();
        	mTimer.scheduleAtFixedRate(new TimerTask() {
				@Override
        	    public void run() {
					try
					{
						mBluetoothAdapter.stopLeScan(mLeScanCallback);
						Utils.calculateRSSI(beacons);
						Utils.calculateDistance(beacons);
			        	MainActivity.this.createData();
						Utils.sendData(beacons, token);
						beacons = new ArrayList<Beacon>();
						mBluetoothAdapter.startLeScan(mLeScanCallback);
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
        	    }
        	}, 0, REFRESH_TIME);
        } else {
        	mTimer.cancel();
        	mTimer.purge();
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }
	
	private void runNotifyThread() {

	    Thread t = new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					runOnUiThread(new Runnable() {
				        @Override
				        public void run() {
				        	MainActivity.this.adapter.setGroups(groups);
				            MainActivity.this.adapter.notifyDataSetChanged();
				        }
				    });
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
			}
		});
	    t.start();
	    try {
			t.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// Device scan callback.
	private BluetoothAdapter.LeScanCallback mLeScanCallback =
	        new BluetoothAdapter.LeScanCallback() {
	    @Override
	    public void onLeScan(final BluetoothDevice device, int rssi,
	            byte[] scanRecord) {
	    	Utils.updateBeaconList(beacons, device.getAddress(), rssi, scanRecord);
	   }
	};
}
