package info.papdt.express.helper.ui.fragment;

import android.util.Log;

import info.papdt.express.helper.ui.adapter.HomeCardRecyclerAdapter;

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
		setListAdapter(
				new HomeCardRecyclerAdapter(
						getActivity(),
						mDB,
						HomeCardRecyclerAdapter.TYPE_ALL
				)
		);
	}

}