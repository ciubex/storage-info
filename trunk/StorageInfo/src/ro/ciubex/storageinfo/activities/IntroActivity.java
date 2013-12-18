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
import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;

/**
 * This is main activity.
 * 
 * @author Claudiu Ciobotariu
 * 
 */
public class IntroActivity extends Activity {
	private StorageInfoApplication mApplication;

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
		setContentView(R.layout.activity_intro);
		Application application = getApplication();
		if (application instanceof StorageInfoApplication) {
			mApplication = (StorageInfoApplication) application;
		}
	}

	/**
	 * Called the activity to start interacting with the user.
	 */
	@Override
	protected void onResume() {
		super.onResume();
		if (mApplication.isUsbDeviceConnected(this)
				&& mApplication.isEnableStorageInfo()) {
			showStorageSettings();
		} else {
			showSettings();
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
	 * Show settings intent.
	 */
	private void showSettings() {
		Intent intent = new Intent(this, StorageInfoPreferences.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		finish();
	}

}
