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

import java.util.HashMap;
import java.util.Map.Entry;

import ro.ciubex.storageinfo.activities.DialogButtonListener;
import ro.ciubex.storageinfo.activities.StorageActivity;
import ro.ciubex.storageinfo.util.Utils.MountService;
import android.app.AlertDialog;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

/**
 * This is main application.
 * 
 * @author Claudiu Ciobotariu
 * 
 */
public class StorageInfoApplication extends Application {
	private static final String TAG = StorageInfoApplication.class.getName();
	private SharedPreferences mSharedPreferences;
	private static final String SHOW_NOTIFICATION = "showNotification";
	private static final String ENABLE_NOTIFICATIONS = "enableNotifications";
	private static final String ENABLE_STORAGE_INFO = "enableStorageInfo";
	private static final String ENABLE_QUICK_STORAGE_ACCESS = "enableQuickStorageAccess";
	private static final String DEBUGGING_ENABLED = "debuggingEnabled";
	private static final int NOTIFICATION_ID = 0;
	private NotificationManager mNotificationManager;
	private NotificationCompat.Builder mNotifBuilder;
	private Object mMountService;
	private String mStoragePath;

	public enum STORAGE_STATE {
		OTHER, MOUNTED, UNMOUNTED
	};

	private STORAGE_STATE mStorageState;

	/**
	 * This method is invoked when the application is created.
	 * 
	 * @see android.app.Application#onCreate()
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "StorageInfoApplication started!");
		mSharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		mStorageState = STORAGE_STATE.OTHER;
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
		SharedPreferences.Editor editor = mSharedPreferences.edit();
		editor.putBoolean(SHOW_NOTIFICATION, flag);
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

	/**
	 * Check if the storage info is enabled.
	 * 
	 * @return True if the storage info is enabled.
	 */
	public boolean isEnableStorageInfo() {
		return mSharedPreferences.getBoolean(ENABLE_STORAGE_INFO, true);
	}

	/**
	 * Show the notification on the navigation bar.
	 */
	public void showNotification() {
		Intent intent = null;
		if (isEnabledQuickStorageAccess()) {
			intent = new Intent(this, StorageActivity.class);
		} else {
			intent = new Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS);
		}

		PendingIntent pIntent = PendingIntent.getActivity(
				this.getBaseContext(), 0, intent, 0);

		if (mNotificationManager == null) {
			mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		}
		if (mNotifBuilder == null) {
			mNotifBuilder = new NotificationCompat.Builder(this);
		}

		mNotifBuilder
				.setContentTitle(this.getText(R.string.notification_title))
				.setContentText(this.getText(R.string.notification_message))
				.setSmallIcon(R.drawable.ic_launcher);

		mNotifBuilder.setContentIntent(pIntent);

		Notification notification = mNotifBuilder.build();
		notification.flags |= Notification.FLAG_NO_CLEAR;
		mNotificationManager.notify(NOTIFICATION_ID, notification);
		setShowNotification(true);
	}

	/**
	 * Hide the notification from the navigation bar.
	 */
	public void hideNotification() {
		if (mNotificationManager != null) {
			mNotificationManager.cancel(NOTIFICATION_ID);
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
	public boolean checkUsbEvent(Context context, Intent intent) {
		boolean result = false;
		if (intent != null) {
			String action = intent.getAction();
			String dataString = String.valueOf(intent.getDataString());
			String dataPath = intent.getData() != null ? intent.getData()
					.getPath() : null;
			showDebuggingMessage(context, action);
			if ("android.intent.action.MEDIA_MOUNTED".contains(action)) {
				if (isUsbDeviceConnected(context)) {
					if (dataString.startsWith("file:") && dataPath != null) {
						mStoragePath = dataPath;
					}
					if (isEnableNotifications() && !isShowNotification()) {
						showNotification();
					}
				}
				result = true;
			} else if ("android.intent.action.MEDIA_UNMOUNTED".equals(action)
					|| "android.intent.action.MEDIA_BAD_REMOVAL".equals(action)
					|| "android.intent.action.MEDIA_EJECT".equals(action)
					|| "android.intent.action.MEDIA_REMOVED".equals(action)
					|| "android.hardware.usb.action.USB_DEVICE_DETACHED"
							.equals(action)
					|| "android.hardware.usb.action.USB_ACCESSORY_DETACHED"
							.equals(action)
					|| "com.sonyericsson.hardware.action.USB_OTG_DEVICE_DISCONNECTED"
							.equals(action)) {
				if (!isUsbDeviceConnected(context) && isShowNotification()) {
					if (mStoragePath != null && mStoragePath.equals(dataPath)) {
						mStoragePath = null;
					}
					hideNotification();
				}
			}
			updateStorageState();
			updateNotificationText();
		}
		return result;
	}

	/**
	 * Update storage state.
	 */
	private void updateStorageState() {
		if (mStoragePath != null) {
			String state = MountService.getVolumeState(getMountService(),
					mStoragePath);
			if (Environment.MEDIA_MOUNTED.equals(state)) {
				mStorageState = STORAGE_STATE.MOUNTED;
			} else if (Environment.MEDIA_UNMOUNTED.equals(state)) {
				mStorageState = STORAGE_STATE.UNMOUNTED;
			}
		}
	}

	/**
	 * Update the notification text.
	 */
	private void updateNotificationText() {
		if (mNotifBuilder != null && mNotificationManager != null
				&& isShowNotification() && isEnabledQuickStorageAccess()) {
			mNotifBuilder
					.setContentText(this
							.getText(mStorageState == STORAGE_STATE.MOUNTED ? R.string.quick_notification_unmount
									: R.string.quick_notification_mount));

			Notification notification = mNotifBuilder.build();
			notification.flags |= Notification.FLAG_NO_CLEAR;
			mNotificationManager.notify(NOTIFICATION_ID, notification);
		}
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
	 * Check if is enabled quick storage access to quick mount or unmount the
	 * USB storage.
	 * 
	 * @return True if the quick storage access is enabled.
	 */
	public boolean isEnabledQuickStorageAccess() {
		return mSharedPreferences
				.getBoolean(ENABLE_QUICK_STORAGE_ACCESS, false);
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
	 * Check if the USB storage is mounted.
	 * 
	 * @return True, if an USB storage is mounted.
	 */
	public boolean isStorageMounted() {
		return mStorageState == STORAGE_STATE.MOUNTED;
	}

	/**
	 * Obtain the storage state.
	 * 
	 * @return The storage state could be: other, mounted or unmounted.
	 */
	public STORAGE_STATE getStorageState() {
		return mStorageState;
	}

	/**
	 * Retrieve USB storage path.
	 * 
	 * @return The USB storage path.
	 */
	public String getStoragePath() {
		return mStoragePath;
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
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(
				listener.getContext(), R.style.AlertDialogCustom);
		alertDialog.setIcon(android.R.drawable.ic_dialog_info);
		alertDialog.setTitle(title);
		alertDialog.setMessage(message);
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
}
