package edu.unc.mnajarian.mymedical;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;

/**
 * Created by mnajarian on 12/5/17.
 */

public class SearchRecords extends AppCompatActivity {

    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_records);

        db = this.openOrCreateDatabase(getResources().getString(R.string.iMed_database), Context.MODE_PRIVATE, null);

    }

    public void searchSingleRecord(View v){

        // TODO: Get strings from edit text fields
        // keep two strings: one for column names, one for values
        // if the edit text fields are not empty, add them to both strings


        // execute query

        // start another activity intent to display results


    }





}
