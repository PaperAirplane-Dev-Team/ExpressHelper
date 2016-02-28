package info.papdt.express.helper.support;

import android.util.Log;

import java.util.*;

import info.papdt.express.helper.api.KuaiDi100Helper;

public class ExpressResult {
	
	public int status, errCode, update, cache;
	public String message, html, mailNo, expSpellName, expTextName, ord;
	public ArrayList<Map<String, String>> data;

	public static final int STATUS_FAILED = 2, STATUS_NORMAL = 0, STATUS_ON_THE_WAY = 5,
		STATUS_DELIVERED = 3, STATUS_RETURNED = 4 /* RETURNING 6 */, STATUS_OTHER = 1;

	public ExpressResult() {
			data = new ArrayList<>();
		}

	public static ExpressResult buildFromJSON(String jsonStr) {
		return KuaiDi100Helper.buildDataFromResultStr(jsonStr);
	}

	public int getTrueStatus() {
		return status;
	}
		
}
