package com.colaorange.dailymoney.core.ui.legacy;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.support.annotation.NonNull;
import android.support.v4.provider.DocumentFile;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.context.Contexts;
import com.colaorange.dailymoney.core.context.ContextsActivity;
import com.colaorange.dailymoney.core.data.CSVImportExporter;
import com.colaorange.dailymoney.core.data.DataBackupRestorer;
import com.colaorange.dailymoney.core.data.DataCreator;
import com.colaorange.dailymoney.core.data.IDataProvider;
import com.colaorange.dailymoney.core.ui.GUIs;
import com.colaorange.dailymoney.core.util.Logger;
import com.colaorange.dailymoney.core.util.Misc;

import java.io.File;
import java.text.DateFormat;
import java.util.Date;

/**
 * @author dennis
 */
public class DataMaintenanceActivity extends ContextsActivity implements OnClickListener {

    String csvEncoding;

    File v23WorkingFolder;
    Uri v29DocTreeRootUri;

    static final String APPVER = "appver:";

    int vercode = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.data_maintenance);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            String uri = contexts().getPreference().getV29DocTreeRootUri();
            System.out.println(">onCreate>>>docTreeRootUri" + uri);
            try {
                v29DocTreeRootUri = Uri.parse(uri);
            } catch (Exception x) {
                //nothing
            }
        } else {
            v23WorkingFolder = contexts().getV23WorkingFolder();
        }

        vercode = contexts().getAppVerCode();
        csvEncoding = preference().getCSVEncoding();

        initMembers();


    }

    @Override
    public void onStart() {
        super.onStart();
        refreshUI();
    }

    private void refreshUI() {

        Button requestPermissionBtn = findViewById(R.id.request_permission);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            //only for 10(29+)
            if (!contexts().hasDocTreeRootPermission(v29DocTreeRootUri)) {
                requestPermissionBtn.setVisibility(View.VISIBLE);
                doV29RequestPermission();
            } else {
                requestPermissionBtn.setVisibility(View.GONE);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //only for 6.0(23+)
            if (!contexts().hasWorkingFolderPermission(v23WorkingFolder)) {
                requestPermissionBtn.setVisibility(View.VISIBLE);
                doV23RequestPermission();
            } else {
                requestPermissionBtn.setVisibility(View.GONE);
            }
        } else {
            requestPermissionBtn.setVisibility(View.GONE);
        }

        //working folder accessibility
        TextView vWorkingFolder = findViewById(R.id.working_folder);
        ImageView vIcon = findViewById(R.id.storage_icon);
        //test accessibility

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (contexts().hasDocTreeRootPermission(v29DocTreeRootUri)) {
                vIcon.setImageResource(resolveThemeAttrResId(R.attr.ic_info));
                vWorkingFolder.setText(v29DocTreeRootUri.toString());
            } else {
                vIcon.setImageResource(resolveThemeAttrResId(R.attr.ic_warning));
                vWorkingFolder.setText(i18n().string(R.string.msg_working_folder_no_access, v29DocTreeRootUri.toString()));
            }
        } else {
            if (contexts().hasV23WorkingFolderPermission()) {
                vIcon.setImageResource(resolveThemeAttrResId(R.attr.ic_info));
                vWorkingFolder.setText(v23WorkingFolder.getAbsolutePath());
            } else {
                vIcon.setImageResource(resolveThemeAttrResId(R.attr.ic_warning));
                vWorkingFolder.setText(i18n().string(R.string.msg_working_folder_no_access, v23WorkingFolder.getAbsolutePath()));
            }
        }

        TextView lastBackupText = findViewById(R.id.lastbackup);

        if (preference().getLastBackup() != null) {
            lastBackupText.setText(preference().getLastBackup());
        }
    }

    private void initMembers() {
        findViewById(R.id.request_permission).setOnClickListener(this);

        findViewById(R.id.backup).setOnClickListener(this);
        findViewById(R.id.export_csv).setOnClickListener(this);
        findViewById(R.id.share_csv).setOnClickListener(this);

        findViewById(R.id.restore).setOnClickListener(this);
        findViewById(R.id.import_csv).setOnClickListener(this);

        //TODO move to developer
        findViewById(R.id.reset).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.request_permission) {
            doV23RequestPermission();
        } else if (v.getId() == R.id.import_csv) {
            doImportCSV();
        } else if (v.getId() == R.id.export_csv) {
            doExportCSV();
        } else if (v.getId() == R.id.share_csv) {
            doShareCSV();
        } else if (v.getId() == R.id.backup) {
            doBackup();
        } else if (v.getId() == R.id.restore) {
            doRestore();
        } else if (v.getId() == R.id.reset) {
            doReset();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == Build.VERSION_CODES.M &&
                Misc.isPermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE, permissions, grantResults)) {
            recreate();
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void doV23RequestPermission() {
        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Build.VERSION_CODES.M);
    }


    @SuppressLint("WrongConstant")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == Build.VERSION_CODES.Q && resultCode == Activity.RESULT_OK) {
            Uri uri = resultData.getData();
            if (uri != null) {
                contexts().getPreference().setV29DocTreeRootUri(uri.toString());

                final int takeFlags = resultData.getFlags()
                        & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                // Check for the freshest data.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    getContentResolver().takePersistableUriPermission(uri, takeFlags);
                }

                recreate();
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.Q)
    private void doV29RequestPermission() {
        System.out.println(">doV29RequestPermission>>>" + v29DocTreeRootUri);
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);

        // Optionally, specify a URI for the directory that should be opened in
        // the system file picker when it loads.
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, v29DocTreeRootUri);

        startActivityForResult(intent, Build.VERSION_CODES.Q);
    }

    private void doBackup() {
        final long now = System.currentTimeMillis();
        final GUIs.IBusyRunnable job = new GUIs.BusyAdapter() {
            DataBackupRestorer.Result result;

            public void onBusyError(Throwable t) {
                GUIs.error(DataMaintenanceActivity.this, t);
            }

            public void onBusyFinish() {
                if (result.isSuccess()) {
                    String count = "" + (result.getDb() + result.getPref());
                    String msg = i18n().string(R.string.msg_db_backuped, count, result.getLastFolder());
                    preference().setLastBackupTime(now);
                    GUIs.alert(DataMaintenanceActivity.this, msg);
                    refreshUI();
                } else {
                    GUIs.alert(DataMaintenanceActivity.this, result.getErr());
                }
            }

            @Override
            public void run() {
                result = new DataBackupRestorer().backup();
                trackEvent(TE.BACKUP);
            }
        };
        GUIs.doBusy(DataMaintenanceActivity.this, job);
    }

    private void doRestore() {
        final GUIs.IBusyRunnable job = new GUIs.BusyAdapter() {
            DataBackupRestorer.Result result;
            Long lastBakcup;

            public void onBusyError(Throwable t) {
                GUIs.error(DataMaintenanceActivity.this, t);
            }

            public void onBusyFinish() {
                if (result.isSuccess()) {
                    String count = "" + (result.getDb() + result.getPref());
                    String msg = i18n().string(R.string.msg_db_restored, count, result.getLastFolder());
                    if (lastBakcup != null) {
                        preference().setLastBackupTime(lastBakcup);
                    }
                    //theme, templates is possible changed
                    GUIs.alert(DataMaintenanceActivity.this, msg, new GUIs.OnFinishListener() {
                        @Override
                        public boolean onFinish(int which, Object data) {
                            if (which == GUIs.OK_BUTTON) {
                                restartAppColdly();
                            }
                            return true;
                        }
                    });
                } else {
                    GUIs.alert(DataMaintenanceActivity.this, result.getErr());
                }
            }

            @Override
            public void run() {
                lastBakcup = preference().getLastBackupTime();
                result = new DataBackupRestorer().restore();
                trackEvent(TE.RESTORE);
            }
        };


        GUIs.confirm(this, i18n().string(R.string.qmsg_restore_data), new GUIs.OnFinishListener() {
            @Override
            public boolean onFinish(int which, Object data) {
                if (which == GUIs.OK_BUTTON) {
                    GUIs.doBusy(DataMaintenanceActivity.this, job);
                }
                return true;
            }
        });
    }

    private void doReset() {
        GUIs.confirm(this, i18n().string(R.string.qmsg_reset_working_book), new GUIs.OnFinishListener() {
            @Override
            public boolean onFinish(int which, Object data) {
                if (which == GUIs.OK_BUTTON) {

                    final GUIs.IBusyRunnable job = new GUIs.BusyAdapter() {
                        public void onBusyError(Throwable t) {
                            GUIs.error(DataMaintenanceActivity.this, t);
                        }

                        public void onBusyFinish() {
                            GUIs.alert(DataMaintenanceActivity.this, R.string.msg_rested);
                        }

                        @Override
                        public void run() {
                            try {
                                resetWorkingBook();
                            } catch (Exception e) {
                                throw new RuntimeException(e.getMessage(), e);
                            }
                        }
                    };
                    GUIs.doBusy(DataMaintenanceActivity.this, job);
                }
                return true;
            }
        });
    }

    private void doExportCSV() {
        new AlertDialog.Builder(this).setTitle(i18n().string(R.string.qmsg_export_csv))
                .setItems(R.array.csv_type_options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, final int which) {
                        final GUIs.IBusyRunnable job = new GUIs.BusyAdapter() {
                            CSVImportExporter.Result result;

                            public void onBusyError(Throwable t) {
                                GUIs.error(DataMaintenanceActivity.this, t);
                            }

                            public void onBusyFinish() {
                                if (result.isSuccess()) {
                                    String msg = i18n().string(R.string.msg_csv_exported,
                                            Integer.toString(result.getDetail() + result.getAccount()),
                                            result.getLastFolder());
                                    GUIs.alert(DataMaintenanceActivity.this, msg);
                                } else {
                                    GUIs.alert(DataMaintenanceActivity.this, i18n().string(R.string.label_error) + ":" + result.getErr());
                                }
                            }

                            @Override
                            public void run() {
                                try {
                                    CSVImportExporter.ExportMode mode;
                                    switch (which) {
                                        case 1:
                                            mode = CSVImportExporter.ExportMode.WORKING_BOOK;
                                            break;
                                        case 2:
                                            mode = CSVImportExporter.ExportMode.WORKING_BOOK_ACCOUNT;
                                            break;
                                        case 0:
                                        default:
                                            mode = CSVImportExporter.ExportMode.ALL_BOOKS;
                                            break;
                                    }
                                    result = new CSVImportExporter().exportIt(mode);
                                } catch (Exception e) {
                                    throw new RuntimeException(e.getMessage(), e);
                                }
                                trackEvent(TE.EXPORT);
                            }
                        };
                        GUIs.doBusy(DataMaintenanceActivity.this, job);
                    }
                }).show();
    }

    private void doImportCSV() {
        final int workingBookId = contexts().getWorkingBookId();
        new AlertDialog.Builder(this).setTitle(i18n().string(R.string.qmsg_import_csv))
                .setItems(R.array.csv_type_import_options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, final int which) {
                        final GUIs.IBusyRunnable job = new GUIs.BusyAdapter() {
                            CSVImportExporter.Result result;

                            public void onBusyError(Throwable t) {
                                GUIs.error(DataMaintenanceActivity.this, t);
                            }

                            public void onBusyFinish() {
                                if (result.isSuccess()) {
                                    String msg = i18n().string(R.string.msg_csv_imported, Integer.toString(result.getAccount() + result.getDetail()), result.getLastFolder());
                                    GUIs.alert(DataMaintenanceActivity.this, msg);
                                } else {
                                    GUIs.alert(DataMaintenanceActivity.this, i18n().string(R.string.label_error) + ":" + result.getErr());
                                }
                            }

                            @Override
                            public void run() {
                                CSVImportExporter.ImportMode mode;
                                switch (which) {
                                    case 1:
                                        mode = CSVImportExporter.ImportMode.WORKING_BOOK_ACCOUNT;
                                        break;
                                    case 0:
                                    default:
                                        mode = CSVImportExporter.ImportMode.WORKING_BOOK;
                                        break;
                                }
                                result = new CSVImportExporter().importIt(mode);

                                trackEvent(TE.IMPORT);
                            }
                        };
                        GUIs.doBusy(DataMaintenanceActivity.this, job);
                    }
                }).show();
    }

    private void doShareCSV() {
        new AlertDialog.Builder(this).setTitle(i18n().string(R.string.qmsg_share_csv))
                .setItems(R.array.csv_type_options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, final int which) {
                        final GUIs.IBusyRunnable job = new GUIs.BusyAdapter() {
                            CSVImportExporter.Result result;

                            public void onBusyError(Throwable t) {
                                GUIs.error(DataMaintenanceActivity.this, t);
                            }

                            public void onBusyFinish() {
                                if (result.getFiles() == null || result.getFiles().size() <= 0) {
                                    GUIs.alert(DataMaintenanceActivity.this, R.string.msg_no_csv);
                                } else {
                                    DateFormat df = preference().getDateTimeFormat();
                                    contexts().shareTextContent(DataMaintenanceActivity.this, i18n().string(R.string.msg_share_csv_title, df.format(new Date())), i18n().string(R.string.msg_share_csv_content), result.getFiles());
                                }
                            }

                            @Override
                            public void run() {
                                try {
                                    CSVImportExporter.ExportMode mode;
                                    switch (which) {
                                        case 1:
                                            mode = CSVImportExporter.ExportMode.WORKING_BOOK;
                                            break;
                                        case 2:
                                            mode = CSVImportExporter.ExportMode.WORKING_BOOK_ACCOUNT;
                                            break;
                                        case 0:
                                        default:
                                            mode = CSVImportExporter.ExportMode.ALL_BOOKS;
                                            break;
                                    }
                                    result = new CSVImportExporter().exportIt(mode);
                                } catch (Exception e) {
                                    throw new RuntimeException(e.getMessage(), e);
                                }
                                trackEvent(TE.SHARE);
                            }
                        };
                        GUIs.doBusy(DataMaintenanceActivity.this, job);
                    }
                }).show();
    }

    //in thread
    private void resetWorkingBook() {
        IDataProvider idp = contexts().getDataProvider();
        idp.reset();
        Logger.d("reset working book");
    }
}
