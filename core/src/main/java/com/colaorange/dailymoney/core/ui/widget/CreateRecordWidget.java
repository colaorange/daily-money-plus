package com.colaorange.dailymoney.core.ui.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.context.Contexts;
import com.colaorange.dailymoney.core.context.Preference;
import com.colaorange.dailymoney.core.ui.Constants;
import com.colaorange.dailymoney.core.ui.StartupActivity;
import com.colaorange.dailymoney.core.util.I18N;

import java.util.Arrays;

/**
 * Created by Dennis
 */
public class CreateRecordWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        for (int widgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(),
                    R.layout.widget_create_record);

            Intent intent = new Intent(context, StartupActivity.class);
            intent.setAction(Constants.ACTION_CREATE_RECORD);

            PendingIntent pending = PendingIntent.getActivity(context, 0, intent, 0);

            views.setOnClickPendingIntent(R.id.widget_create_record_layout, pending);

            appWidgetManager.updateAppWidget(widgetId, views);
        }
    }
}
