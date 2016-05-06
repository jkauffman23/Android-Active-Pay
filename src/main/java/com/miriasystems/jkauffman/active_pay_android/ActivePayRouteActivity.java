package com.miriasystems.jkauffman.active_pay_android;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Adapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Filter;
import android.widget.SearchView;
import android.widget.TextView;

import com.miriasystems.jkauffman.active_pay_android.active_pay_route_items.ActivePayRouteItem;

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


public class ActivePayRouteActivity extends AppCompatActivity {

    //setting authToken and list
    private String authToken;
    private String seqId;
    private List<ActivePayRouteItem> routeUsersList = new ArrayList<ActivePayRouteItem>();
    private RecyclerView mRecyclerView;
    public SearchView search;
    Adapter mAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.out.println("AT ROUTE");
        //spinner
        ProgressDialog progress = new ProgressDialog(this);
        progress.setTitle("Loading");
        progress.setMessage("Wait while loading...");

        super.onCreate(savedInstanceState);
        //gets the auth token from login
        Bundle extras = getIntent().getExtras();
        authToken = extras.getString("globalAuthToken");
        seqId = extras.getString("detailSeqId");
        System.out.println("MAMMOTH : " + seqId);
        setContentView(R.layout.activity_active_pay_route);
        //creates header titlebar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());
        //population of the list, passes url for testing
        String[] strArrGlobalParams = AppDelegate.loadSavedStringPreferences(ActivePayRouteActivity.this);

