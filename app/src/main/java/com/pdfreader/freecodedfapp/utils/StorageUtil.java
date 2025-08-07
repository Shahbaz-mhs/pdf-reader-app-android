package com.pdfreader.freecodedfapp.utils;

import android.content.Context;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.text.TextUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StorageUtil {
    private static final String EMULATED_STORAGE_TARGET = System.getenv("EMULATED_STORAGE_TARGET");
    private static final String EXTERNAL_STORAGE = System.getenv("EXTERNAL_STORAGE");
    private static final String[] KNOWN_PHYSICAL_PATHS = {"/storage/sdcard0", "/storage/sdcard1", "/storage/extsdcard", "/storage/sdcard0/external_sdcard", "/mnt/extsdcard", "/mnt/sdcard/external_sd", "/mnt/sdcard/ext_sd", "/mnt/external_sd", "/mnt/media_rw/sdcard1", "/removable/microsd", "/mnt/emmc", "/storage/external_SD", "/storage/ext_sd", "/storage/removable/sdcard1", "/data/sdext", "/data/sdext2", "/data/sdext3", "/data/sdext4", "/sdcard1", "/sdcard2", "/storage/microsd"};
    private static final String SECONDARY_STORAGES = System.getenv("SECONDARY_STORAGE");

    public static List<String> getStorageDirectories(Context context) {
        ArrayList arrayList = new ArrayList();
        if (!TextUtils.isEmpty(EMULATED_STORAGE_TARGET)) {
            arrayList.add(getEmulatedStorageTarget());
        } else {
            arrayList.addAll(getExternalStorage(context));
        }
        Collections.addAll(arrayList, getAllSecondaryStorages());
        File usbDrive = getUsbDrive();
        if (usbDrive != null && !arrayList.contains(usbDrive.getPath())) {
            arrayList.add(usbDrive.getPath());
        }
        if (Build.VERSION.SDK_INT >= 19 && isUsbDeviceConnected(context)) {
            arrayList.add("otg://");
        }
        return arrayList;
    }

    private static Set<String> getExternalStorage(Context context) {
        HashSet hashSet = new HashSet();
        if (Build.VERSION.SDK_INT >= 23) {
            for (File file : getExternalFilesDirs(context, (String) null)) {
                if (file != null) {
                    String absolutePath = file.getAbsolutePath();
                    hashSet.add(absolutePath.substring(0, absolutePath.indexOf("Android/data")));
                }
            }
        } else if (TextUtils.isEmpty(EXTERNAL_STORAGE)) {
            hashSet.addAll(getAvailablePhysicalPaths());
        } else {
            hashSet.add(EXTERNAL_STORAGE);
        }
        return hashSet;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:5:0x0023, code lost:
        if (android.text.TextUtils.isDigitsOnly(r0) != false) goto L_0x0028;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static String getEmulatedStorageTarget() {

        throw new UnsupportedOperationException("Method not decompiled: StorageUtil.getEmulatedStorageTarget():java.lang.String");
    }

    private static String[] getAllSecondaryStorages() {
        return !TextUtils.isEmpty(SECONDARY_STORAGES) ? SECONDARY_STORAGES.split(File.pathSeparator) : new String[0];
    }

    private static List<String> getAvailablePhysicalPaths() {
        ArrayList arrayList = new ArrayList();
        for (String str : KNOWN_PHYSICAL_PATHS) {
            if (new File(str).exists()) {
                arrayList.add(str);
            }
        }
        return arrayList;
    }

    private static File[] getExternalFilesDirs(Context context, String str) {
        if (Build.VERSION.SDK_INT >= 19) {
            return context.getExternalFilesDirs(str);
        }
        return new File[]{context.getExternalFilesDir(str)};
    }

    private static boolean isUsbDeviceConnected(Context context) {
        UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        return (usbManager != null ? usbManager.getDeviceList().size() : 0) != 0;
    }

    public static File getUsbDrive() {
        try {
            for (File file : new File("/storage").listFiles()) {
                if (file.exists() && file.getName().toLowerCase().contains("usb") && file.canExecute()) {
                    return file;
                }
            }
        } catch (Exception unused) {
        }
        File file2 = new File("/mnt/sdcard/usbStorage");
        if (file2.exists() && file2.canExecute()) {
            return file2;
        }
        File file3 = new File("/mnt/sdcard/usb_storage");
        if (!file3.exists() || !file3.canExecute()) {
            return null;
        }
        return file3;
    }
}
