package com.colaorange.dailymoney.core.ui;

/**
 * Created by Dennis
 */
public class QEevents {

    public interface AccountMgnt {
        public String ON_CLEAR_SELECTION = "accountMgnt:onClearSelection";
        public String ON_SELECT_ACCOUNT = "accountMgnt:onSelectAccount";
        public String ON_RELOAD_LIST = "accountMgnt:onReloadList";

        public String ON_EDIT_SELECTED_ACCOUNT = "accountMgnt:onEditSelectedAccount";
    }
}
