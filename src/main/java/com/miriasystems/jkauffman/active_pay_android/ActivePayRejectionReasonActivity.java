package com.miriasystems.jkauffman.active_pay_android;

import android.app.Dialog;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
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

public class ActivePayRejectionReasonActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    //variables
    private String authToken;
    private String seqId;
    private String wob;
    private String[] reasonsArr = {};
    private boolean successfulRejection = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        //get variables from Bundle
        Bundle extras = getIntent().getExtras();
        authToken = extras.getString("globalAuthToken");
        seqId = extras.getString("detailSeqId");
        reasonsArr = extras.getStringArray("rejectionReasons");
        wob = extras.getString("detailWob");

        setContentView(R.layout.activity_active_pay_rejection_reason);

        Button mLogoutButton = (Button) findViewById(R.id.btn_logout);
        mLogoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppDelegate.logout(ActivePayRejectionReasonActivity.this);
            }
        });



        View recyclerView = findViewById(R.id.rejectionreasons_table);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);

    }


    //set up list view
    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        if(successfulRejection == false) {
            recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(reasonsArr));
        }
    }

    //Adapter class
    public class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        //list of invoices
        private final String[] mValues;

        //initializer
        public SimpleItemRecyclerViewAdapter(String[] items) {
            mValues = items;
        }

        @Override
        public int getItemCount() {
            return mValues.length;
        }

        //
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.content_active_pay_rejection_reason_row, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.mItem = mValues[position];
            holder.mReasonView.setText(mValues[position]);

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    ActivePayRejectionReasonDialog dialog = new ActivePayRejectionReasonDialog(ActivePayRejectionReasonActivity.this, holder, holder.mItem);

                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                    dialog.show();
                    holder.mReasonView.setBackgroundColor(Color.parseColor("#4fa5d5"));

                }
            });
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final TextView mReasonView;
            public String mItem;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mReasonView = (TextView) view.findViewById(R.id.rejectionreasonrow);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mReasonView.getText() + "'";
            }
        }

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


    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class RejectionTask extends AsyncTask<String, Void, Boolean> {

        String content;
        String error;
        String data;
        Context context;

        public RejectionTask(Context context) {
            this.context = context;
        }

        @Override
        public void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Boolean doInBackground(String... params) {
            BufferedReader br = null;
            URL url;

            try {
                url = new URL(params[0]);
                String urlParameters = "authToken=" + authToken + "&wobnumber=" + wob + "&code" + params[1] + "&comments" + params[2];
                //replace characters
                urlParameters = urlParameters.replace("%", "%25");
                urlParameters = urlParameters.replace("%2525", "%25");
                urlParameters = urlParameters.replace("+", "%2B");

                System.out.println("Urlparameters = " + urlParameters);

                byte[] postData = urlParameters.getBytes("UTF-8");

                URLConnection connection = url.openConnection();
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("charset", "utf-8");

                System.out.println("Connection opened");
                //write to connection
                try {
                    DataOutputStream wr = new DataOutputStream(connection.getOutputStream());

                    for (int i = 0; i < postData.length; i++) {
                        wr.write(postData[i]);
                        Log.w(Integer.toString(i), Byte.toString(postData[i]));
                    }
                    wr.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //read response
                br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line = null;

                while ((line = br.readLine()) != null) {
                    sb.append(line);
                    sb.append(System.getProperty("line.separator"));
                }
                content = sb.toString();
                System.out.println("Content is : " + content);
            } catch (MalformedURLException e) {
                error = e.getMessage();
                e.printStackTrace();
            } catch (IOException e) {
                error = e.getMessage();
                e.printStackTrace();
            } finally {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }

            if (content.contains("authToken"))
                return true;
            else
                return false;

        }


        @Override
        protected void onPostExecute(Boolean success) {

            JSONObject jsonResponse;

            if (success) {
                //if recieved json file contains an AuthNum display ActivePayInvoiceListActivity
                String authToken = "";
                try {
                    jsonResponse = new JSONObject(content);



                } catch (JSONException e) {
                    e.printStackTrace();
                }


            } else {

                successfulRejection = false;

            }

            //display the list
            View recyclerView = findViewById(R.id.invoice_list);
            assert recyclerView != null;
            setupRecyclerView((RecyclerView) recyclerView);
        }

        @Override
        protected void onCancelled() {

        }
    }

    //Dialog class
    public class ActivePayRejectionReasonDialog extends Dialog implements android.view.View.OnClickListener {
        public ActivePayRejectionReasonActivity c;
        public Dialog d;
        public Button submit, cancel;
        public SimpleItemRecyclerViewAdapter.ViewHolder holder;
        public String reason;
        public AutoCompleteTextView comments;

        public ActivePayRejectionReasonDialog(ActivePayRejectionReasonActivity a, SimpleItemRecyclerViewAdapter.ViewHolder h, String r) {
            super(a);
            this.c = a;
            this.holder = h;
            this.reason = r;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.resource_active_pay_rejection_reason_dialog);
            submit = (Button) findViewById(R.id.btn_rejection_submit);
            cancel = (Button) findViewById(R.id.btn_rejection_cancel);
            comments = (AutoCompleteTextView) findViewById((R.id.txt_rejection_comments));
            submit.setOnClickListener(this);
            cancel.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_rejection_submit:

                    holder.mReasonView.setBackgroundColor(Color.parseColor("#ffffff"));
                    successfulRejection = true;
                    RejectionTask mRejectionTask = new RejectionTask(ActivePayRejectionReasonActivity.this);
                    mRejectionTask.execute(new String[]{AppDelegate.createRestUrl(ActivePayRejectionReasonActivity.this, "rejectitemservice/reject"), reason, comments.getText().toString()});

                    Intent intent = new Intent(ActivePayRejectionReasonActivity.this, ActivePayInvoiceListActivity.class);
                    intent.putExtra("globalAuthToken", authToken);
                    intent.putExtra("detailSeqId", seqId);
                    startActivity(intent);

                    System.out.println("CASE 1");

                    break;
                case R.id.btn_rejection_cancel:
                    dismiss();
                    holder.mReasonView.setBackgroundColor(Color.parseColor("#ffffff"));
                    System.out.println("CASE 2");

                    break;

                default:
                    dismiss();
                    holder.mReasonView.setBackgroundColor(Color.parseColor("#ffffff"));
                    System.out.println("CASE 3");

                    break;

            }


        }
    }


}