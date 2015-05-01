package info.papdt.express.helper.wearable;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.wearable.view.WatchViewStub;
import android.support.wearable.view.WearableListView;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import info.papdt.express.helper.wearable.adapter.HomeWearableListAdapter;
import info.papdt.express.helper.wearable.support.Constants;
import info.papdt.express.helper.wearable.support.Express;
import info.papdt.express.helper.wearable.support.ExpressDatabase;

public class MainActivity extends Activity
		implements WatchViewStub.OnLayoutInflatedListener,
		WearableListView.ClickListener,
		GoogleApiClient.ConnectionCallbacks,
		GoogleApiClient.OnConnectionFailedListener,
		DataApi.DataListener, ResultCallback<DataApi.DeleteDataItemsResult> {

	private WatchViewStub mViewStub;
	private WearableListView mListView;
	private SwipeRefreshLayout mSwipeRefreshLayout;

	private ExpressDatabase mDatabase;
	private HomeWearableListAdapter mAdapter;

	private GoogleApiClient mGoogleApiClient;

	private boolean isRefreshing = false;

	private static final String TAG = "Wearable";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mGoogleApiClient = new GoogleApiClient.Builder(this)
				.addApi(Wearable.API)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.build();

		mDatabase = ExpressDatabase.getInstance(getApplicationContext());

		mViewStub = (WatchViewStub) findViewById(R.id.watch_view_stub);
		mViewStub.setOnLayoutInflatedListener(this);
	}

	@Override
	public void onLayoutInflated(WatchViewStub watchViewStub) {
		mAdapter = new HomeWearableListAdapter(mDatabase);
		mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
		mListView = (WearableListView) findViewById(R.id.list_view);
		mListView.setAdapter(mAdapter);
		mListView.setClickListener(this);
		mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				if (!isRefreshing) {
					Log.i(TAG, "onRefresh!");
					isRefreshing = true;
					mSwipeRefreshLayout.setRefreshing(true);
					if (mGoogleApiClient.isConnected()) {
						PutDataMapRequest requestMap = PutDataMapRequest.create(Constants.PATH);
						requestMap.getDataMap().putString(Constants.EXTRA_EH_ACTION, Constants.EH_ACTION_REFRESH);
						requestMap.getDataMap().putBoolean(Constants.EXTRA_REFRESH_FROM_NETWORK, false);
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
		});
	}

	@Override
	public void onClick(WearableListView.ViewHolder viewHolder) {
		if (viewHolder instanceof HomeWearableListAdapter.ViewHolder) {
			HomeWearableListAdapter.ViewHolder holder = (HomeWearableListAdapter.ViewHolder) viewHolder;
			Log.i(TAG, "WearableListView: onClick position=" + holder.position);
		}
	}

	@Override
	public void onTopEmptyRegionClick() {

	}

	@Override
	public void onDataChanged(DataEventBuffer dataEvents) {
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
					mAdapter = new HomeWearableListAdapter(mDatabase);
					mListView.setAdapter(mAdapter);
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
						mAdapter.notifyDataSetChanged();
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	@Override
	public void onConnected(Bundle bundle) {
		Wearable.DataApi.addListener(mGoogleApiClient, this);
	}

	@Override
	protected void onResume() {
		super.onStart();
		mGoogleApiClient.connect();
	}

	@Override
	protected void onPause() {
		super.onPause();
		Wearable.DataApi.removeListener(mGoogleApiClient, this);
		mGoogleApiClient.disconnect();
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
