package com.example.locationtracker;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
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
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.TextView;
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
	private MyExpandableListAdapter adapter;
	Timer mTimer;
	public String httpResponse = null;
    
 // Stops scanning after 10 seconds.
    private static final long REFRESH_TIME = 1000;
    private static final int REQUEST_ENABLE_BT = 1;
    
    ArrayList<Group> groups = new ArrayList<Group>();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      
      ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("username", "application"));
		params.add(new BasicNameValuePair("password", "application"));
		runHttpThread(Utils.API_URL+"get-auth-token/", params);
		
		String token = "";
		
		if (httpResponse != null) {
          try {
              JSONObject jsonObj = new JSONObject(httpResponse);
               
              // Getting JSON Array node
              token = jsonObj.getString("token");

          } catch (JSONException e) {
              e.printStackTrace();
          }
      } else {
          Log.e("ServiceHandler", "Couldn't get any data from the url");
      }
		
		System.out.println(token);
      
      setContentView(R.layout.activity_main);
      listView = (ExpandableListView) findViewById(R.id.listView);
      
      final BluetoothManager bluetoothManager =
		        (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();
		
		if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
		    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}

		beacons = new ArrayList<Beacon>();
		adapter = new MyExpandableListAdapter(this,groups);
		listView.setAdapter(adapter);
		createData();
		
		scanLeDevice(true);
    }

    public void createData() {
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
		    groups.add(group);
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
					mBluetoothAdapter.stopLeScan(mLeScanCallback);
					Utils.calculateDistance(beacons);
					groups.clear();
					try
					{
						createData();
						adapter.setGroups(groups);
						runNotifyThread();
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
					Utils.sendData(beacons);
					beacons = new ArrayList<Beacon>();
					mBluetoothAdapter.startLeScan(mLeScanCallback);
        	    }
        	}, 0, REFRESH_TIME);
        } else {
        	mTimer.cancel();
        	mTimer.purge();
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }
	
	private void runNotifyThread() {

	    new Thread() {
	        public void run() {
                try {
					runOnUiThread(new Runnable() {
				        @Override
				        public void run() {
				                adapter.notifyDataSetChanged();
				        }
				    });
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
	        }
	    }.start();
	}
	
	private void runHttpThread(final String u, final ArrayList<NameValuePair> params) {
	    Thread t = new Thread() {
	        public void run() {
					httpResponse = POST(u, params);
	        }
	    };
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
	
	
	public static String GET(String url){
        InputStream inputStream = null;
        String result = "";
        try {
 
            // create HttpClient
            HttpClient httpclient = new DefaultHttpClient();
 
            // make GET request to the given URL
            HttpResponse httpResponse = httpclient.execute(new HttpGet(url));
 
            // receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();
 
            // convert inputstream to string
            if(inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "Did not work!";
 
        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }
 
        return result;
    }
	
	public String POST(String url, ArrayList<NameValuePair> params){
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(url);
		HttpResponse response = null;
		try {
		    // Add your data
		    httppost.setEntity(new UrlEncodedFormEntity(params));

		    // Execute HTTP Post Request
		    response = httpclient.execute(httppost);

		} catch (ClientProtocolException e) {
		    // TODO Auto-generated catch block
		} catch (IOException e) {
		    // TODO Auto-generated catch block
		}
		HttpEntity entity = response.getEntity();
		String responseString = null;
		try {
			responseString = EntityUtils.toString(entity, "UTF-8");
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return responseString;
    }
 
    // convert inputstream to String
    private static String convertInputStreamToString(InputStream inputStream) throws IOException{
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;
 
        inputStream.close();
        return result;
 
    }
}
