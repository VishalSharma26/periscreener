package com.example.u.periscreener_operater;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by U on 12/2/2017.
 */

public class ReportActivity extends Activity{

    Bitmap bitmap = Bitmap.createBitmap(1200,1200, Bitmap.Config.ARGB_8888);
    Canvas canvas;
    Paint paint;
    ImageView imageView;
    char testForEye;
    int loc_9_degree,loc_21_degree,loc_12_degree,loc_5_degree,loc_1_5_degree,loc_3_degree,loc_15_degree,loc_27_degree;

    private static final String MY_PREFS_NAME = "METADATA_PS_OPERATOR";
    int INTENSITY;
    int max_intensity = 7;
    int min_intensity = 1;
    int FIXATION_RECTANGLE_INTENSITY = min_intensity;
    int dot_width = 4;
    int intensity_factor = 5;
    int square_length = 110;
    int tempLength = 0;
    String TIME_DURATION = "Time duration : 02:54";
    String BACKGROUND = "Background : 31.5 ASB";
    String PLACE = "Aravind Eye Hospital\nThavalakuppam\nPondicherry";

    String contact[] = {"bharatvishal26@gmail.com","developer.kotawala@gmail.com"};
    String subject = "Patient Report _PERISCREENER_";
    String emailText = "";

    int small_rect_r[][] = {
            {320,210,4}, // 1
            {820,210,4}, // 2
            {210,320,4}, // 3
            {930,320,4}, // 4
            {210,820,4}, // 5
            {930,820,4}, // 6
            {320,930,4}, // 7
            {820,930,4}, // 8
            {100,430,4}, // 9
            {100,710,4}  // 10
    };
    int small_rect_l[][] = {
            {320,210,4}, // 1
            {820,210,4}, // 2
            {210,320,4}, // 3
            {930,320,4}, // 4
            {210,820,4}, // 5
            {930,820,4}, // 6
            {320,930,4}, // 7
            {820,930,4}, // 8
            {1035,430,0},// 9
            {1035,710,0},// 10
    };
    //
//    boolean response[] = {     true,false,true,true
//            ,false,true,true,true,true,false
//            ,true,true,true,true,true,true,true,false
//            ,true,true,true,true,true,true,true,true,true
//            ,false,true,true,true,true,true,true,true,true
//            ,true,true,true,true,true,true,true,false,
//            false,true,true,true,true,true
//            ,true,false,true,true};
    boolean response[] = new boolean[55];
    TextView textView_mr_number,textView_age,textView_eye,textView_date,textView_time;
    TextView textView_fixationLoss,textView_falsePositve,textView_falseNegative,textView_testDuration,textView_backgroung,textView_place;
    int falsePositive_points,fixationLoss_points;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        final WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.screenBrightness = 1f;       // full screen brightness
        getWindow().setAttributes(lp);

        canvas = new Canvas(bitmap);
        paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);

        textView_mr_number = (TextView)findViewById(R.id.tv_mr_number_id);
        textView_age = (TextView)findViewById(R.id.tv_age_id);
        textView_eye = (TextView)findViewById(R.id.tv_eye_id);
        textView_fixationLoss = (TextView)findViewById(R.id.tv_fixation_loss_id);
        textView_falsePositve = (TextView)findViewById(R.id.tv_falsePositive_points_id);
        textView_falseNegative = (TextView)findViewById(R.id.tv_falseNegative_points_id);
        textView_testDuration = (TextView)findViewById(R.id.tv_test_duration_id);
        textView_backgroung = (TextView)findViewById(R.id.tv_backgroung_id);
        textView_date = (TextView)findViewById(R.id.tv_date_id);
        textView_time = (TextView)findViewById(R.id.tv_time_id);
        textView_place = (TextView)findViewById(R.id.tv_place_id);

        imageView = (ImageView)findViewById(R.id.imageView_id);



        response = ReportResultClass.getResult();
        fixationLoss_points = ReportResultClass.getFixationLoss();
        falsePositive_points = ReportResultClass.getFalseNegative();

        textView_mr_number.setText("MR Number : " + ReportResultClass.getMrNumber());
        textView_age.setText("Age : "+ ReportResultClass.getAge());
        textView_eye.setText("Eye : " + ReportResultClass.getEye());

        textView_fixationLoss.setText("Fixation Loss : "+ fixationLoss_points + " / 10");
        textView_falsePositve.setText("False Positive : "+falsePositive_points + " / 10");
        textView_falseNegative.setText("False Negative : " + ReportResultClass.getFalseNegative() );
        textView_testDuration.setText(TIME_DURATION);
        textView_backgroung.setText(BACKGROUND);
        textView_date.setText("Date : "+ ReportResultClass.getDate());
        textView_time.setText("Time : "+ ReportResultClass.getTime());
        textView_place.setText(PLACE);


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


        int width = 4;

        float left = 600-width/2;