        Button mLogoutButton = (Button) findViewById(R.id.btn_logout);
        mLogoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppDelegate.logout(ActivePayRouteActivity.this);
            }
        });





        SearchView search = (SearchView) findViewById( R.id.action_search);
        search.setOnQueryTextListener(listener);



        RoutePopulateTask mPopTask = new RoutePopulateTask();
        mPopTask.execute(AppDelegate.createRestUrl(ActivePayRouteActivity.this,"userlist/getuserlist"));


    }

      /* this is the Seerach QuerttextListner.
       this method filter the list data with a matching string,
       hence provides user an easy way to find the information he needs.
        */
    SearchView.OnQueryTextListener listener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextChange(String query) {
            query = query.toLowerCase();

            final List<ActivePayRouteItem> filteredList = new ArrayList<>();

            for (int i = 0; i < routeUsersList.size(); i++) {

                final String text = routeUsersList.get(i).getUsername().toLowerCase();
                if (text.contains(query)) {

                    filteredList.add(routeUsersList.get(i));
                }
            }

            //display the list
            View recyclerView = findViewById(R.id.rcyc_route_table);
            assert recyclerView != null;
            setupRecyclerView((RecyclerView) recyclerView, filteredList);


            return true;
        }
        public boolean onQueryTextSubmit(String query) {
            return false;
        }
    };

    private void setupRecyclerView(@NonNull RecyclerView recyclerView, List<ActivePayRouteItem> userList) {
        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(userList));
    }

    //Adapter class
    public class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        //list of invoices
        private final List<ActivePayRouteItem> mValues;

        //initializer
        public SimpleItemRecyclerViewAdapter(List<ActivePayRouteItem> items) {
            mValues = items;
        }

        //
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.content_active_pay_route_row, parent, false);
            return new ViewHolder(view);
        }



        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            holder.mItem = mValues.get(position);
            if (mValues.get(position).getLastname().length() > 0)
                holder.mFullNameView.setText(mValues.get(position).getFirstname() + ", " + mValues.get(position).getLastname());
            else
                holder.mFullNameView.setText(mValues.get(position).getFirstname());
            holder.mUserNameView.setText(mValues.get(position).getUsername());

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String[] strArrGlobalParams = AppDelegate.loadSavedStringPreferences(ActivePayRouteActivity.this);
                    RouteTask mRouteTask = new RouteTask();
                    holder.mView.setBackgroundColor(Color.parseColor("#4fa5d5"));
                    System.out.println("ROUTE EXECUTION");
                    mRouteTask.execute(AppDelegate.createRestUrl(ActivePayRouteActivity.this, "routeitem/route"), mValues.get(position).getSeqid());


                }
            });
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final TextView mFullNameView;
            public final TextView mUserNameView;
            public ActivePayRouteItem mItem;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mFullNameView = (TextView) view.findViewById(R.id.txt_route_fullname);
                mUserNameView = (TextView) view.findViewById(R.id.txt_route_username);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mFullNameView.getText() + "'";
            }
        }
    }

    //async population of the routing table
    public class RoutePopulateTask extends AsyncTask<String, Void, Boolean> {

        String content;
        String error;
        String data;
        ProgressDialog progressDialog = new ProgressDialog(ActivePayRouteActivity.this);


        @Override
        public void onPreExecute() {
            progressDialog.setMessage("Wait while loading...");
            progressDialog.show();
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            BufferedReader br = null;
            URL url;

            try {
                url = new URL(params[0]);
                System.out.println("Inside Async task : " + params[0]);
                String urlParameters = "authToken=" + authToken;
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
                System.out.println("CONTENT IS : " + content);

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

            if (content.contains("userlist"))
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
                    JSONArray items = jsonResponse.getJSONArray("userlist");

                    Log.w("as json block", jsonResponse.toString());
                    Log.w("as array", items.toString());

                    for (int i = 0; i < items.length(); i++) {
                        JSONObject item = items.getJSONObject(i);
                        String firstname = item.getString("First Name");
                        String lastname = item.getString("Last Name");
                        String username = item.getString("User Name");
                        String seqid = item.getString("User SeqID");
                        System.out.println(firstname + lastname + username + seqid);
                        routeUsersList.add(new ActivePayRouteItem(firstname, lastname, username, seqid));

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }


            } else {

            }

            //display the list
            View recyclerView = findViewById(R.id.rcyc_route_table);
            assert recyclerView != null;
            setupRecyclerView((RecyclerView) recyclerView, routeUsersList);

            progressDialog.dismiss();
        }

        @Override
        protected void onCancelled() {

        }
    }

    //async execution of routing the invoice
    public class RouteTask extends AsyncTask<String, Void, Boolean> {
        ActivePayYesNoDialog yndialog;
        String content = "";
        ProgressDialog progressDialog = new ProgressDialog(ActivePayRouteActivity.this);


        @Override
        public void onPreExecute() {
            yndialog = new ActivePayYesNoDialog(ActivePayRouteActivity.this, "Active Finance Routing", "Are you sure you want to route?", authToken);
            yndialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            yndialog.show();
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            BufferedReader br = null;
            URL url;
            System.out.println("ARE WE EVEN HERE!!!");
            System.out.println("THE BOOLEAN RESULT IS: " + (Boolean) yndialog.getResult());
            try {
                while ((Boolean) yndialog.getResult() == null) {
                    Thread.sleep(100);
                    System.out.println("THE BOOLEAN RESULT IS: " + (Boolean) yndialog.getResult());
                }
            }catch (Exception e) {
                e.printStackTrace();
            }

            if (yndialog.getResult()) {
                try {
                    url = new URL(params[0]);
                    System.out.println("Inside Async task : " + params[0]);
                    String urlParameters = "seqid=" + seqId + "&authToken=" + authToken + "&userid=" + params[1];
                    System.out.println("Parameters: " + urlParameters);
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
                    System.out.println("CONTENT IS : " + content);

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
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
            } else {
                return false;
            }
        }


        @Override
        protected void onPostExecute(Boolean success) {

            JSONObject jsonResponse;



            if(success) {
                Intent intent = new Intent(ActivePayRouteActivity.this, ActivePayInvoiceListActivity.class);
                intent.putExtra("globalAuthToken", authToken);
                startActivity(intent);

            } else {

                if (!content.equals("")) {
                    String message = "Timeout Error";
                    String code = "";
                    try {
                        jsonResponse = new JSONObject(content);
                        message = (String) jsonResponse.get("message");
                        code = (String) jsonResponse.get("code");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    ActivePayErrorMessageDialog dialog = new ActivePayErrorMessageDialog(ActivePayRouteActivity.this, "Active Finance Routing", message, code);
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                    dialog.show();
                }

            }

            //display the list
            View recyclerView = findViewById(R.id.rcyc_route_table);
            assert recyclerView != null;
            setupRecyclerView((RecyclerView) recyclerView, routeUsersList);

            progressDialog.dismiss();

        }

        @Override
        protected void onCancelled() {

        }


    }

}
