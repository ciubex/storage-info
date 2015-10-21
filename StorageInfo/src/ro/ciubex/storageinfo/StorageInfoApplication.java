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
package ro.ciubex.storageinfo;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import ro.ciubex.storageinfo.activities.DialogButtonListener;
import ro.ciubex.storageinfo.activities.StorageActivity;
import ro.ciubex.storageinfo.model.AppInfo;
import ro.ciubex.storageinfo.model.MountVolume;
import ro.ciubex.storageinfo.util.Utils.MountService;
import android.app.AlertDialog;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This is main application.
 * 
 * @author Claudiu Ciobotariu
 * 
 */
public class StorageInfoApplication extends Application {
	private static final String TAG = StorageInfoApplication.class.getName();
	private static Context mContext;
	private SharedPreferences mSharedPreferences;
	private static int mVersionCode = -1;
	private static final String FIRST_TIME = "firstTime";
	private static final String SHOW_NOTIFICATION = "showNotification";
	private static final String ENABLE_NOTIFICATIONS = "enableNotifications";
	private static final String NOTIFICATION_TYPE = "notificationType";
	private static final String DISABLED_PATHS = "disabledPaths";
	private static final String DEBUGGING_ENABLED = "debuggingEnabled";
	private static final String FILE_MANAGER = "fileManager";
	private NotificationManager mNotificationManager;
	private Object mMountService;
	private List<MountVolume> mMountVolumes;
	private List<String> mMountVolumesPaths;
	private boolean mVolumeMounded;
	private String[] mDisabledPaths;
	private List<Integer> mNotifications;
	private static final int DEFAULT_NOTIFICATION_ID = 0;
	private List<AppInfo> mApplicationsList;
	private ProgressDialog mProgressDialog;

	public static final int NOTIFICATION_TYPE_STORAGE = 0;
	public static final int NOTIFICATION_TYPE_QUICK = 1;

	/**
	 * This method is invoked when the application is created.
	 * 
	 * @see android.app.Application#onCreate()
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "StorageInfoApplication started!");
		StorageInfoApplication.mContext = getApplicationContext();
		mSharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		mNotifications = new ArrayList<Integer>();
		mMountVolumesPaths = new ArrayList<String>();
		updateMountedVolumes();
	}

	/**
	 * Update mounted volumes.
	 */
	public void updateMountedVolumes() {
		mMountVolumes = MountService.getVolumeList(getMountService(), StorageInfoApplication.mContext);
		if (!mMountVolumesPaths.isEmpty()) {
			mMountVolumesPaths.clear();
		}
		mVolumeMounded = false;
		if (!mMountVolumes.isEmpty()) {
			String path;
			for (MountVolume mountVolume : mMountVolumes) {
				path = mountVolume.getPath();
				mMountVolumesPaths.add(path);
				if (!isDisabledPath(path)
						&& Environment.MEDIA_MOUNTED.equals(mountVolume
								.getVolumeState())) {
					mVolumeMounded = true;
				}
			}
		}
	}

	/**
	 * Check if currently are volume mounted. Only available volumes are
	 * counting.
	 * 
	 * @return True if the volume are mounted.
	 */
	public boolean haveVolumeMounded() {
		return mVolumeMounded;
	}

	/**
	 * Get the mount volumes paths list.
	 * 
	 * @return Mount volumes paths list.
	 */
	public List<String> getMountVolumesPaths() {
		return mMountVolumesPaths;
	}

	/**
	 * Get the mount volumes paths as a string array.
	 * 
	 * @return Mount volumes paths as a string array.
	 */
	public String[] getMountVolumesPathsArray() {
		int i = 0;
		int len = mMountVolumesPaths.size();
		String[] arr = new String[len];
		for (String path : mMountVolumesPaths) {
			arr[i++] = path;
		}
		return arr;
	}

	/**
	 * Get an array strings with disabled paths.
	 * 
	 * @return An array strings with disabled paths.
	 */
	public String[] getDisabledPaths() {
		if (mDisabledPaths == null) {
			Set<String> stringSet = mSharedPreferences.getStringSet(
					DISABLED_PATHS, new HashSet<String>());
			int len = stringSet.size();
			int i = 0;
			mDisabledPaths = new String[len];
			for (String path : stringSet) {
				mDisabledPaths[i++] = path;
			}
		}
		return mDisabledPaths;
	}

