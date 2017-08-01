package com.cst14.im.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Created by hz on 2016/8/25.
 */

public class ImageUtil {
    public static Bitmap bytes2Bitmap(byte[] data){
        if (data != null){
            return BitmapFactory.decodeByteArray(data,0,data.length);
        }
        return null;
    }
}
