package net.yoojia.updatewatcher;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class SimpleJSONParser implements ResponseParser {

	@Override
	public int parser(String response) {
//		{"success":true,"data":{"count":"0"}}
		JSONTokener tokener = new JSONTokener(response);
		int updateCount = 0;
		try {
			JSONObject json = (JSONObject) tokener.nextValue();
			if(json.getBoolean("success")){
				JSONObject data = json.getJSONObject("data");
				updateCount = data.getInt("count");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return updateCount;
	}

}
