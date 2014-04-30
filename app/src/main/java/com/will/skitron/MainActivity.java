package com.will.skitron;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;



public class MainActivity extends ActionBarActivity
{

    private final static String CONTROLLER_MAC = "20:13:02:19:14:01";
    private final static String HUD_MAC = "20:13:01:24:00:59";

    private LocationManager locationManager;
    private LocationListener locationListener;

    private final int REQUEST_ENABLE_BT = 1;
    private final int CONTROLLER_MESSAGE = 2;
    private final int HUD_MESSAGE = 3;

    private final int VIEW_GEO = 1;
    private final int VIEW_MUSIC = 2;
    private final int VIEW_CALL = 3;

    private final char TEAL = '2';
    private final char RED = '1';
    private final char GREEN = '0';
    private final char BLUE = '3';

    private Handler mMessageHandler;

    private BluetoothAdapter mBluetoothAdapter;
    private TelephonyManager mTelephonyManager;

    private BlueSmirfSPP sppController;
    private BlueSmirfSPP sppHUD;

    private CallStateListener mCallStateListener;
    private AudioManager mAudioManger;

    private Switch controllerSwitch;
    private Switch HUDSwitch;
    private Button connectButton;

    private int currentView;

    private LocationHUDView geoView;
    private MusicHUDView musicView;
    private CallHUDView callView;



    @Override
    protected void onCreate (Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        currentView = VIEW_GEO;

        geoView = new LocationHUDView();
        musicView = new MusicHUDView();
        callView = new CallHUDView();

        locationManager = (LocationManager) this.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mAudioManger = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);

        IntentFilter filter = new IntentFilter();
        filter.addAction("com.android.music.metachanged");
        filter.addAction("com.android.music.playstatechanged");
        filter.addAction("com.android.music.playbackcomplete");
        filter.addAction("com.android.music.queuechanged");
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        filter.addAction(Intent.ACTION_NEW_OUTGOING_CALL);

        registerReceiver(mReceiver, filter);

        mTelephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        mCallStateListener = new CallStateListener();
        mTelephonyManager.listen(mCallStateListener, PhoneStateListener.LISTEN_CALL_STATE);

