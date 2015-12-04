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

import java.io.Closeable;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import ro.ciubex.storageinfo.model.AppInfo;
import ro.ciubex.storageinfo.model.MountVolume;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
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
	public static final String INVALID_STATE = "invalid_state";
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
	private static Method METHOD_StorageVolume_getDescription;
	private static Method METHOD_StorageVolume_getPathFile;
	private static Method METHOD_StorageVolume_getPath;
	private static Method METHOD_StorageVolume_isRemovable;
	private static Method METHOD_StorageVolume_isPrimary;
	private static Method METHOD_StorageVolume_isEmulated;
	private static Method METHOD_StorageVolume_getState;

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
				} else if ("getDescription".equals(methodName)) {
					METHOD_StorageVolume_getDescription = method;
				} else if ("getDescriptionId".equals(methodName)) {
					METHOD_StorageVolume_getDescriptionId = method;
				} else if ("getPath".equals(methodName)) {
					METHOD_StorageVolume_getPath = method;
				} else if ("getPathFile".equals(methodName)) {
					METHOD_StorageVolume_getPathFile = method;
				} else if ("isRemovable".equals(methodName)) {
					METHOD_StorageVolume_isRemovable = method;
				} else if ("isPrimary".equals(methodName)) {
					METHOD_StorageVolume_isPrimary = method;
				} else if ("isEmulated".equals(methodName)) {
					METHOD_StorageVolume_isEmulated = method;
				} else if ("getState".equals(methodName)) {
					METHOD_StorageVolume_getState = method;
				}
			}
		} catch (Exception e) {
			Log.wtf(TAG, e);
		}
	}

	private static Object invoke(Method method, Object receiver, Object... args) {
		try {
			if (method != null) {
				return method.invoke(receiver, args);
			}
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
		return null;
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
			String state = Utils.INVALID_STATE;
			try {
				state = (String) invoke(METHOD_IMountService_getVolumeState,
						mountService, mountPoint);
			} catch (Exception e) {
				Log.e(TAG, "getVolumeState(" + mountPoint + ")", e);
			}
			return state;
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

		public static String getStorageVolumeDescription(Object obj, Context context) {
			String result = null;
			if (METHOD_StorageVolume_getDescription != null) {
				switch (METHOD_StorageVolume_getDescription.getParameterTypes().length) {
					case 0: result = (String) invoke(METHOD_StorageVolume_getDescription, obj);
						break;
					case 1: result = (String) invoke(METHOD_StorageVolume_getDescription, obj, context);
						break;
				}
			}
			return result;
		}

		public static List<MountVolume> getVolumeList(Object mountService, Context context) {
			Object[] arr = null;
			if (METHOD_IMountService_getVolumeList != null) {
				switch (METHOD_IMountService_getVolumeList.getParameterTypes().length) {
					case 0: arr = (Object[]) invoke(METHOD_IMountService_getVolumeList, mountService);
						break;
					case 3: arr = (Object[]) invoke(METHOD_IMountService_getVolumeList, mountService, 0, "/", 0);
						break;
				}
			}
			return prepareMountVolumes(mountService, arr, context);
		}

		private static List<MountVolume> prepareMountVolumes(Object mountService, Object[] arr, Context context) {
			int len = arr != null ? arr.length : 0;
			List<MountVolume> volumes = new ArrayList<MountVolume>();
			if (len > 0) {
				MountVolume volume;
				for (Object obj : arr) {
					volume = prepareMountVolume(mountService, obj, context);
					if (volume != null) {
						volumes.add(volume);
					}
				}
			} else {
				prepareVolumeStorages(volumes);
			}
			return volumes;
		}

		private static void prepareVolumeStorages(List<MountVolume> volumes) {
			String externalStorage = System.getenv("EXTERNAL_STORAGE");
			String secondaryStorage = System.getenv("SECONDARY_STORAGE");
			MountVolume volume;
			if (!isEmpty(externalStorage)) {
				volume = new MountVolume();
				volume.setPrimary(true);
				volume.setPathFile(new File(externalStorage));
				volumes.add(volume);
			}
			if (!isEmpty(secondaryStorage)) {
				for (String path : secondaryStorage.split(":")) {
					volume = new MountVolume();
					volume.setPathFile(new File(path));
					volumes.add(volume);
				}
			}
		}

		private static MountVolume prepareMountVolume(Object mountService,
				Object obj, Context context) {
			MountVolume volume = null;
			if ("android.os.storage.StorageVolume".equals(obj.getClass()
					.getName())) {
				try {
					volume = new MountVolume();
					volume.setStorageId((Integer) invoke(
							METHOD_StorageVolume_getStorageId, obj));
					if (METHOD_StorageVolume_getPathFile != null) {
						volume.setPathFile((File) invoke(
								METHOD_StorageVolume_getPathFile, obj));
					} else if (METHOD_StorageVolume_getPath != null) {
						String path = (String) invoke(
								METHOD_StorageVolume_getPath, obj);
						volume.setPathFile(new File(path));
					}
					if (METHOD_StorageVolume_isPrimary != null) {
						volume.setPrimary((Boolean) invoke(
								METHOD_StorageVolume_isPrimary, obj));
					}
					if (METHOD_StorageVolume_isEmulated != null) {
						volume.setEmulated((Boolean) invoke(
								METHOD_StorageVolume_isEmulated, obj));
					}
					volume.setRemovable((Boolean) invoke(
							METHOD_StorageVolume_isRemovable, obj));
					if (METHOD_StorageVolume_getDescriptionId != null) {
						volume.setDescriptionId((Integer) invoke(
								METHOD_StorageVolume_getDescriptionId, obj));
					} else if (METHOD_StorageVolume_getDescription != null) {
						volume.setDescription(getStorageVolumeDescription(obj, context));
					}
					if (METHOD_StorageVolume_getState != null) {
						volume.setVolumeState((String) invoke(METHOD_StorageVolume_getState, obj));
					} else if (METHOD_IMountService_getVolumeState != null) {
						volume.setVolumeState(getVolumeState(mountService,
								volume.getPath()));
					}
				} catch (Exception e) {
					Log.e(TAG, "Exception: " + e.getMessage() + " volume: " + volume, e);
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
				fileManager.setName(String.valueOf(packageManager
						.getApplicationLabel(packageInfo)));
				fileManager.setPackageName(packageInfo.packageName);
			} catch (NameNotFoundException e) {
				Log.e(TAG, e.getMessage(), e);
			}
		}
		return fileManager;
	}

	/**
	 * Join a list of string using a delimiter.
	 * 
	 * @param list
	 *            List of strings.
	 * @param delimiter
	 *            The elements delimiter.
	 * @return The joined list of strings.
	 */
	public static String join(List<String> list, String delimiter) {
		StringBuilder result = new StringBuilder();
		String delim = "";
		if (!list.isEmpty()) {
			for (String item : list) {
				result.append(delim).append(item);
				delim = delimiter;
			}
		}
		return result.toString();
	}

	/**
	 * Join a list of string using a delimiter.
	 * 
	 * @param array
	 *            List of strings.
	 * @param delimiter
	 *            The elements delimiter.
	 * @return The joined list of strings.
	 */
	public static String join(String[] array, String delimiter) {
		StringBuilder result = new StringBuilder();
		String delim = "";
		if (array != null && array.length > 0) {
			for (String item : array) {
				result.append(delim).append(item);
				delim = delimiter;
			}
		}
		return result.toString();
	}

	/**
	 * Returns true if the object is null or is empty.
	 *
	 * @param object The object to be examined.
	 * @return True if object is null or zero length.
	 */
	public static boolean isEmpty(Object object) {
		if (object instanceof CharSequence)
			return ((CharSequence) object).length() == 0;
		return object == null;
	}

	/**
	 * Close a closeable object.
	 *
	 * @param closeable Object to be close.
	 */
	public static void doClose(Object closeable) {
		if (closeable instanceof Closeable) {
			try {
				((Closeable) closeable).close();
			} catch (RuntimeException rethrown) {
				throw rethrown;
			} catch (Exception e) {
				Log.e(TAG, "doClose Exception: " + e.getMessage(), e);
			}
		} else if (closeable instanceof Cursor) {
			try {
				((Cursor) closeable).close();
			} catch (RuntimeException rethrown) {
				throw rethrown;
			} catch (Exception e) {
				Log.e(TAG, "doClose Exception: " + e.getMessage(), e);
			}
		}
	}
}
