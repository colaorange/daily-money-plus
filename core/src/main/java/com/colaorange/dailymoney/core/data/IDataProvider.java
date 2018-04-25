/**
 * 
 */
package com.colaorange.dailymoney.core.data;

import java.util.Date;
import java.util.List;


/**
 * to provide all the data and operation also
 * @author dennis
 *
 */
public interface IDataProvider {
    
    int LIST_DETAIL_MODE_BOTH = 0;
    int LIST_DETAIL_MODE_FROM = 1;
    int LIST_DETAIL_MODE_TO = 2;
    

    void init();

    void destroyed();
    
    void reset();
    
    void deleteAllAccount();
    
    void deleteAllRecord();

    Account findAccount(String id);
    
    Account findAccount(String type,String name);
    
    String toAccountId(Account account);
    
    void newAccount(Account account) throws DuplicateKeyException;
    void newAccount(String id,Account account) throws DuplicateKeyException;
    void newAccountNoCheck(String id,Account account);
    
    boolean updateAccount(String id,Account account);
    
    boolean deleteAccount(String id);

    /**
     * list account by account type, if type null then return all account
     */
    List<Account> listAccount(AccountType type);
    
    
    /** record apis **/
    
    Record findRecord(int id);
    
    void newRecord(Record record);
    void newRecord(int id, Record record)throws DuplicateKeyException;
    void newRecordNoCheck(int id, Record record);
    
    boolean updateRecord(int id, Record record);
    
    boolean deleteRecord(int id);

    List<Record> listAllRecord();

    int countRecord(Date start, Date end);
    /**
     * mode : 0 both, 1 from, 2 to;
     */
    int countRecord(AccountType type, int mode, Date start, Date end);
    int countRecord(Account account, int mode, Date start, Date end);
    int countRecord(String accountId, int mode, Date start, Date end);
    
    List<Record> listRecord(Date start, Date end, int max);
    List<Record> listRecord(Date start, Date end, String note, int max);
    /**
     * mode : 0 both, 1 from, 2 to;
     */
    List<Record> listRecord(AccountType type, int mode, Date start, Date end, int max);
    List<Record> listRecord(Account account, int mode, Date start, Date end, int max);
    List<Record> listRecord(String accountId, int mode, Date start, Date end, int max);

    double sumFrom(AccountType type,Date start, Date end);
    double sumFrom(Account account,Date start, Date end);

    
    double sumTo(AccountType type,Date start, Date end);
    double sumTo(Account account,Date start, Date end);
    
    
    Record getFirstRecord();

    double sumInitialValue(AccountType type);
    
}
