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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import ro.ciubex.storageinfo.R;
import ro.ciubex.storageinfo.StorageInfoApplication;
import ro.ciubex.storageinfo.list.ApplicationsListAdapter;
import ro.ciubex.storageinfo.model.AppInfo;
import ro.ciubex.storageinfo.provider.CachedFileProvider;
import ro.ciubex.storageinfo.task.ScanForApplications;
import ro.ciubex.storageinfo.util.Devices;
import ro.ciubex.storageinfo.util.Utils;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.provider.Settings;
import android.util.Log;

/**
 * This is settings preference activity.
 * 
 * @author Claudiu Ciobotariu
 * 
 */
public class StorageInfoPreferences extends PreferenceActivity implements
		ScanForApplications.Listener, DialogButtonListener,
		OnSharedPreferenceChangeListener {
	private static final String TAG = StorageInfoPreferences.class.getName();
	private StorageInfoApplication mApplication;
	private Preference mSetFileManager;
	private Preference mMakeDonation;
	private ListPreference mNotificationType;
	private MultiSelectListPreference mDisabledPaths;
	private Preference mSendDebugReport;
	private static final int ID_CONFIRMATION_ALERT = 0;
	private static final int CONFIRM_ID_DONATE = 1;
	private static final int ID_CONFIRMATION_DEBUG_REPORT = 2;
	private static final int REQUEST_SEND_REPORT = 1;
	private static final int BUFFER = 1024;

	/**
	 * Method called when this preference activity is created
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mApplication = (StorageInfoApplication) getApplication();
		addPreferencesFromResource(R.xml.application_preferences);
		prepareCommands();
		checkProVersion();
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
		mNotificationType = (ListPreference) findPreference("notificationType");
		mDisabledPaths = (MultiSelectListPreference) findPreference("disabledPaths");
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
						return onToggleNotification(false);
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
		mMakeDonation = (Preference) findPreference("makeDonation");
		mMakeDonation
				.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

					@Override
					public boolean onPreferenceClick(Preference preference) {
						return onMakeDonation();
					}
				});
		mSendDebugReport = (Preference) findPreference("sendDebugReport");
		mSendDebugReport.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				return onSendDebugReport();
			}
		});
	}

	/**
	 * Check if the pro version is present.
	 */
	private void checkProVersion() {
		if (mApplication.isProPresent()) {
			mMakeDonation.setEnabled(false);
			mMakeDonation.setTitle(R.string.thank_you_title);
			mMakeDonation.setSummary(R.string.thank_you_desc);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		getPreferenceScreen().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);
		prepareTexts();
		showFirstTimeMessage();
	}

	/**
	 * Check to show the first time message.
	 */
	private void showFirstTimeMessage() {
		if (mApplication.isFirstTime()) {
			if (mApplication.getSdkInt() != 14 || !"LT25i".equals(Build.MODEL)) {
				showConfirmationDialog(R.string.app_name,
						getString(R.string.first_time_message),
						ID_CONFIRMATION_ALERT,
						null);
			}
		}
	}

	/**
	 * Unregister the preference changes when the activity is on pause
	 */
	@Override
	protected void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(this);
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
			if (appInfo != null) { // maybe was uninstalled
				text = getString(R.string.file_manager_selected,
						appInfo.getName());
			} else {
				mApplication.setFileManager("null");
			}
		}
		String[] arr = mApplication.getMountVolumesPathsArray();
		if (arr.length > 0) {
			mDisabledPaths.setEntries(arr);
			mDisabledPaths.setEntryValues(arr);
		}
		arr = mApplication.getDisabledPaths();
		if (arr.length > 0) {
			mDisabledPaths.setSummary(getString(
					R.string.disabled_paths_desc_paths, Utils.join(arr, ", ")));
		} else {
			mDisabledPaths.setSummary(R.string.disabled_paths_desc);
		}
		int type = mApplication.getNotificationType();
		if (type == StorageInfoApplication.NOTIFICATION_TYPE_STORAGE
				|| type == StorageInfoApplication.NOTIFICATION_TYPE_QUICK) {
			arr = getResources()
					.getStringArray(R.array.notification_type_array);
			mNotificationType.setSummary(arr[type]);
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
		return true;
	}

	/**
	 * Toggle notification shortcut.
	 * 
	 * @return Always true.
	 */
	private boolean onToggleNotification(boolean preserveState) {
		if (mApplication.isEnableNotifications()) {
			if (!preserveState && mApplication.isShowNotification()) {
				mApplication.hideAllNotifications();
			} else {
				if (preserveState && mApplication.isShowNotification()) {
					mApplication.hideAllNotifications();
				}
				mApplication.updateMountedVolumes();
				int type = mApplication.getNotificationType();
				if (StorageInfoApplication.NOTIFICATION_TYPE_QUICK == type) {
					mApplication.updateQuickNotifications();
				} else if (StorageInfoApplication.NOTIFICATION_TYPE_STORAGE == type) {
					mApplication.updateDefaultNotification();
				}
			}
		} else if (mApplication.isShowNotification()) {
			mApplication.hideAllNotifications();
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
				mApplication.getString(R.string.donate_confirmation),
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
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
		alertDialog.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(titleStringId)
				.setMessage(message);

		if (confirmationId == ID_CONFIRMATION_ALERT) {
			alertDialog.setNeutralButton(R.string.ok, null);
		} else {
			alertDialog.setPositiveButton(R.string.yes,
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

							});
		}
		alertDialog.show();
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
		if (positive) {
			if (CONFIRM_ID_DONATE == confirmationId) {
				startBrowserWithPage(R.string.donate_url);
			} else if (ID_CONFIRMATION_DEBUG_REPORT == confirmationId) {
				confirmedSendReport(getString(R.string.send_debug_email_title));
			}
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

	/**
	 * Called when a shared preference is changed, added, or removed. This may
	 * be called even if a preference is set to its existing value.
	 * 
	 * <p>
	 * This callback will be run on your main thread.
	 * 
	 * @param sharedPreferences
	 *            The {@link SharedPreferences} that received the change.
	 * @param key
	 *            The key of the preference that was changed, added, or removed.
	 */
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (StorageInfoApplication.DISABLED_PATHS.equals(key)) {
			mApplication.updateDisabledPaths();
		} else if (StorageInfoApplication.ENABLE_NOTIFICATIONS.equals(key)) {
			onToggleNotification(true);
		} else if (StorageInfoApplication.NOTIFICATION_TYPE.equals(key)) {
			onToggleNotification(true);
		} else if (StorageInfoApplication.ALLOW_NOTIFICATIONS_DISMISS.equals(key)) {
			onToggleNotification(true);
		}
		prepareTexts();
	}

	/**
	 * Prepare the confirmation dialog for sending debugging reports.
	 */
	private boolean onSendDebugReport() {
		showConfirmationDialog(R.string.send_debug_title,
				getString(R.string.send_debug_confirmation),
				ID_CONFIRMATION_DEBUG_REPORT, null);
		return true;
	}


	/**
	 * User just confirmed to send a report.
	 */
	private void confirmedSendReport(String emailTitle) {
		mApplication.showProgressDialog(this, R.string.send_debug_title);
		String message = getString(R.string.report_body);
		File logsFolder = mApplication.getLogsFolder();
		File archive = getLogArchive(logsFolder);
		String[] TO = {"ciubex@yahoo.com"};

		Intent emailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
		emailIntent.setType("text/plain");
		emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
		emailIntent.putExtra(Intent.EXTRA_SUBJECT, emailTitle);
		emailIntent.putExtra(Intent.EXTRA_TEXT, message);
		emailIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);

		ArrayList<Uri> uris = new ArrayList<Uri>();
		if (archive != null && archive.exists() && archive.length() > 0) {
			uris.add(Uri.parse("content://" + CachedFileProvider.AUTHORITY
					+ "/" + archive.getName()));
		}
		if (!uris.isEmpty()) {
			emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
			emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
		}
		mApplication.hideProgressDialog();
		try {
			startActivityForResult(Intent.createChooser(emailIntent,
					getString(R.string.send_report)), REQUEST_SEND_REPORT);
		} catch (ActivityNotFoundException ex) {
			mApplication.logE(TAG,
					"confirmedSendReport Exception: " + ex.getMessage(), ex);
		}
	}

	/**
	 * Build the logs and call the archive creator.
	 *
	 * @param logsFolder The logs folder.
	 * @return The archive file which should contain the logs.
	 */
	private File getLogArchive(File logsFolder) {
		File logFile = mApplication.getLogFile();
		File logcatFile = getLogcatFile(logsFolder);
		Date now = new Date();
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss");
		String fileName = "StorageInfo_logs_" + format.format(now) + ".zip";
		return getArchives(new File[]{logFile, logcatFile}, logsFolder, fileName);
	}

	/**
	 * Method used to build a ZIP archive with log files.
	 *
	 * @param files       The log files to be added.
	 * @param logsFolder  The logs folder where should be added the archive name.
	 * @param archiveName The archive file name.
	 * @return The archive file.
	 */
	private File getArchives(File[] files, File logsFolder, String archiveName) {
		File archive = new File(logsFolder, archiveName);
		try {
			BufferedInputStream origin;
			FileOutputStream dest = new FileOutputStream(archive);
			ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
			byte data[] = new byte[BUFFER];
			File file;
			FileInputStream fi;
			ZipEntry entry;
			int count;
			for (int i = 0; i < files.length; i++) {
				file = files[i];
				if (file.exists() && file.length() > 0) {
					mApplication.logD(TAG, "Adding to archive: " + file.getName());
					fi = new FileInputStream(file);
					origin = new BufferedInputStream(fi, BUFFER);
					entry = new ZipEntry(file.getName());
					out.putNextEntry(entry);
					while ((count = origin.read(data, 0, BUFFER)) != -1) {
						out.write(data, 0, count);
					}
					Utils.doClose(entry);
					Utils.doClose(origin);
				}
			}
			Utils.doClose(out);
		} catch (FileNotFoundException e) {
			mApplication.logE(TAG, "getArchives failed: FileNotFoundException", e);
		} catch (IOException e) {
			mApplication.logE(TAG, "getArchives failed: IOException", e);
		}
		return archive;
	}

	/**
	 * Generate logs file on cache directory.
	 *
	 * @param cacheFolder Cache directory where are the logs.
	 * @return File with the logs.
	 */
	private File getLogcatFile(File cacheFolder) {
		File logFile = new File(cacheFolder, "StorageInfo_logcat.log");
		Process shell = null;
		InputStreamReader reader = null;
		FileWriter writer = null;
		char LS = '\n';
		char[] buffer = new char[BUFFER];
		String model = Build.MODEL;
		if (!model.startsWith(Build.MANUFACTURER)) {
			model = Build.MANUFACTURER + " " + model;
		}
		mApplication.logD(TAG, "Prepare Logs to be send via e-mail.");
		String oldCmd = "logcat -d -v threadtime ro.ciubex.storageinfo:v dalvikvm:v System.err:v *:s";
		String newCmd = "logcat -d -v threadtime";
		String command = newCmd;
		try {
			if (!logFile.exists()) {
				logFile.createNewFile();
			}
			if (mApplication.getSdkInt() <= 15) {
				command = oldCmd;
			}
			shell = Runtime.getRuntime().exec(command);
			reader = new InputStreamReader(shell.getInputStream());
			writer = new FileWriter(logFile);
			writer.write("Android version: " + Build.VERSION.SDK_INT +
					" (" + Build.VERSION.CODENAME + ")" + LS);
			writer.write("Device: " + model + LS);
			writer.write("Device name: " + Devices.getDeviceName() + LS);
			writer.write("App version: " + mApplication.getVersionName() +
					" (" + mApplication.getVersionCode() + ")" + LS);
			mApplication.writeSharedPreferences(writer);
			int n;
			do {
				n = reader.read(buffer, 0, BUFFER);
				if (n == -1) {
					break;
				}
				writer.write(buffer, 0, n);
			} while (true);
			shell.waitFor();
		} catch (IOException e) {
			mApplication.logE(TAG, "getLogcatFile failed: IOException", e);
		} catch (InterruptedException e) {
			mApplication.logE(TAG, "getLogcatFile failed: InterruptedException", e);
		} catch (Exception e) {
			mApplication.logE(TAG, "getLogcatFile failed: Exception", e);
		} finally {
			Utils.doClose(writer);
			Utils.doClose(reader);
			if (shell != null) {
				shell.destroy();
			}
		}
		return logFile;
	}
}
