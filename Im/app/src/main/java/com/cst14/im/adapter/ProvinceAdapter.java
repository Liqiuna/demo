package com.cst14.im.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.cst14.im.R;
import com.cst14.im.utils.AddressData;

import kankan.wheel.widget.adapters.AbstractWheelTextAdapter;


public class ProvinceAdapter extends AbstractWheelTextAdapter {
    // Countries names
    private String countries[] = AddressData.PROVINCES;
    /**
     * Constructor
     */
    public ProvinceAdapter(Context context) {
        super(context, R.layout.province_layout, NO_RESOURCE);

        setItemTextResource(R.id.province_name);
    }

    @Override
    public View getItem(int index, View cachedView, ViewGroup parent) {
        View view = super.getItem(index, cachedView, parent);
        return view;
    }

    @Override
    public int getItemsCount() {
        return countries.length;
    }

    @Override
    protected CharSequence getItemText(int index) {
        return countries[index];
    }
}
