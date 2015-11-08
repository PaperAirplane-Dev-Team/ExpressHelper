package info.papdt.express.helper.ui;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.ViewCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.melnykov.fab.FloatingActionButton;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.HashMap;
import java.util.Random;

import info.papdt.express.helper.R;
import info.papdt.express.helper.api.KuaiDi100Helper;
import info.papdt.express.helper.dao.ExpressDatabase;
import info.papdt.express.helper.support.ExpressResult;
import info.papdt.express.helper.support.HttpUtils;
import info.papdt.express.helper.support.Settings;
import info.papdt.express.helper.support.Utility;

public class AddActivity extends AbsActivity {

	private boolean isChecking = false;

	private MaterialEditText mEditTextSerial, mEditTextName;
	private TextView mCompanyNameText;
	private ProgressBar mProgress;
	private FloatingActionButton mFAB;
	private CheckBox mForceAddCheckBox;
	private int mNow = -1;

	public static final String TAG = "AddActivity";

	public static final String TRANSITION_NAME_FAB = "fab";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add);

		mActionBar.setDisplayHomeAsUpEnabled(true);
		mActionBar.setDisplayShowTitleEnabled(false);
	}

	@Override
	protected void setUpViews() {
		mEditTextSerial = (MaterialEditText) findViewById(R.id.et_number);
		mCompanyNameText = (TextView) findViewById(R.id.tv_company_name);
		mEditTextName = (MaterialEditText) findViewById(R.id.et_name);
		mProgress = (ProgressBar) findViewById(R.id.progressBar);
		mFAB = (FloatingActionButton) findViewById(R.id.fab);
		mForceAddCheckBox = (CheckBox) findViewById(R.id.cb_force_add);
		mFAB.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isChecking) return;
				postData();
			}
		});

		ViewCompat.setElevation(findViewById(R.id.headerView), getResources().getDimension(R.dimen.toolbar_elevation));

		ImageButton mButtonSelect = (ImageButton) findViewById(R.id.btn_select);
		mButtonSelect.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mEditTextName.clearFocus();
				mEditTextSerial.clearFocus();
				CompanySelectActivity.launchActivity(AddActivity.this);
			}
		});

		if (!mSets.getBoolean(Settings.KEY_DISABLE_ANIMATION, false)) {
			ViewCompat.setTransitionName(mFAB, TRANSITION_NAME_FAB);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		switch (requestCode) {
			case CompanySelectActivity.REQUEST_CODE_SELECT:
				if (resultCode == CompanySelectActivity.RESULT_SELECTED) {
					mNow = KuaiDi100Helper.CompanyInfo.findCompanyByCode(intent.getStringExtra("company_code"));
					setCompanyNameText();
				}
				break;
		}
	}

	@Override
	public void onBackPressed() {
		setSwipeBackEnable(false);
		super.onBackPressed();
	}

	private void setCompanyNameText() {
		mCompanyNameText.setText(KuaiDi100Helper.CompanyInfo.names [mNow]);
	}

	private void postData() {
		if (TextUtils.isEmpty(mEditTextSerial.getText())) {
			Toast.makeText(
					getApplicationContext(),
					R.string.toast_number_empty,
					Toast.LENGTH_SHORT
			).show();
			return;
		}
		if (mNow == -1) {
			Toast.makeText(
					getApplicationContext(),
					R.string.toast_company_name_empty,
					Toast.LENGTH_SHORT
			).show();
			return;
		}

		new PostApiTask().execute(
				KuaiDi100Helper.CompanyInfo.info.get(mNow).code,
				mEditTextSerial.getText().toString()
		);
	}
	
	private void receiveData(String result, String name) {
		if (!mForceAddCheckBox.isChecked()) {
			ExpressResult er = ExpressResult.buildFromJSON(result);
			if (er.getTrueStatus() == ExpressResult.STATUS_FAILED) {
				Toast.makeText(
						getApplicationContext(),
						getResources().getStringArray(R.array.errCode_toast) [er.errCode],
						Toast.LENGTH_SHORT
				).show();
				return;
			}
		}

		Intent intent = new Intent(this, MainActivity.class);
		intent.putExtra("result", result);
		intent.putExtra("name", name);
		setResult(MainActivity.RESULT_ADD_FINISH, intent);
		finish();
	}

	public static void launch(AbsActivity mActivity, View fab) {
		Intent intent = new Intent(mActivity, AddActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
		if (!mActivity.mSets.getBoolean(Settings.KEY_DISABLE_ANIMATION, false)) {
			ActivityOptionsCompat options =
					ActivityOptionsCompat.makeSceneTransitionAnimation(
							mActivity, fab, TRANSITION_NAME_FAB
					);
			ActivityCompat.startActivityForResult(
					mActivity,
					intent,
					MainActivity.REQUEST_ADD,
					options.toBundle()
			);
		} else {
			mActivity.startActivityForResult(intent, MainActivity.REQUEST_ADD);
		}
	}

	private class PostApiTask extends AsyncTask<String, Void, String> {

		public static final String FLAG_COMPANY_NOT_EXIST = "company_null",
				FLAG_NETWORK_ERROR = "network_error", FLAG_UNKNOWN_ERROR = "unknown_error",
				FLAG_CLIENT_ERROR = "client_error", FLAG_HAS_BEEN_EXIST = "has_been_exist";

		@Override
		protected void onPreExecute() {
			isChecking = true;
			mProgress.setVisibility(View.VISIBLE);
		}

		@Override
		protected String doInBackground(String... src) {
			String[] result = new String[1];
			String companyCode = src[0];
			String mailNumber = src[1];

			ExpressDatabase db = ExpressDatabase.getInstance(getApplicationContext());
			db.init();
			if (db.findExpress(companyCode, mailNumber) != -1) {
				return FLAG_HAS_BEEN_EXIST;
			}

			HashMap<String, String> token = Utility.getAPIToken(getApplicationContext());
			String app_id = token.get("id");
			String secret = token.get("secret");

			int resultCode = HttpUtils.get(KuaiDi100Helper.getRequestUrl(app_id, secret, companyCode, mailNumber, "utf8"), result);
			switch (resultCode) {
				case HttpUtils.CODE_OKAY:
					return result[0];
				case HttpUtils.CODE_NONE_200:
				case HttpUtils.CODE_NETWORK_ERROR:
					return FLAG_NETWORK_ERROR;
				case HttpUtils.CODE_CLIENT_ERROR:
					return FLAG_CLIENT_ERROR;
				default:
					return FLAG_UNKNOWN_ERROR;
			}

		}

		@Override
		protected void onPostExecute(String result) {
			isChecking = false;
			mProgress.setVisibility(View.INVISIBLE);
			if (result == null || result == FLAG_UNKNOWN_ERROR) {
				Toast.makeText(
						getApplicationContext(),
						R.string.toast_unknown_error,
						Toast.LENGTH_SHORT
				).show();
				return;
			}
			if (result == FLAG_COMPANY_NOT_EXIST) {
				Toast.makeText(
						getApplicationContext(),
						R.string.toast_company_not_exist,
						Toast.LENGTH_SHORT
				).show();
				return;
			}
			if (result == FLAG_CLIENT_ERROR) {
				Toast.makeText(
						getApplicationContext(),
						R.string.toast_client_error,
						Toast.LENGTH_SHORT
				).show();
				return;
			}
			if (result == FLAG_HAS_BEEN_EXIST) {
				Toast.makeText(
						getApplicationContext(),
						R.string.toast_has_been_exist,
						Toast.LENGTH_SHORT
				).show();
				return;
			}
			String name = mEditTextName.getText().toString();
			if (name == null || TextUtils.isEmpty(name)) {
				name = mEditTextSerial.getText().toString();
			}
			receiveData(result, name);
		}

	}

}
