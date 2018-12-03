package com.colaorange.dailymoney.core.drive;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.colaorange.commons.util.Streams;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.drive.query.SortOrder;
import com.google.android.gms.drive.query.SortableField;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by Dennis
 */
public class GoogleDriveHelper {

    private static GoogleSignInClient googleSignInClient;


    private final Context context;

    private GoogleSignInAccount googleSignInAccount;

    private DriveResourceClient driveResourceClient;
    private DriveClient driveClient;


    public GoogleDriveHelper(Context context, GoogleSignInAccount googleSignInAccount) {
        this.context = context;
        this.googleSignInAccount = googleSignInAccount;
    }

    public static synchronized GoogleSignInClient getSignInClient(final Context context) {
        if (googleSignInClient == null) {
            GoogleSignInOptions signInOptions =
                    new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestScopes(Drive.SCOPE_FILE)
                            .build();
            googleSignInClient = GoogleSignIn.getClient(context, signInOptions);
        }
        return googleSignInClient;
    }


    public static Task<GoogleDriveHelper> signIn(final Context context) {
        GoogleSignInClient gsiClient = getSignInClient(context);

        Task<GoogleSignInAccount> tgsia = gsiClient.silentSignIn();

        Task<GoogleDriveHelper> tgdh = tgsia.continueWith(new Continuation<GoogleSignInAccount, GoogleDriveHelper>() {
            @Override
            public GoogleDriveHelper then(@NonNull Task<GoogleSignInAccount> task) throws Exception {
                return new GoogleDriveHelper(context, task.getResult());
            }
        });

        return tgdh;
    }

    public static Intent getSignInIntent(Context context) {
        GoogleSignInClient gsiClient = getSignInClient(context);
        return gsiClient.getSignInIntent();
    }

    public static Task<Void> revokeAccess(Context context) {
        return getSignInClient(context).revokeAccess();
    }

    public boolean requestSync() {
        /*
        com.google.android.gms.common.api.ApiException: 1507: Sync request rate limit exceeded.
        java.util.concurrent.ExecutionException: com.google.android.gms.common.api.ApiException: 1507: Sync request rate limit exceeded.
        */
        try {
            Tasks.await(getDriveClient().requestSync());
            return true;
        } catch (Exception x) {
            return false;
        }
    }


    public GoogleSignInAccount getGoogleSignInAccount() {
        return googleSignInAccount;
    }

    public DriveResourceClient getDriveResourceClient() {
        return driveResourceClient != null ? driveResourceClient :
                (driveResourceClient = Drive.getDriveResourceClient(context, googleSignInAccount));
    }

    public DriveClient getDriveClient() {
        return driveClient != null ? driveClient :
                (driveClient = Drive.getDriveClient(context, googleSignInAccount));
    }


    public DriveFolder retrieveRoot() throws ExecutionException, InterruptedException {
        Task<DriveFolder> trf = getDriveResourceClient().getRootFolder();
        Tasks.await(trf);
        return trf.getResult();
    }

    public DriveFolder retrieveFolder(DriveFolder parent, String name, boolean create) throws ExecutionException, InterruptedException {
        if (parent == null) {
            parent = retrieveRoot();
        }

        Task<MetadataBuffer> tmb = getDriveResourceClient().queryChildren(parent,
                new Query.Builder()
                        .addFilter(Filters.eq(SearchableField.TITLE, name))
                        .addFilter(Filters.eq(SearchableField.TRASHED, false))
                        .setSortOrder(new SortOrder.Builder().addSortDescending(SortableField.CREATED_DATE).build())
                        .build());
        Tasks.await(tmb);

        MetadataBuffer mb = tmb.getResult();
        DriveFolder appFolder = null;
        try {
            Iterator<Metadata> iter = mb.iterator();
            while (iter.hasNext()) {
                Metadata data = iter.next();
                if (data.isFolder() && !data.isTrashed()) {
                    appFolder = data.getDriveId().asDriveFolder();
                    break;
                }
            }
        } finally {
            mb.release();
        }

        if (create && appFolder == null) {
            MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                    .setTitle(name)
                    .build();

            Task<DriveFolder> cft = getDriveResourceClient().createFolder(parent, changeSet);
            Tasks.await(cft);
            appFolder = cft.getResult();
        }
        return appFolder;
    }

