package example.com.fashionsme.fashionsme;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.LinearLayoutManager;
import android.widget.Toast;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import android.text.TextUtils;
import java.util.Calendar;
import java.util.HashMap;
import java.util.SimpleTimeZone;
import org.w3c.dom.Comment;


public class CommentsActivity extends AppCompatActivity
{
    private RecyclerView CommentsList;
    private ImageButton PostCommentButton;
    private EditText CommentInputText;

    private DatabaseReference UsersRef, PostsRef;
    private FirebaseAuth mAuth;

    private String Post_Key, current_user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        Post_Key = getIntent().getExtras().get("Post Key").toString();

        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(Post_Key).child("Comments");

        CommentsList = (RecyclerView) findViewById(R.id.comments_list);
        CommentsList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        CommentsList.setLayoutManager(linearLayoutManager);

        CommentInputText = (EditText) findViewById(R.id.comment_input);
        PostCommentButton = (ImageButton) findViewById(R.id.post_comment_btn);

        PostCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                UsersRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        if(dataSnapshot.exists())
                        {
                            String userName = dataSnapshot.child("username").getValue().toString();

                            ValidateComment(userName);

                            CommentInputText.setText("");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });
    }

     @Override
    protected void onStart()
    {
        super.onStart();

        FirebaseRecyclerAdapter<Comments, CommentsViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<Comments, CommentsViewHolder>
                (
                        Comments.class,
                        R.layout.all_comments_layout,
                        CommentsViewHolder.class,
                        PostsRef
                )
        {
            @NonNull
            @Override
            public CommentsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                return null;
            }

            @Override
            protected void onBindViewHolder(@NonNull CommentsViewHolder holder, int position, @NonNull Comments model) {

            }

            @Override
            protected void populateViewHolder(CommentsViewHolder viewHolder, Comments model, int position)
            {
                viewHolder.setUsername(model.getUsername());
                viewHolder.setComment(model.getComment());
                viewHolder.setDate(model.getDate());
                viewHolder.setTime(model.getTime());
            }
        };

        CommentsList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class CommentsViewHolder extends RecyclerView.ViewHolder
    {
        View mView;

        public CommentsViewHolder(View itemView)
        {
            super(itemView);

            mView = itemView;
        }

        public void setUsername(String username)
        {
            TextView myUserName = (TextView) mView.findViewById(R.id.comment_username);
            myUserName.setText("@" + username + "");
        }

        public void setComment(String comment)
        {
            TextView myComment = (TextView) mView.findViewById(R.id.comment_text);
            myComment.setText(comment);
        }

        public void setDate(String date)
        {
            TextView myDate = (TextView) mView.findViewById(R.id.comment_date);
            myDate.setText(" Date: " + date);
        }

        public void setTime(String time)
        {
            TextView myTime = (TextView) mView.findViewById(R.id.comment_time);
            myTime.setText(" Time:  " + time);
        }
    }

    private void ValidateComment(String userName)
    {
        String commentText = CommentInputText.getText().toString();

        if (TextUtils.isEmpty(commentText))
        {
            Toast.makeText(this, "please write text to comment", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Calendar calFordDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MM-YYYY");
            final String saveCurrentDate = currentDate.format(calFordDate.getTime());

            Calendar calFordTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
            final String saveCurrentTime = currentTime.format(calFordDate.getTime());

            final String RandomKey = current_user_id + saveCurrentDate + saveCurrentTime;

            HashMap commentMap = new HashMap();
            commentMap.put("uid", current_user_id);
            commentMap.put("comment", commentText);
            commentMap.put("date", saveCurrentDate);
            commentMap.put("time", saveCurrentTime);
            commentMap.put("username", userName);
            PostsRef.child(RandomKey).updateChildren(commentMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task task)
                        {
                            if (task.isSuccessful())
                            {
                                Toast.makeText(CommentsActivity.this, "You have commented successfully ...", Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                Toast.makeText(CommentsActivity.this, "Error occured! try again", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }
}
