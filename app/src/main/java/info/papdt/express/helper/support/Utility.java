package info.papdt.express.helper.support;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.ConnectivityManager;
import android.os.Build;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Random;

import info.papdt.express.helper.api.KuaiDi100Helper;

public class Utility {

	public static boolean isChrome() {
		return Build.BRAND.equals("chromium") || Build.BRAND.equals("chrome");
	}

	public static int getStatusBarHeight(Context context) {
		int result = 0;
		int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
		if (resourceId > 0) {
			result = context.getResources().getDimensionPixelSize(resourceId);
		}
		return result;
	}

	public static boolean isApplicationAvailable(Context context, String packageName) {
		if (packageName == null || "".equals(packageName))
			return false;
		try {
			ApplicationInfo info = context.getPackageManager()
					.getApplicationInfo(
							packageName,
							PackageManager.GET_UNINSTALLED_PACKAGES
					);
			return true;
		} catch (PackageManager.NameNotFoundException e) {
			return false;
		}
	}

	public static void saveFile(Context context, String name, String text) throws IOException {
		FileOutputStream fos = context.openFileOutput(name, Context.MODE_PRIVATE);
		fos.write(text.getBytes());
		fos.close();
	}

	public static String readFile(Context context, String name) throws IOException{
		File file = context.getFileStreamPath(name);
		InputStream is = new FileInputStream(file);

		byte b[] = new byte[(int) file.length()];

		is.read(b);
		is.close();

		String string = new String(b);

		return string;
	}

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

	public static boolean isDisturbTime(Calendar c) {
		int hours = c.get(Calendar.HOUR_OF_DAY);
		return hours >= 23 | hours < 6;
	}

	public static HashMap<String, String> getAPIToken(Context context) {
		Settings mSets = Settings.getInstance(context);
		String secret, app_id;

		int choice = mSets.getInt(Settings.KEY_TOKEN_CHOOSE, 0);
		if (choice == 0) {
			Random r = new Random();
			choice = r.nextInt(3) + 1;
		}
		switch (choice) {
			case 1:
				secret = KuaiDi100Helper.xfsecret;
				app_id = KuaiDi100Helper.xfid;
				break;
			case 2:
				secret = KuaiDi100Helper.smsecret;
				app_id = KuaiDi100Helper.smid;
				break;
			case 3:
				secret = KuaiDi100Helper.mysecret;
				app_id = KuaiDi100Helper.myid;
				break;
			case 4:
				secret = KuaiDi100Helper.e0secret;
				app_id = KuaiDi100Helper.e0id;
				break;
			case 5:
			default:
				secret = mSets.getString(Settings.KEY_CUSTOM_SECRET, "error");
				app_id = mSets.getString(Settings.KEY_CUSTOM_ID, "error");
				break;
		}

		HashMap<String, String> result = new HashMap<>();
		result.put("id", app_id);
		result.put("secret", secret);
		return result;
	}

}