package example.com.fashionsme.fashionsme;

import android.app.ProgressDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.app.ActionBar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class PostActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private ProgressDialog loadingBar;

    private ImageButton SelectPostImage;
    private Button UpdatePostButton;
    private EditText PostDescription;

    private static final int Gallery_Pick = 1;
    private Uri ImageUri;
    private String Description;
    private boolean task;

    private StorageReference PostsImageRefrence;
    private DatabaseReference UsersRef, PostsRef;
    private FirebaseAuth mAuth;
    private String saveCurrentData, saveCurrentTimeReference, postRandomName, downloadUrl, current_user_id, saveCurrentDate, saveCurrentTime;
    private long countPosts = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();
        PostsImageRefrence = FirebaseStorage.getInstance().getReference();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");

        SelectPostImage = (ImageButton) findViewById(R.id.select_post_image);
        UpdatePostButton = (Button) findViewById(R.id.update_post_button);
        PostDescription = (EditText) findViewById(R.id.post_description);
        loadingBar = new ProgressDialog(this);

        mToolbar = (Toolbar) findViewById(R.id.update_post_page_toolbar);
        getSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Update Post");

        SelectPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenGallery();
            }
        });

        UpdatePostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                ValidatePostInfo();
            }
        });
    }

    private ActionBar getSupportActionBar(Toolbar mToolbar) {
    }

    private void ValidatePostInfo(){
        Description = PostDescription.getText().toString();

        if(ImageUri == null){
            Toast.makeText(this, "Please select post image ...", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(Description)){
            Toast.makeText(this, "Please say something about your image ...", Toast.LENGTH_SHORT).show();
        }
        else{
            loadingBar.setTitle("Add New Post");
            loadingBar.setMessage("Please wait, whilewe are updating your new post...");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);

            StoringImageToFirebaseStorage();
        }
    }

    private void StoringImageToFirebaseStorage() {
        Calendar calFordDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MM-YYYY");
        saveCurrentDate = currentDate.format(calFordDate.getTime());

        Calendar calFordTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
        saveCurrentTime = currentTime.format(calFordDate.getTime());

        postRandomName = saveCurrentDate + saveCurrentTime;

        StorageReference filePath = PostsImageRefrence.child("Post Image").child(ImageUri.getLastPathSegment() + postRandomName + ".jpg");

        filePath.putFile(ImageUri).addOnCompleteListener(new OnCompleteListener < UploadTask.TaskSnapshot > ()
        {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
            {
                if (task.isSuccessful()) {
                    downloadUrl = task.getResult().getDownloadUrl().toString();
                    Toast.makeText(PostActivity.this, "image uploaded successfully to storage", Toast.LENGTH_SHORT).show();

                    SavingPostInformationToDatabase();
                } else {
                    String message = task.getException().getMessage();
                    Toast.makeText(PostActivity.this, "Error occured :" + message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void SavingPostInformationToDatabase()
    {
        PostsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists())
                {
                    countPosts = dataSnapshot.getChildrenCount();
                }

                else
                {
                    countPosts = 0;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        UsersRef.child(current_user_id).addValueEventListener(new ValueEventListener(){
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String userFullName = dataSnapshot.child("fullname").getValue().toString();
                    String userProfileImage = dataSnapshot.child("Profileimage").getValue().toString();

                    HashMap postsMap = new HashMap();
                    postsMap.put("uid", current_user_id);
                    postsMap.put("date", saveCurrentDate);
                    postsMap.put("time", saveCurrentTime);
                    postsMap.put("description", Description);
                    postsMap.put("postimage", downloadUrl);
                    postsMap.put("profileimage", userProfileImage);
                    postsMap.put("fullname", userFullName);
                    postsMap.put("counter", countPosts);
                    PostsRef.child(current_user_id + postRandomName).updateChildren(postsMap)
                            .addOnCompleteListener(new OnCompleteListener(){
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if(task.isSuccessful())
                                    {
                                        SendUserToMainActivity();
                                        Toast.makeText(PostActivity.this, "New Post is updated successfully", Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();
                                    }
                                    else
                                    {
                                        Toast.makeText(PostActivity.this, "Error occured during updating posts", Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();
                                    }
                                }
                            });

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void OpenGallery() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, Gallery_Pick);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @NonNull Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Gallery_Pick && resultCode == RESULT_OK && data != null) {

            ImageUri = data.getData();
            SelectPostImage.setImageURI(ImageUri);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == android.R.id.home)
        {
            SendUserToMainActivity();
        }
        return super.onOptionsItemSelected(item);
    }


    private void SendUserToMainActivity()
    {
        Intent mainIntent = new Intent(PostActivity.this,MainActivity.class);
        startActivity(mainIntent);
    }
}
