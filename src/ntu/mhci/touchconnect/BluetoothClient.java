package ntu.mhci.touchconnect;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class BluetoothClient extends Activity {
	private BluetoothAdapter mBluetoothAdapter;
	private final String mMAC = "AA:B0:12:40:00:74"; // host: Jason's desire S
	private final UUID mUUID = UUID.fromString("d4925895-0722-4252-a969-03be18b8ffba");
	private BluetoothDevice hostDevice;
	private Button bt_plus;
	private BluetoothSocket client_socket;
	
	Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch(msg.what){
			case 1:
				Toast.makeText(BluetoothClient.this, "connect ok", Toast.LENGTH_SHORT).show();
				break;
			case 2:
				connectToHost();
				break;
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.client);
		
		bindViews();
		
		// Register the BroadcastReceiver
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy
		
		
		Log.e("Jason", "ohohoh start");
		initBluetooth();
		findHost(); // initial hostDevice
	}

	private void bindViews() {
		bt_plus = (Button)findViewById(R.id.button1);
		bt_plus.setOnClickListener(new Button.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				try {
					client_socket.getOutputStream().write("0".getBytes(), 0, 1);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}

	private void connectToHost() {
		
		Log.e("Jason", ""+hostDevice);
		Thread t = new ConnectThread(hostDevice);
		t.start();
	}

	private void findHost() {
		// in paired list
		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
		if (pairedDevices.size() > 0) {
		    // Loop through paired devices
		    for (BluetoothDevice device : pairedDevices) {
		    	if(device.getAddress().equals(mMAC)){
		    		hostDevice = device;
		    		connectToHost();
		    		Log.e("Jason", "FOUND by pair");
		    		return;
		    	}
		    	Log.e("Jason", "p "+device.getAddress());
		    }
		}
		
		// go slow scanning
		mBluetoothAdapter.startDiscovery();
	}

	private void initBluetooth() {
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "There is no Bluetooth", Toast.LENGTH_SHORT).show();
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
	            // MY_UUID is the app's UUID string, also used by the server code
	            tmp = device.createRfcommSocketToServiceRecord(mUUID);
	        } catch (IOException e) { }
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
	            } catch (IOException closeException) { }
	            return;
	        }
	 
	        // Do work to manage the connection (in a separate thread)
	        //manageConnectedSocket(mmSocket);
	        client_socket = mmSocket;
	        Message m = new Message();
	        m.what = 1;
	        mHandler.sendMessage(m);
	    }
	 
	    /** Will cancel an in-progress connection, and close the socket */
	    public void cancel() {
	        try {
	            mmSocket.close();
	        } catch (IOException e) { }
	    }
	}

	// Create a BroadcastReceiver for ACTION_FOUND
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
	    public void onReceive(Context context, Intent intent) {
	        String action = intent.getAction();
	        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
	            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
	            if(device.getAddress().equals(mMAC)){
		    		hostDevice = device;
		    		mBluetoothAdapter.cancelDiscovery();
		    		
		    		
		    		
		    		Message m = new Message();
			        m.what = 2;
			        mHandler.sendMessage(m);
			        
			        
			        
		    		Log.e("Jason", "FOUND by scan");
		    	}
	            Log.e("Jason", "s "+device.getAddress());
	        }
	    }
	};
	
	private class ConnectedThread extends Thread {
	    private final BluetoothSocket mmSocket;
	    private final InputStream mmInStream;
	    private final OutputStream mmOutStream;
	 
	    public ConnectedThread(BluetoothSocket socket) {
	        mmSocket = socket;
	        InputStream tmpIn = null;
	        OutputStream tmpOut = null;
	 
	        // Get the input and output streams, using temp objects because
	        // member streams are final
	        try {
	            tmpIn = socket.getInputStream();
	            tmpOut = socket.getOutputStream();
	        } catch (IOException e) { }
	 
	        mmInStream = tmpIn;
	        mmOutStream = tmpOut;
	    }
	 
	    public void run() {
	        byte[] buffer = new byte[1024];  // buffer store for the stream
	        int bytes; // bytes returned from read()
	 
	        // Keep listening to the InputStream until an exception occurs
	        while (true) {
	            try {
	                // Read from the InputStream
	                bytes = mmInStream.read(buffer, 0, buffer.length);
	                // Send the obtained bytes to the UI activity
	          
	                Message m = new Message();
	                m.what = 5;
	                m.obj = new String(buffer).trim();
	                mHandler.sendMessage(m);
	                
	            } catch (IOException e) {
	                break;
	            }
	        }
	    }
	 
	    /* Call this from the main activity to send data to the remote device */
	    public void write(byte[] bytes) {
	        try {
	            mmOutStream.write(bytes);
	        } catch (IOException e) { }
	    }
	 
	    /* Call this from the main activity to shutdown the connection */
	    public void cancel() {
	        try {
	            mmSocket.close();
	        } catch (IOException e) { }
	    }
	}
}
