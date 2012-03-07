package ntu.mhci.touchconnect;

import java.io.IOException;
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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class BluetoothHost extends Activity {
	private BluetoothAdapter mBluetoothAdapter;
	private TextView tv_uuid;
	Button btn;
	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case 0:
				tv_uuid.setText("" + msg.obj);
				break;
			case 1:
				Toast.makeText(BluetoothHost.this, "accept ok",
						Toast.LENGTH_SHORT).show();
				break;
			}
		}
	};
	BluetoothSocket sockets[] = new BluetoothSocket[10];
	int cnt = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.host);
		bindViews();

		initBluetooth();
		setDiscoverable();
		listenClient();

		Log.e("Jason", mBluetoothAdapter.getAddress());
	}

	private void bindViews() {
		tv_uuid = (TextView) findViewById(R.id.tv_uuid);
		btn = (Button) findViewById(R.id.button1);
		btn.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				byte[] b = (cnt + " ").getBytes();
				for (int i = 0; i < cnt; i++) {
					try {
						sockets[i].getOutputStream().write(b);
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
		Intent discoverableIntent = new Intent(
				BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		discoverableIntent.putExtra(
				BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
		startActivity(discoverableIntent);
	}

	private void initBluetooth() {
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "There is no Bluetooth", Toast.LENGTH_SHORT)
					.show();
		}
	}

	private class AcceptThread extends Thread {
		private BluetoothServerSocket mmServerSocket;
		private final UUID mUUID;

		public AcceptThread() {
			BluetoothServerSocket tmp = null;
			mUUID = UUID.fromString("d4925895-0722-4252-a969-03be18b8ffba");

			Log.e("Jason", "" + mUUID);
			try {
				tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(
						"TouchConnect", mUUID);
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
					Log.e("host", "1");
					socket = mmServerSocket.accept();
					Log.e("host", "2");

				} catch (IOException e) {
					Log.e("host", "3");
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}

					try {
						mmServerSocket.close();
						BluetoothServerSocket tmp = mBluetoothAdapter
								.listenUsingRfcommWithServiceRecord(
										"TouchConnect", mUUID);
						mmServerSocket = tmp;
					} catch (IOException e2) {
					}

					continue;
				}
				// If a connection was accepted
				if (socket != null) {
					// Do work to manage the connection (in a separate thread)
					Log.e("host", "4");
					// manageConnectedSocket(socket);
					sockets[cnt] = socket;
					cnt++;
					Message m3=new Message();
					m3.what=0;
					m3.obj=""+cnt;
					mHandler.sendMessage(m3);
					Message m = new Message();
					m.what = 1;
					mHandler.sendMessage(m);
					Log.e("host", "5");
					try {
						mmServerSocket.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
				}
			}
			Log.e("host", "6");
			new Thread(this).start();
			Log.e("host", "7");
		}

		/** Will cancel the listening socket, and cause the thread to finish */
		public void cancel() {
			try {
				mmServerSocket.close();
			} catch (IOException e) {
			}
		}
	}
}
