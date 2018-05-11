package com.colaorange.dailymoney.core.ui.legacy;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.colaorange.commons.util.CalendarHelper;
import com.colaorange.dailymoney.core.data.Record;
import com.colaorange.dailymoney.core.util.GUIs;
import com.colaorange.dailymoney.core.util.I18N;
import com.colaorange.dailymoney.core.context.ContextsActivity;
import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.context.Preference;
import com.colaorange.dailymoney.core.data.AccountType;
import com.colaorange.dailymoney.core.data.IDataProvider;
import com.colaorange.dailymoney.core.ui.Constants;

/**
 * @author dennis
 */
public class RecordlListActivity extends ContextsActivity implements OnClickListener {

    public static final int MODE_DAY = 0;
    public static final int MODE_WEEK = 1;
    public static final int MODE_MONTH = 2;
    public static final int MODE_YEAR = 3;
    public static final int MODE_ALL = 4;

    public static final String PARAM_MODE = "dtlist.mode";
    public static final String PARAM_TARGET_DATE = "dtlist.target";

    RecordListHelper recordListHelper;

    TextView vInfo;
    TextView vSumIncome;
    TextView vSumExpense;
    TextView vSumAsset;
    TextView vSumLiability;
    TextView vSumOther;
    TextView vSumUnknow;

    View toolbarView;

    private Date targetDate;
    private Date currentDate;
    private int mode = MODE_WEEK;

    private DateFormat dateFormat;
//    private DateFormat monthDateFormat;
    private DateFormat yearMonthFormat;
    private DateFormat yearFormat;
    private DateFormat nonDigitalMonthFormat;

