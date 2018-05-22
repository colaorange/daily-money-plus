package com.colaorange.dailymoney.core.ui;

/**
 * @author Dennis
 */
public class QEvents {

    public interface DesktopMgntFrag {
        //activity -> fragment
        public String ON_CLEAR_SELECTION = "DesktopMgntFrag:onClearSelection";
        public String ON_RELOAD_FRAGMENT = "DesktopMgntFrag:onReloadFragment";

        //fragment -> activity
        public String ON_SELECT_DESKTOP_TEIM = "DesktopMgntFrag:onSelectDesktopItem";
        public String ON_RESELECT_DESKTOP_TEIM = "DesktopMgntFrag:onReselectDesktopItem";
    }

    public interface CardsFrag {
        //activity -> fragment
        public String ON_RELOAD_FRAGMENT = "CardsFrag:onReloadFragment";
        public String ON_RELOAD_CARD_VIEW = "CardsFrag:onReloadCardView";
    }


    public interface AccountMgntFrag {
        //activity -> fragment
        public String ON_CLEAR_SELECTION = "AccountMgntFrag:onClearSelection";
        public String ON_RELOAD_FRAGMENT = "AccountMgntFrag:onReloadFragment";

        //fragment -> activity
        public String ON_SELECT_ACCOUNT = "AccountMgntFrag:onSelectAccount";
        public String ON_RESELECT_ACCOUNT = "AccountMgntFrag:onReselectAccount";
    }


    public interface BalanceMgntFrag {
        //activity -> fragment
        public String ON_CLEAR_SELECTION = "BalanceMgntFrag:onClearSelection";
        public String ON_RELOAD_FRAGMENT = "BalanceMgntFrag:onReloadFragment";

        //fragment -> activity
        public String ON_SELECT_BALANCE = "balanceMgnt:onSelectBalance";
        public String ON_RESELECT_BALANCE = "balanceMgnt:onReselectBalance";
        public String ON_FRAGMENT_START = "balanceMgnt:onFragmentStart";
        public String ON_FRAGMENT_STOP = "balanceMgnt:onFragmentStop";

    }


    public interface RecordMgntFrag {
        //activity -> fragment
        public String ON_RELOAD_FRAGMENT = "RecordMgntFrag:onReloadFragment";

        //fragment -> activity
        public String ON_FRAGMENT_START = "RecordMgntFrag:onFragmentStart";
        public String ON_FRAGMENT_STOP = "RecordMgntFrag:onFragmentStop";

    }

    public interface RecordListFrag {
        //activity -> fragment
        public String ON_CLEAR_SELECTION = "RecordListFrag:onClearSelection";
        public String ON_RELOAD_FRAGMENT = "RecordListFrag:onReloadFragment";

        //fragment -> activity
        public String ON_SELECT_RECORD = "RecordListFrag:onSelectRecord";
        public String ON_RESELECT_RECORD = "RecordListFrag:onReselectRecord";

    }

}
