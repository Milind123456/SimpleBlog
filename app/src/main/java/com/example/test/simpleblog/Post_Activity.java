package com.example.test.simpleblog;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_);

        selectImage = (ImageButton) findViewById(R.id.imagefld);

        mtitle = (EditText) findViewById(R.id.titlefield);
        mdesc = (EditText) findViewById(R.id.descriptionfield);

        msubmit = (Button) findViewById(R.id.submitbutton);

        mStorage = FirebaseStorage.getInstance().getReference();

        mRef = FirebaseDatabase.getInstance().getReference().child("Blog");

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

                    Uri downloadUrl = taskSnapshot.getDownloadUrl();

                    DatabaseReference newPost = mRef.push();

                    newPost.child("title").setValue(title);
                    newPost.child("description").setValue(desc);
                    newPost.child("image").setValue(downloadUrl.toString());


                    mProgress.dismiss();


                    Intent intent = new Intent(Post_Activity.this, MainActivity.class);
                    startActivity(intent);
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
