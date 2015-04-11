package info.papdt.express.helper.support;

import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import info.papdt.express.helper.api.KuaiDi100Helper;

public class Express {

	private String companyCode, mailNumber, name;
	private String jsonData = null, lastJsonData = null;
	private int lastStatus = ExpressResult.STATUS_OTHER;
	public boolean shouldPush = true, needPush = true;

	public Express(String companyCode, String mailNumber){
		this(companyCode, mailNumber, mailNumber);
	}

	public Express(String companyCode, String mailNumber, String name){
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

	public ExpressResult getData() {
		return ExpressResult.buildFromJSON(jsonData);
	}

	public ExpressResult getLastData() {
		return ExpressResult.buildFromJSON(lastJsonData);
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

	public JSONObject toJSONObject() {
		JSONObject obj0 = new JSONObject();
		try {
			obj0.put("name", getName());
			obj0.put("companyCode", getCompanyCode());
			obj0.put("mailNumber", getMailNumber());
			obj0.put("cache", getDataStr());
			obj0.put("lastCache", getLastDataStr());
			obj0.put("lastStatus", getLastStatus());
			obj0.put("needPush", needPush);
			obj0.put("shouldPush", shouldPush);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return obj0;
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

}
