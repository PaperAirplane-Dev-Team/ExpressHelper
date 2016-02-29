package info.papdt.express.helper.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.github.ksoichiro.android.observablescrollview.ObservableRecyclerView;
import com.quinny898.library.persistentsearch.SearchBox;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;

import info.papdt.express.helper.R;
import info.papdt.express.helper.api.KuaiDi100Helper;
import info.papdt.express.helper.support.HttpUtils;
import info.papdt.express.helper.support.Utility;
import info.papdt.express.helper.ui.adapter.CompanyListRecyclerAdapter;
import info.papdt.express.helper.ui.common.MyRecyclerViewAdapter;

public class CompanySelectActivity extends AbsActivity {

	private SearchBox mSearchBox;
	private ObservableRecyclerView mRecyclerView;
	private CompanyListRecyclerAdapter mCompanyListAdapter;
	private AddActivity _mActivity;

	public static final int REQUEST_CODE_SELECT = 0x100, RESULT_SELECTED = 0x100;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_select_company);

		setSwipeBackEnable(false);

		mToolbar.setTitle("");

		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						openSearchBox();
					}
				});
			}
		}, 100);
	}

	public void openSearchBox() {
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
				close();
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

	public void close() {
		mSearchBox.hideCircularly(this);
		hideSoftKeyboard();
		finish();
	}

	private void hideSoftKeyboard() {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(mSearchBox.getEditTextWindowToken(), 0);
	}

	@Override
	protected void setUpViews() {
		mRecyclerView = (ObservableRecyclerView) findViewById(R.id.company_list);
		mSearchBox = (SearchBox) findViewById(R.id.searchBox);

		mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
		mRecyclerView.setHasFixedSize(true);
		mSearchBox.setHintText(getString(R.string.search_hint_company));
		mSearchBox.setLogoText("");
		mSearchBox.setShouldOpenKeyboard(false);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.select_company_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	public static void launchActivity(AddActivity mActivity) {
		Intent intent = new Intent(mActivity, CompanySelectActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
		intent.putExtra("express_number",mActivity.getExpressNumber());
		mActivity.startActivityForResult(intent, REQUEST_CODE_SELECT);
	}

	public class SearchCompanyTask extends AsyncTask<String, Void, ArrayList<KuaiDi100Helper.CompanyInfo.Company>> {

		@Override
		protected ArrayList<KuaiDi100Helper.CompanyInfo.Company> doInBackground(String... params) {
			if (params.length > 0) {
				return KuaiDi100Helper.searchCompany(params [0]);
			} else {
				try {
					ArrayList<KuaiDi100Helper.CompanyInfo.Company> companies = new ArrayList<>();
					Bundle extras = getIntent().getExtras();
					if (extras != null) {
						String number = extras.getString("express_number");
						String[] result = new String[1];
						int resultCode = HttpUtils.post(KuaiDi100Helper.getDetectUrl(number), result);
						Log.d("debug:coderfox", Integer.toString(resultCode));
						switch (resultCode) {
							case HttpUtils.CODE_OKAY: {
								JSONObject jsonObj = new JSONObject(result[0]);
								JSONArray auto = jsonObj.getJSONArray("auto");
								for(int i=0;i<auto.length();i++) {
									String json2 = auto.getString(i);
									JSONTokener jsonParser2 = new JSONTokener(json2);
									JSONObject auto2 = (JSONObject) jsonParser2.nextValue();
									try {
										companies.add(KuaiDi100Helper.CompanyInfo.info.get(
												KuaiDi100Helper.CompanyInfo.findCompanyByCode(
														auto2.getString("comCode"))));
									} catch (Exception e) {
										e.printStackTrace();
									}
								}
								return companies;
							}
							default:
								return KuaiDi100Helper.CompanyInfo.info;
						}
					} else {
						return KuaiDi100Helper.CompanyInfo.info;
					}
				} catch (JSONException e) {
					e.printStackTrace();
					return KuaiDi100Helper.CompanyInfo.info;
				} catch (Exception e) {
					e.printStackTrace();
					return KuaiDi100Helper.CompanyInfo.info;
				}
			}
		}

		@Override
		protected void onPostExecute(ArrayList<KuaiDi100Helper.CompanyInfo.Company> result) {
			if (result != null) {
				mCompanyListAdapter = new CompanyListRecyclerAdapter(result);
				mRecyclerView.setAdapter(mCompanyListAdapter);
				mCompanyListAdapter.setOnItemClickListener(new MyRecyclerViewAdapter.OnItemClickListener() {
					@Override
					public void onItemClicked(int position) {
						Intent intent = new Intent();
						intent.putExtra("company_code", mCompanyListAdapter.getItem(position).code);
						setResult(RESULT_SELECTED, intent);
						close();
					}
				});
			}
		}

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

}
