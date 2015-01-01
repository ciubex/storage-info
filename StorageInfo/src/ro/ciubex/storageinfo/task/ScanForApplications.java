/**
 * This file is part of StorageInfo application.
 * 
 * Copyright (C) 2014 Claudiu Ciobotariu
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
package ro.ciubex.storageinfo.task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ro.ciubex.storageinfo.StorageInfoApplication;
import ro.ciubex.storageinfo.model.AppInfo;
import ro.ciubex.storageinfo.model.AppInfoComparator;
import ro.ciubex.storageinfo.util.Utils;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;

/**
 * An asynchronous task used to scan for available applications.
 * 
 * @author Claudiu Ciobotariu
 * 
 */
public class ScanForApplications extends AsyncTask<Void, Void, Void> {

	public interface Listener {
		public StorageInfoApplication getStorageInfoApplication();

		public void onStartScan();

		public void onEndScan();
	}

	private Listener listener;

	public ScanForApplications(Listener listener) {
		this.listener = listener;
	}

	/**
	 * Method invoked on the background thread.
	 */
	@Override
	protected Void doInBackground(Void... params) {
		final StorageInfoApplication app = listener.getStorageInfoApplication();
		final PackageManager pm = app.getPackageManager();
		List<ApplicationInfo> packages = pm
				.getInstalledApplications(PackageManager.GET_META_DATA);
		List<AppInfo> list = app.getApplicationsList();
		List<AppInfo> newList = new ArrayList<AppInfo>();
		AppInfo fileManager;
		Intent intent;
		fileManager = new AppInfo();
		fileManager.setPackageName("null");
		fileManager.setName("Disabled");
		newList.add(fileManager);
		for (ApplicationInfo packageInfo : packages) {
			intent = pm.getLaunchIntentForPackage(packageInfo.packageName);
			if (intent != null) {
				fileManager = Utils.getFileManager(pm, packageInfo.packageName);
				if (fileManager != null) {
					newList.add(fileManager);
				}
			}
		}
		if (list == null || newList.size() != list.size()) {
			Collections.sort(newList, new AppInfoComparator());
			app.setApplicationsList(newList);
		}
		return null;
	}

	/**
	 * Method invoked on the UI thread before the task is executed.
	 */
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		listener.onStartScan();
	}

	/**
	 * Method invoked on the UI thread after the background computation
	 * finishes.
	 */
	@Override
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);
		listener.onEndScan();
	}

}
