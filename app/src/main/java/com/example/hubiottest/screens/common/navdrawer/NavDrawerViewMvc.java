package com.example.hubiottest.screens.common.navdrawer;

import android.widget.FrameLayout;

import com.example.hubiottest.screens.common.views.ObservableViewMvc;

public interface NavDrawerViewMvc extends ObservableViewMvc<NavDrawerViewMvc.Listener> {
    interface Listener {
        void onEspConfigClicked();
    }

    FrameLayout getFragmentFrame();

    boolean isDrawerOpen();

    void openDrawer();

    void closeDrawer();
}
