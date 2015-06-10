package info.papdt.express.helper.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import org.json.JSONException;

import java.io.IOException;

import info.papdt.express.helper.R;
import info.papdt.express.helper.dao.ExpressDatabase;
import info.papdt.express.helper.support.Settings;
import info.papdt.express.helper.ui.DetailsActivity;
import info.papdt.express.helper.ui.MainActivity;
import info.papdt.express.helper.ui.adapter.HomeCardRecyclerAdapter;
import info.papdt.express.helper.ui.common.MyRecyclerViewAdapter;

public abstract class BaseHomeFragment extends Fragment {

	public ExpressDatabase mDB;

	protected Settings mSets;

	protected SwipeRefreshLayout refreshLayout;
	protected RecyclerView mRecyclerView;
	protected View headerView;

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
		headerView = inflater.inflate(R.layout.padding, null);

		Activity parentActivity = getActivity();
		context = parentActivity.getApplicationContext();

		refreshLayout.setProgressViewEndTarget(
				true,
				getResources().getDimensionPixelOffset(R.dimen.abc_action_bar_default_height_material) +
						getResources().getDimensionPixelOffset(R.dimen.tab_height)
		);
		refreshLayout.setColorSchemeResources(R.color.blue_500);
		refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				mHandler.sendEmptyMessage(FLAG_REFRESH_LIST);
			}
		});

		mDB = ((MainActivity) getActivity()).mExpressDB;
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
						super.onPositive(dialog);
						mDB.deleteExpress(realPosition);
						try {
							mDB.save();
						} catch (JSONException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
						mRecyclerView.getAdapter().notifyDataSetChanged();
					}
				})
				.show();
	}

	protected void setUpAdapterListener() {
		MyRecyclerViewAdapter adapter = (MyRecyclerViewAdapter) mRecyclerView.getAdapter();
		adapter.setOnItemClickListener(new MyRecyclerViewAdapter.OnItemClickListener() {
			@Override
			public void onItemClicked(int position) {
				if (position == 0) return;

				HomeCardRecyclerAdapter adapter =
						(HomeCardRecyclerAdapter) mRecyclerView.getAdapter();
				int realPosition = mDB.findExpress(
						adapter.getItem(position - 1).getCompanyCode(),
						adapter.getItem(position - 1).getMailNumber()
				);
				Intent intent = new Intent(getActivity(), DetailsActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
				intent.putExtra("id", realPosition);
				intent.putExtra("data", mDB.getExpress(realPosition).toJSONObject().toString());
				getActivity().startActivityForResult(intent, MainActivity.REQUEST_DETAILS);
			}
		});
		adapter.setOnItemLongClickListener(new MyRecyclerViewAdapter.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClicked(int position) {
				if (position == 0) return false;

				HomeCardRecyclerAdapter adapter =
						(HomeCardRecyclerAdapter) mRecyclerView.getAdapter();
				int realPosition = mDB.findExpress(
						adapter.getItem(position - 1).getCompanyCode(),
						adapter.getItem(position - 1).getMailNumber()
				);

				showDeleteDialog(realPosition);

				return true;
			}
		});
	}

	public Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case FLAG_REFRESH_LIST:
					if (!refreshLayout.isRefreshing()) {
						refreshLayout.setRefreshing(true);
					}
					new RefreshTask().execute();
					break;
				case FLAG_REFRESH_ADAPTER_ONLY:
					mDB.init();
					setUpAdapter();
					break;
			}
		}

	};

	private class RefreshTask extends AsyncTask<Void, Void, ExpressDatabase> {

		@Override
		protected ExpressDatabase doInBackground(Void... params) {
			try {
				((MainActivity) getActivity()).refreshDatabase(true);
				return ((MainActivity) getActivity()).mExpressDB;
			} catch (Exception e) {
				// failed
				return null;
			}
		}

		@Override
		protected void onPostExecute(ExpressDatabase db) {
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
