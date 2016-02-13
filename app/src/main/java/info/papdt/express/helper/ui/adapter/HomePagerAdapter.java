package info.papdt.express.helper.ui.adapter;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;

import info.papdt.express.helper.R;
import info.papdt.express.helper.ui.fragment.BaseHomeFragment;
import info.papdt.express.helper.ui.fragment.HomeFragment;
import info.papdt.express.helper.ui.fragment.ReceivedListFragment;
import info.papdt.express.helper.ui.fragment.UnreceivedListFragment;

public class HomePagerAdapter extends FragmentPagerAdapter {

	private static String[] TITLES;
	private Fragment[] f = new Fragment[3];

	private int mScrollY;

	public HomePagerAdapter(Context context, FragmentManager fm) {
		super(fm);
		TITLES = context.getResources().getStringArray(R.array.title_sections);
	}

	public void setScrollY(int scrollY) {
		mScrollY = scrollY;
	}

	@Override
	public Fragment getItem(int position) {
		switch (position) {
			case 0:
				f[0] = HomeFragment.newInstance();
				if (0 < mScrollY) {
					Bundle args = new Bundle();
					args.putInt(BaseHomeFragment.ARG_INITIAL_POSITION, 1);
					f[0].setArguments(args);
				}
				break;
			case 1:
				f[1] = UnreceivedListFragment.newInstance();
				if (0 < mScrollY) {
					Bundle args = new Bundle();
					args.putInt(BaseHomeFragment.ARG_INITIAL_POSITION, 1);
					f[1].setArguments(args);
				}
				break;
			case 2:
				f[2] = ReceivedListFragment.newInstance();
				if (0 < mScrollY) {
					Bundle args = new Bundle();
					args.putInt(BaseHomeFragment.ARG_INITIAL_POSITION, 1);
					f[2].setArguments(args);
				}
				break;
		}
		return f[position];
	}

	public void notifyDataSetChanged() {
		for (int i = 0; i < 3; i++) {
			try {
				BaseHomeFragment fragment = (BaseHomeFragment) this.getItem(i);
				fragment.mHandler.sendEmptyMessage(BaseHomeFragment.FLAG_REFRESH_ADAPTER_ONLY);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public int getCount() {
		return 3;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		return TITLES[position];
	}

}
