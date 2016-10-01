package com.moon.pimsnewoffline.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.moon.pimsnewoffline.R;
import com.moon.pimsnewoffline.app.AppConfigURL;
import com.moon.pimsnewoffline.app.AppController;
import com.moon.pimsnewoffline.helper.SQLiteHandler;
import com.moon.pimsnewoffline.helper.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class Main2Activity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    private TextView txtName;
    private TextView txtEmail;
    private TextView txtuId;
    private TextView txtID;

    private DrawerLayout drawer;

    private ProgressDialog pDialog;
    private Button btnLogout;
    private Button btnDocProfile;
    private Button btnPatientList;
    private Button btnAddPatient;
    private Button btnReports;


    private SQLiteHandler db;
    private SessionManager session;

    public static final int CONNECTION_TIMEOUT = 10000;
    public static final int READ_TIMEOUT = 15000;



    private static final String TAG = Main2Activity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);




        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        txtName = (TextView) findViewById(R.id.UserName);
        txtEmail = (TextView) findViewById(R.id.UserEmail);
        txtuId = (TextView) findViewById(R.id.U_id);
        txtID = (TextView)findViewById(R.id.globalID);

        btnDocProfile=(Button)findViewById(R.id.DocProfile);
        btnPatientList = (Button)findViewById(R.id.btnPatientList);
        btnAddPatient = (Button)findViewById(R.id.btnAddPatient);
        btnReports = (Button)findViewById(R.id.btnReport);
        btnLogout = (Button) findViewById(R.id.PIMS_LogOut);

        // SqLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // session manager
        session = new SessionManager(getApplicationContext());

        if (!session.isLoggedIn()) {
            logoutUser();
        }

        // Fetching user details from sqlite
        HashMap<String, String> user = db.getUserDetails();

        String name = user.get("name");
        String email = user.get("email");
        String UID = user.get("uid");
        String ID = user.get("created_at");


        // Displaying the user details on the screen
        txtName.setText(name);
        txtEmail.setText(email);
        txtuId.setText(UID);
        txtID.setText(ID);

        // Logout button click event
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                // Fetching user details from sqlite
                HashMap<String, String> user = db.getUserDetails();

                final String name = user.get("name");
                final String email = user.get("email");
                final String UID = user.get("uid");
                final String ID = user.get("created_at");
                final String message ="yes";


                Log.d(TAG, "U_id: " + UID.toString() + "id: " + ID.toString()+ "message: " + message.toString());

//                // Fetching user details from LoginActivity
//        Bundle bundle = getIntent().getExtras();
//        String uname = bundle.getString("name");
//        String uemail = bundle.getString("email");
//
//        final String uid = bundle.getString("uid");
//        final String id = bundle.getString("id");
//        final String message ="yes";
               checkLogOut(UID,ID, message);
               // new AsyncFetch().execute(UID, ID, message);
            }
        });


        btnDocProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Main2Activity.this, UserProfile.class));
            }
        });

        btnPatientList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Main2Activity.this, PatientList.class));
            }
        });

        btnAddPatient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Main2Activity.this, AddPatient.class));
            }
        });


        btnReports.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Main2Activity.this, Report.class));
            }
        });


