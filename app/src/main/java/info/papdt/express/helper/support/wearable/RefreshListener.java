package info.papdt.express.helper.support.wearable;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

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
	}

	@Override
	public void onDataChanged(DataEventBuffer dataEvents) {
		for (DataEvent event : dataEvents) {
			if (event.getType() == DataEvent.TYPE_CHANGED) {
				if (Constants.EH_ACTION_REFRESH.equals(event.getDataItem().getUri().getPath())) {
					Log.i(TAG, "Received ACTION_REFRESH");
					sendDataClear();

					ExpressDatabase database = ExpressDatabase.getInstance(getApplicationContext());
					for (int i = 0; i < database.size(); i++) {
						Log.i(TAG, "sending " + i + "....");
						sendDataAdd(database.getExpress(i).getDataStr());
					}
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

	private void sendDataClear() {
		PutDataMapRequest requestMap = PutDataMapRequest.create(Constants.PATH);
		requestMap.getDataMap().putString(Constants.EXTRA_EH_ACTION, Constants.EH_ACTION_CLEAR);
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

	private void sendDataAdd(String jsonStr) {
		PutDataMapRequest requestMap = PutDataMapRequest.create(Constants.PATH);
		requestMap.getDataMap().putString(Constants.EXTRA_EH_ACTION, Constants.EH_ACTION_ADD);
		requestMap.getDataMap().putString(Constants.EH_KEY_DATA, jsonStr);
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
