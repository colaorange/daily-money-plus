package com.colaorange.dailymoney.core.ui.legacy;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.colaorange.commons.util.CalendarHelper;
import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.context.Contexts;
import com.colaorange.dailymoney.core.context.ContextsActivity;
import com.colaorange.dailymoney.core.context.EventQueue;
import com.colaorange.dailymoney.core.data.Account;
import com.colaorange.dailymoney.core.data.AccountType;
import com.colaorange.dailymoney.core.data.Book;
import com.colaorange.dailymoney.core.data.IDataProvider;
import com.colaorange.dailymoney.core.data.IMasterDataProvider;
import com.colaorange.dailymoney.core.data.Record;
import com.colaorange.dailymoney.core.ui.GUIs;
import com.colaorange.dailymoney.core.ui.QEvents;
import com.colaorange.dailymoney.core.ui.RegularSpinnerAdapter;
import com.colaorange.dailymoney.core.util.I18N;

import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by Dennis
 */
public class RecordMigratorActivity extends ContextsActivity implements View.OnClickListener, EventQueue.EventListener {

    private DateFormat dateFormat;

    private List<Book> bookList;

    private RegularSpinnerAdapter<Book> bookAdapter;

    private CollapsingToolbarLayout collapsingToolbar;
    private Spinner vBook;

    private EditText vToDate;
    private View vPreviewhHint;
    private TabLayout vTabs;
    private View vTabsPadding;
    private ViewPager vPager;

    private I18N i18n;
    private int workingBookId;

    private Book destBook;
    private List<Record> srcRecordList = new LinkedList<>();
    private List<ReviewAccount> newAccountList = new LinkedList<>();
    private List<ReviewAccount> updateAccountList = new LinkedList<>();

    private int srcRecordListProgress;
    private int newAccountListProgress;
    private int updateAccountListProgress;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.record_migrator);
        initArgs();
        initMembers();
        refreshSpinner();
        refreshTabPager();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.record_migrator_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (this.isProcessing()) {
            this.toastProcessing();
            return true;
        }

        if (item.getItemId() == R.id.menu_reset) {
            doReset();
        }
        return super.onOptionsItemSelected(item);
    }

    private void initArgs() {

    }


    private void initMembers() {
        i18n = i18n();
        workingBookId = Contexts.instance().getWorkingBookId();
        dateFormat = preference().getDateFormat();

        collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.appCollapsingToolbar);
