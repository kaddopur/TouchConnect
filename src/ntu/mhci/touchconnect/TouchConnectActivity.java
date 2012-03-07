package ntu.mhci.touchconnect;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.bluetooth.*;

public class TouchConnectActivity extends Activity {
	private static final int REQUEST_ENABLE_BT = 23;
	private ListView lv_paired;
	private BluetoothAdapter mBluetoothAdapter;
	private List<String> pairedDeviceNames = new ArrayList<String>();
	private ArrayAdapter<String> mArrayAdapter;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		findViews();

		initBluetooth();

		// Register the BroadcastReceiver
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		registerReceiver(mReceiver, filter); // Don't forget to unregister
												// during onDestroy

	}

	private void findViews() {
		lv_paired = (ListView) findViewById(R.id.lv_paired);
		mArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, pairedDeviceNames);
		lv_paired.setAdapter(mArrayAdapter);
	}

	private void initBluetooth() {
		Log.e("Jason", "Bluetooth device ok");
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
			Log.e("Jason", "There is no bluetooth");
		}

		Log.e("Jason", "Bluetooth is enabled");
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}

		Log.e("Jason", "Find paired devices");
		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
		if (pairedDevices.size() > 0) {
			for (BluetoothDevice device : pairedDevices) {
				if (pairedDeviceNames.indexOf("[PAIR]: " + device.getName() + "\n" + device.getAddress()) == -1){
					pairedDeviceNames.add("[PAIR]: " + device.getName() + "\n" + device.getAddress());
				}
			}
			mArrayAdapter.notifyDataSetChanged();
		}

		Log.e("Jason", "Scan new devices");
		mBluetoothAdapter.startDiscovery();
	}

	// Create a BroadcastReceiver for ACTION_FOUND
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			// When discovery finds a device
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				// Get the BluetoothDevice object from the Intent
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				// Add the name and address to an array adapter to show in a
				// ListView
				if (pairedDeviceNames.indexOf("[SCAN] " + device.getName() + "\n" + device.getAddress()) == -1){
					pairedDeviceNames.add("[SCAN] " + device.getName() + "\n" + device.getAddress());
					mArrayAdapter.notifyDataSetChanged();
				}
			}
		}
	};
}
