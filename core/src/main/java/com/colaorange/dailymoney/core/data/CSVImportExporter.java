package com.colaorange.dailymoney.core.data;

import com.colaorange.commons.util.Files;
import com.colaorange.commons.util.Formats;
import com.colaorange.dailymoney.core.util.I18N;
import com.colaorange.dailymoney.core.util.Logger;
import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.context.Contexts;
import com.colaorange.dailymoney.core.context.Preference;
import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Dennis
 */
public class CSVImportExporter {
    private static final String APPVER = "appver:";
    private static final String CSV_FOLER = "csv";
    private static final String LAST_FOLER = "last";

    private static Contexts contexts() {
        return Contexts.instance();
    }

    public enum ExportMode {
        ALL_BOOKS, WORKING_BOOK, WORKING_BOOK_ACCOUNT
    }

    public enum ImportMode {
        WORKING_BOOK, WORKING_BOOK_ACCOUNT
    }


    public static class Result {
        boolean success = false;
        int book = 0;
        int detail = 0;
        int account = 0;
        List<File> files;
        File lastFolder;
        String err;

        public boolean isSuccess() {
            return success;
        }

        public int getBook() {
            return book;
        }

        public int getDetail() {
            return detail;
        }

        public int getAccount() {
            return account;
        }

        public String getErr() {
            return err;
        }

        public List<File> getFiles() {
            return files;
        }

        public File getLastFolder() {
            return lastFolder;
        }

        public void addFile(File f) {
            if (files == null) {
                files = new LinkedList<>();
            }
            files.add(f);
        }
    }

    /**
     * @param mode
     * @return
     */
    public Result exportIt(ExportMode mode) {
        I18N i18n = contexts().getI18n();
        Preference preference = contexts().getPreference();
        Result r = new Result();
        int workingBookId = contexts().getWorkingBookId();
        boolean accountOnly = false;
        int[] bookIds = null;
        if (mode == ExportMode.WORKING_BOOK || mode == ExportMode.WORKING_BOOK_ACCOUNT) {
            bookIds = new int[]{workingBookId};
            if (mode == ExportMode.WORKING_BOOK_ACCOUNT) {
                accountOnly = true;
            }
        } else {
            List<Book> books = contexts().getMasterDataProvider().listAllBook();
            bookIds = new int[books.size()];
            int i = 0;
            for (Book b : books) {
                bookIds[i++] = b.getId();
            }
        }

        if (bookIds.length == 0) {
            r.err = i18n.string(R.string.msg_no_data_to_backup);
        } else {
            long now = System.currentTimeMillis();

            File csvFolder = new File(contexts().getWorkingFolder(), CSV_FOLER);
            File lastFolder = r.lastFolder = new File(csvFolder, "last");

            if (!lastFolder.exists()) {
                lastFolder.mkdirs();
            } else if (!lastFolder.isDirectory()) {
                r.err = "last folder is not directory";
                return r;
            }
            for (File f : lastFolder.listFiles()) {
                if (f.isFile()) {
                    f.delete();
                }
            }

            File withTimeFolder = null;
            if (preference.isBackupWithTimestamp()) {
                withTimeFolder = new File(csvFolder, preference.getBackupMonthFormat().format(now));
                withTimeFolder = new File(withTimeFolder, preference.getBackupDateTimeFormat().format(now));
                if (!withTimeFolder.exists()) {
                    withTimeFolder.mkdirs();
                }
            }

            for (int bookId : bookIds) {
                exportIt(r, lastFolder, workingBookId, bookId, accountOnly, withTimeFolder);
                if (!r.success) {
                    break;
                }
            }
            Logger.d("Exported " + r.getBook() + ", " + r.getDetail() + ", " + r.getAccount());
        }

        return r;
    }

    private void exportIt(Result r, File lastFolder, int workingBookId, int bookId, boolean accountOnly, File withTimeFolder) {
        I18N i18n = contexts().getI18n();
        int vercode = contexts().getAppVerCode();
        String encoding = contexts().getPreference().getCSVEncoding();

        IDataProvider idp = contexts().newDataProvider(bookId);
        try {

            StringWriter sw;
            CsvWriter csvw;
            String csvStr;
            int detailCount = 0;

            File file;

            if (!accountOnly) {
                sw = new StringWriter();
                csvw = new CsvWriter(sw, ',');
                csvw.writeRecord(new String[]{"id", "from", "to", "date", "value", "note", "archived", APPVER + vercode});
                for (Record d : idp.listAllRecord()) {
                    detailCount++;
                    csvw.writeRecord(new String[]{Integer.toString(d.getId()), d.getFrom(), d.getTo(),
                            Formats.normalizeDate2String(d.getDate()), Formats.normalizeDouble2String(d.getMoney()),
                            d.getNote(), d.isArchived() ? "1" : "0"});
                }
                csvw.close();
                csvStr = sw.toString();

                if (workingBookId == bookId) {
                    saveCSVFile(csvStr, encoding, file = new File(lastFolder, "details.csv"), withTimeFolder);
                    r.addFile(file);
                }
                saveCSVFile(csvStr, encoding, file = new File(lastFolder, "details-" + bookId + ".csv"), withTimeFolder);
                r.addFile(file);
            }


            int accountCount = 0;

            sw = new StringWriter();
            csvw = new CsvWriter(sw, ',');
            csvw.writeRecord(new String[]{"id", "type", "name", "init", "cash", APPVER + vercode});
            for (Account a : idp.listAccount(null)) {
                accountCount++;
                csvw.writeRecord(new String[]{a.getId(), a.getType(), a.getName(), Formats.normalizeDouble2String(a.getInitialValue()), a.isCashAccount() ? "1" : "0"});
            }
            csvw.close();
            csvStr = sw.toString();

            if (workingBookId == bookId) {
                saveCSVFile(csvStr, encoding, file = new File(lastFolder, "accounts.csv"), withTimeFolder);
                r.addFile(file);
            }
            saveCSVFile(csvStr, encoding, file = new File(lastFolder, "accounts-" + bookId + ".csv"), withTimeFolder);
            r.addFile(file);

            r.book++;
            r.detail += detailCount;
            r.account += accountCount;
            r.success = true;
            Logger.d("Exported " + detailCount + "," + accountCount);

        } catch (Exception x) {
            Logger.e(x.getMessage(), x);
            r.success = false;
            r.err = x.getMessage();
        } finally {
            try {
                idp.close();
            } catch (Exception x) {
                Logger.w(x.getMessage(), x);
            }
        }
    }

