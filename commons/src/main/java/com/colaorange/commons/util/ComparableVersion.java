package com.colaorange.commons.util;
//package org.apache.maven.artifact.versioning;
/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Stack;

/**
 * <p>
 * Generic implementation of version comparison.
 * </p>
 * <p>
 * Features:
 * <ul>
 * <li>mixing of '<code>-</code>' (hyphen) and '<code>.</code>' (dot) separators,</li>
 * <li>transition between characters and digits also constitutes a separator:
 * <code>1.0alpha1 =&gt; [1, 0, alpha, 1]</code></li>
 * <li>unlimited number of version components,</li>
 * <li>version components in the text can be digits or strings,</li>
 * <li>strings are checked for well-known qualifiers and the qualifier ordering is used for version ordering.
 * Well-known qualifiers (case insensitive) are:<ul>
 * <li><code>alpha</code> or <code>a</code></li>
 * <li><code>beta</code> or <code>b</code></li>
 * <li><code>milestone</code> or <code>m</code></li>
 * <li><code>rc</code> or <code>cr</code></li>
 * <li><code>snapshot</code> or <code>freshly</code> or <code>fl</code></li>
 * <li><code>(the empty string)</code> or <code>ga</code> or <code>final</code></li>
 * <li><code>sp</code></li>
 * </ul>
 * Unknown qualifiers are considered after known qualifiers, with lexical order (always case insensitive),
 * </li>
 * <li>a hyphen usually precedes a qualifier, and is always less important than something preceded with a dot.</li>
 * </ul>
 *
 * @author <a href="mailto:kenney@apache.org">Kenney Westerhof</a>
 * @author <a href="mailto:hboutemy@apache.org">Hervé Boutemy</a>
 * @see <a href="https://cwiki.apache.org/confluence/display/MAVENOLD/Versioning">"Versioning" on Maven Wiki</a>
 */
