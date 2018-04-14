package com.colaorange.dailymoney.ui.legacy;

import java.io.File;
import java.text.DateFormat;
import java.util.Date;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.colaorange.dailymoney.util.GUIs;
import com.colaorange.dailymoney.util.Logger;
import com.colaorange.dailymoney.context.Contexts;
import com.colaorange.dailymoney.context.ContextsActivity;
import com.colaorange.dailymoney.R;
import com.colaorange.dailymoney.data.DataBackupRestorer;
import com.colaorange.dailymoney.data.CSVImportExporter;
import com.colaorange.dailymoney.data.DataCreator;
import com.colaorange.dailymoney.data.IDataProvider;

/**
 * @author dennis
 */
public class DataMaintenanceActivity extends ContextsActivity implements OnClickListener {

    String csvEncoding;

    File workingFolder;

    static final String APPVER = "appver:";

    int vercode = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.data_maintenance);
        workingFolder = contexts().getWorkingFolder();

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

        Button requestPermissionBtn = findViewById(R.id.data_maintenance_request_permission);
        //only for 6.0(23+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !contexts().hasWorkingFolderPermission()) {
            requestPermissionBtn.setVisibility(View.VISIBLE);
        } else {
            requestPermissionBtn.setVisibility(View.GONE);
        }

        //working fodler accessibility
        TextView workingFolderText = findViewById(R.id.data_maintenance_workingfolder);
        //test accessable
        if (contexts().hasWorkingFolderPermission()) {
            workingFolderText.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(android.R.drawable.ic_dialog_info), null, null, null);
            workingFolderText.setText(workingFolder.getAbsolutePath());
        } else {
            workingFolderText.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(android.R.drawable.ic_dialog_alert), null, null, null);
            workingFolderText.setText(i18n().string(R.string.msg_working_folder_no_access, workingFolder.getAbsolutePath()));
        }

        TextView lastBackupText = findViewById(R.id.datamain_lastbackup);

        if (preference().getLastBackup() != null) {
            lastBackupText.setText(preference().getLastBackup());
        }
    }

    private void initMembers() {
        findViewById(R.id.data_maintenance_request_permission).setOnClickListener(this);
        findViewById(R.id.data_maintenance_backup).setOnClickListener(this);
        findViewById(R.id.data_maintenance_export_csv).setOnClickListener(this);
        findViewById(R.id.data_maintenance_share_csv).setOnClickListener(this);

        findViewById(R.id.data_maintenance_restore).setOnClickListener(this);
        findViewById(R.id.data_maintenance_import_csv).setOnClickListener(this);

        //TODO move to developer
        findViewById(R.id.data_maintenance_reset).setOnClickListener(this);
        findViewById(R.id.data_maintenance_clear_folder).setOnClickListener(this);
        findViewById(R.id.data_maintenance_create_default).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.data_maintenance_request_permission) {
            doRequestPermission();
        } else if (v.getId() == R.id.data_maintenance_import_csv) {
            doImportCSV();
        } else if (v.getId() == R.id.data_maintenance_export_csv) {
            doExportCSV();
        } else if (v.getId() == R.id.data_maintenance_share_csv) {
            doShareCSV();
        } else if (v.getId() == R.id.data_maintenance_backup) {
            doBackup();
        } else if (v.getId() == R.id.data_maintenance_restore) {
            doRestore();
        } else if (v.getId() == R.id.data_maintenance_reset) {
            doReset();
        } else if (v.getId() == R.id.data_maintenance_create_default) {
            doCreateDefault();
        } else if (v.getId() == R.id.data_maintenance_clear_folder) {
            doClearFolder();
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void doRequestPermission() {
        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
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
                result = DataBackupRestorer.backup();
                trackEvent("backup");
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
                    GUIs.alert(DataMaintenanceActivity.this, msg);
                } else {
                    GUIs.alert(DataMaintenanceActivity.this, result.getErr());
                }
            }

            @Override
            public void run() {
                lastBakcup = preference().getLastBackupTime();
                result = DataBackupRestorer.restore();
                trackEvent("restore");
            }
        };


        GUIs.confirm(this, i18n().string(R.string.qmsg_restore_data), new GUIs.OnFinishListener() {
            @Override
            public boolean onFinish(Object data) {
                if (((Integer) data).intValue() == GUIs.OK_BUTTON) {
                    GUIs.doBusy(DataMaintenanceActivity.this, job);
                }
                return true;
            }
        });
    }

    private void doReset() {
        GUIs.confirm(this, i18n().string(R.string.qmsg_reset_working_book), new GUIs.OnFinishListener() {
            @Override
            public boolean onFinish(Object data) {
                if (((Integer) data).intValue() == GUIs.OK_BUTTON) {

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

    private void doClearFolder() {
        //TODO move to devlope
        final GUIs.IBusyRunnable job = new GUIs.BusyAdapter() {
            @Override
            public void onBusyFinish() {
                GUIs.alert(DataMaintenanceActivity.this, i18n().string(R.string.msg_folder_cleared, workingFolder));
            }

            @Override
            public void run() {
                for (File f : workingFolder.listFiles()) {
                    String fnm = f.getName().toLowerCase();
                    //don't delete sub folder
                    if (f.isFile()) {
                        f.delete();
                    }
                }
            }
        };

        GUIs.confirm(this, i18n().string(R.string.qmsg_clear_folder, workingFolder), new GUIs.OnFinishListener() {
            @Override
            public boolean onFinish(Object data) {
                if (((Integer) data).intValue() == GUIs.OK_BUTTON) {
                    GUIs.doBusy(DataMaintenanceActivity.this, job);
                }
                return true;
            }
        });

    }

    private void doCreateDefault() {

        final GUIs.IBusyRunnable job = new GUIs.BusyAdapter() {
            @Override
            public void onBusyFinish() {
                GUIs.alert(DataMaintenanceActivity.this, R.string.msg_default_created);
            }

            @Override
            public void run() {
                IDataProvider idp = contexts().getDataProvider();
                new DataCreator(idp, i18n()).createDefaultAccount();
            }
        };

        GUIs.confirm(this, i18n().string(R.string.qmsg_create_default), new GUIs.OnFinishListener() {
            @Override
            public boolean onFinish(Object data) {
                if (((Integer) data).intValue() == GUIs.OK_BUTTON) {
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
                                    GUIs.alert(DataMaintenanceActivity.this, i18n().string(R.string.clabel_error) + ":" + result.getErr());
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
                                trackEvent("export_csv_v2");
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
                                    String msg = i18n().string(R.string.msg_csv_imported, Integer.toString(result.getAccount() + result.getDetail()), workingFolder);
                                    GUIs.alert(DataMaintenanceActivity.this, msg);
                                } else {
                                    GUIs.alert(DataMaintenanceActivity.this, i18n().string(R.string.clabel_error) + ":" + result.getErr());
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

                                trackEvent("import_csv_v2");
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
                                trackEvent("share_csv_v2");
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
