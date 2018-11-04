package example.com.fashionsme.fashionsme;

import android.annotation.SuppressLint;
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
import android.widget.Toast;

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

import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    private EditText UserName, FullName, CountryName;
    private Button SaveInformationbutton;
    private CircleImageView ProfileImage;
    private ProgressDialog loadingBar;

    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef;
    private StorageReference UserProfileImageRef;

    String currentUserID;
    final static int Gallery_Pick = 1;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("profile Images");

        UserName = (EditText) findViewById(R.id.setup_username);
        FullName = (EditText) findViewById(R.id.setup_full_name);
        CountryName = (EditText) findViewById(R.id.setup_country);
        SaveInformationbutton = (Button) findViewById(R.id.setup_button);
        ProfileImage = (CircleImageView) findViewById(R.id.setup_profile_image);
        loadingBar = new ProgressDialog(this);

        SaveInformationbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SaveAccountSetupInformation();
            }
        });

        ProfileImage.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view)
            {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, Gallery_Pick);
            }
        });

        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    if(dataSnapshot.hasChild("profileimage")) {
                        String image = dataSnapshot.child("profileimage").getValue().toString();

                        Picasso.with(SetupActivity.this).load(image).placeholder(R.drawable.profile).into(ProfileImage);
                    }
                    else{
                        Toast.makeText(SetupActivity.this, "Please select Profile image first", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Gallery_Pick && resultCode == RESULT_OK && data != null) {
            Uri ImageUri = data.getData();

            CropImage.activity(ImageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                loadingBar.setTitle("Profile Image");
                loadingBar.setMessage("Please wait, while we are updating your profile image..");
                loadingBar.setCanceledOnTouchOutside(true);
                loadingBar.show();


                Uri resultUri = result.getUri();

                StorageReference filePath = UserProfileImageRef.child(currentUserID + ".jpg");

                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(SetupActivity.this, "Profile Image stored successfully to Firebase storage", Toast.LENGTH_SHORT).show();

                            final String downloadUrl = task.getResult().getDownloadUrl().toString();

                            UsersRef.child("profileimage").setValue(downloadUrl)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Intent selfIntent = new Intent(SetupActivity.this, SetupActivity.class);
                                                startActivity(selfIntent);

                                                Toast.makeText(SetupActivity.this, "Profile Image stored to Firebase successfully..", Toast.LENGTH_SHORT).show();
                                                loadingBar.dismiss();
                                            } else {
                                                String message = task.getException().getMessage();
                                                Toast.makeText(SetupActivity.this, "Error occured: " + message, Toast.LENGTH_SHORT).show();
                                                loadingBar.dismiss();
                                            }

                                        }
                                    });
                        }
                    }

                });

            }
            else
            {
                Toast.makeText(SetupActivity.this, "Error occured: Image can't be cropped ! Try again..", Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }
        }
    }

    private void SaveAccountSetupInformation (){
        String username = UserName.getText().toString();
        String fullname = FullName.getText().toString();
        String country = CountryName.getText().toString();

        if (TextUtils.isEmpty(username)) {
            Toast.makeText(this, "Please write your username...", Toast.LENGTH_SHORT).
                    show();
        }

        if (TextUtils.isEmpty(fullname)) {
            Toast.makeText(this, "Please write your full name...", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(country)) {
            Toast.makeText(this, "Please enter your country name...", Toast.LENGTH_SHORT).show();
        }

        else {

            loadingBar.setTitle("Saving Information");
            loadingBar.setMessage("Please wait, while we are creating your new Account..");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);

            HashMap userMap = new HashMap();
            userMap.put("username", username);
            userMap.put("fullname", fullname);
            userMap.put("country", country);
            userMap.put("status", "Hello");
            userMap.put("gender", "none");
            userMap.put("dob", "none");
            userMap.put("relationshipstatus", "none");
            UsersRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        SendUserToMainActivity();
                        Toast.makeText(SetupActivity.this, "Your account is created successfully", Toast.LENGTH_LONG).show();
                        loadingBar.dismiss();
                    } else {
                        String message = task.getException().getMessage();
                        Toast.makeText(SetupActivity.this, "Error occured" + message, Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }

                }
            });
        }
    }

    private void SendUserToMainActivity () {
        Intent mainIntent = new Intent(SetupActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

}
