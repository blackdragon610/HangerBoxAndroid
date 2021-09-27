package com.hanger_box.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.hanger_box.R;
import com.hanger_box.common.Common;
import com.hanger_box.utils.ImageUtils;
import com.hanger_box.views.TouchImageView;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import static com.hanger_box.common.Common.cm;

public class ImageEditActivity extends AppCompatActivity {

    private static final int CAMERA_ACTIVITY_ID = 2002;
    private static final int GALLERY_ACTIVITY_ID = 2003;
    private final static int CAMERA_PERMISSIONS_RESULT = 102;

    private Uri imageUri;
    private File targetImage = null;
    private CropImageView imageView;

    private String from;
    private int rotate = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_edit);

        Common.currentActivity = this;

        from = getIntent().getExtras().getString("from");

        ArrayList<String> permissions = new ArrayList();
        permissions.add(Manifest.permission.CAMERA);
        permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);

        ArrayList requirePermissions = cm.checkPermissions(permissions);
        if (!requirePermissions.isEmpty()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions((String[]) requirePermissions.toArray(new String[requirePermissions.size()]),
                        CAMERA_PERMISSIONS_RESULT);
            }
        }else {
            if (from.equals("from_camera")) {
                camera_call();
            }else {
                gallery_call();
            }
        }

        findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent returnIntent = new Intent();
                setResult(Activity.RESULT_CANCELED, returnIntent);
                finish();
            }
        });

        findViewById(R.id.save_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap cropped = imageView.getCroppedImage();
                String root = Environment.getExternalStorageDirectory().toString();
                File myDir = new File(root + "/hanger_box");
                if (!myDir.exists()) {
                    myDir.mkdirs();
                }
                String fName = "cropped_image.jpeg";
                final File dest = new File(myDir.getPath()+"/"+fName);
                ImageUtils.createImage(cropped, fName, myDir.getPath());
                Intent returnIntent = new Intent();
                returnIntent.putExtra("path", dest.getAbsolutePath());
                setResult(Activity.RESULT_OK,returnIntent);
                finish();
            }
        });

        imageView = findViewById(R.id.image_view);

        findViewById(R.id.rotate_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageView.rotateImage(90);
            }
        });

    }

    private void camera_call()
    {
        ContentValues values = new ContentValues();
        imageUri = getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent intent1 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent1.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);

        startActivityForResult(intent1, CAMERA_ACTIVITY_ID);
    }

    private void gallery_call()
    {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");

        startActivityForResult(photoPickerIntent, GALLERY_ACTIVITY_ID);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CAMERA_ACTIVITY_ID:
                    try
                    {
                        String filePath = ImageUtils.getRealPathFromURI(imageUri.toString());
                        final File dest = new File(filePath);
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                        Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);
                        imageView.setImageBitmap(bitmap);
                        if (dest.exists()) {
                            targetImage = dest;
                        }
                    }
                    catch(Exception e)
                    {
                        e.printStackTrace();
                    }
                    break;
                case GALLERY_ACTIVITY_ID:
                    if (data != null) {
                        Uri selectedImage = data.getData();
                        InputStream imageStream = null;
                        try {
                            imageStream = getContentResolver().openInputStream(selectedImage);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        Bitmap bitmap = BitmapFactory.decodeStream(imageStream);
                        imageView.setImageBitmap(bitmap);
                    }
                    break;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_PERMISSIONS_RESULT:
                if (cm.hasPermission(Manifest.permission.CAMERA)) {
                    camera_call();
                }
                break;
            case GALLERY_ACTIVITY_ID:
                if (cm.hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    gallery_call();
                }
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Common.currentActivity = this;
    }
}