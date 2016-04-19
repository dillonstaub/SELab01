package com.example.guilhermecortes.contactmanager;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import junit.framework.Assert;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class MainActivity extends Activity {

    private EditText nameTxt, phoneTxt, emailTxt, addressTxt;
    ImageView contactImageImgView;
    List<Contact> Contacts = new ArrayList<Contact>();
    ListView contactListView;
    Uri imageURI = null;

    private static final String encryptionKey = "00112233445566778899AABBCCDDEEFF";
    private static final String authCode = "a4027119f0ef686219b636be44a9f415fc61339c3f09162ca6c4c0f0cc40687a";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        pref_thing = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        editor_thing = pref_thing.edit();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        nameTxt = (EditText) findViewById(R.id.txtName);
        phoneTxt = (EditText) findViewById(R.id.txtPhone);
        emailTxt = (EditText) findViewById(R.id.txtEmail);
        addressTxt = (EditText) findViewById(R.id.txtAddress);
        contactListView = (ListView) findViewById(R.id.listView);
        contactImageImgView = (ImageView) findViewById(R.id.imgViewContactImage);

        TabHost tabHost = (TabHost) findViewById(R.id.tabHost);

        tabHost.setup();

        TabHost.TabSpec tabSpec = tabHost.newTabSpec("creator");
        tabSpec.setContent(R.id.tabCreator);
        tabSpec.setIndicator("Creator");
        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec("list");
        tabSpec.setContent(R.id.tabContactList);
        tabSpec.setIndicator("List");
        tabHost.addTab(tabSpec);

        /*
        Listener for the button that hides the privacy settings dialog when the user is done with
        it. Calls populateList() to update the app's state with the new settings.
        */
        final Button hideBtn = (Button) findViewById(R.id.hideButton);
        hideBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                populateList();
                View v = findViewById(R.id.privacy_options);
                v.setVisibility(View.GONE);
                v.setClickable(false);
            }
        });

        //Set the state of the checkboxes to what is indicated in the preferences.
        CheckBox checkbox1 = (CheckBox)findViewById(R.id.checkBox);
        CheckBox checkbox2 = (CheckBox)findViewById(R.id.checkBox2);
        CheckBox checkbox3 = (CheckBox)findViewById(R.id.checkBox3);

        checkbox1.setChecked(pref_thing.getBoolean("phone",false));
        checkbox2.setChecked(pref_thing.getBoolean("email",false));
        checkbox3.setChecked(pref_thing.getBoolean("address",false));


        final Button addBtn = (Button) findViewById(R.id.btnAdd);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Contacts.add(new Contact(Encryptor.encrypt(nameTxt.getText().toString(),encryptionKey), Encryptor.encrypt(phoneTxt.getText().toString(),encryptionKey), Encryptor.encrypt(emailTxt.getText().toString(), encryptionKey), /*Encryptor.encrypt(*/addressTxt.getText().toString()/*,encryptionKey)*/, imageURI));
                populateList();
                Toast.makeText(getApplicationContext(), nameTxt.getText().toString() +  " has been added to your Contacts!", Toast.LENGTH_SHORT).show();
            }
        });

        nameTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            //habilitar o botao se o valor do campo for diferente de vazio
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                addBtn.setEnabled(!nameTxt.getText().toString().trim().isEmpty()); //trim para "cortar os espaços em branco"
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        contactImageImgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Contact Image"), 1);
            }
        });


        // Try to get the data if we were opened by MapView, then set that info as defaults
        try {
            Intent intent = getIntent();

            String mvName = intent.getStringExtra("Contact_Name");
            mvName = Encryptor.decrypt(mvName, encryptionKey);
            String mvAddress = intent.getStringExtra("Contact_Address");
            mvAddress = Encryptor.decrypt(mvAddress, encryptionKey);
            String mvNumber = intent.getStringExtra("Contact_Number");
            mvNumber = Encryptor.decrypt(mvNumber, encryptionKey);

            // Check auth code
            String providedAuthCode = intent.getStringExtra("Auth_Mac");
            //String newAuthCode = AESHelper.buildMac(encryptionKey, mvName, mvAddress, mvNumber);
            Log.i("decrypting: ", mvName + "\n" + mvAddress + "\n" + mvNumber + "\n" + encryptionKey);
            Log.i("decrypting: ", encryptionKey);
            //Log.i("auth codes: ", providedAuthCode + "\n" + newAuthCode);
            if (/*!providedAuthCode.equals(newAuthCode)*/ !Encryptor.macIsValid(providedAuthCode, encryptionKey, mvName, mvAddress, mvNumber)) {
                throw new Exception("Auth codes didn't match.");
            }

            if (mvName != null && mvName != "") {
                nameTxt.setText(mvName);
            }
            if (mvAddress != null && mvAddress != "") {
                addressTxt.setText(mvAddress);
            }
            if (mvNumber != null && mvNumber != "") {
                phoneTxt.setText(mvNumber);
            }
        } catch (Exception e) {
            Log.i("onCreate", "failure decrypting data: " + e.getMessage());
        }
    }
