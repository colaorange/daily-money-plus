package com.colaorange.dailymoney.core.drive;

import android.content.Context;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.util.Iterator;
import java.util.concurrent.ExecutionException;

/**
 * Created by Dennis
 */
public class GoogleDriveHelper {
    final GoogleSignInAccount googleSignInAccount;
    final Context context;

    DriveResourceClient driveResourceClient;

    DriveClient driveClient;


    public GoogleDriveHelper(Context context, GoogleSignInAccount googleSignInAccount) {
        this.context = context;
        this.googleSignInAccount = googleSignInAccount;
    }

    public DriveResourceClient getDriveResourceClient() {
        return driveResourceClient != null ? driveResourceClient :
                (driveResourceClient = Drive.getDriveResourceClient(context, googleSignInAccount));
    }

    public DriveClient getDriveClient() {
        return driveClient != null ? driveClient :
                (driveClient = Drive.getDriveClient(context, googleSignInAccount));
    }

    public DriveFolder getOrCreateFolder(DriveFolder parent, String name) throws ExecutionException, InterruptedException {
        if (parent == null) {
            Task<DriveFolder> trf = getDriveResourceClient().getRootFolder();
            Tasks.await(trf);
            parent = trf.getResult();
        }

        Task<MetadataBuffer> tmb = getDriveResourceClient().queryChildren(parent,
                new Query.Builder()
                        .addFilter(Filters.eq(SearchableField.TITLE, name))
                        .build());
        Tasks.await(tmb);

        MetadataBuffer mb = tmb.getResult();
        DriveFolder appFolder = null;
        try {
            Iterator<Metadata> iter = mb.iterator();
            while (iter.hasNext()) {
                Metadata data = iter.next();
                if (data.isFolder()) {
                    appFolder = data.getDriveId().asDriveFolder();
                    break;
                }
            }
        } finally {
            mb.release();
        }

        if (appFolder == null) {
            MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                    .setTitle(name)
                    .build();

            Task<DriveFolder> cft = getDriveResourceClient().createFolder(parent, changeSet);
            Tasks.await(cft);
            appFolder = cft.getResult();
        }
        return appFolder;
    }
}
