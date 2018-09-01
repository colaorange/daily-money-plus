package com.colaorange.dailymoney.core.xlsx;

import com.colaorange.dailymoney.core.data.Account;
import com.colaorange.dailymoney.core.data.AccountType;
import com.colaorange.dailymoney.core.util.I18N;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.Map;

/**
 * Created by Dennis
 */
public class XlsxUtil {

    //https://github.com/ClosedXML/ClosedXML/wiki/Excel-Indexed-Colors
    public static final short INCOME_LIGHT_COLOR = IndexedColors.LIGHT_GREEN.getIndex();
    public static final short ASSET_LIGHT_COLOR = IndexedColors.LIGHT_YELLOW.getIndex();
    public static final short EXPENSE_LIGHT_COLOR = IndexedColors.ROSE.getIndex();
    public static final short LIABILITY_LIGHT_COLOR = IndexedColors.PALE_BLUE.getIndex();
    public static final short OTHER_LIGHT_COLOR = IndexedColors.LIGHT_TURQUOISE.getIndex();
    public static final short UNKNOW_LIGHT_COLOR = IndexedColors.GREY_25_PERCENT.getIndex();

    public static final int WIDTH_BASE = 256;

    public static int characterToWidth(int i) {
        return i * WIDTH_BASE;
    }


    public static CellStyle newAccountFillStyle(Workbook workbook, Map<String, CellStyle> reuse, CellStyle accountStyle, String accountType) {
        CellStyle style = reuse.get(accountType);
        if(style!=null){
            return style;
        }
        style = workbook.createCellStyle();
        if(accountStyle!=null) {
            style.cloneStyleFrom(accountStyle);
        }
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        AccountType type = AccountType.find(accountType);
        switch(type){
            case INCOME:
                style.setFillForegroundColor(INCOME_LIGHT_COLOR);
                break;
            case EXPENSE:
                style.setFillForegroundColor(EXPENSE_LIGHT_COLOR);
                break;
            case ASSET:
                style.setFillForegroundColor(ASSET_LIGHT_COLOR);
                break;
            case LIABILITY:
                style.setFillForegroundColor(LIABILITY_LIGHT_COLOR);
                break;
            case OTHER:
                style.setFillForegroundColor(OTHER_LIGHT_COLOR);
                break;
            case UNKONW:
            default:
                style.setFillForegroundColor(UNKNOW_LIGHT_COLOR);
                break;
        }
        reuse.put(accountType, style);
        return style;
    }

    public static String toAccountDisplay(I18N i18n, Map<String, Account> accountMap, String accountId) {
        Account account = null;
        if (accountMap != null) {
            account = accountMap.get(accountId);
        }
        if (account == null) {
            return accountId;
        }
        return AccountType.getDisplay(i18n, account.getType()) + "-" + account.getName();
    }
}
