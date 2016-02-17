package info.papdt.express.helper.common.model;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Map;

public class Item {

	private String companyCode, mailNumber, name;
	@SerializedName("cache")
	private String jsonData = null;
	@SerializedName("lastCache")
	private String lastJsonData = null;
	private int lastStatus = Result.STATUS_OTHER;
	public boolean shouldPush = true, needPush = false;

	public Item(String companyCode, String mailNumber){
		this(companyCode, mailNumber, mailNumber);
	}

	public Item(String companyCode, String mailNumber, String name){
		this.companyCode = companyCode;
		this.mailNumber = mailNumber;
		this.name = name;
	}

	public String getDataStr() {
		return jsonData;
	}

	public void setData(String jsonStr) {
		this.jsonData = jsonStr;
	}

	public String getLastDataStr() {
		return lastJsonData;
	}

	public void setLastData(String lastJsonStr) {
		this.lastJsonData = lastJsonStr;
		if (this.lastJsonData != null) {
			this.lastStatus = getLastData().getTrueStatus();
		}
	}

	public int getLastStatus() {
		return lastStatus;
	}

	public Result getData() {
		return new Gson().fromJson(jsonData, Result.class);
	}

	public Result getLastData() {
		return new Gson().fromJson(lastJsonData, Result.class);
	}

	public String getCompanyCode(){
		return companyCode;
	}

	public String getMailNumber(){
		return mailNumber;
	}

	public void setCompanyCode(String companyCode){
		this.companyCode = companyCode;
	}

	public void setMailNumber(String mailNumber){
		this.mailNumber = mailNumber;
	}

	public String toJsonStr() {
		return new Gson().toJson(this);
	}

	public static Item buildFromJSONObject(String json) {
		return new Gson().fromJson(json, Item.class);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		if (name == null) {
			this.name = mailNumber;
			return;
		} else if (TextUtils.isEmpty(name)) {
			this.name = mailNumber;
			return;
		}
		this.name = name;
	}

	public static class Result {

		/** 爱查快递数据格式 */
		public int status, errCode, update, cache;
		public String message, html, mailNo, expSpellName, expTextName, ord;
		public ArrayList<Map<String, String>> data;

		public static final int STATUS_FAILED = 0, STATUS_NORMAL = 1, STATUS_ON_THE_WAY = 2,
				STATUS_DELIVERED = 3, STATUS_RETURNED = 4, STATUS_OTHER = 5;

		public Result() {
			data = new ArrayList<>();
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

		public static Result buildFromJSON(String json) {
			return new Gson().fromJson(json, Result.class);
		}

	}

}
