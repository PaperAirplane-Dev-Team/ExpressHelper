package info.papdt.express.helper.ui;

import android.app.Fragment;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.quinny898.library.persistentsearch.SearchBox;

import java.io.IOException;
import java.util.ArrayList;

import info.papdt.express.helper.R;
import info.papdt.express.helper.support.CrashHandler;
import info.papdt.express.helper.view.SlidingTabLayout;
import info.papdt.express.helper.common.Settings;
import info.papdt.express.helper.common.Utility;
import info.papdt.express.helper.support.wearable.Constants;
import info.papdt.express.helper.support.wearable.RefreshListener;
import info.papdt.express.helper.ui.adapter.CompanyListRecyclerAdapter;
import info.papdt.express.helper.ui.adapter.HomePagerAdapter;
import info.papdt.express.helper.ui.common.MyRecyclerViewAdapter;
import info.papdt.express.helper.ui.fragment.BaseHomeFragment;
import info.papdt.express.helper.common.api.KuaiDi100Helper;
import info.papdt.express.helper.common.model.ItemsKeeper;

public class MainActivity extends AbsActivity {

	private ItemsKeeper mExpressDB;

	private SlidingTabLayout mTabLayout;
	private ViewPager mPager;
	private static HomePagerAdapter mPagerAdapter;
	private FloatingActionsMenu mFAB;

	private CoordinatorLayout mMainLayout;
	private SearchBox mSearchBox;
	private View mCompanyListPage, mCompanyListPageBackground, mActionBackground;
	private RecyclerView mCompanyList;
	private CompanyListRecyclerAdapter mCompanyListAdapter;

