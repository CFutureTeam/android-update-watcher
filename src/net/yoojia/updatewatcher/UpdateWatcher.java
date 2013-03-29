package net.yoojia.updatewatcher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

/**
 * 服务更新监视，一旦服务端返回更新数据，发送广播提示应用
 */
public class UpdateWatcher {
	
	static final String DATA_FIELD = "UPDATE_COUNT#o51LKJEviLKWE09c&f-df-fd90";
	static final String ERROR_FIELD = "UPDATE_ERROR#OILME321VOI446512%*!*@DR65V.*";
	static final String URL_FIELD = "UPDATE_URL#PLKNWEVNE(*&*@#$)(VN(*@#()FN";
	static final String KEY_FIELD = "UPDATE_KEY#)_(#LMV)(@#JV)(SJFOK:LAQWPI";
	
	static final String UPDATE_WATCHER_ACTION = "sg.ilovedeals.updatewatcher.watching-";
	
	static final int fixedThreadPoolSize = 3;
	
	public static boolean DEBUG = true;
	
	private Context context;
	private long period = 10*1000;
	private ResponseParser parser;
	private ConcurrentHashMap<String,String> targetUrls = new ConcurrentHashMap<String,String>();
	private ExecutorService executor = Executors.newFixedThreadPool(fixedThreadPoolSize);
	private Timer timer;
	
	public UpdateWatcher(Context context,ResponseParser parser){
		this.context = context;
		this.parser = parser;
	}
	
	public UpdateWatcher(Context context){
		this(context,new SimpleJSONParser());
	}
	
	/**
	 * 设置更新监听周期
	 * @param ms
	 */
	public void setPeriod(long ms){
		this.period = ms;
	}
	
	private class WatchingTask implements Runnable{
		final String taskUrl;
		final String key;
		public WatchingTask(String key,String url){
			this.taskUrl = url;
			this.key = key;
		}
		@Override
		public void run() {
			if(DEBUG){
				Log.d("UpdateWatcher", "Update-watching --> "+taskUrl);
			}
			try {
				URL targetUrl = new URL(taskUrl);
				HttpURLConnection connection = (HttpURLConnection) targetUrl.openConnection();
				connection.setConnectTimeout(10*1000);
				connection.setReadTimeout(1000);
				InputStream is = connection.getInputStream();
				int updateCount = parser.parser(toStringBuffer(is).toString());
				is.close();
				connection.disconnect();
				sendBroadcast(key,taskUrl, updateCount,null);
			} catch (Exception exp) {
				exp.printStackTrace();
				sendBroadcast(key,taskUrl, -1, exp.getMessage());
			}
		}
		
		StringBuffer toStringBuffer(InputStream is) throws IOException{
		    if( null == is) return null;
		    BufferedReader in = new BufferedReader(new InputStreamReader(is));
			StringBuffer buffer = new StringBuffer();
			String line = null;
			while ((line = in.readLine()) != null){
			      buffer.append(line).append("\n");
			}
			is.close();
			return buffer;
		}
	}
	
	/**
	 * 开启监听操作
	 */
	public void start(){
		if(timer == null){
			 timer = new Timer();
		}
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				Iterator<Map.Entry<String, String>> iterator = targetUrls.entrySet().iterator();
				while(iterator.hasNext()){
					Map.Entry<String, String> item = iterator.next();
					String url = item.getValue();
					String key = item.getKey();
					executor.submit(new WatchingTask(key,url));
				}
			}
		}, 100, period);
	}
	
	/**
	 * 注册一个监听地址
	 * @param url
	 * @param peroid
	 * @return
	 */
	public void registerWatchingUrl(String key,String url){
		targetUrls.put(key, url);
	}
	
	/**
	 * 取消监听某个URL的更新
	 * @param registerCode
	 */
	public void cancelWatchingUrl(Object key){
		targetUrls.remove(key);
	}
	
	/**
	 * 更新指定K对应的URL
	 * @param key
	 * @param newUrl
	 */
	public void updateWatchingUrl(String key,String newUrl){
		if(targetUrls.contains(key)){
			targetUrls.replace(key, newUrl);
		}else{
			registerWatchingUrl(key, newUrl);
		}
	}
	
	/**
	 * 取消全部监听。
	 */
	public void cancel(){
		if(timer != null){
			timer.cancel();
		}
		timer = null;
	}
	
	private void sendBroadcast(String key,String taskUrl,int data,String error){
		Intent intent = new Intent(UPDATE_WATCHER_ACTION);
		intent.putExtra(DATA_FIELD, data);
		intent.putExtra(URL_FIELD, taskUrl);
		intent.putExtra(KEY_FIELD, key);
		if(error != null){
			intent.putExtra(ERROR_FIELD, error);
		}
		context.sendBroadcast(intent);
	}
	
	/**
	 * 创建Intent过滤器
	 * @param registerCode
	 * @return
	 */
	public static IntentFilter createIntentFilter(){
		return new IntentFilter(UPDATE_WATCHER_ACTION);
	}
	
	
	/**
	 * 取得Data字段数据
	 * @param intent
	 * @return
	 */
	public static int getDataField(Intent intent){
		return intent.getIntExtra(DATA_FIELD, 0);
	}
	
	/**
	 * 取得RegisterURL字段数据
	 * @param intent
	 * @return
	 */
	public static String getRegisterURLField(Intent intent){
		return intent.getStringExtra(URL_FIELD);
	}
	
	/**
	 * 取得RegisterURL字段数据
	 * @param intent
	 * @return
	 */
	public static String getRegisterKeyField(Intent intent){
		return intent.getStringExtra(KEY_FIELD);
	}
	
	/**
	 * 取得异常信息
	 * @param intent
	 * @return
	 */
	public static String getErrorField(Intent intent){
		return intent.getStringExtra(ERROR_FIELD);
	}
}
