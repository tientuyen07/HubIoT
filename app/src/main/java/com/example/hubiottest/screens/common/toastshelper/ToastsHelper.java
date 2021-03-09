package com.example.hubiottest.screens.common.toastshelper;

import android.content.Context;
import android.widget.Toast;

public class ToastsHelper {
    private Context mContext;

    public ToastsHelper(Context context) {
        mContext = context;
    }

    public void showToast(String msg) {
        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
    }
}
