package example.com.fashionsme.fashionsme;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private TextView userName, userProfName, userStatus, userCountry, userGender, userRelation, userDOB;
    private CircleImageView userProfileImage;

    private DatabaseReference profileUserRef, FriendsRef, PostsRef;
    private FirebaseAuth mAuth;
    private Button MyPosts, MyFriends;

    private String currentUserId;
    private int countFriends = 0, countPosts = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        profileUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        FriendsRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");

        userName = (TextView) findViewById(R.id.my_username);
        userProfName = (TextView) findViewById(R.id.my_profile_full_name);
        userStatus = (TextView) findViewById(R.id.my_profile_status);
        userCountry = (TextView) findViewById(R.id.my_country);
        userGender = (TextView) findViewById(R.id.my_gender);
        userRelation = (TextView) findViewById(R.id.my_relationship_status);
        userDOB = (TextView) findViewById(R.id.my_dob);
        userProfileImage = (CircleImageView) findViewById(R.id.my_profile_pic);
        MyPosts = (Button) findViewById(R.id.my_post_button);
        MyFriends = (Button) findViewById(R.id.my_friends_button);

        MyFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                SendUserToFriendsActivity();
            }
        });

        MyPosts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                SendUserToMyPostsActivity();
            }
        });

        PostsRef.orderByChild("uid")
                .startAt(currentUserId).endAt(currentUserId + "\uf8ff")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        if (dataSnapshot.exists())
                        {
                            countPosts = (int) dataSnapshot.getChildrenCount();
                            MyPosts.setText(Integer.toString(countPosts) + " Posts");
                        }
                        else
                        {
                            MyPosts.setText("0 Posts");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

        FriendsRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists())
                {
                    countFriends = (int) dataSnapshot.getChildrenCount();
                    MyFriends.setText(Integer.toString(countFriends )+ "  Friends");
                }
                else
                {
                    MyFriends.setText("0 Friends");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        profileUserRef.addValueEventListener(new ValueEventListener() {
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

                    Picasso.with(ProfileActivity.this).load(myProfileImage).placeholder(R.drawable.profile).into(userProfileImage);

                    userName.setText( "@" + myUserName);
                    userProfName.setText(myProfileName);
                    userStatus.setText(myProfileStatus);
                    userDOB.setText( "DOB:" + myDOB);
                    userCountry.setText( "Country:" + mycountry);
                    userGender.setText( "Gender:" + myGender);
                    userRelation.setText( "Relationship:" + myRelationStatus);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError){

            }

        });
    }

    private void SendUserToMyPostsActivity() {
        Intent friendsIntent = new Intent(ProfileActivity.this, MyPostActivity.class);
        startActivity(friendsIntent);
    }

    private void SendUserToFriendsActivity() {
        Intent friendsIntent = new Intent(ProfileActivity.this, FriendsActivity.class);
        startActivity(friendsIntent);
    }

}
