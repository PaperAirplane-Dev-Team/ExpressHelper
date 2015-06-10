package info.papdt.express.helper.ui;

import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.DecelerateInterpolator;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.astuetz.PagerSlidingTabStrip;
import com.github.florent37.materialviewpager.MaterialViewPager;
import com.melnykov.fab.FloatingActionButton;
import com.quinny898.library.persistentsearch.SearchBox;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

import info.papdt.express.helper.R;
import info.papdt.express.helper.api.KuaiDi100Helper;
import info.papdt.express.helper.dao.ExpressDatabase;
import info.papdt.express.helper.support.CrashHandler;
import info.papdt.express.helper.support.Settings;
import info.papdt.express.helper.support.Utility;
import info.papdt.express.helper.support.wearable.Constants;
import info.papdt.express.helper.support.wearable.RefreshListener;
import info.papdt.express.helper.ui.adapter.CompanyListRecyclerAdapter;
import info.papdt.express.helper.ui.adapter.HomePagerAdapter;
import info.papdt.express.helper.ui.common.MyRecyclerViewAdapter;
import info.papdt.express.helper.ui.fragment.BaseHomeFragment;

public class MainActivity extends AbsActivity {

	public ExpressDatabase mExpressDB;

	private MaterialViewPager mMaterialPager;
	private ViewPager mPager;
	private HomePagerAdapter mPagerAdapter;
	private FloatingActionButton mFAB;

	private SearchBox mSearchBox;
	private View mCompanyListPage, mCompanyListPageBackground;
	private RecyclerView mCompanyList;
	private CompanyListRecyclerAdapter mCompanyListAdapter;

	public static final int REQUEST_ADD = 100, RESULT_ADD_FINISH = 100,
			REQUEST_DETAILS = 101, RESULT_HAS_CHANGED = 101;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		int selectedTab = mSets.getInt(Settings.STATE_SELECTED_TAB, 0);

		/** Init crash handler */
		CrashHandler.init(getApplicationContext());
		CrashHandler.register();

		setSwipeBackEnable(false);

		/** Init Database */
		mExpressDB = new ExpressDatabase(getApplicationContext());
		refreshDatabase(false);

		/** Init ViewPager */
		mPagerAdapter = new HomePagerAdapter(getApplicationContext(), getSupportFragmentManager());
		mPager.setAdapter(mPagerAdapter);
		mPager.setCurrentItem(selectedTab, false);
		PagerSlidingTabStrip tabView = mMaterialPager.getPagerTitleStrip();
		tabView.setViewPager(mPager);

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
		if (pullNewData) {
			mExpressDB.pullNewDataFromNetwork(false);
			try {
				mExpressDB.save();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				try {
					mPagerAdapter.notifyDataSetChanged();
				} catch (Exception e) {
					
				}
			}
		});
	}

	@Override
	protected void setUpViews() {
		View statusHeaderView1 = findViewById(R.id.statusHeaderView1);
		statusHeaderView1.getLayoutParams().height = statusBarHeight;

		mMaterialPager = (ViewPager) findViewById(R.id.materialViewPager);
		mToolbar = mMaterialPager.getToolbar();
		setSupportActionBar(mToolbar);
		mActionBar = getSupportActionBar();

		mSearchBox = (SearchBox) findViewById(R.id.searchBox);
		mCompanyListPage = findViewById(R.id.company_list_page);
		mCompanyListPageBackground = findViewById(R.id.company_list_page_background);
		mCompanyList = (RecyclerView) mCompanyListPage.findViewById(R.id.recycler_view);

		mSearchBox.setLogoText("");
		mSearchBox.setHintText(getString(R.string.search_hint_company));
		mCompanyList.setLayoutManager(new LinearLayoutManager(this));
		mCompanyList.setHasFixedSize(true);

		mPager = mMaterialPager.getViewPager();

		/** Set up FloatingActionButton */
		mFAB = (FloatingActionButton) findViewById(R.id.fab);
		mFAB.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				AddActivity.launch(MainActivity.this, mFAB);
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
					mExpressDB.addExpress(jsonStr, name);
					try {
						mExpressDB.save();
					} catch (IOException e) {
						e.printStackTrace();
					} catch (JSONException e) {
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
		} catch (JSONException e) {
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
			Fragment fragment = mPagerAdapter.getItemAt(mPager.getCurrentItem());
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

}
