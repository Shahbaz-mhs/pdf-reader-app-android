package com.pdfreader.freecodedfapp.utils;

import com.tom_roush.fontbox.ttf.OS2WindowsMetricsTable;

import java.util.TreeMap;

public class NumberUtils {
    private static final TreeMap<Integer, String> map;

    static {
        TreeMap<Integer, String> treeMap = new TreeMap<>();
        map = treeMap;
        treeMap.put(1000, "m");
        map.put(Integer.valueOf(OS2WindowsMetricsTable.WEIGHT_CLASS_BLACK), "cm");
        map.put(Integer.valueOf(OS2WindowsMetricsTable.WEIGHT_CLASS_MEDIUM), "d");
        map.put(Integer.valueOf(OS2WindowsMetricsTable.WEIGHT_CLASS_NORMAL), "cd");
        map.put(100, "c");
        map.put(90, "xc");
        map.put(50, "l");
        map.put(40, "xl");
        map.put(10, "x");
        map.put(9, "ix");
        map.put(5, "v");
        map.put(4, "iv");
        map.put(1, "i");
    }

    public static final String toRoman(int i) {
        int intValue = map.floorKey(Integer.valueOf(i)).intValue();
        if (i == intValue) {
            return map.get(Integer.valueOf(i));
        }
        return map.get(Integer.valueOf(intValue)) + toRoman(i - intValue);
    }
}
