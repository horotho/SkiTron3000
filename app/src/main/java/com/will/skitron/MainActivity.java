package com.will.skitron;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
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
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;


public class MainActivity extends ActionBarActivity
{

    private LocationManager locationManager;
    private LocationListener locationListener;
    private TextView trentView, locView;
    private ListView listView;
    private BluetoothAdapter mBluetoothAdapter;
    private int REQUEST_ENABLE_BT = 1;
    private ArrayAdapter mArrayAdapter;
    private ArrayList<BluetoothDevice> pairedList;
    private Handler handler;
    private ConnectedThread connectedThread;
    private ConnectionThread connectionThread;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationManager = (LocationManager) this.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        trentView = (TextView) this.findViewById(R.id.trentView);
        locView = (TextView) this.findViewById(R.id.locView);
        listView = (ListView) this.findViewById(R.id.listView);
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
        } else
        {
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            pairedList = new ArrayList<BluetoothDevice>();
            pairedList.addAll(pairedDevices);

            // If there are paired devices
            if (pairedDevices.size() > 0)
            {
                // Loop through paired devices
                for (BluetoothDevice device : pairedDevices)
                {
                    // Add the name and address to an array adapter to show in a ListView
                    mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                }
            }
        }

        listView.setAdapter(mArrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                connectionThread = new ConnectionThread(pairedList.get(position));
                connectionThread.run();
            }
        });


        handler = new Handler()
        {
            @Override
            public void handleMessage(Message msg)
            {
                if (msg.what == 0)
                {
                    Toast.makeText(getApplicationContext(), msg.getData().toString(), Toast.LENGTH_SHORT).show();
                } else
                {

                }
            }
        };
    }

    public void red(View view)
    {
        TextView shits = (TextView) findViewById(R.id.textView);
        shits.setText("SHITS: RED");

        if (connectedThread != null)
        {
            byte[] message = {0x77, 0x12, 0x0};
            connectedThread.write(message);
        }
    }

    public void green(View view)
    {
        TextView shits = (TextView) findViewById(R.id.textView);
        shits.setText("SHITS: GREEN");

        if (connectedThread != null)
        {
            byte[] message = {0x77, 0x12, 0x1};
            connectedThread.write(message);
        }
    }

    public void teal(View view)
    {
        TextView shits = (TextView) findViewById(R.id.textView);
        shits.setText("SHITS: TEAL");

        if (connectedThread != null)
        {
            byte[] message = {0x77, 0x12, 0x2};
            connectedThread.write(message);
        }
    }

    public void blue(View view)
    {
        TextView shits = (TextView) findViewById(R.id.textView);
        shits.setText("SHITS: BLUE");

        if (connectedThread != null)
        {
            byte[] message = {'f', 'u', 'c', 'k'};
            connectedThread.write(message);
        }
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

    private class ConnectionThread extends Thread
    {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectionThread(BluetoothDevice device)
        {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;
            makeToast("Starting connection thread...");

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try
            {
                ParcelUuid[] uuids = mmDevice.getUuids();

                if (uuids.length > 0)
                {
                    tmp = device.createRfcommSocketToServiceRecord(uuids[0].getUuid());
                } else
                {
                    tmp = device.createRfcommSocketToServiceRecord(UUID.fromString("a44f84a0-c8d9-11e3-9c1a-0800200c9a66"));
                }

            } catch (IOException e)
            {
            }

            mmSocket = tmp;
        }

        public void run()
        {
            // Cancel discovery because it will slow down the connection
            mBluetoothAdapter.cancelDiscovery();

            try
            {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
            } catch (IOException connectException)
            {
                // Unable to connect; close the socket and get out
                try
                {
                    Toast.makeText(getApplicationContext(), "Unable to connect...", Toast.LENGTH_SHORT).show();
                    mmSocket.close();
                } catch (IOException closeException)
                {
                }
                return;
            }

            // Do work to manage the connection (in a separate thread)
            Toast.makeText(getApplicationContext(), "Starting connected thread...", Toast.LENGTH_SHORT).show();
            connectedThread = new ConnectedThread(mmSocket);
        }

        /**
         * Will cancel an in-progress connection, and close the socket
         */
        public void cancel()
        {
            try
            {
                mmSocket.close();
            } catch (IOException e)
            {
            }
        }
    }

    private class ConnectedThread extends Thread
    {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        private static final int MESSAGE_READ = 0;
        private static final int MESSAGE_WRITE = 1;

        public ConnectedThread(BluetoothSocket socket)
        {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try
            {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e)
            {
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run()
        {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true)
            {
                try
                {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    // Send the obtained bytes to the UI activity
                    handler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                } catch (IOException e)
                {
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes)
        {
            try
            {
                mmOutStream.write(bytes);
            } catch (IOException e)
            {
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel()
        {
            try
            {
                mmSocket.close();
            } catch (IOException e)
            {
            }
        }
    }

}
