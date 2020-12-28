package com.yudhanproject.mypersonalapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.accounts.Account;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class SecondActivity extends AppCompatActivity {

    static final private int RC_SIGN_IN = 1;
    static final private String TAG = "hole";
    private WeakReference<SecondActivity> weakAct = new WeakReference<>(this);
    private GoogleSignInClient mGoogleSignInClient;
    private GoogleSignInAccount account;

    TextView name, birth, email;
    ImageView user_image;

    Button sign_out;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        account = GoogleSignIn.getLastSignedInAccount(this);
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


        name = findViewById(R.id.name);
        birth = findViewById(R.id.birthday);
        email = findViewById(R.id.email);
        user_image = findViewById(R.id.user_image);

        sign_out = findViewById(R.id.sign_out);

        name.setText(account.getDisplayName());
        email.setText(account.getEmail());
        if (account != null) {
            SharedPreferences sharedPref = getSharedPreferences(account.getId(), MODE_PRIVATE);
            if (sharedPref.contains("gender")) { //this checking works since null still saved
                String gender = sharedPref.getString("gender", "");
                if (sharedPref.contains("bday") && birth != null) { //this checking works since null still saved
                    String bday = sharedPref.getString("bday", "");
                    String bmonth = sharedPref.getString("bmonth", "");
                    String byear = sharedPref.getString("byear", "");
                    birth.setOnClickListener(v -> {
                        Calendar calendar = Calendar.getInstance();
                        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                calendar.set(Calendar.YEAR, year);
                                calendar.set(Calendar.MONTH, month);
                                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                                birth.setText(dayOfMonth + "/" + month + "/" + year);
                            }
                        };
                        new DatePickerDialog(SecondActivity.this, dateSetListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
                    });
                    birth.setText(bday + "/" + bmonth + "/" + byear);

                    Glide.with(this).load(account.getPhotoUrl()).into(user_image);
                }
            }


            sign_out.setOnClickListener(v -> mGoogleSignInClient.signOut()
                    .addOnCompleteListener(task -> {
                        Toast.makeText(getApplicationContext(), "Sign out successfully!", Toast.LENGTH_LONG).show();
                        finish();
                    }));
        }
    }
}
