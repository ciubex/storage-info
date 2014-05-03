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
package ro.ciubex.storageinfo.util;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import ro.ciubex.storageinfo.model.AppInfo;
import ro.ciubex.storageinfo.model.MountVolume;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.IBinder;
import android.util.AndroidRuntimeException;
import android.util.Log;

/**
 * @author Claudiu Ciobotariu
 * 
 */
public class Utils {
	static final String TAG = Utils.class.getName();
	static final String SERVICE_MOUNT = "mount";
	private static Method METHOD_ServiceManager_getService;
	private static Method METHOD_IMountService_asInterface;
	private static Method METHOD_IMountService_getVolumeList;
	private static Method METHOD_IMountService_getVolumeState;
	private static Method METHOD_IMountService_mountVolume;
	private static Method METHOD_IMountService_unmountVolume;
	private static Method METHOD_IMountService_getStorageUsers;
	private static Method METHOD_IMountService_isUsbMassStorageEnabled;

	private static Method METHOD_StorageVolume_getStorageId;
	private static Method METHOD_StorageVolume_getDescriptionId;
	private static Method METHOD_StorageVolume_getPathFile;
	private static Method METHOD_StorageVolume_isRemovable;
	private static Method METHOD_StorageVolume_isPrimary;

	static {
		try {
			Class<?> clazz;

			clazz = Class.forName("android.os.ServiceManager");
			METHOD_ServiceManager_getService = clazz.getMethod("getService",
					String.class);

			clazz = Class.forName("android.os.storage.IMountService$Stub");
			METHOD_IMountService_asInterface = clazz.getMethod("asInterface",
					IBinder.class);

			clazz = Class.forName("android.os.storage.IMountService");
			String methodName;
			for (Method method : clazz.getDeclaredMethods()) {
				methodName = method.getName();
				if ("getVolumeList".equals(methodName)) {
					METHOD_IMountService_getVolumeList = method;
				} else if ("getVolumeState".equals(methodName)) {
					METHOD_IMountService_getVolumeState = method;
				} else if ("mountVolume".equals(methodName)) {
					METHOD_IMountService_mountVolume = method;
				} else if ("unmountVolume".equals(methodName)) {
					METHOD_IMountService_unmountVolume = method;
				} else if ("getStorageUsers".equals(methodName)) {
					METHOD_IMountService_getStorageUsers = method;
				} else if ("isUsbMassStorageEnabled".equals(methodName)) {
					METHOD_IMountService_isUsbMassStorageEnabled = method;
				}
			}

			clazz = Class.forName("android.os.storage.StorageVolume");
			for (Method method : clazz.getDeclaredMethods()) {
				methodName = method.getName();
				if ("getStorageId".equals(methodName)) {
					METHOD_StorageVolume_getStorageId = method;
				} else if ("getDescriptionId".equals(methodName)) {
					METHOD_StorageVolume_getDescriptionId = method;
				} else if ("getPathFile".equals(methodName)) {
					METHOD_StorageVolume_getPathFile = method;
				} else if ("isRemovable".equals(methodName)) {
					METHOD_StorageVolume_isRemovable = method;
				} else if ("isPrimary".equals(methodName)) {
					METHOD_StorageVolume_isPrimary = method;
				}
			}
		} catch (Exception e) {
			Log.wtf(TAG, e);
		}
	}

	private static Object invoke(Method method, Object receiver, Object... args) {
		try {
			return method.invoke(receiver, args);
		} catch (IllegalArgumentException e) {
			Log.e(TAG, e.getMessage(), e);
			throw new AndroidRuntimeException(e);
		} catch (IllegalAccessException e) {
			Log.e(TAG, e.getMessage(), e);
			throw new AndroidRuntimeException(e);
		} catch (InvocationTargetException e) {
			Log.e(TAG, e.getMessage(), e);
			throw new AndroidRuntimeException(e);
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
			throw new AndroidRuntimeException(e);
		}
	}

	public static class MountService {

