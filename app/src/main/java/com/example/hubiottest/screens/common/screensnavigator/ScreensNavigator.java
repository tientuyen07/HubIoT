package com.example.hubiottest.screens.common.screensnavigator;
import com.example.hubiottest.screens.scanconfigesp.espdevicelist.EspListFragment;
import com.techyourchance.fragmenthelper.FragmentHelper;

public class ScreensNavigator {
    private static final String TAG = "ScreensNavigator";
    private final FragmentHelper mFragmentHelper;

    public ScreensNavigator(FragmentHelper fragmentHelper) {
        mFragmentHelper = fragmentHelper;
    }

    public void navigateBack() {
        mFragmentHelper.navigateBack();
    }

    public void navigateUp() {
        mFragmentHelper.navigateUp();
    }


    public void toEspListScreen() {
        mFragmentHelper.replaceFragment(EspListFragment.newInstance());
    }
}
