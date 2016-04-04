package info.papdt.express.helper.common.model;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import info.papdt.express.helper.common.HttpUtils;
import info.papdt.express.helper.common.Utility;
import info.papdt.express.helper.common.api.KuaiDi100Helper;

public class ItemsKeeper {

	private ArrayList<Item> mItemArray, array_ur, array_ok;
	private Context context;

	private final static String TAG = "ItemsKeeper";

	private static ItemsKeeper sInstance;

	public ItemsKeeper(Context context) {
		this.context = context;
		mItemArray = new ArrayList<>();
	}

	public static ItemsKeeper getInstance(Context context) {
		if (sInstance == null) {
			sInstance = new ItemsKeeper(context);
			sInstance.init();
		}
		return sInstance;
	}

	public void addItem(String jsonStr, String name) {
		Item.Result res = new Gson().fromJson(jsonStr, Item.Result.class);
		Item exp;
		if (name != null) {
			exp = new Item(res.expSpellName, res.mailNo, name);
		} else {
			exp = new Item(res.expSpellName, res.mailNo);
		}
		exp.setData(jsonStr);
		this.addItem(exp);
	}

	public void addItem(Item express) {
		mItemArray.add(express);
	}

	public Item getItem(int index){
		return mItemArray.get(index);
	}

	public void deleteItem(int position){
		mItemArray.remove(position);
	}

	public int size() {
		return mItemArray.size();
	}

	public ArrayList<Item> getUnreceivedArray() {
		return array_ur;
	}

	public ArrayList<Item> getReceivedArray() {
		return array_ok;
	}

	public int urSize() {
		return array_ur.size();
	}

	public int okSize() {
		return array_ok.size();
	}

	public void calcItem() {
		array_ok = new ArrayList<>();
		array_ur = new ArrayList<>();
		for (int i = 0; i < size(); i++) {
			Item exp = getItem(i);
			Item.Result cache = exp.getData();
			if (cache.getTrueStatus() == 3) {
				array_ok.add(exp);
			} else {
				array_ur.add(exp);
			}
		}
	}

	public int findItem(String companyCode, String mailNumber){
		boolean ok = false;
		int i;

		for (i = 0; i < mItemArray.size(); i++){
			if (mItemArray.get(i).getCompanyCode().equals(companyCode)
					&& mItemArray.get(i).getMailNumber().equals(mailNumber)){
				ok = true;
				break;
			}
		}

		if (ok) return i; else return -1;
	}

	public void init() {
		mItemArray = null;
		String jsonData;
		try {
			jsonData = Utility.readFile(context, "data.json");
		} catch (IOException e) {
			jsonData = "{\"data\":[]}";
			Log.i(TAG, "文件不存在,初始化新的文件.");
			e.printStackTrace();
		}
		Log.i(TAG, "读入json数据结果:");
		Log.i(TAG, jsonData);

		mItemArray = new Gson().fromJson(jsonData, Data.class).data;

		Log.i(TAG, "okay");

		calcItem();
	}

	public void clear() {
		mItemArray = new ArrayList<>();
	}

	public void save() throws IOException{
		Data d = new Data();
		d.data = mItemArray;
		Utility.saveFile(context, "data.json", new Gson().toJson(d));
	}

	public void pullNewDataFromNetwork(boolean refreshDelivered) {
		for (Item nowExp : mItemArray) {
			if (!refreshDelivered && nowExp.getData().getTrueStatus() == Item.Result.STATUS_DELIVERED) {
				continue;
			}
			String result = getDataFromNetwork(nowExp.getCompanyCode(), nowExp.getMailNumber());
			if (result != null) {
				try {
					nowExp.setLastData(nowExp.getDataStr());
					if (nowExp.getLastData().data.size() != nowExp.getData().data.size()) nowExp.needPush = true;
				} catch (Exception e) {
					e.printStackTrace();
				}
				nowExp.setData(result);
			}
		}
	}

	public String getDataFromNetwork(String companyCode, String mailNumber) {
		HashMap<String, String> token = Utility.getAPIToken(context);
		String app_id = token.get("id");
		String secret = token.get("secret");

		Message<String> msg = HttpUtils.getString(
				KuaiDi100Helper.getRequestUrl(
						app_id,
						secret,
						companyCode,
						mailNumber,
						"utf8"
				)
		);
		switch (msg.getCode()) {
			case HttpUtils.CODE_OKAY:
				return msg.getObject();
			default:
				return null;
		}
	}

	private class Data {

		ArrayList<Item> data;

	}

}