        locationListener = new LocationListener()
        {
            @Override
            public void onLocationChanged (Location location)
            {
                String altitude = String.format("%5.0f", location.getAltitude() * 3.28084);
                String speed = String.format("%4.1f", location.getSpeed() * 2.237);

                //geoView.update(altitude, speed);

                //sppHUD.write(geoView.toHML().getBytes(), 0, geoView.toHML().length());
                String newHML = geoView.updateHML(altitude, speed);
                sppHUD.write(newHML.getBytes(), 0, newHML.length());
            }

            @Override
            public void onStatusChanged (String provider, int status, Bundle extras)
            {

            }

            @Override
            public void onProviderEnabled (String provider)
            {

            }

            @Override
            public void onProviderDisabled (String provider)
            {

            }
        };

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 0, locationListener);

        if (!mBluetoothAdapter.isEnabled())
        {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        sppController = new BlueSmirfSPP("Controller");
        sppHUD = new BlueSmirfSPP("HUD");

        controllerSwitch = (Switch) findViewById(R.id.controllerSwitch);
        HUDSwitch = (Switch) findViewById(R.id.HUDSwitch);
        connectButton = (Button) findViewById(R.id.connectButton);

        controllerSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged (CompoundButton buttonView, boolean isChecked)
            {
                if(isChecked)
                    connect(sppController, CONTROLLER_MAC, CONTROLLER_MESSAGE);
                else
                    disconnect(sppController);

            }
        });

        HUDSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged (CompoundButton buttonView, boolean isChecked)
            {
                if(isChecked)
                    connect(sppHUD, HUD_MAC, HUD_MESSAGE);
                else
                {
                    sppHUD.getReadThread().stopThread();
                    sppHUD.disconnect();
                }

            }
        });

        connectButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick (View v)
            {
                if(!controllerSwitch.isChecked())
                    controllerSwitch.setChecked(true);
                if(!HUDSwitch.isChecked())
                    HUDSwitch.setChecked(true);
            }
        });

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
                        for (int i = 0; i < msg.arg1; i++)
                        {
                            data += (((byte[]) msg.obj)[i]);
                        }

                        if(data != "" && data.length() >= 2)
                            buttonPress(data.charAt(0), data.charAt(1));

                        break;
                    }
                }
            }
        };

    }

    public boolean buttonPress (char button, char press)
    {
        switch (button)
        {
            case (TEAL):
            {
                if (press == '1')
                {
                    volumeChange(1);
                }
                else if (press == '2')
                {
                    changeScreen();
                }
                break;
            }

            case (GREEN):
            {
                if (press == '1')
                {
                    if (mTelephonyManager.getCallState() == TelephonyManager.CALL_STATE_RINGING)
                        answerCall();
                    else volumeChange(-1);
                }
                else if (press == '2')
                {
                    playPause();
                }
                break;
            }

            case (RED):
            {
                if (press == '1')
                {

                }
                else if (press == '2')
                {
                    if (mTelephonyManager.getCallState() == TelephonyManager.CALL_STATE_RINGING)
                        blockCall();
                    if (mTelephonyManager.getCallState() == TelephonyManager.CALL_STATE_OFFHOOK)
                        endCall();
                    else skip("previous");
                    break;
                }
                break;
            }

            case (BLUE):
            {
                if (press == '1')
                {

                }
                else if (press == '2')
                {
                    skip("next");
                    break;
                }
                break;
            }
        }
        return false;
    }

    public void changeScreen()
    {
        if(currentView == VIEW_GEO)
        {
            currentView = VIEW_MUSIC;
            locationManager.removeUpdates(locationListener);
        }
        else if(currentView == VIEW_MUSIC)
        {
            currentView = VIEW_GEO;
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 0, locationListener);
        }

        updateScreen(currentView);
    }

    public void showCallScreen()
    {
        locationManager.removeUpdates(locationListener);
        updateScreen(VIEW_CALL);
    }

    public void updateScreen(int view)
    {
        switch (view)
        {
            case (VIEW_GEO):
                sppHUD.write(geoView.toHML().getBytes(), 0, geoView.toHML().length());
                break;

            case (VIEW_MUSIC):
                sppHUD.write(musicView.toHML().getBytes(), 0, musicView.toHML().length());
                break;

            case (VIEW_CALL):
                sppHUD.write(callView.toHML().getBytes(), 0, callView.toHML().length());
                break;
        }

    }

    public void playPause ()
    {
        Intent i = new Intent("com.android.music.musicservicecommand");
        if (mAudioManger.isMusicActive())
        {
            i.putExtra("command", "pause");
        } else
        {
            i.putExtra("command", "play");
        }
        MainActivity.this.sendBroadcast(i);
    }

    public void volumeChange (int change)
    {
        int flag = 1;
        int stream = 0;
        if (mTelephonyManager.getCallState() == TelephonyManager.CALL_STATE_OFFHOOK)
            stream = AudioManager.USE_DEFAULT_STREAM_TYPE;
        else stream = AudioManager.STREAM_MUSIC;

        mAudioManger.adjustSuggestedStreamVolume(change, stream, flag);
    }

    public void skip (String direction)
    {
        Intent i = new Intent("com.android.music.musicservicecommand");
        i.putExtra("command", direction);
        MainActivity.this.sendBroadcast(i);
    }

    public void answerCall ()
    {
        makeToast("Attempting to Answer");
        Intent i = new Intent(Intent.ACTION_MEDIA_BUTTON);
        i.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_HEADSETHOOK));
        sendOrderedBroadcast(i, null);
    }

    public void endCall ()
    {
        makeToast("Attempting to end Call");
        Intent i = new Intent(Intent.ACTION_MEDIA_BUTTON);
        i.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_HEADSETHOOK));
        sendOrderedBroadcast(i, null);
    }

    public void blockCall ()
    {
        makeToast("Attempting to block Call");
        Intent i = new Intent(Intent.ACTION_MEDIA_BUTTON);
        i.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_CALL));
        sendOrderedBroadcast(i, null);
        sendOrderedBroadcast(i, null);
    }

    public void makeToast (CharSequence input)
    {
        Toast.makeText(getApplicationContext(), input, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item)
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

    public void connect (BlueSmirfSPP device, String mac, int type)
    {
        if (!device.connect(mac))
        {
            makeToast(device.getName() + " Not Found");

            if (device.getName() == "Controller") controllerSwitch.setChecked(false);
            else if (device.getName() == "HUD") HUDSwitch.setChecked(false);
        }

        else
        {
            makeToast(device.getName() + " Connected!");
            ReadThread readThread = new ReadThread(type);

            readThread.start();
            device.setReadThread(readThread);
            updateScreen(currentView);
        }

    }

    public void disconnect(BlueSmirfSPP device)
    {
        device.getReadThread().stopThread();
        device.disconnect();

        makeToast(device.getName() + " Disconnected");

        if(device.getName() == "Controller" && controllerSwitch.isChecked())
            controllerSwitch.setChecked(false);
        else if(device.getName() == "HUD" && HUDSwitch.isChecked())
            HUDSwitch.setChecked(false);
    }

    public String getContactName(Context context, String number) {

        String name = null;

        // define the columns I want the query to return
        String[] projection = new String[] {
                ContactsContract.PhoneLookup.DISPLAY_NAME,
                ContactsContract.PhoneLookup._ID};

        // encode the phone number and build the filter URI
        Uri contactUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));

        // query time
        Cursor cursor = context.getContentResolver().query(contactUri, projection, null, null, null);

        if(cursor != null) {
            if (cursor.moveToFirst())
            {
                name =      cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
            }
            else
            {
                name = "Unknown";
            }
            cursor.close();
        }
        return name;
    }

    public class ReadThread extends Thread
    {
        private int DEVICE_MESSAGE;
        private boolean read = true;

        public ReadThread (int deviceType)
        {
            DEVICE_MESSAGE = deviceType;
        }

        public void run ()
        {
            byte[] readVal = new byte[1024];
            int length;
            int bytes;

            while (read)
            {
                length = sppController.readByte();

                for (int i = 0; i < length; i++)
                {
                    readVal[i] = (byte) (sppController.readByte());
                }

                mMessageHandler.obtainMessage(DEVICE_MESSAGE, length, -1, readVal.clone()).sendToTarget();
            }
        }

        public void stopThread()
        {
            read = false;
        }
    }

    private class CallStateListener extends PhoneStateListener
    {
        boolean callState = false;
        String outgoing = " ";
        @Override
        public void onCallStateChanged (int state, String incomingNumber)
        {
            switch (state)
            {
                case TelephonyManager.CALL_STATE_RINGING:
                    // called when someone is ringing to this phone
                    makeToast("Incoming: " + incomingNumber);
                    callView.update(getContactName(getApplicationContext(), incomingNumber), incomingNumber);
                    showCallScreen();
                    callState = true;
                    break;

                case TelephonyManager.CALL_STATE_OFFHOOK:
                    makeToast("Answered");
                    showCallScreen();
                    break;

                case TelephonyManager.CALL_STATE_IDLE:

                    if(callState)
                    {
                        updateScreen(currentView);
                        if(currentView == VIEW_GEO)
                            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 0, locationListener);
                        callState = false;
                    }
                    break;
            }
        }

        public void setCallState(boolean cs)
        {
            callState = cs;
        }
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver()
    {

        @Override
        public void onReceive (Context context, Intent intent)
        {

            if(intent.getAction() == BluetoothDevice.ACTION_ACL_DISCONNECTED)
            {
                if(sppController.isError() && sppController.isConnected())
                    disconnect(sppController);
                if(sppHUD.isError() && sppHUD.isConnected())
                    disconnect(sppHUD);
            }
            else if (Intent.ACTION_NEW_OUTGOING_CALL.equals(intent.getAction()))
            {
                final String originalNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
                callView.update(getContactName(getApplicationContext(), originalNumber), originalNumber);
                showCallScreen();
                mCallStateListener.setCallState(true);
            }
            else
            {
                String action = intent.getAction();
                String cmd = intent.getStringExtra("command");
                Log.d("mIntentReceiver.onReceive ", action + " / " + cmd);
                String artist = intent.getStringExtra("artist");
                String album = intent.getStringExtra("album");
                String track = intent.getStringExtra("track");

                musicView.update(artist, track);
                updateScreen(currentView);
                ((TextView)(findViewById(R.id.nameText))).setText(track);
                ((TextView)findViewById(R.id.artistView)).setText(artist);
            }



        }
    };

}
