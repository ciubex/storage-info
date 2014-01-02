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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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
	public static final String NEWLINE = System.getProperty("line.separator");
	private static Method METHOD_ServiceManager_getService;
	private static Method METHOD_IMountService_asInterface;
	private static Method METHOD_IMountService_getVolumeState;
	private static Method METHOD_IMountService_mountVolume;
	private static Method METHOD_IMountService_unmountVolume;
	private static Method METHOD_IMountService_getStorageUsers;
	private static Method METHOD_IMountService_isUsbMassStorageEnabled;

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
			for (Method method : clazz.getDeclaredMethods()) {
				if ("getVolumeState".equals(method.getName())) {
					METHOD_IMountService_getVolumeState = method;
				} else if ("mountVolume".equals(method.getName())) {
					METHOD_IMountService_mountVolume = method;
				} else if ("unmountVolume".equals(method.getName())) {
					METHOD_IMountService_unmountVolume = method;
				} else if ("getStorageUsers".equals(method.getName())) {
					METHOD_IMountService_getStorageUsers = method;
				} else if ("isUsbMassStorageEnabled".equals(method.getName())) {
					METHOD_IMountService_isUsbMassStorageEnabled = method;
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
	}
}
