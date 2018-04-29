package com.colaorange.dailymoney.core.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.context.ContextsActivity;

import java.util.List;

/**
 * common utility class for regular_spinner
 *
 * @param <T>
 */
public abstract class RegularSpinnerAdapter<T> extends ArrayAdapter<T> {

    protected LayoutInflater inflater;

    protected int selectedBgColor;
    protected int selectedTexColor;

    abstract public ViewHolder<T> createViewHolder();

    public boolean isSelected(int position) {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return true;
    }

    public RegularSpinnerAdapter(@NonNull ContextsActivity context, List<T> items) {
        super(context, R.layout.regular_spinner, items);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        selectedBgColor = context.resolveThemeAttrResData(R.attr.appSecondaryLightColor);
        selectedTexColor = context.resolveThemeAttrResData(R.attr.appSecondaryTextColor);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getView(position, convertView, parent, false);
    }


    public View getView(int position, @Nullable View convertView,
                        @NonNull ViewGroup parent, boolean isDropdown) {
        ViewHolder<T> holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.regular_spinner, null);
            convertView.setTag(holder = createViewHolder());
        } else {
            holder = (ViewHolder<T>) convertView.getTag();
        }

        holder.bindViewValue(getItem(position), convertView, isDropdown, isDropdown && isSelected(position));

        return convertView;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView,
                                @NonNull ViewGroup parent) {
        return getView(position, convertView, parent, true);
    }


    static public abstract class ViewHolder<T> {
        protected ContextsActivity activity;
        protected RegularSpinnerAdapter adapter;

        public ViewHolder(RegularSpinnerAdapter adapter) {
            this.adapter = adapter;
            this.activity = (ContextsActivity)adapter.getContext();
        }

        abstract public void bindViewValue(T item, LinearLayout layout, TextView text, boolean isDropdown, boolean isSelected);

        public void bindViewValue(T item, View convertView, boolean isDropdown, boolean isSelected) {
            LinearLayout vlayout = convertView.findViewById(R.id.spinner_layout);
            TextView vtext = convertView.findViewById(R.id.spinner_text);
            if (isSelected) {
                //todo keep ripple
                vlayout.setBackgroundColor(adapter.selectedBgColor);
                vtext.setTextColor(adapter.selectedTexColor);
            }
            this.bindViewValue(item, vlayout, vtext, isDropdown, isSelected);
        }
    }
}