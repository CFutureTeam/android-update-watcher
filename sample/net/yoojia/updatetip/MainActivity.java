package net.yoojia.updatetip;

import net.yoojia.updatewatcher.UpdateWatcher;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends Activity {

	private UpdateWatcherBroadcastRecever reveiver;
	
	private String url_1 = "http://api.sytime.com:8088/goods/latest?type=today&timestamp=1363708333609&datatype=json";
	private String url_2 = "http://api.sytime.com:8088/goods/latest?type=all&timestamp=1363708333609&datatype=json";
	private String url_3 = "http://api.sytime.com:8088/goods/latest?type=feature&timestamp=1363708333609&datatype=json";
	
	UpdateWatcher watcher;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		reveiver = new UpdateWatcherBroadcastRecever();
		watcher = new UpdateWatcher(this);
		watcher.registerWatchingUrl(url_1,url_1);
		watcher.registerWatchingUrl(url_2,url_2);
		watcher.registerWatchingUrl(url_3,url_3);
		
	}
	
	class UpdateWatcherBroadcastRecever extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			Toast.makeText(context, "返回数据:"+UpdateWatcher.getDataField(intent), Toast.LENGTH_SHORT).show();
		}
		
	}


	@Override
	public void onResume(){
		super.onResume();
		registerReceiver(reveiver, UpdateWatcher.createIntentFilter());
		
		watcher.start();
	}
	
	@Override
	public void onPause(){
		super.onPause();
		watcher.cancel();
	}
}
