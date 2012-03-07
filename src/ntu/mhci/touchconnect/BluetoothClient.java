package ntu.mhci.touchconnect;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class BluetoothClient extends Activity {
	private BluetoothAdapter mBluetoothAdapter;
	private final String mMAC = "3C:5A:37:89:3F:38"; // host: Jason's desire S
	private final UUID mUUID = UUID
			.fromString("d4925895-0722-4252-a969-03be18b8ffba");
	private BluetoothDevice hostDevice;
	byte[] b = new byte[100];
	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case 1:
				Toast.makeText(BluetoothClient.this, "connect ok",
						Toast.LENGTH_SHORT).show();
				break;
			case 2:
				connectToHost();
				break;
			case 3:
				Toast.makeText(BluetoothClient.this, new String(b),
						Toast.LENGTH_SHORT).show();
				break;

			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.client);

		// Register the BroadcastReceiver
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		registerReceiver(mReceiver, filter); // Don't forget to unregister
												// during onDestroy

		Log.e("Jason", "ohohoh start");
		initBluetooth();
		findHost(); // initial hostDevice
	}

	private void connectToHost() {

		Log.e("Jason", "" + hostDevice);
		Thread t = new ConnectThread(hostDevice);
		t.start();
	}

	private void findHost() {
		// in paired list
		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter
				.getBondedDevices();
		if (pairedDevices.size() > 0) {
			// Loop through paired devices
			for (BluetoothDevice device : pairedDevices) {
				if (device.getAddress().equals(mMAC)) {
					hostDevice = device;
					connectToHost();
					Log.e("Jason", "FOUND by pair");
					return;
				}
				Log.e("Jason", "p " + device.getAddress());
			}
		}

		// go slow scanning
		mBluetoothAdapter.startDiscovery();
	}

	private void initBluetooth() {
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "There is no Bluetooth", Toast.LENGTH_SHORT)
					.show();
		}
	}

	private class ConnectThread extends Thread {
		private final BluetoothSocket mmSocket;
		private final BluetoothDevice mmDevice;

		public ConnectThread(BluetoothDevice device) {
			// Use a temporary object that is later assigned to mmSocket,
			// because mmSocket is final
			BluetoothSocket tmp = null;
			mmDevice = device;

			// Get a BluetoothSocket to connect with the given BluetoothDevice
			try {
				// MY_UUID is the app's UUID string, also used by the server
				// code
				tmp = device.createRfcommSocketToServiceRecord(mUUID);
			} catch (IOException e) {
			}
			mmSocket = tmp;
		}

		public void run() {
			// Cancel discovery because it will slow down the connection
			mBluetoothAdapter.cancelDiscovery();

			try {
				// Connect the device through the socket. This will block
				// until it succeeds or throws an exception
				mmSocket.connect();
			} catch (IOException connectException) {
				// Unable to connect; close the socket and get out
				try {
					mmSocket.close();
				} catch (IOException closeException) {
				}
				return;
			}

			// Do work to manage the connection (in a separate thread)
			// manageConnectedSocket(mmSocket);
			Message m = new Message();
			m.what = 1;
			mHandler.sendMessage(m);

			try {
				mmSocket.getInputStream().read(b);
				Message m2 = new Message();
				m2.what = 3;
				mHandler.sendMessage(m2);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		/** Will cancel an in-progress connection, and close the socket */
		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) {
			}
		}
	}

	// Create a BroadcastReceiver for ACTION_FOUND
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				BluetoothDevice device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				if (device.getAddress().equals(mMAC)) {
					hostDevice = device;
					mBluetoothAdapter.cancelDiscovery();
					Message m = new Message();
					m.what = 2;
					mHandler.sendMessage(m);
					Log.e("Jason", "FOUND by scan");
				}
				Log.e("Jason", "s " + device.getAddress());
			}
		}
	};
}