//        int top = 600-report_rectangle_width/4*3;
        float top = 0;
//        float bottom = top + report_rectangle_width*3/2;
        float right = left+width;
        float bottom  = 1200;

        canvas.drawRect(left,top,right,bottom,paint);
        left = 0;
        top = 600-width/2;
        right = 1200;
        bottom = top+width;
        canvas.drawRect(left,top,right,bottom,paint);

        // making cut for different degrees - 10 | 20 | 30
        // scaling down by factor of 0.85
//        loc_10_degree = (int) (.85*loc_10_degree);
//        loc_20_degree = (int) (.85*loc_20_degree);
//        loc_30_degree = (int) (.85*loc_30_degree);
        int distancePoint_10degree = (int) 183.33;
        int distancePoint_20degree = (int) (183.33*2);
        int distancePoint_30degree = (int) (183.33*3);


        // region axis cuts
        //  x-axis
        left = 600+distancePoint_10degree-width;
        top = 600-3*width;
        right = left+width;
        bottom = top+6*width;
        canvas.drawRect(left,top,right,bottom,paint);

        left = 600+distancePoint_20degree-width;
        top = 600-3*width;
        right = left+width;
        bottom = top+6*width;
        canvas.drawRect(left,top,right,bottom,paint);

        left = 600+distancePoint_30degree-width;
        top = 600-3*width;
        right = left+width;
        bottom = top+6*width;
        canvas.drawRect(left,top,right,bottom,paint);

        left = 600-distancePoint_10degree-width;
        top = 600-3*width;
        right = left+width;
        bottom = top+6*width;
        canvas.drawRect(left,top,right,bottom,paint);


        left = 600-distancePoint_20degree-width;
        top = 600-3*width;
        right = left+width;
        bottom = top+6*width;
        canvas.drawRect(left,top,right,bottom,paint);


        left = 600-distancePoint_30degree-width;
        top = 600-3*width;
        right = left+width;
        bottom = top+6*width;
        canvas.drawRect(left,top,right,bottom,paint);


        // y-axis
        left = 600-3*width;
        top = 600-distancePoint_10degree-width;
        right = left+6*width;
        bottom = top+width;
        canvas.drawRect(left,top,right,bottom,paint);

        left = 600-3*width;
        top = 600-distancePoint_20degree-width;
        right = left+6*width;
        bottom = top+width;
        canvas.drawRect(left,top,right,bottom,paint);

        left = 600-3*width;
        top = 600-distancePoint_30degree-width;
        right = left+6*width;
        bottom = top+width;
        canvas.drawRect(left,top,right,bottom,paint);

        left = 600-3*width;
        top = 600+distancePoint_10degree-width;
        right = left+6*width;
        bottom = top+width;
        canvas.drawRect(left,top,right,bottom,paint);

        left = 600-3*width;
        top = 600+distancePoint_20degree-width;
        right = left+6*width;
        bottom = top+width;
        canvas.drawRect(left,top,right,bottom,paint);

        left = 600-3*width;
        top = 600+distancePoint_30degree-width;
        right = left+6*width;
        bottom = top+width;
        canvas.drawRect(left,top,right,bottom,paint);

        // endregion


        // region Step 3 :  Solid small dots
        loc_3_degree = 55;
        loc_9_degree = 165;
        loc_15_degree = 275;
        loc_21_degree = 385;
        loc_27_degree = 495;


        for(int i = 1;i<=54;i++){
            int coor[] = getCoordinates(i,testForEye);

            if(response[i]){
                INTENSITY = min_intensity;
            }else{
                INTENSITY = max_intensity;
            }
            draw_dots_rectangle(coor[0]+600-55 , coor[1]+600-55 , INTENSITY);

        }

        // region Calculating intensity of small dots
        if(testForEye == 'r') {
            small_rect_r[0][2] = setIntensity(response[45], response[51]);
            small_rect_r[1][2] = setIntensity(response[54], response[50]);
            small_rect_r[2][2] = setIntensity(response[37], response[45]);
            small_rect_r[3][2] = setIntensity(response[50], response[44]);
            small_rect_r[4][2] = setIntensity(response[11], response[5]);
            small_rect_r[5][2] = setIntensity(response[18], response[10]);
            small_rect_r[6][2] = setIntensity(response[5], response[1]);
            small_rect_r[7][2] = setIntensity(response[4], response[10]);
            small_rect_r[8][2] = setIntensity(response[28], response[37]);
            small_rect_r[9][2] = setIntensity(response[11], response[19]);
        } else{
            small_rect_l[0][2] = setIntensity(response[45], response[51]);
            small_rect_l[1][2] = setIntensity(response[54], response[50]);
            small_rect_l[2][2] = setIntensity(response[37], response[45]);
            small_rect_l[3][2] = setIntensity(response[50], response[44]);
            small_rect_l[4][2] = setIntensity(response[11], response[5]);
            small_rect_l[5][2] = setIntensity(response[18], response[10]);
            small_rect_l[6][2] = setIntensity(response[5], response[1]);
            small_rect_l[7][2] = setIntensity(response[4], response[10]);
            small_rect_l[8][2] = setIntensity(response[28], response[44]);
            small_rect_l[9][2] = setIntensity(response[18], response[19]);
        }
        // endregion

        // FIXME: 11/23/2017  1/4 square adding at the boundaries

        for(int i=0;i<10;i++) {
            if(testForEye=='r') {
                draw_dots_small_rectangle(small_rect_r[i][0], small_rect_r[i][1], small_rect_r[i][2]);
            }else{
                draw_dots_small_rectangle(small_rect_l[i][0], small_rect_l[i][1], small_rect_l[i][2]);
            }
        }


        // endregion

        // region Step 4 : Blind Spot

        loc_12_degree = (int) 18.33*12;
        loc_15_degree = (int) 18.33*15;
        loc_5_degree = (int) 18.33*5;
        loc_1_5_degree = (int) ((int) 18.33*1.5);


        // region making triangle

