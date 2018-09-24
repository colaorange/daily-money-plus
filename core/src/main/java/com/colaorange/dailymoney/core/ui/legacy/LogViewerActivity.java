package com.colaorange.dailymoney.core.ui.legacy;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.context.ContextsActivity;
import com.colaorange.dailymoney.core.util.Logger;

/**
 * Edit or create a book
 *
 * @author dennis
 */
public class LogViewerActivity extends ContextsActivity {

    private TextView vLogs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.log_viewer);
        initArgs();
        initMembers();
    }

    private void initArgs() {
        Bundle bundle = getIntentExtras();
    }


    private void initMembers() {
        vLogs = findViewById(R.id.logs);
        vLogs.setText(Logger.getLogs());

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.log_viewer_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_share) {
            doShare();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void doShare() {
        Intent intent = new Intent();

        intent.setAction(Intent.ACTION_SEND).setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, i18n().string(R.string.title_log_viewer));
        intent.putExtra(Intent.EXTRA_TEXT, vLogs.getText().toString());

        startActivity(intent);
    }

}
