package com.example.murryxi.traveldiaryapp;

import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.io.InputStream;

public class Register extends AppCompatActivity
{
    private Button signupBtn;
    private EditText newEmail, newUsername, newPassword;
    private FirebaseAuth fbAuth;
    private DatabaseReference dbRef;
    private TextView loginView;
    private ImageView logo;

    private static final String TAG = "Register";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        loginView = (TextView)findViewById(R.id.loginView);
        signupBtn = (Button)findViewById(R.id.registerBtn);
        newEmail = (EditText)findViewById(R.id.newEmail);
        newUsername = (EditText)findViewById(R.id.newUser);
        newPassword = (EditText)findViewById(R.id.newPass);
        logo = findViewById(R.id.app_logo);
        fbAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference().child("users");

        AssetManager assetManager = getAssets();

        InputStream is = null;
        try {
            is = assetManager.open("travel.png");
        } catch (IOException e) {
            e.printStackTrace();
        }
        Bitmap  bitmap = BitmapFactory.decodeStream(is);
        logo.setImageBitmap(bitmap);


        loginView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                startActivity(new Intent(Register.this, LogIn.class));
            }
        });

        signupBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Toast.makeText(Register.this, "Please wait...", Toast.LENGTH_LONG).show();
                final String username = newUsername.getText().toString().trim();
                final String email = newEmail.getText().toString().trim();
                final String password = newPassword.getText().toString().trim();


                if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(username) && !TextUtils.isEmpty(password))
                {
                    fbAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task)
                        {
                            if (task.isSuccessful())
                            {
                                Log.d(TAG, "createUserWithEmail:success");
                                FirebaseUser user = fbAuth.getCurrentUser();
                                String user_id = user.getUid();
                                final UserInfo u = new UserInfo(username, user_id, email);
                                dbRef.child(user_id).setValue(u);
                                /*dbRef.child("username").setValue(username);
                                dbRef.child("email").setValue(email);
                                dbRef.child("user_id").setValue(user.getUid());*/
                                Toast.makeText(Register.this, "Registration complete!", Toast.LENGTH_SHORT).show();
                                Intent reg_Intent = new Intent(Register.this, LogIn.class);
                                reg_Intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(reg_Intent);
                            }
                            else
                            {
                                Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                Toast.makeText(Register.this, "Registration failed...", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                else
                {
                    Toast.makeText(Register.this, "Some fields are missing...", Toast.LENGTH_SHORT).show();
                }

            }
        });


    }

}