	/**
	 * Update disabled paths.
	 */
	public void updateDisabledPaths() {
		mDisabledPaths = null;
		getDisabledPaths();
	}

	/**
	 * Check if a path is marked as disabled.
	 * 
	 * @param path
	 *            Path to be verified.
	 * @return True, if the path is marked as disabled.
	 */
	public boolean isDisabledPath(String path) {
		String[] array = getDisabledPaths();
		for (String item : array) {
			if (item.equals(path)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check if the application is launched for the first time.
	 * 
	 * @return True if is the first time when the application is launched.
	 */
	public boolean isFirstTime() {
		String key = FIRST_TIME + getVersion();
		boolean result = mSharedPreferences.getBoolean(key, true);
		if (result) {
			saveBooleanSharedPreference(key, false);
		}
		return result;
	}

	/**
	 * Retrieve the application version code.
	 * 
	 * @return The application version code.
	 */
	public int getVersion() {
		if (mVersionCode == -1) {
			try {
				mVersionCode = getPackageManager().getPackageInfo(
						getPackageName(), 0).versionCode;
			} catch (NameNotFoundException e) {
			}
		}
		return mVersionCode;
	}

	/**
	 * Check if the notification is showed.
	 * 
	 * @return True if the notification is showed.
	 */
	public boolean isShowNotification() {
		return mSharedPreferences.getBoolean(SHOW_NOTIFICATION, false);
	}

	/**
	 * Set notification showing boolean state.
	 * 
	 * @param flag
	 *            True if the notification is showed.
	 */
	public void setShowNotification(boolean flag) {
		saveBooleanSharedPreference(SHOW_NOTIFICATION, flag);
	}

	/**
	 * Internal method to store a boolean shared preference.
	 * 
	 * @param key
	 *            The key of boolean shared preference.
	 * @param value
	 *            The boolean value to be stored.
	 */
	private void saveBooleanSharedPreference(String key, boolean value) {
		SharedPreferences.Editor editor = mSharedPreferences.edit();
		editor.putBoolean(key, value);
		editor.commit();
	}

	/**
	 * Check if the notification is enabled.
	 * 
	 * @return True if the notification is enabled.
	 */
	public boolean isEnableNotifications() {
		return mSharedPreferences.getBoolean(ENABLE_NOTIFICATIONS, true);
	}

	private NotificationManager getNotificationManager() {
		if (mNotificationManager == null) {
			mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		}
		return mNotificationManager;
	}

	/**
	 * Show the notification on the navigation bar.
	 */
	public void updateDefaultNotification() {
		NotificationManager notificationManager = getNotificationManager();
		if (notificationManager != null) {
			if (mVolumeMounded) {
				if (mNotifications.isEmpty()) {
					Intent intent = new Intent(
							Settings.ACTION_INTERNAL_STORAGE_SETTINGS);
					NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(
							this);

					PendingIntent pIntent = PendingIntent.getActivity(
							this.getBaseContext(), 0, intent, 0);

					notifBuilder
							.setContentTitle(
									this.getText(R.string.notification_title))
							.setContentText(
									this.getText(R.string.notification_message))
							.setSmallIcon(R.drawable.ic_launcher);

					notifBuilder.setContentIntent(pIntent);

					Notification notification = notifBuilder.build();
					notification.flags |= Notification.FLAG_NO_CLEAR;
					notificationManager.notify(DEFAULT_NOTIFICATION_ID,
							notification);
					mNotifications.add(DEFAULT_NOTIFICATION_ID);
				}
			} else {
				if (!mNotifications.isEmpty()) {
					hideAllNotifications();
				}
			}
		}
		setShowNotification(!mNotifications.isEmpty());
	}

	/**
	 * Hide the notification from the navigation bar.
	 */
	private void hideNotification(NotificationManager notificationManager,
			int id) {
		if (notificationManager != null) {
			notificationManager.cancel(id);
		}
	}

	/**
	 * Hide all notifications.
	 */
	public void hideAllNotifications() {
		if (mNotificationManager != null && !mNotifications.isEmpty()) {
			for (Integer id : mNotifications) {
				hideNotification(mNotificationManager, id);
			}
			mNotifications.clear();
		}
		setShowNotification(false);
	}

	/**
	 * Check if is an USB event.
	 * 
	 * @param intent
	 *            The intend with the action.
	 * @return True if is mounted an USB device, otherwise false.
	 */
	public boolean checkUsbEvent(Context context, Intent intent, String dataPath) {
		boolean result = false;
		if (intent != null) {
			String action = intent.getAction();
			String dataString = String.valueOf(intent.getDataString());
			showDebuggingMessage(context, action);
			if ("android.intent.action.MEDIA_MOUNTED".contains(action)) {
				if (isUsbDeviceConnected(context)
						&& dataString.startsWith("file:")) {
					result = checkMountVolume(dataPath);
					if (result) {
						launchFileManager(dataPath);
					}
				}
			} else if ("android.intent.action.MEDIA_UNMOUNTED".equals(action)) {
				result = checkMountVolume(dataPath);
			} else if ("android.intent.action.MEDIA_BAD_REMOVAL".equals(action)
					|| "android.intent.action.MEDIA_EJECT".equals(action)
					|| "android.intent.action.MEDIA_REMOVED".equals(action)
					|| "android.hardware.usb.action.USB_DEVICE_DETACHED"
							.equals(action)
					|| "android.hardware.usb.action.USB_ACCESSORY_DETACHED"
							.equals(action)
					|| "com.sonyericsson.hardware.action.USB_OTG_DEVICE_DISCONNECTED"
							.equals(action)) {
				if (isShowNotification()) {
					result = checkMountVolume(dataPath);
				}
			}

		}
		return result;
	}

	/**
	 * Obtain the mount volume for a specified storage id.
	 * 
	 * @param storageId
	 *            The storage id of the mount volume.
	 * @return The mount volume or null;
	 */
	public MountVolume getMountVolume(int storageId) {
		if (mMountVolumes != null) {
			for (MountVolume mountVolume : mMountVolumes) {
				if (mountVolume.getStorageId() == storageId) {
					return mountVolume;
				}
			}
		}
		return null;
	}

	/**
	 * Check if a path is on the mount volumes list.
	 * 
	 * @param path
	 *            Path to be check.
	 * @return True if the path is on the mount volumes list.
	 */
	private boolean checkMountVolume(String path) {
		boolean result = false;
		if (mMountVolumes != null) {
			for (MountVolume mountVolume : mMountVolumes) {
				if (mountVolume.getPath().equals(path)) {
					return true;
				}
			}
		}
		return result;
	}

	/**
	 * Get notification type.
	 * 
	 * @return Notification type: 0 - disabled; 1 - create shortcut to Storage
	 *         Settings; 2 - create a quick access shortcut.
	 */
	public int getNotificationType() {
		int type = NOTIFICATION_TYPE_STORAGE;
		if (isEnableNotifications()) {
			type = getIntPreference(NOTIFICATION_TYPE,
					NOTIFICATION_TYPE_STORAGE);
		}
		return type;
	}

	/**
	 * Retrieve an int value from the preferences.
	 * 
	 * @param key
	 *            The name of the preference to retrieve.
	 * @param defValue
	 *            Value to return if this preference does not exist.
	 * 
	 * @return Returns the preference value if it exists, or defValue.
	 */
	private int getIntPreference(String key, int defValue) {
		int value = defValue;
		String val = mSharedPreferences.getString(key, "" + defValue);
		try {
			value = Integer.parseInt(val);
		} catch (NumberFormatException e) {
			Log.e(TAG, "NumberFormatException: " + val, e);
		}
		return value;
	}

	/**
	 * Update quick notifications.
	 */
	public void updateQuickNotifications() {
		NotificationManager notificationManager = getNotificationManager();
		if (notificationManager != null) {
			String state;
			int storageId;
			List<Integer> notifList = new ArrayList<Integer>();
			if (mMountVolumes != null) {
				for (MountVolume mountVolume : mMountVolumes) {
					if (!isDisabledPath(mountVolume.getPath())
							&& mountVolume.isRemovable()
							&& !mountVolume.isPrimary()
							&& !mountVolume.isEmulated()) {
						state = mountVolume.getVolumeState();
						storageId = mountVolume.getStorageId();
						if (Environment.MEDIA_UNMOUNTED.equals(state)) {
							notifList.add(storageId);
							updateNotification(notificationManager,
									mountVolume, storageId, state);
						} else if (Environment.MEDIA_MOUNTED.equals(state)) {
							notifList.add(storageId);
							updateNotification(notificationManager,
									mountVolume, storageId, state);
						}
					}
				}
			}
			// hide unused notifications
			for (Integer id : mNotifications) {
				if (!notifList.contains(id)) {
					hideNotification(notificationManager, id);
				}
			}
			mNotifications = notifList;
		}
		setShowNotification(!mNotifications.isEmpty());
	}

	private void updateNotification(NotificationManager notificationManager,
			MountVolume mountVolume, int storageId, String state) {
		Intent intent = new Intent(this, StorageActivity.class);
		intent.putExtra("storageId", storageId);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_CLEAR_TASK);
		PendingIntent pIntent = PendingIntent.getActivity(
				this.getBaseContext(), storageId, intent, 0);
		NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(
				this);
		notifBuilder.setSmallIcon(R.drawable.ic_launcher);
		notifBuilder.setContentIntent(pIntent);
		String path = mountVolume.getPath();
		String text = path != null ? getString(
				R.string.notification_title_path, path)
				: getString(R.string.notification_message);

		int titleId = R.string.notification_title;
		if (path != null) {
			titleId = (Environment.MEDIA_MOUNTED.equals(state) ? R.string.quick_notification_unmount
					: R.string.quick_notification_mount);
		}
		notifBuilder.setContentTitle(getString(titleId));
		notifBuilder.setContentText(text);

		Notification notification = notifBuilder.build();
		notification.flags |= Notification.FLAG_NO_CLEAR;
		notificationManager.notify(storageId, notification);
	}

	/**
	 * Check if are USB devices connected to the device.
	 * 
	 * @return True if is an USB device connected.
	 */
	public boolean isUsbDeviceConnected(Context context) {
		UsbManager manager = (UsbManager) context
				.getSystemService(Context.USB_SERVICE);
		HashMap<String, UsbDevice> devices = manager.getDeviceList();
		if (devices != null && devices.size() > 0) {
			UsbDevice device;
			UsbInterface usbInterface;
			int i, count;
			for (Entry<String, UsbDevice> entry : devices.entrySet()) {
				device = entry.getValue();
				count = device.getInterfaceCount();
				for (i = 0; i < count; i++) {
					usbInterface = device.getInterface(i);
					if (UsbConstants.USB_CLASS_MASS_STORAGE == usbInterface
							.getInterfaceClass()) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Check if is enabled debugging support.
	 * 
	 * @return True if debugging is enabled.
	 */
	public boolean isDebuggingEnabled() {
		return mSharedPreferences.getBoolean(DEBUGGING_ENABLED, false);
	}

	/**
	 * Show a debugging message on the screen.
	 * 
	 * @param message
	 *            Message to be show on the screen.
	 */
	public void showDebuggingMessage(Context context, String message) {
		if (isDebuggingEnabled()) {
			showToastMessage(context,
					getString(R.string.debug_message, "" + message));
		}
	}

	/**
	 * Show a toast message.
	 * 
	 * @param context
	 *            The context for the message to be show.
	 * @param message
	 *            The message to be show.
	 */
	public void showToastMessage(Context context, String message) {
		Toast.makeText(context, message, Toast.LENGTH_LONG).show();
	}

	/**
	 * Obtain the Mount Service object.
	 * 
	 * @return The Mount Service.
	 */
	public Object getMountService() {
		if (mMountService == null) {
			mMountService = MountService.getService();
		}
		return mMountService;
	}

	/**
	 * Display an exception dialog message.
	 * 
	 * @param listener
	 *            The dialog parent listener.
	 * @param title
	 *            The dialog title text.
	 * @param message
	 *            The message to be displayed.
	 */
	public void showExceptionMessage(final DialogButtonListener listener,
			String title, String message) {
		showExceptionMessage(listener, title, message, false);
	}

	/**
	 * Display an exception dialog message.
	 * 
	 * @param listener
	 *            The dialog parent listener.
	 * @param title
	 *            The dialog title text.
	 * @param message
	 *            The message to be displayed.
	 * @param messageContainLink
	 *            A boolean flag which mark if the text contain links.
	 */
	public void showExceptionMessage(final DialogButtonListener listener,
			String title, String message, boolean messageContainLink) {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(
				listener.getContext());
		alertDialog.setIcon(android.R.drawable.ic_dialog_info);
		alertDialog.setTitle(title);
		if (messageContainLink) {
			ScrollView scrollView = new ScrollView(listener.getContext());
			SpannableString spanText = new SpannableString(message);
			Linkify.addLinks(spanText, Linkify.ALL);
			
			TextView textView = new TextView(listener.getContext());
			textView.setMovementMethod(LinkMovementMethod.getInstance());
			textView.setText(spanText);
			
			scrollView.setPadding(14, 2, 10, 12);
			scrollView.addView(textView);
			alertDialog.setView(scrollView);
		} else {
			alertDialog.setMessage(message);
		}
		alertDialog.setCancelable(false);
		alertDialog.setPositiveButton(R.string.ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int buttonId) {
						listener.onButtonClicked(buttonId);
						dialog.dismiss();
					}
				});

		AlertDialog alert = alertDialog.create();
		alert.show();
	}

	/**
	 * @return the fileManagers
	 */
	public List<AppInfo> getApplicationsList() {
		return mApplicationsList;
	}

	/**
	 * @param applicationsList
	 *            the fileManagers to set
	 */
	public void setApplicationsList(List<AppInfo> applicationsList) {
		this.mApplicationsList = applicationsList;
	}

	/**
	 * This will show a progress dialog using a context and a message ID from
	 * application string resources.
	 * 
	 * @param context
	 *            The context where should be displayed the progress dialog.
	 * @param messageId
	 *            The string resource id.
	 */
	public void showProgressDialog(Context context, int messageId) {
		hideProgressDialog();
		mProgressDialog = ProgressDialog.show(context,
				getString(R.string.please_wait), getString(messageId));
	}

	/**
	 * Method used to hide the progress dialog.
	 */
	public void hideProgressDialog() {
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
		}
		mProgressDialog = null;
	}

	/**
	 * Get file manager. If no file manager is set is returned "null" string.
	 * 
	 * @return File manager, default is the string "null".
	 */
	public String getFileManager() {
		return mSharedPreferences.getString(FILE_MANAGER, "null");
	}

	/**
	 * Set file manager.
	 * 
	 * @param fileManager
	 *            The file manager to set.
	 */
	public void setFileManager(String fileManager) {
		SharedPreferences.Editor editor = mSharedPreferences.edit();
		if ("null".equals(fileManager)) {
			editor.remove(FILE_MANAGER);
		} else {
			editor.putString(FILE_MANAGER, fileManager);
		}
		editor.commit();
	}

	/**
	 * Launch a file manager with the specified path.
	 * 
	 * @param path
	 *            The specified path to be open on the file manager.
	 */
	public void launchFileManager(String path) {
		String fileManager = getFileManager();
		if (!"null".equals(fileManager)) {
			try {
				final PackageManager pm = getPackageManager();
				Intent intent = pm.getLaunchIntentForPackage(fileManager);
				intent.addCategory(Intent.CATEGORY_LAUNCHER);
				intent.setType("*/*");
				intent.setData(Uri.fromFile(new File(path)));
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
						| Intent.FLAG_ACTIVITY_CLEAR_TASK);
				startActivity(intent);
			} catch (Exception e) {
				setFileManager("null");
				Log.e(TAG, "file manager: " + fileManager + " path: " + path, e);
			}
		}
	}

	/**
	 * Check for pro version.
	 * 
	 * @return True if pro version exist.
	 */
	public boolean isProPresent() {
		PackageManager pm = getPackageManager();
		boolean success = false;
		try {
			success = (PackageManager.SIGNATURE_MATCH == pm.checkSignatures(
					this.getPackageName(), "ro.ciubex.storageinfopro"));
			Log.d(TAG, "isProPresent: " + success);
		} catch (Exception e) {
			Log.e(TAG, "isProPresent: " + e.getMessage(), e);
		}
		return success;
	}
}
