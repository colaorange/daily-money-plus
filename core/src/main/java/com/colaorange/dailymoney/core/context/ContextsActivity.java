package com.colaorange.dailymoney.core.context;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.MenuItem;

import com.colaorange.commons.util.CalendarHelper;
import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.data.AccountType;
import com.colaorange.dailymoney.core.util.GUIs;
import com.colaorange.dailymoney.core.util.I18N;
import com.colaorange.dailymoney.core.util.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * provide life cycle and easy access to contexts
 *
 * @author dennis
 */
@InstanceState
public class ContextsActivity extends AppCompatActivity {

    public static final String PARAM_TITLE = "activity.title";

    private long onCreateTime;
    private static long recreateTimeMark;

    private Map<AccountType, Integer> accountTextColorMap;
    private Map<AccountType, Integer> accountBgColorMap;

    private InstanceStateHelper instanceStateHelper;

    private Float dpRatio;

    private Boolean lightTheme;

    private ThemeApplier themeApplier;

    protected static final int homeAsUpNone = -1;
    protected int homeAsUpBackId;
    protected int homeAsUpAppId;

    protected int selectableBackgroundId;
    protected int selectedBackgroundId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //for init ui related resource in ui thread
        GUIs.touch();

        //do before super on create;
        themeApplier = new ThemeApplier(this);
        themeApplier.applyTheme();

        homeAsUpBackId = resolveThemeAttrResId(R.attr.ic_arrow_back);
        homeAsUpAppId = resolveThemeAttrResId(R.attr.ic_apps);
        selectableBackgroundId = resolveThemeAttrResId(android.R.attr.selectableItemBackground);
        if (isLightTheme()) {
            selectedBackgroundId = resolveThemeAttrResId(R.attr.appSecondaryLightColor);
        } else {
            selectedBackgroundId = resolveThemeAttrResId(R.attr.appSecondaryDarkColor);
        }


        super.onCreate(savedInstanceState);
        instanceStateHelper = new InstanceStateHelper(this);
        instanceStateHelper.onCreate(savedInstanceState);


