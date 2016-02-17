package info.papdt.express.helper.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.ShareActionProvider;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;
import info.papdt.express.helper.R;
import info.papdt.express.helper.common.api.ACKDHelper;
import info.papdt.express.helper.common.model.Item;
import info.papdt.express.helper.common.model.ItemsKeeper;

public class DetailsActivity extends AbsActivity {

	private LinearLayout mContentLayout;
	private EditText mEditTextName;
	private TextView tv_company, tv_mail_no, tv_status, tv_round_center;
	private CircleImageView iv_round;
	private ImageButton mButtonNumberVisible;

	private int eid;
	private Item express;
	private Item.Result cache;
	private ItemsKeeper edb;

	private String phoneNumber;
	private boolean hasPhoneNumber = true;

	private boolean isEditingTitle = false, isShowingTrueNumber = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_details);

		Intent intent = getIntent();
		eid = intent.getIntExtra("id", 0);
		try {
			JSONObject obj = new JSONObject(intent.getStringExtra("data"));
			express = new Item(obj.getString("companyCode"),
					obj.getString("mailNumber"),
					obj.getString("name"));
			express.setData(obj.getString("cache"));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		cache = express.getData();

		edb = ItemsKeeper.getInstance(getApplicationContext());

		new Thread() {
			@Override
			public void run() {
				try {
					edb.init();
					edb.getItem(eid).needPush = false;
					edb.save();
				} catch (Exception e) {

				}
			}
		}.start();

		int company_id = ACKDHelper.CompanyInfo.findCompanyByCode(express.getCompanyCode());
		if (company_id != -1) {
			phoneNumber = ACKDHelper.CompanyInfo.info.get(company_id).phone;
			hasPhoneNumber = phoneNumber != null && phoneNumber != "null" && !TextUtils.isEmpty(phoneNumber);
		}

		mActionBar.setDisplayHomeAsUpEnabled(true);
		mActionBar.setTitle(express.getName());

		setUpHeaderView();
		setUpListView();
	}

	@Override
	protected void setUpViews() {
		mContentLayout = (LinearLayout) findViewById(R.id.content_list);
		tv_company = (TextView) findViewById(R.id.tv_express_company);
		tv_mail_no = (TextView) findViewById(R.id.tv_mail_no);
		tv_status = (TextView) findViewById(R.id.tv_status);
		tv_round_center = (TextView) findViewById(R.id.center_text);
		iv_round = (CircleImageView) findViewById(R.id.iv_round);
		mButtonNumberVisible = (ImageButton) findViewById(R.id.btn_number_visible);

		try {
			ViewCompat.setElevation(findViewById(R.id.headerView), getResources().getDimension(R.dimen.toolbar_elevation));
		} catch (NullPointerException e) {
			// Just ignore it.
		}

		mEditTextName = new EditText(mActionBar.getThemedContext());
		ActionBar.LayoutParams lp = new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER_VERTICAL);
		mActionBar.setCustomView(mEditTextName, lp);
		mActionBar.setDisplayShowCustomEnabled(false);
		mActionBar.setDisplayShowTitleEnabled(true);
		mButtonNumberVisible.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				toggleNumberShowing();
			}
		});
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		if (isEditingTitle) {
			getMenuInflater().inflate(R.menu.details_menu_editing, menu);
		} else {
			getMenuInflater().inflate(R.menu.details_menu, menu);
			menu.findItem(R.id.action_phone).setVisible(hasPhoneNumber);
			final MenuItem shouldPushItem = menu.findItem(R.id.action_should_push);
			new Thread() {
				@Override
				public void run() {
					try {
						edb.init();
						final boolean b = edb.getItem(eid).shouldPush;
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								shouldPushItem.setChecked(b);
							}
						});
					} catch (Exception e) {

					}
				}
			}.start();

			try {
				Intent intent = new Intent(Intent.ACTION_SEND);
				intent.setType("text/plain");
				intent.putExtra(Intent.EXTRA_TEXT,
						String.format(
								getString(R.string.string_copy_format),
								express.getName(),
								express.getMailNumber(),
								cache.expTextName
						) + String.format(
								getString(R.string.string_share_format),
								getResources().getStringArray(R.array.status)[cache.getTrueStatus()],
								cache.data.get(cache.data.size() - 1).get("context")
						)
				);
				ShareActionProvider provider = (ShareActionProvider) MenuItemCompat.getActionProvider(menu.findItem(R.id.action_share));
				provider.setShareIntent(intent);
			} catch (Exception e) {

			}
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_edit_done) {
			isEditingTitle = false;
			new Thread() {
				@Override
				public void run() {
					edb.init();
					edb.getItem(eid).setName(mEditTextName.getText().toString().trim());
					try {
						edb.save();
					} catch (IOException e) {
						e.printStackTrace();
					}
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							mActionBar.setTitle(edb.getItem(eid).getName());
							setResult(MainActivity.RESULT_HAS_CHANGED);
						}
					});
				}
			}.start();
			hideSoftKeyboard();
			syncActionBarCustomView();
			invalidateOptionsMenu();
			return true;
		}
		if (id == R.id.action_edit) {
			isEditingTitle = true;
			mEditTextName.setText(mActionBar.getTitle());
			syncActionBarCustomView();
			invalidateOptionsMenu();
			return true;
		}
		if (id == R.id.action_phone) {
			Intent intent = new Intent(Intent.ACTION_DIAL);
			intent.setData(Uri.parse("tel:" + phoneNumber));
			startActivity(intent);
			return true;
		}
		if (id == R.id.action_copy) {
			setClipboard(
					String.format(
							getString(R.string.string_copy_format),
							express.getName(),
							express.getMailNumber(),
							cache.expTextName
					)
			);
			Toast.makeText(
					getApplicationContext(),
					R.string.details_has_copied,
					Toast.LENGTH_SHORT
			).show();
			return true;
		}
		if (id == R.id.action_should_push) {
			express.shouldPush = !item.isChecked();
			item.setChecked(!item.isChecked());
			new Thread() {
				@Override
				public void run() {
					try {
						edb.init();
						edb.getItem(eid).shouldPush = express.shouldPush;
						edb.save();
						setResult(MainActivity.RESULT_HAS_CHANGED);
					} catch (Exception e) {

					}
				}
			}.start();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void toggleNumberShowing() {
		isShowingTrueNumber = !isShowingTrueNumber;
		if (isShowingTrueNumber) {
			tv_mail_no.setText(express.getMailNumber());
			mButtonNumberVisible.setImageResource(R.drawable.ic_visibility_white_24dp);
		} else {
			int length = tv_mail_no.length();
			String str = "";
			for (int i = 0; i < length; i++) str += "*";
			tv_mail_no.setText(str);
			mButtonNumberVisible.setImageResource(R.drawable.ic_visibility_off_white_24dp);
		}
	}

	@Override
	public void onBackPressed() {
		if (isEditingTitle) {
			isEditingTitle = false;
			hideSoftKeyboard();
			syncActionBarCustomView();
			invalidateOptionsMenu();
		} else {
			scrollToFinishActivity();
		}
	}

	private void hideSoftKeyboard() {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(mEditTextName.getWindowToken(), 0);
	}

	private void syncActionBarCustomView() {
		mActionBar.setDisplayShowCustomEnabled(isEditingTitle);
		mActionBar.setDisplayShowTitleEnabled(!isEditingTitle);
	}

	private void addDetailsItem(String title, String info) {
		View v = View.inflate(
				new ContextThemeWrapper(
						getApplicationContext(),
						R.style.Base_Theme_AppCompat_Light
				), R.layout.simple_list_item, null
		);

		((TextView) v.findViewById(android.R.id.text1)).setText(title);
		((TextView) v.findViewById(android.R.id.text2)).setText(info);
		v.setTag(title + ": " + info);
		v.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View view) {
				setClipboard((String) view.getTag());
				Toast.makeText(
						getApplicationContext(),
						R.string.details_has_copied,
						Toast.LENGTH_SHORT
				).show();
				return true;
			}
		});

		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

		mContentLayout.addView(v, 0, lp);
	}

	private void setUpHeaderView() {
		tv_status.setText(getResources().getStringArray(R.array.status)[cache.getTrueStatus()]);

		try {
			tv_company.setText(cache.expTextName);
			tv_round_center.setText(cache.expTextName.substring(0, 1));
		} catch (Exception e) {

		}

		tv_mail_no.setText(express.getMailNumber());
		toggleNumberShowing();

		ColorDrawable drawable = new ColorDrawable(getResources().getIntArray(R.array.statusColor) [cache.status]);
		iv_round.setImageDrawable(drawable);
	}

	private void setUpListView() {
		if (cache.errCode != 0){
			addDetailsItem(getString(R.string.item_errorcode), getResources().getStringArray(R.array.errCode)[cache.errCode]);
			addDetailsItem(getString(R.string.item_errormessage), cache.message);
		}

		for (int i = 0; i < cache.data.size(); i++){
			addDetailsItem(cache.data.get(i).get("time"), cache.data.get(i).get("context"));
		}
	}

	private void setClipboard(String text) {
		ClipboardManager clipMan = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
		clipMan.setPrimaryClip(ClipData.newPlainText(null, text));
	}

}
