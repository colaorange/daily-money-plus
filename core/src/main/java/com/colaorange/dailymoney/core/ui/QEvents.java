package com.colaorange.dailymoney.core.ui;

/**
 * @author Dennis
 */
public class QEvents {

    public interface DesktopMgntFrag {
        //activity -> fragment
        String ON_CLEAR_SELECTION = "DesktopMgntFrag:onClearSelection";
        String ON_RELOAD_FRAGMENT = "DesktopMgntFrag:onReloadFragment";

        //fragment -> activity
        String ON_SELECT_DESKTOP_ITEM = "DesktopMgntFrag:onSelectDesktopItem";
        String ON_RESELECT_DESKTOP_ITEM = "DesktopMgntFrag:onReselectDesktopItem";
    }

    public interface CardDesktopFrag {
        //activity -> fragment
        String ON_RELOAD_FRAGMENT = "CardDesktopFrag:onReloadFragment";
        String ON_CLEAR_FRAGMENT =  "CardDesktopFrag:onClearFragment";
        
    }
    public interface CardFrag {
        //activity -> fragment
        String ON_RELOAD_VIEW = "CardFrag:onReloadView";
    }


    public interface AccountMgntFrag {
        //activity -> fragment
        String ON_CLEAR_SELECTION = "AccountMgntFrag:onClearSelection";
        String ON_RELOAD_FRAGMENT = "AccountMgntFrag:onReloadFragment";
        String ON_REORDER_MODE = "AccountMgntFrag:onReOrderMode";

        //fragment -> activity
        String ON_SELECT_ACCOUNT = "AccountMgntFrag:onSelectAccount";
        String ON_RESELECT_ACCOUNT = "AccountMgntFrag:onReselectAccount";
    }


    public interface BalanceMgntFrag {
        //activity -> fragment
        String ON_CLEAR_SELECTION = "BalanceMgntFrag:onClearSelection";
        String ON_RELOAD_FRAGMENT = "BalanceMgntFrag:onReloadFragment";
        String ON_EXPORT_EXCEL = "BalanceMgntFrag:onExportExcel";

        //fragment -> activity
        String ON_SELECT_BALANCE = "BalanceMgnt:onSelectBalance";
        String ON_RESELECT_BALANCE = "BalanceMgnt:onReselectBalance";
        String ON_FRAGMENT_START = "BalanceMgnt:onFragmentStart";
        String ON_FRAGMENT_STOP = "BalanceMgnt:onFragmentStop";

    }


    public interface RecordMgntFrag {
        //activity -> fragment
        String ON_RELOAD_FRAGMENT = "RecordMgntFrag:onReloadFragment";

        //fragment -> activity
        String ON_FRAGMENT_START = "RecordMgntFrag:onFragmentStart";
        String ON_FRAGMENT_STOP = "RecordMgntFrag:onFragmentStop";

    }

    public interface RecordListFrag {
        //activity -> fragment
        String ON_CLEAR_SELECTION = "RecordListFrag:onClearSelection";
        String ON_RELOAD_FRAGMENT = "RecordListFrag:onReloadFragment";
        String ON_EXPORT_EXCEL = "RecordListFrag:onExportExcel";

        //fragment -> activity
        String ON_SELECT_RECORD = "RecordListFrag:onSelectRecord";
        String ON_RESELECT_RECORD = "RecordListFrag:onReselectRecord";

    }

    public interface RecordMigratorFrag {
        //fragment -> activity
        String ON_MIGRATE = "RecordMigrateFrag:onMigrate";
    }

    public interface RecordMigratorReviewAccountListFrag {
        //activity -> fragment
        String ON_RELOAD_FRAGMENT = "RecordMigrateReviewAccountListFrag:onReloadFragment";
    }

    public interface RecordMigratorIndicatorFrag {
        //activity -> fragment
        String ON_RELOAD_FRAGMENT = "RecordMigrateIndicatorFrag:onReloadFragment";
    }
}
