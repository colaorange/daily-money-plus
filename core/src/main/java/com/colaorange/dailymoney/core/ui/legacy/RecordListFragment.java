package com.colaorange.dailymoney.core.ui.legacy;

import android.Manifest;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.colaorange.commons.util.CalendarHelper;
import com.colaorange.commons.util.Files;
import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.context.Contexts;
import com.colaorange.dailymoney.core.context.ContextsActivity;
import com.colaorange.dailymoney.core.context.ContextsFragment;
import com.colaorange.dailymoney.core.context.EventQueue;
import com.colaorange.dailymoney.core.context.PeriodMode;
import com.colaorange.dailymoney.core.data.Account;
import com.colaorange.dailymoney.core.data.AccountType;
import com.colaorange.dailymoney.core.data.Book;
import com.colaorange.dailymoney.core.data.IDataProvider;
import com.colaorange.dailymoney.core.data.Record;
import com.colaorange.dailymoney.core.ui.GUIs;
import com.colaorange.dailymoney.core.ui.QEvents;
import com.colaorange.dailymoney.core.ui.helper.SelectableRecyclerViewAdaptor;
import com.colaorange.dailymoney.core.util.I18N;
import com.colaorange.dailymoney.core.util.Misc;
import com.colaorange.dailymoney.core.xlsx.XlsxRecordExporter;

import java.io.File;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author dennis
 */
public class RecordListFragment extends ContextsFragment implements EventQueue.EventListener {

    public static final String ARG_TARGET_DATE = "targetDate";
    public static final String ARG_PERIOD_MODE = "periodMode";

    public static final String ARG_POS = "pos";

    public static final String ARG_DISABLE_SELECTION = "disableSelection";

    private View vNoData;
    private TextView vNoDataText;

    private int pos;
    private PeriodMode periodMode;
    private Date targetDate;
    private boolean disableSelection;

    private List<RecordRecyclerAdapter.RecordFolk> recyclerDataList;
    private RecyclerView vRecycler;
    private RecordRecyclerAdapter recyclerAdapter;

    private View rootView;

    private Map<String, Account> accountMap = new HashMap<>();
    private Map<AccountType, Integer> accountBgColorMap;
    private Map<AccountType, Integer> accountTextColorMap;
    private I18N i18n;
    private CalendarHelper calendarHelper;

//    private boolean lightTheme;

    private boolean groupRecordByDate;

