package example.com.fashionsme.fashionsme;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.LinearLayoutManager;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ImageButton;
import de.hdodenhof.circleimageview.CircleImageView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import java.lang.String;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

public class FindFriendsActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    private ImageButton SearchButton;
    private EditText SearchInputText;

    private RecyclerView SearchResultList;

    private DatabaseReference allUsersDatabaseRef;
    private FindFriends model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        allUsersDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users");

        mToolbar = (Toolbar) findViewById(R.id.find_friends_appbar_layout);
        getSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Find Friends");

        SearchResultList = (RecyclerView) findViewById(R.id.search_result_list);
        SearchResultList.setHasFixedSize(true);
        SearchResultList.setLayoutManager(new LinearLayoutManager(this));

        SearchButton = (ImageButton) findViewById(R.id.search_people_friends_button);
        SearchInputText = (EditText) findViewById(R.id.search_box_input);

        SearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                String searchBoxInput = SearchInputText.getText().toString();

                SearchPeopleAndFriends(searchBoxInput);
            }
        });
    }

    private ActionBar getSupportActionBar(Toolbar mToolbar) {
        return null;
    }

    private void SearchPeopleAndFriends(String searchBoxInput)
    {
        Toast.makeText(this, "Searching....", Toast.LENGTH_LONG).show();

        final Query searchPeopleandFriendsQuery = allUsersDatabaseRef.orderByChild("fullname")
                .startAt(searchBoxInput).endAt(searchBoxInput + "\uf8ff");

        /*Query searchPeopleandFriendsQuery = FirebaseDatabase.getInstance()
                .getReference()
                .child("users")
                .equalTo(name);*/



        FirebaseRecyclerAdapter<FindFriends, FindfriendsViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<FindFriends, FindfriendsViewHolder>
                (
                        FindFriends.class,
                        R.layout.all_users_display_layout,
                        FindfriendsViewHolder.class,
                        searchPeopleandFriendsQuery

                )

        {

            FirebaseRecyclerOptions<FindFriends> firebaseRecyclerOptions =
                    new FirebaseRecyclerOptions.Builder<FindFriends>()
                            .setQuery(searchPeopleandFriendsQuery, FindFriends.class)
                            .build();

            @NonNull
            @Override
            public FindfriendsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                return null;
            }

            @Override
            protected void onBindViewHolder(@NonNull FindfriendsViewHolder holder, int position, @NonNull FindFriends model) {

            }
            @Override
            protected void populateViewHolder(final FindfriendsViewHolder viewHolder, FindFriends model, final int position) {
                viewHolder.setFullname(model.getFullname());
                viewHolder.setStatus(model.getStatus());
                viewHolder.setProfileimage(getApplicationContext(), model.getProfileimage());

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        String visit_user_id = getRef(position).getKey();
                        Intent profileIntent = new Intent(FindFriendsActivity.this, PersonProfileActivity.class);
                        profileIntent.putExtra("visit_user_id", visit_user_id);
                        startActivity(profileIntent);
                    }
                });

            }

        };

        SearchResultList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class FindfriendsViewHolder extends RecyclerView.ViewHolder
    {
        View mView;

        public FindfriendsViewHolder(View itemView)
        {
            super(itemView);
            mView = itemView;
        }

        public void setProfileimage(Context ctx, String profileimage)
        {
            CircleImageView myImage = (CircleImageView) mView.findViewById(R.id.all_users_profile_image);

            Picasso.with(ctx).load(profileimage).placeholder(R.drawable.profile).into(myImage);
        }

        public void setFullname(String fullname)
        {
            TextView myName = (TextView) mView.findViewById(R.id.all_users_profile_full_name);
            myName.setText(fullname);
        }

        public void setStatus(String status)
        {
            TextView myName = (TextView) mView.findViewById(R.id.all_users_status);
            myName.setText(status);
        }
    }

}
