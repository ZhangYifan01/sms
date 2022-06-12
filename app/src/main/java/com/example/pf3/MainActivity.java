package com.example.pf3;


import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends Activity implements SensorEventListener {


    private SensorManager sensorManager;
    private final float[] accelerometerReading = new float[3];
    private final float[] magnetometerReading = new float[3];

    private final float[] rotationMatrix = new float[9];
    private final float[] orientationAngles = new float[3];
    private double mov;
    private int accMov = 10;
    private final int size = 10;
    private double heightAcc, S, D = 0;

    TextView text, direction;
    ImageView canvasView;

    private Sensor countSensor, directionSensor;
    private Button clear;
    private int oldstep, step = 0;
    private Canvas canvas;
    private double dir;
    private int width, height;

    private ShapeDrawable[] dots = new ShapeDrawable[100];
    private int[][] rooms = new int[4][4];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        text = findViewById(R.id.step);
        direction = findViewById(R.id.direction);
        clear = findViewById(R.id.clear);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        if (sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null) {
            countSensor = sensorManager
                    .getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
            sensorManager.registerListener(this, countSensor,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }

        // get the screen dimensions
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x;
        height = size.y;

        // create a canvas
        canvasView = (ImageView) findViewById(R.id.canvas);
        Bitmap blankBitmap = Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(blankBitmap);
        canvasView.setImageBitmap(blankBitmap);
        reset();

        startTimer();

        clear.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                canvas.drawColor(Color.WHITE);
                reset();
                drawRoom();
            }
        });

    }

    private void reset() {
        for (int i = 0; i < 8; i++) {
            toRoom1(i);
        }
        for (int i = 8; i < 16; i++) {
            toRoom2(i);
        }
        for (int i = 16; i < 24; i++) {
            toRoom3(i);
        }
        for (int i = 24; i < 32; i++) {
            toRoom4(i);
        }
        for (int i = 32; i < 40; i++) {
            toRoom5(i);
        }
        for (int i = 40; i < 48; i++) {
            toRoom6(i);
        }
        for (int i = 48; i < 56; i++) {
            toRoom7(i);
        }
        for (int i = 56; i < 64; i++) {
            toRoom8(i);
        }
        for (int i = 64; i < 72; i++) {
            toRoom9(i);
        }
        for (int i = 72; i < 79; i++) {
            toRoom10(i);
        }
        for (int i = 79; i < 86; i++) {
            toRoom11(i);
        }
        for (int i = 86; i < 93; i++) {
            toRoom12(i);
        }
        for (int i = 93; i < 100; i++) {
            toRoom13(i);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, countSensor, SensorManager.SENSOR_DELAY_NORMAL);
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
        }
        Sensor magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (magneticField != null) {
            sensorManager.registerListener(this, magneticField,
                    SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }


    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelerometerReading,
                    0, accelerometerReading.length);

            mov = (event.values[2] - 9.8) * (event.values[2] - 9.8);
            accMov = accMov + (int)mov;
            heightAcc = event.values[2] - 9.8;
            S = S + heightAcc;
            //text.setText(String.valueOf((int)mov));
            if ((int)mov > 2) {
                //move();

            }
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magnetometerReading,
                    0, magnetometerReading.length);
        }
