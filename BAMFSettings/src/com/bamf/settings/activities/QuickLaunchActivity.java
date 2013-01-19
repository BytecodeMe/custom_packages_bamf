package com.bamf.settings.activities;

import java.util.ArrayList;

import com.android.internal.view.RotationPolicy;
import com.bamf.settings.R;
import com.bamf.settings.utils.QuickSettingsUtil;
import com.bamf.settings.widgets.ReorderListView;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.PreferenceActivity;
import android.provider.Settings;
import android.util.Log;
import android.util.TypedValue;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class QuickLaunchActivity extends ListActivity implements
		OnClickListener, OnItemLongClickListener {

	private static final String TAG = "LockScreenSettings";

	/** If there is no setting in the provider, use this. */

	private static final int REQUEST_PICK_SHORTCUT = 1;
	private static final int REQUEST_PICK_APPLICATION = 2;
	private static final int REQUEST_CREATE_SHORTCUT = 3;

	private PackageManager mPackageManager;
	private ContentResolver mResolver;
	private ListView mIntentList;
	private IntentAdapter mIntentAdapter;
	private Button mClearButton;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.reorder_list_activity);
		mPackageManager = getPackageManager();
		mResolver = getContentResolver();

		mIntentList = getListView();
		mIntentList.setFastScrollEnabled(true);
		mIntentList.setOnItemLongClickListener(this);

		((ReorderListView) mIntentList).setDropListener(mDropListener);

		if (mIntentAdapter == null) {
			mIntentAdapter = new IntentAdapter(this);
		}
		setListAdapter(mIntentAdapter);

		Button addButton = (Button) findViewById(R.id.lockscreen_button_add);
		addButton.setOnClickListener(this);
		mClearButton = (Button) findViewById(R.id.lockscreen_button_clear);
		mClearButton.setEnabled(!parseIntents().isEmpty());
		mClearButton.setOnClickListener(this);

		setupActionBar();

	}

	private void setupActionBar() {
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP,
				ActionBar.DISPLAY_HOME_AS_UP);
		actionBar.setTitle(R.string.quicklaunch_settings);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// app icon in action bar clicked;
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.lockscreen_button_add:
			pickShortcut();
			break;
		case R.id.lockscreen_button_clear:
			new AlertDialog.Builder(this)
					.setTitle(getString(R.string.lockscreen_clear_shortcut))
					.setMessage(
							getString(R.string.lockscreen_clear_description))
					.setPositiveButton(getString(android.R.string.ok),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									setIntents(new ArrayList<String>());
								}
							})
					.setNegativeButton(getString(android.R.string.cancel),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {

								}
							}).show();
			break;
		}
	}

	private void pickShortcut() {

		Bundle bundle = new Bundle();

		ArrayList<String> shortcutNames = new ArrayList<String>();

		shortcutNames.add(getString(R.string.group_applications));
		bundle.putStringArrayList(Intent.EXTRA_SHORTCUT_NAME, shortcutNames);

		ArrayList<ShortcutIconResource> shortcutIcons = new ArrayList<ShortcutIconResource>();

		shortcutIcons.add(ShortcutIconResource.fromContext(this,
				R.drawable.ic_lockscreen_apps));
		bundle.putParcelableArrayList(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
				shortcutIcons);

		Intent pickIntent = new Intent(Intent.ACTION_PICK_ACTIVITY);
		pickIntent.putExtras(bundle);
		pickIntent.putExtra(Intent.EXTRA_INTENT, new Intent(
				Intent.ACTION_CREATE_SHORTCUT));
		pickIntent.putExtra(Intent.EXTRA_TITLE,
				getString(R.string.use_custom_title));

		startActivityForResult(pickIntent, REQUEST_PICK_SHORTCUT);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (resultCode == Activity.RESULT_OK) {
			switch (requestCode) {
			case REQUEST_PICK_APPLICATION:
				completeSetCustomApp(data);
				break;
			case REQUEST_CREATE_SHORTCUT:
				completeSetCustomShortcut(data);
				break;
			case REQUEST_PICK_SHORTCUT:
				processShortcut(data, REQUEST_PICK_APPLICATION,
						REQUEST_CREATE_SHORTCUT);
				break;
			}
		}
	}

	void processShortcut(Intent intent, int requestCodeApplication,
			int requestCodeShortcut) {

		// Handle case where user selected "Applications"
		String applicationName = getString(R.string.group_applications);
		String shortcutName = intent.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);

		if (applicationName != null && applicationName.equals(shortcutName)) {
			Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
			mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

			Intent pickIntent = new Intent(Intent.ACTION_PICK_ACTIVITY);
			pickIntent.putExtra(Intent.EXTRA_INTENT, mainIntent);
			startActivityForResult(pickIntent, requestCodeApplication);
		} else {
			startActivityForResult(intent, requestCodeShortcut);
		}
	}

	void completeSetCustomShortcut(Intent data) {

		Intent intent = (Intent) data
				.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);

		ArrayList<String> temp = parseIntents();
		temp.add(intent.toUri(0));

		setIntents(temp);

	}

	void completeSetCustomApp(Intent data) {

		ArrayList<String> temp = parseIntents();
		temp.add(data.toUri(0));

		setIntents(temp);
	}

	private ReorderListView.DropListener mDropListener = new ReorderListView.DropListener() {
		public void drop(int from, int to) {
			// get the current button list
			ArrayList<String> currentIntents = parseIntents();

			// move the button
			if (from < currentIntents.size()) {
				String intent = currentIntents.remove(from);

				if (to <= currentIntents.size()) {
					currentIntents.add(to, intent);
					setIntents(currentIntents);

				}
			}
		}
	};

	private ArrayList<String> parseIntents() {

		final ArrayList<String> mQuickIntents = new ArrayList<String>();

		String[] temp = Settings.System.getString(mResolver,
				Settings.System.QUICK_LAUNCH_TARGETS, "").split("<>");

		for (String s : temp) {
			if (!s.isEmpty())
				mQuickIntents.add(s);
		}
		if (mQuickIntents.isEmpty())
			mQuickIntents.add("");
		return mQuickIntents;
	}

	protected void setIntents(ArrayList<String> intents) {

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < intents.size(); i++) {
			sb.append(intents.get(i));
			if (i < (intents.size() - 1)) {
				sb.append("<>");
			}
		}
		Settings.System.putString(mResolver,
				Settings.System.QUICK_LAUNCH_TARGETS, sb.toString());

		// tell our adapter/listview to reload
		mIntentAdapter.reloadIntents();
		mIntentAdapter.notifyDataSetChanged();
		mIntentList.invalidateViews();

		mClearButton.setEnabled(!intents.isEmpty());

	}

	private class IntentAdapter extends BaseAdapter {
		private Context mContext;
		private LayoutInflater mInflater;
		private ArrayList<String> mIntents;

		public IntentAdapter(Context c) {
			mContext = c;
			mInflater = LayoutInflater.from(mContext);

			reloadIntents();
		}

		public void reloadIntents() {

			mIntents = parseIntents();
		}

		public int getCount() {
			return mIntents.size();
		}

		public Object getItem(int position) {
			return mIntents.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			final View v;
			if (convertView == null) {
				v = mInflater
						.inflate(R.layout.list_item_image_text_image, null);
			} else {
				v = convertView;
			}				

			ActivityInfo ai = null;
			try {
				ai = mPackageManager.getActivityInfo(
						Intent.parseUri(mIntents.get(position), 0)
								.resolveActivity(mPackageManager), 0);
			} catch (Exception e) {
				e.printStackTrace();
			}

			IntentAdapter local = this;
			final ViewHolder vh;

			if (v.getTag() == null) {
				vh = new ViewHolder(local);
			} else {
				vh = (ViewHolder) v.getTag();
			}

			vh.line1 = (TextView) v.findViewById(R.id.txt_1x1);
			vh.icon = (ImageView) v.findViewById(R.id.img_1x1);
			v.findViewById(R.id.img_indicator).setVisibility(View.GONE);

			vh.line1.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
			vh.line1.setText(ai == null ? (mIntents.size() <= 1 && position == 0) ? getString(R.string.warning_none_set)
					: getString(R.string.warning_problem_shortcut)
					: ai.loadLabel(mPackageManager));

			vh.icon.setImageDrawable(ai == null ? null : ai
					.loadIcon(mPackageManager));
			vh.icon.setVisibility(ai == null ? View.GONE : View.VISIBLE);

			v.setTag(vh);

			return v;
		}

		public class ViewHolder {
			ImageView icon;
			TextView line1;

			ViewHolder(IntentAdapter intentAdapter) {
			}
		}

	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			final int position, long id) {
		
		new AlertDialog.Builder(this)
        .setTitle("Delete")
        .setMessage("Are you sure you want to remove this shortcut?")
        .setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                	ArrayList<String> temp = parseIntents();
            		temp.remove(position); 
            		setIntents(temp);
                }
        })
        .setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                        
                }
        })
        .show();   
		
		return true;
	}

}
