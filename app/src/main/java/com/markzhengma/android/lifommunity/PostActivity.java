package com.markzhengma.android.lifommunity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.FileNotFoundException;

import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class PostActivity extends Fragment {
    private Button submitPostBtn;
    private Button addImageBtn;
    private EditText titleTextView;
    private EditText contentTextView;
    private Uri uri = null;

    private String titleText;
    private String contentText;
    private PostData post;
    private ImageView imageView;

    private FirebaseDatabase database;
    private DatabaseReference postRef;
    private DatabaseReference picRef;
    private DatabaseReference userRef;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;
    private ProgressDialog progressDialog;
    private String username;

    private static final int RC_PHOTO_PICKER = 1;
    private StorageReference mStorage;
//    private ProgressDialog mProgress;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View rootView = inflater.inflate(R.layout.activity_post, container, false);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        postRef = database.getReference("post");
        picRef = database.getReference("pic");
        userRef = database.getReference("users");
        user = mAuth.getCurrentUser();
        mStorage = FirebaseStorage.getInstance().getReference();
        progressDialog = new ProgressDialog(getActivity());


        titleTextView = rootView.findViewById(R.id.post_title_edit_text);
        contentTextView = rootView.findViewById(R.id.post_content_edit_text);
        submitPostBtn = rootView.findViewById(R.id.submit_post_btn);
        addImageBtn = rootView.findViewById(R.id.add_image_button);
        imageView = rootView.findViewById(R.id.post_image_view);
        setSubmitPostListener();
        setAddImageBtnListener();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(user != null){
            DatabaseReference childRef = userRef.child(user.getUid().toString());
            childRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    username = dataSnapshot.child("username").getValue().toString();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(getActivity(), "Error loading Firebase", Toast.LENGTH_SHORT).show();
                }
            });
        }
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                if (user == null) {
                    Log.v("NOT LOGGED IN", "**************************************");
                    startActivity(intent);
                }
            }
        };
    }

    @Override

    public void onPause() {
        super.onPause();
        mAuth.removeAuthStateListener(authStateListener);
    }

    private void getPostData(){
        titleText = titleTextView.getText().toString();
        contentText = contentTextView.getText().toString();

    }

    private void setSubmitPostListener(){

        submitPostBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                final FirebaseUser user = mAuth.getCurrentUser();
                final Date currentTime = Calendar.getInstance().getTime();
                if(user != null) {
                    try {
                        final SimpleDateFormat dateString = new SimpleDateFormat("MMM d, yyyy HH:mm:ss");
                        System.out.println(dateString.format(currentTime));
                        progressDialog.setMessage("Uploading...");
                        progressDialog.show();
                        getPostData();
                        if(uri != null) {
                            StorageReference filePath = mStorage.child("Post Image").child(uri.getLastPathSegment());
                            filePath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    Uri downloadUri = taskSnapshot.getDownloadUrl();
                                    postRef.child(currentTime.toString()).setValue(new PostData(user.getUid().toString(), username, downloadUri.toString(), dateString.format(currentTime), titleText, contentText));
                                    Toast.makeText(getActivity(), "Storage complete", Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();
                                }
                            });
                        }else{
                            postRef.child(currentTime.toString()).setValue(new PostData(user.getUid().toString(), username, null, dateString.format(currentTime), titleText, contentText));
                        }

                        //                StorageReference filePath = mStorage.child("Post Image").child(uri.getLastPathSegment());
                        //                filePath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        //                    @Override
                        //                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //                        Uri downloadUri = taskSnapshot.getDownloadUrl();
                        //                        Toast.makeText(getActivity(), "Storage complete", Toast.LENGTH_SHORT).show();
                        //                    }
                        //                });
                        Intent intent = new Intent(getActivity(), TabActivity.class);
                        startActivity(intent);
                        Log.v(username, currentTime.toString());
                    }
                    catch (Exception e) {
                        System.out.println(e.toString() + ".");
                    }
                }else{
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    startActivity(intent);
                }
            }
        });
    }

    //I want to allow the user to upload his or her local image and save the image to the firebase as well as the main page
    private void setAddImageBtnListener(){
        addImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });
    }

    private void selectImage(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/jpeg");
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PHOTO_PICKER);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(data != null) {
            uri = data.getData();//Uri can store the value and path of the image and we can get the path from data
        }
        if (resultCode != Activity.RESULT_OK) {return;}

        if (requestCode == RC_PHOTO_PICKER) {
            try {
                //final Date currentTime = Calendar.getInstance().getTime();
                decodeUri(uri);
                picRef.push().setValue(ImageUtil.bitmapToByteString(((BitmapDrawable) imageView.getDrawable()).getBitmap())); // Save image to Firebase
                Toast.makeText(getActivity(), "Upload successfully", Toast.LENGTH_SHORT).show();

            } catch (FileNotFoundException e) {
                Toast.makeText(getActivity(), "Error uploading photo", Toast.LENGTH_SHORT).show();
            }
//        }else if(requestCode == REQUEST_IMAGE_CAPTURE){
//            try {
//                progressDialog.setMessage("Uploading...");
//                progressDialog.show();
//                Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
//                imageView.setImageBitmap(thumbnail);
//                picRef.push().setValue(ImageUtil.bitmapToByteString(((BitmapDrawable) imageView.getDrawable()).getBitmap())); // Save image to Firebase
//                Toast.makeText(getActivity(), "Upload successfully", Toast.LENGTH_SHORT).show();
//                mStorage = mStorage.child("Post Image").child(uri.getLastPathSegment());
//                mStorage.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                    @Override
//                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                        Uri downloadUri = taskSnapshot.getDownloadUrl();
//                        Toast.makeText(getActivity(), "Storage complete", Toast.LENGTH_SHORT).show();
//                        progressDialog.dismiss();
//                    }
//                });
//            } catch (Exception e) {
//                Toast.makeText(getActivity(), "Error uploading photo", Toast.LENGTH_SHORT).show();
//            }
       }

//            mProgress.setMessage("uploading image...");
//            mProgress.show();
//            Uri cameraUri = data.getData();
//            StorageReference filePath = mStorage.child("photo").child(cameraUri.getLastPathSegment());
//            filePath.getFile(cameraUri).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
//                @Override
//                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
//                    Toast.makeText(getActivity(), "Upload successfully", Toast.LENGTH_SHORT).show();
//                    mProgress.dismiss();
//                }
//            }).addOnFailureListener(new OnFailureListener() {
//                @Override
//                public void onFailure(@NonNull Exception e) {
//                    Toast.makeText(getActivity(), "Failure Upload", Toast.LENGTH_SHORT).show();
//                }
//            });
//            }
//        super.onActivityResult(requestCode, resultCode, data);
    }

    // Compress and then display the image
    private void decodeUri(Uri uri) throws FileNotFoundException {

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();

        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(getActivity().getApplicationContext().getContentResolver().openInputStream(uri), null, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image so it's not bigger than 500 x 500 pixels
        int scaleFactor = (int) Math.ceil(Math.min(photoW / 500, photoH / 500));

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        // Create the compressed bitmap and load it to the imageView
        Bitmap bitmap = BitmapFactory.decodeStream(getActivity().getApplicationContext().getContentResolver().openInputStream(uri), null, bmOptions);
        imageView.setImageBitmap(bitmap);
    }





    //I want to make the picture that the camera took to be saved into firebase as well as the main page
//    private void setCameraBtnListener(){
//        cameraBtn.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//
//                if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
//                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
//                }
//            }
//        });
    }

