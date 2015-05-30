package info.papdt.express.helper.support.wearable;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.concurrent.TimeUnit;

import info.papdt.express.helper.dao.ExpressDatabase;

public class RefreshListener extends WearableListenerService
		implements GoogleApiClient.ConnectionCallbacks,
		GoogleApiClient.OnConnectionFailedListener {

	private static final String TAG = "Wearable";
	private GoogleApiClient mGoogleApiClient;

	@Override
	public void onCreate() {
		Log.i(TAG, "onCreate RefreshListener");
		super.onCreate();
		mGoogleApiClient = new GoogleApiClient.Builder(this)
				.addApi(Wearable.API)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.build();
		mGoogleApiClient.connect();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null) {
			if (Constants.ACTION_REFRESH.equals(intent.getAction())) {
				Log.i(TAG, "onStartCommand ACTION_REFRESH");
				sendAllData();
			}
		}
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDataChanged(DataEventBuffer dataEvents) {
		Log.i(TAG, "onDataChanged");
		for (DataEvent event : dataEvents) {
			if (event.getType() == DataEvent.TYPE_CHANGED) {
				DataMap dataMap = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
				String action = dataMap.getString(Constants.EXTRA_EH_ACTION);
				if (action.equals(Constants.EH_ACTION_REFRESH)) {
					sendAllData();
				}
			}
		}
	}

	@Override
	public void onConnected(Bundle bundle) {
		Log.i(TAG, "onConnected");
		Uri dataItemUri = new Uri.Builder()
				.scheme(PutDataRequest.WEAR_URI_SCHEME)
				.path(Constants.PATH)
				.build();
		Wearable.DataApi.deleteDataItems(mGoogleApiClient, dataItemUri);
	}

	@Override
	public void onConnectionSuspended(int i) {

	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {

	}

	private void sendAllData() {
		new Thread() {
			@Override
			public void run() {
				ConnectionResult connectionResult =
						mGoogleApiClient.blockingConnect(30, TimeUnit.SECONDS);

				if (!connectionResult.isSuccess()) {
					Log.e(TAG, "Failed to connect to GoogleApiClient.");
					return;
				}

				Log.i(TAG, "Received ACTION_REFRESH");
				sendDataClear();

				ExpressDatabase database = ExpressDatabase.getInstance(getApplicationContext());
				for (int i = 0; i < database.size(); i++) {
					Log.i(TAG, "sending " + i + "....");
					sendDataAdd(database.getExpress(i).toJSONObject().toString());
				}
			}
		}.start();
	}

	private void sendDataClear() {
		PutDataMapRequest requestMap = PutDataMapRequest.create(Constants.PATH);
		requestMap.getDataMap().putString(Constants.EXTRA_EH_ACTION, Constants.EH_ACTION_CLEAR);
		requestMap.getDataMap().putLong("timestamp", System.currentTimeMillis());
		PutDataRequest request = requestMap.asPutDataRequest();
		if (!mGoogleApiClient.isConnected()) {
			return;
		}
		Wearable.DataApi.putDataItem(mGoogleApiClient, request).await();
	}

	private void sendDataAdd(String jsonStr) {
		PutDataMapRequest requestMap = PutDataMapRequest.create(Constants.PATH);
		requestMap.getDataMap().putString(Constants.EXTRA_EH_ACTION, Constants.EH_ACTION_ADD);
		requestMap.getDataMap().putString(Constants.EH_KEY_DATA, jsonStr);
		requestMap.getDataMap().putLong("timestamp", System.currentTimeMillis());
		PutDataRequest request = requestMap.asPutDataRequest();
		if (!mGoogleApiClient.isConnected()) {
			return;
		}
		Wearable.DataApi.putDataItem(mGoogleApiClient, request).await();
	}

}
