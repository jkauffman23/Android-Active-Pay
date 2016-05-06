package com.miriasystems.jkauffman.active_pay_android;

import android.app.ProgressDialog;
import android.content.Intent;

import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;

import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.AsyncTask;
import android.os.Bundle;

import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Set;


public class ActivePayLoginActivity extends AppCompatActivity {

    //variables
    private UserLoginTask mAuthTask = null;
    private String[] strArrGlobalParams;
    private boolean[] boolArrGlobalParams;
    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;

    //activity initialization
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_active_pay_login);

        //handles background display
        LinearLayout mLayout = (LinearLayout)findViewById(R.id.layout_login);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(getResources(), R.drawable.miria_loginscreen_iphone6_portrait, options);
        BitmapDrawable ob = new BitmapDrawable(getResources(), AppDelegate.decodeSampledBitmapFromResource(getResources(), R.drawable.miria_loginscreen_iphone6_portrait, 300, 300));
        mLayout.setBackground(ob);

        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.txtvw_email);
        String[] usernames=AppDelegate.loadSavedUsernamesAsArray(ActivePayLoginActivity.this);
        ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,usernames);

        mEmailView.setAdapter(adapter);
        mEmailView.setThreshold(1);


        //sets up the password textbox to have similar font to username and to hide password
        mPasswordView = (EditText) findViewById(R.id.txtvw_password);
        mPasswordView.setTypeface(Typeface.DEFAULT);
        mPasswordView.setTransformationMethod(new PasswordTransformationMethod());
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        //buttons
        Button mEmailSignInButton = (Button) findViewById(R.id.btn_email_sign_in);
        Button mServerSettingsButton = (Button) findViewById(R.id.btn_sever_settings);


        //click handlers
        mServerSettingsButton.setOnClickListener(new OnClickListener() {
                                                     @Override
                                                     public void onClick(View view) {
                                                         Intent intent = new Intent(ActivePayLoginActivity.this, ActivePayServerSettingsActivity.class);
                                                         startActivity(intent);
                                                     }
                                                 } );

        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

    }

    @Override
    public void onBackPressed(){

    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (cancel) {

            focusView.requestFocus();
        } else {
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute(AppDelegate.createRestUrl(ActivePayLoginActivity.this, "user/login"));
        }
    }


    //async login operation
    public class UserLoginTask extends AsyncTask<String, Void, Boolean> {

        private String mEmail = "";
        private String mPassword = "";
        String content = "";

        ProgressDialog progressDialog = new ProgressDialog(ActivePayLoginActivity.this);

        //constructor
        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }


        @Override
        public void onPreExecute() {
            super.onPreExecute();

            progressDialog.setTitle("Please wait ...");
            progressDialog.show();

        }

        @Override
        protected Boolean doInBackground(String... params) {

            BufferedReader br = null;
            URL url;

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                //create url
                url = new URL(params[0]);
                //create parameters to pass to service
                String urlParameters = "username="+mEmail+"&password="+mPassword;
                //parameters converted to byte array
                byte[] postData = urlParameters.getBytes("UTF-8");

                //create the url connection to the service
                URLConnection connection = url.openConnection();
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("charset", "utf-8");

                //write parameters to the connection
                try {
                    DataOutputStream wr = new DataOutputStream(connection.getOutputStream());

                    for (int i = 0; i < postData.length; i++) {
                        wr.write(postData[i]);
                    }
                    wr.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //read connection output
                br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line = null;

                while ((line = br.readLine()) != null) {
                    sb.append(line);
                    sb.append(System.getProperty("line.separator"));
                }


                content = sb.toString();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            finally {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }

            try {
                if (content.contains("authToken"))
                    return true;
                else
                    return false;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }


        @Override
        protected void onPostExecute(Boolean success) {
            mAuthTask = null;
            progressDialog.dismiss();

            JSONObject jsonResponse;

            if (success) {
                String authToken = "";
                try{
                    jsonResponse = new JSONObject(content);
                    authToken = (String) jsonResponse.get("authToken");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Set<String> usernames = AppDelegate.loadSavedUsernamesAsSet(ActivePayLoginActivity.this);
                usernames.add(mEmail);

                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ActivePayLoginActivity.this);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putStringSet("storedUsernames", usernames);
                editor.commit();


                Intent intent = new Intent(ActivePayLoginActivity.this, ActivePayInvoiceListActivity.class);
                intent.putExtra("globalAuthToken", authToken);
                progressDialog.dismiss();
                startActivity(intent);

            } else {
                String message = "Timeout Error";
                String code = "";
                try{
                    jsonResponse = new JSONObject(content);
                    message = (String) jsonResponse.get("message");
                    code = (String) jsonResponse.get("code");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                ActivePayErrorMessageDialog dialog = new ActivePayErrorMessageDialog(ActivePayLoginActivity.this, "Active Finance Login",message, code);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                dialog.show();

            }


        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
        }
    }


}

