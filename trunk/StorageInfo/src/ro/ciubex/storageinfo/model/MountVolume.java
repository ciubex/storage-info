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

import java.io.File;

import android.content.Context;

/**
 * This class is a lighter copy of StorageVolume.
 * 
 * @author Claudiu Ciobotariu
 * 
 */
public class MountVolume {
	private int mStorageId;
	private int mDescriptionId;
	private File mPath;
	private boolean mRemovable;
	private boolean mPrimary;

	/**
	 * Returns the MTP storage ID for the volume.
	 * 
	 * @return MTP storage ID
	 */
	public int getStorageId() {
		return mStorageId;
	}

	/**
	 * The MTP storage ID for the volume.
	 * 
	 * @param storageId
	 *            MTP storage ID
	 */
	public void setStorageId(int storageId) {
		this.mStorageId = storageId;
	}

	/**
	 * Returns a user visible description of the volume.
	 * 
	 * @param context
	 *            Current application context.
	 * @return The volume description.
	 */
	public String getDescription(Context context) {
		return context.getResources().getString(mDescriptionId);
	}

	public int getDescriptionId() {
		return mDescriptionId;
	}

	public void setDescriptionId(int descriptionId) {
		this.mDescriptionId = descriptionId;
	}

	/**
	 * Returns the mount path for the volume.
	 * 
	 * @return The mount path.
	 */
	public String getPath() {
		return mPath.toString();
	}

	public File getPathFile() {
		return mPath;
	}

	public void setPathFile(File path) {
		this.mPath = path;
	}

	/**
	 * Returns true if the volume is removable.
	 * 
	 * @return Is removable.
	 */
	public boolean isRemovable() {
		return mRemovable;
	}

	/**
	 * Set volume removable flag.
	 * 
	 * @param removable
	 *            Volume removable flag to set.
	 */
	public void setRemovable(boolean removable) {
		this.mRemovable = removable;
	}

	public boolean isPrimary() {
		return mPrimary;
	}

	public void setPrimary(boolean primary) {
		this.mPrimary = primary;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder("MountVolume [");
		builder.append("mStorageId=").append(mStorageId)
				.append(", mDescriptionId=").append(mDescriptionId)
				.append(", mPath=").append(mPath).append(", mRemovable=")
				.append(mRemovable).append(", mPrimary=").append(mPrimary)
				.append("]");
		return builder.toString();
	}

}
