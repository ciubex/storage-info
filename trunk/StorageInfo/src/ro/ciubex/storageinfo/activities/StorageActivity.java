/**
 * This file is part of StorageInfo application.
 * 
 * Copyright (C) 2013 Claudiu Ciobotariu
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ro.ciubex.storageinfo.activities;

import ro.ciubex.storageinfo.R;
import ro.ciubex.storageinfo.StorageInfoApplication;
import ro.ciubex.storageinfo.StorageInfoApplication.STORAGE_STATE;
import ro.ciubex.storageinfo.util.Utils.MountService;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * This is actually the dialog showed to the user to quickly mount or unmount
 * the USB storage.
 * 
 * @author Claudiu Ciobotariu
 * 
 */
public class StorageActivity extends Activity implements View.OnClickListener,
		DialogButtonListener {
	static final String TAG = StorageActivity.class.getName();
	private StorageInfoApplication mApplication;
	private TextView confirmText;
	private Button btnOk, btnCancel;
	private String mStoragePath;

	/**
	 * Called when the activity is starting.
	 * 
	 * @param savedInstanceState
	 *            If the activity is being re-initialized after previously being
	 *            shut down then this Bundle contains the data it most recently
	 *            supplied in onSaveInstanceState(Bundle).
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.storage_layout);
		Application application = getApplication();
		if (application instanceof StorageInfoApplication) {
			mApplication = (StorageInfoApplication) application;
		}

		confirmText = (TextView) findViewById(R.id.confirmText);
		initButtons();
	}

	/**
	 * Initialize default buttons.
	 */
	private void initButtons() {
		btnOk = (Button) findViewById(R.id.btn_ok);
		btnOk.setOnClickListener(this);
		btnCancel = (Button) findViewById(R.id.btn_cancel);
		btnCancel.setOnClickListener(this);
	}

	/**
	 * Method invoked when the activity is resumed.
	 */
	@Override
	protected void onResume() {
		super.onResume();
		prepareActivity();
	}

	/**
	 * Prepare this activity for unmount, mount or storage settings.
	 */
	private void prepareActivity() {
		mStoragePath = mApplication.getStoragePath();
		if (mApplication.getStorageState() != STORAGE_STATE.OTHER) {
			prepareActivityText();
		} else {
			showStorageSettings();
		}
	}

	/**
	 * Prepare texts for this activity.
	 */
	private void prepareActivityText() {
		if (mApplication.getStorageState() == STORAGE_STATE.MOUNTED) {
			setTitle(R.string.confirm_unmount_title);
			confirmText.setText(mApplication
					.getString(R.string.confirm_unmount_text));
		} else {
			setTitle(R.string.confirm_mount_title);
			confirmText.setText(mApplication
					.getString(R.string.confirm_mount_text));
		}
	}

	/**
	 * Called when a view has been clicked.
	 * 
	 * @param view
	 *            The view that was clicked.
	 */
	@Override
	public void onClick(View view) {
		if (view == btnOk) {
			if (mApplication.getStorageState() == STORAGE_STATE.MOUNTED) {
				doUnmount();
			} else {
				doMount();
			}
		}
	}

	/**
	 * Hide this activity.
	 */
	private void showStorageSettings() {
		Intent intent = new Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		finish();
	}

	/**
	 * Do unmount
	 */
	private void doUnmount() {
		if (mStoragePath != null) {
			try {
				MountService.unmountVolume(mApplication.getMountService(),
						mStoragePath, true);
				finish();
			} catch (Exception e) {
				Log.e(TAG, e.getMessage(), e);
				mApplication.showExceptionMessage(
						this,
						mApplication.getString(R.string.error_unmount_title),
						mApplication.getString(R.string.error_unmount_text,
								e.getMessage()));
			}
		}
	}

	/**
	 * Do mount
	 */
	private void doMount() {
		if (mStoragePath != null) {
			try {
				MountService.mountVolume(mApplication.getMountService(),
						mStoragePath);
				finish();
			} catch (Exception e) {
				Log.e(TAG, e.getMessage(), e);
				mApplication.showExceptionMessage(
						this,
						mApplication.getString(R.string.error_mount_title),
						mApplication.getString(R.string.error_mount_text,
								e.getMessage()));
			}
		}
	}

	/**
	 * Invoked when the user click on a dialog button.
	 * 
	 * @param buttonId
	 *            The ID of the clicked button.
	 */
	@Override
	public void onButtonClicked(int buttonId) {
		finish();
	}

	/**
	 * Obtain the activity context.
	 * 
	 * @return The activity context.
	 */
	@Override
	public Context getContext() {
		return this;
	}
}
