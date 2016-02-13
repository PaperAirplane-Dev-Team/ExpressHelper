package info.papdt.express.helper.support;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;

public class ConnectivityReceiver extends BroadcastReceiver {

	public static boolean isWIFI = true;

	@Override
	public void onReceive(Context context, Intent intent) {
		if (readNetworkState(context)) {
			Log.i("ConnectivityReceiver", "Start ReminderService.");
			AppUtility.startServices(context);
		} else {
			Log.i("ConnectivityReceiver", "Stop ReminderService.");
			AppUtility.stopServices(context);
		}
	}

	public static boolean readNetworkState(Context context) {
		if (context == null) return false;

		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

		if (cm != null && cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected()) {
			isWIFI = (cm.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_WIFI);
			return true;
		} else {
			return false;
		}
	}

}