		public static Object getService() {
			Object service = invoke(METHOD_ServiceManager_getService, null,
					SERVICE_MOUNT);
			if (service != null) {
				return invoke(METHOD_IMountService_asInterface, null, service);
			}
			return null;
		}

		public static String getVolumeState(Object mountService,
				String mountPoint) {
			return (String) invoke(METHOD_IMountService_getVolumeState,
					mountService, mountPoint);
		}

		public static int mountVolume(Object mountService, String mountPoint) {
			return (Integer) invoke(METHOD_IMountService_mountVolume,
					mountService, mountPoint);
		}

		public static void unmountVolume(Object mountService,
				String mountPoint, boolean force) {
			if (METHOD_IMountService_unmountVolume != null) {
				switch (METHOD_IMountService_unmountVolume.getParameterTypes().length) {
				case 1:
					invoke(METHOD_IMountService_unmountVolume, mountService,
							mountPoint);
					break;
				case 2:
					invoke(METHOD_IMountService_unmountVolume, mountService,
							mountPoint, force);
					break;
				case 3:
					invoke(METHOD_IMountService_unmountVolume, mountService,
							mountPoint, force, force);
					break;
				}
			}
		}

		public static int[] getStorageUsers(Object mountService, String path) {
			return (int[]) invoke(METHOD_IMountService_getStorageUsers,
					mountService, path);
		}

		public static boolean isUsbMassStorageEnabled(Object mountService) {
			return (Boolean) invoke(
					METHOD_IMountService_isUsbMassStorageEnabled, mountService);
		}

		public static List<MountVolume> getVolumeList(Object mountService) {
			Object[] arr = (Object[]) invoke(
					METHOD_IMountService_getVolumeList, mountService);
			return prepareMountVolumes(arr);
		}

		private static List<MountVolume> prepareMountVolumes(Object[] arr) {
			int len = arr != null ? arr.length : 0;
			List<MountVolume> volumes = new ArrayList<MountVolume>();
			if (len > 0) {
				MountVolume volume;
				for (Object obj : arr) {
					volume = prepareMountVolume(obj);
					if (volume != null) {
						volumes.add(volume);
					}
				}
			}
			return volumes;
		}

		private static MountVolume prepareMountVolume(Object obj) {
			MountVolume volume = null;
			if ("android.os.storage.StorageVolume".equals(obj.getClass()
					.getName())) {
				try {
					volume = new MountVolume();
					volume.setStorageId((Integer) invoke(
							METHOD_StorageVolume_getStorageId, obj));
					volume.setDescriptionId((Integer) invoke(
							METHOD_StorageVolume_getDescriptionId, obj));
					volume.setPathFile((File) invoke(
							METHOD_StorageVolume_getPathFile, obj));
					volume.setPrimary((Boolean) invoke(
							METHOD_StorageVolume_isPrimary, obj));
					volume.setRemovable((Boolean) invoke(
							METHOD_StorageVolume_isRemovable, obj));
				} catch (Exception e) {
					Log.e(TAG, e.getMessage(), e);
					throw new AndroidRuntimeException(e);
				}
			}
			return volume;
		}
	}

	/**
	 * Populate file manager model with application info.
	 * 
	 * @param packageManager
	 *            PackageManager instance to find global package information.
	 * @param packageName
	 *            The full name (i.e. com.google.apps.contacts) of an
	 *            application.
	 * @return Populated file manager model.
	 */
	public static AppInfo getFileManager(PackageManager packageManager,
			String packageName) {
		AppInfo fileManager = null;
		if (packageManager != null) {
			try {
				ApplicationInfo packageInfo = packageManager
						.getApplicationInfo(packageName, 0);
				fileManager = new AppInfo();
				fileManager.setIcon(packageInfo.loadIcon(packageManager));
				fileManager.setName(String.valueOf(packageManager
						.getApplicationLabel(packageInfo)));
				fileManager.setPackageName(packageInfo.packageName);
			} catch (NameNotFoundException e) {
				Log.e(TAG, e.getMessage(), e);
			}
		}
		return fileManager;
	}
}
