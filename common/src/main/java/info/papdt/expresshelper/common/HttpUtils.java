package info.papdt.expresshelper.common;

import android.util.Log;

import com.google.gson.Gson;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

import info.papdt.expresshelper.common.model.Message;

public class HttpUtils {

	public static final String TAG = "HttpUtils";
	public static final int CODE_OKAY = 0, CODE_NETWORK_ERROR = -1, CODE_CLIENT_ERROR = -2,
			CODE_NONE_200 = 1;

	public static final String COMPUTER_UA = "Mozilla/5.0 (Windows NT 6.1)" +
			" AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.89 Safari/537.36";

	public static <OBJ> Message<OBJ> getSimpleUrlResult(String url, Class<OBJ> obj) {
		Log.i(TAG, url);

		Request request = new Request.Builder()
				.url(url)
				.header("User-Agent", COMPUTER_UA)
				.build();
		Log.i(TAG, "Set up the request" + request.toString());

		Message<OBJ> msg = new Message<>();
		try {
			Response response = new OkHttpClient().newCall(request).execute();
			Log.i(TAG, "Get response:" + response.code());
			String result = response.body().string();
			Log.i(TAG, result);
			msg.setObject(new Gson().fromJson(result, obj));
			msg.setCode(CODE_OKAY);
		} catch (IOException e) {
			e.printStackTrace();
			msg.setCode(CODE_NETWORK_ERROR);
		}

		return msg;
	}

	public static Message<String> getString(String url) {
		Log.i(TAG, url);

		Request request = new Request.Builder()
				.url(url)
				.header("User-Agent", COMPUTER_UA)
				.build();
		Log.i(TAG, "Set up the request" + request.toString());

		Message<String> msg = new Message<>();
		try {
			Response response = new OkHttpClient().newCall(request).execute();
			Log.i(TAG, "Get response:" + response.code());
			String result = response.body().string();
			Log.i(TAG, result);
			msg.setObject(result);
			msg.setCode(CODE_OKAY);
		} catch (IOException e) {
			e.printStackTrace();
			msg.setCode(CODE_NETWORK_ERROR);
		}

		return msg;
	}

}
