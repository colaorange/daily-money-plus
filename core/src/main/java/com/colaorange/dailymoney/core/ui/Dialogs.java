package com.colaorange.dailymoney.core.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.context.Contexts;
import com.colaorange.dailymoney.core.context.ContextsActivity;
import com.colaorange.dailymoney.core.util.I18N;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Utitls to help use handling guis opersation, make sure all api are using in GUI scope.
 *
 * @author dennis
 */
public class Dialogs {


    public static final int OK_BUTTON = AlertDialog.BUTTON_POSITIVE;
    public static final int CANCEL_BUTTON = AlertDialog.BUTTON_NEGATIVE;

    static public void showTextEditor(ContextsActivity activity, @Nullable String title,
                                      @Nullable String msg, int inputType, @Nullable String text, final OnFinishListener listener) {

        I18N i18n = Contexts.instance().getI18n();
        String okText = i18n.string(R.string.act_ok);
        String cancelText = Contexts.instance().getI18n().string(R.string.act_cancel);

        AlertDialog.Builder b = new AlertDialog.Builder(activity);
        b.setTitle(title);
        b.setMessage(msg);

        // Set up the input

        View view = ((LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.guis_text_input, null, false);

        final EditText input = view.findViewById(R.id.guis_input);

        input.setInputType(inputType);

        input.setText(text);

        b.setView(view);

        // Set up the buttons
        b.setPositiveButton(okText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String title = input.getText().toString();
                listener.onFinish(which, title);
            }
        });
        b.setNegativeButton(cancelText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                listener.onFinish(which, null);
            }
        });

        b.show();
    }

    static public void showSelectionList(final ContextsActivity activity, @Nullable String title,
                                         @Nullable String msg, final List<Object> values, @Nullable final List<String> labels,
                                         final boolean multiple, @Nullable final Set<Object> selection, final OnFinishListener listener) {

        if (values.size() != labels.size()) {
            throw new IllegalArgumentException("values, labels size not equals " + values.size() + "!=" + labels.size());
        }

        I18N i18n = Contexts.instance().getI18n();
        String okText = i18n.string(R.string.act_ok);
        String cancelText = Contexts.instance().getI18n().string(R.string.act_cancel);

        AlertDialog.Builder b = new AlertDialog.Builder(activity);
        b.setTitle(title);
        b.setMessage(msg);

        // Set up the selection
        final LinkedHashSet newSelection = new LinkedHashSet();
        if (selection != null) {
            for (Object o : selection) {
                if (!multiple) {
                    newSelection.clear();
                }
                newSelection.add(o);
            }
        }

        float dpHeight = GUIs.getDPHeight(activity);
        float dpWidth = GUIs.getDPWidth(activity);

        final LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.guis_selection_list, null, false);
        ListView listView = view.findViewById(R.id.guis_list);

        //to prevent list is too height, that cause button disappear, we limit it height;
        ViewGroup.LayoutParams lp = listView.getLayoutParams();
        lp = new LinearLayout.LayoutParams(lp.width, GUIs.dp2Pixel(activity, dpHeight * 0.6f));
        listView.setLayoutParams(lp);//need to set it back


        SelectionListAdapter adapter = new SelectionListAdapter(activity, values, labels, multiple, newSelection);
        listView.setAdapter(adapter);
        adapter.bind(listView);


        b.setView(view);
        // Set up the buttons
        b.setPositiveButton(okText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listener.onFinish(which, newSelection);
            }
        });
        b.setNegativeButton(cancelText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                listener.onFinish(which, null);
            }
        });

        AlertDialog dialog = b.create();

        dialog.show();
//        Window w = dialog.getWindow();
//        w.setLayout(GUIs.dp2Pixel(activity, dpWidth * 0.9f), GUIs.dp2Pixel(activity, dpHeight * 0.75f));
    }

    public interface SupportIcon {
        int getIcon();
    }

    public static class SupportIconObject<T> implements SupportIcon {
        public final T obj;
        public final int icon;

        public SupportIconObject(T obj, int icon) {
            this.obj = obj;
            this.icon = icon;
        }

        @Override
        public int getIcon() {
            return icon;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            SupportIconObject that = (SupportIconObject) o;

            return obj != null ? obj.equals(that.obj) : that.obj == null;
        }

        @Override
        public int hashCode() {
            return obj != null ? obj.hashCode() : 0;
        }
    }

    private static class SelectionListAdapter extends BaseAdapter {

        final private List<?> values;
        final private List<String> labels;
        final private boolean multiple;
        final private Set<Object> selection;
        final private LayoutInflater inflater;
        final private ContextsActivity activity;
        private ListView listView;

        public SelectionListAdapter(ContextsActivity activity, List<Object> values, List<String> labels, boolean multiple, Set<Object> selection) {
            this.values = values;
            this.labels = labels;
            this.multiple = multiple;
            this.selection = selection;
            this.inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            this.activity = activity;
        }

        public void bind(ListView listView) {
            this.listView = listView;
        }

        @Override
        public int getCount() {
            return values.size();
        }

        @Override
        public Object getItem(int position) {
            return values.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        int getViewPosition(View view) {
            return listView == null ? -1 : listView.getPositionForView(view);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                ViewHolder vh = new ViewHolder(convertView = inflater.inflate(R.layout.guis_selection_list_item, parent, false));
                convertView.setTag(vh);
                convertView.setBackgroundResource(activity.getSelectableBackgroundId());
                View select = convertView.findViewById(R.id.guis_layout_select);
                CheckBox checkbox = (CheckBox) convertView.findViewById(R.id.guis_check);

                final View v = convertView;

                View.OnClickListener l = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Object obj = getItem(getViewPosition(v));
                        if (!selection.contains(obj)) {
                            if (!multiple) {
                                selection.clear();
                            }
                            selection.add(obj);
                        } else {
                            selection.remove(obj);
                        }
                        notifyDataSetChanged();
                    }
                };

                checkbox.setOnClickListener(l);
                convertView.setOnClickListener(l);
            }
            Object obj = getItem(position);
            //bindView
            ViewHolder vh = (ViewHolder) convertView.getTag();
            vh.bindView(position, obj);

            return convertView;
        }


        private class ViewHolder {
            View itemView;

            public ViewHolder(View itemView) {
                this.itemView = itemView;
            }

            public void bindView(int position, Object obj) {

                View select = itemView.findViewById(R.id.guis_layout_select);
                TextView vtext = (TextView) itemView.findViewById(R.id.guis_text);
                ImageView vicon = (ImageView) itemView.findViewById(R.id.guis_icon);

                if (obj instanceof SupportIcon) {
                    vicon.setVisibility(View.VISIBLE);
                    int icon = ((SupportIcon) obj).getIcon();
                    if (icon > 0) {
                        vicon.setImageResource(icon);
                    } else {
                        vicon.setImageDrawable(null);
                    }
                } else {
                    vicon.setVisibility(View.GONE);
                }

                String label = labels.get(position);
                vtext.setText(label);

                CheckBox checkbox = (CheckBox) itemView.findViewById(R.id.guis_check);
                checkbox.setVisibility(multiple ? View.VISIBLE : View.GONE);

                boolean selected = selection.contains(obj);

                select.setSelected(selected);

                if (!multiple) {
                    if (selected) {
                        select.setBackgroundDrawable(activity.getSelectedBackground());
                    } else {
                        select.setBackgroundDrawable(null);
                    }
                }

                checkbox.setChecked(selected);
            }
        }
    }

    public interface OnFinishListener {
        boolean onFinish(int which, Object data);
    }
}
