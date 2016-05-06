package com.miriasystems.jkauffman.active_pay_android;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.view.Window;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.miriasystems.jkauffman.active_pay_android.active_pay_invoice_list_items.ActivePayInvoiceHeader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.List;

public class ActivePayInvoiceHeaderActivity extends AppCompatActivity {

    private String authToken;
    private String seqId;
    private String guid;
    private String wob;
    private ArrayList<String> reasonsArrList = new ArrayList<String>();
    private String[] reasonsArr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_pay_invoice_header);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //gets the auth token from login
        Bundle extras = getIntent().getExtras();
        authToken = extras.getString("globalAuthToken");
        seqId = extras.getString("detailSeqId");
        setContentView(R.layout.activity_active_pay_invoice_header);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        DetailPopulateTask mPopTask = new DetailPopulateTask();
        mPopTask.execute(AppDelegate.createRestUrl(ActivePayInvoiceHeaderActivity.this, "item/getitem"));

        ImageButton mRejectNav = (ImageButton) findViewById(R.id.reject_nav_button);
        mRejectNav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navigateToRejection();
            }
        });

        TextView mRejectTitle = (TextView) findViewById((R.id.reject_nav_title));
        mRejectTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navigateToRejection();
            }
        });

        ImageButton mRouteNav = (ImageButton) findViewById(R.id.route_nav_button);
        mRouteNav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navigateToRouting();
            }
        });

        TextView mRouteTitle = (TextView) findViewById(R.id.route_nav_title);
        mRouteTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navigateToRouting();
            }
        });




        ImageButton mImageNav = (ImageButton) findViewById(R.id.image_nav_button);
        mImageNav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navigateToImage();
            }
        });

        ImageButton mLinesNav = (ImageButton) findViewById(R.id.line_details_nav_button);
        mLinesNav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navigateToLines();
            }
        });

        ImageButton mHeaderNav = (ImageButton) findViewById(R.id.header_nav_button);
        mHeaderNav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navigateToHeader();
            }
        });

        Button mLogoutButton = (Button) findViewById(R.id.btn_logout);
        mLogoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppDelegate.logout(ActivePayInvoiceHeaderActivity.this);
            }
        });


        ImageButton mApprovalNav = (ImageButton) findViewById(R.id.approve_nav_button);
       mApprovalNav.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {

               ActivePayApprovalDialog dialog = new ActivePayApprovalDialog(ActivePayInvoiceHeaderActivity.this);
               dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
               dialog.show();

           }
       });

        TextView mApprovalTitle = (TextView) findViewById(R.id.approve_nav_title);
        mApprovalTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ActivePayApprovalDialog dialog = new ActivePayApprovalDialog(ActivePayInvoiceHeaderActivity.this);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                dialog.show();

            }
        });


        if (savedInstanceState == null) {

            Bundle arguments = new Bundle();
            arguments.putString(ActivePayInvoiceListFragment.ARG_ITEM_ID,
                    getIntent().getStringExtra(ActivePayInvoiceListFragment.ARG_ITEM_ID));
            ActivePayInvoiceListFragment fragment = new ActivePayInvoiceListFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.invoice_detail_container, fragment)
                    .commit();
        }
    }

    public void navigateToRejection(){
        Intent intent = new Intent(this, ActivePayRejectionReasonActivity.class);
        intent.putExtra("globalAuthToken", authToken);
        intent.putExtra("detailSeqId", seqId);
        intent.putExtra("detailGuid", guid);
        intent.putExtra("detailWob", wob);
        intent.putExtra("rejectionReasons", reasonsArr);
        startActivity(intent);
    }

    public void navigateToRouting(){
        Intent intent = new Intent(this, ActivePayRouteActivity.class);
        intent.putExtra("globalAuthToken", authToken);
        intent.putExtra("detailSeqId", seqId);
        intent.putExtra("detailGuid", guid);
        intent.putExtra("detailWob", wob);
        intent.putExtra("rejectionReasons", reasonsArr);
        startActivity(intent);
    }

    @Override
    public void onBackPressed(){
        Intent intent = new Intent(this, ActivePayInvoiceListActivity.class);
        intent.putExtra("globalAuthToken", authToken);
        intent.putExtra("detailSeqId", seqId);
        intent.putExtra("detailGuid", guid);
        startActivity(intent);
    }

    public void navigateToImage(){
        findViewById(R.id.image_nav_button).setBackgroundColor(Color.parseColor("#034f84"));
        Intent intent = new Intent(this, ActivePayImageViewActivity.class);
        intent.putExtra("globalAuthToken", authToken);
        intent.putExtra("detailSeqId", seqId);
        intent.putExtra("detailGuid", guid);
        startActivity(intent);
    }

    public void navigateToHeader(){
        findViewById(R.id.header_nav_button).setBackgroundColor(Color.parseColor("#034f84"));
        Intent intent = new Intent(this, ActivePayInvoiceHeaderActivity.class);
        intent.putExtra("globalAuthToken", authToken);
        intent.putExtra("detailSeqId", seqId);
        intent.putExtra("detailGuid", guid);
        startActivity(intent);
    }

    public void navigateToLines(){
        Intent intent = new Intent(this, ActivePayInvoiceDetailLinesActivity.class);
        findViewById(R.id.line_details_nav_button).setBackgroundColor(Color.parseColor("#034f84"));
        intent.putExtra("globalAuthToken", authToken);
        intent.putExtra("detailSeqId", seqId);
        intent.putExtra("detailGuid", guid);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            NavUtils.navigateUpTo(this, new Intent(this, ActivePayInvoiceListActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //async task that populates the table of invoice details
    public class DetailPopulateTask extends AsyncTask<String, Void, Boolean> {

        String content;
        String error;
        ProgressDialog progressDialog = new ProgressDialog(ActivePayInvoiceHeaderActivity.this);


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
                url = new URL(params[0]);
                Log.w("URL", url.toString());

                String urlParameters = "authToken=" + authToken + "&seqId=" + seqId;
                Log.w("BEFORE REPLACE", urlParameters);
                urlParameters = urlParameters.replace("%", "%25");
                urlParameters = urlParameters.replace("%2525", "%25");
                urlParameters = urlParameters.replace("+", "%2B");
                Log.w("AFTER REPLACE", urlParameters);

                byte[] postData = urlParameters.getBytes("UTF-8");
                //int postDataLength = postData.length;
                String postDataString = URLEncoder.encode(urlParameters, "utf-8");
                Log.w("Array", Arrays.toString(postData));
                Log.w("URL Encoder", postDataString);
                String remadeString = new String(postData);
                Log.w("Byte[]", remadeString);

                URLConnection connection = url.openConnection();
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("charset", "utf-8");

                Log.w("CONN", "Opened");
                try {
                    DataOutputStream wr = new DataOutputStream(connection.getOutputStream());

                    for (int i = 0; i < postData.length; i++) {
                        wr.write(postData[i]);
                        Log.w(Integer.toString(i), Byte.toString(postData[i]));
                    }
                    //wr.writeBytes(urlParameters);
                    //Log.w("SIZE", Integer.toString(wr.size()));
                    wr.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line = null;

                while ((line = br.readLine()) != null) {
                    sb.append(line);
                    sb.append(System.getProperty("line.separator"));
                }

                content = sb.toString();
                Log.w("CONTENT", content);

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

            if (content.contains("items"))
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
                try{


                    jsonResponse = new JSONObject(content);
                    JSONArray items = jsonResponse.getJSONArray("items");
                    JSONArray rejectionReasons = jsonResponse.getJSONArray("rejectreasons");

                    Log.w("as json block", jsonResponse.toString());
                    Log.w("as array", items.toString());

                    List<Dictionary<String, String>> invoiceHeader = new ArrayList<Dictionary<String, String>>();
                    TableLayout table = (TableLayout)ActivePayInvoiceHeaderActivity.this.findViewById(R.id.invoice_detail_table);

                    for (int i = 0; i < items.length(); i++){
                        JSONObject detail = new JSONObject(items.get(i).toString());

                        Log.w("label", detail.get("label").toString());
                        Log.w("value", detail.get("value").toString());

                        if (detail.get("label").equals("GUID")) {
                            guid = detail.get("value").toString();
                        }

                        if (detail.get("label").equals("WOB")) {
                            wob = detail.get("value").toString();
                        }

                        if (!detail.get("label").equals("GUID") & !detail.get("label").equals("WOB")) {
                            // Inflate your row "template" and fill out the fields.
                            TableRow row = (TableRow) LayoutInflater.from(ActivePayInvoiceHeaderActivity.this).inflate(R.layout.content_active_pay_invoice_header_row, null);
                            ((TextView) row.findViewById(R.id.attrib_label)).setText(detail.get("label").toString());
                            ((TextView) row.findViewById(R.id.attrib_value)).setText(detail.get("value").toString());
                            table.addView(row);
                        }
                    }



                    for (int i = 0; i < rejectionReasons.length(); i++){
                        JSONObject detail = new JSONObject(rejectionReasons.get(i).toString());

                        Log.w("rejection", detail.get("reason").toString());

                        reasonsArrList.add(detail.get("reason").toString());


                    }

                    reasonsArr = new String[reasonsArrList.size()];
                    reasonsArr = reasonsArrList.toArray(reasonsArr);



                } catch (JSONException e) {
                    e.printStackTrace();
                }


            } else {

            }

            progressDialog.dismiss();
        }

        @Override
        protected void onCancelled() {

        }
    }

    //Dialog class
    public class ActivePayApprovalDialog extends Dialog implements android.view.View.OnClickListener {
        public ActivePayInvoiceHeaderActivity c;
        public Dialog d;
        public Button submit, cancel;
        public String reason;
        public AutoCompleteTextView comments;

        public ActivePayApprovalDialog(ActivePayInvoiceHeaderActivity a) {
            super(a);
            this.c = a;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.resource_active_pay_approval_dialog);
            submit = (Button) findViewById(R.id.btn_approval_submit);
            cancel = (Button) findViewById(R.id.btn_approval_cancel);
            comments = (AutoCompleteTextView) findViewById((R.id.txt_approval_comments));
            submit.setOnClickListener(this);
            cancel.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_approval_submit:

                    //TODO update restUrl logic
                    String restUrl = "http://10.212.212.82:9082/rest/iswebsvc/approveitemservice/approve";

                    ApprovalTask mApprovalTask = new ApprovalTask();
                    mApprovalTask.execute(new String[]{AppDelegate.createRestUrl(ActivePayInvoiceHeaderActivity.this, "approveitemservice/approve"), comments.getText().toString()});

                    Intent intent = new Intent(ActivePayInvoiceHeaderActivity.this, ActivePayInvoiceListActivity.class);
                    intent.putExtra("globalAuthToken", authToken);
                    intent.putExtra("detailSeqId", seqId);
                    startActivity(intent);

                    break;
                case R.id.btn_approval_cancel:
                    dismiss();

                    break;

                default:
                    dismiss();
                    break;

            }


        }
    }

    public class ApprovalTask extends AsyncTask<String, Void, Boolean> {

        String content;
        String error;
        String data;
        Context context;

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
                String urlParameters = "authToken=" + authToken + "&wobnumber=" + wob + "&comments=" + params[1];
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

            //TODO handle error messages
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

            }



        }

        @Override
        protected void onCancelled() {

        }
    }

}
