package edu.unc.mnajarian.mymedical;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.NaturalLanguageUnderstanding;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.AnalysisResults;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.AnalyzeOptions;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.EntitiesOptions;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.Features;
import com.sixthsolution.apex.Apex;
import com.sixthsolution.apex.nlp.english.EnglishParser;

import java.io.IOException;


public class TextFromImage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fields_from_image);

        Uri photoUri = Uri.parse(getIntent().getStringExtra("captureUri"));

        // show thumbnail
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoUri);
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
            TextView ti = (TextView) findViewById(R.id.ti);

            // Date extraction test
            Apex.init(new Apex.ApexBuilder()
                    .addParser("en", new EnglishParser())
                    .build());

            String strResult = "";
            for (int i=0; i<result.length; i++){
                strResult += result[i];
                /*if (result[i].toLowerCase().contains("date")) {
                    Log.i("Apex test", result[i]);
                    try {
                        Log.i("APEX TEST", Apex.nlp("en", result[i]).toString());
                    } catch (org.threeten.bp.DateTimeException e) {
                        Log.i("Exception tag", "Datetime exception");
                    }
                }*/
            }
            // Log.i("OCR Entire text", strResult);
            ti.setText(strResult);


            Button labResultButton = (Button) findViewById(R.id.saveAsLab);
            labResultButton.setVisibility(View.VISIBLE);
            // Watson NLU
            new RunWatsonNLU().execute(strResult);

        }

    }

    private class RunWatsonNLU extends AsyncTask<String, Void, String>{

        protected String doInBackground(String... textBlocks){

            String tb = textBlocks[0]; // Assume first list
            NaturalLanguageUnderstanding service = new NaturalLanguageUnderstanding(
                    NaturalLanguageUnderstanding.VERSION_DATE_2017_02_27,
                    getResources().getString(R.string.natural_language_understanding_username),
                    getResources().getString(R.string.natural_language_understanding_password));

            EntitiesOptions entities = new EntitiesOptions.Builder().build();
            Features features = new Features.Builder().entities(entities).build();
            StringBuilder sb = new StringBuilder();
            /* for (int i=0; i<tb.length; i++){
                Log.i("OCR Text", tb[i]);
                if (tb[i].length() >= 15) {
                    AnalyzeOptions parameters = new AnalyzeOptions.Builder().text(tb[i]).features(features).build();
                    AnalysisResults results = service.analyze(parameters).execute();
                    Log.i("Watson tag", results.toString());
                    sb.append(results.toString());
                }
            }*/
            AnalyzeOptions parameters = new AnalyzeOptions.Builder().text(tb).features(features).build();
            AnalysisResults results = service.analyze(parameters).execute();
            return sb.toString();
        }

        protected void onPostExecute(String result){
            // do something with Watson result (entities)
            Log.i("Watson tag", result);


        }


    }

}
