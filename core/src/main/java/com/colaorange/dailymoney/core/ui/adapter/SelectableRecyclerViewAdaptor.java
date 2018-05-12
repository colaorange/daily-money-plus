package com.colaorange.dailymoney.core.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;

import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.context.ContextsActivity;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by Dennis
 */
public abstract class SelectableRecyclerViewAdaptor<T, VH extends SelectableRecyclerViewAdaptor.SelectableViewHolder> extends RecyclerViewAdaptor<T, VH> {

    private Set<T> selectionSet = new LinkedHashSet<>();

    protected boolean multipleSelection;

    private OnSelectListener<T> onSelectListener;

    public SelectableRecyclerViewAdaptor(ContextsActivity activity, List<T> data) {
        this(activity, data, false);

    }

    public SelectableRecyclerViewAdaptor(ContextsActivity activity, List<T> data, boolean multipleSelection) {
        super(activity, data);
        this.multipleSelection = multipleSelection;
    }

    public OnSelectListener<T> getOnSelectListener() {
        return onSelectListener;
    }

    public void setOnSelectListener(OnSelectListener<T> onSelectListener) {
        this.onSelectListener = onSelectListener;
    }

    public void clearSelection() {
        Set<Integer> updateSet = new LinkedHashSet<>();
        for(T s:selectionSet){
            int idx = data.indexOf(s);
            if (idx >= 0) {
                updateSet.add(idx);
            }
        }
        selectionSet.clear();
        for (Integer i : updateSet) {
            notifyItemRangeChanged(i, 1);
        }
        if(updateSet.size()>0 && onSelectListener!=null){
            onSelectListener.onSelect(Collections.unmodifiableSet(selectionSet));
        }
    }

    public void addSelection(T... objs) {
        boolean update = false;
        Set<Integer> updateSet = new LinkedHashSet<>();
        for (T obj : objs) {
            if (!selectionSet.contains(obj)) {
                if (!multipleSelection && selectionSet.size() > 0) {
                    for (T s : selectionSet) {
                        int idx = data.indexOf(s);
                        if (idx >= 0) {
                            updateSet.add(idx);
                        }
                    }
                    selectionSet.clear();
                }
                selectionSet.add(obj);

                update = true;
            }
            int idx = data.indexOf(obj);
            if (idx >= 0) {
                updateSet.add(idx);
            }
        }
        if (update) {
            for (Integer i : updateSet) {
                notifyItemRangeChanged(i, 1);
            }

            if(updateSet.size()>0 && onSelectListener!=null){
                onSelectListener.onSelect(Collections.unmodifiableSet(selectionSet));
            }
        }

    }

    public void removeSelection(T... objs) {
        boolean update = false;
        Set<Integer> updateSet = new LinkedHashSet<>();
        for (T obj : objs) {
            if (selectionSet.remove(obj)) {
                update = true;
            }
            int idx = data.indexOf(obj);
            if (idx >= 0) {
                updateSet.add(idx);
            }
        }
        if (update) {
            for (Integer i : updateSet) {
                notifyItemRangeChanged(i, 1);
            }
            if(updateSet.size()>0 && onSelectListener!=null){
                onSelectListener.onSelect(Collections.unmodifiableSet(selectionSet));
            }
        }
    }

    public boolean isSelected(T obj) {
        return selectionSet.contains(obj);
    }

    public boolean isSelectable(T obj) {
        return true;
    }

    public static class SelectableViewHolder<A extends SelectableRecyclerViewAdaptor<T, ?>, T> extends RecyclerViewAdaptor.SimpleViewHolder<A, T> implements View.OnClickListener, View.OnTouchListener {

        public SelectableViewHolder(A adaptor, View itemView) {
            super(adaptor, itemView);
            itemView.setOnClickListener(this);
            itemView.setOnTouchListener(this);
            itemView.setBackgroundResource(adaptor.activity.getSelectableBackgroundId());
        }

        @Override
        public void bindViewValue(T item) {
            super.bindViewValue(item);

            if (adaptor.isSelectable(item)) {
                if (adaptor.isSelected(item)) {
                    itemView.setBackgroundResource(adaptor.activity.getSelectedBackgroundId());
                } else {
                    itemView.setBackgroundResource(adaptor.activity.getSelectableBackgroundId());
                }
            } else {
                itemView.setBackgroundResource(adaptor.activity.getSelectableBackgroundId());
            }
        }

        @Override
        public void onClick(View v) {
            if (v == itemView) {
//                int pos = getAdapterPosition();
//                T item = adaptor.get(pos);
//                if (!adaptor.isSelectable(item)) {
//                    return;
//                }
//                if (adaptor.isSelected(item)) {
//                    adaptor.removeSelection(item);
//                } else {
//                    adaptor.addSelection(item);
//                }
            }
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (v == itemView) {
                if(!adaptor.multipleSelection && event.getAction()==MotionEvent.ACTION_DOWN){
//                    adaptor.clearSelection();
                }
                if(event.getAction()==MotionEvent.ACTION_UP){
                    int pos = getAdapterPosition();
                    T item = adaptor.get(pos);
                    if (!adaptor.isSelectable(item)) {
                        //eat it, no ripple effect
                        return true;
                    }
                    if (adaptor.isSelected(item)) {
                        adaptor.removeSelection(item);
                    } else {
                        adaptor.addSelection(item);
                    }
                }
                //return false to keep ripple effect
                return false;
            }
            return false;
        }
    }


    public interface OnSelectListener<T>{
        public void onSelect(Set<T> selection);
    }
}
