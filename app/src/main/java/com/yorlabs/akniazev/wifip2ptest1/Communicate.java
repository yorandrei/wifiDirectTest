package com.yorlabs.akniazev.wifip2ptest1;

import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDeviceList;

public interface Communicate {
    void GetAvailableDeviceList(WifiP2pDeviceList peerList);
    void GetClientList(String list,String gpowner,Boolean isGp);
    void notifyThisDeviceChanged(Intent intent);
}
