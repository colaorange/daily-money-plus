package com.colaorange.dailymoney.core.ui;

/**
 * Created by Dennis
 */
public class QEvents {

    public interface AccountMgnt {
        //activity -> fragment
        public String ON_CLEAR_SELECTION = "accountMgnt:onClearSelection";
        public String ON_RELOAD_FRAGMENT = "accountMgnt:onReloadFragment";

        //fragment -> activity
        public String ON_SELECT_ACCOUNT = "accountMgnt:onSelectAccount";
        public String ON_RESELECT_ACCOUNT = "accountMgnt:onReselectedAccount";
    }


    public interface Balance {
        //activity -> fragment
        public String ON_CLEAR_SELECTION = "balance:onClearSelection";
        public String ON_RELOAD_FRAGMENT = "balance:onReloadFragment";

        //fragment -> activity
        public String ON_SELECT_BALANCE = "balance:onSelectBalance";
        public String ON_RESELECT_BALANCE = "balance:onReselectedBalance";
        public String ON_FRAGMENT_START = "balance:onFragmentStart";
        public String ON_FRAGMENT_STOP = "balance:onFragmentStop";


        public String ARG_FRAG_START_DATE = "fragStartDate";
        public String ARG_FRAG_END_DATE = "fragEndDate";
        public String ARG_FRAG_DATE = "fragDate";
        public String ARG_FRAG_POS = "fragPos";

    }

}
