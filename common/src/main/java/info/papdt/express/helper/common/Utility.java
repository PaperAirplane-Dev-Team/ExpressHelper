package info.papdt.express.helper.common;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Random;

import info.papdt.express.helper.common.Settings;
import info.papdt.express.helper.common.api.ACKDHelper;

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
				secret = ACKDHelper.xfsecret;
				app_id = ACKDHelper.xfid;
				break;
			case 2:
				secret = ACKDHelper.smsecret;
				app_id = ACKDHelper.smid;
				break;
			case 3:
				secret = ACKDHelper.mysecret;
				app_id = ACKDHelper.myid;
				break;
			case 4:
				secret = ACKDHelper.e0secret;
				app_id = ACKDHelper.e0id;
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