    ImageButton btnMode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.record_list);
        initArgs();
        initMembers();
        refreshUI();
    }


    private void initArgs() {
        Bundle b = getIntentExtras();
        mode = b.getInt(PARAM_MODE, MODE_WEEK);
        Object o = b.get(PARAM_TARGET_DATE);
        if (o instanceof Date) {
            targetDate = (Date) o;
        } else {
            targetDate = new Date();
        }
        currentDate = targetDate;
    }

    private void initMembers() {
        Preference preference = preference();
        dateFormat = preference.getDateFormat();//new SimpleDateFormat("yyyy/MM/dd");
//        monthDateFormat = preference.getMonthDateFormat();//new SimpleDateFormat("MM/dd");
        yearMonthFormat = preference.getYearMonthFormat();//new SimpleDateFormat("yyyy/MM - MMM");
        yearFormat = preference.getYearFormat();//new SimpleDateFormat("yyyy");
        nonDigitalMonthFormat = preference.getNonDigitalMonthFormat();

        vInfo = findViewById(R.id.record_list_infobar);
        toolbarView = findViewById(R.id.record_list_toolbar);
        vSumIncome = findViewById(R.id.sum_income);
        vSumExpense = findViewById(R.id.sum_expense);
        vSumAsset = findViewById(R.id.sum_asset);
        vSumLiability = findViewById(R.id.sum_liability);
        vSumOther = findViewById(R.id.sum_other);
        vSumUnknow = findViewById(R.id.sum_unknow);
        btnMode = findViewById(R.id.toolbar_btn_mode);

        btnMode.setOnClickListener(this);

        recordListHelper = new RecordListHelper(this, true, new RecordListHelper.OnRecordListener() {
            @Override
            public void onRecordDeleted(Record record) {
                GUIs.shortToast(RecordlListActivity.this, i18n().string(R.string.msg_record_deleted));
                refreshUI();
                trackEvent(TE.DELETE_RECORD);
            }
        });
        ListView listView = findViewById(R.id.record_list_list);
        recordListHelper.setup(listView);


        findViewById(R.id.toolbar_btn_prev).setOnClickListener(this);
        findViewById(R.id.toolbar_btn_next).setOnClickListener(this);
        findViewById(R.id.toolbar_btn_today).setOnClickListener(this);

        registerForContextMenu(listView);
    }

    private void refreshUI() {
        refreshToolbar();

        GUIs.delayPost(new Runnable() {
            @Override
            public void run() {
                refreshData();
            }
        }, 25);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQUEST_RECORD_EDITOR_CODE && resultCode == Activity.RESULT_OK) {
            GUIs.delayPost(new Runnable() {
                @Override
                public void run() {
                    refreshData();
                }
            });

        }
    }

    private void refreshToolbar() {

        toolbarView.setVisibility(TextView.VISIBLE);
        btnMode.setVisibility(ImageButton.VISIBLE);

        switch (mode) {
            case MODE_ALL:
                toolbarView.setVisibility(TextView.GONE);
                break;
            case MODE_MONTH:
                setTitle(R.string.dtitem_reclist_month);
                break;
            case MODE_WEEK:
                setTitle(R.string.dtitem_reclist_week);
                break;
            case MODE_DAY:
                setTitle(R.string.dtitem_reclist_day);
                break;
            case MODE_YEAR:
                setTitle(R.string.dtitem_reclist_year);
                break;
            default:
                setTitle(R.string.dtitem_reclist_month);
                break;
        }
    }


    private void refreshData() {
        final CalendarHelper cal = calendarHelper();
        final Date start;
        final Date end;
        vInfo.setText("");
        refreshToolbar();
        vSumIncome.setVisibility(TextView.GONE);
        vSumExpense.setVisibility(TextView.GONE);
        vSumAsset.setVisibility(TextView.GONE);
        vSumLiability.setVisibility(TextView.GONE);
        vSumOther.setVisibility(TextView.GONE);

        vSumUnknow.setVisibility(TextView.VISIBLE);

        trackEvent(TE.RECORD_LIST + mode);
        switch (mode) {
            case MODE_ALL:
                start = end = null;
                break;
            case MODE_MONTH:
                start = cal.monthStartDate(currentDate);
                end = cal.monthEndDate(currentDate);
                break;
            case MODE_DAY:
                start = cal.toDayStart(currentDate);
                end = cal.toDayEnd(currentDate);
                break;
            case MODE_YEAR:
                start = cal.yearStartDate(currentDate);
                end = cal.yearEndDate(currentDate);

                break;
            case MODE_WEEK:
            default:
                start = cal.weekStartDate(currentDate);
                end = cal.weekEndDate(currentDate);
                break;
        }
        final IDataProvider idp = contexts().getDataProvider();

        final boolean sameYear = cal.isSameYear(start, end);
        final boolean sameMonth = cal.isSameMonth(start, end);

//        recordListHelper.refreshData(idp.listAllRecord());
        GUIs.doBusy(this, new GUIs.BusyAdapter() {
            List<Record> data = null;

            double expense;
            double income;
            double asset;
            double liability;
            double other;
            int count;

            @Override
            public void run() {
                data = idp.listRecord(start, end, preference().getMaxRecords());
                count = idp.countRecord(start, end);
                income = idp.sumFrom(AccountType.INCOME, start, end);
                expense = idp.sumTo(AccountType.EXPENSE, start, end);//nagivate
                asset = idp.sumTo(AccountType.ASSET, start, end) - idp.sumFrom(AccountType.ASSET, start, end);
                liability = idp.sumTo(AccountType.LIABILITY, start, end) - idp.sumFrom(AccountType.LIABILITY, start, end);
                liability = -liability;
                other = idp.sumTo(AccountType.OTHER, start, end) - idp.sumFrom(AccountType.OTHER, start, end);
            }

            @Override
            public void onBusyFinish() {
                CalendarHelper cal = calendarHelper();
                I18N i18n = i18n();

                vSumUnknow.setVisibility(TextView.GONE);
                //update data
                recordListHelper.reloadData(data);
                if (income != 0) {
                    vSumIncome.setText(i18n.string(R.string.label_reclist_sum_income, contexts().toFormattedMoneyString((income))));
                    vSumIncome.setVisibility(TextView.VISIBLE);
                }
                if (expense != 0) {
                    vSumExpense.setText(i18n.string(R.string.label_reclist_sum_expense, contexts().toFormattedMoneyString((expense))));
                    vSumExpense.setVisibility(TextView.VISIBLE);
                }
                if (asset != 0) {
                    vSumAsset.setText(i18n.string(R.string.label_reclist_sum_asset, contexts().toFormattedMoneyString((asset))));
                    vSumAsset.setVisibility(TextView.VISIBLE);
                }
                if (liability != 0) {
                    vSumLiability.setText(i18n.string(R.string.label_reclist_sum_liability, contexts().toFormattedMoneyString((liability))));
                    vSumLiability.setVisibility(TextView.VISIBLE);
                }
                if (other != 0) {
                    vSumOther.setText(i18n.string(R.string.label_reclist_sum_other, contexts().toFormattedMoneyString((other))));
                    vSumOther.setVisibility(TextView.VISIBLE);
                }

                StringBuilder sb = new StringBuilder();
                //update info
                switch (mode) {
                    case MODE_ALL:
                        vInfo.setText(i18n.string(R.string.label_all_records, Integer.toString(count)));
                        break;
                    case MODE_MONTH:
                        //<string name="label_month_details">%1$s (%2$s records)</string>
                        if (sameMonth) {
                            sb.append(yearMonthFormat.format(start));
                        }else {
                            sb.append(yearFormat.format(start));
                            sb.append(" ").append(nonDigitalMonthFormat.format(start)).append(", ").append(cal.dayOfMonth(start)).append(" - ")
                                    .append(nonDigitalMonthFormat.format(end)).append(" ").append(cal.dayOfMonth(end));
                        }
                        vInfo.setText(i18n.string(R.string.label_month_details, sb.toString(), Integer.toString(count)));
                        break;
                    case MODE_DAY:
                        vInfo.setText(i18n.string(R.string.label_day_records, dateFormat.format(currentDate), Integer.toString(count)));
                        break;
                    case MODE_YEAR:

                        //<string name="label_year_details">%1$s (%2$s records)</string>
                        if (sameYear) {
                            sb.append(yearFormat.format(start));
                        } else {
                            sb.append(yearFormat.format(start));
                            sb.append(", ").append(nonDigitalMonthFormat.format(start)).append(" ").append(cal.dayOfMonth(start)).append(" - ")
                                    .append(yearFormat.format(end)).append(" ").append(nonDigitalMonthFormat.format(end)).append(" ").append(cal.dayOfMonth(end));
                        }

                        vInfo.setText(i18n.string(R.string.label_year_details, sb.toString(), Integer.toString(count)));
                        break;
                    case MODE_WEEK:
                    default:
                        //<string name="label_week_details">%5$s %1$s to %2$s - Week %3$s/%4$s (%6$s)</string>
                        vInfo.setText(i18n.string(R.string.label_week_details, nonDigitalMonthFormat.format(start) + " " + cal.dayOfMonth(start),
                                (!sameMonth ? nonDigitalMonthFormat.format(end) + " " : "") + cal.dayOfMonth(end),
                                cal.weekOfMonth(currentDate), cal.weekOfYear(currentDate), yearFormat.format(start), Integer.toString(count)));
                        break;
                }

            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.record_list_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_new) {
            recordListHelper.doNewRecord(currentDate);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.record_list_list) {
            getMenuInflater().inflate(R.menu.record_list_ctxmenu, menu);
        }

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        if (item.getItemId() == R.id.menu_edit) {
            recordListHelper.doEditRecord(info.position);
            return true;
        } else if (item.getItemId() == R.id.menu_delete) {
            recordListHelper.doDeleteRecord(info.position);
            return true;
        } else if (item.getItemId() == R.id.menu_copy) {
            recordListHelper.doCopyRecord(info.position);
            return true;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.toolbar_btn_prev) {
            onPrev();
        } else if (v.getId() == R.id.toolbar_btn_next) {
            onNext();
        } else if (v.getId() == R.id.toolbar_btn_today) {
            onToday();
        } else if (v.getId() == R.id.toolbar_btn_mode) {
            onMode();
        }
    }

    private void onMode() {
        switch (mode) {
            case MODE_WEEK:
                mode = MODE_MONTH;
                break;
            case MODE_DAY:
                mode = MODE_WEEK;
                break;
            case MODE_MONTH:
                mode = MODE_YEAR;
                break;
            case MODE_YEAR:
                mode = MODE_DAY;
                break;
        }
        refreshData();

    }

    private void onNext() {
        CalendarHelper cal = calendarHelper();
        switch (mode) {
            case MODE_DAY:
                currentDate = cal.dateAfter(currentDate, 1);
                break;
            case MODE_WEEK:
                currentDate = cal.dateAfter(currentDate, 7);
                break;
            case MODE_MONTH:
                currentDate = cal.monthAfter(currentDate, 1);
                break;
            case MODE_YEAR:
                currentDate = cal.yearAfter(currentDate, 1);
                break;
        }
        refreshData();
    }

    private void onPrev() {
        CalendarHelper cal = calendarHelper();
        switch (mode) {
            case MODE_DAY:
                currentDate = cal.dateBefore(currentDate, 1);
                break;
            case MODE_WEEK:
                currentDate = cal.dateBefore(currentDate, 7);
                break;
            case MODE_MONTH:
                currentDate = cal.monthBefore(currentDate, 1);
                break;
            case MODE_YEAR:
                currentDate = cal.yearBefore(currentDate, 1);
                break;
        }
        refreshData();
    }

    private void onToday() {
        switch (mode) {
            case MODE_WEEK:
            case MODE_MONTH:
            case MODE_DAY:
            case MODE_YEAR:
                currentDate = targetDate;
                break;
        }
        refreshData();
    }

}
