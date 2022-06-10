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

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends Activity implements SensorEventListener {


    private SensorManager sensorManager;
    private final float[] accelerometerReading = new float[3];
    private final float[] magnetometerReading = new float[3];

    private final float[] rotationMatrix = new float[9];
    private final float[] orientationAngles = new float[3];
    private  double mov;

    TextView text, direction;
    ImageView canvasView;

    private Sensor countSensor, directionSensor;
    private Button clear;
    private int oldstep, step = 0;
    private ShapeDrawable drawable,anchor1, anchor2;
    private Canvas canvas;
    private double dir;
    private int width, height;


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
        Toast.makeText(getApplication(),(String.valueOf(height)),Toast.LENGTH_SHORT).show();


        // create a drawable object
        relocate();
//        drawable = new ShapeDrawable(new OvalShape());
//        drawable.getPaint().setColor(Color.BLUE);
//        drawable.setBounds(width/2-10, height/2-10, width/2+10, height/2+10);

//        anchor1 = new ShapeDrawable(new OvalShape());
//        anchor1.getPaint().setColor(Color.RED);
//        anchor1.setBounds(width/2-10, height/2-10, width/2+10, height/2+10);

        // create a canvas
        canvasView = (ImageView) findViewById(R.id.canvas);
        Bitmap blankBitmap = Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(blankBitmap);
        canvasView.setImageBitmap(blankBitmap);

        drawable.draw(canvas);


        startTimer();

        clear.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //move();
            }
        });

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

        SensorManager.getOrientation(rotationMatrix, orientationAngles);
        dir = (orientationAngles[0] * 100) / 1.722;
        if (dir < 0) {
            dir = dir + 360;
        }
        direction.setText(String.valueOf((int)dir));
        // "orientationAngles" now has up-to-date information.
    }

    public void move() {
        int xMove = 0;
        int yMove = 0;
        xMove = (int)(Math.sin(Math.toRadians(dir)) * 20);
        yMove = (int)(Math.cos(Math.toRadians(dir)) * 20);

        Rect r = drawable.getBounds();

        if (r.left + xMove < 0 || r.right + xMove > 1080 || r.top - yMove < 0 || r.bottom - yMove > 2066) {
            relocate();
        } else {
            drawable.setBounds(r.left + xMove,r.top - yMove,r.right + xMove,r.bottom - yMove);
            canvas.drawColor(Color.WHITE);
            drawable.draw(canvas);
        }

//        drawable.setBounds(r.left + xMove,r.top - yMove,r.right + xMove,r.bottom - yMove);
//        canvas.drawColor(Color.WHITE);
//        drawable.draw(canvas);
        drawRoom();
        canvasView.invalidate();

        text.setText(String.valueOf(drawable.getBounds()));
    }

    public void relocate() {
        int randomX = new Random().nextInt(1080);
        int randomY = new Random().nextInt(2066);
        drawable = new ShapeDrawable(new OvalShape());
        drawable.getPaint().setColor(Color.BLUE);
        drawable.setBounds(randomX-10, randomY-10, randomX+10, randomY+10);

    }

    public void drawRoom() {

        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(8);
        paint.setAntiAlias(true);

        canvas.drawLine(0, 0, 1080, 0, paint);
        canvas.drawLine(0, 2066, 1080, 2066, paint);
        canvas.drawLine(0, 0, 0, 2066, paint);
        canvas.drawLine(1080, 0, 1080, 2066, paint);
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

    private void startTimer(){
        mTimer1 = new Timer();
        updatePosition = new TimerTask() {
            public void run() {
                mTimerHandler.post(new Runnable() {
                    public void run(){
                        move();
                    }
                });
            }
        };
        mTimer1.schedule(updatePosition, 1, 1000);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
