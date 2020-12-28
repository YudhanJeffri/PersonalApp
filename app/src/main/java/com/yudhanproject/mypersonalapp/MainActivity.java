package com.yudhanproject.mypersonalapp;

import androidx.appcompat.app.AppCompatActivity;

import android.accounts.Account;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
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
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    static final private int RC_SIGN_IN = 1;
    static final private String TAG = "hole";
    private WeakReference<MainActivity> weakAct = new WeakReference<>(this);
    private GoogleSignInClient mGoogleSignInClient;
    private GoogleSignInAccount account;


    SignInButton btn_sign;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_sign = findViewById(R.id.btn_sign);
        Scope myScope = new Scope("https://www.googleapis.com/auth/user.birthday.read");
        Scope myScope2 = new Scope(Scopes.PLUS_ME);
        Scope myScope3 = new Scope(Scopes.PROFILE); //get name and id
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(myScope, myScope2)
                .requestEmail()
                .requestProfile()
                .build();



        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        account = GoogleSignIn.getLastSignedInAccount(this);

        btn_sign.setOnClickListener(v -> {
            if (account == null) {
                reqPerm();
            } else {
                SharedPreferences sharedPref = getSharedPreferences(account.getId(), MODE_PRIVATE);
                if (sharedPref.contains("gender")) {
                    printBasic();
                    printAdvanced();
                } else {
                    new GetProfileDetails(account, weakAct, TAG).execute();

                }
            }
        });
    }

    private void reqPerm() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);

    }

    private void printBasic() {
        account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {



            Log.d(TAG, "latest sign in: "
                    + "\n\tPhoto url:" + account.getPhotoUrl()
                    + "\n\tEmail:" + account.getEmail()
                    + "\n\tDisplay name:" + account.getDisplayName()
                    + "\n\tFamily(last) name:" + account.getFamilyName()
                    + "\n\tGiven(first) name:" + account.getGivenName()
                    + "\n\tId:" + account.getId()
                    + "\n\tIdToken:" + account.getIdToken()

            );
            Intent intent1 = new Intent(MainActivity.this, SecondActivity.class);
            startActivity(intent1);
        } else {
            Log.w(TAG, "basic info is null");
        }
    }

    private void saveAdvanced(Person meProfile) {
        account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            SharedPreferences sharedPref = getSharedPreferences(account.getId(), MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            List<Gender> genders = meProfile.getGenders();
            if (genders != null && genders.size() > 0) {
                String gender = genders.get(0).getValue();
                Log.d(TAG, "onPostExecute gender: " + gender);
                editor.putString("gender", gender);
            } else {
                Log.d(TAG, "onPostExecute no gender if set to private ");
                editor.putString("gender", ""); //save as main key to know pref saved
            }
            List<Birthday> birthdays = meProfile.getBirthdays();
            if (birthdays != null && birthdays.size() > 0) {
                for (Birthday b : birthdays) { //birthday still able to get even private, unlike gender
                    Date bdate = b.getDate();
                    if (bdate != null) {
                        String bday, bmonth, byear;
                        if (bdate.getDay() != null) bday = bdate.getDay().toString();
                        else bday = "";
                        if (bdate.getMonth() != null) bmonth = bdate.getMonth().toString();
                        else bmonth = "";
                        if (bdate.getYear() != null) byear = bdate.getYear().toString();
                        else byear = "";
                        editor.putString("bday", bday);
                        editor.putString("bmonth", bmonth);
                        editor.putString("byear", byear);
                    }
                }
            } else {
                Log.w(TAG, "saveAdvanced no birthday");
            }
            editor.commit();  //next instruction is print from pref, so don't use apply()
        } else {
            Log.w(TAG, "saveAdvanced no acc");
        }
    }

    private void printAdvanced() {
        account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            SharedPreferences sharedPref = getSharedPreferences(account.getId(), MODE_PRIVATE);
            if (sharedPref.contains("gender")) { //this checking works since null still saved
                String gender = sharedPref.getString("gender", "");
                Log.d(TAG, "gender: " + gender);
                if (sharedPref.contains("bday")) { //this checking works since null still saved
                    String bday = sharedPref.getString("bday", "");
                    String bmonth = sharedPref.getString("bmonth", "");
                    String byear = sharedPref.getString("byear", "");
                    Log.d(TAG, bday + "/" + bmonth + "/" + byear);
                } else {
                    Log.w(TAG, "failed ot get birthday from pref");
                }
                String givenName = sharedPref.getString("givenName", "");
                String familyName = sharedPref.getString("familyName", "");
                String id = sharedPref.getString("id", "");
            } else {
                Log.w(TAG, "failed ot get data from pref -2");
            }

        } else {
            Log.w(TAG, "failed ot get data from pref -1");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == Activity.RESULT_OK) {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                handleSignInResult(task);
            } else {
                Log.w(TAG, "failed, user denied OR no network OR jks SHA1 not configure yet at play console android project");
            }
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            // Signed in successfully, show authenticated UI.
            new GetProfileDetails(account, weakAct, TAG).execute();
        } catch (ApiException e) { //cancel choose acc will come here with status code 12501 if not check RESULT_OK
            // , more status code at:
            //https://developers.google.com/android/reference/com/google/android/gms/auth/api/signin/GoogleSignInStatusCodes
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
        }
    }

    static class GetProfileDetails extends AsyncTask<Void, Void, Person> {

        private PeopleService ps;
        private int authError = -1;
        private WeakReference<MainActivity> weakAct;
        private String TAG;

        GetProfileDetails(GoogleSignInAccount account, WeakReference<MainActivity> weakAct, String TAG) {
            this.TAG = TAG;
            this.weakAct = weakAct;
            GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                    this.weakAct.get(), Collections.singleton(Scopes.PROFILE));
            credential.setSelectedAccount(
                    new Account(account.getEmail(), "com.google"));
            HttpTransport HTTP_TRANSPORT = AndroidHttp.newCompatibleTransport();
            JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
            ps = new PeopleService.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                    .setApplicationName("Google Sign In Quickstart")
                    .build();
        }

        @Override
        protected Person doInBackground(Void... params) {
            Person meProfile = null;
            try {
                meProfile = ps
                        .people()
                        .get("people/me")
                        .setPersonFields("names,genders,birthdays")
                        .execute();
            } catch (UserRecoverableAuthIOException e) {
                e.printStackTrace();
                authError = 0;
            } catch (GoogleJsonResponseException e) {
                e.printStackTrace();
                authError = 1;
            } catch (IOException e) {
                e.printStackTrace();
                authError = 2;
            }
            return meProfile;
        }

        @Override
        protected void onPostExecute(Person meProfile) {
            MainActivity mainAct = weakAct.get();
            if (mainAct != null) {
                mainAct.printBasic();
                if (authError == 0) { //app has been revoke, re-authenticated required.
                    mainAct.reqPerm();
                } else if (authError == 1) {
                    Log.w(TAG, "People API might not enable at" +
                            " https://console.developers.google.com/apis/library/people.googleapis.com/?project=<project name>");
                } else if (authError == 2) {
                    Log.w(TAG, "API io error");
                } else {
                    if (meProfile != null) {
                        mainAct.saveAdvanced(meProfile);
                        mainAct.printAdvanced();
                    }
                }
            }
        }
    }
}