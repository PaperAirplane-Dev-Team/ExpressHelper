package info.papdt.express.helper.support;

import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class HttpUtils {

	public static final String TAG = "HttpUtils";
	public static final int CODE_OKAY = 0, CODE_NETWORK_ERROR = -1, CODE_CLIENT_ERROR = -2,
			CODE_NONE_200 = 1;

	public static int get(String url, String[] result) {
		HttpResponse httpResponse;
		try {
			Log.v(TAG, "HTTP请求:" + url);
			HttpGet httpGet = new HttpGet(url);
			httpGet.setHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.116 Safari/537.36");
			httpGet.setHeader("Referer", "http://www.kuaidi100.com/");
			httpGet.setHeader("X-Requested-With", "XMLHttpRequest");
			httpGet.setHeader("Accept-Language", "zh-CN,zh;q=0.8,en;q=0.6");
			// httpGet.setHeader("Accept-Encoding", "gzip, deflate, sdch");
			httpGet.setHeader("Accept", "*/*");
			httpResponse = new DefaultHttpClient().execute(httpGet);
			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				result[0] = EntityUtils.toString(httpResponse.getEntity(),"UTF-8");
				Log.v(TAG, "返回结果为" + result[0]);
				return CODE_OKAY;
			} else {
				return CODE_NONE_200;
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			return CODE_CLIENT_ERROR;
		} catch (IOException e) {
			e.printStackTrace();
			return CODE_NETWORK_ERROR;
		}
	}

}
