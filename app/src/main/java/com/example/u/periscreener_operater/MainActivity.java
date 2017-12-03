package com.example.u.periscreener_operater;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.ToneGenerator;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class MainActivity extends AppCompatActivity {


    // variables for screen graphics
    private static final int IMAGE_VIEW_WIDTH = 1200;
    private static final int IMAGE_VIEW_LENGTH = 1200;

    // variables for run 1
    private static final int BLIND_SPOT_STIMULI_RUN1 = 5;   // they should be evenly disstributed
    private static final int FALSE_POS_RUN1 = 5;            // they should be evenly distributed
    private static final int TOTAL_POINTS_RUN1 = 54 + BLIND_SPOT_STIMULI_RUN1+FALSE_POS_RUN1;
        /*
    Total points run 1 will always include 54 normal stimuli and remaining blind spot and false positive
        */

    // variables for run2
    private static final int BLIND_SPOT_STIMULI_RUN2 = 5;   // they should be evenly disstributed
    private static final int FALSE_POS_RUN2 = 5;            // they should be evenly distributed
    private static final int TOTAL_POINTS_RUN2 = 54 + BLIND_SPOT_STIMULI_RUN2+FALSE_POS_RUN2;


    // endregion

    // region - Constants that can be changed during test

    private static  int COLOR_BACKGROUND = Color.parseColor("#000000");
    private static  int COLOR_STIMULI = Color.parseColor("#FFFFFF");       // this will depend upon brightness setting
    private static  int COLOR_FIXATION = 34;
    private int RADIUS_STIMULI = 20;
    private int RADIUS_FIXATION = 30;


    // endregion

    ServerSocket serverSocket;

    private int server_port = 8080;
    private String client_message = null;
    private String ip_address;    // to be colected in run time

    private Socket connection_socket;
    private String message_to_client = null;

    private SeekBar brightness_selector = null;
    private SeekBar contrast_selector = null;
    private int seekbar_brightness_progress = 0;
    private int seekbar_contrast_progress = 0;

    private IntentFilter mIntentFilter;

    int CLICK_LENGTH = 0;
    int CLICK_MAX = 5;


//    private int test_points = 54;
//    private int blind_spot_points = 10;
    // fixme add false positive points also

    int[] pointSequence = new int[66];
    boolean[] notClickForRun_1 = new boolean[55];
    boolean[] notClickForRun_2 = new boolean[55];
    boolean[] notClickForRun_3 = new boolean[55];
    boolean[] notClickAtAll = new boolean[55];
// having 64+1 because test points will start from 1 and NOT 0

    private Button start_button = null;
    private Button pause_button = null;
    private Button restart_button  = null;
    private TextView textView_fixationLoss;
    private TextView textView_falsepositive;
    private ImageView feedbackImageView;
    int report_rectangle_width = 50;
    Canvas reportCanvas;
    Paint stdPaint = new Paint();
    Bitmap stdBmp,copy_stdBmp;
    Canvas stdCanvas,circleCanvas,rectCanvas;

    int testPhase = 0;
    boolean is_shuffle = true;
    boolean is_first_start = true;
    int fixationLoss_shown = 0 , fixationLoss_click = 0;
    int falseNegative_shown = 0 ,falseNegative_click = 0;

    // above variable keeps a track - in which phase test is currently in


//    private ArrayList<Integer> test_point_list = new ArrayList<>();
    // instead of test point list, using array

    //region Setting up the Broadcast Receiver
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String intentAction = intent.getAction();
            if (!Intent.ACTION_MEDIA_BUTTON.equals(intentAction)) {
                return;
            }
            KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            if (event == null) {
                return;
            }
            int action = event.getAction();

            if (action == KeyEvent.ACTION_DOWN) {

            }
            abortBroadcast();

        }
    };
    //endregion

    //endregion

    // variables for getting point coordinates
    private static final int screenHalfLength = 600;
    double eyeToScreenDistance;
    int loc_3_degree;
    int loc_9_degree;
    int loc_15_degree;
    int loc_21_degree;
    int loc_27_degree;
    int loc_10_degree;
    int loc_20_degree;
    int loc_30_degree;
    private static  int blindSpot_x = 281;
    private static  int blindSpot_y = 30;
    private static final int maxScreenSize = 600;
    private static final double bsCenter_degree = 13.5;

    // eye for which test is performed
    // change this variable is test if performed for left eye
    private char testForEye = 'r';

    // variables that define test parameters
    int stimuliPresentationTime = 200;
    int waitingTime = 2700;
    boolean is_testPaused = false;
    boolean is_visual_possible = true;

    int blink_point_i;
    boolean click = false;
    // experimental
    private static final int POINTS_IN_RUN_1 = 54+5+5;
    private static final int POINTS_IN_RUN_2 = 54+5+5;
    int run_points;
    int currentRun = 1;

    int missPoint_count =0;
    int[] missPoint_arr = new int[55];
    int[] pointCoor;

    int[][] LocationVal;
    Handler experimentHandler = new Handler();
    long endTime = 0;
    long startTime = System.currentTimeMillis();
    private static final String MY_PREFS_NAME = "METADATA_PS_OPERATOR";
    boolean is_run3Started = false;
    boolean is_test_running = false;

    int[] coordinate = new int[2];
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

