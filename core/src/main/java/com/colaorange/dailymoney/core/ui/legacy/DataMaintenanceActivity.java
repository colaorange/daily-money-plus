package com.colaorange.dailymoney.core.ui.legacy;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.context.ContextsActivity;
import com.colaorange.dailymoney.core.data.CSVImportExporter;
import com.colaorange.dailymoney.core.data.DataBackupRestorer;
import com.colaorange.dailymoney.core.data.DataCreator;
import com.colaorange.dailymoney.core.data.IDataProvider;
import com.colaorange.dailymoney.core.ui.GUIs;
import com.colaorange.dailymoney.core.util.Logger;

import java.io.File;
import java.text.DateFormat;
import java.util.Date;

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

        Button requestPermissionBtn = findViewById(R.id.request_permission);
        //only for 6.0(23+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !contexts().hasWorkingFolderPermission()) {
            requestPermissionBtn.setVisibility(View.VISIBLE);
        } else {
            requestPermissionBtn.setVisibility(View.GONE);
        }

        //working fodler accessibility
        TextView vWorkingFolder = findViewById(R.id.working_folder);
        ImageView vIcon = findViewById(R.id.storage_icon);
        //test accessibility
        if (contexts().hasWorkingFolderPermission()) {
            vIcon.setImageResource(resolveThemeAttrResId(R.attr.ic_info));
            vWorkingFolder.setText(workingFolder.getAbsolutePath());
        } else {
            vIcon.setImageResource(resolveThemeAttrResId(R.attr.ic_warning));
            vWorkingFolder.setText(i18n().string(R.string.msg_working_folder_no_access, workingFolder.getAbsolutePath()));
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
        findViewById(R.id.clear_folder).setOnClickListener(this);
        findViewById(R.id.create_default).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.request_permission) {
            doRequestPermission();
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
        } else if (v.getId() == R.id.create_default) {
            doCreateDefault();
        } else if (v.getId() == R.id.clear_folder) {
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
                            if(which == GUIs.OK_BUTTON) {
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
                result = DataBackupRestorer.restore();
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
            public boolean onFinish(int which, Object data) {
                if (which == GUIs.OK_BUTTON) {
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
            public boolean onFinish(int which, Object data) {
                if (which == GUIs.OK_BUTTON) {
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
