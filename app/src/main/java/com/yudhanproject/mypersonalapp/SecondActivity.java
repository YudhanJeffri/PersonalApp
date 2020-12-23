package com.yudhanproject.mypersonalapp;

import androidx.appcompat.app.AppCompatActivity;

import android.accounts.Account;
import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.people.v1.PeopleService;
import com.google.api.services.people.v1.model.Birthday;
import com.google.api.services.people.v1.model.Date;
import com.google.api.services.people.v1.model.Gender;
import com.google.api.services.people.v1.model.Person;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;

public class SecondActivity extends AppCompatActivity {

    static final private int RC_SIGN_IN = 1;
    static final private String TAG = "hole";
    private WeakReference<SecondActivity> weakAct = new WeakReference<>(this);
    private GoogleSignInClient mGoogleSignInClient;
    private GoogleSignInAccount account;

    TextView name,birth,email;
    ImageView user_image;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        account = GoogleSignIn.getLastSignedInAccount(this);


        name = findViewById(R.id.name);
        birth = findViewById(R.id.birthday);
        email = findViewById(R.id.email);
        user_image = findViewById(R.id.user_image);

        name.setText(account.getDisplayName());
        email.setText(account.getEmail());
        if (account != null) {
            SharedPreferences sharedPref = getSharedPreferences(account.getId(), MODE_PRIVATE);
            if (sharedPref.contains("gender")) { //this checking works since null still saved
                String gender = sharedPref.getString("gender", "");
                Log.d(TAG, "gender: " + gender);
                if (sharedPref.contains("bday")) { //this checking works since null still saved
                    String bday = sharedPref.getString("bday", "");
                    String bmonth = sharedPref.getString("bmonth", "");
                    String byear = sharedPref.getString("byear", "");
                    birth.setText(bday + "/" + bmonth + "/" + byear);

                    Glide.with(this).load(account.getPhotoUrl()).into(user_image);
                }
            }
        }

    }
}