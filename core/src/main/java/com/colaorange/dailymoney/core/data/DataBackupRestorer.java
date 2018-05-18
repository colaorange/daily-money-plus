package com.colaorange.dailymoney.core.data;

import com.colaorange.commons.util.Files;
import com.colaorange.dailymoney.core.util.Logger;
import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.context.Contexts;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Backup & Restore util
 * <p>
 * @author Dennis
 */

public class DataBackupRestorer {

    private static final String DB = "dm.db";
    private static final String DB_PRE = "dm_";
    private static final String DB_POS = ".db";

    private static final String BACKUP_FOLER = "backup";
    private static final String LAST_FOLER = "last";

    private static Contexts contexts() {
        return Contexts.instance();
    }

    public static class Result {
        boolean success = false;
        int db = 0;
        int pref = 0;
        String err;
        File lastFolder;

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

        public File getLastFolder() {
            return lastFolder;
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
        long now = System.currentTimeMillis();
        try {
            File backupFolder = new File(ctxs.getWorkingFolder(), BACKUP_FOLER);
            File lastFolder = r.lastFolder = new File(backupFolder, LAST_FOLER);

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


            File dbFolder = ctxs.getAppDbFolder();
            File prefFolder = ctxs.getAppPrefFolder();

            File withTimeFolder = null;
            if (ctxs.getPreference().isBackupWithTimestamp()) {
                withTimeFolder = new File(backupFolder, ctxs.getPreference().getBackupMonthFormat().format(now));
                withTimeFolder = new File(withTimeFolder, ctxs.getPreference().getBackupDateTimeFormat().format(now));
                if (!withTimeFolder.exists()) {
                    withTimeFolder.mkdirs();
                }
            }

            //backup db
            File[] dbfs = dbFolder.listFiles(new DBFileFilter());
            if (dbfs != null) {//just in case
                for (File dbf : dbfs) {
                    File tf;
                    Files.copyFileTo(dbf, tf = new File(lastFolder, dbf.getName()));
                    Logger.d("backup db " + dbf + " to " + tf);
                    if (withTimeFolder != null) {
                        Files.copyFileTo(dbf, tf = new File(withTimeFolder, dbf.getName()));
                        Logger.d("backup db " + dbf + " to " + tf);
                    }
                    r.db++;
                }
            }

            //backup preference
            File[] prefs = prefFolder.listFiles(new PrefFileFilter());
            if (prefs != null) {//just in case
                for (File pref : prefs) {
                    File tf;
                    Files.copyFileTo(pref, tf = new File(lastFolder, pref.getName()));
                    Logger.d("backup pref " + pref + " to " + tf);
                    if (withTimeFolder != null) {
                        Files.copyFileTo(pref, tf = new File(withTimeFolder, pref.getName()));
                        Logger.d("backup pref " + pref + " to " + tf);
                    }
                    r.pref++;
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
            File backupFolder = new File(ctxs.getWorkingFolder(), BACKUP_FOLER);
            File lastFolder = new File(backupFolder, LAST_FOLER);
            if (!(lastFolder.exists() && lastFolder.isDirectory() && lastFolder.listFiles().length > 0)) {
                lastFolder = ctxs.getWorkingFolder();
            }
            r.lastFolder = lastFolder;

            File dbFolder = ctxs.getAppDbFolder();
            File prefFolder = ctxs.getAppPrefFolder();

            DBFileFilter dbFileFilter = new DBFileFilter();
            PrefFileFilter prefFileFilter = new PrefFileFilter();

            Set<String> filesToRemove = new LinkedHashSet<>();
            //remove old db, for full backup
            //if we don't it is possible copy 2 file, but there are many other db files in dbfolder
            if (lastFolder.listFiles(dbFileFilter).length > 0) {
                for (File f : dbFolder.listFiles(dbFileFilter)) {
                    filesToRemove.add(f.getAbsolutePath());
                }
            }
            //remove old db, for full backup
            //if we don't it is possible copy 2 file, but there are many other db files in dbfolder
            if (lastFolder.listFiles(prefFileFilter).length > 0) {
                for (File f : prefFolder.listFiles(prefFileFilter)) {
                    filesToRemove.add(f.getAbsolutePath());
                }
            }

            //restore db
            for (File dbfile : lastFolder.listFiles(dbFileFilter)) {
                File targetfile;
                Files.copyFileTo(dbfile, targetfile = new File(dbFolder, dbfile.getName()));
                Logger.d("restore db " + dbfile + " to " + targetfile);
                filesToRemove.remove(targetfile.getAbsolutePath());
                r.db++;
            }


            //restore preference
            for (File preffile : lastFolder.listFiles(prefFileFilter)) {
                File targetfile;
                Files.copyFileTo(preffile, targetfile = new File(prefFolder, preffile.getName()));
                Logger.d("restore pref " + preffile + " to " + targetfile);
                filesToRemove.remove(targetfile.getAbsolutePath());
                r.pref++;
            }

            for(String f:filesToRemove){
                new File(f).delete();
                Logger.d("delete unnecessary db/pref file " + f);
            }

            r.success = true;
        } catch (Exception x) {
            Logger.e(x.getMessage(), x);
            r.err = x.getMessage();
            r.success = false;
        }
        contexts().setWorkingBookId(Contexts.DEFAULT_BOOK_ID);
        return r;
    }
}
