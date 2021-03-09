package com.example.hubiottest.screens.scanconfigesp.espdevicelist;

import android.bluetooth.BluetoothDevice;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hubiottest.screens.common.ViewMvcFactory;
import com.example.hubiottest.screens.scanconfigesp.espdevicelist.esplistitem.EspItemViewMvc;

import java.util.ArrayList;
import java.util.List;

public class EspRecyclerAdapter extends RecyclerView.Adapter<EspRecyclerAdapter.EspViewHolder>
        implements EspItemViewMvc.Listener {
    interface Listener {
        void onEspClicked(BluetoothDevice bleDevice);
    }

    private List<BluetoothDevice> mBleDevices = new ArrayList<>();
    private Listener mListener;
    private ViewMvcFactory mViewMvcFactory;

    public EspRecyclerAdapter(Listener listener, ViewMvcFactory viewMvcFactory) {
        mListener = listener;
        mViewMvcFactory = viewMvcFactory;
    }

    public class EspViewHolder extends RecyclerView.ViewHolder {
        private final EspItemViewMvc mViewMvc;

        public EspViewHolder(@NonNull EspItemViewMvc viewMvc) {
            super(viewMvc.getRootView());
            mViewMvc = viewMvc;
        }
    }

    @NonNull
    @Override
    public EspViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        EspItemViewMvc mViewMvc = mViewMvcFactory.getConfigEspListItemViewMvc(parent);
        mViewMvc.registerListener(this);
        return new EspViewHolder(mViewMvc);
    }

    @Override
    public void onBindViewHolder(@NonNull EspViewHolder holder, int position) {
        holder.mViewMvc.bindEsp(mBleDevices.get(position));
    }

    @Override
    public void onEspClicked(BluetoothDevice device) {
        mListener.onEspClicked(device);
    }

    @Override
    public int getItemCount() {
        return mBleDevices.size();
    }

    public void bindEsps(List<BluetoothDevice> listEsp) {
        mBleDevices = new ArrayList<>(listEsp);
        notifyDataSetChanged();
    }

}