//        Log.d("logtag6","Starting test for : "+testForEye);
        // restoring ip address from shared pref


        ip_address = prefs.getString("ipAddress",ip_address);
        Log.d("logtag6","Ip address : "+ip_address);

        //region Initialising the test control buttons
        start_button = (Button) findViewById(R.id.button_startTest);
        pause_button = (Button) findViewById(R.id.button_stopTest);
        restart_button = (Button) findViewById(R.id.button_restartTest);
        textView_fixationLoss = (TextView) findViewById(R.id.textview_fixationLoss);
        textView_falsepositive = (TextView) findViewById(R.id.textview_falsePositive);

        // fixme : Temperory setup to open Report Activity
        TextView text_parameters = (TextView)findViewById(R.id.textview_testParameters);
        text_parameters.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                startActivity(new Intent(MainActivity.this,ReportActivity.class));
                return true;
            }
        });

        // initially pause and restart are disabled
        pause_button.setEnabled(false);
        restart_button.setEnabled(false);

        is_visual_possible = true;

        feedbackImageView = (ImageView) findViewById(R.id.testingActImageView);
        stdPaint.setColor(Color.BLACK);
        stdPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        stdBmp = Bitmap.createBitmap(1200,1200, Bitmap.Config.ARGB_8888);
        stdCanvas = new Canvas(stdBmp);

        // initializing variables
        eyeToScreenDistance = blindSpot_x/(Math.tan(Math.toRadians(13.5)));
        loc_3_degree = (int) (eyeToScreenDistance*Math.tan(Math.toRadians(3)));
        loc_9_degree = (int) (eyeToScreenDistance*Math.tan(Math.toRadians(9)));
        loc_15_degree = (int) (eyeToScreenDistance*Math.tan(Math.toRadians(15)));
        loc_21_degree = (int) (eyeToScreenDistance*Math.tan(Math.toRadians(21)));
        loc_27_degree = (int) (eyeToScreenDistance*Math.tan(Math.toRadians(27)));
        loc_10_degree = (int) (eyeToScreenDistance*Math.tan(Math.toRadians(10)));
        loc_20_degree = (int) (eyeToScreenDistance*Math.tan(Math.toRadians(20)));
        loc_30_degree = (int) (eyeToScreenDistance*Math.tan(Math.toRadians(30)));


        for(int i=0;i<55;i++){
            notClickForRun_1[i] = false;
            notClickForRun_2[i] = false;
            notClickForRun_3[i] = false;
            notClickAtAll[i] = false;
        }

        //endregion


        ReportResultClass.setResult(notClickAtAll);

        //region Function for controlling the functioning of the seek-bars

        //region Brightness seek-bar controller
//        brightness_selector = (SeekBar) findViewById(R.id.seekbar_brightness);
//        brightness_selector.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                seekbar_brightness_progress = progress;
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//
//            }
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//                Log.i("custom", seekbar_brightness_progress + "");
//                sendSeekbarValue(seekbar_brightness_progress);
//            }
//        });
        //endregion

        //region Contrast seek-bar controller
//        contrast_selector = (SeekBar) findViewById(R.id.seekbar_contrast);
//        contrast_selector.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                seekbar_contrast_progress = progress;
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//
//            }
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//                Log.i("custom", seekbar_contrast_progress + "");
//                sendSeekbarValue(seekbar_contrast_progress);
//            }
//        });
        //endregion

        //endregion

        //region Function for starting up the socket server
//        Thread server_thread = new Thread(new ServerThread());
//        server_thread.start();
        //endregion

        //region Setting up the intent filter for handling the game-pad buttons
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(Intent.ACTION_MEDIA_BUTTON);
        mIntentFilter.setPriority(999999999);

        registerReceiver(mReceiver, mIntentFilter);
        //endregion


        // region Initializing 'look' of image view

        // initializing look of image view

        /*
        *
        *       Algo :
        *
        *           1 - Show axis
        *           2 - Make cuts for angle - 10 | 20 | 30 - resized as per screen
        *           3 - show solid small dots are corresponding point locations
        *           4 - Show blind spot
        *           5 - Set bitmap on image view
        *
        * */

        // region Step 1|2 : Making rectangles and cuts on axis

        int width = 4;

        float left = 600-width/2;
//        int top = 600-report_rectangle_width/4*3;
        float top = 0;
//        float bottom = top + report_rectangle_width*3/2;
        float right = left+width;
        float bottom  = 1200;

        stdCanvas.drawRect(left,top,right,bottom,stdPaint);
        left = 0;
        top = 600-width/2;
        right = 1200;
        bottom = top+width;
        stdCanvas.drawRect(left,top,right,bottom,stdPaint);

        // making cut for different degrees - 10 | 20 | 30
        // scaling down by factor of 0.85
//        loc_10_degree = (int) (.85*loc_10_degree);
//        loc_20_degree = (int) (.85*loc_20_degree);
//        loc_30_degree = (int) (.85*loc_30_degree);
        int distancePoint_10degree = (int) 183.33;
        int distancePoint_20degree = (int) (183.33*2);
        int distancePoint_30degree = (int) (183.33*3);
