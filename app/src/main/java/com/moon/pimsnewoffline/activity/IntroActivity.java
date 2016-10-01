package com.moon.pimsnewoffline.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.moon.pimsnewoffline.R;
import com.moon.pimsnewoffline.helper.SQLiteHandler;
import com.moon.pimsnewoffline.helper.SessionManager;

public class IntroActivity extends AppCompatActivity {

    private ImageButton imgReg, imgLog;
    private SQLiteHandler db;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        // SqLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // session manager
        session = new SessionManager(getApplicationContext());


////                // Check if user is already logged in or not
//                if (session.isLoggedIn()) {
//                    Intent intent = new Intent(IntroActivity.this, Main2Activity.class);
//                    startActivity(intent);
//                    finish();
//                }


        imgLog= (ImageButton)findViewById(R.id.getLogIn);
        imgLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (session.isLoggedIn()) {
                    Intent intent = new Intent(IntroActivity.this, Main2Activity.class);
                    startActivity(intent);
                    finish();
                }else{
                    startActivity(new Intent(IntroActivity.this,LogInActivity.class));
                }

            }
        });

    }
}
