package com.pedro.fitnessglobal;

import android.content.Context;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.os.Build;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

@CapacitorPlugin(name = "Headset")
public class HeadsetPlugin extends Plugin {

    @PluginMethod
    public void isConnected(PluginCall call) {
        AudioManager am = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        boolean connected = false;

        if (am != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                AudioDeviceInfo[] devices = am.getDevices(AudioManager.GET_DEVICES_OUTPUTS);
                for (AudioDeviceInfo d : devices) {
                    int t = d.getType();
                    if (t == AudioDeviceInfo.TYPE_WIRED_HEADPHONES
                            || t == AudioDeviceInfo.TYPE_WIRED_HEADSET
                            || t == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP
                            || t == AudioDeviceInfo.TYPE_BLUETOOTH_SCO
                            || t == AudioDeviceInfo.TYPE_USB_HEADSET
                            || t == AudioDeviceInfo.TYPE_USB_DEVICE) {
                        connected = true;
                        break;
                    }
                }
            } else {
                connected = am.isWiredHeadsetOn() || am.isBluetoothA2dpOn();
            }
        }

        JSObject ret = new JSObject();
        ret.put("connected", connected);
        call.resolve(ret);
    }
}
