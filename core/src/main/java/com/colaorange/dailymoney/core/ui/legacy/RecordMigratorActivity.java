package com.colaorange.dailymoney.core.ui.legacy;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.colaorange.commons.util.CalendarHelper;
import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.context.Contexts;
import com.colaorange.dailymoney.core.context.ContextsActivity;
import com.colaorange.dailymoney.core.context.ContextsFragment;
import com.colaorange.dailymoney.core.context.EventQueue;
import com.colaorange.dailymoney.core.data.Account;
import com.colaorange.dailymoney.core.data.Book;
import com.colaorange.dailymoney.core.data.IDataProvider;
import com.colaorange.dailymoney.core.data.IMasterDataProvider;
import com.colaorange.dailymoney.core.data.Record;
import com.colaorange.dailymoney.core.ui.GUIs;
import com.colaorange.dailymoney.core.ui.QEvents;
import com.colaorange.dailymoney.core.ui.RegularSpinnerAdapter;
import com.colaorange.dailymoney.core.util.I18N;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

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

    I18N i18n;
    int workingBookId;

    List<Record> step1RecordList;
    List<ReviewAccount> step2AccountList;
    List<ReviewAccount> step3AccountList;

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
                switch (pos){
                    case 0:
                        publishReloadStep1Data();
                        break;
                    case 1:
                        publishReloadStep2Data();
                        break;
                    case 2:
                        publishReloadStep3Data();
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
            //nothing currently
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

        step1RecordList = new LinkedList<>();
        step2AccountList = new LinkedList<>();
        step3AccountList = new LinkedList<>();

        publishReloadStep1Data();
        publishReloadStep2Data();
        publishReloadStep3Data();

        ((AppBarLayout) findViewById(R.id.appbar)).setExpanded(true);

        setTitle(i18n.string(R.string.label_migrate));
    }


    private void doPreview(boolean collapse) {
        Book book = bookList.get(vBook.getSelectedItemPosition());
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

        final IDataProvider idp = contexts().getDataProvider();
        GUIs.doBusy(this, new GUIs.BusyAdapter() {

            @Override
            public void run() {

                step1RecordList = idp.searchRecord(condition, preference().getMaxRecords());
                //TODO step2 data
                step2AccountList = new LinkedList<>();

                //TODO step3 data
                step3AccountList = new LinkedList<>();

            }

            @Override
            public void onBusyFinish() {
                publishReloadStep1Data();
                publishReloadStep2Data();
                publishReloadStep3Data();

                int count = step1RecordList.size() + step2AccountList.size() + step3AccountList.size();
                setTitle(i18n.string(R.string.label_migrate) + " - " + i18n.string(R.string.msg_n_items, count));
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void publishReloadStep1Data() {
        lookupQueue().publish(new EventQueue.EventBuilder(QEvents.RecordListFrag.ON_RELOAD_FRAGMENT).withData(step1RecordList).withArg(RecordListFragment.ARG_POS, 0).build());
    }
    private void publishReloadStep2Data() {
        lookupQueue().publish(new EventQueue.EventBuilder(QEvents.MigrateReviewAccountListFrag.ON_RELOAD_FRAGMENT).withData(step2AccountList).withArg(RecordListFragment.ARG_POS, 1).build());
    }
    private void publishReloadStep3Data() {
        lookupQueue().publish(new EventQueue.EventBuilder(QEvents.MigrateReviewAccountListFrag.ON_RELOAD_FRAGMENT).withData(step3AccountList).withArg(RecordListFragment.ARG_POS, 2).build());
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
            }else if (position == 1) {
                //step2
                RecordMigratorReviewAccountListFragment frag = new RecordMigratorReviewAccountListFragment();
                Bundle b = new Bundle();
                b.putSerializable(RecordMigratorReviewAccountListFragment.ARG_STEP_MODE, RecordMigratorReviewAccountListFragment.StepMode.CREATE_NEW);
                b.putInt(RecordMigratorReviewAccountListFragment.ARG_POS, position);
                b.putBoolean(RecordMigratorReviewAccountListFragment.ARG_DISABLE_SELECTION, true);
                frag.setArguments(b);
                return frag;
            }else if (position == 2) {
                //step3
                RecordMigratorReviewAccountListFragment frag = new RecordMigratorReviewAccountListFragment();
                Bundle b = new Bundle();
                b.putSerializable(RecordMigratorReviewAccountListFragment.ARG_STEP_MODE, RecordMigratorReviewAccountListFragment.StepMode.UPDATE_EXISTING);
                b.putInt(RecordMigratorReviewAccountListFragment.ARG_POS, position);
                b.putBoolean(RecordMigratorReviewAccountListFragment.ARG_DISABLE_SELECTION, true);
                frag.setArguments(b);
                return frag;
            }else if (position == 3) {
                //step4
                MigrateFragment frag = new MigrateFragment();
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
                    return i18n.string(R.string.label_migrate_create_accounts);
                case 2:
                    return i18n.string(R.string.label_migrate_update_accounts);
                case 3:
                    return i18n.string(R.string.label_migrate);
            }
            return Integer.toString(position);
        }
    }

    public static class ReviewAccount {
        Account account;
        double newInitialValue;
    }


    public static class MigrateFragment extends ContextsFragment{

        private View rootView;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            rootView = inflater.inflate(R.layout.record_migrator_migrate_frag, container, false);
            return rootView;
        }


        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            initArgs();
            initMembers();
        }

        @Override
        protected RecordMigratorActivity getContextsActivity(){
            return (RecordMigratorActivity)super.getContextsActivity();
        }

        private void initArgs() {
            Bundle args = getArguments();
        }

        private void initMembers() {
            RecordMigratorActivity activity = getContextsActivity();

        }
    }

}
