package com.example.u.periscreener_operater;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

/**
 * Created by U on 12/2/2017.
 */

public class LoginActivity extends Activity {
    private static final String MY_PREFS_NAME = "METADATA_PS_OPERATOR";
    EditText ipAddressEt;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ipAddressEt = (EditText)findViewById(R.id.edittext_ipaddress);

    }

    public void onLoginClick(View view) {
        SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
        editor.putString("ipAddress", ipAddressEt.getText().toString());
        editor.apply();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Choose eye for which test is to be performed.").setCancelable(false)
                .setTitle("Choose Eye")
                .setPositiveButton("Right Eye", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ReportResultClass.setEye("Right");
                        SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                        editor.putString("testForEye", "r");
                        editor.apply();
                        Intent temp = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(temp);
                    }
                }).setNegativeButton("Left Eye", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ReportResultClass.setEye("Left");
                SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                editor.putString("testForEye", "l");
                editor.apply();
                Intent temp = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(temp);
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }
}
