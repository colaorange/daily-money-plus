package com.colaorange.dailymoney.core.ui.legacy;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.colaorange.dailymoney.core.data.Account;
import com.colaorange.dailymoney.core.data.AccountType;

/**
 * @author dennis
 */
public class AccountUtil {

    public static List<AccountIndentNode> toIndentNode(List<Account> accountList) {

        Map<String, TreeNode> treeMap = new LinkedHashMap<String, TreeNode>();


        TreeNode root = new TreeNode();
        root.children = new LinkedList<>();

        for (Account acc : accountList) {
            String name = acc.getName();
            StringBuilder path = new StringBuilder();
            TreeNode node = null;
            String parentPath = null;
            String nodePath = null;
            AccountType type = AccountType.find(acc.getType());
            int indent = 0;
            for (String element : name.split("\\.")) {
                if (element.length() == 0) {
                    continue;
                }
                parentPath = path.toString();
                if (path.length() != 0) {
                    path.append(".");
                }
                nodePath = path.append(element).toString();

                node = treeMap.get(nodePath);

                if (node != null) {
                    indent++;
                    continue;
                }

                node = new TreeNode();

                node.intentNode = new AccountIndentNode(parentPath, element, indent, type, null);
                node.children = new LinkedList<>();


                TreeNode parentNode = treeMap.get(parentPath);
                if (parentNode == null) {
                    parentNode = root;
                }

                parentNode.children.add(node);

                indent++;
                treeMap.put(nodePath, node);
            }

            if (node != null) {
                node.intentNode.account = acc;
            }
        }

        List<AccountIndentNode> list = new LinkedList<>();

        List<TreeNode> stack = new LinkedList();
        stack.addAll(root.children);

        while (stack.size() > 0) {
            TreeNode node = stack.get(0);
            stack.remove(0);

            list.add(node.intentNode);

            for (int i = node.children.size() - 1; i >= 0; i--) {
                TreeNode n = node.children.get(i);
                stack.add(0, n);
            }
        }

        return list;
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

    private static class TreeNode {
        AccountIndentNode intentNode;
        List<TreeNode> children;
    }
}
