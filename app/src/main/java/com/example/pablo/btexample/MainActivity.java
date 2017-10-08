package com.example.pablo.btexample;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Bluetooth";
    private ToggleButton bluetoothBtn;
    private BluetoothAdapter bluetoothAdapter;
    private Button searchBtn;
    private ArrayList<BluetoothDevice> list = new ArrayList<BluetoothDevice>();

    private ProgressDialog prd;
    private View.OnClickListener bluetoothListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(bluetoothBtn.isChecked()) {
                if(!bluetoothAdapter.isEnabled()) {
                    Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBT, 1000);
                }
            } else {
                bluetoothAdapter.disable();
            }

        }
    };
    private View.OnClickListener searchListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
        bluetoothAdapter.startDiscovery();
        }
    };

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i(TAG, action);
            if(BluetoothDevice.ACTION_FOUND.equals(action)) {
                list.add((BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE));

            } else if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                list.clear();
                prd.show();
            } else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                prd.dismiss();
                Intent intentsearch = new Intent(MainActivity.this, Dispositivos.class);
                intentsearch.putExtra("Lista", list);
                ArrayList<BluetoothDevice> paired = new ArrayList<BluetoothDevice>(bluetoothAdapter.getBondedDevices());
                intentsearch.putExtra("Vinculados", paired);
                startActivity(intentsearch);
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bluetoothBtn = (ToggleButton) findViewById(R.id.bluetoothBtn);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        searchBtn = (Button) findViewById(R.id.button);
        prd = new ProgressDialog(this);
        prd.setMessage("Buscando dispositivos");
        if(bluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "THERE IS NO BLUETOOTH", Toast.LENGTH_LONG).show();

        } else {
            if(bluetoothAdapter.isEnabled()) {
                bluetoothBtn.setChecked(true);
            }
            bluetoothBtn.setOnClickListener(bluetoothListener);
            searchBtn.setOnClickListener(searchListener);
            IntentFilter filter = new IntentFilter();
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            filter.addAction(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mReceiver, filter);
        }
    }

    @Override
    public void onDestroy(){
        unregisterReceiver(mReceiver);
        if(bluetoothAdapter != null) {
            bluetoothAdapter.disable();
        }
        super.onDestroy();
    }
}
