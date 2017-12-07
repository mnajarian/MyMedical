package edu.unc.mnajarian.imed;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

/**
 * Created by mnajarian on 12/5/17.
 */

public class SearchResults extends AppCompatActivity {

    private String [] results;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_results);

        String dbQuery = getIntent().getStringExtra("queryStr");
        iMedDatabaseHelper helper = iMedDatabaseHelper.getInstance(this);
        SQLiteDatabase db = helper.getWritableDatabase();

        // execute query
        Cursor c = db.rawQuery(dbQuery, null);
        c.moveToFirst();

        results = new String[c.getCount()];
        for (int i=0; i<c.getCount(); i++) {
            String str = "";
            for (int j = 0; j < c.getColumnCount(); j++) {
                if (c.getString(j) != null && !c.getString(j).equals("")) {
                    str = str + c.getString(j) + "\n";
                }
            }
            Log.i("db query results", str);
            results[i] = str;
            c.moveToNext();
        }

        TextView tv1SearchResults = (TextView) findViewById(R.id.tv1SearchResults);
        tv1SearchResults.setText(results[0].substring(0, results[0].indexOf("content://")-1));

    }

    public void expandToPicture(View v){
        if (v.equals(findViewById(R.id.tv1SearchResults))){
            String r = results[0];
            String photoUri = r.substring(r.indexOf("content://"));
            Log.i("result string", photoUri);
            Intent x = new Intent(this, DisplaySearchImage.class);
            x.putExtra("photoUri", photoUri);
            startActivity(x);
        }
    }
    public void goHome(View v){
        Intent x = new Intent(this, MainActivity.class);
        startActivity(x);
    }

}
