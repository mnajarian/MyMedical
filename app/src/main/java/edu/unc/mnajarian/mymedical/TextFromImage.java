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

import java.io.IOException;


public class TextFromImage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.text_from_image);

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

    private class RunOCR extends AsyncTask<Bitmap, Void, String> {
        private Context context;
        TextRecognizer textRecognizer;

        private RunOCR(Context context) {
            this.context = context;
        }

        protected String doInBackground(Bitmap... b) {
            Bitmap bitmap = b[0];
            StringBuilder sb = new StringBuilder();
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
                for (int i = 0; i < textBlocks.size(); i++) {
                    TextBlock textBlock = textBlocks.get(textBlocks.keyAt(i));
                    sb.append(textBlock.getValue());
                }
            }
            return sb.toString();
        }

        protected void onPostExecute(String result) {
            TextView ti = (TextView) findViewById(R.id.ti);
            ti.setText(result);

            Button labResultButton = (Button) findViewById(R.id.saveAsLab);
            labResultButton.setVisibility(View.VISIBLE);
        }

    }

}
