package com.colaorange.commons.util;

import android.graphics.Color;

/**
 * Created by Dennis
 */
public class Colors {

    /**
     * @param color  the color rgb
     * @param distance 0-1f
     * @return
     */
    public static int darken(int color, float distance) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] = hsv[2] - distance;
        if (hsv[2] < 0f) {
            hsv[2] = 0f;
        }
        color = Color.HSVToColor(hsv);
        return color;
    }

    /**
     * @param color  the color rgb
     * @param distance 0-1f
     * @return
     */
    public static int lighten(int color, float distance) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] = hsv[2] + distance;
        if (hsv[2] > 1f) {
            hsv[2] = 1f;
        }
        color = Color.HSVToColor(hsv);
        return color;
    }

    public static int saturation(int color, float s){
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] = s;
        if (hsv[2] < 0f) {
            hsv[2] = 0f;
        }else if (hsv[2] > 1f) {
            hsv[2] = 1f;
        }
        color = Color.HSVToColor(hsv);
        return color;
    }
}
