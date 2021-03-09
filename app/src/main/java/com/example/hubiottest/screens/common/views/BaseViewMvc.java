package com.example.hubiottest.screens.common.views;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;

import androidx.annotation.StringRes;

public class BaseViewMvc implements ViewMvc {

    private View mRootView;

    @Override
    public View getRootView() {
        return mRootView;
    }

    protected void setRootView(View rootView) {
        mRootView = rootView;
    }

    protected <T extends View> T findViewById(int id) {
        return getRootView().findViewById(id);
    }

    protected Context getContext(){
        return getRootView().getContext();
    }

    protected String getClassName() {
        return getClass().getSimpleName();
    }

    protected String getString(@StringRes int resId) {
        return getContext().getString(resId);
    }

    protected Resources getResources() {
        return getContext().getResources();
    }

}
