package info.papdt.express.helper.wearable.support;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Item.Result {
	
	public int status, errCode, update, cache;
	public String message, html, mailNo, expSpellName, expTextName, ord;
	public ArrayList<Map<String, String>> data;

	public static final int STATUS_FAILED = 0, STATUS_NORMAL = 1, STATUS_ON_THE_WAY = 2,
		STATUS_DELIVERED = 3, STATUS_RETURNED = 4, STATUS_OTHER = 5;

	public Item.Result() {
			data = new ArrayList<>();
		}

	public static Item.Result buildFromJSON(String jsonStr) {
		Item.Result result = new Item.Result();
		try {
			JSONObject person = new JSONObject(jsonStr);
			JSONArray array = person.getJSONArray("data");

			String json2;
			Map<String, String> map;

			result.status = person.getInt("status");
			result.errCode = person.getInt("errCode");
			result.message = person.getString("message");
			result.html = person.getString("html");
			result.mailNo = person.getString("mailNo");
			result.expSpellName = person.getString("expSpellName");
			result.expTextName = person.getString("expTextName");
			result.update = person.getInt("update");
			result.cache = person.getInt("cache");
			result.ord = person.getString("ord");

			for (int i = 0; i < array.length(); i++) {
				map = new HashMap<>();
				json2 = array.get(i).toString();
				JSONTokener jsonParser2 = new JSONTokener(json2);
				JSONObject person2 = (JSONObject) jsonParser2.nextValue();
				map.put("time", person2.getString("time"));
				map.put("context", person2.getString("context"));
				if (!person2.getString("context").contains("官网")) {
					result.data.add(map);
				}
			}

		} catch (JSONException e) {
			e.printStackTrace();
			result.status = 0;
			result.message = "JSON String token error!";
			return result;
		} catch (NullPointerException e) {
			e.printStackTrace();
			result.status = 0;
			result.message = "Unknown error!";
		}
		return result;
	}

	/** 为了解决奇葩的API把顺丰快递准备签收的状态当成已签收而设 F**k */
	public int getTrueStatus() {
		try {
			if (expSpellName.equals("shunfeng")) {
				if (data.get(data.size() - 1).get("context").contains("准备")) {
					return STATUS_ON_THE_WAY;
				} else {
					return status;
				}
			} else {
				if (data.get(data.size() - 1).get("context").contains("妥投") &&
						!data.get(data.size() - 1).get("context").contains("未")) {
					return STATUS_DELIVERED;
				} else {
					return status;
				}
			}
		} catch (Exception e) {
			return status;
		}
	}
		
}
