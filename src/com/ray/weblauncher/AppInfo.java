package com.ray.weblauncher;

public class AppInfo{

	public String mTitle;
	public String mPackageName;
	public String mClassName;
	
	AppInfo(){
	}
	
	public String getIconName(){
		return mPackageName + ".png";
	}
}
