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
import android.app.AlertDialog;
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
public class StorageInfoPreferences extends PreferenceActivity {
	private StorageInfoApplication application;
	private final int CONFIRM_ID_DONATE = 1;

	/**
	 * Method called when this preference activity is created
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		application = (StorageInfoApplication) getApplication();
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
		if (application.isShowNotification()) {
			application.hideNotification();
		} else {
			application.showNotification();
			if (application.isEnabledQuickStorageAccess()) {
				application.updateStorageState();
				application.updateNotificationText();
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
				application.getString(R.string.donate_message),
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
		String url = application.getString(urlResourceId);
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setData(Uri.parse(url));
		startActivity(i);
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
}
