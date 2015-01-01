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
package ro.ciubex.storageinfo.model;

import android.content.pm.ApplicationInfo;

/**
 * Application info model, used to show on the list of file managers.
 * 
 * @author Claudiu Ciobotariu
 * 
 */
public class AppInfo {
	private String mName;
	private String mPackageName;

	/**
	 * @return the name
	 */
	public String getName() {
		return mName;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.mName = name;
	}

	/**
	 * @return the packageName
	 */
	public String getPackageName() {
		return mPackageName;
	}

	/**
	 * @param packageName
	 *            the packageName to set
	 */
	public void setPackageName(String packageName) {
		this.mPackageName = packageName;
	}
}