/*
    private static String decryptData(final String encryptedData)
            throws Exception {
        Assert.assertNotNull(encryptedData, "encryptedData is required");

        // Get the key
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        sr.setSeed(encryptionKey.getBytes());
        kgen.init(128, sr); // 192 and 256 bits may not be available
        SecretKey skey = kgen.generateKey();
        byte[] rawKey = skey.getEncoded();

        // Covert from hex
        int len = encryptedData.length() / 2;
        byte[] result = new byte[len];
        for (int i = 0; i < len; i++)
            result[i] = Integer.valueOf(
                    encryptedData.substring(2 * i, 2 * i + 2), 16).byteValue();

        // Do the decryption
        SecretKeySpec skeySpec = new SecretKeySpec(rawKey, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);
        byte[] decrypted = cipher.doFinal(result);

        return new String(decrypted);
    }*/

    public void onActivityResult(int reqCode, int resCode, Intent data){
        if (resCode == RESULT_OK){
            if (reqCode == 1){
                imageURI = data.getData();
                contactImageImgView.setImageURI(data.getData());
            }
        }
    }

    private void populateList(){
        ArrayAdapter<Contact> adapter = new ContactListAdapter();
        contactListView.setAdapter(adapter);
    }

    //add contact
//    private void addContact(String name, String phone, String email, String address){
//        Contacts.add(new Contact(name, phone, email, address));
//    }

    private class ContactListAdapter extends ArrayAdapter<Contact>{
        public ContactListAdapter(){
            super(MainActivity.this, R.layout.listview_item, Contacts);
        }

        //criar função para retornar o emelento do array
        @Override
        public View getView(int position, View view, ViewGroup parent){
            if (view == null) {
                view = getLayoutInflater().inflate(R.layout.listview_item, parent, false);
            }

            Contact currentContact = Contacts.get(position);


            TextView name = (TextView) view.findViewById(R.id.contactName);
            ///////////////////////////////////////////////////////////////////////////////////////////////////
            TextView phone = (TextView) view.findViewById(R.id.phoneNumber);

            TextView email = (TextView) view.findViewById(R.id.emailAddress);
            TextView address = null;
            //SharedPreferences pref_thing = getSharedPreferences("MyPref",MODE_PRIVATE);
            boolean use_hyperlinks = (pref_thing.getBoolean("address",false));
            if(use_hyperlinks) {
                Toast.makeText(this.getContext(), "use hyperlinks status: true", Toast.LENGTH_LONG).show();
            } else if(!use_hyperlinks){
                Toast.makeText(this.getContext(), "use hyperlinks status: false", Toast.LENGTH_LONG).show();
            }

            try {



                String mv_name = "";
             //   try {
                    //mv_name = Encryptor.encrypt(currentContact.get_name(), encryptionKey);
               // } catch (Exception e1){
                    mv_name = currentContact.get_name();
              //  }
                String mv_phone = "";
              //  try {
                    //mv_phone = Encryptor.encrypt(currentContact.get_phone(), encryptionKey);
              //  } catch( Exception e2){
                    mv_phone = currentContact.get_phone();
               // }
                String mv_email ="";
               // try{
                    //mv_email = Encryptor.encrypt(currentContact.get_email(), encryptionKey);
               // } catch (Exception e3){
                    mv_email = currentContact.get_email();
               // }

                String mv_address = "";
                if(use_hyperlinks) {
                    address = (TextView) view.findViewById(R.id.cAddress);
                   // try {
                        //mv_address = Html.fromHtml("<a href=\"http://www.google.com\">" + Encryptor.encrypt(currentContact.get_address(), encryptionKey) + "</a> ").toString();
                  //  } catch (Exception e4) {
                        mv_address = currentContact.get_address();

                   // }

                } else {
                    address = (TextView) view.findViewById(R.id.cAddress);
                  //  try {
                        //mv_address = Encryptor.encrypt(currentContact.get_address(), encryptionKey);
                  //  } catch (Exception e4){
                        mv_address = currentContact.get_address();
                   // }
                }
                String encryptedAuthCode = Encryptor.buildMac(encryptionKey, mv_name, mv_address, mv_phone);



                // Check auth code
                Log.i("decrypting: ", mv_name + "\n" + mv_address + "\n" + mv_phone + "\n" + encryptionKey);
                Log.i("decrypting: ", encryptionKey);
                //Log.i("auth codes: ", providedAuthCode + "\n" + newAuthCode);
                if (/*!providedAuthCode.equals(newAuthCode)*/ !Encryptor.macIsValid(encryptedAuthCode, encryptionKey, mv_name, mv_address, mv_phone)) {
                    throw new Exception("Auth codes didn't match.");
                }

                if (mv_name != null && mv_name != "") {
                    name.setText(Encryptor.decrypt(mv_name,encryptionKey));
                }
                if (mv_email != null && mv_email != "") {
                    email.setText(Encryptor.decrypt(mv_email,encryptionKey));
                }
                if (mv_phone != null && mv_phone != "") {
                    phone.setText(Encryptor.decrypt(mv_phone, encryptionKey));
                }
                if(mv_address != null && mv_address != ""){
                    address = (TextView) view.findViewById(R.id.cAddress);
                    if(use_hyperlinks) {
                        address.setMovementMethod(LinkMovementMethod.getInstance());
                        address.setText(Html.fromHtml("<a href=\"http://www.google.com\"> " + /*Encryptor.decrypt(*/mv_address/*, encryptionKey)*/ + " </a> ").toString());
                        address.setMovementMethod(LinkMovementMethod.getInstance());
                    } else {
                        address.setText(/*Encryptor.decrypt(*/mv_address/*, encryptionKey)*/);
                    }
                    //mv_address = Html.fromHtml("<a href=\"http://www.google.com\">" + currentContact.get_address() + "</a> ").toString();
                }




            } catch (Exception e) {
                Log.i("getView", "failure decrypting data: " + e.getMessage());
                e.printStackTrace();
            }


            ImageView ivContactImage = (ImageView) view.findViewById(R.id.ivContactImage);
            ivContactImage.setImageURI(currentContact.get_imageURI());

            return view;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
            //int x = 9 / 0;
           // this.fragment
            //this.startActivity(new Intent(this, SettingsActivity.class));
            //setContentView(R.layout.privacy_settings_view);
            View v = findViewById(R.id.privacy_options);
            v.setVisibility(View.VISIBLE);
            v.setClickable(true);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public void boxToggled(View v){
        CheckBox checkBox = (CheckBox)v;
        //SharedPreferences pref_thing = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        //SharedPreferences.Editor editor_thing = pref_thing.edit();
//        editor_thing.clear();
//        editor_thing.commit();
        //Toast.makeText(this,"got in", Toast.LENGTH_LONG).show();
        if(v.getId() == R.id.checkBox){
            editor_thing.putBoolean("phone", checkBox.isChecked());
            //Toast.makeText(this,"got in1", Toast.LENGTH_LONG).show();
        }
        if(v.getId() == R.id.checkBox2) {
            editor_thing.putBoolean("email", checkBox.isChecked());
            //Toast.makeText(this,"got in2", Toast.LENGTH_LONG).show();
        }
        if(v.getId() == R.id.checkBox3) {
            editor_thing.putBoolean("address", checkBox.isChecked());
            //Toast.makeText(this,"got in3 " + checkBox.isChecked(), Toast.LENGTH_LONG).show();
        }
        editor_thing.commit();

    }

    public void clickedOnHide(View v){

    }

    private SharedPreferences pref_thing = null;
    private SharedPreferences.Editor editor_thing = null;

}
