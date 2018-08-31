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

import com.colaorange.commons.util.CalendarHelper;
import com.colaorange.commons.util.Files;
import com.colaorange.commons.util.Formats;
import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.context.Contexts;
import com.colaorange.dailymoney.core.context.ContextsActivity;
import com.colaorange.dailymoney.core.context.ContextsFragment;
import com.colaorange.dailymoney.core.context.EventQueue;
import com.colaorange.dailymoney.core.context.PeriodMode;
import com.colaorange.dailymoney.core.context.Preference;
import com.colaorange.dailymoney.core.data.AccountType;
import com.colaorange.dailymoney.core.data.Balance;
import com.colaorange.dailymoney.core.data.BalanceHelper;
import com.colaorange.dailymoney.core.data.Book;
import com.colaorange.dailymoney.core.ui.GUIs;
import com.colaorange.dailymoney.core.ui.QEvents;
import com.colaorange.dailymoney.core.ui.helper.PeriodInfoFragment;
import com.colaorange.dailymoney.core.ui.helper.SelectableRecyclerViewAdaptor;
import com.colaorange.dailymoney.core.util.I18N;
import com.colaorange.dailymoney.core.util.Misc;
import com.colaorange.dailymoney.core.xlsx.XlsxBalanceExporter;

import java.io.File;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * @author dennis
 */
public class BalanceMgntFragment extends ContextsFragment implements EventQueue.EventListener {

    public static final String ARG_TARGET_DATE = "targetDate";
    public static final String ARG_PERIOD_MODE = "periodMode";
    public static final String ARG_FROM_BEGINNING = "fromBeginning";
    public static final String ARG_POS = "pos";

    private Date targetDate;
    private PeriodMode periodMode;
    private boolean fromBeginning = false;
    private int pos;

    private Date targetStartDate;
    private Date targetEndDate;

    private List<Balance> recyclerDataList;
    private RecyclerView vRecycler;
    private BalanceRecyclerAdapter recyclerAdapter;

    private View rootView;

    private I18N i18n;
    private int decimalLength = 0;

    static Set<PeriodMode> supportPeriod = com.colaorange.commons.util.Collections.asSet(PeriodMode.MONTHLY, PeriodMode.YEARLY);

