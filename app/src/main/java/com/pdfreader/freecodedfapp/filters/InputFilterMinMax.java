package com.pdfreader.freecodedfapp.filters;

import android.text.InputFilter;
import android.text.Spanned;

public class InputFilterMinMax implements InputFilter {
    private final int max;
    private final int min;

    public InputFilterMinMax(int i, int i2) {
        this.min = i;
        this.max = i2;
    }

    public InputFilterMinMax(String str, String str2) {
        this.min = Integer.parseInt(str);
        this.max = Integer.parseInt(str2);
    }

    /* JADX WARNING: Removed duplicated region for block: B:6:0x000e A[ORIG_RETURN, RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean isInRange(int r3, int r4, int r5) {

        throw new UnsupportedOperationException("Method not decompiled: InputFilterMinMax.isInRange(int, int, int):boolean");
    }

    public CharSequence filter(CharSequence charSequence, int i, int i2, Spanned spanned, int i3, int i4) {
        try {
            if (isInRange(this.min, this.max, Integer.parseInt(spanned.toString() + charSequence.toString()))) {
                return null;
            }
            return "";
        } catch (NumberFormatException unused) {
            return "";
        }
    }
}