//        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER){
//            step = (int)event.values[0];
//            text.setText(String.valueOf(step - oldstep));
//        }
        updateOrientationAngles();
    }

    public void updateOrientationAngles() {
        // Update rotation matrix, which is needed to update orientation angles.
        SensorManager.getRotationMatrix(rotationMatrix, null,
                accelerometerReading, magnetometerReading);

        // "rotationMatrix" now has up-to-date information.

        SensorManager.getOrientation(rotationMatrix, orientationAngles); //TODO direction
        dir = (orientationAngles[0] * 100) / 1.722;
        if (dir < 0) {
            dir = dir + 360;
        }
        direction.setText(String.valueOf("Current Direction: " + (int)dir));
        // "orientationAngles" now has up-to-date information.
    }

    public void move() {
        int xMove = 0;
        int yMove = 0;
        xMove = (int)(Math.sin(Math.toRadians(dir)) * 20); //TODO actual distance not 20
        yMove = (int)(Math.cos(Math.toRadians(dir)) * 20);
        canvas.drawColor(Color.WHITE);
        for (int i = 0; i < 100; i++) {
            Rect r = dots[i].getBounds();
            if (!survive(i)) {
                relocate(i);
            } else {
                dots[i].setBounds(r.left + xMove,r.top - yMove,r.right + xMove,r.bottom - yMove);
                dots[i].draw(canvas);
                int[] count = {0,0,0,0,0,0,0,0,0,0,0,0,0}; //should be 13
                for (int j = 0; j < 100; j++) { //j stands for room number
                    if (inRoom1(j)) {
                        count[0]++;
                    } else if (inRoom2(j)) {
                        count[1]++;
                    } else if (inRoom3(j)) {
                        count[2]++;
                    } else if (inRoom4(j)) {
                        count[3]++;
                    } else if (inRoom5(j)) {
                        count[4]++;
                    } else if (inRoom6(j)) {
                        count[5]++;
                    } else if (inRoom7(j)) {
                        count[6]++;
                    } else if (inRoom8(j)) {
                        count[7]++;
                    } else if (inRoom9(j)) {
                        count[8]++;
                    } else if (inRoom10(j)) {
                        count[9]++;
                    } else if (inRoom11(j)) {
                        count[10]++;
                    } else if (inRoom12(j)) {
                        count[11]++;
                    } else if (inRoom13(j)) {
                        count[12]++;
                    }
                }
                int sum = 0;
                for (int k = 0; k < 13; k++) {
                    count[k] = count[k] * count[k];
                    sum += count[k];
                }
                if (sum == 0 ){
                    Arrays.fill(count,7);
                    sum = 100;
                }
                text.setText("Current Room: " + String.valueOf(getIndexOfLargest(count) + 1));
            }
        }
        drawRoom();
        canvasView.invalidate();
    }
    public int getIndexOfLargest( int[] array )
    {
        if ( array == null || array.length == 0 ) return -1; // null or empty

        int largest = 0;
        for ( int i = 1; i < array.length; i++ )
        {
            if ( array[i] > array[largest] ) largest = i;
        }
        return largest; // position of the first largest found
    }

    public void relocate(int i) {
        int[] count = {0,0,0,0,0,0,0,0,0,0,0,0,0}; //should be 13
        for (int j = 0; j < 100; j++) { //j stands for room number
            if (inRoom1(j)) {
                count[0]++;
            } else if (inRoom2(j)) {
                count[1]++;
            } else if (inRoom3(j)) {
                count[2]++;
            } else if (inRoom4(j)) {
                count[3]++;
            } else if (inRoom5(j)) {
                count[4]++;
            } else if (inRoom6(j)) {
                count[5]++;
            } else if (inRoom7(j)) {
                count[6]++;
            } else if (inRoom8(j)) {
                count[7]++;
            } else if (inRoom9(j)) {
                count[8]++;
            } else if (inRoom10(j)) {
                count[9]++;
            } else if (inRoom11(j)) {
                count[10]++;
            } else if (inRoom12(j)) {
                count[11]++;
            } else if (inRoom13(j)) {
                count[12]++;
            }
        }
        int sum = 0;
        for (int k = 0; k < 13; k++) {
            count[k] = count[k] * count[k];
            sum += count[k];
        }
        if (sum == 0 ){
            Arrays.fill(count,7);
            sum = 100;
        }

        int pos = new Random().nextInt(sum);

        if (pos < count[0]) {
            toRoom1(i);
        }
        if (pos >= count[0] && pos < count[0]+count[1]){
            toRoom2(i);
        }
        if (pos >= count[0]+count[1] && pos < count[0]+count[1]+count[2]){
            toRoom3(i);
        }
        if (pos >= count[0]+count[1]+count[2] && pos < count[0]+count[1]+count[2]+count[3]) {
            toRoom4(i);
        }
        if (pos >= count[0]+count[1]+count[2]+count[3] && pos < count[0]+count[1]+count[2]+count[3]+
                count[4]) {
            toRoom5(i);
        }
        if (pos >= count[0]+count[1]+count[2]+count[3]+count[4] && pos < count[0]+count[1]+count[2]+
                count[3]+count[4]+count[5]) {
            toRoom6(i);
        }
        if (pos >= count[0]+count[1]+count[2]+count[3]+count[4]+count[5] && pos < count[0]+count[1]+
                count[2]+count[3]+count[4]+count[5]+count[6]) {
            toRoom7(i);
        }
        if (pos >= count[0]+count[1]+count[2]+count[3]+count[4]+count[5]+count[6] && pos < count[0]+
                count[1]+count[2]+count[3]+count[4]+count[5]+count[6]+count[7]) {
            toRoom8(i);
        }
        if (pos >= count[0]+count[1]+count[2]+count[3]+count[4]+count[5]+count[6]+count[7] && pos <
                count[0]+count[1]+count[2]+count[3]+count[4]+count[5]+count[6]+count[7]+count[8]) {
            toRoom9(i);
        }
        if (pos >= count[0]+count[1]+count[2]+count[3]+count[4]+count[5]+count[6]+count[7]+count[8]
                && pos < count[0]+count[1]+count[2]+count[3]+count[4]+count[5]+count[6]+count[7]+count[8]+count[9]) {
            toRoom10(i);
        }
        if (pos >= count[0]+count[1]+count[2]+count[3]+count[4]+count[5]+count[6]+count[7]+count[8]+
                count[9] && pos < count[0]+count[1]+count[2]+count[3]+count[4]+count[5]+count[6]+count[7]+count[8]+count[9]+count[10]) {
            toRoom11(i);
        }
        if (pos >= count[0]+count[1]+count[2]+count[3]+count[4]+count[5]+count[6]+count[7]+count[8]+
                count[9]+count[10] && pos < count[0]+count[1]+count[2]+count[3]+count[4]+count[5]+count[6]+count[7]+count[8]+count[9]+count[10]+count[11]) {
            toRoom12(i);
        }
        if (pos >= count[0]+count[1]+count[2]+count[3]+count[4]+count[5]+count[6]+count[7]+count[8]+
                count[9]+count[10]+count[11] && pos < count[0]+count[1]+count[2]+count[3]+count[4]+
                count[5]+count[6]+count[7]+count[8]+count[9]+count[10]+count[11]+count[12]) {
            toRoom13(i);
        }
    }
    private void toRoom1(int i ) {
        int randomX = new Random().nextInt(277) + 442;
        int randomY = new Random().nextInt(86)+ 1601;
        dots[i] = new ShapeDrawable(new OvalShape());
        dots[i].getPaint().setColor(Color.BLUE);
        dots[i].setBounds(randomX-size, randomY-size, randomX+size, randomY+size);
        dots[i].draw(canvas);
    }
    private void toRoom2(int i ) {
        int randomX = new Random().nextInt(221) + 221;
        int randomY = new Random().nextInt(254) + 1601;
        dots[i] = new ShapeDrawable(new OvalShape());
        dots[i].getPaint().setColor(Color.BLUE);
        dots[i].setBounds(randomX-size, randomY-size, randomX+size, randomY+size);
        dots[i].draw(canvas);
    }
    private void toRoom3(int i ) {
        int randomX = new Random().nextInt(221);
        int randomY = new Random().nextInt(254) + 1601;
        dots[i] = new ShapeDrawable(new OvalShape());
        dots[i].getPaint().setColor(Color.BLUE);
        dots[i].setBounds(randomX-size, randomY-size, randomX+size, randomY+size);
        dots[i].draw(canvas);
    }
    private void toRoom4(int i ) {
        int randomX = new Random().nextInt(155) + 144;
        int randomY = new Random().nextInt(337) + 1264;
        dots[i] = new ShapeDrawable(new OvalShape());
        dots[i].getPaint().setColor(Color.BLUE);
        dots[i].setBounds(randomX-size, randomY-size, randomX+size, randomY+size);
        dots[i].draw(canvas);
    }
    private void toRoom5(int i ) {
        int randomX = new Random().nextInt(155) + 144;
        int randomY = new Random().nextInt(337) + 927;
        dots[i] = new ShapeDrawable(new OvalShape());
        dots[i].getPaint().setColor(Color.BLUE);
        dots[i].setBounds(randomX-size, randomY-size, randomX+size, randomY+size);
        dots[i].draw(canvas);
    }
    private void toRoom6(int i ) {
        int randomX = new Random().nextInt(155) + 144;
        int randomY = new Random().nextInt(337) + 591;
        dots[i] = new ShapeDrawable(new OvalShape());
        dots[i].getPaint().setColor(Color.BLUE);
        dots[i].setBounds(randomX-size, randomY-size, randomX+size, randomY+size);
        dots[i].draw(canvas);
    }
    private void toRoom7(int i ) {
        int randomX = new Random().nextInt(155) + 144;
        int randomY = new Random().nextInt(337) + 254;
        dots[i] = new ShapeDrawable(new OvalShape());
        dots[i].getPaint().setColor(Color.BLUE);
        dots[i].setBounds(randomX-size, randomY-size, randomX+size, randomY+size);
        dots[i].draw(canvas);
    }
    private void toRoom8(int i ) {
        int randomX = new Random().nextInt(143) + 299;
        int randomY = new Random().nextInt(337) + 254;
        dots[i] = new ShapeDrawable(new OvalShape());
        dots[i].getPaint().setColor(Color.BLUE);
        dots[i].setBounds(randomX-size, randomY-size, randomX+size, randomY+size);
        dots[i].draw(canvas);
    }
    private void toRoom9(int i ) {
        int randomX = new Random().nextInt(221);
        int randomY = new Random().nextInt(254);
        dots[i] = new ShapeDrawable(new OvalShape());
        dots[i].getPaint().setColor(Color.BLUE);
        dots[i].setBounds(randomX-size, randomY-size, randomX+size, randomY+size);
        dots[i].draw(canvas);
    }
    private void toRoom10(int i ) {
        int randomX = new Random().nextInt(221) + 221;
        int randomY = new Random().nextInt(254);
        dots[i] = new ShapeDrawable(new OvalShape());
        dots[i].getPaint().setColor(Color.BLUE);
        dots[i].setBounds(randomX-size, randomY-size, randomX+size, randomY+size);
        dots[i].draw(canvas);
    }
    private void toRoom11(int i ) {
        int randomX = new Random().nextInt(277) + 442;
        int randomY = new Random().nextInt(86) + 168;
        dots[i] = new ShapeDrawable(new OvalShape());
        dots[i].getPaint().setColor(Color.BLUE);
        dots[i].setBounds(randomX-size, randomY-size, randomX+size, randomY+size);
        dots[i].draw(canvas);
    }
    private void toRoom12(int i ) {
        int randomX = new Random().nextInt(110) + 719;
        int randomY = new Random().nextInt(407) + 168;
        dots[i] = new ShapeDrawable(new OvalShape());
        dots[i].getPaint().setColor(Color.BLUE);
        dots[i].setBounds(randomX-size, randomY-size, randomX+size, randomY+size);
        dots[i].draw(canvas);
    }
    private void toRoom13(int i ) {
        int randomX = new Random().nextInt(303) + 299;
        int randomY = new Random().nextInt(160) + 666;
        dots[i] = new ShapeDrawable(new OvalShape());
        dots[i].getPaint().setColor(Color.BLUE);
        dots[i].setBounds(randomX-size, randomY-size, randomX+size, randomY+size);
        dots[i].draw(canvas);
    }
    public void drawRoom() {

        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
        paint.setAntiAlias(true);
        //frame
        //drawOneRoom(0,0,1080,2066, paint);

        //rooms
        drawOneRoom(442,1601, 719,1687, paint);
        drawOneRoom(221, 1601,442, 1855, paint);
        drawOneRoom(0,1601, 221,1855, paint);
        drawOneRoom(114,1264, 299, 1601, paint);
        drawOneRoom(114, 927, 299, 1264, paint);
        drawOneRoom(114, 591, 299, 927, paint);
        drawOneRoom(114, 254, 299, 591, paint);
        drawOneRoom(299, 254, 442, 591, paint);
        drawOneRoom(0, 0, 221, 254, paint);
        drawOneRoom(221, 0, 442, 254, paint);
        drawOneRoom(442, 168, 719, 254, paint);
        drawOneRoom(719, 168, 829, 575, paint);
        drawOneRoom(299,666, 602, 826, paint);

    }
    public void drawOneRoom(int xStart, int yStart, int xEnd, int yEnd, Paint paint) {
        canvas.drawLine(xStart, yStart, xEnd, yStart, paint);
        canvas.drawLine(xEnd, yStart, xEnd, yEnd, paint);
        canvas.drawLine(xStart, yEnd, xEnd, yEnd, paint);
        canvas.drawLine(xStart, yStart, xStart, yEnd, paint);
    }
    public boolean inRoom1(int i) {
        if (dots[i] == null) {
            return false;
        }
        Rect r = dots[i].getBounds();
        int left = r.left;
        int top = r.top;
        if (left >= 442 && left <= 719 && top >= 1601 && top <= 1687) {
            return true;
        } else {
            return false;
        }
    }
    public boolean inRoom2(int i) {
        if (dots[i] == null) {
            return false;
        }
        Rect r = dots[i].getBounds();
        int left = r.left;
        int top = r.top;
        if (left >= 221 && left <= 442 && top >= 1601 && top <= 1855) {
            return true;
        } else {
            return false;
        }
    }
    public boolean inRoom3(int i) {
        if (dots[i] == null) {
            return false;
        }
        Rect r = dots[i].getBounds();
        int left = r.left;
        int top = r.top;
        if (left >= 0 && left <= 221 && top >= 1601 && top <= 1855) {
            return true;
        } else {
            return false;
        }
    }
    public boolean inRoom4(int i) {
        if (dots[i] == null) {
            return false;
        }
        Rect r = dots[i].getBounds();
        int left = r.left;
        int top = r.top;
        if (left >= 114 && left <= 299 && top >= 1264 && top <= 1601) {
            return true;
        } else {
            return false;
        }
    }
    public boolean inRoom5(int i) {
        if (dots[i] == null) {
            return false;
        }
        Rect r = dots[i].getBounds();
        int left = r.left;
        int top = r.top;
        if (left >= 114 && left <= 299 && top >= 927 && top <= 1264) {
            return true;
        } else {
            return false;
        }
    }
    public boolean inRoom6(int i) {
        if (dots[i] == null) {
            return false;
        }
        Rect r = dots[i].getBounds();
        int left = r.left;
        int top = r.top;
        if (left >= 114 && left <= 299 && top >= 591 && top <= 927) {
            return true;
        } else {
            return false;
        }
    }
    public boolean inRoom7(int i) {
        if (dots[i] == null) {
            return false;
        }
        Rect r = dots[i].getBounds();
        int left = r.left;
        int top = r.top;
        if (left >= 114 && left <= 299 && top >= 254 && top <= 591) {
            return true;
        } else {
            return false;
        }
    }
    public boolean inRoom8(int i) {
        if (dots[i] == null) {
            return false;
        }
        Rect r = dots[i].getBounds();
        int left = r.left;
        int top = r.top;
        if (left >= 299 && left <= 442 && top >= 254 && top <= 591) {
            return true;
        } else {
            return false;
        }
    }
    public boolean inRoom9(int i) {
        if (dots[i] == null) {
            return false;
        }
        Rect r = dots[i].getBounds();
        int left = r.left;
        int top = r.top;
        if (left >= 0 && left <= 221 && top >= 0 && top <= 254) {
            return true;
        } else {
            return false;
        }
    }
    public boolean inRoom10(int i) {
        if (dots[i] == null) {
            return false;
        }
        Rect r = dots[i].getBounds();
        int left = r.left;
        int top = r.top;
        if (left >= 221 && left <= 442 && top >= 0 && top <= 254) {
            return true;
        } else {
            return false;
        }
    }
    public boolean inRoom11(int i) {
        if (dots[i] == null) {
            return false;
        }
        Rect r = dots[i].getBounds();
        int left = r.left;
        int top = r.top;
        if (left >= 442 && left <= 719 && top >= 168 && top <= 254) {
            return true;
        } else {
            return false;
        }
    }
    public boolean inRoom12(int i) {
        if (dots[i] == null) {
            return false;
        }
        Rect r = dots[i].getBounds();
        int left = r.left;
        int top = r.top;
        if (left >= 719 && left <= 829 && top >= 168 && top <= 575) {
            return true;
        } else {
            return false;
        }
    }
    public boolean inRoom13(int i) {
        if (dots[i] == null) {
            return false;
        }
        Rect r = dots[i].getBounds();
        int left = r.left;
        int top = r.top;
        if (left >= 299 && left <= 602 && top >= 666 && top <= 826) {
            return true;
        } else {
            return false;
        }
    }
    public boolean survive(int i) {
        if (inRoom1(i) ||
            inRoom2(i) ||
            inRoom3(i) ||
            inRoom4(i) ||
            inRoom5(i) ||
            inRoom6(i) ||
            inRoom7(i) ||
            inRoom8(i) ||
            inRoom9(i) ||
            inRoom10(i) ||
            inRoom11(i) ||
            inRoom12(i) ||
            inRoom13(i)) {
            return true;
        } else {
            return false;
        }
    }

    private Timer mTimer1;
    private TimerTask updatePosition;
    private Handler mTimerHandler = new Handler();

    private void stopTimer(){
        if(mTimer1 != null){
            mTimer1.cancel();
            mTimer1.purge();
        }
    }

    private int temp = 0;
    private void startTimer(){
        mTimer1 = new Timer();
        updatePosition = new TimerTask() {
            public void run() {
                mTimerHandler.post(new Runnable() {
                    public void run(){
                        if (accMov > 1) {
                            move();
                        }
                        accMov = 0;
                        if (temp < 10) {
                            D = D + S;

                            temp++;
                        } else {
                            temp = 0;
                            //text.setText(String.valueOf((int)S));
                            S = 0;
                        }
                    }
                });
            }
        };
        mTimer1.schedule(updatePosition, 1, 100);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
