package com.will.skitron;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.ParcelUuid;
import android.os.Vibrator;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;


public class MainActivity extends ActionBarActivity
{

    private final static String CONTROLLER_MAC =  "20:13:02:19:14:01";

    private LocationManager locationManager;
    private LocationListener locationListener;
    private TextView trentView, locView, shits;
    private ListView deviceListView;


    final private int REQUEST_ENABLE_BT = 1;
    final private int MESSAGE_READ = 2;

    private ArrayAdapter mArrayAdapter;
    private ArrayList<BluetoothDevice> devices;
    private Handler mMessageHandler;

    private BluetoothAdapter mBluetoothAdapter;
   // private ConnectedThread mBluetoothConnection;
    private BlueSmirfSPP sppController;
    private BlueSmirfSPP sppHUD;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationManager = (LocationManager) this.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        trentView = (TextView) this.findViewById(R.id.trentView);
        locView = (TextView) this.findViewById(R.id.locView);
        shits = (TextView) findViewById(R.id.textView);
        deviceListView = (ListView) this.findViewById(R.id.listView);
        mArrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, android.R.id.text1);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        locationListener = new LocationListener()
        {
            @Override
            public void onLocationChanged(Location location)
            {
                trentView.setText("Speed: " + location.getSpeed());
                locView.setText("Location: " + location.getLatitude() + ", " + location.getLongitude());
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras)
            {

            }

            @Override
            public void onProviderEnabled(String provider)
            {

            }

            @Override
            public void onProviderDisabled(String provider)
            {

            }
        };


        //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 0, locationListener);

        if (!mBluetoothAdapter.isEnabled())
        {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        sppController = new BlueSmirfSPP();

        mMessageHandler = new Handler()
        {
            @Override
            public void handleMessage (Message msg)
            {
                super.handleMessage(msg);
                switch (msg.what)
                {
                    case MESSAGE_READ:
                    {
                        String data = "";
                        data = ((byte[]) msg.obj).toString();
                        shits.setText(data);
                    }
                }
            }
        };

    }

    public void red(View view)
    {
        shits.setText("SHITS: RED");
    }

    public void green(View view)
    {
        shits.setText("SHITS: GREEN");
    }

    public void teal(View view)
    {
        shits.setText("SHITS: TEAL");
    }

    public void blue(View view)
    {
        shits.setText("SHITS: BLUE");
    }

    public void makeToast(CharSequence input)
    {
        Toast.makeText(getApplicationContext(), input, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings)
        {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void connect(View view)
    {
        if(!sppController.connect(CONTROLLER_MAC))
            makeToast("Controller not Found");

        else
        {
            makeToast("Controller Connected!");
            ReadThread readThread = new ReadThread();
            readThread.start();
        }

//        if(!sppHUD.connect(HUD_MAC))
//            makeToast("HUD not Found");

    }

    public class ReadThread extends Thread
    {
        public  ReadThread()
        {

        }

        public void run()
        {
            byte[] readVal = new byte[2];
            int bytes;

            while(true)
            {
                bytes = sppController.read(readVal);
                mMessageHandler.obtainMessage(MESSAGE_READ, bytes, -1, readVal).sendToTarget();
            }
        }

    }
}
