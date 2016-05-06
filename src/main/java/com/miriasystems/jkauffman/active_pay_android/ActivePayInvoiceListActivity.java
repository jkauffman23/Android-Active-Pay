package com.miriasystems.jkauffman.active_pay_android;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;




import com.miriasystems.jkauffman.active_pay_android.active_pay_invoice_list_items.ActivePayInvoiceItem;
import com.miriasystems.jkauffman.active_pay_android.active_pay_route_items.ShakeDetector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;


public class ActivePayInvoiceListActivity extends AppCompatActivity{

    //setting authToken and list
    private String authToken;
    private List<ActivePayInvoiceItem> invoiceList = new ArrayList<ActivePayInvoiceItem>();
    // The following are used for the shake detection
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;
    private SwipeRefreshLayout swipeContainer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //spinner
        ProgressDialog progress = new ProgressDialog(this);
        progress.setTitle("Loading");
        progress.setMessage("Wait while loading...");



        super.onCreate(savedInstanceState);
        //gets the auth token from login
        Bundle extras = getIntent().getExtras();
        authToken = extras.getString("globalAuthToken");
        setContentView(R.layout.activity_active_pay_invoice_list);
        //creates header titlebar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        Button mLogoutButton = (Button) findViewById(R.id.btn_logout);
        mLogoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppDelegate.logout(ActivePayInvoiceListActivity.this);
            }
        });

        InvoiceListPopulateTask mPopTask = new InvoiceListPopulateTask();
        mPopTask.execute(AppDelegate.createRestUrl(ActivePayInvoiceListActivity.this,"approvalitem/getapprovalitem"));

        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                invoiceList = new ArrayList<ActivePayInvoiceItem>();
                InvoiceListPopulateTask mPopTask = new InvoiceListPopulateTask();
                mPopTask.execute(AppDelegate.createRestUrl(ActivePayInvoiceListActivity.this, "approvalitem/getapprovalitem"));

            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);


        // ShakeDetector initialization
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeDetector();
        mShakeDetector.setOnShakeListener(new ShakeDetector.OnShakeListener() {

            @Override
            public void onShake(int count) {
				/*
				 * The following method, "handleShakeEvent(count):" is a stub //
				 * method you would use to setup whatever you want done once the
				 * device has been shook.
				 */
                invoiceList = new ArrayList<ActivePayInvoiceItem>();
                InvoiceListPopulateTask mPopTask = new InvoiceListPopulateTask();
                mPopTask.execute(AppDelegate.createRestUrl(ActivePayInvoiceListActivity.this,"approvalitem/getapprovalitem"));
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Add the following line to register the Session Manager Listener onResume
        mSensorManager.registerListener(mShakeDetector, mAccelerometer,	SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void onPause() {
        // Add the following line to unregister the Sensor Manager onPause
        mSensorManager.unregisterListener(mShakeDetector);
        super.onPause();
    }



    @Override
    public void onBackPressed(){
        Intent intent = new Intent(this, ActivePayLoginActivity.class);
        intent.putExtra("globalAuthToken", authToken);
        startActivity(intent);
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(invoiceList));
    }

    //Adapter class
    public class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        //list of invoices
        private final List<ActivePayInvoiceItem> mValues;

        //initializer
        public SimpleItemRecyclerViewAdapter(List<ActivePayInvoiceItem> items) {
            mValues = items;
        }

        //
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.invoice_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.mItem = mValues.get(position);
            holder.mVendorView.setText(mValues.get(position).getVendorName());
            holder.mGrossAmountView.setText(mValues.get(position).getGrossAmount());

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Context context = v.getContext();
                    Intent intent = new Intent(context, ActivePayInvoiceHeaderActivity.class);
                    intent.putExtra("detailSeqId", holder.mItem.getSeqId());
                    intent.putExtra("globalAuthToken", authToken);
                    context.startActivity(intent);

                }
            });
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final TextView mVendorView;
            public final TextView mGrossAmountView;
            public ActivePayInvoiceItem mItem;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mVendorView = (TextView) view.findViewById(R.id.vendorname);
                mGrossAmountView = (TextView) view.findViewById(R.id.grossamount);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mVendorView.getText() + "'";
            }
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class InvoiceListPopulateTask extends AsyncTask<String, Void, Boolean> {

        String content = "";
        String error;
        String data;

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
                String urlParameters = "authToken=" + authToken + "&type=ALL";
                //replace characters
                urlParameters = urlParameters.replace("%", "%25");
                urlParameters = urlParameters.replace("%2525", "%25");
                urlParameters = urlParameters.replace("+", "%2B");

                byte[] postData = urlParameters.getBytes("UTF-8");

                URLConnection connection = url.openConnection();
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("charset", "utf-8");

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
                System.out.println("CONTENT is : " + content);

            } catch (MalformedURLException e) {
                error = e.getMessage();
                e.printStackTrace();
            } catch (IOException e) {
                error = e.getMessage();
                e.printStackTrace();
            }finally {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }

            System.out.println("CONTENT pre crash is : " + content);

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
                try {
                    jsonResponse = new JSONObject(content);
                    JSONArray items = jsonResponse.getJSONArray("items");

                    Log.w("as json block", jsonResponse.toString());
                    Log.w("as array", items.toString());

                    for (int i = 0; i < items.length(); i++) {
                        JSONObject item = items.getJSONObject(i);
                        String vendor = item.getString("vendor Name");
                        String seqId = item.getString("SeqID");
                        String grossAmount = item.getString("Gross Amount");
                        Log.w("vendor", vendor);
                        Log.w("grossamount", grossAmount);
                        Log.w("seqId", seqId);
                        invoiceList.add(new ActivePayInvoiceItem(seqId, vendor, grossAmount));


                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }


            } else {

            }

            //display the list
            View recyclerView = findViewById(R.id.invoice_list);
            assert recyclerView != null;
            setupRecyclerView((RecyclerView) recyclerView);
            swipeContainer.setRefreshing(false);
        }

        @Override
        protected void onCancelled() {

        }
    }
}