	public static final int REQUEST_ADD = 100, RESULT_ADD_FINISH = 100,
			REQUEST_DETAILS = 101, RESULT_HAS_CHANGED = 101;
	public static final int FLAG_UPDATE_PAGES = 100;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, false);
		setContentView(R.layout.activity_main);

		int selectedTab = mSets.getInt(Settings.STATE_SELECTED_TAB, 0);

		/** Init crash handler */
		CrashHandler.init(getApplicationContext());
		CrashHandler.register();

		setSwipeBackEnable(false);

		/** Init Database */
		mExpressDB = ItemsKeeper.getInstance(getApplicationContext());
		refreshDatabase(false);

		/** Init ViewPager */
		mPagerAdapter = new HomePagerAdapter(getApplicationContext(), getFragmentManager());
		mPager.setAdapter(mPagerAdapter);
		mPager.setCurrentItem(selectedTab, false);
		mTabLayout.setViewPager(mPager);

		Intent intent = new Intent(this, RefreshListener.class);
		startService(intent);
	}

	@Override
	public void onStop() {
		mSets.putInt(Settings.STATE_SELECTED_TAB, mPager.getCurrentItem());
		super.onStop();
	}

	public void refreshDatabase(boolean pullNewData) {
		mExpressDB.init();
		Log.i("tag", "1");
		if (pullNewData) {
			mExpressDB.pullNewDataFromNetwork(false);
			try {
				mExpressDB.save();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		UIHandler.sendEmptyMessage(FLAG_UPDATE_PAGES);
	}

	@Override
	protected void setUpViews() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			View statusHeaderView = findViewById(R.id.statusHeaderView);
			statusHeaderView.setVisibility(View.GONE);
		} else {
			View statusHeaderView1 = findViewById(R.id.statusHeaderView1);
			statusHeaderView1.getLayoutParams().height = statusBarHeight;
		}

		mToolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(mToolbar);
		mActionBar = getSupportActionBar();
		mActionBar.setDisplayHomeAsUpEnabled(false);

		mMainLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);

		mSearchBox = (SearchBox) findViewById(R.id.searchBox);
		mCompanyListPage = findViewById(R.id.company_list_page);
		mCompanyListPageBackground = findViewById(R.id.company_list_page_background);
		mCompanyList = (RecyclerView) mCompanyListPage.findViewById(R.id.recycler_view);

		mSearchBox.setLogoText("");
		mSearchBox.setHintText(getString(R.string.search_hint_company));
		mCompanyList.setLayoutManager(new LinearLayoutManager(this));
		mCompanyList.setHasFixedSize(true);

		mPager = (ViewPager) findViewById(R.id.pager);
		mTabLayout = (SlidingTabLayout) findViewById(R.id.tabs);
		mTabLayout.setCustomTabView(R.layout.tab_indicator, android.R.id.text1);
		mTabLayout.setSelectedIndicatorColors(getResources().getColor(android.R.color.white));
		mTabLayout.setDistributeEvenly(true);
		mTabLayout.setOnTabItemClickListener(new SlidingTabLayout.OnTabItemClickListener() {
			@Override
			public void onTabItemClick(int pos) {
				BaseHomeFragment fragment = (BaseHomeFragment) mPagerAdapter.getItem(pos);
				try {
					fragment.scrollToTopItem();
				} catch (Exception e) {

				}
			}
		});

		/** Set up FloatingActionButton */
		mActionBackground = findViewById(R.id.white_background);
		mActionBackground.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mFAB.collapse();
			}
		});
		mActionBackground.setTranslationY(4096f);
		mActionBackground.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {
				Log.i("TAG", "onTouch");
				return mFAB.isExpanded() ? view.onTouchEvent(motionEvent) : false;
			}
		});
		mFAB = (FloatingActionsMenu) findViewById(R.id.fab);
		mFAB.setOnFloatingActionsMenuUpdateListener(new FloatingActionsMenu.OnFloatingActionsMenuUpdateListener() {
			@Override
			public void onMenuExpanded() {
				mActionBackground.setVisibility(View.VISIBLE);
				mActionBackground.setTranslationY(0f);
				AlphaAnimation anim = new AlphaAnimation(0f, 1f);
				anim.setFillEnabled(true);
				anim.setDuration(200);
				mActionBackground.startAnimation(anim);
			}

			@Override
			public void onMenuCollapsed() {
				AlphaAnimation anim = new AlphaAnimation(1f, 0f);
				anim.setFillAfter(true);
				anim.setDuration(200);
				mActionBackground.startAnimation(anim);
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								mActionBackground.setTranslationY(4096f);
								mActionBackground.setVisibility(View.GONE);
							}
						});
					}
				}, 200);
			}
		});
		findViewById(R.id.fab_add).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mFAB.collapse();
				AddActivity.launch(MainActivity.this);
			}
		});
		findViewById(R.id.fab_scan).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mFAB.collapse();
			}
		});
	}

	public void openCompanyList() {
		mCompanyListPage.setVisibility(View.VISIBLE);
		AlphaAnimation anim = new AlphaAnimation(0f, 1f);
		anim.setInterpolator(new DecelerateInterpolator());
		anim.setFillAfter(true);
		anim.setDuration(250);
		mCompanyListPageBackground.startAnimation(anim);
		mCompanyList.startAnimation(anim);

		mSearchBox.revealFromMenuItem(R.id.action_select_company, this);
		mSearchBox.setSearchListener(new SearchBox.SearchListener() {
			@Override
			public void onSearchOpened() {
				new SearchCompanyTask().execute();
			}

			@Override
			public void onSearchCleared() {
			}

			@Override
			public void onSearchClosed() {
				closeCompanyList();
			}

			@Override
			public void onSearchTermChanged() {
				new SearchCompanyTask().execute(mSearchBox.getSearchText());
			}

			@Override
			public void onSearch(String result) {
			}

		});
	}

	public void closeCompanyList() {
		mSearchBox.hideCircularly(this);

		AlphaAnimation anim = new AlphaAnimation(1f, 0f);
		anim.setInterpolator(new DecelerateInterpolator());
		anim.setFillAfter(true);
		anim.setDuration(250);
		mCompanyListPageBackground.startAnimation(anim);
		mCompanyList.startAnimation(anim);

		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						mCompanyListPage.setVisibility(View.GONE);
					}
				});
			}
		}, 250);
	}

	private void showDonateDialog() {
		View v = View.inflate(
				new ContextThemeWrapper(
						getApplicationContext(),
						R.style.Theme_AppCompat_Light_Dialog
				),
				R.layout.dialog_donate,
				null
		);
		new MaterialDialog.Builder(this)
				.title(R.string.item_donate)
				.customView(v, false)
				.negativeText(android.R.string.ok)
				.show();
	}

	@Override
	public void onBackPressed() {
		if (mFAB.isExpanded()) {
			mFAB.collapse();
			return;
		}
		if (mSearchBox.isSearchOpened()) {
			mSearchBox.toggleSearch();
		} else {
			super.onBackPressed();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		switch (requestCode) {
			case REQUEST_ADD:
				if (resultCode == RESULT_ADD_FINISH) {
					String jsonStr = intent.getStringExtra("result");
					String name = intent.getStringExtra("name");
					mExpressDB.addItem(jsonStr, name);
					try {
						mExpressDB.save();
					} catch (IOException e) {
						e.printStackTrace();
					}
					mPagerAdapter.notifyDataSetChanged();
				}
				break;
			case REQUEST_DETAILS:
				if (resultCode == RESULT_HAS_CHANGED) {
					mPagerAdapter.notifyDataSetChanged();
				}
				break;
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		try {
			mExpressDB.save();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		getMenuInflater().inflate(R.menu.main_menu, menu);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		if (id == R.id.action_settings) {
			SettingsActivity.launchActivity(this, SettingsActivity.FLAG_MAIN);
			return true;
		}
		if (id == R.id.action_select_company) {
			openCompanyList();
			return true;
		}
		if (id == R.id.action_donate) {
			showDonateDialog();
			return true;
		}
		if (id == R.id.action_add) {
			Intent intent = new Intent(this, RefreshListener.class);
			intent.setAction(Constants.ACTION_REFRESH);
			try {
				PendingIntent
						.getService(getApplicationContext(), 1000, intent, PendingIntent.FLAG_UPDATE_CURRENT)
						.send();
			} catch (PendingIntent.CanceledException e) {
				e.printStackTrace();
			}
			// AddActivity.launch(MainActivity.this, mFAB);
			return true;
		}
		if (id == R.id.action_manual_refresh) {
			Fragment fragment = mPagerAdapter.getItem(mPager.getCurrentItem());
			if (fragment instanceof BaseHomeFragment) {
				((BaseHomeFragment) fragment).mHandler.sendEmptyMessage(BaseHomeFragment.FLAG_REFRESH_LIST);
			}
			
			Toast.makeText(
					getApplicationContext(),
					R.string.toast_pull_to_refresh_tips,
					Toast.LENGTH_LONG
			).show();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	public void mic(View v) {
		if (Utility.isApplicationAvailable(getApplicationContext(), "com.mokee.assist")) {
			Intent LaunchIntent = getPackageManager().getLaunchIntentForPackage("com.mokee.assist");
			startActivity(LaunchIntent);
		} else {
			Toast.makeText(
					getApplicationContext(),
					R.string.toast_mic_unsupported,
					Toast.LENGTH_SHORT
			).show();
		}
	}

	public class SearchCompanyTask extends AsyncTask<String, Void, ArrayList<KuaiDi100Helper.CompanyInfo.Company>> {

		@Override
		protected ArrayList<KuaiDi100Helper.CompanyInfo.Company> doInBackground(String... params) {
			if (params.length > 0) {
				return KuaiDi100Helper.searchCompany(params [0]);
			} else {
				return KuaiDi100Helper.CompanyInfo.info;
			}
		}

		@Override
		protected void onPostExecute(ArrayList<KuaiDi100Helper.CompanyInfo.Company> result) {
			if (result != null) {
				mCompanyListAdapter = new CompanyListRecyclerAdapter(result);
				mCompanyList.setAdapter(mCompanyListAdapter);
				mCompanyListAdapter.setOnItemClickListener(new MyRecyclerViewAdapter.OnItemClickListener() {
					@Override
					public void onItemClicked(int position) {
						String phone = mCompanyListAdapter.getItem(position).phone;
						if (phone != null && phone != "null" && !TextUtils.isEmpty(phone)) {
							Intent intent = new Intent(Intent.ACTION_DIAL);
							intent.setData(Uri.parse("tel:" + phone));
							startActivity(intent);
						} else {
							Toast.makeText(
									getApplicationContext(),
									R.string.toast_phone_is_not_exist,
									Toast.LENGTH_SHORT
							).show();
						}
					}
				});
			}
		}

	}

	public static Handler UIHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case FLAG_UPDATE_PAGES:
					mPagerAdapter.notifyDataSetChanged();
					break;
			}
		}

	};

}
