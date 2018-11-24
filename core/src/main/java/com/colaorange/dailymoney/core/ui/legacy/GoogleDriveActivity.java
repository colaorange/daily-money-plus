package com.colaorange.dailymoney.core.ui.legacy;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.colaorange.commons.util.Strings;
import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.context.ContextsActivity;
import com.colaorange.dailymoney.core.drive.GoogleDriveHelper;
import com.colaorange.dailymoney.core.ui.GUIs;
import com.colaorange.dailymoney.core.util.Logger;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.drive.CreateFileActivityOptions;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.io.File;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author dennis
 */
public class GoogleDriveActivity extends ContextsActivity implements OnClickListener {

    public static final String DRIVE_APP_FOLDER_NAME = "coDailyMoneyBackup";

    public static final Scope SCOPE_DRIVE_FILE = new Scope("https://www.googleapis.com/auth/drive.file");
    public static final Scope SCOPE_DRIVE_APPDATA = new Scope("https://www.googleapis.com/auth/drive.appdata");

    /*
    get  API: Drive.API_CONNECTIONLESS is not available on this device.
    com.google.android.gms.common.api.ApiException: 17: API: Drive.API_CONNECTIONLESS is not available on this device.
    when I use this scope
     */
    private static final Scope SCOPE_DRIVE = new Scope("https://www.googleapis.com/auth/drive");

    private static final int REQUEST_AUTH = 101;
    private static final int REQUEST_INTENT_SENDER = 102;

    GoogleSignInClient gsiClient;
    GoogleSignInAccount googleSignInAccount;
    File workingFolder;

