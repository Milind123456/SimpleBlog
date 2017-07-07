package com.example.test.simpleblog;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class Post_Activity extends AppCompatActivity {
    ImageButton selectImage;
    EditText mtitle, mdesc;
    Button msubmit;
    private static final int GALLERY_REQUEST = 1;
    Uri imageUri = null;
    private StorageReference mStorage;
    private ProgressDialog mProgress;
    private DatabaseReference mRef;
    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private DatabaseReference mDatabaseUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_);
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        selectImage = (ImageButton) findViewById(R.id.imagefld);

        mtitle = (EditText) findViewById(R.id.titlefield);
        mdesc = (EditText) findViewById(R.id.descriptionfield);

        msubmit = (Button) findViewById(R.id.submitbutton);

        mStorage = FirebaseStorage.getInstance().getReference();

        mRef = FirebaseDatabase.getInstance().getReference().child("Blog");
        mDatabaseUser = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid());

        mProgress = new ProgressDialog(this);

        selectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_REQUEST);

            }
        });


        msubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPosting();
            }
        });
    }

    private void startPosting() {
        mProgress.setMessage("Posting to Message...");
        mProgress.show();

        final String title = mtitle.getText().toString().trim();
        final String desc = mdesc.getText().toString();

        if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(desc) && imageUri != null) {
            StorageReference filePath = mStorage.child("Blog-Users").child(imageUri.getLastPathSegment());


            filePath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    final Uri downloadUrl = taskSnapshot.getDownloadUrl();

                    final DatabaseReference newPost = mRef.push();



                    mDatabaseUser.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            newPost.child("title").setValue(title);
                            newPost.child("description").setValue(desc);
                            newPost.child("image").setValue(downloadUrl.toString());
                            newPost.child("uid").setValue(mCurrentUser.getUid());
                            newPost.child("username").setValue(dataSnapshot.child("name").getValue()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){


                                        Intent intent = new Intent(Post_Activity.this, MainActivity.class);
                                        startActivity(intent);

                                    }
                                }
                            });
                            mProgress.dismiss();

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {


                        }
                    });

                }
            });

        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {

            imageUri = data.getData();

            selectImage.setImageURI(imageUri);

        }


        super.onActivityResult(requestCode, resultCode, data);
    }
}
