package com.colaorange.dailymoney.core.ui;

/**
 * Created by Dennis
 */
public class QEvents {

    public interface DesktopMgnt {
        //activity -> fragment
        public String ON_CLEAR_SELECTION = "desktopMgnt:onClearSelection";
        public String ON_RELOAD_FRAGMENT = "desktopMgnt:onReloadFragment";

        //fragment -> activity
        public String ON_SELECT_DESKTOP_TEIM = "desktopMgnt:onSelectDesktopItem";
        public String ON_RESELECT_DESKTOP_TEIM = "desktopMgnt:onReselectDesktopItem";
    }


    public interface AccountMgnt {
        //activity -> fragment
        public String ON_CLEAR_SELECTION = "accountMgnt:onClearSelection";
        public String ON_RELOAD_FRAGMENT = "accountMgnt:onReloadFragment";

        //fragment -> activity
        public String ON_SELECT_ACCOUNT = "accountMgnt:onSelectAccount";
        public String ON_RESELECT_ACCOUNT = "accountMgnt:onReselectAccount";
    }


    public interface BalanceMgnt {
        //activity -> fragment
        public String ON_CLEAR_SELECTION = "balanceMgnt:onClearSelection";
        public String ON_RELOAD_FRAGMENT = "balanceMgnt:onReloadFragment";

        //fragment -> activity
        public String ON_SELECT_BALANCE = "balanceMgnt:onSelectBalance";
        public String ON_RESELECT_BALANCE = "balanceMgnt:onReselectBalance";
        public String ON_FRAGMENT_START = "balanceMgnt:onFragmentStart";
        public String ON_FRAGMENT_STOP = "balanceMgnt:onFragmentStop";

    }


    public interface RecordMgnt {
        //activity -> fragment
        public String ON_CLEAR_SELECTION = "recordMgnt:onClearSelection";
        public String ON_RELOAD_FRAGMENT = "recordMgnt:onReloadFragment";

        //fragment -> activity
        public String ON_SELECT_RECORD = "recordMgnt:onSelectRecord";
        public String ON_RESELECT_RECORD = "recordMgnt:onReselectRecord";
        public String ON_FRAGMENT_START = "recordMgnt:onFragmentStart";
        public String ON_FRAGMENT_STOP = "recordMgnt:onFragmentStop";

    }

}