//        collapsingToolbar.setExpandedTitleColor(getResources().getColor(android.R.color.transparent));

        vToDate = findViewById(R.id.record_to_date);
        vToDate.setText(dateFormat.format(new Date()));

        vPreviewhHint = findViewById(R.id.preview_hint);

        findViewById(R.id.btn_to_datepicker).setOnClickListener(this);
        findViewById(R.id.fab_preview).setOnClickListener(this);

        String nullText = " ";

        vBook = findViewById(R.id.record_to_book);
        vBook.setSelection(-1);
        bookList = new LinkedList<>();
        bookAdapter = new RegularSpinnerAdapter<Book>(this, bookList) {
            @Override
            public ViewHolder<Book> createViewHolder() {
                return new ViewHolder<Book>(this) {
                    @Override
                    public void bindViewValue(Book item, LinearLayout layout, TextView text, boolean isDropdown, boolean isSelected) {
                        if (item == null) {
                            text.setText(i18n.string(R.string.label_a_new_book));
                        } else {
                            text.setText(item.getName());
                        }
                    }
                };
            }

            @Override
            public boolean isSelected(int position) {
                return vBook.getSelectedItemPosition() == position;
            }
        };
        vBook.setAdapter(bookAdapter);


        vTabs = findViewById(R.id.tabs);
        vTabsPadding = findViewById(R.id.tabs_padding);
        vPager = findViewById(R.id.viewpager);

        vTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int pos = vTabs.getSelectedTabPosition();
                switch (pos) {
                    case 0:
                        publishReloadStep1Data();
                        break;
                    case 1:
                        publishReloadStep2Data();
                        break;
                    case 2:
                        publishReloadStep3Data();
                        break;
                    case 3:
                        publishReloadStep4Data();
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
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
        switch (event.getName()) {
            case QEvents.RecordMigratorFrag.ON_MIGRATE:
                doMigrate();
        }
    }

    private void refreshTabPager() {
        vPager.setAdapter(new MigratorStepsAdaptor(getSupportFragmentManager()));
        vTabs.setupWithViewPager(vPager);
    }

    private void refreshSpinner() {
        IMasterDataProvider imdp = contexts().getMasterDataProvider();

        bookList.clear();
        bookList.add(null);


        for (Book book : imdp.listAllBook()) {
            if (book.getId() == workingBookId) {
                continue;
            }
            bookList.add(book);
        }
        bookAdapter.notifyDataSetChanged();
    }


    @Override
    public void onClick(View v) {
        if (this.isProcessing()) {
            this.toastProcessing();
            return;
        }
        final int id = v.getId();
        CalendarHelper cal = calendarHelper();
        if (id == R.id.btn_from_datepicker || v.getId() == R.id.btn_to_datepicker) {
            Date d = null;
            try {
                d = dateFormat.parse(vToDate.getText().toString());
            } catch (ParseException e) {
                d = new Date();
            }
            GUIs.openDatePicker(this, d, new GUIs.OnFinishListener() {
                @Override
                public boolean onFinish(int which, Object data) {
                    if (which == GUIs.OK_BUTTON) {
                        vToDate.setText(dateFormat.format((Date) data));
                    }
                    return true;
                }
            });

        } else if (id == R.id.fab_preview) {
            doPreview(true);
        }
    }

    private void doReset() {
        vBook.setSelection(0);
        vToDate.setText("");
        vPreviewhHint.setVisibility(View.VISIBLE);
        vTabs.setVisibility(View.GONE);
        vTabsPadding.setVisibility(View.GONE);
        vPager.setVisibility(View.GONE);

        srcRecordList = new LinkedList<>();
        newAccountList = new LinkedList<>();
        updateAccountList = new LinkedList<>();
        destBook = null;

        publishReloadStep1Data();
        publishReloadStep2Data();
        publishReloadStep3Data();
        publishReloadStep4Data();

        ((AppBarLayout) findViewById(R.id.appbar)).setExpanded(true);

        setTitle(i18n.string(R.string.label_migrate));
    }


    private void doPreview(boolean collapse) {
        destBook = bookList.get(vBook.getSelectedItemPosition());

        String toDateText = vToDate.getText().toString();

        boolean anyCondition = false;
        Date toDate = null;

        try {
            toDate = dateFormat.parse(toDateText);
            toDate = calendarHelper().toDayEnd(toDate);
        } catch (ParseException e) {
        }


        anyCondition = toDate != null;

        if (!anyCondition) {
            GUIs.shortToast(this, i18n.string(R.string.msg_no_condition));
            return;
        }

        final IDataProvider.SearchCondition condition = new IDataProvider.SearchCondition();
        condition.withToDate(toDate);

        if (collapse) {
            ((AppBarLayout) findViewById(R.id.appbar)).setExpanded(false);
        }

        vPreviewhHint.setVisibility(View.GONE);
        vTabs.setVisibility(View.VISIBLE);
        vTabsPadding.setVisibility(View.VISIBLE);
        vPager.setVisibility(View.VISIBLE);


        GUIs.doBusy(this, new GUIs.BusyAdapter() {

            @Override
            public void run() {
                final IDataProvider idp = contexts().getDataProvider();

                srcRecordList = idp.searchRecord(condition, preference().getMaxRecords());
                newAccountList = new LinkedList<>();

                Map<String, Account> accountMap = new HashMap<>();
                for (AccountType type : AccountType.getSupportedType()) {
                    for (Account a : idp.listAccount(type)) {
                        accountMap.put(a.getId(), a);

                    }
                }

                Map<String, ReviewAccount> updateAccountMap = new HashMap<>();

                for (Record r : srcRecordList) {
                    Account fromAcc = accountMap.get(r.getFrom());
                    Account toAcc = accountMap.get(r.getTo());
                    if (fromAcc != null) {
                        ReviewAccount racc = updateAccountMap.get(fromAcc.getId());
                        if (racc == null) {
                            updateAccountMap.put(fromAcc.getId(), racc = new ReviewAccount(fromAcc));
                        }

                        racc.newInitialValue -= r.getMoney();
                    }
                    if (toAcc != null) {
                        ReviewAccount racc = updateAccountMap.get(toAcc.getId());
                        if (racc == null) {
                            updateAccountMap.put(toAcc.getId(), racc = new ReviewAccount(toAcc));
                        }

                        racc.newInitialValue += r.getMoney();
                    }
                }
                updateAccountList = new ArrayList<>(updateAccountMap.values());

                //remove the account that already in target book
                if (destBook != null) {
                    IDataProvider nidp = contexts().newDataProvider(destBook.getId());
                    try {
                        for (AccountType type : AccountType.getSupportedType()) {
                            for (Account a : nidp.listAccount(type)) {
                                if (updateAccountMap.containsKey(a.getId())) {
                                    updateAccountMap.remove(a.getId());
                                }
                            }
                        }
                    } finally {
                        nidp.close();
                    }
                }
                newAccountList = new ArrayList<>(updateAccountMap.values().size());
                for (ReviewAccount acc : updateAccountMap.values()) {
                    //use original initial value for new
                    newAccountList.add(new ReviewAccount(acc.account));
                }


                final Map<String, Integer> priority = new HashMap<>();
                int i = 0;
                for (AccountType t : AccountType.getSupportedType()) {
                    priority.put(t.getType(), i++);
                }

                //sort by
                Comparator<ReviewAccount> comparator = new Comparator<ReviewAccount>() {
                    @Override
                    public int compare(ReviewAccount o1, ReviewAccount o2) {
                        Integer p1 = priority.get(o1.account.getType());
                        Integer p2 = priority.get(o2.account.getType());
                        int c = p1.compareTo(p2);
                        if (c != 0) {
                            return c;
                        }
                        return o1.account.getName().compareTo(o2.account.getName());
                    }
                };
                Collections.sort(newAccountList, comparator);
                Collections.sort(updateAccountList, comparator);
            }

            @Override
            public void onBusyFinish() {
                publishReloadStep1Data();
                publishReloadStep2Data();
                publishReloadStep3Data();
                publishReloadStep4Data();

                int count = srcRecordList.size() + newAccountList.size() + updateAccountList.size();
                setTitle(i18n.string(R.string.label_migrate) + " - " + i18n.string(R.string.msg_n_items, count));
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void publishReloadStep1Data() {
        lookupQueue().publish(new EventQueue.EventBuilder(QEvents.RecordListFrag.ON_RELOAD_FRAGMENT).withData(srcRecordList).withArg(RecordListFragment.ARG_POS, 0).build());
    }

    private void publishReloadStep2Data() {
        lookupQueue().publish(new EventQueue.EventBuilder(QEvents.RecordMigratorReviewAccountListFrag.ON_RELOAD_FRAGMENT).withData(newAccountList).withArg(RecordListFragment.ARG_POS, 1).build());
    }

    private void publishReloadStep3Data() {
        lookupQueue().publish(new EventQueue.EventBuilder(QEvents.RecordMigratorReviewAccountListFrag.ON_RELOAD_FRAGMENT).withData(updateAccountList).withArg(RecordListFragment.ARG_POS, 2).build());
    }

    private void publishReloadStep4Data() {
        String bookName = destBook == null ? i18n.string(R.string.label_a_new_book) : destBook.getName();

        Indicator indicator = new Indicator(isProcessing(), bookName,
                srcRecordList.size(), srcRecordListProgress,
                newAccountList.size(), newAccountListProgress,
                updateAccountList.size(), updateAccountListProgress);

        lookupQueue().publish(new EventQueue.EventBuilder(QEvents.RecordMigratorIndicatorFrag.ON_RELOAD_FRAGMENT).withData(indicator).build());
    }

    public class MigratorStepsAdaptor extends FragmentPagerAdapter {

        public MigratorStepsAdaptor(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                //step1
                RecordListFragment frag = new RecordListFragment();
                Bundle b = new Bundle();
                b.putInt(RecordListFragment.ARG_MODE, RecordListFragment.MODE_ALL);
                b.putInt(RecordListFragment.ARG_POS, position);
                b.putBoolean(RecordListFragment.ARG_DISABLE_SELECTION, true);
                frag.setArguments(b);
                return frag;
            } else if (position == 1) {
                //step2
                RecordMigratorReviewAccountListFragment frag = new RecordMigratorReviewAccountListFragment();
                Bundle b = new Bundle();
                b.putSerializable(RecordMigratorReviewAccountListFragment.ARG_STEP_MODE, RecordMigratorReviewAccountListFragment.StepMode.CREATE_NEW);
                b.putInt(RecordMigratorReviewAccountListFragment.ARG_POS, position);
                b.putBoolean(RecordMigratorReviewAccountListFragment.ARG_DISABLE_SELECTION, true);
                frag.setArguments(b);
                return frag;
            } else if (position == 2) {
                //step3
                RecordMigratorReviewAccountListFragment frag = new RecordMigratorReviewAccountListFragment();
                Bundle b = new Bundle();
                b.putSerializable(RecordMigratorReviewAccountListFragment.ARG_STEP_MODE, RecordMigratorReviewAccountListFragment.StepMode.UPDATE_EXISTING);
                b.putInt(RecordMigratorReviewAccountListFragment.ARG_POS, position);
                b.putBoolean(RecordMigratorReviewAccountListFragment.ARG_DISABLE_SELECTION, true);
                frag.setArguments(b);
                return frag;
            } else if (position == 3) {
                //step4
                RecordMigratorIndicatorFragment frag = new RecordMigratorIndicatorFragment();
                Bundle b = new Bundle();
                frag.setArguments(b);
                return frag;
            }
            return null;
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return i18n.string(R.string.label_records);
                case 1:
                    return i18n.string(R.string.label_record_migrate_create_accounts);
                case 2:
                    return i18n.string(R.string.label_record_migrate_update_accounts);
                case 3:
                    return i18n.string(R.string.label_migrate);
            }
            return Integer.toString(position);
        }
    }

    public static class ReviewAccount {
        Account account;
        double newInitialValue;

        public ReviewAccount(Account account) {
            this.account = account;
            newInitialValue = account.getInitialValue();
        }
    }

    public static class Indicator {
        boolean processing;
        String bookName;
        int srcRecordListSize;
        int newAccountListSize;
        int updateAccountListSize;
        int srcRecordListProgress;
        int newAccountListProgress;
        int updateAccountListProgress;

        public Indicator(boolean processing, String bookName,
                         int srcRecordListSize, int srcRecordListProgress,
                         int newAccountListSize, int newAccountListProgress,
                         int updateAccountListSize, int updateAccountListProgress) {
            this.processing = processing;
            this.bookName = bookName;
            this.srcRecordListSize = srcRecordListSize;
            this.srcRecordListProgress = srcRecordListProgress;
            this.newAccountListSize = newAccountListSize;
            this.newAccountListProgress = newAccountListProgress;
            this.updateAccountListSize = updateAccountListSize;
            this.updateAccountListProgress = updateAccountListProgress;
        }
    }

    private static class MigratorTask extends AsyncTask<Object, Integer, Void> {

        private WeakReference<RecordMigratorActivity> activityRef;
        private Book srcBook;
        private Book destBook;
        private List<Record> srcRecordList;
        private List<ReviewAccount> newAccountList;
        private List<ReviewAccount> updateAccountList;

        private MigratorTask(RecordMigratorActivity activity,
                             Book srcBook, Book destBook, List<Record> srcRecordList, List<ReviewAccount> newAccountList, List<ReviewAccount> updateAccountList) {
            this.activityRef = new WeakReference<>(activity);
            this.srcBook = srcBook;
            this.destBook = destBook;
            this.srcRecordList = srcRecordList;
            this.newAccountList = newAccountList;
            this.updateAccountList = updateAccountList;
        }

        @Override
        protected Void doInBackground(Object[] args) {

            int step1Progress = 0;
            int step2Progress = 0;
            int step3Progress = 0;

            for (Record r : srcRecordList) {
                step1Progress++;


                publishProgress(step1Progress, step2Progress, step3Progress);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            for (ReviewAccount r : newAccountList) {
                step2Progress++;


                publishProgress(step1Progress, step2Progress, step3Progress);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            for (ReviewAccount r : updateAccountList) {
                step3Progress++;


                publishProgress(step1Progress, step2Progress, step3Progress);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            RecordMigratorActivity activity = activityRef.get();
            if (activity != null) {
                activity.srcRecordListProgress = values[0];
                activity.newAccountListProgress = values[1];
                activity.updateAccountListProgress = values[2];

                activity.publishReloadStep4Data();
            }
        }

        @Override
        protected void onPostExecute(Void o) {
            super.onPostExecute(o);
            doDone();
        }

        @Override
        protected void onCancelled(Void o) {
            super.onCancelled(o);
            doDone();
        }

        void doDone() {
            RecordMigratorActivity activity = activityRef.get();
            if (activity != null) {
                activity.doMigrated();
            }
        }
    }

    private void doMigrate() {
        GUIs.confirm(this, i18n.string(R.string.qmsg_record_migrate), new GUIs.OnFinishListener() {
            @Override
            public boolean onFinish(int which, Object data) {
                if (GUIs.OK_BUTTON == which) {
                    markProcessing();
                    Book srcBook = contexts().getMasterDataProvider().findBook(workingBookId);
                    new MigratorTask(RecordMigratorActivity.this, srcBook, destBook, srcRecordList, newAccountList, updateAccountList)
                            .execute();
                }
                return true;
            }
        });
    }

    private void doMigrated() {
        unmarkProcessing();
        GUIs.alert(this, i18n.string(R.string.msg_record_migrate_done), new GUIs.OnFinishListener() {
            @Override
            public boolean onFinish(int which, Object data) {
                RecordMigratorActivity.this.finish();
                return true;
            }
        });
    }

}
