package com.example.pablo.btexample;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class Dispositivos extends AppCompatActivity {
    private ListView listView;
    private ListView listEmparejados;

    private SensorManager sensorManager;

    private AdapterView.OnItemClickListener clickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            Toast.makeText(getApplicationContext(), (String) parent.getItemAtPosition(position), Toast.LENGTH_SHORT).show();
            Intent it = new Intent(Dispositivos.this, ConectarDispositivo.class);
            it.putExtra("aConectar", (String) ((String) parent.getItemAtPosition(position)).split("\n")[1]);
            startActivity(it);

        }
    };
    private AdapterView.OnItemClickListener clickListenerVinculado = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            Toast.makeText(getApplicationContext(), (String) parent.getItemAtPosition(position), Toast.LENGTH_SHORT).show();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dispositivos);
        listView = (ListView) findViewById(R.id.listView);
        listEmparejados = (ListView) findViewById(R.id.listEmparejados);


        ArrayList<BluetoothDevice> devices = getIntent().getExtras().getParcelableArrayList("Lista");
        ArrayList<BluetoothDevice> devicesVinculados = getIntent().getExtras().getParcelableArrayList("Vinculados");
        ArrayList<String> nameDevices = new ArrayList<String>();
        ArrayList<String> nameVinculados = new ArrayList<>();
        devices.removeAll(devicesVinculados);
        for (BluetoothDevice device : devices) {
            nameDevices.add(device.getName() + "\n" + device.getAddress());
        }
        ArrayAdapter<String> devicesAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, nameDevices);


        if (!devicesVinculados.isEmpty()) {
            for (BluetoothDevice device : devicesVinculados) {
                nameVinculados.add(device.getName() + "\n" + device.getAddress());
            }
        }

        ArrayAdapter<String> devicesAdapterVinculados = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, nameVinculados);

        listView.setAdapter(devicesAdapter);
        listView.setOnItemClickListener(clickListener);

        listEmparejados.setAdapter(devicesAdapterVinculados);
        listEmparejados.setOnItemClickListener(clickListener);
    }

}