//        // Fetching user details from LoginActivity
//        Bundle bundle = getIntent().getExtras();
//        String uname = bundle.getString("name");
//        String uemail = bundle.getString("email");
//        String uid = bundle.getString("uid");
//        String id = bundle.getString("id");
//
//        // Displaying the user details on the screen
//        txtName.setText(uname);
//        txtEmail.setText(uemail);
//        txtUid.setText(uid);
//        txtid.setText(id);


    }

    /**
     * Logging out the user. Will set isLoggedIn flag to false in shared
     * preferences Clears the user data from sqlite users table
     * */
    private void logoutUser() {
        session.setLogin(false);

        db.deleteUsers();

        // Launching the login activity
        Intent intent = new Intent(Main2Activity.this, LogInActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

            logoutUser();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_Home) {
            // Handle the camera action
        } else if (id == R.id.nav_Profile) {
            startActivity(new Intent(Main2Activity.this, UserProfile.class));

        } else if (id == R.id.nav_AddPatients) {
            startActivity(new Intent(Main2Activity.this, AddPatient.class));
        } else if (id == R.id.nav_PatientList) {
            startActivity(new Intent(Main2Activity.this, PatientList.class));
        } else if (id == R.id.nav_Report) {
            startActivity(new Intent(Main2Activity.this, Report.class));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }





    private void checkLogOut(final String logout_uid , final String logout_id,final String logout) {
        // Tag used to cancel the request
        String tag_string_req = "req_logout";
        Log.d(TAG, "Method U_id: " + logout_uid.toString() + " Method id: " + logout_id.toString()+ " Method message: " + logout.toString());
        pDialog.setMessage("Logging out ...");
        showDialog();

        StringRequest strReqLogout = new StringRequest(Request.Method.POST,
                AppConfigURL.URL_LOGOUT, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Logout Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                        Log.d(TAG, "Json_object check: " + jObj.toString());

                        boolean error = jObj.getBoolean("error");

                        // Check for error node in json
                        if (!error) {

                            session.setLogin(false);

                            db.deleteUsers();

                            JSONObject user = jObj.getJSONObject("user");

                            String logout = user.getString("uid");
                            String id = user.getString("id");

                            Toast.makeText(getApplicationContext(), logout + id, Toast.LENGTH_LONG).show();
                            // Launch main activity
                            Intent intent = new Intent(Main2Activity.this,
                                    LogInActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            // Error in login. Get the error message
                            String errorMsg = jObj.getString("error_msg");
                            Toast.makeText(getApplicationContext(),
                                    errorMsg, Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        // JSON error
                        e.printStackTrace();
                        Log.e(TAG, "Logout Error: " + e.getMessage());
                        Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }





            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Logout Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();

                    params.put("logout",logout);
                    params.put("uid",logout_uid);
                    params.put("id",logout_id);



                Log.d(TAG, "Map_object when logout: " + params.toString());

                return params;

            }

        };

        // Adding request to request queue
      AppController.getInstance().addToRequestQueue(strReqLogout, tag_string_req);




        //Adding the string request to the queue
//        RequestQueue requestQueue = Volley.newRequestQueue(this);
//        requestQueue.add(strReqLogout);
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }


//    private class AsyncFetch extends AsyncTask<String, String, String> {
//        ProgressDialog pdLoading = new ProgressDialog(Main2Activity.this);
//        HttpURLConnection conn;
//        URL url = null;
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//
//            //this method will be running on UI thread
//            pdLoading.setMessage("\tLoading...");
//            pdLoading.setCancelable(false);
//            pdLoading.show();
//
//        }
//
//        @Override
//        protected String doInBackground(String... params) {
//            try {
//
//                // Enter URL address where your json file resides
//                // Even you can make call to php file which returns json data
//                url = new URL("http://www.twistermedia.com/pims/ndroid_com.php");
//
//            } catch (MalformedURLException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//                return e.toString();
//            }
//            try {
//
//                // Setup HttpURLConnection class to send and receive data from php and mysql
//                conn = (HttpURLConnection) url.openConnection();
//                conn.setReadTimeout(READ_TIMEOUT);
//                conn.setConnectTimeout(CONNECTION_TIMEOUT);
//                conn.setRequestMethod("GET");
//
//                // setDoOutput to true as we recieve data from json file
//                conn.setDoOutput(true);
//                conn.setDoInput(true);
//
//                // Append parameters to URL
//                Uri.Builder builder = new Uri.Builder()
//                        .appendQueryParameter("uid", params[0])
//                        .appendQueryParameter("id", params[1])
//                        .appendQueryParameter("logout", params[2]);
//                String query = builder.build().getEncodedQuery();
//
//                // Open connection for sending data
//                OutputStream os = conn.getOutputStream();
//                BufferedWriter writer = new BufferedWriter( new OutputStreamWriter(os, "UTF-8"));
//                writer.write(query);
//                writer.flush();
//                writer.close();
//                os.close();
//                conn.connect();
//
//
//            } catch (IOException e1) {
//                // TODO Auto-generated catch block
//                e1.printStackTrace();
//                return e1.toString();
//            }
//
//            try {
//
//                int response_code = conn.getResponseCode();
//
//                // Check if successful connection made
//                if (response_code == HttpURLConnection.HTTP_OK) {
//
//                    // Read data sent from server
//                    InputStream input = conn.getInputStream();
//                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
//                    StringBuilder result = new StringBuilder();
//                    String line;
//
//                    while ((line = reader.readLine()) != null) {
//                        result.append(line);
//                    }
//
//                    // Pass data to onPostExecute method
//                    return (result.toString());
//
//                } else {
//
//                    return ("unsuccessful");
//                }
//
//            } catch (IOException e) {
//                e.printStackTrace();
//                return e.toString();
//            } finally {
//                conn.disconnect();
//            }
//
//
//        }
//
//        @Override
//        protected void onPostExecute(String result) {
//
//            //this method will be running on UI thread
//
//            pdLoading.dismiss();
//
//            try {
//                JSONObject jObj = new JSONObject(result);
//                boolean error = jObj.getBoolean("error");
//
//                // Check for error node in json
//                if (!error) {
//                    // user successfully logged in
//                    // Destroy login session
//                    session.setLogin(false);
//
//                    db.deleteUsers();
//
////                    JSONObject user = jObj.getJSONObject("user");
////
////                    String logout = user.getString("uid");
////                    String id = user.getString("id");
////
////                    Toast.makeText(getApplicationContext(), logout + id, Toast.LENGTH_LONG).show();
//                    // Launch main activity
//                    Intent intent = new Intent(Main2Activity.this, LogInActivity.class);
//                    startActivity(intent);
//                    finish();
//                } else {
//                    // Error in login. Get the error message
//                    String errorMsg = jObj.getString("error_msg");
//                    Toast.makeText(getApplicationContext(),
//                            errorMsg, Toast.LENGTH_LONG).show();
//                }
//            } catch (JSONException e) {
//                // JSON error
//                e.printStackTrace();
//                Log.e(TAG, "Logout Error: " + e.getMessage());
//                Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
//            }
//
//        }
//
//    }









}
