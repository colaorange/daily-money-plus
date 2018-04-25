package com.colaorange.commons.util;

import android.graphics.Color;

/**
 * Created by Dennis
 */
public class Colors {

    /**
     * @param color  the color rgb
     * @param factor 0-1f
     * @return
     */
    public static int darken(int color, float factor) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] = hsv[2] * factor;
        if (hsv[2] > 1f) {
            hsv[2] = 1f;
        }
        color = Color.HSVToColor(hsv);
        return color;
    }

    /**
     * @param color  the color rgb
     * @param factor 0-1f
     * @return
     */
    public static int lighten(int color, float factor) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] = 1.0f - factor * (1.0f - hsv[2]);
        if (hsv[2] < 0f) {
            hsv[2] = 0f;
        }
        color = Color.HSVToColor(hsv);
        return color;
    }
}
