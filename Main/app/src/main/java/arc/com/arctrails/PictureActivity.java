package arc.com.arctrails;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.FileDescriptor;
import java.io.IOException;

public class PictureActivity extends AppCompatActivity {

    private static int RESULT_LOAD_IMAGE = 1;
    private final int PICTURE_CROP = 2;
    private Uri picUri;
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_picture);

        Button btnCamera = (Button)findViewById(R.id.btnCamera);
        Button btnGallery = (Button)findViewById(R.id.btnGallery);
        imageView = (ImageView)findViewById(R.id.imageView);

        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentCapture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intentCapture, 0);
            }
        });

        btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentGallery = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intentGallery.setType("image/*");
                startActivityForResult(intentGallery, RESULT_LOAD_IMAGE);
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        //1
        if(requestCode == RESULT_LOAD_IMAGE) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };
            Cursor cursor = getContentResolver().query(selectedImage,filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            Bitmap bitmap = null;
            try {
                bitmap = getBitmapFromUri(selectedImage);
            }
            catch (IOException e){
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            imageView.setImageBitmap(bitmap);

            picUri = data.getData();
            performCrop();
        }
        else if(requestCode == PICTURE_CROP) {
            Bundle extras = data.getExtras();
            Bitmap bitmap = extras.getParcelable("data");

            imageView.setImageBitmap(bitmap);
        }
        else {
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(bitmap);

            picUri = data.getData();
            performCrop();
        }
    }

    //This method is to assist in getting an image from the gallery
    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor = getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }

    /*
        This method crops the actual image
        aspect X and Y implement the aspect ratio
        output X and Y implement the ratio after the picture is taken
    */
    private void performCrop() {
        Intent intentCrop = new Intent("com.android.camera.action.CROP");
        intentCrop.setDataAndType(picUri, "image/*");
        intentCrop.putExtra("crop", "true");
        intentCrop.putExtra("aspectX", 2);
        intentCrop.putExtra("aspectY", 1);
        intentCrop.putExtra("outputX", 256);
        intentCrop.putExtra("outputY", 128);
        intentCrop.putExtra("return-data", true);
        startActivityForResult(intentCrop, PICTURE_CROP);
    }

}
