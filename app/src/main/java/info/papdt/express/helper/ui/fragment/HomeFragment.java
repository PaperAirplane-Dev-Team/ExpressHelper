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
		setListAdapter(
				new HomeCardRecyclerAdapter(
						getActivity().getApplicationContext(),
						mDB,
						HomeCardRecyclerAdapter.TYPE_ALL
				)
		);
	}

}