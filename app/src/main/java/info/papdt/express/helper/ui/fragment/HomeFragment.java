package info.papdt.express.helper.ui.fragment;

import info.papdt.express.helper.ui.adapter.HomeCardRecyclerAdapter;
import info.papdt.express.helper.ui.common.MultiSelectableRecyclerAdapter;

public class HomeFragment extends BaseHomeFragment {

	private HomeCardRecyclerAdapter mAdapter;

	public static HomeFragment newInstance() {
		HomeFragment fragment = new HomeFragment();
		return fragment;
	}

	public HomeFragment() {
	}

	@Override
	public void setUpAdapter() {
		mAdapter = new HomeCardRecyclerAdapter(getActivity().getApplicationContext(), mDB);
		mRecyclerView.setAdapter(mAdapter);
		mAdapter.setOnSelectingStateCallback(new MultiSelectableRecyclerAdapter.OnSelectingStateCallback() {
			@Override
			public void onStart() {
				// TODO
			}

			@Override
			public void onEnd() {
				// TODO
			}
		});
		setUpAdapterListener();
	}

}