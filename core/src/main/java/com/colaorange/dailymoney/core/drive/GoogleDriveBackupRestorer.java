package com.colaorange.dailymoney.core.drive;

import com.colaorange.commons.util.Files;
import com.colaorange.commons.util.Streams;
import com.colaorange.commons.util.Strings;
import com.colaorange.dailymoney.core.context.Contexts;
import com.colaorange.dailymoney.core.util.Logger;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Created by Dennis
 */
public class GoogleDriveBackupRestorer {

    public static final String DRIVE_APP_FOLDER_NAME = "coDailyMoneyBackup";

    GoogleDriveHelper gdHelper;

    DriveFolder appFolder;

    public GoogleDriveBackupRestorer(GoogleDriveHelper gdHelper) {
        this.gdHelper = gdHelper;
    }

    public static class BackupResult {
        int count;
        String err;
        String fileName;

        public boolean isSuccess() {
            return err == null;
        }

        public int getCount() {
            return count;
        }

        public String getErr() {
            return err;
        }

        public String getFileName() {
            return fileName;
        }
    }

    public static class RestoreResult {
        int count;
        String err;
        File folder;

        public boolean isSuccess() {
            return err == null;
        }

        public int getCount() {
            return count;
        }

        public String getErr() {
            return err;
        }

        public File getFolder() {
            return folder;
        }
    }


    public BackupResult backup(File lastFolder) {
        return backup(lastFolder, new AtomicBoolean(false));
    }

    public BackupResult backup(File lastFolder, AtomicBoolean canceling) {
        BackupResult result = new BackupResult();

        DateFormat dateTimeFormat = Contexts.instance().getPreference().getDateTimeFormat();
        String fileName = result.fileName = Strings.format("DM-{}.zip", dateTimeFormat.format(System.currentTimeMillis()));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zipOs = new ZipOutputStream(baos);


        int BUFFER_SIZE = 1024;
        int basePathLength = lastFolder.getPath().length() + 1;
        File[] fileList = lastFolder.listFiles();
        InputStream is = null;
        try {
            for (File file : fileList) {
                if (file.isFile()) {
                    byte data[] = new byte[BUFFER_SIZE];
                    String unmodifiedFilePath = file.getPath();
                    String relativePath = unmodifiedFilePath
                            .substring(basePathLength);
                    is = new FileInputStream(unmodifiedFilePath);

                    ZipEntry entry = new ZipEntry(relativePath);
                    entry.setTime(file.lastModified()); // to keep modification time after unzipping
                    zipOs.putNextEntry(entry);

                    Streams.flush(is, zipOs);

                    is.close();

                    result.count++;
                    checkCanceling(canceling);
                }
            }

            zipOs.close();


            byte[] data = baos.toByteArray();

            DriveFolder appFolder = getAppFolder();

            checkCanceling(canceling);

            gdHelper.writeFile(appFolder, fileName, new ByteArrayInputStream(data));
        } catch (Exception e) {
            Logger.e(e.getMessage(), e);
            result.err = e.getMessage();
            if (result.err == null) {
                result.err = "Error, no message";
            }
        }
        return result;
    }

    public DriveFolder getAppFolder() throws ExecutionException, InterruptedException {
        if (appFolder == null) {
            appFolder = gdHelper.retrieveFolder(null, DRIVE_APP_FOLDER_NAME, true);
        }
        return appFolder;
    }

    public RestoreResult restore(DriveFile driveFile) {
        RestoreResult result = new RestoreResult();
        FileOutputStream fos = null;
        FileInputStream fis = null;
        File tempzip = null;
        File tempfolder = null;
        try {
            File temp = new File(Contexts.instance().getWorkingFolder(), "temp");
            temp.mkdir();
            result.folder = tempfolder = new File(temp, Strings.randomName(5));
            tempzip = new File(temp, tempfolder.getName() + ".zip");
            fos = new FileOutputStream(tempzip);
            gdHelper.readFile(driveFile, fos);
            fos.close();
            fos = null;

            ZipInputStream zipIs = new ZipInputStream(new BufferedInputStream(fis = new FileInputStream(tempzip)));

            byte[] buffer = new byte[1024];
            int count;
            ZipEntry entry;
            while ((entry = zipIs.getNextEntry()) != null) {
                // zapis do souboru
                String filename = entry.getName();

                // Need to create directories if not exists, or
                // it will generate an Exception...
                File file;
                if (entry.isDirectory()) {
                    file = new File(tempfolder, filename);
                    file.mkdirs();
                    continue;
                }

                file = new File(tempfolder, filename);
                file.getParentFile().mkdirs();

                fos = new FileOutputStream(file);

                while ((count = zipIs.read(buffer)) != -1) {
                    fos.write(buffer, 0, count);
                }
                fos.close();
                fos = null;

                zipIs.closeEntry();
                result.count++;
            }

            zipIs.close();
        } catch (Exception e) {
            result.err = e.getMessage();
            Logger.e(e.getMessage(), e);
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e1) {
                }
            }
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e1) {
                }
            }
            if (tempzip != null && tempzip.isFile()) {
                tempzip.delete();
            }
        }

        return result;
    }

    private static void checkCanceling(AtomicBoolean canceling) {
        if (canceling!=null && canceling.get()) {
            throw new RuntimeException("task is canceling, break it");
        }
    }
}
