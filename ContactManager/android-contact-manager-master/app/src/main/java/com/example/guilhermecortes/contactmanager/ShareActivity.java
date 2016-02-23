package com.example.guilhermecortes.contactmanager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by John on 2/16/2016.
 */
public class ShareActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Get Intent
        Intent intent = getIntent();
        String type = intent.getType();
        String action = intent.getAction();

        Log.i("TAGSSSSS", intent.getStringExtra("Contact_Name"));

        //Whether Intent action is ACTION_SEND
        if(Intent.ACTION_SEND.equals(action) && type != null){
            //Whether Intent type is 'text/plain'
            if(type.equals("text/plain")){

                //Get message from Intent
                String name = intent.getStringExtra("Contact_Name");
                String address = intent.getStringExtra("Contact_Address");
                String number = intent.getStringExtra("Contact_Number");
                //Display message
                TextView textView = (TextView) findViewById(R.id.contactName);
                textView.setText(name);

            }
        }
    }
}
