package com.colaorange.dailymoney.core.ui.legacy;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.colaorange.dailymoney.core.data.Account;
import com.colaorange.dailymoney.core.data.AccountType;

/**
 * @author dennis
 */
public class AccountUtil {

    public static List<AccountIndentNode> toIndentNode(List<Account> accl) {
        List<AccountIndentNode> better = new ArrayList<AccountIndentNode>();
        Map<String, AccountIndentNode> tree = new LinkedHashMap<String, AccountIndentNode>();
        for (Account acc : accl) {
            String name = acc.getName();
            StringBuilder path = new StringBuilder();
            AccountIndentNode node = null;
            String pp = null;
            String np = null;
            AccountType type = AccountType.find(acc.getType());
            int indent = 0;
            for (String t : name.split("\\.")) {
                if (t.length() == 0) {
                    continue;
                }
                pp = path.toString();
                if (path.length() != 0) {
                    path.append(".");
                }
                np = path.append(t).toString();
                if ((node = tree.get(np)) != null) {
                    indent++;
                    continue;
                }
                node = new AccountIndentNode(pp, t, indent, type, null);
                indent++;
                tree.put(np, node);
            }
            if (node != null) {
                node.account = acc;
            }
        }

        for (String key : tree.keySet()) {
            AccountIndentNode tn = tree.get(key);
            better.add(tn);
        }

        return better;
    }

    public static class AccountIndentNode {
        private String path;
        private String name;
        private AccountType type;
        private Account account;
        private int indent;
        private String fullpath;

        public AccountIndentNode(String path, String name, int indent, AccountType type, Account account) {
            this.path = path;
            this.name = name;
            this.indent = indent;
            this.type = type;
            this.account = account;
            fullpath = (path == null || path.equals("")) ? name : path + "." + name;
        }

        public String getPath() {
            return path;
        }

        public String getName() {
            return name;
        }

        public AccountType getType() {
            return type;
        }

        public Account getAccount() {
            return account;
        }

        public int getIndent() {
            return indent;
        }

        public String getFullPath() {
            return fullpath;
        }


    }
}
