package com.ray.weblauncher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class WLReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)){
			if (isWifiConnected(context))
				context.startService(new Intent(context, WLService.class));
		}
		else if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)){
			if (isWifiConnected(context))
				context.startService(new Intent(context, WLService.class));
			else{
				context.stopService(new Intent(context, WLService.class));
			}
		}
	}

	private boolean isWifiConnected(Context context){
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService( Context.CONNECTIVITY_SERVICE );
	    NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
	    
	    if (activeNetInfo != null && activeNetInfo.isAvailable() && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI)
	    {
	    	return true;
	    }
	    return false;
	}
}
