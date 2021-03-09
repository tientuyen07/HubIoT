package com.example.hubiottest.common.dependencyinjection;
import androidx.fragment.app.FragmentActivity;

import com.example.hubiottest.screens.common.dialogs.DialogsEventBus;
import com.example.hubiottest.screens.common.screensnavigator.ScreensNavigator;
import com.techyourchance.dialoghelper.DialogHelper;
import com.techyourchance.fragmenthelper.FragmentContainerWrapper;
import com.techyourchance.fragmenthelper.FragmentHelper;

public class ActivityCompositionRoot {
    private final FragmentActivity mActivity;
    private DialogsEventBus mDialogsEventBus;

    public ActivityCompositionRoot(FragmentActivity activity) {
        mActivity = activity;
    }

    public ScreensNavigator getScreensNavigator() {
        return new ScreensNavigator(getFragmentHelper());
    }

    FragmentActivity getActivity() {
        return mActivity;
    }

    private FragmentHelper getFragmentHelper() {
        return new FragmentHelper(mActivity, getFragmentContainerWrapper(), mActivity.getSupportFragmentManager());
    }

    private FragmentContainerWrapper getFragmentContainerWrapper() {
        return (FragmentContainerWrapper) mActivity;
    }

    DialogsEventBus getDialogEventBus() {
        if (mDialogsEventBus == null) {
            mDialogsEventBus = new DialogsEventBus();
        }
        return mDialogsEventBus;
    }

    DialogHelper getDialogHelper() {
        return new DialogHelper(getActivity().getSupportFragmentManager());
    }

}
