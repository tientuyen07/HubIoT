package com.example.hubiottest;

import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.hubiottest.controllers.activities.AbstractActivity;
import com.example.hubiottest.screens.common.navdrawer.NavDrawerHelper;
import com.example.hubiottest.screens.common.navdrawer.NavDrawerViewMvc;
import com.example.hubiottest.screens.common.screensnavigator.ScreensNavigator;
import com.techyourchance.fragmenthelper.FragmentContainerWrapper;

public class MainActivity extends AbstractActivity implements
        FragmentContainerWrapper,
        NavDrawerViewMvc.Listener,
        NavDrawerHelper{
    private ScreensNavigator mScreensNavigator;
    private NavDrawerViewMvc mViewMvc;

    // Show dialog when clicking notification
    private boolean isShowDialog = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mScreensNavigator = getControllerCompositionRoot().getScreensNavigator();
        mViewMvc = getControllerCompositionRoot().getViewMvcFactory().getNavDrawerViewMvc(null);
        setContentView(mViewMvc.getRootView());

        if (savedInstanceState == null) {
            mScreensNavigator.toEspListScreen();
        }

        // Show dialog when clicking notification
    }

    @Override
    protected void onStart() {
        super.onStart();
        mViewMvc.registerListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mViewMvc.unregisterListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("onDestroy", "Set flag");
    }

    @NonNull
    @Override
    public ViewGroup getFragmentContainer() {
        return mViewMvc.getFragmentFrame();
    }

    @Override
    public void onEspConfigClicked() {
//        Intent intent = new Intent(this, DeviceScanActivity.class);
//        startActivity(intent);
        mScreensNavigator.toEspListScreen();
    }

    @Override
    public void openDrawer() {
        mViewMvc.openDrawer();
    }

    @Override
    public void closeDrawer() {
        mViewMvc.closeDrawer();
    }

    @Override
    public boolean isDrawerOpen() {
        return mViewMvc.isDrawerOpen();
    }

}
