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

import java.util.Comparator;

/**
 * @author Claudiu Ciobotariu
 * 
 */
public class AppInfoComparator implements Comparator<AppInfo> {

	@Override
	public int compare(AppInfo app1, AppInfo app2) {
		String p1 = String.valueOf(app1.getPackageName());
		String p2 = String.valueOf(app2.getPackageName());
		String s1 = String.valueOf(app1.getName());
		String s2 = String.valueOf(app2.getName());
		int n1 = (!"null".equals(p1) && !"null".equals(s1)) ? s1.length() : 0;
		int n2 = (!"null".equals(p2) && !"null".equals(s2)) ? s2.length() : 0;
		int min = Math.min(n1, n2);
		for (int i = 0; i < min; i++) {
			char c1 = s1.charAt(i);
			char c2 = s2.charAt(i);
			if (c1 != c2) {
				c1 = Character.toUpperCase(c1);
				c2 = Character.toUpperCase(c2);
				if (c1 != c2) {
					c1 = Character.toLowerCase(c1);
					c2 = Character.toLowerCase(c2);
					if (c1 != c2) {
						return c1 - c2;
					}
				}
			}
		}
		return n1 - n2;
	}

}
