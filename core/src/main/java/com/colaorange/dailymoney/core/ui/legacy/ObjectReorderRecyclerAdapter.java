package com.colaorange.dailymoney.core.ui.legacy;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.colaorange.commons.util.ObjectLabel;
import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.context.ContextsActivity;
import com.colaorange.dailymoney.core.ui.helper.RecyclerViewAdaptor;
import com.colaorange.dailymoney.core.ui.helper.SelectableRecyclerViewAdaptor;

import java.util.List;

/**
 * @author dennis
 */
public class ObjectReorderRecyclerAdapter<T> extends RecyclerViewAdaptor<ObjectLabel<T>, RecyclerViewAdaptor.SimpleViewHolder> {

    private LayoutInflater inflater;
    private ItemTouchHelper touchHelper;
    private ObjectReorderCallback callback;

    public interface ObjectReorderCallback {
        void onMove(int posFrom, int posTo);
    }

    public ObjectReorderRecyclerAdapter(ContextsActivity activity, ObjectReorderCallback callback, List<ObjectLabel<T>> data) {
        super(activity, data);
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.callback = callback;
        this.touchHelper = new ItemTouchHelper(new ReorderCallback());
    }

    @NonNull
    @Override
    public ObjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View viewItem = inflater.inflate(R.layout.object_reorder_item, parent, false);
        ObjectViewHolder holder = new ObjectViewHolder(this, viewItem);
        return holder;
    }

    public void attachToRecyclerView(RecyclerView view) {
        touchHelper.attachToRecyclerView(view);
    }

    public void detachFromRecyclerView(){
        touchHelper.attachToRecyclerView(null);
    }


    private class ObjectViewHolder  extends RecyclerViewAdaptor.SimpleViewHolder<ObjectReorderRecyclerAdapter<T>, ObjectLabel<T>> {

        public ObjectViewHolder(ObjectReorderRecyclerAdapter adapter, View itemView) {
            super(adapter, itemView);
            itemView.findViewById(R.id.icon_reorder).setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    touchHelper.startDrag(ObjectViewHolder.this);
                    return false;
                }
            });
        }

        @Override
        public void bindViewValue(ObjectLabel<T> object) {
            super.bindViewValue(object);

            TextView vlabel = itemView.findViewById(R.id.object_label);
            vlabel.setText(object.getLabel());
        }
    }

    private class ReorderCallback extends ItemTouchHelper.Callback {

        public ReorderCallback() {
        }

        @Override
        public boolean isLongPressDragEnabled() {
            return true;
        }

        @Override
        public boolean isItemViewSwipeEnabled() {
            return false;
        }

        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
            int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
            return makeMovementFlags(dragFlags, swipeFlags);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                              RecyclerView.ViewHolder target) {
            callback.onMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
            return true;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        }

    }
}