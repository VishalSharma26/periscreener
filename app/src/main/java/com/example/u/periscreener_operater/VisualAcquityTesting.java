package com.example.u.periscreener_operater;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.gson.Gson;

/**
 * Created by U on 12/2/2017.
 */

public class VisualAcquityTesting extends Activity {
    /*
  *
  *               --------------------- WHY THIS ACTIVITY? ------------------------
  *               To be able to show some points on patient's screen and know that he can actaully understand what is being shown
  *               here is what it should do
  *
  *               + or - button will increae the font size and save info in shared pref  - whenever font is changed show on text view
  *               brightness will change screen backlit brightness
  *               contrast will change color from FFFFFF to 000000
  *               stop will kill the activity
  *               next will get new point with latest changes in effect
  * */
    private int server_port = 8080;
    private String client_message = null;
    private String ip_address;   // to be colected in run time
    int randomNum;
    // eye for which test is performed
    // change this variable is test if performed for left eye
    private char testForEye = 'r';

    TextView fontSizeTv,textTv;
    Button incBtn,decBtn;
    SeekBar brightnessSeekbar,contrastSeekbar;
    private  int defaultFontSize = 12;
    private static final String MY_PREFS_NAME = "METADATA_PS_OPERATOR";
    private TextView textView_brightness,textView_contrast;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visual_acquity_testing);

        // storing info about eye for whihc test is to be performed
        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        String restoredText = prefs.getString("testForEye", null);
        // storing this info in char

        // Error in new app because of no default share prefference value
        try {
            testForEye = restoredText.charAt(0);
        }catch (Exception e){
            testForEye = 'r';
        }


        // region initializing UI elements
        fontSizeTv = (TextView) findViewById(R.id.fontSizeTv);
        textView_brightness = (TextView) findViewById(R.id.tv_brightness_id);
        textView_contrast = (TextView) findViewById(R.id.tv_contrast_id);
        textTv = (TextView) findViewById(R.id.textTv);
        incBtn = (Button) findViewById(R.id.incrementBtn);
        decBtn = (Button) findViewById(R.id.decrementBtn);
        brightnessSeekbar = (SeekBar) findViewById(R.id.seekbar_brightness);
        contrastSeekbar = (SeekBar) findViewById(R.id.seekbar_contrast);
        // endregion


        // restoring ip address from shared pref
        ip_address = prefs.getString("ipAddress",ip_address);
        Log.d("logtag6","Ip address : "+ip_address);


        // initializing look of activity
        // set default values in brightness and contrast seekbar
        SharedPreferences preferences = getSharedPreferences(MY_PREFS_NAME,MODE_PRIVATE);
        int brightnessVal = preferences.getInt("brightness",50);
        brightnessSeekbar.setProgress(brightnessVal);
        textView_brightness.setText("Brightness:  "+brightnessVal);

        int contrastVal = preferences.getInt("contrast",50);
        contrastSeekbar.setProgress(contrastVal);
        textView_contrast.setText("Contrast:  "+contrastVal);

        // note font size doesn't matter and it'll always start from 12
        brightnessSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                // saving data in shared preferences upon any change
                // also displaying text at same time
                SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME,MODE_PRIVATE).edit();
                editor.putInt("brightness",brightnessSeekbar.getProgress());
                editor.commit();

                // changing brightness textview
                textView_brightness.setText("Brightness:  "+brightnessSeekbar.getProgress());

                // making changes visiable in testing screen
                Gson gson = new Gson();
                int[] val = new int[4];
                val[0] = randomNum;     // text to show
                val[1] = defaultFontSize;    // font size
                val[2] = brightnessSeekbar.getProgress();    // brightness
                val[3]  = mapping(contrastSeekbar.getProgress());    // contrast (0 - 100 ==> 27 - 100 )
                String tempStr = gson.toJson(val);
                String  message_to_client = "VACT"+tempStr;
                MainActivity.ServerCommTask new_server_task = new MainActivity.ServerCommTask(ip_address, server_port, message_to_client);
                new_server_task.execute();


            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {


            }
        });

        contrastSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME,MODE_PRIVATE).edit();
                editor.putInt("contrast",contrastSeekbar.getProgress());
                editor.commit();

                // changing Contrast textview
                textView_contrast.setText("Contrast:  "+contrastSeekbar.getProgress());

                // data saved in prefs
                Gson gson = new Gson();
                int[] val = new int[4];
                val[0] = randomNum;     // text to show
                val[1] = defaultFontSize;    // font size
                val[2] = brightnessSeekbar.getProgress();    // brightness
                val[3]  = mapping(contrastSeekbar.getProgress());    // contrast (0 - 100 ==> 27 - 100 )
                String tempStr = gson.toJson(val);
                String  message_to_client = "VACT"+tempStr;
                MainActivity.ServerCommTask new_server_task = new MainActivity.ServerCommTask(ip_address, server_port, message_to_client);
                new_server_task.execute();
                // immediate visible changes on patient screen


            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        //
        incBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(defaultFontSize<76)
                    defaultFontSize++;

                // save this in shared pref
                SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                editor.putInt("fontSize", defaultFontSize);
                editor.apply();
                fontSizeTv.setText("Font Size: "+defaultFontSize);


                textTv.setTextSize(3*defaultFontSize);
                // making changes visiable in testing screen
                Gson gson = new Gson();
                int[] val = new int[4];
                val[0] = randomNum;     // text to show
                val[1] = defaultFontSize;    // font size
                val[2] = brightnessSeekbar.getProgress();    // brightness
                val[3]  = mapping(contrastSeekbar.getProgress());    // contrast (0 - 100 ==> 27 - 100 )
                String tempStr = gson.toJson(val);
                String  message_to_client = "VACT"+tempStr;
                MainActivity.ServerCommTask new_server_task = new MainActivity.ServerCommTask(ip_address, server_port, message_to_client);
                new_server_task.execute();
            }
        });


        decBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(defaultFontSize>1)
                    defaultFontSize--;

                // save this in shared pref
                SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                editor.putInt("fontSize", defaultFontSize);
                editor.apply();
                fontSizeTv.setText("Font Size: "+defaultFontSize);


                textTv.setTextSize(3*defaultFontSize);
                // making changes visiable in testing screen
                Gson gson = new Gson();
                int[] val = new int[4];
                val[0] = randomNum;     // text to show
                val[1] = defaultFontSize;    // font size
                val[2] = brightnessSeekbar.getProgress();    // brightness
                val[3]  = mapping(contrastSeekbar.getProgress());    // contrast (0 - 100 ==> 27 - 100 )
                String tempStr = gson.toJson(val);
                String  message_to_client = "VACT"+tempStr;
                MainActivity.ServerCommTask new_server_task = new MainActivity.ServerCommTask(ip_address, server_port, message_to_client);
                new_server_task.execute();
            }
        });

    }


    private int mapping(int progress) {
        // mapping ( 1 - 100   to   27 - 100)
        float value;
        value = 26 + (progress * 74.0f)/100.0f;
        return (int)value;

    }

    public void onNextClick(View view){
        /*
        *       Show random text on TV of specific size and brightness and contrast
        *
        * */
        int minimum = 0;
        int maximum = 9;
        randomNum = minimum + (int)(Math.random() * maximum);

        Log.d("logTag","random No - "+randomNum);
        textTv.setTextSize(3*defaultFontSize);
//        textTv.setTextColor();

        textTv.setText(randomNum+"");
        Log.d("logtag","Brightness : "+brightnessSeekbar.getProgress()+"");
        Log.d("logtag","Contrast : "+contrastSeekbar.getProgress()+"");
        // sending data to testing app

        Gson gson = new Gson();
        int[] val = new int[4];
        val[0] = randomNum;     // text to show
        val[1] = defaultFontSize;    // font size
        val[2] = brightnessSeekbar.getProgress();    // brightness
        val[3]  = mapping(contrastSeekbar.getProgress());    // contrast (0 - 100 ==> 27 - 100 )
        String tempStr = gson.toJson(val);
        String  message_to_client = "VACT"+tempStr;
        MainActivity.ServerCommTask new_server_task = new MainActivity.ServerCommTask(ip_address, server_port, message_to_client);
        new_server_task.execute();

    }

    public void onStopClick(View view) {
        String  message_to_client = "STOP";
        MainActivity.ServerCommTask new_server_task = new MainActivity.ServerCommTask(ip_address, server_port, message_to_client);
        new_server_task.execute();
        textTv.setText("");

    }
}
