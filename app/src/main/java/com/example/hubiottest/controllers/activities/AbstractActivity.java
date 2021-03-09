package com.example.hubiottest.controllers.activities;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hubiottest.common.dependencyinjection.ActivityCompositionRoot;
import com.example.hubiottest.common.dependencyinjection.ControllerCompositionRoot;
import com.example.hubiottest.screens.common.CustomApplication;

public class AbstractActivity extends AppCompatActivity {
    private ActivityCompositionRoot mActivityCompositionRoot;
    private ControllerCompositionRoot mControllerCompositionRoot;

    public ActivityCompositionRoot getActivityCompositionRoot() {
        if (mActivityCompositionRoot == null) {
            mActivityCompositionRoot = new ActivityCompositionRoot(
                    this
            );
        }
        return mActivityCompositionRoot;
    }

    protected ControllerCompositionRoot getControllerCompositionRoot() {
        if (mControllerCompositionRoot == null) {
            mControllerCompositionRoot = new ControllerCompositionRoot(getActivityCompositionRoot());
        }
        return mControllerCompositionRoot;
    }

/*    // TODO: maybe we need to preserve the state of the replaced fragments?
    @Override
    public void replaceFragment(Class<? extends Fragment> claz, boolean addToBackStack,
                                boolean clearBackStack, Bundle args) {

        if (clearBackStack) {
            // Remove all entries from back stack
            getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }


        if (isFragmentShown(claz)) {
            // The requested fragment is already shown - nothing to do
            // Log.v(TAG, "the fragment " + claz.getSimpleName() + " is already shown");
            return;
        }

        // Create new fragment
        Fragment newFragment;

        try {
            newFragment = claz.newInstance();
            if (args != null) newFragment.setArguments(args);
        } catch (InstantiationException e) {
            e.printStackTrace();
            return;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return;
        }

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        if (addToBackStack) {
            ft.addToBackStack(null);
        }

        // Change to a new fragment
        ft.replace(R.id.frame_content, newFragment, claz.getClass().getSimpleName());
        ft.commit();
    }

    *//**
     * Check whether a fragment of a specific class is currently shown
     * @param claz class of fragment to test. Null considered as "test no fragment shown"
     * @return true if fragment of the same class (or a superclass) is currently shown
     *//*
    private boolean isFragmentShown(Class<? extends Fragment> claz) {
        Fragment currFragment = getSupportFragmentManager().findFragmentById(R.id.frame_content);


        return (currFragment == null && claz == null) || (
                currFragment != null && claz.isInstance(currFragment));
    }*/

}
