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
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.TextView;

public class MainActivity extends Activity {

	private BluetoothAdapter mBluetoothAdapter;
	private ArrayList<Beacon> beacons;
    
 // Stops scanning after 10 seconds.
    private static final long REFRESH_TIME = 5000;
    private static final int REQUEST_ENABLE_BT = 1;
    
    SparseArray<Group> groups = new SparseArray<Group>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);
      createData();
      ExpandableListView listView = (ExpandableListView) findViewById(R.id.listView);
      MyExpandableListAdapter adapter = new MyExpandableListAdapter(this,
          groups);
      listView.setAdapter(adapter);
      
      final BluetoothManager bluetoothManager =
		        (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();
		
		if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
		    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}
		
		beacons = new ArrayList<Beacon>();
		/*
		final Button button = (Button) findViewById(R.id.scan);
		
      button.setOnClickListener(new View.OnClickListener() {
          public void onClick(View v) {
              scanLeDevice(true);
          }
      });*/
    }

    public void createData() {
      for (int j = 0; j < 5; j++) {
        Group group = new Group("Test " + j);
        for (int i = 0; i < 5; i++) {
          group.children.add("Sub Item" + i);
        }
        groups.append(j, group);
      }
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void scanLeDevice(final boolean enable) {
        if (enable) {
        	Timer mTimer = new Timer();
        	mTimer.scheduleAtFixedRate(new TimerTask() {
				@Override
        	    public void run() {
					mBluetoothAdapter.stopLeScan(mLeScanCallback);
					Utils.sendData(beacons);
					beacons = new ArrayList<Beacon>();
					mBluetoothAdapter.startLeScan(mLeScanCallback);
        	    }
        	}, 0, REFRESH_TIME);
        } else {
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
