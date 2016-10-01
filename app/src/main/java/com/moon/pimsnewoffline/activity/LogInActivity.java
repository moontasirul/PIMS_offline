package com.moon.pimsnewoffline.activity;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;


import com.moon.pimsnewoffline.R;
import com.moon.pimsnewoffline.app.AppConfigURL;
import com.moon.pimsnewoffline.app.AppController;
import com.moon.pimsnewoffline.helper.SQLiteHandler;
import com.moon.pimsnewoffline.helper.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LogInActivity extends AppCompatActivity {


    private EditText inputUserEmail, inputUserPassword;
    private Button loginBtn;
    private ProgressDialog pDialog;
    private SessionManager session;
    private SQLiteHandler db;

    private static final String TAG = LogInActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        inputUserEmail = (EditText) findViewById(R.id.etEmail);
        inputUserPassword = (EditText) findViewById(R.id.etPassword);
        loginBtn = (Button) findViewById(R.id.loginPIMS);

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        // SQLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // Session manager
        session = new SessionManager(getApplicationContext());

        // Check if user is already logged in or not
//        if (session.isLoggedIn()) {
//            // User is already logged in. Take him to main activity
//            Intent intent = new Intent(LogInActivity.this, Main2Activity.class);
//            startActivity(intent);
//            finish();
//        }

        // Login button Click Event
        loginBtn.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                String email = inputUserEmail.getText().toString().trim();
                String password = inputUserPassword.getText().toString().trim();

                // Check for empty data in the form
                if (!email.isEmpty() && !password.isEmpty()) {
                    // login user
                    checkLogin(email, password);
                } else {
                    // Prompt user to enter credentials
                    Toast.makeText(getApplicationContext(),
                            "Please enter the credentials!", Toast.LENGTH_LONG).show();
                }
            }

        });
    }


    /**
     * function to verify login details in mysql db
     * */
    private void checkLogin(final String email, final String password) {
        // Tag used to cancel the request
        String tag_string_req = "req_login";

        pDialog.setMessage("Logging in ...");
        showDialog();

        StringRequest strReq = new StringRequest(Method.POST,
                AppConfigURL.URL_LOGIN, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Login Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    Log.d(TAG, "Json_object check when log_in: " + jObj.toString());
                    boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    if (!error) {
                        // user successfully logged in
                        // Create login session
                        session.setLogin(true);

                        // Now store the user in SQLite


                        JSONObject user = jObj.getJSONObject("user");

                        String uid = user.getString("uid");
                        String name = user.getString("name");
                        String email = user.getString("email");
                        String created_at = user.getString("id");

                        // Inserting row in users table
                        db.addUser(name, email, uid, created_at);

                        // Launch main activity
                        Intent intent = new Intent(LogInActivity.this,  Main2Activity.class);

                        //intent.putExtra("uid",uid);
                        //intent.putExtra("id",created_at);
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
                    Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Login Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("email", email);
                params.put("password", password);

                Log.d(TAG, "Map_object check when log_in: " + params.toString());
                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}
