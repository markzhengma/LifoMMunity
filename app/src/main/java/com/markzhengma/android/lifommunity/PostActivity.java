package com.markzhengma.android.lifommunity;

import android.content.Intent;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class PostActivity extends AppCompatActivity {
    private Button homeBtn;
    private Button profileBtn;
    private Button postBtn;
    private Button submitPostBtn;
    private Button addImageBtn;
    private Button cameraBtn;
    private EditText titleTextView;
    private EditText contentTextView;

    private String titleText;
    private String contentText;

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference postRef = database.getReference("post");
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseAuth.AuthStateListener authStateListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        homeBtn = findViewById(R.id.home_btn);
        postBtn = findViewById(R.id.post_btn);
        profileBtn = findViewById(R.id.profile_btn);

        titleTextView = findViewById(R.id.post_title_edit_text);
        contentTextView = findViewById(R.id.post_content_edit_text);
        submitPostBtn = findViewById(R.id.submit_post_btn);
        addImageBtn = findViewById(R.id.add_image_button);
        cameraBtn = findViewById(R.id.camera_button);
        titleText = titleTextView.getText().toString();
        contentText = contentTextView.getText().toString();


        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null)
                    startActivity(new Intent(PostActivity.this, LoginActivity.class));
            }
        };

        setHomeBtnListener();
        setProfileBtnListener();
        setSubmitPostListener();
        setCameraBtnListener();
    }


    protected void onStart() {
        super.onStart();
        auth.addAuthStateListener(authStateListener);
    }

    @Override

    public void onStop() {
        super.onStop();
        auth.removeAuthStateListener(authStateListener);
    }


    public void setHomeBtnListener() {
        homeBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                loadMainActivity();
            }
        });
    }

    private void loadMainActivity(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }


    public void setProfileBtnListener(){
        profileBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                loadProfileActivity();
            }
        });
    }

    private void loadProfileActivity(){
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }

    private void getPostData(){
        titleText = titleTextView.getText().toString();
        contentText = contentTextView.getText().toString();
    }

    private void setPostData(){
        PostData newPost = new PostData(titleText, contentText);
        Intent intent = new Intent();
        intent.putExtra("NEW_POST", newPost);
        setResult(RESULT_OK, intent);
        finish();
    }



    private void setSubmitPostListener(){

        submitPostBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                getPostData();
                DatabaseReference userPostRef = postRef.child(titleText);
                userPostRef.setValue(contentText);
                setPostData();
            }
        });
    }

    private void setAddImageBtnListener(){
        addImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    private void setCameraBtnListener(){
        cameraBtn.setOnClickListener(new View.OnClickListener() {
            static final int REQUEST_IMAGE_CAPTURE = 1;
            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            }
        });
    }

}
