package com.example.hubiottest.screens.common.dialogs;

import com.example.hubiottest.common.BaseObservable;

public class DialogsEventBus extends BaseObservable<DialogsEventBus.Listener> {
    public interface Listener {
        void onDialogEvent(Object event, Object data);
    }

    public void postEvent(Object event, Object data) {
        for (Listener listener : getListeners()) {
            listener.onDialogEvent(event, data);
        }
    }
}
