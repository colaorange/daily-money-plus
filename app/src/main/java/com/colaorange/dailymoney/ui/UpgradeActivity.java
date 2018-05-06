package com.colaorange.dailymoney.ui;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.colaorange.commons.util.Files;
import com.colaorange.commons.util.Formats;
import com.colaorange.commons.util.GUIs;
import com.colaorange.commons.util.Logger;
import com.colaorange.dailymoney.R;
import com.colaorange.dailymoney.context.Contexts;
import com.colaorange.dailymoney.context.ContextsActivity;
import com.colaorange.dailymoney.data.Account;
import com.colaorange.dailymoney.data.BackupRestorer;
import com.colaorange.dailymoney.data.DataCreator;
import com.colaorange.dailymoney.data.Detail;
import com.colaorange.dailymoney.data.IDataProvider;
import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 
 * @author dennis
 *
 */
public class UpgradeActivity extends ContextsActivity implements OnClickListener {

    File workingFolder;
    


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.upgrade);
        workingFolder = contexts().getWorkingFolder();

        initialListener();

        trackEvent("upgrade-view");
    }

    @Override
    public void onStart(){
        super.onStart();
        refreshUI();
    }

    private void refreshUI() {

        Button requestPermissionBtn = findViewById(R.id.datamain_request_permission);
        //only for 6.0(23+)
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !contexts().hasWorkingFolderPermission()){
            requestPermissionBtn.setVisibility(View.VISIBLE);
        }else{
            requestPermissionBtn.setVisibility(View.GONE);
        }

        //working fodler accessibility
        TextView workingFolderText = findViewById(R.id.datamain_workingfolder);
        //test accessable
        if (contexts().hasWorkingFolderPermission()) {
            workingFolderText.setText(workingFolder.getAbsolutePath());
        } else {
            workingFolderText.setText(i18n.string(R.string.msg_working_folder_no_access, workingFolder.getAbsolutePath()));
        }

        TextView lastBackupText = findViewById(R.id.datamain_lastbackup);

        if(contexts().getPrefLastBackup()!=null) {
            lastBackupText.setText(contexts().getPrefLastBackup());
        }
    }

    private void initialListener() {
        findViewById(R.id.datamain_request_permission).setOnClickListener(this);
        findViewById(R.id.datamain_backup).setOnClickListener(this);
        findViewById(R.id.datamain_install).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.datamain_request_permission) {
            doRequestPermission();
        } else if (v.getId() == R.id.datamain_backup) {
            doBackup();
        } else if (v.getId() == R.id.datamain_install) {
            doInstall();
        }
    }

    private void doInstall() {

        final GUIs.IBusyRunnable job = new GUIs.BusyAdapter() {

            public void onBusyError(Throwable t) {
                GUIs.error(UpgradeActivity.this, t);
            }

            public void onBusyFinish() {
            }

            @Override
            public void run() {
                Intent intent = new Intent();

                Uri uri = Uri.parse("market://details?id=com.colaorange.dailymoney");
                intent.setAction(Intent.ACTION_VIEW).setData(uri);

                startActivity(intent);
                trackEvent("upgrade-install");
            }
        };
        GUIs.doBusy(UpgradeActivity.this, job);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void doRequestPermission() {
        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
    }

    private void doBackup() {
        final long now = System.currentTimeMillis();
        final GUIs.IBusyRunnable job = new GUIs.BusyAdapter() {
            BackupRestorer.Result result;
            
            public void onBusyError(Throwable t) {
                GUIs.error(UpgradeActivity.this, t);
            }

            public void onBusyFinish() {
                if (result.isSuccess()) {
                    String count = ""+(result.getDb()+result.getPref());
                    String msg = i18n.string(R.string.msg_db_backuped, count, workingFolder);
                    contexts().setPrefLastBackupTime(now);
                    GUIs.alert(UpgradeActivity.this, msg);
                    refreshUI();
                } else {
                    GUIs.alert(UpgradeActivity.this, result.getErr());
                }
            }

            @Override
            public void run() {
                result = BackupRestorer.backup();
                trackEvent("upgrade-backup");
            }
        };
        GUIs.doBusy(UpgradeActivity.this, job);
    }

}