    TextView vAuthInfo;
    Button btnRequestAuth;
    Button btnRequestRevoke;
    Button btnRequestList;
    Button btnRequestDownload;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.google_drive);
        workingFolder = contexts().getWorkingFolder();

        initMembers();
    }

    private void initMembers() {
        vAuthInfo = findViewById(R.id.auth_info);


        (btnRequestAuth = findViewById(R.id.request_auth)).setOnClickListener(this);
        (btnRequestRevoke = findViewById(R.id.request_revoke)).setOnClickListener(this);
        (btnRequestList = findViewById(R.id.request_list)).setOnClickListener(this);
        (btnRequestDownload = findViewById(R.id.request_download)).setOnClickListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        refreshUI();
        doRequestAuth(true);
    }

    private void refreshUI() {
        if (googleSignInAccount != null) {
            vAuthInfo.setText("Logged in as " + googleSignInAccount.getDisplayName());
            btnRequestAuth.setEnabled(false);
            btnRequestRevoke.setEnabled(true);
            btnRequestList.setEnabled(true);
            btnRequestDownload.setEnabled(true);

        } else {
            vAuthInfo.setText("Haven't logged in yet");
            btnRequestAuth.setEnabled(true);
            btnRequestRevoke.setEnabled(false);
            btnRequestList.setEnabled(false);
            btnRequestDownload.setEnabled(false);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.request_auth) {
            doRequestAuth(false);
        } else if (v.getId() == R.id.request_revoke) {
            doRevoke();
        } else if (v.getId() == R.id.request_list) {
            doList();
        } else if (v.getId() == R.id.request_download) {
            doDownload();
        }
    }

    private void doRequestAuth(final boolean silentOnly) {
        GoogleSignInOptions signInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestScopes(SCOPE_DRIVE_FILE)
                        .build();
        gsiClient = GoogleSignIn.getClient(this, signInOptions);

        Task<GoogleSignInAccount> task = gsiClient.silentSignIn();
        task.addOnSuccessListener(new OnSuccessListener<GoogleSignInAccount>() {
            @Override
            public void onSuccess(GoogleSignInAccount googleSignInAccount) {
                GoogleDriveActivity.this.googleSignInAccount = googleSignInAccount;
                Logger.i("Sign in success {}" + googleSignInAccount.getEmail());
                refreshUI();
            }
        }).addOnFailureListener(
                new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        if (!silentOnly) {
                            startActivityForResult(gsiClient.getSignInIntent(), REQUEST_AUTH);
                        }
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent data) {
        if (requestCode == REQUEST_AUTH) {
            if (resultCode == RESULT_OK) {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                try {
                    googleSignInAccount = task.getResult(ApiException.class);

                    Logger.i("Sign in success on activity result");
                    refreshUI();
                } catch (ApiException e) {
                    // The ApiException status code indicates the detailed failure reason.
                    // Please refer to the GoogleSignInStatusCodes class reference for more information.
                    Logger.w("signInResult:failed status code=" + e.getStatusCode());
                    GUIs.longToast(this, "Sign in fail, status code " + e.getStatusCode());
                }
            } else {
                GUIs.longToast(this, "Sign in fail, result code " + resultCode);
            }

        } else if (requestCode == REQUEST_INTENT_SENDER) {
            if (resultCode == RESULT_OK) {
                GUIs.longToast(this, "Intent Sender done " + resultCode);
            }
        }
        return;
    }

    private void doRevoke() {
        //gsiClient.signOut()
        gsiClient.revokeAccess().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void v) {
                gsiClient = null;
                googleSignInAccount = null;
                refreshUI();
            }
        }).addOnFailureListener(
                new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        gsiClient = null;
                        googleSignInAccount = null;
                        refreshUI();
                    }
                });
    }

    private void doList() {
        final GoogleDriveHelper gdHelper = new GoogleDriveHelper(this, googleSignInAccount);

        GUIs.doBusy(this, new Runnable() {
            public void run() {
                try {
                    Logger.i(">>>>>>>>>>>>>1");
                    Tasks.await(gdHelper.getDriveClient().requestSync());

                    DriveFolder appFolder = gdHelper.getOrCreateFolder(null, DRIVE_APP_FOLDER_NAME);

                    Task<MetadataBuffer> tmb = gdHelper.getDriveResourceClient().queryChildren(appFolder, new Query.Builder().build());
                    Logger.i(">>>>>>>>>>>>>5");
                    Tasks.await(tmb);

                    Iterator<Metadata> iter = tmb.getResult().iterator();
                    List<Metadata> childrenList = new LinkedList<>();
                    while (iter.hasNext()) {
                        Metadata data = iter.next();

                        String title = data.getTitle();

                        String mineType = data.getMimeType();
                        String ext = data.getFileExtension();
                        long size = data.getFileSize();
                        boolean folder = data.isFolder();

                        if (folder) {
                            Logger.i(">>>>>>> Folder : {}", title);
                        } else {
                            Logger.i(">>>>>>> File {}, mimeTYpe {}, ext {}, size {}", title, mineType, ext, size);
                        }
                        childrenList.add(data);
                    }

                    Logger.i(">>>>>>>>>>>>>6");

                } catch (Exception e) {
                    Logger.e(e.getMessage(), e);
                    GUIs.shortToast(getApplicationContext(), Strings.format("Error: {}", e.getMessage()));
                }
            }


        });
    }

    private DriveFolder retriveAppFolder(DriveResourceClient driveResourceClient) throws ExecutionException, InterruptedException {
        Task<DriveFolder> trf = driveResourceClient.getRootFolder();
        Tasks.await(trf);
        DriveFolder root = trf.getResult();

        Task<MetadataBuffer> tmb = driveResourceClient.queryChildren(root,
                new Query.Builder()
                        .addFilter(Filters.eq(SearchableField.TITLE, DRIVE_APP_FOLDER_NAME))
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
                    .setTitle(DRIVE_APP_FOLDER_NAME)
                    .build();

            Task<DriveFolder> cft = driveResourceClient.createFolder(root, changeSet);
            Tasks.await(cft);
            appFolder = cft.getResult();
        }
        return appFolder;
    }

    private void doDownload() {
        doCreateFolder();
    }

    private void doCreateFolder() {
        final DriveClient driveClient = Drive.getDriveClient(getApplicationContext(), googleSignInAccount);
        // Build a drive resource client.
        final DriveResourceClient driveResourceClient =
                Drive.getDriveResourceClient(getApplicationContext(), googleSignInAccount);

        driveResourceClient.getRootFolder().continueWithTask(new Continuation<DriveFolder, Task<DriveFolder>>() {
            @Override
            public Task<DriveFolder> then(@NonNull Task<DriveFolder> task) throws Exception {
                Logger.i(">>>>>>>>>>>>>1");
                DriveFolder parentFolder = task.getResult();
                MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                        .setTitle("MyFolder1")
                        .setMimeType(DriveFolder.MIME_TYPE)
//                        .setStarred(true)
                        .build();
                return driveResourceClient.createFolder(parentFolder, changeSet);
            }
        }).continueWith(new Continuation<DriveFolder, Void>() {
            @Override
            public Void then(@NonNull Task<DriveFolder> task) throws Exception {
                Logger.i(">>>>>>>>>>>>>2");
                return null;
            }
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void result) {
                Logger.i(">>>>>>>>>>>>>3");
                GUIs.shortToast(getApplicationContext(), Strings.format("Done"));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Logger.e(e.getMessage(), e);
                GUIs.shortToast(getApplicationContext(), Strings.format("Error: {}", e.getMessage()));
            }
        });
    }

    private void doCreateFile() {
        final DriveClient driveClient = Drive.getDriveClient(getApplicationContext(), googleSignInAccount);
        // Build a drive resource client.
        final DriveResourceClient driveResourceClient =
                Drive.getDriveResourceClient(getApplicationContext(), googleSignInAccount);

        driveResourceClient.createContents().continueWithTask(new Continuation<DriveContents, Task<IntentSender>>() {
            @Override
            public Task<IntentSender> then(@NonNull Task<DriveContents> task) throws Exception {
                Logger.i(">>>>>>>>>>>>>1");
                DriveContents contents = task.getResult();
                OutputStream outputStream = contents.getOutputStream();
                Writer writer = new OutputStreamWriter(outputStream);
                try {
                    writer.write("Hello World!");
                } finally {
                    writer.close();
                }

                MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                        .setTitle("NewFile1.txt")
                        .setMimeType("text/plain")
//                        .setStarred(true)
                        .build();

                CreateFileActivityOptions createOptions =
                        new CreateFileActivityOptions.Builder()
                                .setInitialDriveContents(contents)
                                .setInitialMetadata(changeSet)
                                .build();
                return driveClient.newCreateFileActivityIntentSender(createOptions);
            }
        }).continueWith(new Continuation<IntentSender, Void>() {
            @Override
            public Void then(@NonNull Task<IntentSender> task) throws Exception {
                Logger.i(">>>>>>>>>>>>>2");
                IntentSender intentSender = task.getResult();
                try {
                    startIntentSenderForResult(
                            intentSender, REQUEST_INTENT_SENDER, null, 0, 0, 0);
                } catch (IntentSender.SendIntentException e) {
                    Logger.e("Unable to create file", e);
                    GUIs.shortToast(getApplicationContext(), Strings.format("Unable to create file {}", e));
                }

                return null;
            }
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void result) {
                Logger.i(">>>>>>>>>>>>>3");
                GUIs.shortToast(getApplicationContext(), Strings.format("Done"));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Logger.e(e.getMessage(), e);
                GUIs.shortToast(getApplicationContext(), Strings.format("Error: {}", e.getMessage()));
            }
        });
    }

}
