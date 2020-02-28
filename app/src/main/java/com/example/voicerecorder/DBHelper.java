package com.example.voicerecorder;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DBHelper extends SQLiteOpenHelper {

    private Context context;
    public static final String DATABASE_NAME = "saved_recordings.db";
    private static final int DATABASE_VERSION = 1;
    public static final String TABLE_NAME = "saved_recording_table";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_PATH = "path";

    private static final String COMA_SEP = ",";

    private static final String SQLITE_CREATE_TABLE = "CREATE TABLE "+ TABLE_NAME + " (" +  COLUMN_ID + " INTEGER PRIMARY KEY"+
            " AUTOINCREMENT"+COMA_SEP+
            COLUMN_PATH + " TEXT " + ")";

    public DBHelper (Context context)
    {
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQLITE_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
        onCreate(db);
    }
    public boolean addRecording (RecordingItem recordingItem)
    {
        try {

            SQLiteDatabase db = getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put(COLUMN_PATH,recordingItem.getPath());

            db.insert(TABLE_NAME,null,contentValues);
            db.close();
            return true;

        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
    public Cursor fetchAllRecords()
    {
        SQLiteDatabase db = getReadableDatabase();
        String [] row = {COLUMN_PATH,COLUMN_ID};
        Cursor cursor = db.query(TABLE_NAME,row,null,null,null,null,null);
        if(cursor!= null)
            cursor.moveToFirst();
        db.close();
        return cursor;
    }
}
