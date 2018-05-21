package com.colaorange.dailymoney.core.ui.helper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.context.ContextsActivity;

import java.util.List;

/**
 * @author dennis
 */
public class NavMenuAdapter extends BaseAdapter {
    private int TYPE_ITEM = 0;
    private int TYPE_HEADER = 1;
    private int TYPE_DIVIDER = 2;
    private ContextsActivity activity;

    private List<NavMenuObj> menuItems;
    private LayoutInflater infalInflater;

    public NavMenuAdapter(ContextsActivity activity, List<NavMenuObj> menuItems) {
        this.activity = activity;
        this.menuItems = menuItems;
        infalInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getItemViewType(int position) {
        NavMenuObj obj = (NavMenuObj) getItem(position);
        if (obj instanceof NavMenuHeader) {
            return TYPE_HEADER;
        } else if (obj instanceof NavMenuDivider) {
            return TYPE_DIVIDER;
        }
        return TYPE_ITEM;
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    public int getCount() {
        return menuItems.size();
    }

    @Override
    public Object getItem(int position) {
        return menuItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean isEnabled(int position) {
        return getItemViewType(position) == TYPE_ITEM;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        NavMenuObj obj = (NavMenuObj) getItem(position);
        int vt = getItemViewType(position);
        if (convertView == null) {
            if (vt == TYPE_HEADER) {
                convertView = infalInflater.inflate(R.layout.nav_menu_header, parent, false);
                convertView.setTag(new HeaderViewHolder(convertView));
            } else if (vt == TYPE_DIVIDER) {
                convertView = infalInflater.inflate(R.layout.nav_menu_divider, parent, false);
                convertView.setTag(new ItemViewHolder(convertView));
            } else {
                convertView = infalInflater.inflate(R.layout.nav_menu_item, parent, false);
                convertView.setTag(new ItemViewHolder(convertView));
            }
        }
        if (vt == TYPE_HEADER) {
            ((HeaderViewHolder) convertView.getTag()).bindValue((NavMenuHeader) obj);
        } else if (vt == TYPE_DIVIDER) {
            //nothing
        } else {
            ((ItemViewHolder) convertView.getTag()).bindValue((NavMenuItem) obj);
        }

        return convertView;
    }


    private class ItemViewHolder {
        View view;

        public ItemViewHolder(View view) {
            this.view = view;
        }

        public void bindValue(NavMenuItem item) {
            TextView vtext = view.findViewById(R.id.nav_menu_text);
            vtext.setText(item.label);
            ImageView vicon = view.findViewById(R.id.nav_menu_icon);
            if (item.getIcon() > 0) {
                vicon.setVisibility(View.VISIBLE);
                vicon.setImageResource(item.getIcon());
            } else {
                vicon.setVisibility(View.GONE);
                vicon.setImageDrawable(null);
            }
        }
    }

    private class HeaderViewHolder {
        View view;

        public HeaderViewHolder(View view) {
            this.view = view;
        }

        public void bindValue(NavMenuHeader header) {
            TextView vtext = view.findViewById(R.id.nav_menu_text);
            vtext.setText(header.label);
        }
    }

    public static abstract class NavMenuObj {

    }

    public static class NavMenuItem extends NavMenuObj {
        private String label;
        private int icon;
        private View.OnClickListener listener;

        public NavMenuItem(String label, View.OnClickListener listener) {
            this.label = label;
            this.listener = listener;
        }

        public NavMenuItem(String label, View.OnClickListener listener, int icon) {
            this.label = label;
            this.icon = icon;
            this.listener = listener;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public int getIcon() {
            return icon;
        }

        public void setIcon(int icon) {
            this.icon = icon;
        }

        public View.OnClickListener getListener() {
            return listener;
        }

        public void setListener(View.OnClickListener listener) {
            this.listener = listener;
        }
    }

    public static class NavMenuHeader extends NavMenuObj {

        private String label;

        public NavMenuHeader(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }
    }

    public static class NavMenuDivider extends NavMenuObj {

    }
}