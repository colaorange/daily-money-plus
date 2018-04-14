package com.colaorange.dailymoney.util;

import android.content.Context;

/**
 * 
 * @author dennis
 *
 */
public class I18N {
    Context context;
    public I18N(Context context){
        this.context = context;
    }
    
    public String string(int id){
        return context.getString(id);
    }
    
    public String string(int id,Object... args){
        try {
            return context.getString(id, args);
        }catch(Exception x){
            Logger.e(x.getMessage());
            return context.getString(id);
        }

    }
}
