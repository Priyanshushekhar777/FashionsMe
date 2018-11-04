package example.com.fashionsme.fashionsme;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;
import android.text.TextUtils;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.lang.CharSequence;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    private EditText userName, userProfName, userStatus, userCountry, userGender, userRelation, userDOB;
    private Button UpdateAccountSettingsButton;
    private CircleImageView userProfImage;
    private ProgressDialog loadingBar;

    private DatabaseReference SettingsUserRef;
    private FirebaseAuth mAuth;
    private StorageReference UserProfileImageRef;

    private String currentUserId;
    final static int Gallery_Pick = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        SettingsUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("profile Images");

        mToolbar = (Toolbar) findViewById(R.id.settings_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        userName = (EditText) findViewById(R.id.settings_username);
        userProfName = (EditText) findViewById(R.id.settings_profile_full_name);
        userStatus = (EditText) findViewById(R.id.settings_status);
        userCountry = (EditText) findViewById(R.id.settings_country);
        userGender = (EditText) findViewById(R.id.settings_gender);
        userRelation = (EditText) findViewById(R.id.settings_relationship_status);
        userDOB = (EditText) findViewById(R.id.settings_dob);
        userProfImage = (CircleImageView) findViewById(R.id.settings_profile_image);
        UpdateAccountSettingsButton = (Button) findViewById(R.id.update_account_settings_buttons);
        loadingBar = new ProgressDialog(this);

        SettingsUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    String myProfileImage = dataSnapshot.child("profileimage").getValue().toString();
                    String myUserName = dataSnapshot.child("username").getValue().toString();
                    String myProfileName = dataSnapshot.child("fullname").getValue().toString();
                    String myProfileStatus = dataSnapshot.child("status").getValue().toString();
                    String myDOB = dataSnapshot.child("dob").getValue().toString();
                    String mycountry = dataSnapshot.child("country").getValue().toString();
                    String myGender = dataSnapshot.child("gender").getValue().toString();
                    String myRelationStatus = dataSnapshot.child("relationshipstatus").getValue().toString();

                    Picasso.with(SettingActivity.this).load(myProfileImage).placeholder(R.drawable.profile).into(userProfImage);

                    userName.setText(myUserName);
                    userProfName.setText(myProfileName);
                    userStatus.setText(myProfileStatus);
                    userDOB.setText(myDOB);
                    userCountry.setText(mycountry);
                    userGender.setText(myGender);
                    userRelation.setText(myRelationStatus);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        UpdateAccountSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                ValidateAccountInfo();
            }
        });

        userProfImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, Gallery_Pick);
            }
        });

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
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

                StorageReference filePath = UserProfileImageRef.child(currentUserId + ".jpg");

                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(SettingActivity.this, "Profile Image stored successfully to Firebase storage", Toast.LENGTH_SHORT).show();

                            final String downloadUrl = task.getResult().getDownloadUrl().toString();

                            SettingsUserRef.child("profileimage").setValue(downloadUrl)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Intent selfIntent = new Intent(SettingActivity.this, SetupActivity.class);
                                                startActivity(selfIntent);

                                                Toast.makeText(SettingActivity.this, "Profile Image stored to Firebase successfully..", Toast.LENGTH_SHORT).show();
                                                loadingBar.dismiss();
                                            } else {
                                                String message = task.getException().getMessage();
                                                Toast.makeText(SettingActivity.this, "Error occured: " + message, Toast.LENGTH_SHORT).show();
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
                Toast.makeText(SettingActivity.this, "Error occured: Image can't be cropped ! Try again..", Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }
        }
    }


    private void ValidateAccountInfo()
    {
        String username = userName.getText().toString();
        String profilename = userProfName.getText().toString();
        String status = userStatus.getText().toString();
        String dob = userDOB.getText().toString();
        String country = userCountry.getText().toString();
        String gender = userGender.getText().toString();
        String relation = userRelation.getText().toString();

        if (TextUtils.isEmpty(username))
        {
            Toast.makeText(this,"Please write your username...", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(profilename))
        {
            Toast.makeText(this,"Please write your profile name...", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(status))
        {
            Toast.makeText(this,"Please write your status...", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(dob))
        {
            Toast.makeText(this,"Please write your dob...", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(country))
        {
            Toast.makeText(this,"Please write your country...", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(gender))
        {
            Toast.makeText(this,"Please write your gender...", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(relation))
        {
            Toast.makeText(this,"Please write your relation...", Toast.LENGTH_SHORT).show();
        }

        else
        {
            loadingBar.setTitle("Profile Image");
            loadingBar.setMessage("Please wait, while we are updating your profile image..");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();

            UpdateAccountInfo(username, profilename, status, dob, country, gender, relation);
        }
    }

    private void UpdateAccountInfo(String username, String profilename, String status, String dob, String country, String gender, String relation)
    {
        HashMap userMap = new HashMap();
        userMap.put("usename",username);
        userMap.put("fullname",profilename);
        userMap.put("status",status);
        userMap.put("dob",dob);
        userMap.put("country",country);
        userMap.put("gender",gender);
        userMap.put("relationshipstatus",relation);

        SettingsUserRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task)
            {
                if(task.isSuccessful())
                {
                    SendUserToMainActivity();
                    Toast.makeText(SettingActivity.this, "Account Settings Updated successfully", Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                }
                else
                {
                    Toast.makeText(SettingActivity.this, "Error Occure while updating account", Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                }
            }
        });
    }

    private void SendUserToMainActivity(){
        Intent mainIntent = new Intent(SettingActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

}