        Logger.d("activity created:" + this);
        onCreateTime = System.currentTimeMillis();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        instanceStateHelper.onSaveInstanceState(savedInstanceState);
    }

    public void markWholeRecreate() {
        recreateTimeMark = System.currentTimeMillis();
    }

    /*
     * for quick provide action bar in all sub class
     */
    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
        Toolbar toolbar = findViewById(R.id.appToolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            doInitActionBar(getSupportActionBar());
        }
    }


    protected void restartApp(boolean passedProtection) {
        //TODO
        Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        i = (Intent) i.clone();
//        String bypassId = Strings.randomUUID();
//        i.putExtra(StartupActivity.PARAM_BYPASS_PROTECTION,true);

        startActivity(i);
    }

    public boolean isNoActionBarTheme() {
        return true;
    }

    public boolean isDialogTheme() {
        return false;
    }

    public int resolveThemeAttrResId(int attrId) {
        TypedValue attr = resolveThemeAttr(attrId);
        return attr.resourceId;
    }

    public int resolveThemeAttrResData(int attrId) {
        TypedValue attr = resolveThemeAttr(attrId);
        return attr.data;
    }

    public TypedValue resolveThemeAttr(int attrId) {
        Resources.Theme theme = getTheme();
        TypedValue attr = new TypedValue();
        theme.resolveAttribute(attrId, attr, true);
        return attr;
    }


    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        for (int g : grantResults) {
            if (g == PackageManager.PERMISSION_GRANTED) {
                //simply reload this activie
                makeRestart();
                break;
            }
        }
    }

    protected void makeRestart() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    public void trackEvent(String action) {
        Contexts ctxs = Contexts.instance();
        ctxs.trackEvent(Contexts.getTrackerPath(getClass()), action, "", null);
    }

    protected I18N i18n() {
        return contexts().getI18n();
    }

    protected CalendarHelper calendarHelper() {
        return preference().getCalendarHelper();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Logger.d("activity destroyed:" + this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkRecreate();
    }


    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();

        Bundle b = getIntentExtras();
        String t = b.getString(PARAM_TITLE);
        if (t != null) {
            setTitle(t);
        }

        checkRecreate();
    }

    private void checkRecreate() {
        if (recreateTimeMark > onCreateTime) {
            recreate();
        }
    }

    protected void makeRecreate() {
        recreateTimeMark = System.currentTimeMillis();
    }


    Bundle fakeExtra;

    protected Bundle getIntentExtras() {
        if (getIntent() != null && getIntent().getExtras() != null) {
            return getIntent().getExtras();
        }
        // if extra is null;
        if (fakeExtra == null) {
            fakeExtra = new Bundle();
        }
        return fakeExtra;
    }

    protected Contexts contexts() {
        return Contexts.instance();
    }

    protected Preference preference() {
        return contexts().getPreference();
    }

    public Map<AccountType, Integer> getAccountTextColorMap() {
        if (accountTextColorMap != null) {
            return accountTextColorMap;
        }
        Map<AccountType, Integer> map = new HashMap<>();
        map.put(AccountType.INCOME, resolveThemeAttrResData(R.attr.accountIncomeTextColor));
        map.put(AccountType.EXPENSE, resolveThemeAttrResData(R.attr.accountExpenseTextColor));
        map.put(AccountType.ASSET, resolveThemeAttrResData(R.attr.accountAssetTextColor));
        map.put(AccountType.LIABILITY, resolveThemeAttrResData(R.attr.accountLiabilityTextColor));
        map.put(AccountType.OTHER, resolveThemeAttrResData(R.attr.accountOtherTextColor));
        map.put(AccountType.UNKONW, resolveThemeAttrResData(R.attr.accountUnknownTextColor));
        return this.accountTextColorMap = map;
    }

    public Map<AccountType, Integer> getAccountBgColorMap() {
        if (accountBgColorMap != null) {
            return accountBgColorMap;
        }
        Map<AccountType, Integer> map = new HashMap<>();
        map.put(AccountType.INCOME, resolveThemeAttrResData(R.attr.accountIncomeBgColor));
        map.put(AccountType.EXPENSE, resolveThemeAttrResData(R.attr.accountExpenseBgColor));
        map.put(AccountType.ASSET, resolveThemeAttrResData(R.attr.accountAssetBgColor));
        map.put(AccountType.LIABILITY, resolveThemeAttrResData(R.attr.accountLiabilityBgColor));
        map.put(AccountType.OTHER, resolveThemeAttrResData(R.attr.accountOtherBgColor));
        map.put(AccountType.UNKONW, resolveThemeAttrResData(R.attr.accountUnknownBgColor));
        return this.accountBgColorMap = map;
    }

    public float getDpRatio() {
        if (dpRatio == null) {
            dpRatio = GUIs.getDPRatio(this);
        }
        return dpRatio.floatValue();
    }

    public boolean isLightTheme() {
        if (lightTheme == null) {
            lightTheme = preference().isLightTheme();
        }
        return lightTheme;
    }


    protected int getActionBarHomeAsUp() {
        return homeAsUpBackId;
    }

    protected void doInitActionBar(ActionBar supportActionBar) {
        int resId = getActionBarHomeAsUp();
        if (resId > 0) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setHomeAsUpIndicator(resId);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //home button clicked
            case android.R.id.home:
                this.onActionBarHomeAsUp(getActionBarHomeAsUp());
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onActionBarHomeAsUp(int resId) {
        if (resId == homeAsUpBackId) {
            this.finish();
        } else if (resId == homeAsUpAppId) {
            //todo
        }
    }

    public void expandAppbar(boolean expand) {
        AppBarLayout appbar = findViewById(R.id.appbar);
        if (appbar != null) {
            appbar.setExpanded(expand, true);
        }
    }

    public void enableAppbarHideOnScroll(boolean enable) {
        setAppbarHideOnScrollFlag(enable ? AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS : 0);
    }

    public void setAppbarHideOnScrollFlag(int flag) {
        Toolbar toolbar = findViewById(R.id.appToolbar);
        if (toolbar != null) {
            AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) toolbar.getLayoutParams();
            params.setScrollFlags(flag);
        }
    }

    //shortcut
    public interface TE extends Contexts.TE {
    }

    public int getSelectableBackgroundId() {
        return selectableBackgroundId;
    }

    public int getSelectedBackgroundId() {
        return selectedBackgroundId;
    }

    /**
     * to get a drawable icon or build a lighten/darken icon when it is disabled
     */
    public Drawable buildIcon(int drawableResId, boolean enabled){
        Drawable drawable = getResources().getDrawable(drawableResId);
        if(enabled){
            return drawable;
        }

        drawable = drawable.mutate();
        if(isLightTheme()) {
            //https://blog.csdn.net/t12x3456/article/details/10432935
            drawable.setColorFilter(0x5FFFFFFF, PorterDuff.Mode.MULTIPLY);
        }else{
            drawable.setColorFilter(0x5FFFFFFF, PorterDuff.Mode.MULTIPLY);
        }
        return drawable;
    }
}
