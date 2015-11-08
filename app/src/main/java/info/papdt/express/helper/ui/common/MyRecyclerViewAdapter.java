package info.papdt.express.helper.ui.common;

import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import info.papdt.express.helper.R;

public abstract class MyRecyclerViewAdapter<VH extends MyRecyclerViewAdapter.ClickableViewHolder> extends RecyclerView.Adapter<VH> {

	private int lastPosition = -1;
	private boolean firstLoad = true;
	private Context context;

	private boolean useAnimation;

	public MyRecyclerViewAdapter(boolean useAnimation) {
		this.useAnimation = useAnimation;
	}

	public interface OnItemClickListener {
		public void onItemClicked(int position);
	}

	public interface OnItemLongClickListener {
		public boolean onItemLongClicked(int position);
	}

	private OnItemClickListener itemClickListener;
	private OnItemLongClickListener itemLongClickListener;

	public void setOnItemClickListener(OnItemClickListener listener) {
		this.itemClickListener = listener;
	}

	public void setOnItemLongClickListener(OnItemLongClickListener listener) {
		this.itemLongClickListener = listener;
	}

	public OnItemClickListener getOnItemClickListener() {
		return this.itemClickListener;
	}

	public OnItemLongClickListener getOnItemLongClickListener() {
		return this.itemLongClickListener;
	}

	public void bindContext(Context context) {
		this.context = context;
	}

	public Context getContext() {
		return this.context;
	}

	@Override
	public void onBindViewHolder(VH holder, final int position) {
		holder.getParentView().setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (itemClickListener != null) {
					itemClickListener.onItemClicked(position);
				}
			}
		});
		holder.getParentView().setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				if (itemLongClickListener != null) {
					return itemLongClickListener.onItemLongClicked(position);
				} else {
					return false;
				}
			}
		});
		if (useAnimation) setAnimation(holder.getParentView(), position);
	}

	private void setAnimation(final View viewToAnimate, final int position) {
		// If the bound view wasn't previously displayed on screen, it's animated
		if (position > lastPosition) {
			if (firstLoad) {
				viewToAnimate.setAlpha(0f);
				new Handler().postDelayed(
						new Runnable() {
							@Override
							public void run() {
								Animation animation = AnimationUtils.loadAnimation(
										context,
										R.anim.card_slide_in
								);
								animation.setAnimationListener(new Animation.AnimationListener() {
									@Override
									public void onAnimationStart(Animation animation) {
										viewToAnimate.setAlpha(1f);
										firstLoad = true;
									}

									@Override
									public void onAnimationEnd(Animation animation) {
										firstLoad = false;
									}

									@Override
									public void onAnimationRepeat(Animation animation) {

									}
								});
								animation.setFillAfter(true);
								viewToAnimate.startAnimation(animation);
							}
						}
				, position * 180);
			} else {
				Animation animation = AnimationUtils.loadAnimation(context, R.anim.card_slide_in);
				viewToAnimate.startAnimation(animation);
			}
			lastPosition = position;
		}
	}

	public class ClickableViewHolder extends RecyclerView.ViewHolder {

		private View parentView;

		public ClickableViewHolder(View itemView) {
			super(itemView);
			this.parentView = itemView;
		}

		public View getParentView() {
			return parentView;
		}

	}

}
