package example.com.fashionsme.fashionsme;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.content.Intent;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class PasswordResetActivity extends AppCompatActivity
{
    private Toolbar mToolbar;
    private Button ResetPasswordSendEmailButton;
    private EditText ResetEmailInput;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_reset);

        mAuth = FirebaseAuth.getInstance();
        mToolbar = (Toolbar) findViewById(R.id.forget_password_toolbar);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Reset Password");

        ResetPasswordSendEmailButton = (Button) findViewById(R.id.reset_password_email_button);
        ResetEmailInput = (EditText) findViewById(R.id.reset_password_EMAIL);

        ResetPasswordSendEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String userEmail = ResetEmailInput.getText().toString();

                if(TextUtils.isEmpty(userEmail))
                {
                    Toast.makeText(PasswordResetActivity.this, "Please write your valid email first...", Toast.LENGTH_SHORT).show();
                }

                else
                {
                    mAuth.sendPasswordResetEmail(userEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                Toast.makeText(PasswordResetActivity.this, "Please check your email Account , if you want to reset your password...", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(PasswordResetActivity.this, LoginActivity.class));
                            }
                            else
                            {
                                String message = task.getException().getMessage();
                                Toast.makeText(PasswordResetActivity.this, "Error occured: " + message, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }
}
