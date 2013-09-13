package com.ray.weblauncher.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import android.util.Log;

public class WebServer extends Thread {

	private static final String TAG = "WebLauncher";
	
	private ServerSocket mServerSocket = null;
	private volatile boolean bRunning = false;
	private String mRootDir;
	
	public WebServer(String rootDir){
		mRootDir = rootDir;
	}
	
	@Override
	public void run() {
		bRunning = true;
		try {
			mServerSocket = new ServerSocket(8888);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		while(bRunning){
			try {
				Socket socket = mServerSocket.accept();

				//Log.d(TAG, "New connection accepted " + socket.getInetAddress() + ":" + socket.getPort());
				new Thread(new HttpRequestHandler(socket, mRootDir)).start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		Log.e(TAG, "Server thread stopping...");
	}
	
	public synchronized void stopThread(){
		if (bRunning == false) {
			return;
		}
		bRunning = false;
		if (mServerSocket == null) {
			return;
		}
		try {
			mServerSocket.close();
		} catch (IOException e) {
			Log.e(TAG, "Error stoping server thread: ", e);
		}
	}

}
