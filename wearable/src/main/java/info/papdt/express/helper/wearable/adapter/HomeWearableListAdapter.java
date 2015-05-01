package info.papdt.express.helper.wearable.adapter;

import android.support.wearable.view.CircledImageView;
import android.support.wearable.view.WearableListView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import info.papdt.express.helper.wearable.R;
import info.papdt.express.helper.wearable.support.Express;
import info.papdt.express.helper.wearable.support.ExpressDatabase;
import info.papdt.express.helper.wearable.support.ExpressResult;

public class HomeWearableListAdapter extends WearableListView.Adapter {

	private ExpressDatabase mDatabase;

	public HomeWearableListAdapter(ExpressDatabase database) {
		this.mDatabase = database;
	}

	@Override
	public WearableListView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new HomeWearableListAdapter.ViewHolder(View.inflate(
				parent.getContext(),
				R.layout.home_list_item,
				null
		));
	}

	@Override
	public void onBindViewHolder(WearableListView.ViewHolder holder, int position) {
		if (holder instanceof HomeWearableListAdapter.ViewHolder) {
			HomeWearableListAdapter.ViewHolder mHolder = (HomeWearableListAdapter.ViewHolder) holder;
			mHolder.express = mDatabase.getExpress(position);
			mHolder.position = position;
			mHolder.setUpViews();
		}
	}

	@Override
	public int getItemCount() {
		return mDatabase.size();
	}

	public class ViewHolder extends WearableListView.ViewHolder {

		public CircledImageView mImageView;
		public TextView mTitleView, mContentView;
		public int position;
		public Express express;

		public ViewHolder(View itemView) {
			super(itemView);
			mImageView = (CircledImageView) itemView.findViewById(R.id.image_view);
			mTitleView = (TextView) itemView.findViewById(R.id.title);
			mContentView = (TextView) itemView.findViewById(R.id.content);
		}

		public void setUpViews() {
			if (express != null) {
				mTitleView.setText(express.getName());
				try {
					ExpressResult result = express.getData();
					mContentView.setText(result.data.get(result.data.size() - 1).get("context"));
				} catch (Exception e) {
					mContentView.setText(R.string.content_null);
				}
			}
		}

	}

}
