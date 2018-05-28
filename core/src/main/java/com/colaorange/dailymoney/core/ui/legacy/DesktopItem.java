package com.colaorange.dailymoney.core.ui.legacy;

import android.content.Intent;

/**
 * @author dennis
 */
public class DesktopItem {

    private int icon;

    private String label;

    private Runnable run;

    private int priority;

    private boolean inMenu;

    private boolean inDesktop;

    public DesktopItem(Runnable run, String label) {
        this(run, label, -1, false, true, 0);
    }

    public DesktopItem(Runnable run, String label, int icon) {
        this(run, label, icon, true, false, 0);
    }

    public DesktopItem(Runnable run, String label, int icon, boolean inDesktop, boolean menuItem, int priority) {
        this.run = run;
        this.label = label;
        this.icon = icon;
        this.inDesktop = inDesktop;
        this.inMenu = menuItem;
        this.priority = priority;
    }

    public boolean isInDesktop() {
        return inDesktop;
    }

    public void setInDesktop(boolean inDesktop) {
        this.inDesktop = inDesktop;
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

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Runnable getRun() {
        return run;
    }

    public void setRun(Runnable run) {
        this.run = run;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public boolean isInMenu() {
        return inMenu;
    }

    public void setInMenu(boolean inMenu) {
        this.inMenu = inMenu;
    }
}
