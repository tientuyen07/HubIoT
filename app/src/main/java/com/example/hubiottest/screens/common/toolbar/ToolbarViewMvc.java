package com.example.hubiottest.screens.common.toolbar;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.hubiottest.R;
import com.example.hubiottest.screens.common.views.BaseViewMvc;

public class ToolbarViewMvc extends BaseViewMvc {
    private final String TAG = getClassName();

    public interface HamburgerClickListener {
        void onHamburgerClicked();
    }

    public interface NavigateUpClickListener {
        void onNavigateUpClicked();
    }

    public interface ScanButtonClickListener {
        void onScanButtonClicked();
    }

    public interface CancelMultiSelectedButton {
        void onButtonCancelMultiClicked();
    }


    public interface ButtonMenuClickListener {
        void onMenuClicked();
    }

    private TextView mTxtTitle;
    private final Button mBtnScan;

    private HamburgerClickListener mHamburgerClickListener;
    private NavigateUpClickListener mNavigateUpClickListener;
    private ScanButtonClickListener mScanButtonClickListener;

    public ToolbarViewMvc(LayoutInflater layoutInflater, ViewGroup parent) {
        setRootView(layoutInflater.inflate(R.layout.layout_toolbar, parent, false));

        mTxtTitle = findViewById(R.id.txt_toolbar_title);
        mBtnScan = findViewById(R.id.btn_scan);

        mBtnScan.setOnClickListener((v) -> {
            mBtnScan.setText(mBtnScan.getText().equals("Scan") ? "Stop" : "Scan");
            mScanButtonClickListener.onScanButtonClicked();
        });
    }

    public void setTitle(String title) {
        mTxtTitle.setText(title);
    }

    public void enableScanButtonAndListen(ScanButtonClickListener scanButtonClickListener) {
        mScanButtonClickListener = scanButtonClickListener;
        mBtnScan.setVisibility(View.VISIBLE);
    }

    public void disableScanButton() {
        mBtnScan.setVisibility(View.INVISIBLE);
    }

    public TextView getTitle() {
        return mTxtTitle;
    }

    public void setTitle(TextView textView) {
        mTxtTitle = textView;
    }

    public void setScanButton(boolean isScan) {
        mBtnScan.setText(isScan ? "Stop" : "Scan");
    }
}