    public DriveFile retrieveFile(DriveFolder parent, String fileName, boolean create) throws ExecutionException, InterruptedException {
        if (parent == null) {
            parent = retrieveRoot();
        }

        Task<MetadataBuffer> tmb = getDriveResourceClient().queryChildren(parent,
                new Query.Builder()
                        .addFilter(Filters.eq(SearchableField.TITLE, fileName))
                        .addFilter(Filters.eq(SearchableField.TRASHED, false))
                        .setSortOrder(new SortOrder.Builder().addSortDescending(SortableField.CREATED_DATE).build())
                        .build());
        Tasks.await(tmb);

        MetadataBuffer mb = tmb.getResult();
        DriveFile file = null;
        try {
            Iterator<Metadata> iter = mb.iterator();
            while (iter.hasNext()) {
                Metadata data = iter.next();
                if (!data.isFolder()) {
                    file = data.getDriveId().asDriveFile();
                    break;
                }
            }
        } finally {
            mb.release();
        }

        if (create && file == null) {

            Task<DriveContents> tdc = getDriveResourceClient().createContents();
            Tasks.await(tdc);
            DriveContents contents = tdc.getResult();

            MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                    .setTitle(fileName)
                    .build();

            Task<DriveFile> cft = getDriveResourceClient().createFile(parent, changeSet, contents);
            Tasks.await(cft);
            file = cft.getResult();
        }
        return file;
    }

    public DriveFile writeFile(DriveFile file, InputStream dataInputStream) throws ExecutionException, InterruptedException, IOException {
        Task<DriveContents> tdc = getDriveResourceClient().openFile(file, DriveFile.MODE_WRITE_ONLY);
        Tasks.await(tdc);
        DriveContents contents = tdc.getResult();
        OutputStream outputStream = contents.getOutputStream();

        Streams.flush(dataInputStream, outputStream);

        Task<Void> tdf = getDriveResourceClient().commitContents(contents, null);
        Tasks.await(tdf);

        return file;
    }

    public DriveFile renameFile(DriveFile file, String fileName) throws ExecutionException, InterruptedException, IOException {
        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                .setTitle(fileName)
                .build();

        Task<Metadata> tdf = getDriveResourceClient().updateMetadata(file, changeSet);
        Tasks.await(tdf);

        return tdf.getResult().getDriveId().asDriveFile();
    }

    public DriveFile writeFile(DriveFolder parent, String fileName, InputStream fileInputStream) throws ExecutionException, InterruptedException, IOException {
        Task<DriveContents> tdc = getDriveResourceClient().createContents();
        Tasks.await(tdc);
        DriveContents contents = tdc.getResult();
        OutputStream outputStream = contents.getOutputStream();

        Streams.flush(fileInputStream, outputStream);

        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                .setTitle(fileName)
                .build();

        Task<DriveFile> tdf = getDriveResourceClient().createFile(parent, changeSet, contents);
        Tasks.await(tdf);

        return tdf.getResult();
    }

    public void readFile(DriveFile file, OutputStream dataOutputStream) throws ExecutionException, InterruptedException, IOException {
        Task<DriveContents> tdc = getDriveResourceClient().openFile(file, DriveFile.MODE_READ_ONLY);
        Tasks.await(tdc);
        DriveContents contents = tdc.getResult();
        InputStream inputStream = contents.getInputStream();

        try {
            Streams.flush(inputStream, dataOutputStream);
        } finally {
            inputStream.close();
        }
    }


    public InputStream readFile(DriveFile file) throws ExecutionException, InterruptedException {
        Task<DriveContents> tdc = getDriveResourceClient().openFile(file, DriveFile.MODE_READ_ONLY);
        Tasks.await(tdc);
        DriveContents contents = tdc.getResult();
        InputStream inputStream = contents.getInputStream();

        return inputStream;
    }

    public List<Metadata> listChildren(DriveFolder appFolder) throws ExecutionException, InterruptedException {
        return listChildren(appFolder, -1);
    }

    public List<Metadata> listChildren(DriveFolder appFolder, int max) throws ExecutionException, InterruptedException {
        Task<MetadataBuffer> tmb = getDriveResourceClient().queryChildren(appFolder,
                new Query.Builder()
                        .addFilter(Filters.eq(SearchableField.TRASHED, false))
                        .setSortOrder(new SortOrder.Builder().addSortDescending(SortableField.CREATED_DATE).build())
                        .build());
        Tasks.await(tmb);
        Iterator<Metadata> iter = tmb.getResult().iterator();
        List<Metadata> childrenList = new LinkedList<>();
        int i = 0;
        while (iter.hasNext()) {
            if (max >= 0 && i++ >= max) {
                break;
            }
            Metadata data = iter.next();
            childrenList.add(data);
        }
        return childrenList;
    }

    public void deleteFile(DriveFile driveFile) throws ExecutionException, InterruptedException {
        Task<Void> tv = getDriveResourceClient().delete(driveFile);
        Tasks.await(tv);
    }
}
