package com.ray.weblauncher;


import com.ray.weblauncher.server.WebServer;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class WLService extends Service {

    private static final String TAG = "WebLauncher";
    
    private WebServer mWebServer;
    
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		AppManager.getInstance().setContext(getApplicationContext());
		
		startServer();
	}

	@Override
	public void onDestroy() {
		stopServer();
		
		super.onDestroy();
	}
	
	public boolean startServer(){
		if (mWebServer == null || !mWebServer.isAlive()){
			mWebServer = new WebServer(getApplicationContext().getCacheDir().getAbsolutePath());
			mWebServer.start();
			Log.i(TAG, "WebLauncher Server started.");
			return true;
		}
		return false;
	}
	
	public boolean stopServer(){
		if (mWebServer == null) return false;
		
		if (mWebServer.isAlive()){
			mWebServer.stopThread();
			Log.i(TAG, "WebLauncher Server stopped.");
			mWebServer = null;
			return true;
		}
		
		mWebServer = null;
		return false;
	}
}
