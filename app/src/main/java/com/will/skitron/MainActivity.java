package com.will.skitron;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;
import android.provider.Telephony;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.ArrayList;


public class MainActivity extends ActionBarActivity
{

    private final static String CONTROLLER_MAC =  "20:13:02:19:14:01";

    private LocationManager locationManager;
    private LocationListener locationListener;
    private TextView trentView, locView, shits;
    private ListView deviceListView;

    private final int REQUEST_ENABLE_BT = 1;
    private final int CONTROLLER_MESSAGE = 2;

    private final char TEAL = '2';
    private final char RED = '1';
    private final char GREEN = '0';
    private final char BLUE = '3';


    private ArrayAdapter mArrayAdapter;
    private ArrayList<BluetoothDevice> devices;
    private Handler mMessageHandler;

    private BluetoothAdapter mBluetoothAdapter;
    private TelephonyManager mTelephonyManager;

    private BlueSmirfSPP sppController;
    private BlueSmirfSPP sppHUD;

    private ByteBuffer bluetoothByteBuffer;

    private AudioManager mAudioManger;


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

        mAudioManger = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);

        mTelephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        mTelephonyManager.listen(new CallStateListener(), PhoneStateListener.LISTEN_CALL_STATE);


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
                    case CONTROLLER_MESSAGE:
                    {
                        String data = "";


                        for(int i = 0; i < msg.arg1; i++)
                        {
                            data += (((byte []) msg.obj)[i]);
                        }

                        buttonPress(data.charAt(0), data.charAt(1));
                        shits.setText(data);


                    }
                }
            }
        };

    }

    public void red(View view)
    {
        shits.setText("SHITS: RED");
        String red = "RED1234#";
        sppController.write(red.getBytes(), 0, red.getBytes().length);
    }

    public void green(View view)
    {
        shits.setText("SHITS: GREEN");
        String green = "GREEN1234#";
        sppController.write(green.getBytes(), 0, green.getBytes().length);
    }

    public void teal(View view)
    {
        shits.setText("SHITS: TEAL");
        String teal = "TEAL1234#";
        sppController.write(teal.getBytes(), 0, teal.getBytes().length);
    }

    public void blue(View view)
    {
        shits.setText("SHITS: BLUE");
        String blue= "BLUE1234#";
        sppController.write(blue.getBytes(), 0, blue.getBytes().length);
    }

    public boolean buttonPress(char button, char press)
    {
        switch (button)
        {
            case(TEAL):
            {
                if(press == '1')
                {
                    volumeChange(1);
                }
                if(press == '2')
                {

                    break;
                }
                break;
            }

            case(GREEN):
            {
                if(press == '1')
                {
                    if(mTelephonyManager.getCallState() == TelephonyManager.CALL_STATE_RINGING)
                        answerCall();
                    else
                        volumeChange(-1);
                }
                else if(press == '2')
                {
                    playPause();
                    break;
                }
                break;
            }

            case(RED):
            {
                if(press == '1')
                {

                }
                else if(press == '2')
                {
                    if(mTelephonyManager.getCallState() == TelephonyManager.CALL_STATE_RINGING)
                        blockCall();
                    if(mTelephonyManager.getCallState() == TelephonyManager.CALL_STATE_OFFHOOK)
                        endCall();
                    else
                        skip("previous");
                    break;
                }
                break;
            }

            case(BLUE):
            {
                if(press == '1')
                {

                }
                else if(press == '2')
                {
                    skip("next");
                    break;
                }
                break;
            }
        }
        return false;
    }

    public void playPause()
    {
        Intent i = new Intent("com.android.music.musicservicecommand");
        if(mAudioManger.isMusicActive())
        {
            i.putExtra("command", "pause");
        }
        else
        {
            i.putExtra("command", "play");
        }
        MainActivity.this.sendBroadcast(i);
    }

    public void volumeChange(int change)
    {
        int flag = 1;
        int stream = 0;
        if(mTelephonyManager.getCallState() == TelephonyManager.CALL_STATE_OFFHOOK)
            stream = AudioManager.USE_DEFAULT_STREAM_TYPE;
        else
            stream = AudioManager.STREAM_MUSIC;

        mAudioManger.adjustSuggestedStreamVolume(change, stream, flag);
    }

    public void skip(String direction)
    {
        Intent i = new Intent("com.android.music.musicservicecommand");
        i.putExtra("command", direction);
        MainActivity.this.sendBroadcast(i);
    }

    public void answerCall()
    {
        makeToast("Attempting to Answer");
        Intent i = new Intent(Intent.ACTION_MEDIA_BUTTON);
        i.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP,
                KeyEvent.KEYCODE_HEADSETHOOK));
        sendOrderedBroadcast(i, null);
    }
    
    public void endCall()
    {
        makeToast("Attempting to end Call");
        Intent i = new Intent(Intent.ACTION_MEDIA_BUTTON);
        i.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP,
                KeyEvent.KEYCODE_HEADSETHOOK));
        sendOrderedBroadcast(i, null);
    }

    public void blockCall()
    {
        makeToast("Attempting to block Call");
        Intent i = new Intent(Intent.ACTION_MEDIA_BUTTON);
        i.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP,
                KeyEvent.KEYCODE_CALL));
        sendOrderedBroadcast(i, null);
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
            ReadThread readThread = new ReadThread(CONTROLLER_MESSAGE);
            readThread.start();
        }

//        if(!sppHUD.connect(HUD_MAC))
//            makeToast("HUD not Found");

    }

    public class ReadThread extends Thread
    {
        private int DEVICE_MESSAGE;
        public  ReadThread(int deviceType)
        {
            DEVICE_MESSAGE = deviceType;
        }

        public void run()
        {
            byte[] readVal = new byte[1024];
            int length;
            int bytes;

            while(true)
            {
                length = sppController.readByte();

                for(int i = 0; i < length; i++)
                {
                    readVal[i] = (byte) (sppController.readByte());
                }

                mMessageHandler.obtainMessage(DEVICE_MESSAGE, length, -1, readVal.clone()).sendToTarget();
            }
        }
    }

    private class CallStateListener extends PhoneStateListener
    {
        @Override
        public void onCallStateChanged(int state, String incomingNumber)
        {
            switch (state)
            {
                case TelephonyManager.CALL_STATE_RINGING:
                    // called when someone is ringing to this phone
                    makeToast("Incoming: "+ incomingNumber);

                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    makeToast("Answered");
                    break;
            }
        }
    }
}
