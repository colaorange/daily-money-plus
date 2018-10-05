package com.colaorange.dailymoney.core.ui.legacy;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.context.ContextsActivity;
import com.colaorange.dailymoney.core.ui.Constants;
import com.colaorange.dailymoney.core.ui.GUIs;
import com.colaorange.dailymoney.core.util.Logger;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.File;

/**
 * @author dennis
 */
public class GoogleDriveActivity extends ContextsActivity implements OnClickListener {

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
                        .requestScopes(Drive.SCOPE_FILE)
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
                            startActivityForResult(gsiClient.getSignInIntent(), 123);
                        }
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent data) {
        if (requestCode == 123) {
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

        }
        return;
    }

    private void doRevoke() {
        gsiClient.signOut().addOnSuccessListener(new OnSuccessListener<Void>() {
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
        DriveClient driveClient;
        DriveResourceClient driveResourceClient;
        // Build a drive client.
        driveClient = Drive.getDriveClient(getApplicationContext(), googleSignInAccount);
        // Build a drive resource client.
        driveResourceClient =
                Drive.getDriveResourceClient(getApplicationContext(), googleSignInAccount);
    }

    private void doDownload() {
        DriveClient driveClient;
        DriveResourceClient driveResourceClient;
        // Build a drive client.
        driveClient = Drive.getDriveClient(getApplicationContext(), googleSignInAccount);
        // Build a drive resource client.
        driveResourceClient =
                Drive.getDriveResourceClient(getApplicationContext(), googleSignInAccount);
    }

}