    private void saveCSVFile(String csv, String encoding, File file, File withTimeFolder) throws IOException {

        Files.saveString(csv, file, encoding);

        Logger.d("export to {}", file);

        if (withTimeFolder != null) {
            Files.copyFileTo(file, new File(withTimeFolder, file.getName()));
        }

    }

    public Result importIt(ImportMode mode) {
        I18N i18n = contexts().getI18n();
        Preference preference = contexts().getPreference();
        int workingBookId = contexts().getWorkingBookId();
        Result r = new Result();
        boolean accountOnly = false;

        if (mode == ImportMode.WORKING_BOOK_ACCOUNT) {
            accountOnly = true;
        }

        File csvFolder = new File(contexts().getWorkingFolder(), "csv");
        File lastFolder = new File(csvFolder, "last");
        if (!(lastFolder.exists() && lastFolder.isDirectory() && lastFolder.listFiles().length > 0)) {
            lastFolder = contexts().getWorkingFolder();
        }
        r.lastFolder = lastFolder;

        IDataProvider idp = contexts().newDataProvider(workingBookId);
        CsvReader accountReader = null;
        CsvReader detailReader = null;
        String csvEncoding = preference.getCSVEncoding();
        try {
            File accounts = new File(lastFolder, "accounts.csv");
            File details = new File(lastFolder, "details.csv");
            if (!accounts.exists()) {
                r.err = i18n.string(R.string.msg_no_csv) + " : " + accounts.getName();
                return r;
            }
            if (!accountOnly && !accounts.exists()) {
                r.err = i18n.string(R.string.msg_no_csv) + " : " + details.getName();
                return r;
            }

            accountReader = new CsvReader(new InputStreamReader(new FileInputStream(accounts), csvEncoding));
            if (!accountReader.readHeaders()) {
                r.err = i18n.string(R.string.msg_no_csv) + " : accounts no header";
                return r;
            }

            if (!accountOnly) {
                detailReader = new CsvReader(new InputStreamReader(new FileInputStream(details), csvEncoding));
                if (!detailReader.readHeaders()) {
                    r.err = i18n.string(R.string.msg_no_csv) + " : details no header";
                    return r;
                }
            }

            //accounts
            accountReader.setTrimWhitespace(true);
            int appver = getAppver(accountReader.getHeaders()[accountReader.getHeaderCount() - 1]);
            idp.deleteAllAccount();
            while (accountReader.readRecord()) {
                try {
                    String id = accountReader.get("id");

                    if (idp.findAccount(id) != null) {
                        //ignore existed
                        continue;
                    }

                    Account acc = new Account(accountReader.get("type"), accountReader.get("name"), Formats.normalizeString2Double(accountReader.get("init")));
                    String cash = accountReader.get("cash");
                    acc.setCashAccount("1".equals(cash));

                    idp.newAccountNoCheck(id, acc);
                    r.account++;
                } catch (Exception x) {
                    r.err = i18n.string(R.string.msg_no_csv) + " : csv format error : " + x.getMessage();
                    return r;
                }
            }
            accountReader.close();
            accountReader = null;
            Logger.d("import from {} ver: {}", accounts , appver);


            if (!accountOnly) {
                detailReader.setTrimWhitespace(true);
                appver = getAppver(detailReader.getHeaders()[detailReader.getHeaderCount() - 1]);

                //shouldn't delete in import, should append it.
//                idp.deleteAllRecord();
                while (detailReader.readRecord()) {
                    try {
                        Record det = new Record(detailReader.get("from"), detailReader.get("to"), Formats.normalizeString2Date(detailReader.get("date")), Formats.normalizeString2Double(detailReader.get("value")), detailReader.get("note"));
                        String archived = detailReader.get("archived");
                        if ("1".equals(archived)) {
                            det.setArchived(true);
                        } else if ("0".equals(archived)) {
                            det.setArchived(false);
                        } else {
                            det.setArchived(Boolean.parseBoolean(archived));
                        }

                        idp.newRecord(det);
                        r.detail++;
                    } catch (Exception x) {
                        r.err = i18n.string(R.string.msg_no_csv) + " : csv format error : " + x.getMessage();
                        return r;
                    }
                }
                detailReader.close();
                detailReader = null;

                Logger.d("import from {} ver:{}", details, appver);
            }
            r.success = true;
        } catch (Exception x) {
            Logger.e(x.getMessage(), x);
            r.success = false;
            r.err = x.getMessage();
        } finally {
            try {
                idp.close();
            } catch (Exception x) {
                Logger.w(x.getMessage(), x);
            }
            if (accountReader != null) {
                accountReader.close();
            }
            if (detailReader != null) {
                detailReader.close();
            }
        }

        return r;
    }

    private int getAppver(String str) {
        if (str != null && str.startsWith(APPVER)) {
            try {
                return Integer.parseInt(str.substring(APPVER.length()));
            } catch (Exception x) {
                Logger.d(x.getMessage(), x);
            }
        }
        return 0;
    }
}
