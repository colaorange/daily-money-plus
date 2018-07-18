/**
 *
 */
package com.colaorange.dailymoney.core.data;

import com.colaorange.dailymoney.core.util.I18N;
import com.colaorange.dailymoney.core.R;


/**
 * @author dennis
 */
public enum AccountType {

    UNKONW("Z"),
    INCOME("A"),
    EXPENSE("B"),
    ASSET("C"),
    LIABILITY("D"),
    OTHER("E");

    String type;
    int drawable;

    AccountType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public String getDisplay(I18N i18n) {
        return getDisplay(i18n, type);
    }

    static final AccountType[] supported = new AccountType[]{INCOME, EXPENSE, ASSET, LIABILITY, OTHER};

    static final AccountType[] from = new AccountType[]{ASSET, INCOME, LIABILITY, OTHER, EXPENSE};

    static final AccountType[] fromIncome = new AccountType[]{ASSET, EXPENSE, LIABILITY, OTHER};
    static final AccountType[] fromExpense = new AccountType[]{ASSET, INCOME, LIABILITY, OTHER};

    static final AccountType[] fromAsset = new AccountType[]{EXPENSE, ASSET, LIABILITY, OTHER, INCOME};
    static final AccountType[] fromLiability = new AccountType[]{EXPENSE, ASSET, LIABILITY, OTHER, INCOME};
    static final AccountType[] fromOther = new AccountType[]{ASSET, LIABILITY, OTHER, EXPENSE, INCOME};
    static final AccountType[] fromUnknow = new AccountType[]{ASSET, LIABILITY, OTHER, EXPENSE, INCOME};

    static public AccountType[] getSupportedType() {
        return supported;
    }

    static public AccountType find(String type) {
        if (INCOME.type.equals(type)) {
            return INCOME;
        } else if (EXPENSE.type.equals(type)) {
            return EXPENSE;
        } else if (ASSET.type.equals(type)) {
            return ASSET;
        } else if (LIABILITY.type.equals(type)) {
            return LIABILITY;
        } else if (OTHER.type.equals(type)) {
            return OTHER;
        }
        return UNKONW;
    }

    static public String getDisplay(I18N i18n, String type) {
        AccountType at = find(type);
        switch (at) {
            case INCOME:
                return i18n.string(R.string.label_income);
            case EXPENSE:
                return i18n.string(R.string.label_expense);
            case ASSET:
                return i18n.string(R.string.label_asset);
            case LIABILITY:
                return i18n.string(R.string.label_liability);
            case OTHER:
                return i18n.string(R.string.label_other);
            default:
                return i18n.string(R.string.label_unknown);
        }
    }

    public static AccountType[] getFromType() {
        return from;
    }

    public static AccountType[] getToType(String fromType) {
        AccountType at = find(fromType);
        switch (at) {
            case INCOME:
                return fromIncome;
            case EXPENSE:
                return fromExpense;
            case ASSET:
                return fromAsset;
            case LIABILITY:
                return fromLiability;
            case OTHER:
                return fromOther;
            default:
                return fromUnknow;
        }
    }
    public static boolean isPositive(String type){
        return isPositive(find(type));
    }
    public static boolean isPositive(AccountType type){
        switch (type) {
            case INCOME:
            case LIABILITY:
                return false;
            case UNKONW:
            case EXPENSE:
            case ASSET:
            case OTHER:
            default:
                return true;
        }
    }


}
