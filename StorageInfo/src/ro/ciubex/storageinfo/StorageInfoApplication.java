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
import java.util.logging.Level;
import java.util.logging.Logger;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;

/**
 * This is main application.
 * 
 * @author Claudiu Ciobotariu
 * 
 */
public class StorageInfoApplication extends Application {
	private static Logger logger = Logger
			.getLogger(StorageInfoApplication.class.getName());
	private SharedPreferences sharedPreferences;
	private static final String SHOW_NOTIFICATION = "showNotification";
	private static final String ENABLE_NOTIFICATIONS = "enableNotifications";
	private static final String ENABLE_STORAGE_INFO = "enableStorageInfo";
	private static final int NOTIFICATION_ID = 0;

	/**
	 * This method is invoked when the application is created.
	 * 
	 * @see android.app.Application#onCreate()
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		logger.log(Level.INFO, "StorageInfoApplication started!");
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
	}

	/**
	 * Check if the notification is showed.
	 * 
	 * @return True if the notification is showed.
	 */
	public boolean isShowNotification() {
		return sharedPreferences.getBoolean(SHOW_NOTIFICATION, false);
	}

	/**
	 * Set notification showing boolean state.
	 * 
	 * @param flag
	 *            True if the notification is showed.
	 */
	public void setShowNotification(boolean flag) {
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putBoolean(SHOW_NOTIFICATION, flag);
		editor.commit();
	}

	/**
	 * Check if the notification is enabled.
	 * 
	 * @return True if the notification is enabled.
	 */
	public boolean isEnableNotifications() {
		return sharedPreferences.getBoolean(ENABLE_NOTIFICATIONS, true);
	}

	/**
	 * Check if the storage info is enabled.
	 * 
	 * @return True if the storage info is enabled.
	 */
	public boolean isEnableStorageInfo() {
		return sharedPreferences.getBoolean(ENABLE_STORAGE_INFO, true);
	}

	/**
	 * Show the notification on the navigation bar.
	 */
	public void showNotification() {
		Intent intent = new Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS);
		PendingIntent pIntent = PendingIntent.getActivity(
				this.getBaseContext(), 0, intent, 0);

		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(
				this);

		notifBuilder.setContentTitle(this.getText(R.string.notification_title))
				.setContentText(this.getText(R.string.notification_message))
				.setSmallIcon(R.drawable.ic_launcher);

		notifBuilder.setContentIntent(pIntent);

		Notification notification = notifBuilder.build();
		notification.flags |= Notification.FLAG_NO_CLEAR;
		mNotificationManager.notify(NOTIFICATION_ID, notification);
		setShowNotification(true);
	}

	/**
	 * Hide the notification from the navigation bar.
	 */
	public void hideNotification() {
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.cancel(NOTIFICATION_ID);
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
			if ("android.intent.action.MEDIA_MOUNTED".contains(action)) {
				if (isUsbDeviceConnected(context) && isEnableNotifications()
						&& !isShowNotification()) {
					showNotification();
				}
				result = true;
			} else if ("android.intent.action.MEDIA_UNMOUNTED".equals(action)
					|| "android.intent.action.MEDIA_BAD_REMOVAL".equals(action)
					|| "android.intent.action.MEDIA_EJECT".equals(action)
					|| "android.intent.action.MEDIA_REMOVED".equals(action)) {
				if (!isUsbDeviceConnected(context) && isShowNotification()) {
					hideNotification();
				}
			}
		}
		return result;
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
					if (UsbConstants.USB_CLASS_MASS_STORAGE == usbInterface.getInterfaceClass()) {
						return true;
					}
				}
			}
		}
		return false;
	}
}