public class ComparableVersion
        implements Comparable<ComparableVersion> {

    public static final String SNAPSHOT = "snapshot";
    private String value;

    private String canonical;

    /**
     * get semantic of version, e.g. 1.2.3
     * read https://semver.org/
     */
    private String semantic;

    /**
     * if this version contains snapshot qualifier
     */
    private boolean snapshot;

    private ListItem items;

    private interface Item {
        int INTEGER_ITEM = 0;
        int STRING_ITEM = 1;
        int LIST_ITEM = 2;

        int compareTo(Item item);

        int getType();

        boolean isNull();

        boolean isSnapshot();
    }

    /**
     * Represents a numeric item in the version item list.
     */
    private static class IntegerItem
            implements Item {
        private static final BigInteger BIG_INTEGER_ZERO = new BigInteger("0");

        private final BigInteger value;

        public static final IntegerItem ZERO = new IntegerItem();

        private IntegerItem() {
            this.value = BIG_INTEGER_ZERO;
        }

        IntegerItem(String str) {
            this.value = new BigInteger(str);
        }

        public int getType() {
            return INTEGER_ITEM;
        }

        public boolean isNull() {
            return BIG_INTEGER_ZERO.equals(value);
        }
        public boolean isSnapshot(){
            return false;
        }

        public int compareTo(Item item) {
            if (item == null) {
                return BIG_INTEGER_ZERO.equals(value) ? 0 : 1; // 1.0 == 1, 1.1 > 1
            }

            switch (item.getType()) {
                case INTEGER_ITEM:
                    return value.compareTo(((IntegerItem) item).value);

                case STRING_ITEM:
                    return 1; // 1.1 > 1-sp

                case LIST_ITEM:
                    return 1; // 1.1 > 1-1

                default:
                    throw new RuntimeException("invalid item: " + item.getClass());
            }
        }

        public String toString() {
            return value.toString();
        }
    }

    /**
     * Represents a string in the version item list, usually a qualifier.
     */
    private static class StringItem
            implements Item {
        private static final List<String> QUALIFIERS =
                Arrays.asList("alpha", "beta", "milestone", "rc", SNAPSHOT, "", "sp");

        private static final Properties ALIASES = new Properties();

        static {
            ALIASES.put("ga", "");
            ALIASES.put("final", "");
            ALIASES.put("cr", "rc");
            ALIASES.put("freshly", SNAPSHOT);
            ALIASES.put("fl", SNAPSHOT);

        }

        /**
         * A comparable value for the empty-string qualifier. This one is used to determine if a given qualifier makes
         * the version older than one without a qualifier, or more recent.
         */
        private static final String RELEASE_VERSION_INDEX = String.valueOf(QUALIFIERS.indexOf(""));

        private String value;

        StringItem(String value, boolean followedByDigit) {
            if (followedByDigit && value.length() == 1) {
                // a1 = alpha-1, b1 = beta-1, m1 = milestone-1
                switch (value.charAt(0)) {
                    case 'a':
                        value = "alpha";
                        break;
                    case 'b':
                        value = "beta";
                        break;
                    case 'm':
                        value = "milestone";
                        break;
                    default:
                }
            }
            this.value = ALIASES.getProperty(value, value);
        }

        public int getType() {
            return STRING_ITEM;
        }

        public boolean isNull() {
            return (comparableQualifier(value).compareTo(RELEASE_VERSION_INDEX) == 0);
        }

        /**
         * Returns a comparable value for a qualifier.
         * <p>
         * This method takes into account the ordering of known qualifiers then unknown qualifiers with lexical
         * ordering.
         * <p>
         * just returning an Integer with the index here is faster, but requires a lot of if/then/else to check for -1
         * or QUALIFIERS.size and then resort to lexical ordering. Most comparisons are decided by the first character,
         * so this is still fast. If more characters are needed then it requires a lexical sort anyway.
         *
         * @param qualifier
         * @return an equivalent value that can be used with lexical comparison
         */
        public static String comparableQualifier(String qualifier) {
            int i = QUALIFIERS.indexOf(qualifier);

            return i == -1 ? (QUALIFIERS.size() + "-" + qualifier) : String.valueOf(i);
        }

        public int compareTo(Item item) {
            if (item == null) {
                // 1-rc < 1, 1-ga > 1
                return comparableQualifier(value).compareTo(RELEASE_VERSION_INDEX);
            }
            switch (item.getType()) {
                case INTEGER_ITEM:
                    return -1; // 1.any < 1.1 ?

                case STRING_ITEM:
                    return comparableQualifier(value).compareTo(comparableQualifier(((StringItem) item).value));

                case LIST_ITEM:
                    return -1; // 1.any < 1-1

                default:
                    throw new RuntimeException("invalid item: " + item.getClass());
            }
        }

        public String toString() {
            return value;
        }

        public boolean isSnapshot(){
            return SNAPSHOT.equals(value);
        }
    }

    /**
     * Represents a version list item. This class is used both for the global item list and for sub-lists (which start
     * with '-(number)' in the version specification).
     */
    private static class ListItem
            extends ArrayList<Item>
            implements Item {
        public int getType() {
            return LIST_ITEM;
        }

        public boolean isNull() {
            return (size() == 0);
        }

        void normalize() {
            for (int i = size() - 1; i >= 0; i--) {
                Item lastItem = get(i);

                if (lastItem.isNull()) {
                    // remove null trailing items: 0, "", empty list
                    remove(i);
                } else if (!(lastItem instanceof ListItem)) {
                    break;
                }
            }
        }

        public int compareTo(Item item) {
            if (item == null) {
                if (size() == 0) {
                    return 0; // 1-0 = 1- (normalize) = 1
                }
                Item first = get(0);
                return first.compareTo(null);
            }
            switch (item.getType()) {
                case INTEGER_ITEM:
                    return -1; // 1-1 < 1.0.x

                case STRING_ITEM:
                    return 1; // 1-1 > 1-sp

                case LIST_ITEM:
                    Iterator<Item> left = iterator();
                    Iterator<Item> right = ((ListItem) item).iterator();

                    while (left.hasNext() || right.hasNext()) {
                        Item l = left.hasNext() ? left.next() : null;
                        Item r = right.hasNext() ? right.next() : null;

                        // if this is shorter, then invert the compare and mul with -1
                        int result = l == null ? (r == null ? 0 : -1 * r.compareTo(l)) : l.compareTo(r);

                        if (result != 0) {
                            return result;
                        }
                    }

                    return 0;

                default:
                    throw new RuntimeException("invalid item: " + item.getClass());
            }
        }

        public String toString() {
            StringBuilder buffer = new StringBuilder();
            for (Item item : this) {
                if (buffer.length() > 0) {
                    buffer.append((item instanceof ListItem) ? '-' : '.');
                }
                buffer.append(item);
            }
            return buffer.toString();
        }
        public boolean isSnapshot(){
            for (Item item : this) {
                if(item.isSnapshot()){
                    return true;
                }
            }
            return false;
        }
    }

    public ComparableVersion(String version) {
        parseVersion(version);
    }

    @SuppressWarnings("checkstyle:innerassignment")
    public final void parseVersion(String version) {
        this.value = version;

        items = new ListItem();

        version = version.toLowerCase(Locale.ENGLISH);

        ListItem list = items;

        Stack<Item> stack = new Stack<>();
        stack.push(list);

        boolean isDigit = false;

        int startIndex = 0;

        for (int i = 0; i < version.length(); i++) {
            char c = version.charAt(i);

            if (c == '.') {
                if (i == startIndex) {
                    list.add(IntegerItem.ZERO);
                } else {
                    list.add(parseItem(isDigit, version.substring(startIndex, i)));
                }
                startIndex = i + 1;
            } else if (c == '-') {
                if (i == startIndex) {
                    list.add(IntegerItem.ZERO);
                } else {
                    list.add(parseItem(isDigit, version.substring(startIndex, i)));
                }
                startIndex = i + 1;

                list.add(list = new ListItem());
                stack.push(list);
            } else if (Character.isDigit(c)) {
                if (!isDigit && i > startIndex) {
                    list.add(new StringItem(version.substring(startIndex, i), true));
                    startIndex = i;

                    list.add(list = new ListItem());
                    stack.push(list);
                }

                isDigit = true;
            } else {
                if (isDigit && i > startIndex) {
                    list.add(parseItem(true, version.substring(startIndex, i)));
                    startIndex = i;

                    if(semantic==null){
                        semantic = list.toString();
                    }

                    list.add(list = new ListItem());
                    stack.push(list);
                }

                isDigit = false;
            }
        }

        if (version.length() > startIndex) {
            list.add(parseItem(isDigit, version.substring(startIndex)));
        }

        while (!stack.isEmpty()) {
            list = (ListItem) stack.pop();
            list.normalize();
            System.out.println(":>>>>>>>"+list.toString());
            if(stack.empty()){
                StringBuilder sb = new StringBuilder();
                for(Item i:list){
                    if(i.getType()== Item.INTEGER_ITEM){
                        if(sb.length()!=0){
                            sb.append(".");
                        }
                        sb.append(i.toString());
                    }else{
                        break;
                    }
                }
                if(sb.length()>0) {
                    semantic = sb.toString();
                }else{
                    semantic = "0";
                }
            }
        }

        canonical = items.toString();

        snapshot = items.isSnapshot();


    }

    private static Item parseItem(boolean isDigit, String buf) {
        return isDigit ? new IntegerItem(buf) : new StringItem(buf, false);
    }

    public int compareTo(ComparableVersion o) {
        return items.compareTo(o.items);
    }

    public String toString() {
        return value;
    }

    public String getCanonical() {
        return canonical;
    }
    public String getSemantic() {
        return semantic;
    }
    public boolean isSnapshot(){
        return snapshot;
    }


    public boolean equals(Object o) {
        return (o instanceof ComparableVersion) && canonical.equals(((ComparableVersion) o).canonical);
    }

    public int hashCode() {
        return canonical.hashCode();
    }

    // CHECKSTYLE_OFF: LineLength

    /**
     * Main to test version parsing and comparison.
     * <p>
     * To check how "1.2.7" compares to "1.2-SNAPSHOT", for example, you can issue
     * <pre>java -jar ${maven.repo.local}/org/apache/maven/maven-artifact/${maven.version}/maven-artifact-${maven.version}.jar "1.2.7" "1.2-SNAPSHOT"</pre>
     * command to command line. Result of given command will be something like this:
     * <pre>
     * Display parameters as parsed by Maven (in canonical form) and comparison result:
     * 1. 1.2.7 == 1.2.7
     *    1.2.7 &gt; 1.2-SNAPSHOT
     * 2. 1.2-SNAPSHOT == 1.2-snapshot
     * </pre>
     *
     * @param args the version strings to parse and compare. You can pass arbitrary number of version strings and always
     *             two adjacent will be compared
     */
    // CHECKSTYLE_ON: LineLength
    public static void main(String... args) {
        System.out.println("Display parameters as parsed by Maven (in canonical form) and comparison result:");
        if (args.length == 0) {
            return;
        }

        ComparableVersion prev = null;
        int i = 1;
        for (String version : args) {
            ComparableVersion c = new ComparableVersion(version);

            if (prev != null) {
                int compare = prev.compareTo(c);
                System.out.println("   " + prev.toString() + ' '
                        + ((compare == 0) ? "==" : ((compare < 0) ? "<" : ">")) + ' ' + version);
            }

            System.out.println(String.valueOf(i++) + ". " + version + " == " + c.getCanonical());

            prev = c;
        }
    }
}