package info.papdt.express.helper.ui.common;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

public abstract class MultiSelectableRecyclerAdapter<VH extends MultiSelectableRecyclerAdapter.SelectableViewHolder> extends MyRecyclerViewAdapter<VH> {

	private boolean isSelecting = false;
	private boolean[] selectStates;

	private OnSelectingStateCallback mCallback;

	public MultiSelectableRecyclerAdapter(boolean useAnimation) {
		super(useAnimation);
	}

	public void startSelect() {
		Log.i("TAG", "startSelect");
		isSelecting = true;
		selectStates = new boolean[getItemCount()];
		this.notifyDataSetChanged();
		if (mCallback != null) {
			mCallback.onStart();
		}
	}

	public void endSelect() {
		isSelecting = false;
		selectStates = new boolean[getItemCount()];
		this.notifyDataSetChanged();
		if (mCallback != null) {
			mCallback.onEnd();
		}
	}

	public abstract boolean onItemSelect(int position);
	public abstract boolean onItemUnselect(int position);

	public boolean isSelecting() {
		return isSelecting;
	}

	public boolean[] getSelectStates() {
		if (selectStates == null) {
			selectStates = new boolean[getItemCount()];
		}
		return selectStates;
	}

	public void setOnSelectingStateCallback(OnSelectingStateCallback callback) {
		this.mCallback = callback;
	}

	@Override
	public abstract VH onCreateViewHolder(ViewGroup parent, int viewType);

	@Override
	public void onBindViewHolder(final VH holder, final int position) {
		super.onBindViewHolder(holder, position);
		if (isSelecting) {
			holder.getParentView().setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					if (isSelecting && selectStates [position] ? onItemUnselect(position) : onItemSelect(position)) {
						Log.i("TAG", "no." + position + " is selected.");
						holder.changeViewState(selectStates[position] = !selectStates[position],
								holder.nowState != selectStates[position]);
					}
				}
			});
			holder.getParentView().setOnLongClickListener(null);
		} else {
			holder.getParentView().setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (isSelecting && selectStates[position] ? onItemUnselect(position) : onItemSelect(position)) {
						Log.i("TAG", "no." + position + " is selected.");
						holder.changeViewState(selectStates[position] = !selectStates[position],
								holder.nowState != selectStates[position]);
					} else {
						getOnItemClickListener().onItemClicked(position);
					}
				}
			});
			holder.getParentView().setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View view) {
					if (!isSelecting && onItemSelect(position)) {
						startSelect();
						holder.changeViewState(true, true);
						selectStates[position] = true;
						return true;
					}
					return false;
				}
			});
		}
	}

	@Override
	public abstract int getItemCount();

	public abstract class SelectableViewHolder extends MyRecyclerViewAdapter.ClickableViewHolder {

		protected boolean nowState = false;

		public SelectableViewHolder(View itemView) {
			super(itemView);
		}

		public abstract void changeViewState(boolean isSelected, boolean animate);

	}

	public interface OnSelectingStateCallback {

		public void onStart();
		public void onEnd();

	}

}
