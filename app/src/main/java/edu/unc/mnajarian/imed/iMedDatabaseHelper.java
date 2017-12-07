package edu.unc.mnajarian.imed;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by mnajarian on 12/5/17.
 */
public class iMedDatabaseHelper extends SQLiteOpenHelper {

    private static iMedDatabaseHelper sInstance;


    // Database Info
    private static final String DATABASE_NAME = "iMedDB";
    private static final int DATABASE_VERSION = 4;

    // Table Names
    private static final String TABLE_RECORDS = "records";


    public static synchronized iMedDatabaseHelper getInstance(Context context) {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new iMedDatabaseHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    /**
     * Constructor should be private to prevent direct instantiation.
     * Make a call to the static method "getInstance()" instead.
     */
    private iMedDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Called when the database connection is being configured.
    // Configure database settings for things like foreign key support, write-ahead logging, etc.
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    // Called when the database is created for the FIRST time.
    // If a database already exists on disk with the same DATABASE_NAME, this method will NOT be called.
    @Override
    public void onCreate(SQLiteDatabase db) {
        String create_records_table = "CREATE TABLE "+TABLE_RECORDS+" (ID INT PRIMARY KEY, date TEXT," +
                "visit TEXT, visit_professional TEXT, visit_location TEXT," +
                "drugs TEXT, drugs_quantity TEXT, tests TEXT," +
                "reason TEXT, additional_notes TEXT, photoURI TEXT);";
        db.execSQL("DROP TABLE IF EXISTS records;");
        db.execSQL(create_records_table);
    }

    // Called when the database needs to be upgraded.
    // This method will only be called if a database already exists on disk with the same DATABASE_NAME,
    // but the DATABASE_VERSION is different than the version of the database that exists on disk.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            // Simplest implementation is to drop all old tables and recreate them
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECORDS);
            onCreate(db);
        }
    }
}