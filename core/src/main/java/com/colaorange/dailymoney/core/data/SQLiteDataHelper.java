package com.colaorange.dailymoney.core.data;

import com.colaorange.dailymoney.core.util.Logger;
import com.colaorange.dailymoney.core.context.Contexts;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.colaorange.dailymoney.core.data.DataMeta.*;

/**
 * @author dennis
 */
public class SQLiteDataHelper extends SQLiteOpenHelper {
    /**
     * maintain this field carefully
     */
//    private static final int VERSION = 4;//0.9.1-0.9.3
//    private static final int VERSION = 5;//0.9.4-0.10.6
    private static final int VERSION = 6;//0.10.7-freshly-200207001 -

    private static final String ACC_CREATE_SQL = "CREATE TABLE " + TB_ACC + " ("
            + COL_ACC_ID + " TEXT PRIMARY KEY, "
            + COL_ACC_NAME + " TEXT NOT NULL, "
            + COL_ACC_TYPE + " TEXT NOT NULL, "
            + COL_ACC_CASHACCOUNT + " INTEGER NULL, "
            + COL_ACC_PRIORITY + " INTEGER NULL, "
            + COL_ACC_INITVAL + " REAL NOT NULL)";
    private static final String ACC_DROP_SQL = "DROP TABLE IF EXISTS " + TB_ACC;


    private static final String DET_CREATE_SQL = "CREATE TABLE " + TB_DET + " ("
            + COL_DET_ID + " INTEGER PRIMARY KEY, "
            + COL_DET_FROM + " TEXT NOT NULL, "
            + COL_DET_FROM_TYPE + " TEXT NOT NULL, "
            + COL_DET_TO + " TEXT NOT NULL, "
            + COL_DET_TO_TYPE + " TEXT NOT NULL, "
            + COL_DET_DATE + " INTEGER NOT NULL, "
            + COL_DET_MONEY + " REAL NOT NULL, "
            + COL_DET_ARCHIVED + " INTEGER NOT NULL, "
            + COL_DET_NOTE + " TEXT)";

    private static final String DET_DROP_SQL = "DROP TABLE IF EXISTS " + TB_DET;


    public SQLiteDataHelper(Context context, String dbname) {
        super(context, dbname, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Logger.d("create schema {}", ACC_CREATE_SQL);
        db.execSQL(ACC_CREATE_SQL);

        Logger.d("create schema {}" + DET_CREATE_SQL);
        db.execSQL(DET_CREATE_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Logger.i("upgrade db from {} to {}", oldVersion, newVersion);

        if (oldVersion < 0) {
            Logger.i("reset schema");
            //drop and create.
            Logger.i("drop schema " + ACC_DROP_SQL);
            db.execSQL(ACC_DROP_SQL);
            Logger.i("drop schema " + DET_DROP_SQL);
            db.execSQL(DET_DROP_SQL);
            onCreate(db);
            return;
        }
        if (oldVersion == 4) {//schema before 0.9.4
            Logger.i("upgrade schem from "+oldVersion+" to "+newVersion);
            db.execSQL("ALTER TABLE " + TB_ACC + " ADD " + COL_ACC_CASHACCOUNT + " INTEGER NULL ");
            oldVersion++;
        }

        //keep going check next id
        if (oldVersion == 5) {//schema before ?
            Logger.i("upgrade schem from "+oldVersion+" to "+newVersion);
            db.execSQL("ALTER TABLE " + TB_ACC + " ADD " + COL_ACC_PRIORITY + " INTEGER NULL ");
            oldVersion++;
        }
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion){
        Logger.i("downgrade db from {} to {}", oldVersion, newVersion);

        if(newVersion==4){
            Logger.i("downgrade schema from "+oldVersion+" to "+newVersion);
            db.execSQL("ALTER TABLE " + TB_ACC + " DROP " + COL_ACC_PRIORITY);

            newVersion--;
        }
        if(newVersion==3){
            Logger.i("downgrade schema from "+oldVersion+" to "+newVersion);
            db.execSQL("ALTER TABLE " + TB_ACC + " DROP " + COL_ACC_CASHACCOUNT);

            newVersion--;
        }
    }

}