    private List<Record> recordDataList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.record_list_frag, container, false);
        return rootView;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initArgs();
        initMembers();
        reloadData(null);
    }


    private void initArgs() {
        Bundle args = getArguments();

        pos = args.getInt(ARG_POS, 0);
        periodMode = (PeriodMode) args.getSerializable(ARG_PERIOD_MODE);
        if (periodMode == null) {
            periodMode = PeriodMode.MONTHLY;
        }
        disableSelection = args.getBoolean(ARG_DISABLE_SELECTION, false);
        targetDate = (Date) args.get(ARG_TARGET_DATE);
        if (targetDate == null) {
            targetDate = new Date();
        }
    }

    private void initMembers() {

        ContextsActivity activity = getContextsActivity();
//        lightTheme = activity.isLightTheme();
        //<21, it doesn't support color reference in drawable, so we don't support group effect
        groupRecordByDate = preference().isGroupRecordsByDate() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;

        vNoData = rootView.findViewById(R.id.no_data);
        vNoDataText = rootView.findViewById(R.id.no_data_text);

        recyclerDataList = new LinkedList<>();
        recyclerAdapter = new RecordRecyclerAdapter(activity, recyclerDataList);
        recyclerAdapter.setAccountMap(accountMap);
        recyclerAdapter.setShowRecordDate(periodMode.getValue() >= PeriodMode.YEARLY.getValue() || !groupRecordByDate);
        recyclerAdapter.setShowRecordDateWeekDay(periodMode.getValue() >= PeriodMode.MONTHLY.getValue() && !groupRecordByDate);
        recyclerAdapter.setDisableSelection(disableSelection);
        vRecycler = rootView.findViewById(R.id.record_recycler);
        vRecycler.addItemDecoration(new DividerItemDecoration(activity, DividerItemDecoration.VERTICAL));
        vRecycler.setLayoutManager(new LinearLayoutManager(activity));
        vRecycler.setAdapter(recyclerAdapter);

        recordDataList = new LinkedList<>();

        recyclerAdapter.setOnSelectListener(new SelectableRecyclerViewAdaptor.OnSelectListener<RecordRecyclerAdapter.RecordFolk>() {
            @Override
            public void onSelect(Set<RecordRecyclerAdapter.RecordFolk> selection) {
                lookupQueue().publish(QEvents.RecordListFrag.ON_SELECT_RECORD, selection.size() == 0 ? null : selection.iterator().next().getRecord());
            }

            @Override
            public boolean onReselect(RecordRecyclerAdapter.RecordFolk selected) {
                lookupQueue().publish(QEvents.RecordListFrag.ON_RESELECT_RECORD, selected.getRecord());
                return true;
            }
        });

        accountBgColorMap = activity.getAccountBgColorMap();
        accountTextColorMap = activity.getAccountTextColorMap();
        i18n = Contexts.instance().getI18n();
        calendarHelper = calendarHelper();
    }

    private void reloadData(List<Record> data) {

        final CalendarHelper cal = calendarHelper();
        final IDataProvider idp = contexts().getDataProvider();

        accountMap.clear();
        for (Account acc : idp.listAccount(null)) {
            accountMap.put(acc.getId(), acc);
        }

        recordDataList.clear();
        if (data != null) {
            recordDataList.addAll(data);
        }

        //refresh account
        recyclerAdapter.setAccountMap(accountMap);

        //refresh data
        recyclerDataList.clear();
        if (data == null) {
            recyclerDataList.clear();
            vRecycler.setVisibility(View.GONE);
            vNoData.setVisibility(View.GONE);
        } else if (data.size() == 0) {
            recyclerDataList.clear();
            vRecycler.setVisibility(View.GONE);
            vNoData.setVisibility(View.VISIBLE);
        } else {
            vRecycler.setVisibility(View.VISIBLE);
            vNoData.setVisibility(View.GONE);

            recyclerDataList.addAll(processGroupRecordsByDate(data));
        }
        recyclerAdapter.notifyDataSetChanged();

    }

    private Collection<? extends RecordRecyclerAdapter.RecordFolk> processGroupRecordsByDate(List<Record> data) {
        List<RecordRecyclerAdapter.RecordFolk> folks = new LinkedList<>();

        RecordRecyclerAdapter.RecordHeader lastHeader = null;
        RecordRecyclerAdapter.RecordHeader header;
        for (Record r : data) {
            header = null;

            if (groupRecordByDate) {
                Calendar cal = calendarHelper.calendar(r.getDate());

                boolean diffYear = (lastHeader == null || lastHeader.calendar.get(Calendar.YEAR) != cal.get(Calendar.YEAR));
                boolean diffMonth = diffYear || (lastHeader == null || lastHeader.calendar.get(Calendar.MONTH) != cal.get(Calendar.MONTH));
                boolean diffDay = diffMonth || (lastHeader == null || lastHeader.calendar.get(Calendar.DAY_OF_MONTH) != cal.get(Calendar.DAY_OF_MONTH));

                boolean showYear = periodMode.getValue() == PeriodMode.ALL.getValue() && diffYear;
                boolean showMonth = periodMode.getValue() > PeriodMode.MONTHLY.getValue() && diffMonth;
                boolean showDay = periodMode.getValue() >= PeriodMode.DAILY.getValue() && diffDay;
                //is same header
                if (showYear || showMonth || showDay || lastHeader == null) {
                    //add header
                    header = new RecordRecyclerAdapter.RecordHeader(cal, showYear, showMonth, showDay);
                }
                if (header != null) {
                    if (lastHeader != null) {
                        folks.add(new RecordRecyclerAdapter.RecordFolk(new RecordRecyclerAdapter.RecordFooter()));
                    }
                    folks.add(new RecordRecyclerAdapter.RecordFolk(header));
                    lastHeader = header;
                }
            }
            folks.add(new RecordRecyclerAdapter.RecordFolk(r));
        }
        if (groupRecordByDate && lastHeader != null) {
            folks.add(new RecordRecyclerAdapter.RecordFolk(new RecordRecyclerAdapter.RecordFooter()));
        }

        return folks;
    }

    @Override
    public void onStart() {
        lookupQueue().subscribe(this);
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        lookupQueue().unsubscribe(this);
    }

    @Override
    public void onEvent(EventQueue.Event event) {
        Integer pos = event.getArg(ARG_POS);
        switch (event.getName()) {
            case QEvents.RecordListFrag.ON_CLEAR_SELECTION:
                recyclerAdapter.clearSelection();
                break;
            case QEvents.RecordListFrag.ON_RELOAD_FRAGMENT:
                if (pos != null && pos.intValue() == this.pos) {
                    recyclerAdapter.clearSelection();
                    reloadData((List<Record>) event.getData());
                    return;
                }
                break;
            case QEvents.RecordListFrag.ON_EXPORT_EXCEL:
                if (pos != null && pos.intValue() == this.pos) {
                    exportExcel();
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == Contexts.REQ_EXPORT_EXCEL &&
                Misc.isPermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE, permissions, grantResults)) {
            GUIs.post(new Runnable() {
                @Override
                public void run() {
                    exportExcel();
                }
            });
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void doRequestPermission(int code) {
        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, code);
    }

    private void exportExcel() {

        Contexts contexts = Contexts.instance();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !contexts.hasWorkingFolderPermission()) {
            doRequestPermission(Contexts.REQ_EXPORT_EXCEL);
            return;
        }

        final XlsxRecordExporter exporter = new XlsxRecordExporter(getContextsActivity(), accountMap);

        GUIs.doBusy(getContextsActivity(), new GUIs.BusyAdapter() {

            File destFile;

            @Override
            public void run() {
                Contexts contexts = Contexts.instance();

                File folder = new File(contexts.getWorkingFolder(), Contexts.EXCEL_FOLER_NAME);
                folder.mkdir();

                String sheetName = getContextsActivity().getTitle().toString();
                String subject = Misc.toRecordPeriodInfo(periodMode, targetDate, recordDataList.size());

                Book book = contexts.getMasterDataProvider().findBook(contexts.getWorkingBookId());

                String fileName = book.getName() + "-" + i18n.string(R.string.label_record) + "-" + subject + ".xlsx";
                fileName = Files.normalizeFileName(fileName);

                destFile = new File(folder, fileName);

                exporter.export(sheetName, subject, recordDataList, destFile);
            }

            @Override
            public void onBusyFinish() {

                if (exporter.getErrMsg() == null) {
                    GUIs.alert(getContextsActivity(), i18n.string(R.string.msg_excel_exported, destFile.getAbsoluteFile()));
                } else {
                    GUIs.alert(getContextsActivity(), exporter.getErrMsg());
                }
            }
        });
    }
}