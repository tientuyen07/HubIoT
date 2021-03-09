package com.example.hubiottest.common.dependencyinjection;

import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.view.LayoutInflater;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.example.hubiottest.modelandusecase.usecase.bluetoothusecase.BleScanListener;
import com.example.hubiottest.modelandusecase.usecase.bluetoothusecase.OnOffScanBluetoothUseCase;
import com.example.hubiottest.screens.common.ViewMvcFactory;
import com.example.hubiottest.screens.common.dialogs.DialogsEventBus;
import com.example.hubiottest.screens.common.dialogs.DialogsManager;
import com.example.hubiottest.screens.common.navdrawer.NavDrawerHelper;
import com.example.hubiottest.screens.common.permissions.PermissionsHelper;
import com.example.hubiottest.screens.common.screensnavigator.ScreensNavigator;
import com.example.hubiottest.screens.common.toastshelper.ToastsHelper;
import com.techyourchance.dialoghelper.DialogHelper;

public class ControllerCompositionRoot {
    private final ActivityCompositionRoot mActivityCompositionRoot;

    public ControllerCompositionRoot(ActivityCompositionRoot activityCompositionRoot) {
        mActivityCompositionRoot = activityCompositionRoot;
    }

    public FragmentActivity getActivity() {
        return mActivityCompositionRoot.getActivity();
    }

    private Context getContext() {
        return getActivity();
    }

    private LayoutInflater getLayoutInflater() {
        return LayoutInflater.from(getContext());
    }

    public ViewMvcFactory getViewMvcFactory() {
        return new ViewMvcFactory(getLayoutInflater(), getNavDrawerHelper());
    }

    private NavDrawerHelper getNavDrawerHelper() {
        return (NavDrawerHelper) getActivity();
    }

    public ToastsHelper getToastsHelper() {
        return new ToastsHelper(getContext());
    }

    public ScreensNavigator getScreensNavigator() {
        return mActivityCompositionRoot.getScreensNavigator();
    }


    public DialogsEventBus getDialogsEventBus() {
        return mActivityCompositionRoot.getDialogEventBus();
    }

    public DialogHelper getDialogHelper() {
        return mActivityCompositionRoot.getDialogHelper();
    }


    private BluetoothManager getBluetoothManager() {
        return (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
    }

    public OnOffScanBluetoothUseCase getControllerBluetooth(BleScanListener bleScanListener) {
        return new OnOffScanBluetoothUseCase(getBluetoothManager(), getActivity().getPackageManager(), bleScanListener);
    }

    public PermissionsHelper getPermissionsHelper() {
        return new PermissionsHelper(getActivity());
    }

    public DialogsManager getDialogsManager() {
        return new DialogsManager(getContext(), getFragmentManager());
    }

    public FragmentManager getFragmentManager() {
        return getActivity().getSupportFragmentManager();
    }
}
