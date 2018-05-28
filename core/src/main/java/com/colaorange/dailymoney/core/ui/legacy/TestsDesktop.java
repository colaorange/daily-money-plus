package com.colaorange.dailymoney.core.ui.legacy;

import java.util.Date;

import android.app.Activity;
import android.content.Intent;

import com.colaorange.commons.util.CalendarHelper;
import com.colaorange.commons.util.Collections;
import com.colaorange.dailymoney.core.context.Preference;
import com.colaorange.dailymoney.core.data.Card;
import com.colaorange.dailymoney.core.data.CardCollection;
import com.colaorange.dailymoney.core.data.CardType;
import com.colaorange.dailymoney.core.data.DefaultCardsCreator;
import com.colaorange.dailymoney.core.ui.cards.CardFacade;
import com.colaorange.dailymoney.core.ui.nav.NavPage;
import com.colaorange.dailymoney.core.util.GUIs;
import com.colaorange.dailymoney.core.util.I18N;
import com.colaorange.dailymoney.core.context.Contexts;
import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.data.Book;
import com.colaorange.dailymoney.core.data.DataCreator;
import com.colaorange.dailymoney.core.data.IDataProvider;
import com.colaorange.dailymoney.core.data.SymbolPosition;
import com.colaorange.dailymoney.core.ui.Constants;
import com.colaorange.dailymoney.core.util.Logger;

/**
 * @author dennis
 */
public class TestsDesktop extends AbstractDesktop {

    public static final String NAME = "tests";

    public TestsDesktop(Activity activity) {
        super(NAME, activity);

    }

    @Override
    public boolean isAvailable() {
        return Contexts.instance().getPreference().isTestsDesktop();
    }

    @Override
    protected void init() {
        I18N i18n = Contexts.instance().getI18n();

        label = i18n.string(R.string.desktop_tests);

        DesktopItem dt = null;
        dt = new DesktopItem(new Runnable() {
            public void run() {
                Contexts ctx = Contexts.instance();
                ctx.getMasterDataProvider().reset();
            }
        }, "Reset Master Dataprovider", R.drawable.dtitem_test);

        addItem(dt);

        dt = new DesktopItem(new Runnable() {
            public void run() {
                Preference preference = Contexts.instance().getPreference();
                for (int i = 0; i < preference.getCardsSize(); i++) {
                    preference.removeCards(i);
                }
            }
        }, "Remove all cards", R.drawable.dtitem_test);

        addItem(dt);

        dt = new DesktopItem(new Runnable() {
            public void run() {
                Contexts ctx = Contexts.instance();
                I18N i18n = ctx.getI18n();
                Preference preference = ctx.getPreference();
                for (int i = 0; i < 10; i++) {
                    CardCollection cards0 = preference.getCards(i);
                    if (cards0.size() == 0) {
                        cards0.setTitle("T" + i);
                        Card card = new Card(CardType.NAV_PAGES, i18n.string(R.string.card_nav_page));
                        card.withArg(CardFacade.ARG_NAV_PAGES_LIST, Collections.asList(
                                NavPage.HOW2USE));
                        cards0.add(card);

                        card = new Card(CardType.INFO_EXPENSE, i18n.string(R.string.card_info_expense));
                        cards0.add(card);
                        preference.updateCards(i, cards0, true);
                    }
                }
            }
        }, "Create 10 cards ", R.drawable.dtitem_test);

        addItem(dt);

        dt = new DesktopItem(new Runnable() {
            public void run() {
                new DefaultCardsCreator().createForWholeNew(true);
            }
        }, "Create Cards for whole new ", R.drawable.dtitem_test);

        addItem(dt);

        dt = new DesktopItem(new Runnable() {
            public void run() {
                new DefaultCardsCreator().createForUpgrade(true);
            }
        }, "Create Cards for upgrade ", R.drawable.dtitem_test);

        addItem(dt);

        dt = new DesktopItem(new Runnable() {
            public void run() {
                Contexts ctx = Contexts.instance();
                Book book = ctx.getMasterDataProvider().findBook(ctx.getWorkingBookId());

                Intent intent = null;
                intent = new Intent(activity, BookEditorActivity.class);
                intent.putExtra(BookEditorActivity.ARG_MODE_CREATE, false);
                intent.putExtra(BookEditorActivity.ARG_BOOK, book);
                activity.startActivityForResult(intent, Constants.REQUEST_BOOK_EDITOR_CODE);
            }
        }, "Edit selected book", R.drawable.dtitem_test);

        addItem(dt);

        dt = new DesktopItem(new Runnable() {
            public void run() {
                Book book = new Book("test", "$", SymbolPosition.AFTER, "");
                Intent intent = null;
                intent = new Intent(activity, BookEditorActivity.class);
                intent.putExtra(BookEditorActivity.ARG_MODE_CREATE, true);
                intent.putExtra(BookEditorActivity.ARG_BOOK, book);
                activity.startActivityForResult(intent, Constants.REQUEST_BOOK_EDITOR_CODE);
            }
        }, "Add book", R.drawable.dtitem_test);

        addItem(dt);


        addItem(new DesktopItem(new Runnable() {
            @Override
            public void run() {
                Contexts.instance().getDataProvider().reset();
                GUIs.shortToast(activity, "reset data provider");
            }
        }, "rest data provider", R.drawable.dtitem_test));
        addItem(new DesktopItem(new Runnable() {
            @Override
            public void run() {
                testFirstDayOfWeek();
            }
        }, "first day of week", R.drawable.dtitem_test) {
        });
        addItem(new DesktopItem(new Runnable() {
            @Override
            public void run() {
                testBusy(200, null);
            }
        }, "Busy 200ms", R.drawable.dtitem_test));
        addItem(new DesktopItem(new Runnable() {
            @Override
            public void run() {
                testBusy(200, "error short");
            }
        }, "Busy 200ms Error", R.drawable.dtitem_test));
        addItem(new DesktopItem(new Runnable() {
            @Override
            public void run() {
                testBusy(5000, null);
            }
        }, "Busy 5s", R.drawable.dtitem_test));
        addItem(new DesktopItem(new Runnable() {
            @Override
            public void run() {
                testBusy(5000, "error long");
            }
        }, "Busy 5s Error", R.drawable.dtitem_test));


        addItem(new DesktopItem(new Runnable() {
            @Override
            public void run() {
                testCreateTestdata(25);
            }
        }, "test data25", R.drawable.dtitem_test));
        addItem(new DesktopItem(new Runnable() {
            @Override
            public void run() {
                testCreateTestdata(50);
            }
        }, "test data50", R.drawable.dtitem_test));
        addItem(new DesktopItem(new Runnable() {
            @Override
            public void run() {
                testCreateTestdata(100);
            }
        }, "test data100", R.drawable.dtitem_test));
        addItem(new DesktopItem(new Runnable() {
            @Override
            public void run() {
                testCreateTestdata(200);
            }
        }, "test data200", R.drawable.dtitem_test));
        addItem(new DesktopItem(new Runnable() {
            @Override
            public void run() {
                testJust();
            }
        }, "just test", R.drawable.dtitem_test));

        DesktopItem padding = new DesktopItem(new Runnable() {
            @Override
            public void run() {

            }
        }, "padding", R.drawable.dtitem_test);

        addItem(padding);
        addItem(padding);
        addItem(padding);
        addItem(padding);
        addItem(padding);
        addItem(padding);
        addItem(padding);
        addItem(padding);
        addItem(padding);
    }

