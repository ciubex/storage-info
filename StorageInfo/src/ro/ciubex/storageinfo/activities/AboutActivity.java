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
import android.app.Activity;
import android.os.Bundle;

/**
 * This is About activity.
 * 
 * @author Claudiu Ciobotariu
 * 
 */
public class AboutActivity extends Activity {
	/**
	 * Prepare About activity
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about_layout);
	}
}
