package com.example.hubiottest.screens.common;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.example.hubiottest.screens.common.dialogs.promptdialog.PromptViewMvc;
import com.example.hubiottest.screens.common.dialogs.promptdialog.PromptViewMvcImpl;
import com.example.hubiottest.screens.common.navdrawer.NavDrawerHelper;
import com.example.hubiottest.screens.common.navdrawer.NavDrawerViewMvc;
import com.example.hubiottest.screens.common.navdrawer.NavDrawerViewMvcImpl;
import com.example.hubiottest.screens.common.toolbar.ToolbarViewMvc;
import com.example.hubiottest.screens.scanconfigesp.espdevicelist.EspListViewMvc;
import com.example.hubiottest.screens.scanconfigesp.espdevicelist.EspListViewMvcImpl;
import com.example.hubiottest.screens.scanconfigesp.espdevicelist.esplistitem.EspItemViewMvc;
import com.example.hubiottest.screens.scanconfigesp.espdevicelist.esplistitem.EspItemViewMvcImpl;

public class ViewMvcFactory {
    private final LayoutInflater mLayoutInflater;
    private final NavDrawerHelper mNavDrawerHelper;

    public ViewMvcFactory(LayoutInflater layoutInflater, NavDrawerHelper navDrawerHelper) {
        mLayoutInflater = layoutInflater;
        mNavDrawerHelper = navDrawerHelper;
    }

    public NavDrawerViewMvc getNavDrawerViewMvc(@Nullable ViewGroup parent) {
        return new NavDrawerViewMvcImpl(mLayoutInflater, parent);
    }

    public ToolbarViewMvc getToolbarViewMvc(ViewGroup parent) {
        return new ToolbarViewMvc(mLayoutInflater, parent);
    }

    public EspItemViewMvc getConfigEspListItemViewMvc(@Nullable ViewGroup parent) {
        return new EspItemViewMvcImpl(mLayoutInflater, parent);
    }

    public EspListViewMvc getEspListViewMvc(@Nullable ViewGroup parent) {
        return new EspListViewMvcImpl(mLayoutInflater, parent, this);
    }

    public PromptViewMvc getPromptViewMvc(@Nullable ViewGroup parent) {
        return new PromptViewMvcImpl(mLayoutInflater, parent);
    }


}
