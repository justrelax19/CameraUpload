package com.example.anku.camerauload;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    ArrayList<Uri> fileuris=new ArrayList<Uri>();
    Service service;
	
	// LogCat tag
	private static final String TAG = MainActivity.class.getSimpleName();


    private static final int MY_PERMISSIONS_REQUEST=100;
    public int PICK_IMAGE_FROM_GALLERY_REQUEST=1;
    // Camera activity request codes
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    private static final int CAMERA_CAPTURE_VIDEO_REQUEST_CODE = 200;
    
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
 
    private Uri fileUri; // file url to store image/video
    
    private Button btnCapturePicture, btnRecordVideo;
 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Changing action bar background color
        // These two lines are not needed
//        getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#1f2649")));
 
        btnCapturePicture = (Button) findViewById(R.id.btnCapturePicture);
        btnRecordVideo = (Button) findViewById(R.id.btnRecordVideo);
 
        /**
         * Capture image button click event
         */
        btnCapturePicture.setOnClickListener(new View.OnClickListener() {
 
            @Override
            public void onClick(View v) {
                // capture picture
                captureImage();
            }
        });
 
        /**
         * Record video button click event
         */

        // Checking camera availability
        if (!isDeviceSupportCamera()) {
            Toast.makeText(getApplicationContext(),
                    "Sorry! Your device doesn't support camera",
                    Toast.LENGTH_LONG).show();
            // will close the app if the device does't have camera
            finish();
        }
    }
 
    /**
     * Checking device has camera hardware or not
     * */
    private boolean isDeviceSupportCamera() {
        if (getApplicationContext().getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }
 
    /**
     * Launching camera app to capture image
     */
    private void captureImage() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);



        fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
        //Log.e("file",fileUri+"");
       fileuris.add(fileUri);
        Log.e("size",fileuris.size()+"");
 
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
 
        // start the image capture Intent
        startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
    }
    

    /**
     * Here we store the file url as it will be null after returning from camera
     * app
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
 
        // save file url in bundle as it will be null on screen orientation
        // changes
      //  Log.e("file",fileUri+"");

        outState.putParcelable("file_uri", fileUri);
    //    fileuris.add(fileUri);
      //  Log.e("size",fileuris.size()+"");

    }
 
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
 
        // get the file url
        fileUri = savedInstanceState.getParcelable("file_uri");

    }
 
    
 
    /**
     * Receiving activity result method will be called after closing the camera
     * */
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        // if the result is capturing Image
//        if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
//            if (resultCode == RESULT_OK) {
//
//            	// successfully captured the image
//                // launching upload activity
//            	launchUploadActivity(true);
//
//
//            } else if (resultCode == RESULT_CANCELED) {
//
//            	// user cancelled Image capture
//                Toast.makeText(getApplicationContext(),
//                        "User cancelled image capture", Toast.LENGTH_SHORT)
//                        .show();
//
//            } else {
//                // failed to capture image
//                Toast.makeText(getApplicationContext(),
//                        "Sorry! Failed to capture image", Toast.LENGTH_SHORT)
//                        .show();
//            }
//
//        } else if (requestCode == CAMERA_CAPTURE_VIDEO_REQUEST_CODE) {
//            if (resultCode == RESULT_OK) {
//
//            	// video successfully recorded
//                // launching upload activity
//            	launchUploadActivity(false);
//
//            } else if (resultCode == RESULT_CANCELED) {
//
//            	// user cancelled recording
//                Toast.makeText(getApplicationContext(),
//                        "User cancelled video recording", Toast.LENGTH_SHORT)
//                        .show();
//
//            } else {
//                // failed to record video
//                Toast.makeText(getApplicationContext(),
//                        "Sorry! Failed to record video", Toast.LENGTH_SHORT)
//                        .show();
//            }
//        }
//    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        Log.e("data",data+"");

        android.net.Uri selectedImage = fileUri;
     //  Log.e("Imagee",selectedImage+"");
        if(fileuris.size()==1)
        {
            Log.e("reach here","size is 1");
           // Log.e("file:",fileUri+"");
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();
            // Change base URL to your upload server URL.
            service = new Retrofit.Builder().baseUrl("https://sporophoric-reservo.000webhostapp.com/").client(client).build().create(Service.class);
            Log.e("edf","ssssf");
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Log.e("filepath",filePathColumn+"");
            Cursor cursor =managedQuery(selectedImage, filePathColumn, null, null, null);
            int columnindex=cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();

            Log.e("cursor",cursor.getString(columnindex));
            if (cursor == null)
                return;

            cursor.moveToFirst();
            //   File file=FileUtils.getFile(this,selectedImage);
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String filePath = cursor.getString(columnIndex);
            cursor.close();

            File file = new File(filePath);

            RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), file);
            MultipartBody.Part body = MultipartBody.Part.createFormData("upload", file.getName(), reqFile);
            RequestBody name = RequestBody.create(MediaType.parse("text/plain"), "upload_test");


            retrofit2.Call<okhttp3.ResponseBody> req = service.postImage1(body, name);
            req.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    Log.e("Image Uploading","1");
                    Toast.makeText(getApplicationContext(),"uploading image",Toast.LENGTH_LONG).show();
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    t.printStackTrace();
                }
            });

        }

        else if(requestCode==PICK_IMAGE_FROM_GALLERY_REQUEST && resultCode==RESULT_OK )
        {
            Log.e("reach here","size is 2");

            ClipData clipData=data.getClipData();
            Log.e("dd",clipData.getItemCount()+"");

            for(int i=0;i<clipData.getItemCount();i++)
            {
                ClipData.Item item=clipData.getItemAt(i);
                Uri uri=item.getUri();
                fileuris.add(uri);

            }
            postImage(fileuris);
        }
    }

    @NonNull
    private RequestBody createPartFromString(String s) {
        return RequestBody.create(
                okhttp3.MultipartBody.FORM, s);
    }

    @NonNull
    private MultipartBody.Part prepareFilePart(String partName, Uri fileUri) {
        // https://github.com/iPaulPro/aFileChooser/blob/master/aFileChooser/src/com/ipaulpro/afilechooser/utils/FileUtils.java
        // use the FileUtils to get the actual file by uri
        File file = FileUtils.getFile(this, fileUri);
        Log.e("part",partName+"");
        // create RequestBody instance from file
        RequestBody requestFile =
                RequestBody.create(
                        MediaType.parse(getContentResolver().getType(fileUri)),
                        file
                );

        // MultipartBody.Part is used to send also the actual file name
        return MultipartBody.Part.createFormData(partName, file.getName(), requestFile);
    }
    private  void postImage(List<Uri> fileuris)
    {
        final EditText nn=(EditText)findViewById(R.id.input);
        Retrofit.Builder builder=new Retrofit.Builder()
                .baseUrl("https://sporophoric-reservo.000webhostapp.com/")
                .addConverterFactory(GsonConverterFactory.create());

        Retrofit retrofit=builder.build();

        Service client=retrofit.create(Service.class);

        List<MultipartBody.Part> parts=new ArrayList<>();
        for(int i=0;i<fileuris.size();i++)
        {
            parts.add(prepareFilePart("file_array[]",fileuris.get(i)));
        }



        Call<ResponseBody> call=client.postImage(parts,createPartFromString(nn.getText().toString()));
        call.enqueue((new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Toast.makeText(MainActivity.this,"Yes",Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(MainActivity.this,"No",Toast.LENGTH_LONG).show();
            }
        }));
    }






















//    private void launchUploadActivity(boolean isImage){
//    	Intent i = new Intent(MainActivity.this, UploadActivity.class);
//        i.putExtra("filePath", fileUri.getPath());
//        i.putExtra("isImage", isImage);
//        startActivity(i);
//    }
//
//    /**
//     * ------------ Helper Methods ----------------------
//     * */
//
//    /**
//     * Creating file uri to store image/video
//     */
    public Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }
//
//    /**
//     * returning image / video
//     */
    private static File getOutputMediaFile(int type) {

        // External sdcard location
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                Config.IMAGE_DIRECTORY_NAME);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(TAG, "Oops! Failed create "
                        + Config.IMAGE_DIRECTORY_NAME + " directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "IMG_" + timeStamp + ".jpg");
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }
}