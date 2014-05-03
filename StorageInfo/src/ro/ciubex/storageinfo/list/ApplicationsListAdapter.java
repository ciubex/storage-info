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
package ro.ciubex.storageinfo.list;

import java.util.List;

import ro.ciubex.storageinfo.R;
import ro.ciubex.storageinfo.model.AppInfo;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * @author Claudiu Ciobotariu
 * 
 */
public class ApplicationsListAdapter extends BaseAdapter {

	private List<AppInfo> mApplicationsList;
	private LayoutInflater mInflater;
	private String mSelectedAppInfo;

	public ApplicationsListAdapter(Context context,
			List<AppInfo> applicationsList) {
		this.mApplicationsList = applicationsList;
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.Adapter#getCount()
	 */
	@Override
	public int getCount() {
		return mApplicationsList != null ? mApplicationsList.size() : 0;
	}

	/**
	 * Retrieve the application info from the applications list from specified
	 * position. If the list is empty the null is returned
	 * 
	 * @param position
	 *            The position in list of applications for retrieve.
	 * @return The application info from specified position
	 */
	@Override
	public AppInfo getItem(int location) {
		return mApplicationsList != null ? mApplicationsList.get(location)
				: null;
	}

	/**
	 * Get the row id associated with the specified position in the list. In
	 * this case the position is also the id.
	 */
	@Override
	public long getItemId(int id) {
		return id;
	}

	/**
	 * @param selectedPackageName
	 *            the selectedPackageName to set
	 */
	public int getPositionPackageName(String selectedPackageName) {
		int i = 0;
		if (mApplicationsList != null) {
			for (AppInfo appInfo : mApplicationsList) {
				if (selectedPackageName != null
						&& selectedPackageName.equals(appInfo.getPackageName())) {
					return i;
				}
				i++;
			}
		}
		return -1;
	}

	/**
	 * Get a View that displays the data at the specified position in the data
	 * set.
	 * 
	 * @param position
	 *            The position of the item within the adapter's data set of the
	 *            item whose view we want.
	 * @param view
	 *            The old view to reuse, if possible.
	 * @param parent
	 *            The parent that this view will eventually be attached to.
	 * @return A View corresponding to the data at the specified position.
	 */
	@Override
	public View getView(int position, View view, ViewGroup parent) {
		ItemViewHolder viewHolder = null;
		if (view != null) {
			viewHolder = (ItemViewHolder) view.getTag();
		} else {
			view = mInflater.inflate(R.layout.list_item, null);
			viewHolder = new ItemViewHolder();
			viewHolder.imageView = (ImageView) view.findViewById(R.id.imageId);
			viewHolder.textView = (TextView) view.findViewById(R.id.textId);
			viewHolder.checkBox = (CheckBox) view.findViewById(R.id.checkId);
			view.setTag(viewHolder);
		}
		if (viewHolder != null) {
			AppInfo appInfo = getItem(position);
			if (appInfo != null) {
				viewHolder.textView.setText(appInfo.getName());
				Drawable icon = appInfo.getIcon();
				if (icon != null) {
					int size = viewHolder.textView.getHeight();
					icon.setBounds(0, 0, size, size);
					viewHolder.imageView.setImageDrawable(icon);
				}
				if (mSelectedAppInfo != null
						&& mSelectedAppInfo.equals(appInfo.getPackageName())) {
					viewHolder.checkBox.setChecked(true);
				} else {
					viewHolder.checkBox.setChecked(false);
				}
			}
		}
		return view;
	}

	/**
	 * View holder for properties list
	 * 
	 */
	static class ItemViewHolder {
		ImageView imageView;
		TextView textView;
		CheckBox checkBox;
	}

	public void setSelectedAppInfo(String selectedAppInfo) {
		mSelectedAppInfo = selectedAppInfo;
	}

}
