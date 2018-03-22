package com.colaorange.dailymoney.data;

import com.colaorange.commons.util.Files;
import com.colaorange.commons.util.Logger;
import com.colaorange.dailymoney.context.Contexts;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

/**
 * Backup & Restore util
 * <p>
 * Created by Dennis
 */

public class BackupRestorer {

    private static final String DB = "dm.db";
    private static final String DB_MASTER = "dm_master.db";

    private static Contexts contexts() {
        return Contexts.instance();
    }

    private static DateFormat getBackupDateFormat() {
        return new SimpleDateFormat("yyyy-MM-dd_HHmmss");
    }

    /**
     */
    public static int copyDatabases(File sourceFolder, File targetFolder, Long timedup) throws IOException {

        int count = 0;

        File db = new File(sourceFolder, DB);
        File dbmaster = new File(sourceFolder, DB_MASTER);
        if (db.exists() && dbmaster.exists()) {
            Files.copyFileTo(db, new File(targetFolder, DB));
            Files.copyFileTo(dbmaster, new File(targetFolder, DB_MASTER));
            count += 2;
            if (timedup != null) {
                String duppost = "." + getBackupDateFormat().format(System.currentTimeMillis()) + ".bak";
                Files.copyFileTo(db, new File(targetFolder, DB + duppost));
                Files.copyFileTo(dbmaster, new File(targetFolder, DB_MASTER + duppost));
                count += 2;
            }
        } else {
            if (!db.exists()) {
                Logger.w("no db file " + db);
            }
            if (!dbmaster.exists()) {
                Logger.w("no dbmaster file " + dbmaster);
            }
        }
        return count;
    }


    public static int copyPrefFile(File sourceFolder, File targetFolder, Long timedup) throws IOException {
        int count = 0;
        final String prefName = contexts().getAppId() + "_preferences.xml";

        File pref = new File(sourceFolder, prefName);
        if (pref.exists()) {
            Files.copyFileTo(pref, new File(targetFolder, prefName));
            count++;
            if (timedup != null) {
                String dupname = prefName + "." + getBackupDateFormat().format(System.currentTimeMillis()) + ".bak";
                Files.copyFileTo(pref, new File(targetFolder, dupname));
                count++;
            }
        } else {
            Logger.w("no preference file " + pref);
        }
        return count;
    }

    public static boolean hasBackup() {
        try {
            if (contexts().hasWorkingFolderPermission()) {
                List<String> dbs = Arrays.asList(contexts().getWorkingFolder().list());
                return dbs.contains(DB) && dbs.contains(DB_MASTER);
            } else {
                return false;
            }
        }catch(Exception x){
            Logger.w(x.getMessage(),x);
            return false;
        }
    }
}
