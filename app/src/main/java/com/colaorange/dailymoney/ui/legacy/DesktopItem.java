package com.colaorange.dailymoney.ui.legacy;

import android.content.Intent;

import com.colaorange.dailymoney.R;
/**
 * 
 * @author dennis
 *
 */
public class DesktopItem {
    //a non-hidden item should always has icon
    protected int icon;
    
    //a item should always has label
    protected String label;
    Runnable run;
    
    //a importance item(>=0), will show to menu (the larger number will put to front of menu)
    int importance;
    
    //a hidden item, show not show to desktop, but still show to menu if it is importance
    boolean hidden;

    public DesktopItem(Runnable run, String label) {
        this(run, label, R.drawable.dtitem,-1);
    }

    public DesktopItem(Runnable run, String label, int icon) {
        this(run, label, icon,-1);
    }
    
    public DesktopItem(Runnable run, String label, int icon, int importance) {
        this.run = run;
        this.label = label;
        this.icon = icon;
        this.importance = importance;
    }

    public void run() {
        run.run();
    }

    public int getIcon() {
        return icon;
    }

    public String getLabel() {
        return label;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        
    }

    public int getImportance() {
        return importance;
    }

    public void setImportance(int importance) {
        this.importance = importance;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    
    
    
    
}
