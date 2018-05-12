package com.colaorange.dailymoney.core.ui.helper;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.colaorange.dailymoney.core.context.ContextsActivity;

import java.util.Collection;
import java.util.List;

/**
 * Created by Dennis
 */
public abstract class RecyclerViewAdaptor<T, VH extends RecyclerViewAdaptor.SimpleViewHolder> extends RecyclerView.Adapter<VH> {

    protected final ContextsActivity activity;
    protected final List<T> data;

    public RecyclerViewAdaptor(ContextsActivity activity, List<T> data) {
        this.activity = activity;
        this.data = data;
    }

    public void add(T... objs) {
        int i = data.size();
        for (T obj : objs) {
            data.add(obj);
        }
        notifyItemRangeInserted(i, objs.length);
    }

    public void addAll(Collection<T> objs) {
        int i = data.size();
        data.addAll(objs);
        notifyItemRangeInserted(i, objs.size());
    }

    public void remove(T... objs) {
        for (T obj : objs) {
            data.remove(obj);
        }
        notifyDataSetChanged();
    }

    public T get(int i) {
        return data.get(i);
    }

    public void clear() {
        data.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public void onBindViewHolder(@NonNull SimpleViewHolder holder, int position) {
        holder.bindViewValue(get(position));
    }


    public static class SimpleViewHolder<A extends RecyclerViewAdaptor<T, ?>, T> extends RecyclerView.ViewHolder {

        protected final A adaptor;

        public SimpleViewHolder(A adaptor, View itemView) {
            super(itemView);
            this.adaptor = adaptor;
        }

        public void bindViewValue(T item) {
        }

    }
}
