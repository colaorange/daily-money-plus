package com.colaorange.dailymoney.core.ui.legacy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

/**
 * 
 * @author dennis
 * 
 */
public class Desktop {


    public static final int NO_ICON = -1;

    protected String label;
    protected int icon;
    protected Activity activity;

    List<DesktopItem> items = new ArrayList<DesktopItem>();

    public Desktop(Activity activity){
        this(activity,"",NO_ICON);
    }
    
    public Desktop(Activity activity,String label, int icon) {
        this(activity,label, icon, null);
    }

    public Desktop(Activity activity,String label, int icon, List<DesktopItem> items) {
        this.activity = activity;
        this.label = label;
        this.icon = icon;
        if (items != null) {
            this.items.addAll(items);
        }
    }

    public void addItem(DesktopItem item) {
        items.add(item);
    }
    
    

    public String getLabel() {
        return label;
    }

    public int getIcon() {
        return icon;
    }

    public List<DesktopItem> getItems() {
        ArrayList<DesktopItem> list  = new ArrayList<>(items);
        Collections.sort(list, new Comparator<DesktopItem>() {
            public int compare(DesktopItem item1, DesktopItem item2) {
                return Integer.valueOf(item2.getPriority()).compareTo(Integer.valueOf(item1.getPriority()));
            }
        });
        return list;
    }

    public List<DesktopItem> getMenuItems() {
        List<DesktopItem> list = getItems();
        Iterator<DesktopItem> i = list.iterator();
        while(i.hasNext()){
            if(!i.next().isInMenu()){
                i.remove();
            }
        }
        return list;
    }

    public List<DesktopItem> getDesktopItems() {
        List<DesktopItem> list = getItems();
        Iterator<DesktopItem> i = list.iterator();
        while(i.hasNext()){
            if(!i.next().isInDesktop()){
                i.remove();
            }
        }
        return list;
    }


    public static class IntentRun implements Runnable {
        Intent intent;
        Context context;

        public IntentRun(Context context, Intent intent) {
            this.context = context;
            this.intent = intent;
        }

        public void run() {
            context.startActivity(intent);
        }
    }

    public static class ActivityRun implements Runnable {
        Class<? extends Activity> activity;
        Context context;

        public ActivityRun(Context context, Class<? extends Activity> activity) {
            this.context = context;
            this.activity = activity;
        }

        public void run() {
            context.startActivity(new Intent(context, activity));
        }
    }

    public void refresh() {
    }
    
    public boolean isAvailable(){
        return true;
    }

}
