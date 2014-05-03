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

import java.util.List;

import ro.ciubex.storageinfo.R;
import ro.ciubex.storageinfo.StorageInfoApplication;
import ro.ciubex.storageinfo.list.ApplicationsListAdapter;
import ro.ciubex.storageinfo.model.AppInfo;
import ro.ciubex.storageinfo.task.ScanForApplications;
import ro.ciubex.storageinfo.util.Utils;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.provider.Settings;

/**
 * This is settings preference activity.
 * 
 * @author Claudiu Ciobotariu
 * 
 */
public class StorageInfoPreferences extends PreferenceActivity implements
		ScanForApplications.Listener, DialogButtonListener {
	private StorageInfoApplication mApplication;
	private Preference mSetFileManager;
	private final int CONFIRM_ID_DONATE = 1;

	/**
	 * Method called when this preference activity is created
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mApplication = (StorageInfoApplication) getApplication();
		addPreferencesFromResource(R.xml.application_preferences);
		prepareCommands();
	}

	/**
	 * Prepare preference handler
	 */
	private void prepareCommands() {
		((Preference) findPreference("showStorageInfo"))
				.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

					@Override
					public boolean onPreferenceClick(Preference preference) {
						return onShowStorageInfo();
					}
				});
		mSetFileManager = (Preference) findPreference("setFileManager");
		mSetFileManager
				.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

					@Override
					public boolean onPreferenceClick(Preference preference) {
						return onSetFileManager();
					}
				});
		((Preference) findPreference("toggleNotification"))
				.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

					@Override
					public boolean onPreferenceClick(Preference preference) {
						return onToggleNotification();
					}
				});
		((Preference) findPreference("showLicense"))
				.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

					@Override
					public boolean onPreferenceClick(Preference preference) {
						return onShowLicense();
					}
				});
		((Preference) findPreference("showAbout"))
				.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

					@Override
					public boolean onPreferenceClick(Preference preference) {
						return onShowAbout();
					}
				});
		((Preference) findPreference("makeDonation"))
				.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

					@Override
					public boolean onPreferenceClick(Preference preference) {
						return onMakeDonation();
					}
				});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		prepareTexts();
	}

	/**
	 * Prepare all necessary texts.
	 */
	private void prepareTexts() {
		String fileManager = mApplication.getFileManager();
		String text = getString(R.string.file_manager);
		if (!"null".equals(fileManager)) {
			AppInfo appInfo = Utils.getFileManager(getPackageManager(),
					fileManager);
			text = getString(R.string.file_manager_selected, appInfo.getName());
		}
		mSetFileManager.setTitle(text);
	}

	/**
	 * Show a list with applications to chose a file manager.
	 * 
	 * @return Always true.
	 */
	protected boolean onSetFileManager() {
		List<AppInfo> list = mApplication.getApplicationsList();
		if (list == null || list.isEmpty()) {
			new ScanForApplications(this).execute();
		} else {
			checkApplicationList();
		}
		return true;
	}

	/**
	 * Show the Storage Info activity.
	 * 
	 * @return Always true.
	 */
	private boolean onShowStorageInfo() {
		Intent intent = new Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		finish();
		return true;
	}

	/**
	 * Toggle notification shortcut.
	 * 
	 * @return Always true.
	 */
	private boolean onToggleNotification() {
		if (mApplication.isEnableNotifications()) {
			if (mApplication.isShowNotification()) {
				mApplication.hideAllNotifications();
			} else {
				if (mApplication.isEnabledQuickStorageAccess()) {
					mApplication.updateMountVolumes();
					mApplication.updateNotifications();
				} else {
					mApplication.showDefaultNotification();
				}
			}
		}
		return true;
	}

	/**
	 * Show License view.
	 * 
	 * @return Always true.
	 */
	private boolean onShowLicense() {
		Intent intent = new Intent(this, LicenseActivity.class);
		startActivity(intent);
		return true;
	}

	/**
	 * Show About view.
	 * 
	 * @return Always true.
	 */
	private boolean onShowAbout() {
		Intent intent = new Intent(this, AboutActivity.class);
		startActivity(intent);
		return true;
	}

	/**
	 * This is invoked when the user chose the donate item.
	 * 
	 * @return Always true.
	 */
	private boolean onMakeDonation() {
		showConfirmationDialog(R.string.donate_title,
				mApplication.getString(R.string.donate_message),
				CONFIRM_ID_DONATE, null);
		return true;
	}

	/**
	 * Launch the default browser with a specified URL page.
	 * 
	 * @param urlResourceId
	 *            The URL resource id.
	 */
	private void startBrowserWithPage(int urlResourceId) {
		String url = mApplication.getString(urlResourceId);
		Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		try {
			startActivity(i);
		} catch (ActivityNotFoundException exception) {
		}
	}

	/**
	 * On this method is a confirmation dialog.
	 * 
	 * @param titleStringId
	 *            The resource string id used for the confirmation dialog title.
	 * @param message
	 *            The message used for the confirmation dialog text.
	 * @param confirmationId
	 *            The id used to be identified the confirmed case.
	 * @param anObject
	 *            This could be used to send from the object needed on the
	 *            confirmation.
	 */
	private void showConfirmationDialog(int titleStringId, String message,
			final int confirmationId, final Object anObject) {
		new AlertDialog.Builder(this)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(titleStringId)
				.setMessage(message)
				.setPositiveButton(R.string.yes,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								onConfirmation(true, confirmationId, anObject);
							}

						})
				.setNegativeButton(R.string.no,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								onConfirmation(false, confirmationId, anObject);
							}

						}).show();
	}

	/**
	 * This method is invoked by the each time when is accepted a confirmation
	 * dialog.
	 * 
	 * @param positive
	 *            True if the confirmation is positive.
	 * @param confirmationId
	 *            The confirmation ID to identify the case.
	 * @param anObject
	 *            An object send by the caller method.
	 */
	private void onConfirmation(boolean positive, int confirmationId,
			Object anObject) {
		if (positive && CONFIRM_ID_DONATE == confirmationId) {
			startBrowserWithPage(R.string.donate_url);
		}
	}

	@Override
	public void onStartScan() {
		mApplication.showProgressDialog(this, R.string.scan_for_applications);
	}

	@Override
	public void onEndScan() {
		mApplication.hideProgressDialog();
		checkApplicationList();
	}

	/**
	 * Check if the list of applications is available.
	 */
	private void checkApplicationList() {
		List<AppInfo> list = mApplication.getApplicationsList();
		if (list == null || list.isEmpty()) {
			mApplication.showExceptionMessage(this,
					getString(R.string.app_name),
					getString(R.string.no_application));
		} else {
			showApplicationList(list);
		}
	}

	/**
	 * Build and show a dialog with a list of available applications.
	 * 
	 * @param list
	 *            List of available applications.
	 */
	private void showApplicationList(List<AppInfo> list) {
		final ApplicationsListAdapter adapter = new ApplicationsListAdapter(
				this, list);
		adapter.setSelectedAppInfo(mApplication.getFileManager());
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.file_manager);
		builder.setNegativeButton(R.string.cancel,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

					}

				});
		builder.setAdapter(adapter, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				setAppInfo(adapter.getItem(which));
			}

		});
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	/**
	 * An application was chose from the list.
	 * 
	 * @param appInfo
	 *            Chose application.
	 */
	private void setAppInfo(AppInfo appInfo) {
		if (appInfo != null) {
			mApplication.setFileManager(appInfo.getPackageName());
		} else {
			mApplication.showExceptionMessage(this,
					getString(R.string.app_name),
					getString(R.string.no_application));
		}
		prepareTexts();
	}

	@Override
	public Context getContext() {
		return this;
	}

	@Override
	public void onButtonClicked(int buttonId) {

	}

	@Override
	public StorageInfoApplication getStorageInfoApplication() {
		return mApplication;
	}
}
