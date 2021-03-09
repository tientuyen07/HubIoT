package com.example.hubiottest.screens.common.dialogs;

import androidx.fragment.app.DialogFragment;

import com.example.hubiottest.MainActivity;
import com.example.hubiottest.common.dependencyinjection.ControllerCompositionRoot;

public class BaseDialog extends DialogFragment {
    private ControllerCompositionRoot mControllerCompositionRoot;

    protected ControllerCompositionRoot getControllerCompositionRoot() {
        if (mControllerCompositionRoot == null) {
            mControllerCompositionRoot = new ControllerCompositionRoot(
                    ((MainActivity) requireActivity()).getActivityCompositionRoot()
            );
        }
        return mControllerCompositionRoot;
    }
}
