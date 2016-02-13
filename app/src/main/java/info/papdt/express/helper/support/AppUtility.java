package info.papdt.express.helper.support;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;

import info.papdt.expresshelper.common.Settings;

public class AppUtility {

	public static void startServiceAlarm(Context context, Class<?> service, long interval) {
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent(context, service);
		PendingIntent p = PendingIntent.getService(context, 10000, i, PendingIntent.FLAG_CANCEL_CURRENT);
		am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, 0, interval, p);
	}

	public static void stopServiceAlarm(Context context, Class<?> service) {
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent(context, service);
		PendingIntent p = PendingIntent.getService(context, 10000, i, PendingIntent.FLAG_CANCEL_CURRENT);
		am.cancel(p);
	}

	public static void startServices(Context context) {
		Settings settings = Settings.getInstance(context);
		int interval = getIntervalTime(settings.getInt(Settings.KEY_NOTIFICATION_INTERVAL, 0));

		if (interval > -1) {
			Log.i("Utils", "Interval : " + interval);
			startServiceAlarm(context, ReminderService.class, interval);
		}
	}

	public static void stopServices(Context context) {
		stopServiceAlarm(context, ReminderService.class);
	}

	public static void restartServices(Context context) {
		stopServices(context);

		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

		if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected()) {
			startServices(context);
		}
	}

	public static int getIntervalTime(int id) {
		switch (id){
			case 0:
				return 1 * 30 * 60 * 1000;
			case 1:
				return 1 * 60 * 60 * 1000;
			case 2:
				return 3 * 30 * 60 * 1000;
			case 3:
				return 3 * 60 * 60 * 1000;
			case 4:
				return -1;
		}
		return -1;
	}

}
