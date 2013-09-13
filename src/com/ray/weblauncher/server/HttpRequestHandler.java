package com.ray.weblauncher.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import com.ray.weblauncher.AppInfo;
import com.ray.weblauncher.AppManager;

import android.util.Log;

public class HttpRequestHandler implements Runnable {

	private static final String TAG = "WebLauncher";
	private final static String CRLF = "\r\n";
	
	private final static String HTTP_OK					= "200 OK";
	private final static String HTTP_BAD_REQUEST		= "400 Bad Request";
	private final static String HTTP_NOT_FOUND			= "404 Not Found";
	private final static String HTTP_METHOD_NOT_ALLOWED	= "405 Method Not Allowed";
	private final static String HTTP_SERVER_ERROR		= "500 Internal Server Error";
	
	private Socket			mSocket;
	private OutputStream	mOutput;
	private BufferedReader	mInputReader;
	private String 			mRootDir;
	
	private static Map<String, String> mimeTypes;
	static {
		mimeTypes = new HashMap<String, String>();
		mimeTypes.put("htm", "text/html");
		mimeTypes.put("css", "text/css");
		mimeTypes.put("js", "text/javascript");
		mimeTypes.put("html", "text/html");
		mimeTypes.put("xhtml", "text/xhtml");
		mimeTypes.put("txt", "text/html");
		mimeTypes.put("pdf", "application/pdf");
		mimeTypes.put("jpg", "image/jpeg");
		mimeTypes.put("gif", "image/gif");
		mimeTypes.put("png", "image/png");
	}

	
	public HttpRequestHandler(Socket socket, String rootDir) throws Exception{
		mSocket = socket;
		mRootDir = rootDir;
		
		mOutput = socket.getOutputStream();
		mInputReader = new BufferedReader(new InputStreamReader(mSocket.getInputStream()), 2 * 1024);
	}
	
	@Override
	public void run() {
		try {
			processRequest();
		} catch (Exception e) {
			Log.e(TAG, "Error in processing http request:", e);
		}
	}
	