    protected void testBusy(final long i, final String error) {
        GUIs.doBusy(activity, new GUIs.BusyAdapter() {
            @Override
            public void onBusyFinish() {
                GUIs.shortToast(activity, "task finished");
            }

            public void onBusyError(Throwable x) {
                GUIs.shortToast(activity, "Error " + x.getMessage());
            }

            @Override
            public void run() {
                try {
                    Thread.sleep(i);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (error != null) {
                    throw new RuntimeException(error);
                }
            }
        });
    }

    protected void testFirstDayOfWeek() {
        CalendarHelper calHelper = Contexts.instance().getCalendarHelper();
        for (int i = 0; i < 100; i++) {
            Date now = new Date();
            Date start = calHelper.weekStartDate(now);
            Date end = calHelper.weekEndDate(now);
//            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>");
//            System.out.println("1>>>>>>>>>>> "+now);
            System.out.println("2>>>>>>>>>>> " + start);
//            System.out.println("3>>>>>>>>>>> "+end);
//            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>");
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private void testCreateTestdata(final int loop) {
        GUIs.doBusy(activity, new GUIs.BusyAdapter() {
            @Override
            public void onBusyFinish() {
                GUIs.shortToast(activity, "create test data");
            }

            @Override
            public void run() {
                I18N i18n = Contexts.instance().getI18n();
                IDataProvider idp = Contexts.instance().getDataProvider();
                new DataCreator(idp, i18n).createTestData(loop);
            }
        });

    }


    protected void testJust() {
        CalendarHelper calHelper = Contexts.instance().getCalendarHelper();
        Date now = new Date();
        Date start = calHelper.weekStartDate(now);
        Date end = calHelper.weekEndDate(now);
        System.out.println(">>>>>>>>>>>>>>> " + now);
        System.out.println("1>>>>>>>>>>> " + now);
        System.out.println("2>>>>>>>>>>> " + start);
        System.out.println("3>>>>>>>>>>> " + end);
        System.out.println(">>>>>>>>>>>>>> " + now);

    }

}
