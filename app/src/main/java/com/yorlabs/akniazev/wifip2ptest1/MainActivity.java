package com.yorlabs.akniazev.wifip2ptest1;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements Communicate{

    public static final String TAG ="MainActivity";
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    BroadcastReceiver mReceiver;
    private boolean isWifiP2pEnabled = false;
    IntentFilter mIntentFilter;
    Button Discover,CreateGP,Disconnect;
    TextView textview;
    ListView listview;
    public List<WifiP2pDevice> availableDevices = new ArrayList<WifiP2pDevice>();
    ArrayList devicename;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialization();
        createButtons();
    }

    private void createButtons() {
        //Discovery button
        Discover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPeerDiscovery();
            }
        });
        //Create grp button
        CreateGP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateGroup();
            }
        });
        //Disconnect button
        Disconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disconnectingToPeers();
            }
        });
    }

    private void initialization(){
        //Manager,channel,broadcast reciever
        mManager = (WifiP2pManager) getSystemService(this.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this);

        //intent filters
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        //buttons
        Discover=(Button)findViewById(R.id.discover);
        CreateGP=(Button)findViewById(R.id.creategp);
        Disconnect=(Button)findViewById(R.id.disconnect);

        //text views
        textview=(TextView)findViewById(R.id.textView);

        //List view
        listview=(ListView)findViewById(R.id.listview);
    }

    /* register the broadcast receiver with the intent values to be matched */
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, mIntentFilter);
    }

    /* unregister the broadcast receiver */
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    /**************************************Functions********************************************/
    //setIsWifiP2pEnabled
    //startPeerDiscovery
    //connectingToPeers
    //disconnectingToPeers
    //CreateGroup

    public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
        this.isWifiP2pEnabled = isWifiP2pEnabled;
        if(isWifiP2pEnabled==false){
            if (mManager != null && mChannel != null) {
                Toast.makeText(MainActivity.this,"Please enable Wi-Fi",Toast.LENGTH_LONG).show();
            } else {
                Log.e(TAG, "channel or manager is null");
            }
        }
    }
    public  void  startPeerDiscovery(){
        if (!isWifiP2pEnabled) {
            Toast.makeText(MainActivity.this, "P2P is off",
                    Toast.LENGTH_SHORT).show();
        }
        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(MainActivity.this, "Discovering",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int reasonCode) {
                Toast.makeText(MainActivity.this, "Error while discovering: "+reasonCode,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void connectingToPeers(int position, final String name) {
        Toast.makeText(this,"connecting "+name,Toast.LENGTH_LONG).show();
        final WifiP2pDevice device=availableDevices.get(position);
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Toast.makeText(getBaseContext(),"connected to "+ device.deviceName,Toast.LENGTH_LONG).show();
                onConnected();
                //success logic
            }


            @Override
            public void onFailure(int reason) {
                Toast.makeText(getBaseContext(),"Error while connecting:"+ reason,Toast.LENGTH_LONG).show();
                //failure logic
            }
        });
    }

    private void disconnectingToPeers() {
        if (mManager != null && mChannel != null) {
            mManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {
                @Override
                public void onFailure(int reasonCode) {
                    Log.d(TAG, "Disconnect failed. Reason :" + reasonCode);
                }
                @Override
                public void onSuccess() {
                    Toast.makeText(getBaseContext(),"Succesfully disconnected",Toast.LENGTH_LONG).show();
                    onDisconnected();

                }
            });
        }else {
            Log.e(MainActivity.TAG,"something is null");
        }
    }

    public void CreateGroup() {
        mManager.createGroup(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(MainActivity.this,"Group created",Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(MainActivity.this,"Error creating group",Toast.LENGTH_LONG).show();
            }
        });
    }

    private String getDeviceStatus(int deviceStatus) {
        switch (deviceStatus) {
            case WifiP2pDevice.AVAILABLE:
                onDisconnected();
                return getString(R.string.available);
            case WifiP2pDevice.INVITED:
                return getString(R.string.invited);
            case WifiP2pDevice.CONNECTED:
                onConnected();
                return getString(R.string.connected);
            case WifiP2pDevice.FAILED:
                return getString(R.string.failed);
            case WifiP2pDevice.UNAVAILABLE:
                return getString(R.string.unavailable);
            default:
                return getString(R.string.unknown);
        }
    }


    /*****************************************ON ACTIONS************************************************/
    //onConnected
    //onDisconnected

    private void onConnected() {
        listview.setVisibility(View.GONE);
        textview.setText("connected");
    }
    private void onDisconnected() {
        listview.setVisibility(View.VISIBLE);
        textview.setText("disconnected");
    }

    /*****************************************Interface methods***************************************/
    //GetAvailableDeviceList
    @Override
    public void GetAvailableDeviceList(WifiP2pDeviceList peerList) {
        List<WifiP2pDevice> refreshedPeers = new ArrayList<WifiP2pDevice>(peerList.getDeviceList());
        if (!refreshedPeers.equals(availableDevices)) {
            availableDevices.clear();
            availableDevices.addAll(refreshedPeers);
            devicename=new ArrayList();
            for(int i=0;i<availableDevices.size();i++){
                devicename.add(availableDevices.get(i).deviceName);
            }
            ArrayAdapter arrayAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,devicename);
            listview.setAdapter(arrayAdapter);
            listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    connectingToPeers(position,devicename.get(position).toString());
                }
            });
        }
    }

    @Override
    public void GetClientList(String list,String gpowner,Boolean isGp) {
        Log.e(TAG,"group ownr:"+gpowner+"\n"+list);
        if(isGp){
            textview.setText("connected as group owner");
        }else{
            textview.setText("connected as Client");
        }

    }

    @Override
    public void notifyThisDeviceChanged(Intent intent) {
        WifiP2pDevice wifiP2pDevice = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
        getDeviceStatus(wifiP2pDevice.status);
    }
}
