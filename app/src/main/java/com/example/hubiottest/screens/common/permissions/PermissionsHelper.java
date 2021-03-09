package com.example.hubiottest.screens.common.permissions;

import android.app.Activity;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.hubiottest.common.BaseObservable;

public class PermissionsHelper extends BaseObservable<PermissionsHelper.Listener> {
    public interface Listener {
        void onPermissionGranted(String permission, int requestCode);

        void onPermissionDeclined(String permission, int requestCode);

        void onPermissionDeclinedDontAskAgain(String permission, int requestCode);
    }


    private final Activity mActivity;

    public PermissionsHelper(Activity activity) {
        mActivity = activity;
    }

    public boolean hasPermission(String permission) {
        return ContextCompat.checkSelfPermission(mActivity, permission) == PackageManager.PERMISSION_GRANTED;
    }

    public void requestPermission(String permission, int requestCode) {
        ActivityCompat.requestPermissions(mActivity, new String[]{permission}, requestCode);
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (permissions.length < 1) {
            throw new RuntimeException("no permissions on request result");
        }
        String permission = permissions[0];
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            notifyPermissionGranted(permission, requestCode);
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(mActivity, permission)) {
                notifyPermissionDeclined(permission, requestCode);
            } else {
                notifyPermissionDeclinedDontAskAgain(permission, requestCode);
            }
        }
    }

    private void notifyPermissionDeclinedDontAskAgain(String permission, int requestCode) {
        for (Listener listener : getListeners()) {
            listener.onPermissionDeclinedDontAskAgain(permission, requestCode);
        }
    }

    private void notifyPermissionDeclined(String permission, int requestCode) {
        for (Listener listener : getListeners()) {
            listener.onPermissionDeclined(permission, requestCode);
        }
    }

    private void notifyPermissionGranted(String permission, int requestCode) {
        for (Listener listener : getListeners()) {
            listener.onPermissionGranted(permission, requestCode);
        }
    }
}
