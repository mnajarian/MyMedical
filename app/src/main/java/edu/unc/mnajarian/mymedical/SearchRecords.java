package edu.unc.mnajarian.mymedical;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mnajarian on 12/5/17.
 */

public class SearchRecords extends AppCompatActivity {

    // SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_records);

        // db = this.openOrCreateDatabase(getResources().getString(R.string.iMed_database), Context.MODE_PRIVATE, null);

    }

    public void searchSingleRecord(View v){

        // TODO: Get strings from edit text fields

        HashMap <String,String> recordQuery = new HashMap<String, String>();
        // TODO: Clean this up

        if (!((EditText) findViewById(R.id.searchDate)).getText().toString().equals("")){
            recordQuery.put("date", ((EditText) findViewById(R.id.searchDate))
                    .getText().toString().toLowerCase());
        }
        if (!((EditText) findViewById(R.id.searchCompany)).getText().toString().equals("")) {
            recordQuery.put("visit", ((EditText) findViewById(R.id.searchCompany))
                    .getText().toString().toLowerCase());
        }
        if (!((EditText) findViewById(R.id.searchDoc)).getText().toString().equals("")){
            recordQuery.put("visit_professional", ((EditText) findViewById(R.id.searchDoc))
                    .getText().toString().toLowerCase());
        }
        if (!((EditText) findViewById(R.id.searchLocation)).getText().toString().equals("")){
            recordQuery.put("visit_location", ((EditText) findViewById(R.id.searchLocation))
                    .getText().toString().toLowerCase());
        }
        if (!((EditText) findViewById(R.id.searchDrugs)).getText().toString().equals("")){
            recordQuery.put("drugs", ((EditText) findViewById(R.id.searchDrugs)).getText().toString());
        }
        if (!((EditText) findViewById(R.id.searchQuantities)).getText().toString().equals("")){
            recordQuery.put("drugs_quantity", ((EditText) findViewById(R.id.searchQuantities))
                    .getText().toString().toLowerCase());
        }
        if (!((EditText) findViewById(R.id.searchTests)).getText().toString().equals("")){
            recordQuery.put("tests", ((EditText) findViewById(R.id.searchTests))
                    .getText().toString().toLowerCase());
        }
        if (!((EditText) findViewById(R.id.searchVisitReason)).getText().toString().equals("")){
            recordQuery.put("reason", ((EditText) findViewById(R.id.searchVisitReason))
                    .getText().toString().toLowerCase());
        }
        if (!((EditText) findViewById(R.id.searchNotes)).getText().toString().equals("")){
            recordQuery.put("additional_notes", ((EditText) findViewById(R.id.searchNotes))
                    .getText().toString().toLowerCase());
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Select * from records where ");
        int counter = 0;
        for (HashMap.Entry<String, String> entry : recordQuery.entrySet()) {
            String col = entry.getKey();
            String searchVal = entry.getValue();
            if (!searchVal.isEmpty()) {
                if (counter < recordQuery.size()-1) {
                    sb.append(col + " LIKE '%" + searchVal + "%' and ");
                } else {
                    sb.append(col + " LIKE '%" + searchVal + "%'");
                }
            }
            counter++;
        }

        Log.i("db query", sb.toString());

        /*iMedDatabaseHelper helper = iMedDatabaseHelper.getInstance(this);
        SQLiteDatabase db = helper.getWritableDatabase();

        // execute query
        Cursor c = db.rawQuery(sb.toString(), null);
        c.moveToFirst();

        for (int i=0; i<c.getCount(); i++) {
            String str = "";
            for (int j = 0; j < c.getColumnCount(); j++) {
                str = str + c.getString(j) + " ";
            }
            Log.i("db query results", str);
            c.moveToNext();
        }*/

        Intent x = new Intent(this, SearchResults.class);
        x.putExtra("queryStr", sb.toString());
        startActivity(x);


        // start another activity intent to display results


    }





}
