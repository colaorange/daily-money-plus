package com.colaorange.dailymoney.core.ui.legacy;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
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
import java.util.Collections;
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
    private View vResultContainer;
    private RecordListFragment listFragment;

    private int pos = 0;

    I18N i18n;
    int workingBookId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.record_migrator);
        initArgs();
        initMembers();
        refreshSpinner();
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
        collapsingToolbar.setExpandedTitleColor(getResources().getColor(android.R.color.transparent));

        vToDate = findViewById(R.id.record_to_date);

        vPreviewhHint = findViewById(R.id.preview_hint);
        vResultContainer = findViewById(R.id.frag_container);

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

        }
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
        vResultContainer.setVisibility(View.GONE);

        publishReloadData(new LinkedList());

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


        anyCondition = toDate != null ;

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
        vResultContainer.setVisibility(View.VISIBLE);

        if (listFragment == null) {
            listFragment = new RecordListFragment();
            Bundle b = new Bundle();
            b.putInt(RecordListFragment.ARG_POS, pos);
            b.putInt(RecordListFragment.ARG_MODE, RecordListFragment.MODE_ALL);
            b.putBoolean(RecordListFragment.ARG_DISABLE_SELECTION, true);
            listFragment.setArguments(b);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.frag_container, listFragment)
                    .disallowAddToBackStack()
                    .commit();
        }


        final IDataProvider idp = contexts().getDataProvider();
        GUIs.doBusy(this, new GUIs.BusyAdapter() {
            @SuppressWarnings("unchecked")
            List<Record> data = Collections.EMPTY_LIST;
            int count = 0;

            @Override
            public void run() {

                data = idp.searchRecord(condition, preference().getMaxRecords());
                //do we need to count by sql for real size?
                count = data.size();
            }

            @Override
            public void onBusyFinish() {
                publishReloadData(data);

                setTitle(i18n.string(R.string.label_migrate) + " - " + i18n.string(R.string.msg_n_items, count));
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void publishReloadData(List<Record> data) {
        lookupQueue().publish(new EventQueue.EventBuilder(QEvents.RecordListFrag.ON_RELOAD_FRAGMENT).withData(data).withArg(RecordListFragment.ARG_POS, pos).build());
    }
}
