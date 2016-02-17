package info.papdt.express.helper.ui.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import java.io.IOException;

import info.papdt.express.helper.R;
import info.papdt.express.helper.common.Settings;
import info.papdt.express.helper.ui.DetailsActivity;
import info.papdt.express.helper.ui.MainActivity;
import info.papdt.express.helper.ui.adapter.HomeCardRecyclerAdapter;
import info.papdt.express.helper.ui.common.MyRecyclerViewAdapter;
import info.papdt.express.helper.common.model.ItemsKeeper;

public abstract class BaseHomeFragment extends Fragment {

	public ItemsKeeper mDB;

	public Settings mSets;

	private SwipeRefreshLayout refreshLayout;
	private RecyclerView mRecyclerView;

	protected Context context;

	public static final int FLAG_REFRESH_LIST = 0, FLAG_REFRESH_ADAPTER_ONLY = 1;
	public static final String ARG_INITIAL_POSITION = "initial_position";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_home, container, false);

		if (mSets == null) {
			mSets = Settings.getInstance(getActivity().getApplicationContext());
		}

		refreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefreshLayout);

		mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
		mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
		mRecyclerView.setHasFixedSize(true);

		Activity parentActivity = getActivity();
		context = parentActivity.getApplicationContext();

		refreshLayout.setColorSchemeResources(R.color.blue_600);
		refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				mHandler.sendEmptyMessage(FLAG_REFRESH_LIST);
			}
		});

		mDB = ItemsKeeper.getInstance(getActivity().getApplicationContext());
		setUpAdapter();

		return rootView;
	}

	public abstract void setUpAdapter();

	public void scrollToTopItem() {
		mRecyclerView.smoothScrollToPosition(1);
	}

	private void showDeleteDialog(final int realPosition) {
		new MaterialDialog.Builder(getActivity())
				.title(R.string.dialog_delete_title)
				.content(R.string.dialog_delete_msg)
				.positiveText(android.R.string.ok)
				.negativeText(android.R.string.cancel)
				.callback(new MaterialDialog.ButtonCallback() {
					@Override
					public void onPositive(MaterialDialog dialog) {
						mDB.deleteItem(realPosition);
						try {
							mDB.save();
						} catch (IOException e) {
							e.printStackTrace();
						}
						mRecyclerView.getAdapter().notifyDataSetChanged();
						dialog.dismiss();
					}
				})
				.show();
	}

	protected void setUpAdapterListener(MyRecyclerViewAdapter adapter) {
		adapter.setOnItemClickListener(new MyRecyclerViewAdapter.OnItemClickListener() {
			@Override
			public void onItemClicked(int position) {
				HomeCardRecyclerAdapter adapter =
						(HomeCardRecyclerAdapter) mRecyclerView.getAdapter();
				int realPosition = mDB.findItem(
						adapter.getItem(position).getCompanyCode(),
						adapter.getItem(position).getMailNumber()
				);
				Intent intent = new Intent(getActivity(), DetailsActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
				intent.putExtra("id", realPosition);
				intent.putExtra("data", mDB.getItem(realPosition).toJsonStr());
				getActivity().startActivityForResult(intent, MainActivity.REQUEST_DETAILS);
			}
		});
	}

	public void setListAdapter(MyRecyclerViewAdapter adapter) {
		mRecyclerView.setAdapter(adapter);
		setUpAdapterListener(adapter);
	}

	@SuppressLint("HandlerLeak")
	public Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			try {
				switch (msg.what) {
					case FLAG_REFRESH_LIST:
						if (!refreshLayout.isRefreshing()) {
							refreshLayout.setRefreshing(true);
						}
						new RefreshTask().execute();
						break;
					case FLAG_REFRESH_ADAPTER_ONLY:
						// mDB.init();
						if (mRecyclerView != null) {
							mRecyclerView.getAdapter().notifyDataSetChanged();
						}
						break;
				}
			} catch (Exception e) {
				// ignore it
			}
		}

	};

	private class RefreshTask extends AsyncTask<Void, Void, ItemsKeeper> {

		@Override
		protected ItemsKeeper doInBackground(Void... params) {
			try {
				mDB.init();
				mDB.pullNewDataFromNetwork(false);
				try {
					mDB.save();
				} catch (IOException e) {
					e.printStackTrace();
				}
				MainActivity.UIHandler.sendEmptyMessage(MainActivity.FLAG_UPDATE_PAGES);
				return mDB;
			} catch (Exception e) {
				// failed
				return null;
			}
		}

		@Override
		protected void onPostExecute(ItemsKeeper db) {
			refreshLayout.setRefreshing(false);
			if (db != null) {
				mDB = db;
			} else {
				Toast.makeText(
						context,
						R.string.toast_network_error,
						Toast.LENGTH_SHORT
				).show();
			}
			if (mDB != null) {
				setUpAdapter();
			}
		}

	}

}
