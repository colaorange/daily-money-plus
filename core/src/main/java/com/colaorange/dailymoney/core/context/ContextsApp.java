package com.colaorange.dailymoney.core.context;

import android.app.Application;

import com.colaorange.dailymoney.core.util.Logger;

/**
 * @author Dennis
 */

public class ContextsApp extends Application{

    @Override
    public void onCreate(){
        super.onCreate();
        Logger.d("===============Application Created");
        Contexts.instance().initApplication(this);

    }
    @Override
    public void onTerminate() {
        super.onTerminate();
        //this method is not always been called
        Logger.d("===============Application Terminated");
        Contexts.instance().destroyApplication(this);
    }


}
