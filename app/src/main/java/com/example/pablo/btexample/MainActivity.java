package com.example.pablo.btexample;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
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

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = "Bluetooth";
    private ToggleButton bluetoothBtn;
    private BluetoothAdapter bluetoothAdapter;
    private Button searchBtn;
    private ArrayList<BluetoothDevice> list = new ArrayList<BluetoothDevice>();

    private SensorManager sensorManager;

    // Acelerometro
    long ultimaActualizacion = 0, ultimoMovimiento = 0;
    float x = 0, y = 0, z = 0, xAnterior = 0, yAnterior = 0, zAnterior = 0;

    //Giroscopo
    boolean paUnLado = false, palOtro = false;

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
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    }

    @Override
    public void onResume() {
        super.onResume();
        sensorManager.registerListener((SensorEventListener) this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener((SensorEventListener) this, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener((SensorEventListener) this, sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY), SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onStop() {
        super.onStop();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onDestroy(){
        unregisterReceiver(mReceiver);
        if(bluetoothAdapter != null) {
            bluetoothAdapter.disable();
        }
        super.onDestroy();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        synchronized (this) {
            Intent i = new Intent(MainActivity.this, ConectarDispositivo.class);
            i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

            float[] valores =  event.values;

            switch(event.sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:
                    long tiempoActual = event.timestamp;

                    x = valores[0];
                    y = valores[1];
                    z = valores[2];

                    if(xAnterior == 0 && yAnterior == 0 && zAnterior == 0) {
                        ultimaActualizacion = tiempoActual;
                        ultimoMovimiento = tiempoActual;
                        xAnterior = x;
                        yAnterior = y;
                        zAnterior = z;
                    }

                    long diferencia = tiempoActual - ultimaActualizacion;
                    if(diferencia > 0) {
                        float movimiento = Math.abs((x + y + z) - (xAnterior - yAnterior - zAnterior)) / diferencia;
                        int limite = 1500;
                        float movimiento_min = 1E-8f;
                        if(movimiento > movimiento_min) {
                            if(tiempoActual - ultimoMovimiento >= limite) {
                                Log.i("mov", String.valueOf(Math.abs(x - xAnterior)) + "\t" + String.valueOf(Math.abs(y - yAnterior)) + "\t" + String.valueOf(Math.abs(z - zAnterior)));
/*                                Log.i("y-mov", String.valueOf(Math.abs(y - yAnterior)));
                                Log.i("z-mov", String.valueOf(Math.abs(z-zAnterior)));*/
                                if(Math.abs(y - yAnterior) < 1 && Math.abs(x - xAnterior) > 10 && Math.abs(z - zAnterior) < 1) {
                                    /*Log.i("mov", "El movimiento tan preciado");*/
                                    Toast.makeText(getApplicationContext(), "El movimiento tan preciado", Toast.LENGTH_SHORT).show();
                                    // i.putExtra("sensor", "acelerometro");
                                    // startActivity(i);
                                    //new ListenerAcelerometro().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                }
                            }
                        }

                        ultimoMovimiento = tiempoActual;
                    }

                    xAnterior = x;
                    yAnterior = y;
                    zAnterior = z;
                    ultimaActualizacion = tiempoActual;

                    break;

                case Sensor.TYPE_GYROSCOPE:
                    if(event.values[0] > 5f) { // anticlockwise
                        Log.i("GIROSCOPO", "PA UN LADO");
                        paUnLado = !paUnLado;
                    } else if(event.values[0] < - 5f) { // clockwise
                        Log.i("GIROSCOPO", "PAL OTRO");
                        palOtro = !palOtro;
                    }

                    if(paUnLado && palOtro) {
                        Log.i("GIROSCOPO", "PIOLA");
                        paUnLado = false;
                        palOtro = false;
                    }
                    break;

                case Sensor.TYPE_PROXIMITY:
                    if(event.values[0] < ((Sensor) sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)).getMaximumRange()) {
                        Log.i("PROXIMIDAD", "CERCA");
                    } else {
                        Log.i("PROXIMIDAD", "LEJOS");
                    }
                    break;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
