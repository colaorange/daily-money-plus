package com.colaorange.dailymoney.data;

import com.colaorange.commons.util.Files;
import com.colaorange.commons.util.Logger;
import com.colaorange.dailymoney.R;
import com.colaorange.dailymoney.context.Contexts;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.List;

/**
 * Backup & Restore util
 * <p>
 * Created by Dennis
 */

public class BackupRestorer {

    private static final String DB = "dm.db";
    private static final String DB_PRE = "dm_";
    private static final String DB_POS = ".db";

    private static Contexts contexts() {
        return Contexts.instance();
    }

    public static class Result {
        boolean success = false;
        int db = 0;
        int pref = 0;
        String err;

        public boolean isSuccess() {
            return success;
        }

        public int getDb() {
            return db;
        }

        public int getPref() {
            return pref;
        }

        public String getErr() {
            return err;
        }
    }

    public static boolean hasBackup() {
        try {
            if (contexts().hasWorkingFolderPermission()) {
                List<String> dbs = Arrays.asList(contexts().getWorkingFolder().list());
                return dbs.contains(DB);
            } else {
                return false;
            }
        } catch (Exception x) {
            Logger.w(x.getMessage(), x);
            return false;
        }
    }

    private static class DBFileFilter implements FileFilter {

        @Override
        public boolean accept(File file) {
            String nm = file.getName();
            return file.isFile() && (nm.equals(DB) || (nm.startsWith(DB_PRE) && nm.endsWith(DB_POS)));
        }
    }

    private static class PrefFileFilter implements FileFilter {

        @Override
        public boolean accept(File file) {
            String nm = file.getName();
            return file.isFile() && (nm.equals(contexts().getAppId() + "_preferences.xml"));
        }
    }

    public static Result backup() {
        Result r = new Result();
        Contexts ctxs = contexts();
        if (!ctxs.hasWorkingFolderPermission()) {
            r.err = ctxs.getI18n().string(R.string.msg_working_folder_no_access);
            return r;
        }
        boolean bakcupWithTime = ctxs.getPreference().isBackupWithTimestamp();
        try {
            File workingFolder = ctxs.getWorkingFolder();
            File dbFolder = ctxs.getAppDbFolder();
            File prefFolder = ctxs.getAppPrefFolder();

            File withTimeFolder = null;
            if (bakcupWithTime) {
                withTimeFolder = new File(workingFolder, "backup-with-timestamp");
                if (!withTimeFolder.exists()) {
                    withTimeFolder.mkdir();
                }
            }
            String timestamp = ctxs.getPreference().getBackupDateTimeFormat().format(System.currentTimeMillis());

            //backup db
            File[] dbfs = dbFolder.listFiles(new DBFileFilter());
            if (dbfs != null) {//just in case
                for (File dbf : dbfs) {
                    File tf;
                    Files.copyFileTo(dbf, tf = new File(workingFolder, dbf.getName()));
                    Logger.d("backup " + dbf + " to " + tf);
                    if (bakcupWithTime) {
                        Files.copyFileTo(dbf, tf = new File(withTimeFolder, timestamp + "." + dbf.getName()));
                        Logger.d("backup " + dbf + " to " + tf);
                    }
                    r.db++;
                }
            }

            //backup preference
            File[] prefs = prefFolder.listFiles(new PrefFileFilter());
            if (prefs != null) {//just in case
                for (File pref : prefs) {
                    File tf;
                    Files.copyFileTo(pref, tf = new File(workingFolder, pref.getName()));
                    Logger.d("backup " + pref + " to " + tf);
                    if (bakcupWithTime) {
                        Files.copyFileTo(pref, tf = new File(withTimeFolder, timestamp + "." + pref.getName()));
                        Logger.d("backup " + pref + " to " + tf);
                    }
                    r.pref ++;
                }
            }
            r.success = true;
        } catch (Exception x) {
            Logger.e(x.getMessage(), x);
            r.err = x.getMessage();
            r.success = false;
        }
        return r;
    }

    public static Result restore() {
        Result r = new Result();
        Contexts ctxs = contexts();
        if (!contexts().hasWorkingFolderPermission() && !hasBackup()) {
            r.err = ctxs.getI18n().string(R.string.msg_working_folder_no_access, contexts().getWorkingFolder());
            return r;
        }
        try {
            File workingFolder = ctxs.getWorkingFolder();
            File dbFolder = ctxs.getAppDbFolder();
            File prefFolder = ctxs.getAppPrefFolder();

            //restore db
            File[] dbfs = workingFolder.listFiles(new DBFileFilter());
            if (dbfs != null) {//just in case
                for (File dbf : dbfs) {
                    File tf;
                    Files.copyFileTo(dbf, tf = new File(dbFolder, dbf.getName()));
                    Logger.d("restore " + dbf + " to " + tf);
                    r.db++;
                }
            }

            //restore preference
            File[] prefs = workingFolder.listFiles(new PrefFileFilter());
            if (prefs != null) {//just in case
                for (File pref : prefs) {
                    File tf;
                    Files.copyFileTo(pref, tf = new File(prefFolder, pref.getName()));
                    Logger.d("restore " + pref + " to " + tf);
                    r.pref++;
                }
            }
            r.success = true;
        } catch (Exception x) {
            Logger.e(x.getMessage(), x);
            r.err = x.getMessage();
            r.success = false;
        }
        contexts().setWorkingBookId(Contexts.WORKING_BOOK_DEFAULT);
        return r;
    }
}
