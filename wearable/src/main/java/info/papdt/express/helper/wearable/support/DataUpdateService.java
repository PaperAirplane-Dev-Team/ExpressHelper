package info.papdt.express.helper.wearable.support;

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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class DataUpdateService extends WearableListenerService
		implements GoogleApiClient.ConnectionCallbacks,
		GoogleApiClient.OnConnectionFailedListener,
		ResultCallback<DataApi.DeleteDataItemsResult> {

	private String TAG = "Wearable";
	private GoogleApiClient mGoogleApiClient;
	private ItemsKeeper mDatabase;

	@Override
	public void onCreate() {
		Log.i(TAG, "onStart");
		super.onCreate();
		mGoogleApiClient = new GoogleApiClient.Builder(this)
				.addApi(Wearable.API)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.build();
		mDatabase = ItemsKeeper.getInstance(getApplicationContext());
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null) {
			String action = intent.getAction();
			boolean refreshFromNetwork = intent.getBooleanExtra(Constants.EXTRA_REFRESH_FROM_NETWORK, false);
			if (Constants.ACTION_REFRESH.equals(action)) {
				if (mGoogleApiClient.isConnected()) {
					PutDataMapRequest requestMap = PutDataMapRequest.create(Constants.PATH);
					requestMap.getDataMap().putString(Constants.EXTRA_EH_ACTION, Constants.EH_ACTION_REFRESH);
					requestMap.getDataMap().putBoolean(Constants.EXTRA_REFRESH_FROM_NETWORK, refreshFromNetwork);
					PutDataRequest request = requestMap.asPutDataRequest();
					Wearable.DataApi.putDataItem(mGoogleApiClient, request)
							.setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
								@Override
								public void onResult(DataApi.DataItemResult dataItemResult) {
									if (!dataItemResult.getStatus().isSuccess()) {
										Log.e(TAG, "onResult: The status isn't success. Code:" + dataItemResult.getStatus());
									}
								}
							});
				}
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
				if (action.equals(Constants.EH_ACTION_CLEAR)) {
					Log.i(TAG, "Received ACTION_CLEAR");
					mDatabase.clear();
					try {
						mDatabase.save();
					} catch (IOException e) {
						e.printStackTrace();
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
				if (action.equals(Constants.EH_ACTION_ADD)) {
					Log.i(TAG, "Received ACTION_ADD");
					String data = dataMap.getString(Constants.EH_KEY_DATA);
					try {
						JSONObject jsonObject = new JSONObject(data);
						mDatabase.addExpress(Express.buildFromJSONObject(jsonObject));
						try {
							mDatabase.save();
						} catch (IOException e) {
							e.printStackTrace();
						} catch (JSONException e) {
							e.printStackTrace();
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	@Override
	public void onConnected(Bundle bundle) {
		Uri dataItemUri = new Uri.Builder()
				.scheme(PutDataRequest.WEAR_URI_SCHEME)
				.path(Constants.PATH)
				.build();
		Log.i(TAG, "onConnected");
		Wearable.DataApi.deleteDataItems(mGoogleApiClient, dataItemUri).setResultCallback(this);
	}

	@Override
	public void onConnectionSuspended(int i) {

	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {

	}

	@Override
	public void onResult(DataApi.DeleteDataItemsResult deleteDataItemsResult) {
		if (!deleteDataItemsResult.getStatus().isSuccess()) {
			Log.e(TAG, "Failed to delete");
		}
		mGoogleApiClient.disconnect();
	}

}
