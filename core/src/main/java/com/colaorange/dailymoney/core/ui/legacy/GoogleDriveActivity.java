package com.colaorange.dailymoney.core.ui.legacy;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.colaorange.commons.util.Streams;
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
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

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

    GoogleDriveHelper gdHelper;

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
        if (gdHelper != null) {
            vAuthInfo.setText("Logged in as " + gdHelper.getGoogleSignInAccount().getDisplayName());
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

        Task<GoogleDriveHelper> task = GoogleDriveHelper.signIn(this);
        task.addOnSuccessListener(new OnSuccessListener<GoogleDriveHelper>() {
            @Override
            public void onSuccess(GoogleDriveHelper helper) {
                GoogleDriveActivity.this.gdHelper = helper;
                Logger.i("Sign in success {}" + helper.getGoogleSignInAccount().getDisplayName());
                refreshUI();
            }
        }).addOnFailureListener(
                new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        if (!silentOnly) {
                            startActivityForResult(GoogleDriveHelper.getSignInIntent(GoogleDriveActivity.this), REQUEST_AUTH);
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
                    gdHelper = new GoogleDriveHelper(this, task.getResult(ApiException.class));

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
        GoogleDriveHelper.revokeAccess(this).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void v) {
                gdHelper = null;
                refreshUI();
            }
        }).addOnFailureListener(
                new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        gdHelper = null;
                        refreshUI();
                    }
                });
    }

    private void doList() {

        GUIs.doBusy(this, new Runnable() {
            public void run() {
                try {
                    Logger.i(">>>>>>>>>>>>>1");
                    gdHelper.requestSync();

                    DriveFolder appFolder = gdHelper.retrieveFolder(null, DRIVE_APP_FOLDER_NAME, true);

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

                        if(data.isTrashed()){
                            //nothing
                        }else if (folder) {
                            Logger.i(">>>>>>> Folder : {}", title);
                        } else {
                            Logger.i(">>>>>>> File {}, mimeTYpe {}, ext {}, size {}", title, mineType, ext, size);
                        }
                        childrenList.add(data);
                    }

                    Logger.i(">>>>>>>>>>>>>6");

                } catch (Exception e) {
                    Logger.e(e.getMessage(), e);
                }
            }
        });
    }

    private void doDownload() {
        doCreateFile();
    }

    private void doCreateFile() {

        GUIs.doBusy(this, new Runnable() {
            public void run() {
                try {

                    DriveFolder appFolder = gdHelper.retrieveFolder(null, DRIVE_APP_FOLDER_NAME, false);

                    InputStream is = null;
                    try {
                        DriveFile testFile = gdHelper.writeFile(appFolder, "test4.txt",
                                new ByteArrayInputStream("Just test 4".getBytes("UTF8")));

                        testFile = gdHelper.writeFile(testFile,
                                new ByteArrayInputStream("Just test 4-1".getBytes("UTF8")));


                        testFile = gdHelper.renameFile(testFile,"test4-1");

                        ByteArrayOutputStream os = new ByteArrayOutputStream();
                        gdHelper.readFile(testFile, os);

                        Logger.i(">>>>>>>>>>>>>DONE1 "+new String(os.toByteArray(), "UTF8"));

                        is = gdHelper.readFile(testFile);

                        os = new ByteArrayOutputStream();
                        Streams.flush(is, os);
                        is.close();

                        Logger.i(">>>>>>>>>>>>>DONE2 "+new String(os.toByteArray(), "UTF8"));
                    }finally{
                        if(is!=null){
                            is.close();
                        }
                    }

                } catch (Exception e) {
                    Logger.e(e.getMessage(), e);
                }
            }
        });
    }
}
