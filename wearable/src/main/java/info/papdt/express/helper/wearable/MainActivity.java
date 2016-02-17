package info.papdt.express.helper.wearable;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.wearable.view.WatchViewStub;
import android.support.wearable.view.WearableListView;
import android.util.Log;
import android.widget.TextView;

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
import com.google.gson.Gson;

import java.io.IOException;

import info.papdt.express.helper.R;
import info.papdt.express.helper.common.model.Item;
import info.papdt.express.helper.common.model.ItemsKeeper;
import info.papdt.express.helper.wearable.adapter.HomeWearableListAdapter;
import info.papdt.express.helper.wearable.support.Constants;

public class MainActivity extends Activity
		implements WatchViewStub.OnLayoutInflatedListener,
		WearableListView.ClickListener,
		GoogleApiClient.ConnectionCallbacks,
		GoogleApiClient.OnConnectionFailedListener,
		DataApi.DataListener, ResultCallback<DataApi.DeleteDataItemsResult> {

	private WatchViewStub mViewStub;
	private WearableListView mListView;
	private SwipeRefreshLayout mSwipeRefreshLayout;
	private TextView mDebugText;

	private ItemsKeeper mDatabase;
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

		mDatabase = ItemsKeeper.getInstance(getApplicationContext());

		mViewStub = (WatchViewStub) findViewById(R.id.watch_view_stub);
		mViewStub.setOnLayoutInflatedListener(this);
	}

	@Override
	public void onLayoutInflated(WatchViewStub watchViewStub) {
		mAdapter = new HomeWearableListAdapter(mDatabase);
		mDebugText = (TextView) findViewById(R.id.debug_text);
		mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
		mListView = (WearableListView) findViewById(R.id.list_view);
		mListView.setAdapter(mAdapter);
		mListView.setClickListener(this);
		mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				if (!isRefreshing) {
					Log.i(TAG, "onRefresh!");
					addDebugText("onRefresh!");
					isRefreshing = true;
					mSwipeRefreshLayout.setRefreshing(true);
					if (mGoogleApiClient.isConnected()) {
						Log.i(TAG, "send");
						addDebugText("send");
						PutDataMapRequest requestMap = PutDataMapRequest.create(Constants.PATH);
						requestMap.getDataMap().putString(Constants.EXTRA_EH_ACTION, Constants.EH_ACTION_REFRESH);
						requestMap.getDataMap().putBoolean(Constants.EXTRA_REFRESH_FROM_NETWORK, false);
						requestMap.getDataMap().putLong("timestamp", System.currentTimeMillis());
						PutDataRequest request = requestMap.asPutDataRequest();
						if (!mGoogleApiClient.isConnected()) {
							return;
						}
						Wearable.DataApi.putDataItem(mGoogleApiClient, request)
								.setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
									@Override
									public void onResult(DataApi.DataItemResult dataItemResult) {
										if (!dataItemResult.getStatus().isSuccess()) {
											Log.e(TAG, "onResult: The status isn't success. Code:" + dataItemResult.getStatus());
											addDebugText("onResult: The status isn't success. Code:" + dataItemResult.getStatus());
										} else {
											Log.e(TAG, "onResult: Succeed");
											addDebugText("onResult: Succeed");
										}
									}
								});
					}
				}
			}
		});
	}

	private void addDebugText(final String line) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mDebugText.setText(line + "\n" + mDebugText.getText());
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
		Log.i(TAG, "onDataChanged");
		addDebugText("onDataChanged");
		for (DataEvent event : dataEvents) {
			if (event.getType() == DataEvent.TYPE_CHANGED) {
				DataMap dataMap = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
				String action = dataMap.getString(Constants.EXTRA_EH_ACTION);
				if (action.equals(Constants.EH_ACTION_CLEAR)) {
					Log.i(TAG, "Received ACTION_CLEAR");
					addDebugText("Received ACTION_CLEAR");
					mDatabase.clear();
					try {
						mDatabase.save();
					} catch (IOException e) {
						e.printStackTrace();
					}
					mAdapter = new HomeWearableListAdapter(mDatabase);
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							mListView.setAdapter(mAdapter);
						}
					});
				}
				if (action.equals(Constants.EH_ACTION_ADD)) {
					Log.i(TAG, "Received ACTION_ADD");
					addDebugText("Received ACTION_ADD");
					String data = dataMap.getString(Constants.EH_KEY_DATA);
					mDatabase.addItem(new Gson().fromJson(data, Item.class));
					try {
						mDatabase.save();
					} catch (IOException e) {
						e.printStackTrace();
					}
					mAdapter.notifyDataSetChanged();
				}
			}
		}
	}

	@Override
	public void onConnected(Bundle bundle) {
		addDebugText("onConnected!");
		Wearable.DataApi.addListener(mGoogleApiClient, this);
	}

	@Override
	protected void onStart() {
		super.onStart();
		mGoogleApiClient.connect();
	}

	@Override
	protected void onPause() {
		super.onPause();
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
