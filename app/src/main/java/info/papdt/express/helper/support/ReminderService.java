package info.papdt.express.helper.support;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.util.Calendar;

import info.papdt.express.helper.R;
import info.papdt.express.helper.ui.DetailsActivity;
import info.papdt.expresshelper.common.Settings;
import info.papdt.expresshelper.common.Utility;
import info.papdt.expresshelper.common.model.Item;
import info.papdt.expresshelper.common.model.ItemsKeeper;

@SuppressWarnings("ALL")
public class ReminderService extends IntentService {

	private static final String TAG = ReminderService.class.getSimpleName();

	private static final int ID = 100000;

	private Notification produceNotifications(int position, Item exp) {
		if (exp != null) {
			int defaults = parseDefaults(getApplicationContext());

			PendingIntent pi;

			Intent i = new Intent(getApplicationContext(), DetailsActivity.class);
			i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			i.putExtra("id", position);
			i.putExtra("data", exp.toJsonStr());

			pi = PendingIntent.getActivity(getApplicationContext(), position, i, PendingIntent.FLAG_UPDATE_CURRENT);

			String title = exp.getName();
			if (exp.getData().getTrueStatus() == Item.Result.STATUS_DELIVERED) {
				title += getString(R.string.notification_delivered);
			} else {
				if (exp.getData().getTrueStatus() == Item.Result.STATUS_ON_THE_WAY) {
					title += getString(R.string.notification_on_the_way);
				} else {
					title += getString(R.string.notification_new_message);
				}
			}

			int smallIcon;
			switch (exp.getData().getTrueStatus()) {
				case Item.Result.STATUS_DELIVERED:
					smallIcon = R.drawable.ic_done_white_24dp;
					break;
				case Item.Result.STATUS_ON_THE_WAY:
					smallIcon = R.drawable.ic_assignment_turned_in_white_24dp;
					break;
				default:
					smallIcon = R.drawable.ic_assignment_returned_white_24dp;
			}

			Notification n = buildNotification(getApplicationContext(),
						title,
						exp.getData().data.get(exp.getData().data.size() - 1).get("context"),
						Build.VERSION.SDK_INT < 20 ? R.drawable.ic_local_shipping_white_24dp
								: R.drawable.ic_local_shipping_black_24dp,
						smallIcon,
						getResources().getIntArray(R.array.statusColor) [exp.getData().getTrueStatus()],
						defaults,
						pi,
						null);

			n.tickerText = title;

			return n;
		}
		return null;
	}

	public ReminderService() {
		super(TAG);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onHandleIntent(Intent intent) {
		boolean isEnabledDontDisturbMode = Settings.getInstance(getApplicationContext())
				.getBoolean(Settings.KEY_NOTIFICATION_DO_NOT_DISTURB, true);
		if (isEnabledDontDisturbMode && Utility.isDisturbTime(Calendar.getInstance())) {
			Log.i(TAG, "现在是勿扰时间段，跳过检查。");
			return;
		}

		NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		ItemsKeeper db = ItemsKeeper.getInstance(getApplicationContext());

		db.pullNewDataFromNetwork(false);
		try {
			db.save();
		} catch (IOException e) {
			e.printStackTrace();
		}

		for (int i = 0; i < db.size(); i++) {
			Item exp = db.getItem(i);
			if (exp.getData().getTrueStatus() != Item.Result.STATUS_FAILED && !exp.needPush && exp.shouldPush) {
				if (exp.getLastStatus() == Item.Result.STATUS_DELIVERED) continue;
				Notification n = produceNotifications(i, exp);
				if (exp != null) {
					nm.notify(i + 20000, n);
					exp.needPush = false;
				}
			}
		}

		try {
			db.save();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static int parseDefaults(Context context) {
		Settings settings = Settings.getInstance(context);

		return (settings.getBoolean(Settings.KEY_NOTIFICATION_SOUND, true) ? Notification.DEFAULT_SOUND : 0) |
				(settings.getBoolean(Settings.KEY_NOTIFICATION_VIBRATE, true) ? Notification.DEFAULT_VIBRATE : 0) |
				Notification.DEFAULT_LIGHTS;
	}

	@SuppressWarnings("getNotification")
	private static Notification buildNotification(Context context, String title, String text, int icon0, int icon1, int color,
	                                              int defaults, PendingIntent contentIntent, PendingIntent deleteIntent) {
		Notification n;
		Notification.Builder builder = new Notification.Builder(context)
				.setContentTitle(title)
				.setContentText(text)
				.setSmallIcon(icon1)
				.setLargeIcon(((BitmapDrawable) context.getResources().getDrawable(icon0)).getBitmap())
				.setDefaults(defaults)
				.setAutoCancel(true)
				.setContentIntent(contentIntent);

		if (Build.VERSION.SDK_INT >= 16) {
			if (Build.VERSION.SDK_INT >= 21) {
				builder.setColor(color);
			}
			n = builder.build();
		} else {
			n = builder.getNotification();
		}

		return n;
	}

}