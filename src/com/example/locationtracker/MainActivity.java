package com.example.locationtracker;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.TextView;

public class MainActivity extends Activity {

	private BluetoothAdapter mBluetoothAdapter;
	private ArrayList<Beacon> beacons = null;
	private ExpandableListView listView;
	private MyExpandableListAdapter adapter;
	Timer mTimer;
    
 // Stops scanning after 10 seconds.
    private static final long REFRESH_TIME = 1000;
    private static final int REQUEST_ENABLE_BT = 1;
    
    ArrayList<Group> groups = new ArrayList<Group>();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
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
      for (Beacon b : beacons) {
    	Group group = new Group(b.getMinor(), b.getDistance(), b.getRssi());
        for (int i = 0; i < 5; i++) {
          group.children.add("Sub Item" + i);
        }
        groups.add(group);
      }
    }
    
    /*public void createData() {
    	int j = 0;
        for (Beacon b: beacons) {
          Group group = new Group(j, 0.0, 0.0);
          for (int i = 0; i < 5; i++) {
            group.children.add("Sub Item" + i);
          }
          groups.add(j++, group);
        }
      }*/

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
					createData();
					adapter.setGroups(groups);
					runOnUiThread(new Runnable() {
				        @Override
				        public void run() {
				                adapter.notifyDataSetChanged();
				        }
				    });
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