	private void processRequest() throws Exception {
		String httpRequest = mInputReader.readLine();
		if (httpRequest == null) return;
		
		Log.d(TAG, "New HttpRequest: " + httpRequest);
		
		Map<String, String> responseHeader = new LinkedHashMap<String, String>();
		Map<String, String> paramMap = new HashMap<String, String>();
		
		StringTokenizer s = new StringTokenizer(httpRequest);
		String httpCommand = s.nextToken();
		String fileGet = URLDecoder.decode(s.nextToken(), "UTF-8");
		String paramStr = "";
		
		int idx = fileGet.indexOf("?");
		if (idx > 0){
			paramStr = fileGet.substring(idx + 1);
			fileGet = fileGet.substring(0, idx);
		}
		if (!paramStr.isEmpty()){
			String[] params = paramStr.split("&");
			for (String param : params){
				String[] keys = param.split("=");
				if (keys.length > 1){
					paramMap.put(keys[0], keys[1]);
				}
			}
		}
		
		String responseStatus = HTTP_BAD_REQUEST;
		String responseBody = "";
		FileInputStream fis = null;
		
		if (httpCommand.equals("GET")){
			if (fileGet.equals("/")){
				responseStatus = HTTP_OK;
				responseHeader.put("Content-Type", mimeTypes.get("html"));
				
				responseBody = getIndexPage();
			} else if (fileGet.equals("/main.css")){
				responseStatus = HTTP_OK;
				responseHeader.put("Content-Type", mimeTypes.get("css"));
				
				responseBody = getMainCSS();
			} else if (fileGet.equals("/main.js")){
				responseStatus = HTTP_OK;
				responseHeader.put("Content-Type", mimeTypes.get("js"));
				
				responseBody = getMainJS();
			} else if (fileGet.equals("/open")){
				responseStatus = HTTP_OK;
				responseHeader.put("Content-Type", mimeTypes.get("txt"));
				
				String appId = paramMap.get("app");
				if (appId != null){
					int i = Integer.valueOf(appId.substring(appId.indexOf("_") + 1)).intValue();
					AppManager.getInstance().openActivity(i);
				}
			} else {
				String fileName = mRootDir + fileGet;
				Log.d(TAG, "Request file: " + fileName);
				File file = new File(fileName);
				if (file.exists() && file.isFile()){
					fis = new FileInputStream(file);
					
					if (fis != null){
						responseStatus = HTTP_OK;
						responseHeader.put("Content-Type", getFileContentType(file.getName()));
						
						int lenght = fis.available();
						if (lenght > 0) {
							responseHeader.put("Content-Length", String.valueOf(lenght));
						}
					}
				} else {
					responseStatus = HTTP_NOT_FOUND;
					responseHeader.put("Content-Type", mimeTypes.get("html"));
				}
			}
		} else {
			responseStatus = HTTP_METHOD_NOT_ALLOWED;
			responseHeader.put("Content-Type", mimeTypes.get("html"));
		}
		
		// Response Status
		mOutput.write(("HTTP/1.0 " + responseStatus + CRLF).getBytes());
		// Response headers
		for (Entry<String, String> header : responseHeader.entrySet())
			mOutput.write((header.getKey() + ": " + header.getValue() + CRLF).getBytes());
		
		// Blank line to indicate the end of the response header.
		mOutput.write(CRLF.getBytes());

		if (!responseBody.isEmpty()){
			mOutput.write(responseBody.getBytes());
		} else if (fis != null){
			byte[] buffer = new byte[1024];
			int bytes = 0;

			while ((bytes = fis.read(buffer)) != -1) {
				mOutput.write(buffer, 0, bytes);
			}
			fis.close();
		}
		
		mOutput.close();
		mInputReader.close();
		mSocket.close();
	}
	
	private String getIndexPage(){
		StringBuffer html = new StringBuffer();
		ArrayList<AppInfo> appList = AppManager.getInstance().getAppList(false);
		if (!appList.isEmpty()){
			html.append("<HTML><HEAD><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/><TITLE>Web Launcher</TITLE>");
			html.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"main.css\">");
			html.append(CRLF);
			html.append("<script type=\"text/javascript\" src=\"http://ajax.googleapis.com/ajax/libs/jquery/1.10.2/jquery.min.js\"></script>");
			html.append(CRLF);
			html.append("<script type=\"text/javascript\" src=\"main.js\"></script>");
			html.append(CRLF);
			html.append("</HEAD><BODY>");
			html.append(CRLF);
			
			int i = 0;
			for (AppInfo app : appList){
				html.append("<div id=\"app_" + (i++) + "\" class=\"iconBox\"><img src=\"/");
				html.append(app.getIconName());
				html.append("\" width=\"72\" height=\"72\"/><div class=\"iconTitle\">");
				html.append(app.mTitle);
				html.append("</div></div>");
				html.append(CRLF);
			}
			html.append("</BODY></HTML>");
		}
		
		return html.toString();
	}
	
	private String getMainCSS(){
		return "body{font-family:Arial,sans-serif;font-size:12px;}.iconBox{width:90px;height:100px;float:left;text-align:center;cursor:pointer;padding:5px;}.iconTitle{height:20px;width:100%;padding-top:8px;}";
	}
	
	private String getMainJS(){
		return "$(document).ready(function(){$(\".iconBox\").click(function(){$.get(\"./open\",{app:this.id})})});";
	}
	
	private String getFileContentType(String fileName){
		String ext = "";
		int idx = fileName.lastIndexOf(".");
		if (idx >= 0) {
			ext = fileName.substring(idx + 1);
		}

		if (mimeTypes.containsKey(ext))
			return mimeTypes.get(ext);
		else
			return "application/octet-stream";
	}

}
