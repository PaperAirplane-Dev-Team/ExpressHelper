package info.papdt.express.helper.ui.fragment;

import info.papdt.express.helper.ui.adapter.HomeCardRecyclerAdapter;

public class UnreceivedListFragment extends BaseHomeFragment {

	private HomeCardRecyclerAdapter mAdapter;

	public static UnreceivedListFragment newInstance() {
		UnreceivedListFragment fragment = new UnreceivedListFragment();
		return fragment;
	}

	public UnreceivedListFragment() {
	}

	@Override
	public void setUpAdapter() {
		setListAdapter(
				new HomeCardRecyclerAdapter(
						getActivity().getApplicationContext(),
						mDB,
						HomeCardRecyclerAdapter.TYPE_UNRECEIVED
				)
		);
	}

}