package info.papdt.express.helper.ui;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import info.papdt.express.helper.R;
import info.papdt.express.helper.common.Settings;
import info.papdt.express.helper.common.Utility;
import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

public abstract class AbsActivity extends SwipeBackActivity {

	protected Toolbar mToolbar;
	protected ActionBar mActionBar;
	protected Settings mSets;
	private SwipeBackLayout mSwipeBackLayout;

	protected int statusBarHeight = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		this.onCreate(savedInstanceState, true);
	}

	protected void onCreate(Bundle savedInstanceState, boolean useTranslucentStatusBar) {
		/** Initialize Settings */
		mSets = Settings.getInstance(getApplicationContext());
		statusBarHeight = Utility.getStatusBarHeight(getApplicationContext());

		/** Set up translucent status bar */
		if (Build.VERSION.SDK_INT >= 19 && !Utility.isChrome() && useTranslucentStatusBar) {
			getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
		}

		if (Build.VERSION.SDK_INT >= 21) {
			if (useTranslucentStatusBar) {
				getWindow().setStatusBarColor(Color.TRANSPARENT);
			}
			if (mSets.getBoolean(Settings.KEY_NAVIGATION_TINT, true)) {
				getWindow().setNavigationBarColor(getResources().getColor(R.color.blue_800));
			}
		}

		super.onCreate(savedInstanceState);
	}

	protected abstract void setUpViews();

	@Override
	public void setContentView(@LayoutRes int layoutResId) {
		super.setContentView(layoutResId);

		try {
			View statusHeaderView = findViewById(R.id.statusHeaderView);
			statusHeaderView.getLayoutParams().height = statusBarHeight;
		} catch (NullPointerException e) {

		}

		try {
			mToolbar = (Toolbar) findViewById(R.id.toolbar);
			setSupportActionBar(mToolbar);
		} catch (Exception e) {

		}
		mActionBar = getSupportActionBar();

		try {
			mSwipeBackLayout = getSwipeBackLayout();

			mSwipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT | SwipeBackLayout.EDGE_RIGHT);
			mSwipeBackLayout.setEnableGesture(mSets.getBoolean(Settings.KEY_SWIPE_BACK, true));
		} catch (Exception e) {

		}

		setUpViews();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			this.onBackPressed();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {
		if (mSwipeBackLayout != null && mSwipeBackLayout.isGestureEnabled()) {
			scrollToFinishActivity();
		} else {
			super.onBackPressed();
		}
	}

}
