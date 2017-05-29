package com.example.anku.camerauload;

/**
 * Created by Anku on 5/29/2017.
 */
import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;



interface Service {
    @Multipart
    @POST("upload.php")
    Call<ResponseBody> postImage(@Part List<MultipartBody.Part> image, @Part("name") RequestBody name);

    @Multipart
    @POST("fileUpload.php")
    Call<ResponseBody> postImage1(@Part MultipartBody.Part image, @Part("name") RequestBody name);
}