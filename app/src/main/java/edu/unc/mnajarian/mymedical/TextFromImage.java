package edu.unc.mnajarian.mymedical;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.NaturalLanguageUnderstanding;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.AnalysisResults;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.AnalyzeOptions;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.EntitiesOptions;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.EntitiesResult;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.Features;
import com.sixthsolution.apex.Apex;
import com.sixthsolution.apex.nlp.english.EnglishParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class TextFromImage extends AppCompatActivity {

    Uri currentPicUri;
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fields_from_image);

        db = this.openOrCreateDatabase(getResources().getString(R.string.iMed_database), Context.MODE_PRIVATE, null);
        currentPicUri = Uri.parse(getIntent().getStringExtra("captureUri"));

        // show thumbnail
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), currentPicUri);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // ocr processing
        new TextFromImage.RunOCR(this).execute(bitmap);

    }

    private class RunOCR extends AsyncTask<Bitmap, Void, String[]> {
        private Context context;
        TextRecognizer textRecognizer;

        private RunOCR(Context context) {
            this.context = context;
        }

        protected String[] doInBackground(Bitmap... b) {
            Bitmap bitmap = b[0];
            // StringBuilder sb = new StringBuilder();
            TextRecognizer textRecognizer = new TextRecognizer.Builder(context).build();

            if(bitmap != null) {

                if(!textRecognizer.isOperational()) {
                    // Note: The first time that an app using a Vision API is installed on a
                    // device, GMS will download a native libraries to the device in order to do detection.
                    // Usually this completes before the app is run for the first time.  But if that
                    // download has not yet completed, then the above call will not detect any text,
                    // barcodes, or faces.
                    // isOperational() can be used to check if the required native libraries are currently
                    // available.  The detectors will automatically become operational once the library
                    // downloads complete on device.
                    Log.i("LOG", "Detector dependencies are not yet available.");

                    // Check for low storage.  If there is low storage, the native library will not be
                    // downloaded, so detection will not become operational.
                    IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
                    boolean hasLowStorage = registerReceiver(null, lowstorageFilter) != null;

                    if (hasLowStorage) {
                        Toast.makeText(context,"Low Storage", Toast.LENGTH_LONG).show();
                        Log.w("LOG", "Low Storage");
                    }
                }

                Frame imageFrame = new Frame.Builder()
                        .setBitmap(bitmap)
                        .build();

                SparseArray<TextBlock> textBlocks = textRecognizer.detect(imageFrame);
                String [] textStrings = new String[textBlocks.size()];
                for (int i = 0; i < textBlocks.size(); i++) {
                    TextBlock textBlock = textBlocks.get(textBlocks.keyAt(i));
                    //sb.append(textBlock.getValue()+"\n");
                    textStrings[i] = textBlock.getValue()+"\n";
                    //Log.i("OCR Text Block", textBlock.getValue());
                }
                return textStrings;
            }

            return null;
        }

        protected void onPostExecute(String[] result) {
            //TextView ti = (TextView) findViewById(R.id.ti);

            // Date extraction test
            Apex.init(new Apex.ApexBuilder()
                    .addParser("en", new EnglishParser())
                    .build());

            String strResult = "";
            for (int i=0; i<result.length; i++){
                strResult += result[i]+"\n";
                /*if (result[i].toLowerCase().contains("date")) {
                    Log.i("Apex test", result[i]);
                    try {
                        Log.i("APEX TEST", Apex.nlp("en", result[i]).toString());
                    } catch (org.threeten.bp.DateTimeException e) {
                        Log.i("Exception tag", "Datetime exception");
                    }
                }*/
            }
            Log.i("OCR Entire text", strResult);
            //ti.setText(strResult);


            //Button labResultButton = (Button) findViewById(R.id.saveAsLab);
            //labResultButton.setVisibility(View.VISIBLE);
            // Watson NLU
            new RunWatsonNLU(context).execute(strResult);

        }

    }

    private class RunWatsonNLU extends AsyncTask<String, Void, HashMap<String, ArrayList<String>>>{

        private Context context;

        private RunWatsonNLU(Context context) {
            this.context = context;
        }

        protected HashMap<String, ArrayList<String>> doInBackground(String... textBlocks){

            String tb = textBlocks[0]; // Assume first list

            // create list for each category and populate with contendors from NLU
            ArrayList<String> dates = new ArrayList<String>();
            ArrayList<String> drugs = new ArrayList<String>();
            ArrayList<String> docs = new ArrayList<String>();
            ArrayList<String> locations = new ArrayList<String>();
            ArrayList<String> orgs = new ArrayList<String>();
            ArrayList<String> quantities = new ArrayList<String>();

            NaturalLanguageUnderstanding service = new NaturalLanguageUnderstanding(
                    NaturalLanguageUnderstanding.VERSION_DATE_2017_02_27,
                    getResources().getString(R.string.natural_language_understanding_username),
                    getResources().getString(R.string.natural_language_understanding_password));

            // Extract entities from custom model -- picks out drugs and dates
            EntitiesOptions entitiesModel = new EntitiesOptions.Builder()
                    // TODO: abstract into strings xml file
                    // use custom model with drug names
                    .model(getResources().getString(R.string.natural_language_understanding_model_ID))
                    .build();
            AnalysisResults resultsModel = runNLURequest(service, entitiesModel, tb);
            Log.i("NLU with Model results", resultsModel.toString());
            List<EntitiesResult> erm = resultsModel.getEntities();
            for (int i=0; i<erm.size(); i++){
                EntitiesResult e = (EntitiesResult) erm.get(i);
                if (e.getType().equals("Date")){
                    dates.add(e.getText());
                }
                if (e.getType().equals("Drug") && !e.getText().equals("Date")) {
                    drugs.add(e.getText());
                }
            }

            // Extract standard entities (no custom model)
            EntitiesOptions entities = new EntitiesOptions.Builder().build();
            AnalysisResults results = runNLURequest(service, entities, tb);
            Log.i("NLU results no model", results.toString());
            List<EntitiesResult> er = results.getEntities();
            for (int i=0; i<er.size(); i++){
                EntitiesResult e = (EntitiesResult) er.get(i);
                // TODO: Don't let it add the user as a Person
                if (e.getType().equals("Person")){
                    docs.add(e.getText());
                }
                if (e.getType().equals("Location")) {
                    locations.add(e.getText());
                }
                if (e.getType().equals("Organization") || e.getType().equals("Company")){
                    orgs.add(e.getText());
                }
                if (e.getType().equals("Quantity")){
                    quantities.add(e.getText());
                }
            }

            // pull out only relevant ones
            HashMap<String,ArrayList<String>> d = new HashMap<String,ArrayList<String>>();
            d.put("Date", dates);
            d.put("Drug", drugs);
            d.put("Doc", docs);
            d.put("Location", locations);
            d.put("Organization", orgs);
            d.put("Quantity", quantities);

            return d;
        }

        private AnalysisResults runNLURequest(NaturalLanguageUnderstanding service,
                                              EntitiesOptions entities,
                                              String t){
            Features features = new Features.Builder()
                    .entities(entities)
                    .build();
            AnalyzeOptions parameters = new AnalyzeOptions.Builder()
                    .text(t)
                    .features(features)
                    .build();
            AnalysisResults results = service.analyze(parameters).execute();
            return results;
        }

        private String arrayToString(ArrayList<String> sarr){
            StringBuilder sb = new StringBuilder();
            for (int i=0; i<sarr.size(); i++){
                sb.append(sarr.get(i));
                if (i<sarr.size()-1){
                    sb.append(", ");
                }
            }
            return sb.toString();
        }

        protected void onPostExecute(HashMap<String, ArrayList<String>> h){
            // do something with Watson result (entities)
            // Log.i("Watson tag", h.toString());

            // set result edit text views

            EditText etDate = (EditText) findViewById(R.id.etDate);
            //etDate.setText(h.get("Date").toString());
            etDate.setText(arrayToString(h.get("Date")));

            EditText etDoc = (EditText) findViewById(R.id.etDoc);
            //etDoc.setText(h.get("Doc").toString());
            etDoc.setText(arrayToString(h.get("Doc")));

            EditText etLocation = (EditText) findViewById(R.id.etLocation);
            //etLocation.setText(h.get("Location").toString());
            etLocation.setText(arrayToString(h.get("Location")));

            EditText etDrugs = (EditText) findViewById(R.id.etDrugs);
            //etDrugs.setText(h.get("Drug").toString());
            etDrugs.setText(arrayToString(h.get("Drug")));

            EditText etCompany = (EditText) findViewById(R.id.etCompany);
            //etCompany.setText(h.get("Organization").toString());
            etCompany.setText(arrayToString(h.get("Organization")));

            EditText etQuantities = (EditText) findViewById(R.id.etQuantities);
            //etQuantities.setText(h.get("Quantity").toString());
            etQuantities.setText(arrayToString(h.get("Quantity")));

            // get rid of loading spinner, as we now have results
            findViewById(R.id.loadingPanel).setVisibility(View.GONE);

            // show thumbnail
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), currentPicUri);
            } catch (IOException e) {
                e.printStackTrace();
            }

            ImageView imgCapture = (ImageView) findViewById(R.id.imgCaptureThumb);
            imgCapture.setImageBitmap(bitmap);
        }
    }

    public void saveRecord(View v){
        // move back to main activity page

        // insert record into sqlite db
        ArrayList<String> record = new ArrayList<String>();
        record.add(((EditText) findViewById(R.id.etDate)).getText().toString()); // date field
        record.add(((EditText) findViewById(R.id.etCompany)).getText().toString()); // visit field
        record.add(((EditText) findViewById(R.id.etDoc)).getText().toString()); // visit_professional field
        record.add(((EditText) findViewById(R.id.etLocation)).getText().toString()); // visit_location field
        record.add(((EditText) findViewById(R.id.etDrugs)).getText().toString()); // drugs field
        record.add(((EditText) findViewById(R.id.etQuantities)).getText().toString()); // drugs_quantity field
        record.add(((EditText) findViewById(R.id.etTests)).getText().toString()); // tests field
        record.add(((EditText) findViewById(R.id.etVisitReason)).getText().toString()); // Reason field
        record.add(((EditText) findViewById(R.id.etNotes)).getText().toString()); // Additional_notes field
        record.add(currentPicUri.toString()); // photo URI

        StringBuilder insertQuery = new StringBuilder();
        insertQuery.append("INSERT INTO records (date, visit, visit_professional, visit_location," +
                "drugs, drugs_quantity, tests, reason, additional_notes, photoURI) VALUES (");
        for (int i=0; i<record.size()-1; i++){
            insertQuery.append("'"+record.get(i)+"',");
        }
        insertQuery.append("'"+record.get(record.size()-1)+"');");

        db.execSQL(insertQuery.toString());

        /*Cursor c = db.rawQuery("select * from records;", null);
        c.moveToFirst();
        for (int i=0; i<c.getCount(); i++){
            String str = "";
            for (int j=0; j < c.getColumnCount(); j++){
                str = str + c.getString(j) + " ";
            }
            Log.i("db query", str);
            c.moveToNext();
        }*/

        Intent x = new Intent(this, MainActivity.class);
        startActivity(x);
        Toast.makeText(this, "Record saved!", Toast.LENGTH_SHORT).show();
    }



}
