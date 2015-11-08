package info.papdt.express.helper.ui.adapter;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;

import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import info.papdt.express.helper.R;
import info.papdt.express.helper.dao.ExpressDatabase;
import info.papdt.express.helper.support.Express;
import info.papdt.express.helper.support.ExpressResult;
import info.papdt.express.helper.support.Settings;
import info.papdt.express.helper.ui.common.MultiSelectableRecyclerAdapter;

public class HomeCardRecyclerAdapter extends MultiSelectableRecyclerAdapter<HomeCardRecyclerAdapter.ViewHolder> {

	private ExpressDatabase db;
	private int type;

	private int[] defaultColors;

	public static final int TYPE_ALL = 0, TYPE_UNRECEIVED = 1, TYPE_RECEIVED = 2;

	public HomeCardRecyclerAdapter(Context context, ExpressDatabase db, int type) {
		super(!Settings.getInstance(context).getBoolean(Settings.KEY_DISABLE_ANIMATION, false));
		this.db = db;
		this.defaultColors = context.getResources().getIntArray(R.array.statusColor);
		this.type = type;
	}

	@Override
	public boolean onItemSelect(int position) {
		return position < getExpressCount();
	}

	@Override
	public boolean onItemUnselect(int position) {
		return position < getExpressCount();
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		bindContext(parent.getContext());
		View v = LayoutInflater.from(getContext()).inflate(R.layout.card_express_item, parent, false);
		return new ViewHolder(v);
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, final int position) {
		super.onBindViewHolder(holder, position);

		Express item = getItem(position);
		ExpressResult cache = item.getData();

		ColorDrawable drawable = new ColorDrawable(defaultColors[cache.getTrueStatus()]);
		holder.iv_round.setImageDrawable(drawable);

		holder.tv_title.setText(item.getName());

		try {
			Map<String, String> lastData = cache.data.get(cache.data.size() - 1);
			holder.tv_center_round.setText(cache.expTextName.substring(0, 1));
			holder.tv_desp.setText(lastData.get("context"));
			holder.tv_time.setText(lastData.get("time"));
			holder.tv_time.setVisibility(View.VISIBLE);
		} catch (Exception e) {
			holder.tv_desp.setText(R.string.list_error_cannot_get_latest_status);
			holder.tv_time.setVisibility(View.GONE);
		}

		holder.changeViewState(getSelectStates()[position], false);
	}

	public int getExpressCount() {
		if (type == TYPE_ALL) {
			return db.size();
		} else if (type == TYPE_UNRECEIVED) {
			return db.urSize();
		} else if (type == TYPE_RECEIVED) {
			return db.okSize();
		}
		return -1;
	}

	@Override
	public int getItemCount() {
		return getExpressCount();
	}

	public Express getItem(int i) {
		if (type == TYPE_ALL) {
			return db.getExpress(getExpressCount() - i - 1);
		} else if (type == TYPE_UNRECEIVED) {
			return db.getUnreceivedArray().get(getExpressCount() - i - 1);
		} else if (type == TYPE_RECEIVED) {
			return db.getReceivedArray().get(getExpressCount() - i - 1);
		}
		return null;
	}

	public class ViewHolder extends MultiSelectableRecyclerAdapter.SelectableViewHolder {

		public CircleImageView iv_round;
		public TextView tv_title, tv_desp, tv_time, tv_center_round;
		public View mSelectStateView;

		public ViewHolder(View itemView) {
			super(itemView);
			this.iv_round = (CircleImageView) itemView.findViewById(R.id.iv_round);
			this.tv_title = (TextView) itemView.findViewById(R.id.tv_title);
			this.tv_desp = (TextView) itemView.findViewById(R.id.tv_desp);
			this.tv_time = (TextView) itemView.findViewById(R.id.tv_time);
			this.tv_center_round = (TextView) itemView.findViewById(R.id.center_text);
			this.mSelectStateView = itemView.findViewById(R.id.selected_state);
		}

		@Override
		public void changeViewState(boolean isSelected, boolean animate) {
			if (animate) {
				this.mSelectStateView.animate()
						.alpha(isSelected ? 1f : 0f)
						.scaleX(isSelected ? 1f : 0f)
						.scaleY(isSelected ? 1f : 0f)
						.setInterpolator(new OvershootInterpolator())
						.start();
			} else {
				this.mSelectStateView.setAlpha(isSelected ? 1f : 0f);
			}
		}

	}

}