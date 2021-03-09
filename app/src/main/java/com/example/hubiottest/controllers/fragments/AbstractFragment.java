package com.example.hubiottest.controllers.fragments;

import androidx.fragment.app.Fragment;

import com.example.hubiottest.MainActivity;
import com.example.hubiottest.common.dependencyinjection.ControllerCompositionRoot;

public class AbstractFragment extends Fragment {
    private ControllerCompositionRoot mControllerCompositionRoot;

    protected ControllerCompositionRoot getControllerCompositionRoot() {
        if (mControllerCompositionRoot == null) {
            mControllerCompositionRoot = new ControllerCompositionRoot(
                    ((MainActivity) requireActivity()).getActivityCompositionRoot());

        }
        return mControllerCompositionRoot;
    }

/*    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            mCallback = (AWSIoTFragmentCallback) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() +
                    "must implement AWSIoTFragmentCallback");
        }
    }

    @Nullable
    @Override
    public Fragment getHierarchicalParentFragment() {
        return FloorsListFragment.newInstance();
    }*/
}
