package com.colaorange.dailymoney.core.ui.helper;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.context.ContextsActivity;
import com.colaorange.dailymoney.core.context.ContextsFragment;
import com.colaorange.dailymoney.core.util.Logger;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * a recycler view fragment to append to pageview for single or multiple child fragment to vertical solve scroll issue
 *
 * @author dennis
 */
public class FragsRecyclerViewFragment extends ContextsFragment {


    public static final String ARG_FRAGS_NEWER_LIST = "fragsNewerList";

    private View rootView;

    List<FragNewer> fragmentNewers;
    RecyclerView vRecycler;
    FragsRecyclerAdapter adapter;

    Map<String, Fragment> createdFragments = new LinkedHashMap<>();

    public interface FragNewer extends Serializable {
        Fragment newFragment();
    }

    public FragsRecyclerViewFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.frags_recycler_view_frag, container, false);
        return rootView;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initArgs();
        initMembers();
    }


    private void initArgs() {
        Bundle args = getArguments();
        fragmentNewers = (List) args.getSerializable(ARG_FRAGS_NEWER_LIST);
    }

    private void initMembers() {
        vRecycler = rootView.findViewById(R.id.frags_recycler);
        vRecycler.setLayoutManager(new LinearLayoutManager(getContextsActivity()));
        vRecycler.setAdapter(adapter = new FragsRecyclerAdapter());
    }

    @Override
    public void onResume() {
        super.onResume();
        clearCreatedFragments();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onStop() {
//        //it is ok to clear here, fragment will build back in onResume
        clearCreatedFragments();
        super.onStop();

    }


    private void clearCreatedFragments() {
        if (createdFragments.size() > 0) {
            FragmentTransaction ft = getChildFragmentManager().beginTransaction();
            for (String k : createdFragments.keySet()) {
                Fragment f = createdFragments.get(k);
                ft.remove(f);
            }
            //very important to call commitNow, or will get error for immedidatelly adapter binding
            ft.commitNowAllowingStateLoss();
            createdFragments.clear();
        }
    }

    public class FragsRecyclerAdapter extends RecyclerView.Adapter<FragViewHolder> {


        @NonNull
        @Override
        public FragViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ContextsActivity activity = getContextsActivity();
            View itemView = ((LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.frags_recycler_view_anchor, parent, false);
            //use dynamic id , so we can add fragment to it later.
            itemView.setId(activity.generateViewId());
            return new FragViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull FragViewHolder holder, int position) {
        }


        @Override
        public void onViewAttachedToWindow(@NonNull FragViewHolder vh) {
            super.onViewAttachedToWindow(vh);
            int pos = vh.getAdapterPosition();
            FragNewer newer = fragmentNewers.get(pos);
            String fragTag = "frags-recycler:" + vh.itemView.getId();
            FragmentManager fm = getChildFragmentManager();
            Fragment f = fm.findFragmentByTag(fragTag);
            if (f != null) {
                fm.beginTransaction()
                        .attach(f)
                        .commitNowAllowingStateLoss();
            } else {
                fm.beginTransaction()
                        .add(vh.itemView.getId(), f = newer.newFragment(), fragTag)
                        .commitNowAllowingStateLoss();
                vh.boundFragTag = fragTag;

                createdFragments.put(fragTag, f);
            }
        }

        @Override
        public void onViewDetachedFromWindow(@NonNull FragViewHolder vh) {
            super.onViewDetachedFromWindow(vh);
            //detach old fragment
            if (vh.boundFragTag != null) {
                FragmentManager fm = getChildFragmentManager();
                Fragment f = fm.findFragmentByTag(vh.boundFragTag);
                if (f != null) {
                    try {
                        FragmentTransaction ft = fm.beginTransaction();
                        ft.detach(f);
                        ft.commitNowAllowingStateLoss();
                    } catch (Exception x) {
                        Logger.e(x.getMessage(), x);
                    }
                }
                vh.boundFragTag = null;
            }
        }

        @Override
        public int getItemCount() {
            return fragmentNewers == null ? 0 : fragmentNewers.size();
        }

    }

    public class FragViewHolder extends RecyclerView.ViewHolder {
        String boundFragTag;

        public FragViewHolder(View itemView) {
            super(itemView);
        }
    }

}