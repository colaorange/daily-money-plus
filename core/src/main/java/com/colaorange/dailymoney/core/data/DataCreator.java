package com.colaorange.dailymoney.core.data;

import java.util.Date;

import com.colaorange.commons.util.CalendarHelper;
import com.colaorange.dailymoney.core.util.I18N;
import com.colaorange.dailymoney.core.util.Logger;
import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.context.Contexts;

/**
 * 
 * @author dennis
 * 
 */
public class DataCreator {

    I18N i18n;
    IDataProvider idp;

    public DataCreator(IDataProvider idp, I18N i18n) {
        this.idp = idp;
        this.i18n = i18n;
    }

    public void createDefaultAccount() {
        createAccountNoThrow(i18n.string(R.string.defacc_salary), AccountType.INCOME, 0D,false);
        createAccountNoThrow(i18n.string(R.string.defacc_otherincome), AccountType.INCOME, 0D,false);

        createAccountNoThrow(i18n.string(R.string.defacc_food1), AccountType.EXPENSE, 0D,false);
        createAccountNoThrow(i18n.string(R.string.defacc_food2), AccountType.EXPENSE, 0D,false);
        createAccountNoThrow(i18n.string(R.string.defacc_clothing), AccountType.EXPENSE, 0D,false);
        createAccountNoThrow(i18n.string(R.string.defacc_housing), AccountType.EXPENSE, 0D,false);
        createAccountNoThrow(i18n.string(R.string.defacc_transportation), AccountType.EXPENSE, 0D,false);
        createAccountNoThrow(i18n.string(R.string.defacc_education), AccountType.EXPENSE, 0D,false);
        createAccountNoThrow(i18n.string(R.string.defacc_entertainment), AccountType.EXPENSE, 0D,false);
        createAccountNoThrow(i18n.string(R.string.defacc_otherexpense), AccountType.EXPENSE, 0D,false);


        createAccountNoThrow(i18n.string(R.string.defacc_cash), AccountType.ASSET, 0D,true);
        createAccountNoThrow(i18n.string(R.string.defacc_bank), AccountType.ASSET, 0D,false);
        
        createAccountNoThrow(i18n.string(R.string.defacc_creditcard), AccountType.LIABILITY, 0D,false);
    }

    public void createTestData(int loop) {
        //only for call from ui, so use uiInstance
        CalendarHelper cal = Contexts.instance().getCalendarHelper();
        
        Account income1 = createAccountNoThrow(i18n.string(R.string.defacc_salary), AccountType.INCOME, 0D,false);
        Account income2 = createAccountNoThrow(i18n.string(R.string.defacc_otherincome), AccountType.INCOME, 0D,false);

        Account expense1 = createAccountNoThrow(i18n.string(R.string.defacc_food1), AccountType.EXPENSE, 0D,false);
        Account expense2 = createAccountNoThrow(i18n.string(R.string.defacc_entertainment), AccountType.EXPENSE, 0D,false);
        Account expense3 = createAccountNoThrow(i18n.string(R.string.defacc_otherexpense), AccountType.EXPENSE, 0D,false);

        Account asset1 = createAccountNoThrow(i18n.string(R.string.defacc_cash), AccountType.ASSET, 5000D,true);
        Account asset2 = createAccountNoThrow(i18n.string(R.string.defacc_bank), AccountType.ASSET, 100000D,false);
        
        Account liability1 = createAccountNoThrow(i18n.string(R.string.defacc_creditcard), AccountType.LIABILITY, 0D,false);
        
        Account other1 = createAccountNoThrow("Other", AccountType.OTHER, 0D,false);

        Date today = new Date();
        
        int base = 0;

        for(int i=0;i<loop;i++){
            createDetail(income1, asset1, cal.dateBefore(today, base + 3), 5000D, "salary "+i);
            createDetail(income2, asset2, cal.dateBefore(today, base + 3), 10D, "some where "+i);
    
            createDetail(asset1, expense1, cal.dateBefore(today, base + 2), 100D, "date with Cica "+i);
            createDetail(asset1, expense1, cal.dateBefore(today,base + 2), 30D, "taiwan food is great "+i);
            createDetail(asset1, expense2, cal.dateBefore(today,base + 1), 11D, "buy DVD "+i);
            createDetail(asset1, expense3, cal.dateBefore(today,base + 1), 300D, "it is a secrt  "+i);
    
            createDetail(asset1, asset2, cal.dateBefore(today, base+0), 4000D, "deposit  "+i);
            createDetail(asset2, asset1, cal.dateBefore(today, base+0), 1000D, "drawing  "+i);
            
            createDetail(liability1, expense2, cal.dateBefore(today, base+2), 20.9D, "buy Game "+i);
            createDetail(asset1, liability1, cal.dateBefore(today, base+1), 19.9D, "pay credit card "+i);
            createDetail(asset1, other1, cal.dateBefore(today, base+1), 1D, "salary to other "+i);
            createDetail(other1, liability1, cal.dateBefore(today, base+1), 1D, "other pay credit card "+i);
            
            base = base+5;
        }

    }

    private Record createDetail(Account from, Account to, Date date, Double money, String note) {
        Record det = new Record(from.getId(),to.getId(), date, money, note);
        idp.newRecord(det);
        return det;
    }

    private Account createAccountNoThrow(String name, AccountType type,double initval, boolean cashAccount) {
        try {
            Account account = null;
            if ((account = idp.findAccount(type.getType(), name)) == null) {
                Logger.d("createDefaultAccount : {}", name);

                account = new Account(type.getType(), name, initval);
                account.setCashAccount(cashAccount);
                idp.newAccount(account);
            }
            return account;
        } catch (DuplicateKeyException e) {
            Logger.e(e.getMessage(), e);
        }
        return null;
    }
}
