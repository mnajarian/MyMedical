package edu.unc.mnajarian.mymedical;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    Uri currentPicUri;
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup database

        /*db = this.openOrCreateDatabase(getResources().getString(R.string.iMed_database), Context.MODE_PRIVATE, null);
        db.execSQL("DROP TABLE IF EXISTS Records");
        db.execSQL("CREATE TABLE records (ID INT PRIMARY KEY, date TEXT," +
                "visit TEXT, visit_professional TEXT, visit_location TEXT," +
                "drugs TEXT, drugs_quantity TEXT, tests TEXT," +
                "reason TEXT, additional_notes TEXT, photoURI TEXT);");*/

    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        /**Create the storage directory if it does not exist*/
        if (! storageDir.exists()){
            if (! storageDir.mkdirs()){
                return null;
            }
        }
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        return image;
    }

    public void captureImage(View v){
        // Ensure that there's a camera activity to handle the intent
        Intent x = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (x.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.i("ERROR", "Error occurred while creating the File");
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "edu.unc.mnajarian.mymedical",
                        photoFile);
                Log.i("PhotoURI", photoURI.toString());
                currentPicUri = photoURI;
                x.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                Log.i("XIntent", x.toString());
                startActivityForResult(x, 1);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Intent x = new Intent(this, CaptureImage.class);
            x.putExtra("captureUri", currentPicUri.toString()); //key, value
            startActivity(x);
        }
    }

    public void searchRecords(View v){
        Intent x = new Intent(this, SearchRecords.class);
        startActivity(x);
    }


}