    private static int EXPORT_EXCEL_REQ_CODE = 101;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.balance_mgnt_frag, container, false);
        return rootView;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initArgs();
        initMembers();
        reloadData();
    }


    private void initArgs() {
        Bundle args = getArguments();
        periodMode = (PeriodMode) args.getSerializable(ARG_PERIOD_MODE);
        if (periodMode == null) {
            periodMode = PeriodMode.MONTHLY;
        }

        if (!supportPeriod.contains(periodMode)) {
            throw new IllegalStateException("unsupported period " + periodMode);
        }

        pos = args.getInt(ARG_POS, 0);
        fromBeginning = args.getBoolean(ARG_FROM_BEGINNING, false);
        Object o = args.get(ARG_TARGET_DATE);
        if (o instanceof Date) {
            targetDate = (Date) o;
        } else {
            targetDate = new Date();
        }
    }

    private void initMembers() {
        i18n = i18n();
        ContextsActivity activity = getContextsActivity();

        Preference pref = preference();

        recyclerDataList = new LinkedList<>();
        recyclerAdapter = new BalanceRecyclerAdapter(activity, recyclerDataList);
        vRecycler = rootView.findViewById(R.id.balance_recycler);
        vRecycler.addItemDecoration(new DividerItemDecoration(activity, DividerItemDecoration.VERTICAL));
        vRecycler.setLayoutManager(new LinearLayoutManager(activity));
        vRecycler.setAdapter(recyclerAdapter);


        recyclerAdapter.setOnSelectListener(new SelectableRecyclerViewAdaptor.OnSelectListener<Balance>() {
            @Override
            public void onSelect(Set<Balance> selection) {
                lookupQueue().publish(QEvents.BalanceMgntFrag.ON_SELECT_BALANCE, selection.size() == 0 ? null : selection.iterator().next());
            }

            @Override
            public boolean onReselect(Balance selected) {
                lookupQueue().publish(QEvents.BalanceMgntFrag.ON_RESELECT_BALANCE, selected);
                return true;
            }
        });

        String fragTag = "periodInfo";
        if (getChildFragmentManager().findFragmentByTag(fragTag) == null) {
            PeriodInfoFragment f = new PeriodInfoFragment();
            Bundle b = new Bundle();
            b.putBoolean(PeriodInfoFragment.ARG_FROM_BEGINNING, fromBeginning);
            b.putSerializable(PeriodInfoFragment.ARG_PERIOD_MODE, periodMode);
            b.putSerializable(PeriodInfoFragment.ARG_TARGET_DATE, targetDate);
            f.setArguments(b);

            getChildFragmentManager().beginTransaction()
                    .add(R.id.frag_container, f, fragTag)
                    .disallowAddToBackStack()
                    .commit();
        }

    }

    private void reloadData() {
        CalendarHelper cal = calendarHelper();
        targetEndDate = null;
        targetStartDate = null;
        decimalLength = 0;
        switch (periodMode) {
            case YEARLY:
                targetEndDate = cal.yearEndDate(targetDate);
                targetStartDate = fromBeginning ? null : cal.yearStartDate(targetDate);
                break;
            default:
                targetEndDate = cal.monthEndDate(targetDate);
                targetStartDate = fromBeginning ? null : cal.monthStartDate(targetDate);
                break;
        }

        GUIs.doBusy(getContextsActivity(), new GUIs.BusyAdapter() {
            List<Balance> all = new ArrayList<Balance>();

            @Override
            public void run() {
                boolean hierarchical = preference().isHierarchicalBalance();

                List<Balance> asset = BalanceHelper.calculateBalanceList(AccountType.ASSET, targetStartDate, targetEndDate);
                List<Balance> income = BalanceHelper.calculateBalanceList(AccountType.INCOME, targetStartDate, targetEndDate);
                List<Balance> expense = BalanceHelper.calculateBalanceList(AccountType.EXPENSE, targetStartDate, targetEndDate);
                List<Balance> liability = BalanceHelper.calculateBalanceList(AccountType.LIABILITY, targetStartDate, targetEndDate);
                List<Balance> other = BalanceHelper.calculateBalanceList(AccountType.OTHER, targetStartDate, targetEndDate);


                if (hierarchical) {
                    asset = BalanceHelper.adjustNestedTotalBalance(AccountType.ASSET, fromBeginning ? i18n.string(R.string.label_balance_from_beginning_asset)
                            : i18n.string(R.string.label_asset), asset);
                    income = BalanceHelper.adjustNestedTotalBalance(AccountType.INCOME, fromBeginning ? i18n.string(R.string.label_balance_from_beginning_income)
                            : i18n.string(R.string.label_income), income);
                    expense = BalanceHelper.adjustNestedTotalBalance(
                            AccountType.EXPENSE,
                            fromBeginning ? i18n.string(R.string.label_balance_from_beginning_expense) : i18n
                                    .string(R.string.label_expense), expense);
                    liability = BalanceHelper.adjustNestedTotalBalance(
                            AccountType.LIABILITY,
                            fromBeginning ? i18n.string(R.string.label_balance_from_beginning_liability) : i18n
                                    .string(R.string.label_liability), liability);
                    other = BalanceHelper.adjustNestedTotalBalance(AccountType.OTHER, fromBeginning ? i18n.string(R.string.label_balance_from_beginning_other)
                            : i18n.string(R.string.label_other), other);

                } else {
                    asset = BalanceHelper.adjustTotalBalance(AccountType.ASSET, fromBeginning ? i18n.string(R.string.label_balance_from_beginning_asset)
                            : i18n.string(R.string.label_asset), asset);
                    income = BalanceHelper.adjustTotalBalance(AccountType.INCOME, fromBeginning ? i18n.string(R.string.label_balance_from_beginning_income)
                            : i18n.string(R.string.label_income), income);
                    expense = BalanceHelper.adjustTotalBalance(
                            AccountType.EXPENSE,
                            fromBeginning ? i18n.string(R.string.label_balance_from_beginning_expense) : i18n
                                    .string(R.string.label_expense), expense);
                    liability = BalanceHelper.adjustTotalBalance(
                            AccountType.LIABILITY,
                            fromBeginning ? i18n.string(R.string.label_balance_from_beginning_liability) : i18n
                                    .string(R.string.label_liability), liability);
                    other = BalanceHelper.adjustTotalBalance(AccountType.OTHER, fromBeginning ? i18n.string(R.string.label_balance_from_beginning_other)
                            : i18n.string(R.string.label_other), other);

                }

                if (fromBeginning) {
                    all.addAll(asset);
                    all.addAll(liability);
                    all.addAll(income);
                    all.addAll(expense);
                    all.addAll(other);
                } else {
                    all.addAll(income);
                    all.addAll(expense);
                    all.addAll(asset);
                    all.addAll(liability);
                    all.addAll(other);
                }

                DecimalFormat df = Formats.getMoneyFormat();
                for (Balance b : all) {
                    decimalLength = Math.max(decimalLength, Formats.getDecimalLength(df, b.getMoney()));
                }
            }

            @Override
            public void onBusyFinish() {
                I18N i18n = i18n();
                CalendarHelper cal = calendarHelper();

                recyclerDataList.clear();
                recyclerDataList.addAll(all);
                recyclerAdapter.setDecimalLength(decimalLength);
                recyclerAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onStart() {
        lookupQueue().subscribe(this);
        super.onStart();

        EventQueue.EventBuilder eb = new EventQueue.EventBuilder(QEvents.BalanceMgntFrag.ON_FRAGMENT_START);
        eb.withData(new FragInfo(pos, targetDate, targetStartDate, targetEndDate));
        lookupQueue().publish(eb.build());

    }

    @Override
    public void onStop() {
        super.onStop();
        EventQueue.EventBuilder eb = new EventQueue.EventBuilder(QEvents.BalanceMgntFrag.ON_FRAGMENT_STOP);
        eb.withData(pos);
        lookupQueue().publish(eb.build());

        lookupQueue().unsubscribe(this);

    }

    @Override
    public void onEvent(EventQueue.Event event) {
        switch (event.getName()) {
            case QEvents.BalanceMgntFrag.ON_CLEAR_SELECTION:
                recyclerAdapter.clearSelection();
                break;
            case QEvents.BalanceMgntFrag.ON_RELOAD_FRAGMENT:
                if (event.getArg(ARG_POS) == null || pos == ((Integer) event.getArg(ARG_POS)).intValue()) {
                    reloadData();
                }
                break;
            case QEvents.BalanceMgntFrag.ON_EXPORT_EXCEL:
                if (event.getArg(ARG_POS) == null || pos == ((Integer) event.getArg(ARG_POS)).intValue()) {
                    exportExcel();
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == EXPORT_EXCEL_REQ_CODE &&
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
            doRequestPermission(EXPORT_EXCEL_REQ_CODE);
            return;
        }

        final XlsxBalanceExporter exporter = new XlsxBalanceExporter(getContextsActivity());

        GUIs.doBusy(getContextsActivity(), new GUIs.BusyAdapter() {

            File destFile;

            @Override
            public void run() {
                Contexts contexts = Contexts.instance();

                File folder = new File(contexts.getWorkingFolder(), Contexts.EXCEL_FOLER_NAME);
                folder.mkdir();

                String sheetName = getContextsActivity().getTitle().toString();
                String subject = Misc.toPeriodInfo(periodMode, targetDate, fromBeginning);

                Book book = contexts.getMasterDataProvider().findBook(contexts.getWorkingBookId());

                String fileName = book.getName() + "-" + subject + ".xlsx";
                fileName = Files.normalizeFileName(fileName);

                destFile = new File(folder, fileName);

                exporter.export(sheetName, subject, recyclerDataList, destFile);
            }

            @Override
            public void onBusyFinish() {

                if (exporter.getErrMsg() == null) {
                    GUIs.longToast(getContextsActivity(), i18n.string(R.string.msg_excel_exported, destFile.getAbsoluteFile()));
                } else {
                    GUIs.longToast(getContextsActivity(), exporter.getErrMsg());
                }
            }
        });
    }

    public static class FragInfo implements Serializable {
        final public int pos;
        final public Date date;
        final public Date startDate;
        final public Date endDate;

        public FragInfo(int pos, Date date, Date startDate, Date endDate) {
            this.pos = pos;
            this.date = date;
            this.startDate = startDate;
            this.endDate = endDate;
        }
    }
}