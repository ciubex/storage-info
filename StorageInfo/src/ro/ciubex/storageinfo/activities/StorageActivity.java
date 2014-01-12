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
import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Window;

/**
 * This is actually the dialog showed to the user to quickly mount or unmount
 * the USB storage.
 * 
 * @author Claudiu Ciobotariu
 * 
 */
public class StorageActivity extends Activity implements DialogButtonListener {
	static final String TAG = StorageActivity.class.getName();
	private StorageInfoApplication mApplication;
	private String mStoragePath;
	private static final int ALERT_UNMOUNT = 0;
	private static final int ALERT_MOUNT = 1;

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
		requestWindowFeature(Window.FEATURE_LEFT_ICON);
		setContentView(R.layout.storage_layout);
		getWindow().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON,
				android.R.drawable.ic_dialog_alert);
		Application application = getApplication();
		if (application instanceof StorageInfoApplication) {
			mApplication = (StorageInfoApplication) application;
		}
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
		if (mStoragePath != null
				&& mApplication.getStorageState() == STORAGE_STATE.MOUNTED) {
			setTitle(R.string.confirm_unmount_title);
			showDialog(ALERT_UNMOUNT);
		} else {
			setTitle(R.string.confirm_mount_title);
			showDialog(ALERT_MOUNT);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateDialog(int, android.os.Bundle)
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		AlertDialog.Builder builder = null;
		switch (id) {
		case ALERT_UNMOUNT:
			builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.confirm_unmount_title).setMessage(
					mApplication.getString(R.string.confirm_unmount_text,
							mStoragePath));
			break;
		case ALERT_MOUNT:
			builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.confirm_mount_title).setMessage(
					R.string.confirm_mount_text);
			break;
		}
		if (builder != null) {
			builder.setCancelable(false)
					.setPositiveButton(R.string.ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									onClickOk();
								}
							})
					.setNegativeButton(R.string.cancel,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									onClickCancel();
								}
							});
			dialog = builder.create();
		}
		return dialog;
	}

	private void onClickOk() {
		if (mApplication.getStorageState() == STORAGE_STATE.MOUNTED) {
			doUnmount();
		} else {
			doMount();
		}
	}

	private void onClickCancel() {
		finish();
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
				mApplication.showExceptionMessage(this, mApplication
						.getString(R.string.error_unmount_title), mApplication
						.getString(R.string.error_unmount_text, mStoragePath, e
								.getMessage(), (e.getCause() != null) ? e
								.getCause().getCause() : "null"));
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
				mApplication.showExceptionMessage(this, mApplication
						.getString(R.string.error_mount_title), mApplication
						.getString(R.string.error_mount_text, e.getMessage(),
								(e.getCause() != null) ? e.getCause()
										.getCause() : "null"));
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
