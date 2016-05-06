package com.miriasystems.jkauffman.active_pay_android;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.miriasystems.jkauffman.active_pay_android.active_pay_detail_lines_items.ActivePayDetailLineItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class ActivePayInvoiceDetailLinesActivity extends AppCompatActivity {

    //setting authToken and list
    private String authToken;
    private String seqId;
    private String guid;
    private List<ActivePayDetailLineItem> invoiceList = new ArrayList<ActivePayDetailLineItem>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //gets the auth token from login
        Bundle extras = getIntent().getExtras();
        authToken = extras.getString("globalAuthToken");
        seqId = extras.getString("detailSeqId");
        guid = extras.getString("detailGuid");
        setContentView(R.layout.activity_active_pay_invoice_detail_lines);
        //population of the list, passes url for testing

        DetailLinesListPopulateTask mDetailPopTask = new DetailLinesListPopulateTask();
        mDetailPopTask.execute(AppDelegate.createRestUrl(ActivePayInvoiceDetailLinesActivity.this, "lineitem/getlineitem"));


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
                AppDelegate.logout(ActivePayInvoiceDetailLinesActivity.this);
            }
        });

    }

    @Override
    public void onBackPressed(){
        Intent intent = new Intent(this, ActivePayInvoiceHeaderActivity.class);
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
        findViewById(R.id.line_details_nav_button).setBackgroundColor(Color.parseColor("#034f84"));
        Intent intent = new Intent(this, ActivePayInvoiceDetailLinesActivity.class);
        intent.putExtra("globalAuthToken", authToken);
        intent.putExtra("detailSeqId", seqId);
        intent.putExtra("detailGuid", guid);
        startActivity(intent);
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(invoiceList));
    }

    //Adapter class
    public class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        //list of invoices
        private final List<ActivePayDetailLineItem> mValues;

        //initializer
        public SimpleItemRecyclerViewAdapter(List<ActivePayDetailLineItem> items) {
            mValues = items;
        }

        //
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.content_active_pay_invoice_detail_lines_row, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.mItem = mValues.get(position);
            holder.mItemDescriptionView.setText(mValues.get(position).getItemDescription());
            holder.mItemCodeView.setText(mValues.get(position).getItemCode());
            holder.mQuantityView.setText(mValues.get(position).getQuantity());
            holder.mLineTotalView.setText(mValues.get(position).getLineTotal());

        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final TextView mItemDescriptionView;
            public final TextView mItemCodeView;
            public final TextView mQuantityView;
            public final TextView mLineTotalView;
            public ActivePayDetailLineItem mItem;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mItemDescriptionView = (TextView) view.findViewById(R.id.itemdescription);
                mItemCodeView = (TextView) view.findViewById(R.id.itemcode);
                mQuantityView = (TextView) view.findViewById(R.id.quantity);
                mLineTotalView = (TextView) view.findViewById(R.id.linetotal);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mItemDescriptionView.getText() + "'";
            }
        }


    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class DetailLinesListPopulateTask extends AsyncTask<String, Void, Boolean> {

        String content;
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
                String urlParameters = "authToken=" + authToken + "&seqId=" + seqId;
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

            if (content.contains("lineitems"))
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
                    JSONArray items = jsonResponse.getJSONArray("lineitems");

                    Log.w("as json block", jsonResponse.toString());
                    Log.w("as array", items.toString());

                    for (int i = 0; i < items.length(); i++) {
                        JSONObject item = items.getJSONObject(i);
                        String lineTotal = item.getString("Line Total");
                        String itemCode = item.getString("Item Code");
                        String quantity = item.getString("Quantity");
                        String itemdDescription = item.getString("Item Description");
                        Log.w("Line Total", lineTotal);
                        Log.w("Item Code", itemCode );
                        Log.w("Quantity", quantity );
                        Log.w("Item Description", itemdDescription );
                        invoiceList.add(new ActivePayDetailLineItem(itemCode, itemdDescription, quantity, lineTotal));

                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }


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
                ActivePayErrorMessageDialog dialog = new ActivePayErrorMessageDialog(ActivePayInvoiceDetailLinesActivity.this, "Active Finance Login",message, code);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                dialog.show();



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
}