package com.example.cropdetector;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.UUID;

public class UploadUtil {
    private static String TAG="UploadUtil";
    Bitmap image;
    String crop;
    String latitude;
    String longitude;
    String uuid;
    String downloadUrl;
    Context context;
    public UploadUtil(Context context,Bitmap img,String crop,String lat,String longitude)
    {
        this.image = img;
        this.crop = crop;
        this.latitude = lat;
        this.longitude = longitude;
        this.context = context;
        downloadUrl="www.google.com";

    }
    public void upload()
    {
           uuid = UUID.randomUUID().toString();
           uploadToStorage();
    }
    public void uploadToStorage()
    {
        final StorageReference sref = FirebaseStorage.getInstance().getReference().child("Crops").child(crop).child(uuid);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG,100,baos);
        byte[] data = baos.toByteArray();
        UploadTask uploadTask = sref.putBytes(data);
        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if(!task.isSuccessful())
                    throw task.getException();
                return sref.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if(task.isSuccessful())
                {
                    Uri uri = task.getResult();
                    downloadUrl=uri.toString();
                    Log.d(TAG, "onComplete: "+uri);
                    uploadToDatabase();
                }
            }
        });

    }
    public void uploadToDatabase()
    {
         DatabaseReference dref =FirebaseDatabase.getInstance().getReference().child("Crops");
         dref.child(crop).child(uuid).child("Latitude").setValue(latitude);
         dref.child(crop).child(uuid).child("Longitude").setValue(longitude);
         dref.child(crop).child(uuid).child("Name").setValue(crop);
         dref.child(crop).child(uuid).child("DownloadUrl").setValue(downloadUrl);

    }

}