//        paint.setStrokeWidth(5);
//        canvas.drawLine(600+loc_12_degree,600+loc_1_5_degree,600+loc_15_degree,600+loc_1_5_degree,paint);
//        canvas.drawLine(600+loc_12_degree,600+loc_1_5_degree,600+loc_12_degree+27,600+loc_5_degree,paint);
//        canvas.drawLine(600+loc_15_degree,600+loc_1_5_degree,600+loc_12_degree+27,600+loc_5_degree,paint);

        if(testForEye=='r') {
            for (int i = (FIXATION_RECTANGLE_INTENSITY * intensity_factor)-20  ; i < square_length - (FIXATION_RECTANGLE_INTENSITY * intensity_factor)+20 ; i = i + FIXATION_RECTANGLE_INTENSITY * intensity_factor) {
                for (int j = (FIXATION_RECTANGLE_INTENSITY * intensity_factor)-20+tempLength; j < square_length+20 - (FIXATION_RECTANGLE_INTENSITY * 5) -tempLength; j = j + FIXATION_RECTANGLE_INTENSITY * intensity_factor) {
                    canvas.drawCircle((int) (600 + (18.33 * 13.5)) + j - 55, (int) (600 + (18.33 * 1.5)) + i -6, dot_width, paint);

                }
                tempLength +=1;
            }
        }else{
            for (int i = (FIXATION_RECTANGLE_INTENSITY * intensity_factor)-20; i < square_length - (FIXATION_RECTANGLE_INTENSITY * intensity_factor) +20; i = i + FIXATION_RECTANGLE_INTENSITY * intensity_factor) {
                for (int j = (FIXATION_RECTANGLE_INTENSITY * intensity_factor)-20 + tempLength; j < square_length+20 - (FIXATION_RECTANGLE_INTENSITY * 5) -tempLength; j = j + FIXATION_RECTANGLE_INTENSITY * intensity_factor) {
                    canvas.drawCircle((int) (600 - (18.33 * 13.5)) + j - 55, (int) (600 + (18.33 * 1.5)) + i -6, dot_width, paint);
                }
                tempLength +=1;
            }
        }

        // endregion

        imageView.setImageBitmap(bitmap);

        // get values of brightness and contrast
        SharedPreferences preferences = getSharedPreferences(MY_PREFS_NAME,MODE_PRIVATE);
        int brightnessVal = preferences.getInt("brightness",50);
        int contrastVal = preferences.getInt("contrast",50);

        emailText = "MR Number : " + ReportResultClass.getMrNumber() + "\n" +
                "Age : " + ReportResultClass.getAge() + "\n" +
                "Eye  : " + ReportResultClass.getEye() + "\n" +
                "Test type : Supra threshold 24-2 \nFixation Monitor : Blind Spot\n\n"+

                "Fixation Loss : " + fixationLoss_points + " / 10\n" +
                "False Positive : " + falsePositive_points + "/ 10\n" +
                "False Negative : " + ReportResultClass.getFalseNegative() + "\n" +
                TIME_DURATION + "\n" +
                "Brightness : " + brightnessVal + "\n" +
                "Contrast : " + contrastVal + "\n" +
                "Date : " + ReportResultClass.getDate() + "\n" +
                "Time : " + ReportResultClass.getTime() + "\n" +
                "Place : " + PLACE + "\n\n" +
                "RESULT\n";

        for(int i=0;i<55;i++) {
            if (response[i]) {
                emailText = emailText + "\n" + i + " - Black";
            } else {
                emailText = emailText + "\n" + i + " - White";
            }
        }


    }


    private int setIntensity(boolean b1, boolean b2) {
        if(b1 && b2){
            return 1;
        }
        else if(!b1 && !b2){
            return 7;
        }
        return 2;
    }

    // region for dots rectangle based on intensity
    private void draw_dots_rectangle(int x, int y , int intensity) {
        for(int i=(intensity*intensity_factor)/4;i<square_length-(intensity*intensity_factor)/4; i=i+intensity*intensity_factor){
            for(int j=(intensity*intensity_factor)/4;j<square_length -(intensity*intensity_factor)/4;j = j+intensity*intensity_factor) {
                canvas.drawCircle(x + i, y + j, dot_width, paint);
            }
        }
    }
    // endregion



    // region for 1/4 of dots rectangle based on intensity
    private void draw_dots_small_rectangle(int x1, int y1, int intensity) {
        for(int i=(intensity*intensity_factor)/4;i<60-(intensity*intensity_factor)/4; i=i+intensity*intensity_factor){
            for(int j=(intensity*intensity_factor)/4;j<60-(intensity*intensity_factor)/4;j = j+intensity*intensity_factor) {
                canvas.drawCircle(x1 + i, y1 + j, dot_width, paint);
            }
        }
    }

    //endregion

    // region Function to get coordinates corresponding to point number
    public int[] getCoordinates (int pointNumber, char eye){
        int[] coordinate = new int[2];
        {
            switch (pointNumber) {
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
                        Log.d("LOG_DEBUG","Entered case 19 - R");}
                    else {
                        coordinate[0] = loc_27_degree;
                        coordinate[1] = loc_3_degree;
                        Log.d("LOG_DEBUG","Entered case 19- L");
                    }
                    Log.d("LOG_DEBUG","Entered case 19");
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
                        Log.d("LOG_DEBUG","Entered case 28 - R");

                    }
                    else {
                        coordinate[0] = loc_27_degree;
                        coordinate[1] = -loc_3_degree;
                        Log.d("LOG_DEBUG","Entered case 28 - L");

                    }
                    Log.d("LOG_DEBUG","Entered case 28");
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


            }

        }
        return coordinate;
    }



    // endregion

    public void onShareReport(View view) {
        Intent email_intent = new Intent(Intent.ACTION_SEND);
        email_intent.putExtra(Intent.EXTRA_EMAIL,contact);
        email_intent.putExtra(Intent.EXTRA_SUBJECT,subject);
//        email_intent.putExtra(android.content.Intent.EXTRA_TEXT, " ");
        email_intent.setType("plain/test"); // accept any image
        //attach the file to the intent

        email_intent.putExtra(Intent.EXTRA_TEXT,emailText);
//        email_intent.setType("image/*");
//        email_intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(savebitmap(bitmap)));

        startActivity(email_intent);


    }

}