//        Log.d("LOG_TAG","Value of location = "+loc_10_degree+" | "+loc_20_degree+" | "+loc_30_degree);

        left = 600+distancePoint_10degree-width;
        top = 600-3*width;
        right = left+width;
        bottom = top+6*width;
        stdCanvas.drawRect(left,top,right,bottom,stdPaint);

        left = 600+distancePoint_20degree-width;
        top = 600-3*width;
        right = left+width;
        bottom = top+6*width;
        stdCanvas.drawRect(left,top,right,bottom,stdPaint);

        left = 600+distancePoint_30degree-width;
        top = 600-3*width;
        right = left+width;
        bottom = top+6*width;
        stdCanvas.drawRect(left,top,right,bottom,stdPaint);

        left = 600-distancePoint_10degree-width;
        top = 600-3*width;
        right = left+width;
        bottom = top+6*width;
        stdCanvas.drawRect(left,top,right,bottom,stdPaint);


        left = 600-distancePoint_20degree-width;
        top = 600-3*width;
        right = left+width;
        bottom = top+6*width;
        stdCanvas.drawRect(left,top,right,bottom,stdPaint);


        left = 600-distancePoint_30degree-width;
        top = 600-3*width;
        right = left+width;
        bottom = top+6*width;
        stdCanvas.drawRect(left,top,right,bottom,stdPaint);


        // endregion


        // region Step 3 :  Solid small dots
        loc_3_degree = 55;
        loc_9_degree = 165;
        loc_15_degree = 275;
        loc_21_degree = 385;
        loc_27_degree = 495;


        for(int i = 1;i<=54;i++){

            int coor[] = getCoordinates(i,testForEye,true);
            stdCanvas.drawCircle(coor[0]+600,coor[1]+600,4,stdPaint);

        }


        // endregion

        // region Step 4 : Blind Spot

        int loc_12_degree = (int) 18.33*12;
        int loc_15_degree = (int) 18.33*15;
        int loc_5_degree = (int) 18.33*5;
        int loc_1_5_degree = (int) ((int) 18.33*1.5);


        // making triangle
        stdPaint.setStrokeWidth(5);
        if(testForEye == 'r') {
            stdCanvas.drawLine(600 + loc_12_degree, 600 + loc_1_5_degree, 600 + loc_15_degree, 600 + loc_1_5_degree, stdPaint);
            stdCanvas.drawLine(600 + loc_12_degree, 600 + loc_1_5_degree, 600 + loc_12_degree + 27, 600 + loc_5_degree, stdPaint);
            stdCanvas.drawLine(600 + loc_15_degree, 600 + loc_1_5_degree, 600 + loc_12_degree + 27, 600 + loc_5_degree, stdPaint);
        } else{
            stdCanvas.drawLine(600 - loc_12_degree, 600 + loc_1_5_degree, 600 - loc_15_degree, 600 + loc_1_5_degree, stdPaint);
            stdCanvas.drawLine(600 - loc_12_degree, 600 + loc_1_5_degree, 600 - loc_12_degree - 27, 600 + loc_5_degree, stdPaint);
            stdCanvas.drawLine(600 - loc_15_degree, 600 + loc_1_5_degree, 600 - loc_12_degree - 27, 600 + loc_5_degree, stdPaint);

        }
        // endregion



        feedbackImageView.setImageBitmap(stdBmp);

        copy_stdBmp = stdBmp.copy(stdBmp.getConfig(),true);
        blink_point_i = -1;


    }

    //region Functions for controlling the test on the device
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)

    public void startClientTest(View view) {
        is_test_running = true;  // to accept click only when test is running (To avoid crash)
        is_visual_possible = false;  // visual acquity test is not possible while test is running
        // IF test is pause send resume command  ELSE send the cordinates of 64 points
        if(is_testPaused){



            message_to_client = "Resume";

            ServerCommTask new_server_task = new ServerCommTask(ip_address, server_port, message_to_client);
            new_server_task.execute();
        }
        else {
            if (is_first_start)  // if is_first_start test if the test is started for first time else it is assumed that test was paused earlier
            {


                // this is case when test is started WITHOUT ANY PAUSE
                is_first_start = false;
                is_shuffle = true;
                blink_point_i = -1;
                // copy_stdBmp is initialized earlier and copies stdBmp layout
                stdBmp = copy_stdBmp.copy(copy_stdBmp.getConfig(), true);      // Restore stdBmp

                fixationLoss_click = 0;
                fixationLoss_shown = 0;
                falseNegative_click = 0;
                falseNegative_shown = 0;
                currentRun = 1;

                missPoint_count = 0;

                // generate test points when test is started and send the points to client
                if (is_shuffle) {
                    generate_TestPoints();
                    is_shuffle = false;
                }

                // only one time
                String sampleDataToClient;
                Gson gson = new Gson();
                int[][] val = new int[pointSequence.length][2];
                // re-initializing location proing
                loc_3_degree = (int) (eyeToScreenDistance * Math.tan(Math.toRadians(3)));
                loc_9_degree = (int) (eyeToScreenDistance * Math.tan(Math.toRadians(9)));
                loc_15_degree = (int) (eyeToScreenDistance * Math.tan(Math.toRadians(15)));
                loc_21_degree = (int) (eyeToScreenDistance * Math.tan(Math.toRadians(21)));
                loc_27_degree = (int) (eyeToScreenDistance * Math.tan(Math.toRadians(27)));
                loc_10_degree = (int) (eyeToScreenDistance * Math.tan(Math.toRadians(10)));
                loc_20_degree = (int) (eyeToScreenDistance * Math.tan(Math.toRadians(20)));
                loc_30_degree = (int) (eyeToScreenDistance * Math.tan(Math.toRadians(30)));

                //  Printing point sequence
                for (int p = 0; p < pointSequence.length; p++) {
                    Log.d("logtag7", pointSequence[p] + "");
                }

                for (int i = 0; i < pointSequence.length; i++) {
                    // for every point in point sequence give corresponding coordinates // whihc are not changed
                    int[] pointCoor = getCoordinates(pointSequence[i], testForEye,false);
                    val[i][0] = pointCoor[0] + 600;
                    val[i][1] = pointCoor[1] + 600;

                }
                sampleDataToClient = gson.toJson(val);

                // changing variable values
                loc_3_degree = 55;
                loc_9_degree = 165;
                loc_15_degree = 275;
                loc_21_degree = 385;
                loc_27_degree = 495;

                // sending data about when to start test
                long timeNow = System.currentTimeMillis();
                long startTime = timeNow + 2000;         // giving sufficient time at which patient's app should start test
                endTime = startTime;
                message_to_client = "Data" + startTime;
                Log.d("logtag", "Sequence generated : " + Arrays.deepToString(val));

                ServerCommTask new_server_task = new ServerCommTask(ip_address, server_port, message_to_client);
                new_server_task.execute();

                new_server_task = new ServerCommTask(ip_address, server_port, sampleDataToClient);
                new_server_task.execute();

                // sending eye ( right or left )
                char[] eye = new char[1];
                eye[0] = testForEye;     // text to show
                String tempStr = gson.toJson(eye);
                message_to_client = "eye"+tempStr;
                new_server_task = new ServerCommTask(ip_address, server_port, message_to_client);
                new_server_task.execute();

            } else {
                // case when once test was paused and then started again

                // sending info to un-pause test
                message_to_client = "unpause";
                ServerCommTask new_server_task = new ServerCommTask(ip_address, server_port, message_to_client);
                new_server_task.execute();

                // sending data about when to start test
                long timeNow = System.currentTimeMillis();
                long startTime = timeNow + 2000;         // giving sufficient time at which patient's app should start test
                endTime = startTime;
                message_to_client = "Data" + startTime;

                new_server_task = new ServerCommTask(ip_address, server_port, message_to_client);
                new_server_task.execute();

            }
        }
        onStartClintTestButton();

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void onStartClintTestButton() {


        // disabling start button and enabling others
        start_button.setEnabled(false);
        pause_button.setEnabled(true);
        restart_button.setEnabled(true);

        // this function will simultaneously send data to client for starting test and will start test on operator's app also



        start_button.setBackgroundResource(R.color.colorButtonSecondary);
        pause_button.setBackgroundResource(R.color.colorButtonPrimary);

        // starting test on operator's app

        is_testPaused = false;

        final Thread thread = new Thread(){


            @Override
            public void run() {

                while (!is_testPaused){

                    // region ALGO
                    // when test is running
                        /*
                        *       Algorithm - RUN 1
                        *
                        *           1- Get shuffled array to be shown
                        *               - if point is normal point - get coordinates
                         *              - BS = get coordinates
                         *              - else make a minute dot at center (invisible)
                         *          2- show big white dot at the point that is being shown
                         *              - to make a temporary image copy the original canvas
                         *              - add a circle on copied canvas
                         *              - after some time delete the copy
                         *              - show back the previous (original) canvas
                         *          3- Listen for click
                         *              - Click Received - Run 1 TO BE MODIFIED APPROPRIATELY FOR RUN 2 | 3
                          *                 - If point is normal - make ring wrt center
                          *                 - if point is BS - Fixation Loss ++
                          *                 - if point is false +ve - FP++
                         *              - Not clicked
                         *                  - Normal - leave the point
                         *                  - BS - 0/0-> 0/1
                         *                  - F+ve - 0/0 -> 0/1
                         *
                         *          Finally - when all 74 points are shown for run 1 - start run 2
                        *
                        * */

                    // endregion

                    if(currentRun==1){
                        run_points = POINTS_IN_RUN_1;
                    }
                    else if(currentRun==2){
                        run_points = POINTS_IN_RUN_2;
                    }
                    else if(currentRun==3){

                        // Following points are not properly defined - points that are missed must be randomised
                        run_points = missPoint_count;
//                        for (int p =0;p<missPoint_count;p++){
//                            pointSequence[p] = missPoint_arr[p];    // for run 3 copying points in pointSequence
//                        }
                        // shuffling point sequence
                        if(!is_run3Started){
                            // copying points in point sequence
                            for (int p =0;p<missPoint_count;p++){
                                pointSequence[p] = missPoint_arr[p];    // for run 3 copying points in pointSequence
                            }

                            int[] tempArr = new int[missPoint_count];

                            for(int p =0;p<missPoint_count;p++){
                                tempArr[p] = missPoint_arr[p];
                                Log.d("logtag6","tempArr = "+Arrays.toString(tempArr));
                            }
                            shuffleArray(tempArr);
                            // this means run 3 is starting for first time - Shuffling array to show
//                            Log.d("logtag6","Temp Arr = "+Arrays.toString(tempArr));
                            //copying values into point sequence
                            for(int p = 0;p<missPoint_count;p++){
                                pointSequence[p] = tempArr[p];
                                is_run3Started = true;
                            }


                            int[][] val =new  int[missPoint_count][2];
                            for(int p = 0;p<missPoint_count;p++){
                                int[] coors = getCoordinates(pointSequence[p],testForEye,false);
                                val[p][0] = coors[0]+600;
                                val[p][1] = coors[1]+600;
                            }
                            Gson gson = new Gson();
                            String sampleDataToClient  = gson.toJson(val);
                            message_to_client = "RUN3"+sampleDataToClient;

                            ServerCommTask new_server_task = new ServerCommTask(ip_address, server_port, message_to_client);
                            new_server_task.execute();



                        }

                    }

                    if(blink_point_i<=run_points){

                        blink_point_i++;// Blink Point is initialised with -1

                    }

//                    if(currentRun==3 || pointSequence[blink_point_i]!=0)    // DOUBT why is it checking if current run is three?
                    // skipping center point (0) points  -  VERIFIED
//                    if(pointSequence[blink_point_i]!=0)
                    if(true)
                    {
//                        if(currentRun==3){
//                            pointCoor = getCoordinates(missPoint_arr[blink_point_i],testForEye);
//                        }else {
                        pointCoor = getCoordinates(pointSequence[blink_point_i], testForEye,true);
//                        }
                        pointCoor[0] += 600;
                        pointCoor[1] += 600;
                        // making temporary bmp

                        final Bitmap tempBmp = stdBmp.copy(stdBmp.getConfig(), true);
                        final Canvas tempCanvas = new Canvas(tempBmp);
                        circleCanvas = new Canvas(stdBmp);
                        rectCanvas = new Canvas(stdBmp);
                        tempCanvas.drawCircle(pointCoor[0], pointCoor[1], 30, stdPaint);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (!is_testPaused) {
                                    Log.d("LOG_TAG2","Showing Point : "+blink_point_i);
                                    feedbackImageView.setImageBitmap(tempBmp);
                                }
                                return;
                            }
                        });
//                            feedbackImageView.setImageBitmap(stdBmp);
//                        Log.d("Log_tag", "above_1000");

                        endTime = endTime+waitingTime;

                        while (System.currentTimeMillis()<endTime){
                            // wait

                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (!is_testPaused) {
                                    feedbackImageView.setImageBitmap(stdBmp);
                                }
                                return;
                            }
                        });
//                        Log.d("Log_tag", "above_200");
                        endTime+=stimuliPresentationTime;
                        while (System.currentTimeMillis()<endTime){
                            // wait
                        }
//                        try {
//                            Thread.sleep(200);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                            feedbackImageView.setImageBitmap(stdBmp);


                        if (pointSequence[blink_point_i] > 54 && pointSequence[blink_point_i] < 60) {
                            fixationLoss_shown++;
                        }
                        if (pointSequence[blink_point_i] > 59) {
                            falseNegative_shown++;
                        }

                        if (click) {
                            stdPaint.setStyle(Paint.Style.STROKE);
//                                stdPaint.setStrokeWidth(5);
                            if(currentRun==1) {
                                circleCanvas.drawCircle(pointCoor[0], pointCoor[1], 15, stdPaint);
                            }else if(currentRun==2){
                                circleCanvas.drawRect(pointCoor[0]-20,pointCoor[1]-20,pointCoor[0]+20,pointCoor[1]+20,stdPaint);
                            } else{
                                circleCanvas.drawCircle(pointCoor[0], pointCoor[1], 15, stdPaint);
                                circleCanvas.drawRect(pointCoor[0]-20,pointCoor[1]-20,pointCoor[0]+20,pointCoor[1]+20,stdPaint);
                            }
                            stdPaint.setStyle(Paint.Style.FILL_AND_STROKE);
                            click = false;


                            if (pointSequence[blink_point_i] > 54 && pointSequence[blink_point_i] < 60) {
                                fixationLoss_click++;
                            }
                            if (pointSequence[blink_point_i] > 59) {
                                falseNegative_click++;
                            }

                        } else {
                            if(currentRun==1 && pointSequence[blink_point_i]<55){
                                notClickForRun_1[pointSequence[blink_point_i]] = true;
                            }else if(currentRun==2 && pointSequence[blink_point_i]<55){
                                notClickForRun_2[pointSequence[blink_point_i]] = true;
                            }else if(currentRun==3 && pointSequence[blink_point_i]<55){
                                notClickForRun_3[pointSequence[blink_point_i]] = true;
                            }
                            click = false;
                        }


                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                textView_fixationLoss.setText("Fixation Loss: " + fixationLoss_click + "/" + fixationLoss_shown  + " Total Points:" + blink_point_i);
                                textView_falsepositive.setText("False Positive: " + falseNegative_click + "/" + falseNegative_shown + " | Run " + currentRun + " | Disparity "+ missPoint_count);

                            }
                        });

                    }
                    else {Log.d("logtag","Point Skipped : "+blink_point_i);}


                    if(blink_point_i == 65 && currentRun==1){
                        currentRun++;
                        blink_point_i=-1;        // confirm it - TESTED
                    }else if(blink_point_i==65 && currentRun==2){
                        currentRun++;
                        blink_point_i=-1;
                        for(int c=0;c<55;c++){
                            if((notClickForRun_1[c] && !notClickForRun_2[c]) || (!notClickForRun_1[c] && notClickForRun_2[c])){
                                missPoint_arr[missPoint_count] = c;
                                missPoint_count++;
                            }

                        }
                    }else if(blink_point_i==missPoint_count && currentRun==3){

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(),"Test Completed",Toast.LENGTH_LONG).show();
                                start_button.setEnabled(true);
                                pause_button.setEnabled(false);
                                restart_button.setEnabled(false);
                                start_button.setBackgroundResource(R.color.colorButtonPrimary);


                                Log.d("logTag","run1 points :" +Arrays.toString(notClickForRun_1));
                                Log.d("logTag","run2 points :" +Arrays.toString(notClickForRun_2));
                                Log.d("logTag","run3 points :" +Arrays.toString(notClickForRun_3));

                                for(int i =0;i<55;i++){
                                    if((notClickForRun_1[i] && notClickForRun_2[i])
                                            || (notClickForRun_1[i] && notClickForRun_3[i])
                                            || (notClickForRun_2[i] && notClickForRun_3[i])){
                                        notClickAtAll[i] = true;
                                    }
                                }

                                ReportResultClass.setFalseNegative(missPoint_count);
                                ReportResultClass.setResult(notClickAtAll);
                                ReportResultClass.setFalseNegative(falseNegative_click);
                                ReportResultClass.setFixationLoss(fixationLoss_click);

                                // start new activity - Generate Report
                                Intent temp = new Intent(MainActivity.this, ReportActivity.class);
                                startActivity(temp);


                            }
                        });
                        // disabling buttons
                        // Note : After test completion all the parameters should be reseted
                        break;
                    }

                    if(CLICK_LENGTH<CLICK_MAX){
                        CLICK_LENGTH = 0;
                    }else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                Toast.makeText(MainActivity.this, "Bluetooth Button is is kept on Long Press", Toast.LENGTH_SHORT).show();
                                onPauseButtonClick();
                            }
                        });

                        CLICK_LENGTH = 0;
                    }
                }
                return;
            }

        };
        while (System.currentTimeMillis()<startTime){
            // do nothing  - WAIT
        }
        thread.start();


    }


    public void pauseClientTest(View view) {
        onPauseButtonClick();
    }

    private void onPauseButtonClick() {
        message_to_client = "Pause";
        // test is paused, enable start button and disbale pause button
        start_button.setEnabled(true);
        pause_button.setEnabled(false);

        start_button.setBackgroundResource(R.color.colorButtonPrimary);
        pause_button.setBackgroundResource(R.color.colorButtonSecondary);

        ServerCommTask new_server_task = new ServerCommTask(ip_address, server_port, message_to_client);
        new_server_task.execute();
        // pausing test
        is_testPaused = true;
        is_visual_possible = true;
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)

    public void restartClientTest(View view){

        // temporary function
        start_button.setBackgroundResource(R.color.colorButtonPrimary);
        pause_button.setBackgroundResource(R.color.colorButtonPrimary);

        if(is_testPaused) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Are you sure you want to restart?").setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            // user is sure to restart

                            message_to_client = "Restart";
                            ServerCommTask new_server_task = new ServerCommTask(ip_address, server_port, message_to_client);
                            new_server_task.execute();
                            // KILLING ACTIVITY
                            finish();
                        }
                    }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    // cancel
                }
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();


        } else{
            Snackbar.make(view,"Please Pause your test to RESTART",Snackbar.LENGTH_SHORT).setAction("Action",null).show();
        }
    }


    // TODO : ISSUES WITH FOLLWOING FUNCTION IS THAT UPON RESTART PREVIOUS RUNNABLES ARE ALSO RUNNING AT SAME TIME

    public void restartClientTest_old(View view) {

        is_visual_possible= false;

        // finish activity in restart
//        generate_TestPoints();

//        message_to_client += ": " + pointSequence.toString();
//        Log.i("custom", message_to_client);

        start_button.setBackgroundResource(R.color.colorButtonPrimary);
        pause_button.setBackgroundResource(R.color.colorButtonPrimary);

        if(is_testPaused) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Are you sure you want to restart?").setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            message_to_client = "Restart";
                            ServerCommTask new_server_task = new ServerCommTask(ip_address, server_port, message_to_client);
                            new_server_task.execute();

                            generate_TestPoints();
                            is_shuffle = true;
                            blink_point_i = -1;
                            stdBmp = copy_stdBmp.copy(copy_stdBmp.getConfig(), true);      // Restore stdBmp

                            fixationLoss_click=0;
                            fixationLoss_shown=0;
                            falseNegative_click=0;
                            falseNegative_shown=0;
                            currentRun = 1;

                            missPoint_count=0;

                            // following is commented because blink_point_i is initialised as -1 and it causes activity to crash
//                            textView_fixationLoss.setText("Fixation Loss: "+fixationLoss_click+"/"+fixationLoss_shown+" Total Points: " +pointSequence[blink_point_i]);
//                            textView_falsepositive.setText("False Positive: "+falseNegative_click+"/"+falseNegative_shown+" " +pointSequence[blink_point_i]);


                            onStartClintTestButton();

                        }
                    }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    // cancel
                }
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();


        } else{
            Snackbar.make(view,"Please PAUSE your test to RESTART",Snackbar.LENGTH_SHORT).setAction("Action",null).show();
        }
    }


    public void sendSeekbarValue(int progress) {
        ServerCommTask new_server_task = new ServerCommTask(ip_address, server_port, "" + progress);
        new_server_task.execute();
    }
    //endregion

    //region onDestroy function for removing all code-links
    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (serverSocket != null) {
            try {
                serverSocket.close();
                unregisterReceiver(mReceiver);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        message_to_client = "Restart";
        ServerCommTask new_server_task = new ServerCommTask(ip_address, server_port, message_to_client);
        new_server_task.execute();
    }
    //endregion

    //region Code for setting up the BroadcastReceiver and the gamepad buttons
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(is_test_running) {
            if (keyCode == 97) {
                Log.i("custom", "Button 2");

            } else if (keyCode == 96) {
                Log.i("custom", "Button 1");
            } else if (keyCode == 25) {
                Log.i("custom", "Joystick is down");

            } else if (keyCode == 24) {
                Log.i("custom", "Joystick is up");

                CLICK_LENGTH++;
                // this function should act only if click is false - if click is already true, that means person has clicked the button twice for the same point

                Log.d("log_click","click : " + CLICK_LENGTH);
                if (!click) {
                    // if click is false

                    click = true;
                    // playing sound
                    Thread newThread = new Thread() {
                        @Override
                        public void run() {


                            if (pointSequence[blink_point_i] > 54) {
//                                playSound_ERROR();
                                playSound();
                            } else {

                                playSound();
                            }
                        }
                    };
                    newThread.start();
                }


            } else if (keyCode == 21 || keyCode == 88) {
                Log.i("custom", "Joystick is left");
            } else if (keyCode == 22 || keyCode == 87) {
                Log.i("custom", "Joystick is right");


            } else if (keyCode == 85) {
                Log.i("custom", "Button A");
            }

            //endregion

        }
        return true;

    }
    //endregion

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(mReceiver, mIntentFilter);
    }

    @Override
    protected void onPause() {
        unregisterReceiver(mReceiver);
        super.onPause();
    }

    //region Function : Generate Test Points
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void generate_TestPoints() {
//        test_point_list.clear();
//
//        for (int x = 1; x <= (test_points + blind_spot_points); x++) {
//            test_point_list.add(x);
//        }
//
//        Collections.shuffle(test_point_list);

        // new Code for generating test points

        // randomly initializing
        for (int i = 1;i<=POINTS_IN_RUN_1;i++){
            pointSequence[i] = i;
        }
        shuffleArray(pointSequence);




        int i = 1;
// check if points are concurrent
        int pre = pointSequence[1];
        int current = pointSequence[2];

        boolean flag = true;
        while (flag){
            if(((pre>=55)&&(pre<=POINTS_IN_RUN_1))){
                if(((current>=55)&&(current<=POINTS_IN_RUN_1))){
                    // this means current and pre both are fixation points
                    // shuffling array again
                    shuffleArray(pointSequence);
                    i = 0;
//                    Log.d("LOG_TAG","Repeated entry found!");
                }
            }
            i++;
            pre = pointSequence[i];
            current = pointSequence[i+1];

            if(i>(POINTS_IN_RUN_1-2)){
                flag = false;
                Log.d("LOG_TAG","Confirm - No repeated blind points");
                Log.d("LOG_TAG_before",""+ Arrays.toString(pointSequence));
            }
        }

// here pointSequence is properly shuffled and has no repeated entry of blind spot or false positive

        // FIXME : making first point swapping with zero
        int k = 1;
        while(k<=65){
            if(pointSequence[k] == 0){
                pointSequence[k] = pointSequence[0];
                pointSequence[0] = 0;
                break;
            }
            k++;
        }
        // FIXME : making last point swapping with zero
        k = 2;
        while(k<=65){
            if(pointSequence[k] == 0){
                pointSequence[k] = pointSequence[65];
                pointSequence[65] = 0;
                break;
            }
            k++;
        }

        Log.d("LOG_TAG_after",""+ Arrays.toString(pointSequence));
    }
    //endregion

    //region Class for initialising the client task
    public static class ServerCommTask extends AsyncTask<Void, Void, Void> {

        String destination_address;
        int destination_port;
        //String response = "";
        String message_to_server;

        ServerCommTask(String address, int port, String message) {
            destination_address = address;
            destination_port = port;
            message_to_server = message;
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            Socket socket = null;
            DataOutputStream dataOutputStream = null;
            DataInputStream dataInputStream = null;

            try {
                socket = new Socket(destination_address, destination_port);
                dataOutputStream = new DataOutputStream(
                        socket.getOutputStream());
                dataInputStream = new DataInputStream(socket.getInputStream());

                if (message_to_server != null) {
                    dataOutputStream.writeUTF(message_to_server);
                }

                //response = dataInputStream.readUTF();

            } catch (UnknownHostException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
//                response = "UnknownHostException: " + e.toString();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
//                response = "IOException: " + e.toString();
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

                if (dataOutputStream != null) {
                    try {
                        dataOutputStream.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

                if (dataInputStream != null) {
                    try {
                        dataInputStream.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
//            textResponse.setText(response);
            super.onPostExecute(result);
        }
    }
    //endregion

    //region Function for starting Visual Acquity test
    public void startVisualAcquityTest(View view) {

        if(is_visual_possible) {
            // sending eye ( right or left )
            Gson gson = new Gson();
            char[] eye = new char[1];
            eye[0] = testForEye;     // text to show
            String tempStr = gson.toJson(eye);
            message_to_client = "eye" + tempStr;
            ServerCommTask new_server_task = new ServerCommTask(ip_address, server_port, message_to_client);
            new_server_task.execute();

            // start 'VisualAcquityTesting' Activity
            Intent temp = new Intent(MainActivity.this, VisualAcquityTesting.class);
            startActivity(temp);
        }else{
            Snackbar.make(view,"Please PAUSE your test for VISUAL ACQUITY TEST",Snackbar.LENGTH_SHORT).setAction("Action",null).show();
        }
    }

    //endregion

    // region Function : Shuffle array

    // Implementing Fisher–Yates shuffle
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    static void shuffleArray(int[] ar)
    {
        // If running on Java 6 or older, use `new Random()` on RHS here
        Random rnd = ThreadLocalRandom.current();
        for (int i = ar.length - 1; i > 0; i--)
        {
            int index = rnd.nextInt(i + 1);
            // Simple swap
            int a = ar[index];
            ar[index] = ar[i];
            ar[i] = a;
        }
    }
// endregion

    // region Function to get coordinates corresponding to point number
    public int[] getCoordinates (int pointNumber, char eye,boolean isRescaled){
        int actualBs_x = blindSpot_x;
        int actualBs_y = blindSpot_y;
        if(isRescaled){
            // points for operator's app
            blindSpot_x = (int) (18.33*13.2);
            blindSpot_y = (int) (45);
        }



        {
            switch (pointNumber) {
                case 0:
                    coordinate[0] = 2000;
                    coordinate[1] = 2000;
                    break;
                case 1:
                    coordinate[0] = -loc_9_degree;
                    coordinate[1] = loc_21_degree;
                    break;

                case 2:
                    coordinate[0] = -loc_3_degree;
                    coordinate[1] = loc_21_degree;
                    break;
                case 3:
                    coordinate[0] = loc_3_degree;
                    coordinate[1] = loc_21_degree;
                    break;
                case 4:
                    coordinate[0] = loc_9_degree;
                    coordinate[1] = loc_21_degree;
                    break;

                case 5:
                    coordinate[0] = -loc_15_degree;
                    coordinate[1] = loc_15_degree;
                    break;
                case 6:
                    coordinate[0] = -loc_9_degree;
                    coordinate[1] = loc_15_degree;
                    break;
                case 7:
                    coordinate[0] = -loc_3_degree;
                    coordinate[1] = loc_15_degree;
                    break;
                case 8:
                    coordinate[0] = loc_3_degree;
                    coordinate[1] = loc_15_degree;
                    break;
                case 9:
                    coordinate[0] = loc_9_degree;
                    coordinate[1] = loc_15_degree;
                    break;
                case 10:
                    coordinate[0] = loc_15_degree;
                    coordinate[1] = loc_15_degree;
                    break;

                case 11:
                    coordinate[0] = -loc_21_degree;
                    coordinate[1] = loc_9_degree;
                    break;
                case 12:
                    coordinate[0] = -loc_15_degree;
                    coordinate[1] = loc_9_degree;
                    break;
                case 13:
                    coordinate[0] = -loc_9_degree;
                    coordinate[1] = loc_9_degree;
                    break;
                case 14:
                    coordinate[0] = -loc_3_degree;
                    coordinate[1] = loc_9_degree;
                    break;
                case 15:
                    coordinate[0] = loc_3_degree;
                    coordinate[1] = loc_9_degree;
                    break;
                case 16:
                    coordinate[0] = loc_9_degree;
                    coordinate[1] = loc_9_degree;
                    break;
                case 17:
                    coordinate[0] = loc_15_degree;
                    coordinate[1] = loc_9_degree;
                    break;
                case 18:
                    coordinate[0] = loc_21_degree;
                    coordinate[1] = loc_9_degree;
                    break;
                case 19:
                    if(eye =='r'){
                        coordinate[0] = -loc_27_degree;
                        coordinate[1] = loc_3_degree;
//                        Log.d("LOG_DEBUG","Entered case 19 - R");
                    }
                    else {
                        coordinate[0] = loc_27_degree;
                        coordinate[1] = loc_3_degree;
//                        Log.d("LOG_DEBUG","Entered case 19- L");
                    }
//                    Log.d("LOG_DEBUG","Entered case 19");
                    break;
                case 20:
                    coordinate[0] = -loc_21_degree;
                    coordinate[1] = loc_3_degree;
                    break;
                case 21:
                    coordinate[0] = -loc_15_degree;
                    coordinate[1] = loc_3_degree;
                    break;
                case 22:
                    coordinate[0] = -loc_9_degree;
                    coordinate[1] = loc_3_degree;
                    break;
                case 23:
                    coordinate[0] = -loc_3_degree;
                    coordinate[1] = loc_3_degree;
                    break;
                case 24:
                    coordinate[0] = loc_3_degree;
                    coordinate[1] = loc_3_degree;
                    break;
                case 25:
                    coordinate[0] = loc_9_degree;
                    coordinate[1] = loc_3_degree;
                    break;
                case 26:
                    coordinate[0] = loc_15_degree;
                    coordinate[1] = loc_3_degree;
                    break;
                case 27:
                    coordinate[0] = loc_21_degree;
                    coordinate[1] = loc_3_degree;
                    break;
                case 28:
                    if(eye=='r'){
                        coordinate[0] = -loc_27_degree;
                        coordinate[1] = -loc_3_degree;
//                        Log.d("LOG_DEBUG","Entered case 28 - R");

                    }
                    else {
                        coordinate[0] = loc_27_degree;
                        coordinate[1] = -loc_3_degree;
//                        Log.d("LOG_DEBUG","Entered case 28 - L");

                    }
//                    Log.d("LOG_DEBUG","Entered case 28");
                    break;
                case 29:
                    coordinate[0] = -loc_21_degree;
                    coordinate[1] = -loc_3_degree;
                    break;
                case 30:
                    coordinate[0] = -loc_15_degree;
                    coordinate[1] = -loc_3_degree;
                    break;
                case 31:
                    coordinate[0] = -loc_9_degree;
                    coordinate[1] = -loc_3_degree;
                    break;
                case 32:
                    coordinate[0] = -loc_3_degree;
                    coordinate[1] = -loc_3_degree;
                    break;
                case 33:
                    coordinate[0] = loc_3_degree;
                    coordinate[1] = -loc_3_degree;
                    break;
                case 34:
                    coordinate[0] = loc_9_degree;
                    coordinate[1] = -loc_3_degree;
                    break;
                case 35:
                    coordinate[0] = loc_15_degree;
                    coordinate[1] = -loc_3_degree;
                    break;
                case 36:
                    coordinate[0] = loc_21_degree;
                    coordinate[1] = -loc_3_degree;
                    break;
                case 37:
                    coordinate[0] = -loc_21_degree;
                    coordinate[1] = -loc_9_degree;
                    break;
                case 38:
                    coordinate[0] = -loc_15_degree;
                    coordinate[1] = -loc_9_degree;
                    break;
                case 39:
                    coordinate[0] = -loc_9_degree;
                    coordinate[1] = -loc_9_degree;
                    break;
                case 40:
                    coordinate[0] = -loc_3_degree;
                    coordinate[1] = -loc_9_degree;
                    break;
                case 41:
                    coordinate[0] = loc_3_degree;
                    coordinate[1] = -loc_9_degree;
                    break;
                case 42:
                    coordinate[0] = loc_9_degree;
                    coordinate[1] = -loc_9_degree;
                    break;
                case 43:
                    coordinate[0] = loc_15_degree;
                    coordinate[1] = -loc_9_degree;
                    break;
                case 44:
                    coordinate[0] = loc_21_degree;
                    coordinate[1] = -loc_9_degree;
                    break;
                case 45:
                    coordinate[0] = -loc_15_degree;
                    coordinate[1] = -loc_15_degree;
                    break;
                case 46:
                    coordinate[0] = -loc_9_degree;
                    coordinate[1] = -loc_15_degree;
                    break;
                case 47:
                    coordinate[0] = -loc_3_degree;
                    coordinate[1] = -loc_15_degree;
                    break;
                case 48:
                    coordinate[0] = loc_3_degree;
                    coordinate[1] = -loc_15_degree;
                    break;
                case 49:
                    coordinate[0] = loc_9_degree;
                    coordinate[1] = -loc_15_degree;
                    break;
                case 50:
                    coordinate[0] = loc_15_degree;
                    coordinate[1] = -loc_15_degree;
                    break;
                case 51:
                    coordinate[0] = -loc_9_degree;
                    coordinate[1] = -loc_21_degree;
                    break;
                case 52:
                    coordinate[0] = -loc_3_degree;
                    coordinate[1] = -loc_21_degree;
                    break;
                case 53:
                    coordinate[0] = loc_3_degree;
                    coordinate[1] = -loc_21_degree;
                    break;
                case 54:
                    coordinate[0] = loc_9_degree;
                    coordinate[1] = -loc_21_degree;
                    break;

                // adding point location for blind spot

                case 55:
                    showBlinkSpot();
                    break;
                case 56:
                    showBlinkSpot();
                    break;
                case 57:
                    showBlinkSpot();
                    break;
                case 58 :
                    showBlinkSpot();
                    break;
                case 59:
                    showBlinkSpot();
                    break;
                case 60 :
                    coordinate[0] = 2000;
                    coordinate[1] = 2000;
                    break;
                case 61 :
                    coordinate[0] = 2000;
                    coordinate[1] = 2000;
                    break;
                case 62 :
                    coordinate[0] = 2000;
                    coordinate[1] = 2000;
                    break;
                case 63 :
                    coordinate[0] = 2000;
                    coordinate[1] = 2000;
                    break;
                case 64 :
                    coordinate[0] = 2000;
                    coordinate[1] = 2000;
                    break;

            }

        }
        blindSpot_x = actualBs_x;
        blindSpot_y = actualBs_y;
        return coordinate;
    }

    private void showBlinkSpot() {
        if (testForEye == 'r') {
            coordinate[0]  = blindSpot_x;
            coordinate[1] = blindSpot_y;
        } else{
            coordinate[0]  = -1 * blindSpot_x;
            coordinate[1] = blindSpot_y;
        }
    }

    // endregion

    //region Code for playing a tone on button click
    public void playSound() {
//        AudioTrack tone = generateTone(400, 150);
//        tone.play();

        // Experimental

        // experimental code for generating tone
        ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC,100);
        toneGenerator.startTone(3);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        toneGenerator.stopTone();
        toneGenerator.release();


    }
    //endregion

    // region Play sound - Error
    public  void playSound_ERROR(){
//        AudioTrack tone = generateTone(600, 200);
//        tone.play();
        // experimental

        // experimental code for generating tone
        ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC,100);
        toneGenerator.startTone(9);
        try {
            Thread.sleep(150);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        toneGenerator.stopTone();
        toneGenerator.release();

    }

    // endregion

    // region Code for generating custom tone
    private AudioTrack generateTone(double freqHz, int durationMs) {
        int count = (int) (44100.0 * 2.0 * (durationMs / 1000.0)) & ~1;
        short[] samples = new short[count];
        for (int i = 0; i < count; i += 2) {
            short sample = (short) (Math.sin(2 * Math.PI * i / (44100.0 / freqHz)) * 0x7FFF);
            samples[i] = sample;
            samples[i + 1] = sample;
        }
        AudioTrack track = new AudioTrack(AudioManager.STREAM_MUSIC, 44100,
                AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT,
                count * (Short.SIZE / 8), AudioTrack.MODE_STATIC);
        track.write(samples, 0, count);
        return track;
    }
    // endregion





}
