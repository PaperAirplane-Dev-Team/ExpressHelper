package info.papdt.express.helper.ui.fragment;

import info.papdt.express.helper.ui.adapter.HomeCardRecyclerAdapter;

public class ReceivedListFragment extends BaseHomeFragment {

	private HomeCardRecyclerAdapter mAdapter;
	public static ReceivedListFragment newInstance() {
		ReceivedListFragment fragment = new ReceivedListFragment();
		return fragment;
	}

	public ReceivedListFragment() {
	}

	@Override
	public void setUpAdapter() {
		setListAdapter(
				new HomeCardRecyclerAdapter(
						getActivity().getApplicationContext(),
						mDB,
						HomeCardRecyclerAdapter.TYPE_RECEIVED
				)
		);
	}

}