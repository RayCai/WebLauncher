package com.ray.weblauncher.server;

import java.util.HashMap;

public class FileCache {

	private static FileCache instance = new FileCache();
	
	private HashMap<String, byte[]> fileMapping = new HashMap<String, byte[]>();
	
	private FileCache(){
	}
	
	public static FileCache getInstance(){
		return instance;
	}
	
	public void addFile(String fileName, byte[] data){
		fileMapping.put(fileName, data);
	}
}
