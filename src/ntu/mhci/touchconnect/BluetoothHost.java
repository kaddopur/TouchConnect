package ntu.mhci.touchconnect;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class BluetoothHost extends Activity {
	private BluetoothAdapter mBluetoothAdapter;
	private TextView tv_uuid;
	private ListView lv_clients;
	private Button bt_plus;
	private ArrayAdapter<String> ada_clients;
	private ArrayList<String> client_value = new ArrayList<String>();
	private int count;
	private ArrayList<BluetoothSocket> client_socket = new ArrayList<BluetoothSocket>();

	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case 0:
				tv_uuid.setText("" + msg.obj);
				break;
			case 1:
				Toast.makeText(BluetoothHost.this, "accept ok", Toast.LENGTH_SHORT).show();
				client_value.add("" + count);
				ada_clients.notifyDataSetChanged();
				break;
			case 5:
				int index = Integer.parseInt(""+msg.obj);
				int temp = Integer.parseInt(client_value.get(index)) + 1;
				client_value.set(index, ""+temp);
				ada_clients.notifyDataSetChanged();
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.host);
		bindViews();

		initBluetooth();
		setDiscoverable();
		listenClient();
	}

	private void bindViews() {
		ada_clients = new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1, client_value);

		tv_uuid = (TextView) findViewById(R.id.tv_uuid);
		lv_clients = (ListView) findViewById(R.id.lv_clients);
		lv_clients.setAdapter(ada_clients);
		count = 0;

		bt_plus = (Button) findViewById(R.id.bt_plus);
		bt_plus.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				for (int i = 0; i < client_value.size(); i++) {
					int temp_value = Integer.parseInt(client_value.get(i)) + 1;
					client_value.set(i, ""+temp_value);
					ada_clients.notifyDataSetChanged();
					try {
						client_socket.get(i).getOutputStream().write(("" + i).getBytes());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});
	}

	private void listenClient() {
		Thread t = new AcceptThread();
		t.start();
	}

	private void setDiscoverable() {
		Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
		startActivity(discoverableIntent);
	}

	private void initBluetooth() {
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "There is no Bluetooth", Toast.LENGTH_SHORT).show();
		}
		
		if(!mBluetoothAdapter.isEnabled()){
			mBluetoothAdapter.enable();
		}
	}

	private class AcceptThread extends Thread {
		private BluetoothServerSocket mmServerSocket;
		private UUID mUUID;

		public AcceptThread() {
			BluetoothServerSocket tmp = null;
			mUUID = UUID.fromString("d4925895-0722-4252-a969-03be18b8ffba");

			try {
				tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("TouchConnect", mUUID);
			} catch (IOException e) {
			}

			mmServerSocket = tmp;
			Message m = new Message();
			m.what = 0;
			m.obj = mUUID;
			mHandler.sendMessage(m);
		}

		public void run() {
			BluetoothSocket socket = null;

			// Keep listening until exception occurs or a socket is returned
			while (true) {
				try {
					Log.e("Jason", "1");
					socket = mmServerSocket.accept();
					client_socket.add(socket);
					Log.e("Jason", "2");
				} catch (IOException e) {
					Log.e("Jason", "3");

					try {
						mmServerSocket = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("TouchConnect", mUUID);
					} catch (IOException e2) {
					}
					break;
				}
				// If a connection was accepted
				if (socket != null) {
					// Do work to manage the connection (in a separate thread)
					new ConnectedThread(socket).start();

					// manageConnectedSocket(socket);
					Message m = new Message();
					m.what = 1;
					mHandler.sendMessage(m);
					try {
						Log.e("Jason", "6");
						mmServerSocket.close();
						Log.e("Jason", "7");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						Log.e("Jason", "8");
						e.printStackTrace();
					}
					break;
				}
			}

			new Thread(this).start();
		}

		/** Will cancel the listening socket, and cause the thread to finish */
		public void cancel() {
			try {
				mmServerSocket.close();
			} catch (IOException e) {
			}
		}
	}
	
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
