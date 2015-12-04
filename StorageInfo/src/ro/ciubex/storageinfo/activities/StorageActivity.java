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
import ro.ciubex.storageinfo.model.MountVolume;
import ro.ciubex.storageinfo.util.Utils.MountService;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
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
	private static final int ALERT_UNMOUNT = 0;
	private static final int ALERT_MOUNT = 1;
	private static final int ALERT_INVALID = 2;
	private MountVolume mMountVolume;
	private String mMountState;

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
		initActivity(getApplication());
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		initActivity(getApplication());
	}

	private void initActivity(Application application) {
		if (application instanceof StorageInfoApplication) {
			mApplication = (StorageInfoApplication) application;
			int storageId = getIntent().getIntExtra("storageId", -1);
			if (storageId != -1) {
				mMountVolume = mApplication.getMountVolume(storageId);
				if (mMountVolume != null) {
					mMountState = mMountVolume.getVolumeState();
				}
			}
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
		if (mMountVolume != null) {
			prepareActivityText();
		} else {
			showStorageSettings();
		}
	}

	/**
	 * Prepare texts for this activity.
	 */
	private void prepareActivityText() {
		int alertId = ALERT_INVALID;
		if (Environment.MEDIA_MOUNTED.equals(mMountState)) {
			setTitle(R.string.confirm_unmount_title);
			alertId = ALERT_UNMOUNT;
		} else if (Environment.MEDIA_UNMOUNTED.equals(mMountState)) {
			setTitle(R.string.confirm_mount_title);
			alertId = ALERT_MOUNT;
		} else {
			setTitle(R.string.invalid_mount_title);
			showDialog(ALERT_INVALID);
		}
		if (mApplication.hideUnmountConfirmation() && alertId != ALERT_INVALID) {
			onClickOk();
		} else {
			showDialog(alertId);
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
		String path = mMountVolume.getPath();
		switch (id) {
		case ALERT_UNMOUNT:
			builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.confirm_unmount_title).setMessage(
					getString(R.string.confirm_unmount_text, path));
			break;
		case ALERT_MOUNT:
			builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.confirm_mount_title).setMessage(
					getString(R.string.confirm_mount_text, path));
			break;
		case ALERT_INVALID:
			builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.invalid_mount_title).setMessage(
					getString(R.string.invalid_mount_text, path));
			break;
		}
		if (builder != null) {
			builder.setCancelable(false)
					.setNegativeButton(R.string.cancel,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									onClickCancel();
								}
							})
					.setPositiveButton(R.string.ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									onClickOk();
								}
							});
			dialog = builder.create();
		}
		return dialog;
	}

	private void onClickOk() {
		if (Environment.MEDIA_MOUNTED.equals(mMountState)) {
			doUnmount();
		} else if (Environment.MEDIA_UNMOUNTED.equals(mMountState)) {
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
		try {
			startActivity(intent);
			finish();
		} catch (ActivityNotFoundException e) {
			mApplication.showExceptionMessage(
					this,
					getString(R.string.error_storage_settings_title),
					getString(R.string.error_storage_settings_text,
							Settings.ACTION_INTERNAL_STORAGE_SETTINGS), true);
		}
	}

	/**
	 * Do unmount
	 */
	private void doUnmount() {
		if (mMountVolume != null) {
			String path = mMountVolume.getPath();
			try {
				MountService.unmountVolume(mApplication.getMountService(), path, true);
				finish();
			} catch (Exception e) {
				mApplication.logE(TAG, e.getMessage(), e);
				handleUnmountException(e, path);
			}
		}
	}

	/**
	 * Handle the unmount exceptions cases.
	 * @param e The encountered exception.
	 * @param path The path for which was encountered the exception.
	 */
	private void handleUnmountException(Exception e, String path) {
		if (e.getCause() instanceof SecurityException ||
				(e.getCause() != null && e.getCause().getCause() instanceof SecurityException)) {
			mApplication.showExceptionMessage(
					this,
					getString(R.string.error_unmount_title),
					getString(R.string.error_unmount_text_SecurityException, path));
		} else {
			mApplication.showExceptionMessage(
					this,
					getString(R.string.error_unmount_title),
					getString(R.string.error_unmount_text, path, e
							.getMessage(), (e.getCause() != null) ? e
							.getCause().getCause() : "null"));
		}
	}

	/**
	 * Do mount
	 */
	private void doMount() {
		if (mMountVolume != null) {
			String path = mMountVolume.getPath();
			try {
				int result = MountService.mountVolume(
						mApplication.getMountService(), path);
				if (result != 0) {
					mApplication.showExceptionMessage(this,
							getString(R.string.error_mount_title),
							getString(R.string.error_mount_detach_text, path));
				} else {
					finish();
				}
			} catch (Exception e) {
				mApplication.logE(TAG, e.getMessage(), e);
				handleMountException(e, path);
			}
		}
	}

	/**
	 * Handle the mount exceptions cases.
	 * @param e The encountered exception.
	 * @param path The path for which was encountered the exception.
	 */
	private void handleMountException(Exception e, String path) {
		if (e.getCause() instanceof SecurityException ||
				(e.getCause() != null && e.getCause().getCause() instanceof SecurityException)) {
			mApplication.showExceptionMessage(
					this,
					getString(R.string.error_mount_title),
					getString(R.string.error_mount_text_SecurityException, path));
		} else {
			mApplication.showExceptionMessage(
					this,
					getString(R.string.error_mount_title),
					getString(R.string.error_mount_text, path, e
							.getMessage(), (e.getCause() != null) ? e
							.getCause().getCause() : "null"));
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
