package com.miriasystems.jkauffman.active_pay_android;

import android.app.ActionBar;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.SweepGradient;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;

import java.util.ArrayList;
import java.util.List;

public class ActivePayServerSettingsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private EditText mServerAddress;
    private EditText mServerPort;
    private EditText mServerPath;

    private Switch mSSLSwitch;
    private Switch mDefaultSwitch;

    public static final String PREFS_NAME="MyPrefsFile";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_active_pay_server_settings);

        mServerAddress = (EditText) findViewById(R.id.server_address);
        mServerPath = (EditText) findViewById(R.id.server_path);
        mServerPort = (EditText) findViewById(R.id.server_port);

        mSSLSwitch = (Switch) findViewById(R.id.ssl_switch);
        mDefaultSwitch = (Switch) findViewById(R.id.default_switch);

        loadSavedPreferences();

        Button mSaveButton = (Button) findViewById(R.id.server_save_button);
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                commit();
            }
        } );

        Button mCancelButton = (Button) findViewById(R.id.server_cancel_button);
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        } );

    }

    private void loadSavedPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(this);
        String address = sharedPreferences.getString("storedServerAddress", "");
        String port = sharedPreferences.getString("storedServerPort", "");
        String path = sharedPreferences.getString("storedServicePath", "");
        Boolean useSSL = sharedPreferences.getBoolean("storedUseSSL", true);
        Boolean useDefaults = sharedPreferences.getBoolean("storedUseDefaults", false);
        Log.w("Saved Setting", address);
        mServerAddress.setText(address);
        mServerPort.setText(port);
        mServerPath.setText(path);
        mSSLSwitch.setChecked(useSSL);
        mDefaultSwitch.setChecked(useDefaults);
    }

    private void savePreferences(String key, String value){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    private void savePreferences(String key, Boolean value){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public void commit() {
        savePreferences("storedServerAddress", mServerAddress.getText().toString());
        savePreferences("storedServerPort", mServerPort.getText().toString());
        savePreferences("storedServicePath", mServerPath.getText().toString());
        savePreferences("storedUseSSL", mSSLSwitch.isChecked());
        savePreferences("storedUseDefaults", mDefaultSwitch.isChecked());
        finish();
    }





    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        cursor.moveToFirst();

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public boolean saveServerInformation(){
        return true;
    }

}
