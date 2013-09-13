package com.ray.weblauncher;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;


public class AppManager {

	private static final String TAG = "WebLauncher";
	private static AppManager instance = new AppManager();
	
	private Context		mContext;
    private ArrayList<AppInfo> mAppList = new ArrayList<AppInfo>();
	
	private AppManager() {
		
	}
	
	public static AppManager getInstance() {
		return instance;
	}
	
	public void setContext(Context context) {
		mContext = context;
	}
	
	public ArrayList<AppInfo> getAppList(boolean bForceToReload) {
		if (mContext == null) return null;
		
		synchronized (mAppList) {
			if (!bForceToReload && !mAppList.isEmpty()) return mAppList;
			
			PackageManager manager = mContext.getPackageManager();
			
			Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
			mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
			
			final List<ResolveInfo> apps = manager.queryIntentActivities(mainIntent, 0);
			Collections.sort(apps, new ResolveInfo.DisplayNameComparator(manager));
			
			if (apps != null) {
				final int count = apps.size();
				
				if (mAppList == null) {
					mAppList = new ArrayList<AppInfo>(count);
				}
				mAppList.clear();
				
				for (ResolveInfo info : apps) {
					AppInfo app = new AppInfo();
					app.mTitle = info.loadLabel(manager).toString().trim();
					app.mPackageName = info.activityInfo.applicationInfo.packageName;
					app.mClassName = info.activityInfo.name;
					
					File file = new File(mContext.getCacheDir(), app.getIconName());
					if(!file.exists()){ 
						cacheIcon(file, info.activityInfo.loadIcon(manager));
					}
					
					mAppList.add(app);
				}
			}
			return mAppList;
		}
	}
	
	public void openActivity(int idx) {
		if (mContext == null) return;

		synchronized (mAppList) {
			if (idx >=0 && idx < mAppList.size()) {
				AppInfo app = mAppList.get(idx);
				
				Intent intent = new Intent(Intent.ACTION_MAIN);
				intent.addCategory(Intent.CATEGORY_LAUNCHER);
				intent.setComponent(new ComponentName(app.mPackageName, app.mClassName));
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
				
				mContext.startActivity(intent);
			}
		}
	}
	
	private void cacheIcon(File file, Drawable icon){
		Log.d(TAG, "Cache icon: " + file.getAbsolutePath());
		try{
			file.createNewFile();
		} catch(Exception e){
			e.printStackTrace();
		}

		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
			
			BitmapDrawable bitmap = (BitmapDrawable)icon;
			
			ByteArrayOutputStream baops = new ByteArrayOutputStream();
			bitmap.getBitmap().compress(CompressFormat.PNG, 100, baops);
			
			fos.write(baops.toByteArray());
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			try {
				if(fos != null){
					fos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
