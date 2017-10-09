package com.example.pablo.btexample;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class ConectarDispositivo extends AppCompatActivity {

    private static final String TAG = "ConectarDispositivos" ;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); ;
    private BluetoothSocket btSocket;
    private final int handlerState = 0;
    private Handler handlerBluetooth;
    private StringBuilder recDataString = new StringBuilder();
    private TextView valor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conectar_dispositivo);
        handlerBluetooth = myHandler();
        valor = (TextView) findViewById(R.id.valor);
    }

    private Handler myHandler() {
        return new Handler() {
            public void handleMessage(android.os.Message msg)
            {
                //si se recibio un msj del hilo secundario
                if (msg.what == handlerState)
                {
                    //voy concatenando el msj
                    String readMessage = (String) msg.obj;
                    recDataString.append(readMessage);
                    int endOfLineIndex = recDataString.indexOf("\r\n");

                    //cuando recibo toda una linea la muestro en el layout
                    if (endOfLineIndex > 0)
                    {
                        String dataInPrint = recDataString.substring(0, endOfLineIndex);
                        valor.setText(dataInPrint);

                        recDataString.delete(0, recDataString.length());
                    }
                }
            }
        };

    }


    @Override
    public void onResume() {
        super.onResume();
        BluetoothDevice dev = BluetoothAdapter.getDefaultAdapter().getRemoteDevice((String) getIntent().getExtras().get("aConectar"));
        ConnectBT connectThread = new ConnectBT(dev);
    }


    private class ConnectBT {
        public ConnectBT(BluetoothDevice device) {
            try {
                btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
                btSocket.connect();
                UseDevice useDevice = new UseDevice(btSocket);
                useDevice.start();
                useDevice.write("x");
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "No pude crear socket", Toast.LENGTH_SHORT).show();
                return;
            }
        }
    }

    private class UseDevice extends Thread {
        private InputStream in;
        private OutputStream out;

        public UseDevice(BluetoothSocket btSocket) {

            try {
                out = btSocket.getOutputStream();
                in = btSocket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Error al obtener input/output", Toast.LENGTH_SHORT).show();
                return;
            }


        }

        public void write(String x) {
            byte[] msgBuffered = x.getBytes();
            try {
                out.write(msgBuffered);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Error al enviar x", Toast.LENGTH_SHORT).show();
                finish();
            }
        }


        public void run() {
            byte[] buffer = new byte[512];
            int bytes;

            //el hilo secundario se queda esperando mensajes del HC05
            while (true)
            {
                try
                {
                    //se leen los datos del Bluethoot
                    bytes = in.read(buffer);
                    String readMessage = new String(buffer, 0, bytes);


                    //se muestran en el layout de la activity, utilizando el handler del hilo
                    // principal antes mencionado
                    handlerBluetooth.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }

        }
    }
}
