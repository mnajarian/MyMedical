package edu.unc.mnajarian.mymedical;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

import java.io.IOException;

/**
 * Created by mnajarian on 12/5/17.
 */

public class DisplaySearchImage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.display_search_image);

        String photoUriStr = getIntent().getStringExtra("photoUri");
        Log.i("photo uri", photoUriStr);
        Uri photoUri = Uri.parse(photoUriStr);
        // show thumbnail
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoUri);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ImageView imgCapture = (ImageView) findViewById(R.id.ivSearchImage);
        imgCapture.setImageBitmap(bitmap);

    }
}
