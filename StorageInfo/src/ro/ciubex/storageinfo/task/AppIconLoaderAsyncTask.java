/**
 * This file is part of StorageInfo application.
 * 
 * Copyright (C) 2015 Claudiu Ciobotariu
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

import java.lang.ref.WeakReference;

import ro.ciubex.storageinfo.model.AppInfo;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.widget.ImageView;

/**
 * This an asynchronous task to load applications icons.
 * 
 * @author Claudiu Ciobotariu
 * 
 */
public class AppIconLoaderAsyncTask extends AsyncTask<Void, Void, Drawable> {

	private final WeakReference<ImageView> mImageViewReference;
	private PackageManager mPackageManager;
	private AppInfo mAppInfo;
	private Drawable mDefaultIcon;
	private int mIconWidth;
	private int mIconHeight;

	public AppIconLoaderAsyncTask(ImageView imageView,
			PackageManager packageManager, AppInfo appInfo,
			Drawable defaultIcon, int iconWidth, int iconHeight) {
		super();
		this.mImageViewReference = new WeakReference<ImageView>(imageView);
		this.mPackageManager = packageManager;
		this.mAppInfo = appInfo;
		this.mDefaultIcon = defaultIcon;
		this.mIconWidth = iconWidth;
		this.mIconHeight = iconHeight;
	}

	/**
	 * Method invoked on the background thread.
	 */
	@Override
	protected Drawable doInBackground(Void... params) {
		return loadAppIconBitmap();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.AsyncTask#onPreExecute()
	 */
	@Override
	protected void onPreExecute() {
		if (mImageViewReference != null && mDefaultIcon != null) {
			ImageView imageView = mImageViewReference.get();
			if (imageView != null) {
				imageView.setImageDrawable(mDefaultIcon);
			}
		}
		super.onPreExecute();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
	 */
	@Override
	protected void onPostExecute(Drawable drawable) {
		if (isCancelled()) {
			drawable = mDefaultIcon;
		}
		if (mImageViewReference != null && drawable != null) {
			ImageView imageView = mImageViewReference.get();
			if (imageView != null) {
				imageView.setImageDrawable(drawable);
			}
		}
		super.onPostExecute(drawable);
	}

	/**
	 * Load an application icon or return default icon from resource.
	 * 
	 * @return A drawable icon.
	 */
	private Drawable loadAppIconBitmap() {
		Drawable drawable = mDefaultIcon;
		try {
			ApplicationInfo packageInfo = mPackageManager.getApplicationInfo(
					mAppInfo.getPackageName(), 0);
			drawable = packageInfo.loadIcon(mPackageManager);
			drawable.setBounds(0, 0, mIconWidth, mIconHeight);
		} catch (NameNotFoundException e) {
		}
		return drawable;
	}

}
