package ntu.mhci.touchconnect;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class TouchConnectActivity extends Activity {
	private Button bt_host, bt_client;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		findViews();
		setListeners();
	}

	private void setListeners() {
		bt_host.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent it = new Intent(TouchConnectActivity.this, BluetoothHost.class);
				startActivity(it);
			}
		});
		
		bt_client.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent it = new Intent(TouchConnectActivity.this, BluetoothClient.class);
				startActivity(it);
			}
		});
	}

	private void findViews() {
		bt_host = (Button) findViewById(R.id.bt_host);
		bt_client = (Button) findViewById(R.id.bt_client);
	}
}
