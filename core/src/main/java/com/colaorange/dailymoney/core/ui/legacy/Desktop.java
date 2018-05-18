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

    final protected String name;
    protected String label;
    protected int icon;
    protected Activity activity;

    List<DesktopItem> items = new ArrayList<DesktopItem>();

    public Desktop(String name, Activity activity){
        this(name,activity,"",NO_ICON);
    }
    
    public Desktop(String name, Activity activity,String label, int icon) {
        this(name,activity,label, icon, null);
    }

    public Desktop(String name, Activity activity,String label, int icon, List<DesktopItem> items) {
        this.name = name;
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

    public String getName() {
        return name;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Desktop desktop = (Desktop) o;

        if (icon != desktop.icon) return false;
        if (label != null ? !label.equals(desktop.label) : desktop.label != null) return false;
        return items != null ? items.equals(desktop.items) : desktop.items == null;
    }

    @Override
    public int hashCode() {
        int result = label != null ? label.hashCode() : 0;
        result = 31 * result + icon;
        result = 31 * result + (items != null ? items.hashCode() : 0);
        return result;
    }
}
