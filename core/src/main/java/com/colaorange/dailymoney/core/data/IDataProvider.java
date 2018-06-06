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
    
    int LIST_RECORD_MODE_BOTH = 0;
    int LIST_RECORD_MODE_FROM = 1;
    int LIST_RECORD_MODE_TO = 2;
    

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


    List<Record> searchRecord(SearchCondition condition, int max);

    public class SearchCondition{
        String fromAccountId = null;
        String toAccountId = null;
        Date fromDate = null;
        Date toDate = null;
        Double fromMoney = null;
        Double toMoney = null;
        String note = null;

        public String getFromAccountId() {
            return fromAccountId;
        }

        public SearchCondition withFromAccountId(String fromAccountId) {
            this.fromAccountId = fromAccountId;
            return this;
        }

        public String getToAccountId() {
            return toAccountId;
        }

        public SearchCondition withToAccountId(String toAccountId) {
            this.toAccountId = toAccountId;
            return this;
        }

        public Date getFromDate() {
            return fromDate;
        }

        public SearchCondition withFromDate(Date fromDate) {
            this.fromDate = fromDate;
            return this;
        }

        public Date getToDate() {
            return toDate;
        }

        public SearchCondition withToDate(Date toDate) {
            this.toDate = toDate;
            return this;
        }

        public Double getFromMoney() {
            return fromMoney;
        }

        public SearchCondition withFromMoney(Double fromMoney) {
            this.fromMoney = fromMoney;
            return this;
        }

        public Double getToMoney() {
            return toMoney;
        }

        public SearchCondition withToMoney(Double toMoney) {
            this.toMoney = toMoney;
            return this;
        }

        public String getNote() {
            return note;
        }

        public SearchCondition withNote(String note) {
            this.note = note;
            return this;
        }
    }


    
    Record getFirstRecord();

    double sumInitialValue(AccountType type);
    
}
