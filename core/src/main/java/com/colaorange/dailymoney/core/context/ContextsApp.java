package com.colaorange.dailymoney.core.context;

import android.app.Application;
import android.support.multidex.MultiDexApplication;

import com.colaorange.dailymoney.core.util.Logger;

/**
 * @author Dennis
 */

public class ContextsApp extends MultiDexApplication{

    static{
        //to support poi,
        //read https://github.com/centic9/poi-on-android
        System.setProperty("org.apache.poi.javax.xml.stream.XMLInputFactory", "com.fasterxml.aalto.stax.InputFactoryImpl");
        System.setProperty("org.apache.poi.javax.xml.stream.XMLOutputFactory", "com.fasterxml.aalto.stax.OutputFactoryImpl");
        System.setProperty("org.apache.poi.javax.xml.stream.XMLEventFactory", "com.fasterxml.aalto.stax.EventFactoryImpl");
    }

    @Override
    public void onCreate(){
        super.onCreate();
        Contexts.instance().initApplication(this);
        Logger.d("===============Application Created");

    }
    @Override
    public void onTerminate() {
        super.onTerminate();
        //this method is not always been called
        Logger.d("===============Application Terminated");
        Contexts.instance().destroyApplication(this);
    }


}
