package com.colaorange.dailymoney.core.ui.legacy;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.colaorange.commons.util.CalendarHelper;
import com.colaorange.commons.util.Colors;
import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.context.Contexts;
import com.colaorange.dailymoney.core.context.ContextsActivity;
import com.colaorange.dailymoney.core.context.Preference;
import com.colaorange.dailymoney.core.data.Account;
import com.colaorange.dailymoney.core.data.AccountType;
import com.colaorange.dailymoney.core.data.Record;
import com.colaorange.dailymoney.core.ui.helper.SelectableRecyclerViewAdaptor;
import com.colaorange.dailymoney.core.util.I18N;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class RecordRecyclerAdapter extends SelectableRecyclerViewAdaptor<RecordRecyclerAdapter.RecordFolk, SelectableRecyclerViewAdaptor.SelectableViewHolder> {

    static final private int VT_RECORD = 0;
    static final private int VT_HEADER = 1;
    static final private int VT_FOOTER = 2;

    private int listLayout;
    private Map<String, Account> accountMap;
    private LayoutInflater inflater;
    private Map<AccountType, Integer> accountBgColorMap;
    private Map<AccountType, Integer> accountTextColorMap;
    private I18N i18n;
    private boolean lightTheme;
    private DateFormat dateFormat;
    private DateFormat yearFormat;
    private DateFormat monthFormat;
    private DateFormat nonDigitalMonthFormat;
    private DateFormat weekDayFormat;
    private CalendarHelper calHelper;
    private Date today;
    private boolean showRecordDate;

    private int header1Color;
    private int header1TextColor;
    private float dpRatio;

    private boolean disableSelection;

    public RecordRecyclerAdapter(ContextsActivity activity, List<RecordFolk> data) {
        super(activity, data);
        accountBgColorMap = activity.getAccountBgColorMap();
        accountTextColorMap = activity.getAccountTextColorMap();
        lightTheme = activity.isLightTheme();

        Preference preference = Contexts.instance().getPreference();
        yearFormat = preference.getYearFormat();
        monthFormat = preference.getMonthFormat();
        nonDigitalMonthFormat = preference.getNonDigitalMonthFormat();
        dateFormat = preference.getDateFormat();//new SimpleDateFormat("yyyy/MM/dd");
        weekDayFormat = preference.getWeekDayFormat();// Wed.

        listLayout = preference.getRecordListLayout();
        i18n = Contexts.instance().getI18n();
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        calHelper = Contexts.instance().getCalendarHelper();
        today = new Date();
        header1Color = activity.resolveThemeAttrResData(R.attr.appPrimaryDarkColor);
        header1TextColor = activity.resolveThemeAttrResData(R.attr.appPrimaryTextColor);
        if (lightTheme) {
            header1TextColor = Colors.lighten(header1TextColor, 0.8f);
        } else {
            header1TextColor = Colors.lighten(header1TextColor, 0.1f);
        }
        dpRatio = activity.getDpRatio();
    }

    public void setAccountMap(Map<String, Account> accountMap) {
        this.accountMap = accountMap;
    }

    public void setShowRecordDate(boolean showRecordDate) {
        this.showRecordDate = showRecordDate;
    }

    public boolean isDisableSelection() {
        return disableSelection;
    }

    public void setDisableSelection(boolean disableSelection) {
        this.disableSelection = disableSelection;
    }

    @Override
    public boolean isSelectable(RecordFolk obj) {
        return disableSelection ? false : obj.isRecord();
    }

    @Override
    public int getItemViewType(int position) {
        RecordFolk folk = get(position);
        return folk.isRecord() ? VT_RECORD : folk.isHeader() ? VT_HEADER : VT_FOOTER;
    }

    @NonNull
    @Override
    public SelectableRecyclerViewAdaptor.SelectableViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VT_RECORD) {
            int itemLayout;
            switch (listLayout) {
                case 2:
                    itemLayout = R.layout.record_list_item2;
                    break;
                case 3:
                    itemLayout = R.layout.record_list_item3;
                    break;
                case 4:
                    itemLayout = R.layout.record_list_item4;
                    break;
                case 1:
                default:
                    itemLayout = R.layout.record_list_item1;
            }

            View viewItem = inflater.inflate(R.layout.record_list_item, parent, false);
            inflater.inflate(itemLayout, (ViewGroup) viewItem.findViewById(R.id.layout_select), true);
            return new RecordViewHolder(this, viewItem);
        } else if (viewType == VT_HEADER) {
            View viewItem = inflater.inflate(R.layout.record_list_item_header, parent, false);
            return new RecordHeaderViewHolder(this, viewItem);
        } else if (viewType == VT_FOOTER) {
            View viewItem = inflater.inflate(R.layout.record_list_item_footer, parent, false);
            return new RecordFooterViewHolder(this, viewItem);
        }
        throw new IllegalStateException("unknow view type " + viewType);
    }


    private class RecordViewHolder extends SelectableRecyclerViewAdaptor.SelectableViewHolder<RecordRecyclerAdapter, RecordFolk> {

        private RecordViewHolder(RecordRecyclerAdapter adapter, View itemView) {
            super(adapter, itemView);
        }

        @Override
        public void bindViewValue(RecordFolk folk) {
            super.bindViewValue(folk);

            if (accountMap == null) {
                throw new IllegalStateException("accountMap is null");
            }

            LinearLayout vlayout = itemView.findViewById(R.id.record_item_layout);
            LinearLayout vfromborder = itemView.findViewById(R.id.record_item_from_border);
            LinearLayout vtoborder = itemView.findViewById(R.id.record_item_to_border);
            TextView vfrom = itemView.findViewById(R.id.record_item_from);
            TextView vto = itemView.findViewById(R.id.record_item_to);
            TextView vmoney = itemView.findViewById(R.id.record_item_money);
            TextView vnote = itemView.findViewById(R.id.record_item_note);
            TextView vdate = itemView.findViewById(R.id.record_item_date);

            Record record = folk.getRecord();

            Account fromAcc = accountMap.get(record.getFrom());
            Account toAcc = accountMap.get(record.getTo());

            AccountType fromAccType = fromAcc == null ? AccountType.UNKONW : AccountType.find(fromAcc.getType());
            AccountType toAccType = toAcc == null ? AccountType.UNKONW : AccountType.find(toAcc.getType());

            //transparent mask for selecting ripple effect
            int mask = 0xE0FFFFFF;
            boolean selected = adaptor.isSelected(folk);

            int bgcolor;
            bgcolor = mask & accountBgColorMap.get(toAccType);
            if (selected) {
                if (lightTheme) {
                    bgcolor = Colors.darken(bgcolor, 0.15f);
                } else {
                    bgcolor = Colors.lighten(bgcolor, 0.15f);
                }

            }
            vlayout.setBackgroundColor(bgcolor);

            bgcolor = mask & accountTextColorMap.get(toAccType);
            if (selected) {
                if (lightTheme) {
                    bgcolor = Colors.darken(bgcolor, 0.15f);
                } else {
                    bgcolor = Colors.lighten(bgcolor, 0.15f);
                }
            }
            vtoborder.setBackgroundColor(bgcolor);

            bgcolor = mask & accountTextColorMap.get(fromAccType);
            if (selected) {
                if (lightTheme) {
                    bgcolor = Colors.darken(bgcolor, 0.15f);
                } else {
                    bgcolor = Colors.lighten(bgcolor, 0.15f);
                }
            }
            vfromborder.setBackgroundColor(bgcolor);

            vto.setTextColor(accountTextColorMap.get(toAccType));
            vfrom.setTextColor(accountTextColorMap.get(fromAccType));
            vnote.setTextColor(accountTextColorMap.get(toAccType));


            //
            String from = fromAcc == null ? record.getFrom() : (i18n.string(R.string.label_reclist_from, fromAcc.getName(), AccountType.getDisplay(i18n, fromAcc.getType())));
            String to = toAcc == null ? record.getTo() : (i18n.string(R.string.label_reclist_to, toAcc.getName(), AccountType.getDisplay(i18n, toAcc.getType())));
            String money = Contexts.instance().toFormattedMoneyString(record.getMoney());
            String date = "";

            if (showRecordDate) {
                date = dateFormat.format(record.getDate());
            }

            vfrom.setText(from);
            vto.setText(to);
            vmoney.setText(money);
            vnote.setText(record.getNote());
            vdate.setText(date);

        }
    }


    private class RecordHeaderViewHolder extends SelectableRecyclerViewAdaptor.SelectableViewHolder<RecordRecyclerAdapter, RecordFolk> {

        private RecordHeaderViewHolder(RecordRecyclerAdapter adapter, View itemView) {
            super(adapter, itemView);
        }

        @Override
        public void bindViewValue(RecordFolk folk) {
            super.bindViewValue(folk);

            RecordHeader header = folk.getHeader();

            View vH1 = itemView.findViewById(R.id.record_header_1);
            TextView vYear = itemView.findViewById(R.id.record_header_year);
            TextView vMonth = itemView.findViewById(R.id.record_header_month);
            TextView vDate = itemView.findViewById(R.id.record_header_day);

            vH1.setBackgroundColor(header1Color);
            vYear.setTextColor(header1TextColor);
            vMonth.setTextColor(header1TextColor);

            if (header.showYear) {
                vYear.setVisibility(View.VISIBLE);
                vYear.setText(yearFormat.format(header.calendar.getTime()));
            } else {
                vYear.setVisibility(View.GONE);
                vYear.setText("");
            }
            if (header.showMonth) {
                vMonth.setVisibility(View.VISIBLE);
                vMonth.setText(nonDigitalMonthFormat.format(header.calendar.getTime()));
            } else {
                vMonth.setVisibility(View.GONE);
                vMonth.setText("");
            }
            if (header.showDay) {
                vDate.setVisibility(View.VISIBLE);
                int d = header.calendar.get(Calendar.DAY_OF_MONTH);
                StringBuilder sb = new StringBuilder();
                sb.append((d < 10 ? "0" : "")).append(d).append(" ( ").append(weekDayFormat.format(header.calendar.getTime()))
                        .append(" ) ");
                if (calHelper.isSameDay(today, header.calendar.getTime())) {
                    sb.append(" - ").append(i18n.string(R.string.label_today));
                } else if (calHelper.isYesterday(today, header.calendar.getTime())) {
                    sb.append(" - ").append(i18n.string(R.string.label_yesterday));
                } else if (calHelper.isTomorrow(today, header.calendar.getTime())) {
                    sb.append(" - ").append(i18n.string(R.string.label_tomorrow));
                } else if (calHelper.isFutureDay(today, header.calendar.getTime())) {
                    sb.append(" - ").append(i18n.string(R.string.label_future));
                }

                vDate.setText(sb.toString());
            } else {
                vDate.setVisibility(View.GONE);
                vDate.setText("");
            }
        }
    }

    private class RecordFooterViewHolder extends SelectableRecyclerViewAdaptor.SelectableViewHolder<RecordRecyclerAdapter, RecordFolk> {

        private RecordFooterViewHolder(RecordRecyclerAdapter adapter, View itemView) {
            super(adapter, itemView);
        }

        @Override
        public void bindViewValue(RecordFolk folk) {
            super.bindViewValue(folk);
            if (getAdapterPosition() < data.size() - 1) {
                RecordFolk f = get(getAdapterPosition() + 1);
                View vFooter1 = itemView.findViewById(R.id.record_footer_1);
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) vFooter1.getLayoutParams();
                int mbottom;
                if (f.isHeader() && (f.getHeader().showYear || f.getHeader().showMonth)) {
                    //enlarge bottom space
                    mbottom = (int) (36 * dpRatio);
                } else {
                    mbottom = (int) (4 * dpRatio);
                }
                lp.bottomMargin = mbottom;
                vFooter1.setLayoutParams(lp);
            }
        }
    }

    public static class RecordHeader {
        final Calendar calendar;
        final boolean showYear;
        final boolean showMonth;
        final boolean showDay;

        public RecordHeader(Calendar calendar, boolean showYear, boolean showMonth, boolean showDay) {
            this.calendar = calendar;
            this.showYear = showYear;
            this.showMonth = showMonth;
            this.showDay = showDay;
        }
    }

    public static class RecordFooter {
        public RecordFooter() {
        }
    }

    public static class RecordFolk {

        final Object obj;

        public RecordFolk(Object obj) {
            this.obj = obj;
        }

        public boolean isRecord() {
            return obj instanceof Record;
        }

        public boolean isHeader() {
            return obj instanceof RecordHeader;
        }

        public boolean isFooter() {
            return obj instanceof RecordFooter;
        }


        public Record getRecord() {
            return (Record) obj;
        }

        public RecordHeader getHeader() {
            return (RecordHeader) obj;
        }

        public RecordFooter getFooter() {
            return (RecordFooter) obj;
        }

    }

}