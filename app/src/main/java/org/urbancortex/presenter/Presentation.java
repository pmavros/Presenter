package org.urbancortex.presenter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.os.Vibrator;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.widget.Button;
import android.widget.ImageView;

import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import static android.os.SystemClock.elapsedRealtime;
import static org.urbancortex.presenter.Presenter.*;


public class Presentation extends Activity implements SensorEventListener {

    static long updateUITime;
    Button btn;
    DecimalFormat df = new DecimalFormat("#.#");
    SimpleDateFormat formatterDate = new SimpleDateFormat("dd/MM/yyyy");
    SimpleDateFormat formatterTime = new SimpleDateFormat("HH:mm:ss.SSS");
    private ImageView imageView;
    int lastIndex = 0;
    boolean mBound = false;
    csv_logger mService;
    TextView textViewDescription;
    TextView textViewTitle;
    private Vibrator v;
    boolean isPressed = true;
    private Button startBtn;
    private Button stopBtn;

    Sensor accelerometer;
    Sensor magnetometer;
    private String participantID;
    SensorManager mSensorManager;



    ServiceConnection mConnection = new ServiceConnection()
    {
        public void onServiceConnected(ComponentName paramAnonymousComponentName, IBinder paramAnonymousIBinder)
        {
            csv_logger.LocalBinder localLocalBinder = (csv_logger.LocalBinder)paramAnonymousIBinder;
            mService = localLocalBinder.getService();
            mBound = true;
            System.out.println("Service is connected");
        }

        public void onServiceDisconnected(ComponentName paramAnonymousComponentName)
        {
            mBound = false;
            System.out.println("Service is disconnected");
        }
    };
    private float azimut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_presentation);
        System.out.println("onCreate");

        // Get the message from the intent
        participantID = getIntent().getStringExtra(MainActivity.EXTRA_MESSAGE);
        bindService(new Intent(this, csv_logger.class), mConnection, 0);

        mSensorManager = (SensorManager)getSystemService(android.content.Context.SENSOR_SERVICE);




        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

//        SensorManager.getRotationMatrixFromVector(rotationV, rotationVector);
//        Sensor SensorOrientation = SensorManager.getOrientation(rotationV, orientationValuesV);
//        mSensorManager.getOrientation(rotationV, orientationValuesV);

        df = new DecimalFormat("#.00");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_presentation, menu);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected void onStart(){
        super.onStart();
        System.out.println("onStart "+Presenter.experimentEvents.length);
        setupUI();
    }

    protected void onPause()
    {
        System.out.println("onPause");
        super.onPause();

        mSensorManager.unregisterListener(this);
    }

    protected void onResume()
    {
        System.out.println("onResume");
        super.onResume();

        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);


        setupUI();
        try {
            updateUI();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop()
    {
        System.out.println("Presentation onStop");
        super.onStop();
        if (mBound)
        {
            unbindService(mConnection);
            mBound = false;
        }
    }

    @Override
    protected void onDestroy() {

        System.out.println("Presentation onDestroy");
        super.onDestroy();

        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    private long mLastClickTime = 0;

    private void renameButtons(boolean enable)
    {
        if(enable) {
            Button localButton = null;
            for (int i = 0; i < 3; i++) {
                int id = 1 + i;
                String buttonID = "present_button" + id;
                int layoutID = getResources().getIdentifier(buttonID, "id", getPackageName());
                localButton = (Button) findViewById(layoutID);

                if (!experimentEvents[index].Buttons[i].equals("NA") && !experimentEvents[index].Buttons[i].equals("") && !experimentEvents[index].Buttons[i].equals(" ") && localButton != null) {
                    localButton.setText(experimentEvents[index].Buttons[i]);
                    localButton.setEnabled(true);
                } else if (localButton != null) {
                    localButton.setText(" ");
                    localButton.setEnabled(false);
                }
            }

            alertActive = true;

        } else {
            for (int i = 0; i < 3; i++) {
               int id = 1 + i;
                String buttonID = "present_button" + id;
                int layoutID = getResources().getIdentifier(buttonID, "id", getPackageName());
                Button localButton = (Button) findViewById(layoutID);

                localButton.setText(" ");
                localButton.setEnabled(false);

            }

        }
    }

    private void updateUI()
            throws IOException, ParseException
    {
        System.out.println(Presenter.index);
        renameButtons(true);

        textViewTitle.setText(experimentEvents[index].Title);
        textViewDescription.setText(experimentEvents[index].Text);
        String str = imgDirectory.toString() + "/" + experimentEvents[index].Image.replaceAll("\\s+", "");
        System.out.println(str);
        Bitmap localBitmap = BitmapFactory.decodeFile(str);
        imageView.setImageBitmap(localBitmap);
        imageView.invalidate();

        if (Presenter.index != lastIndex)
        {
            updateUITime = SystemClock.elapsedRealtime();
            logEvent(experimentEvents[index].Title, "new_stimulus", experimentEvents[index].Code, elapsedRealtime());
            lastIndex = Presenter.index;
        }
        if (btn != null) {
            btn.setClickable(true);
        }



    }

    public void loadNextEvent(){
        System.out.println( "loadNextEvent " ) ;
        if (Presenter.index < (Presenter.experimentEvents.length - 1)) {
            Presenter.index++;

           try {
                updateUI();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }
    public boolean alertActive = false;

    public void loadPreviousEvent()
    {
        if (Presenter.index > 0) {
            Presenter.index--;
        }
    }

    public void setToNeutral()  {

        renameButtons(false);

        textViewTitle.setText("");
        textViewDescription.setText("");
        String str = imgDirectory.toString() + "/fixationcross.PNG";
        System.out.println(str);
        Bitmap localBitmap = BitmapFactory.decodeFile(str);

        imageView.setImageBitmap(localBitmap);
        imageView.invalidate();

        long d = (long) (2000.0D * Math.random());
        System.out.println("jitter duration "+d);


        new Timer().schedule(new TimerTask() {
            @Override

            public void run() {
                // this code will be executed after 2 seconds
                Presentation.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        loadNextEvent();
                    }
                });
            }


        }, d);

    }

    public boolean logEvent(String eventName, String eventResponse, String eventType, long eventEpoch)
            throws IOException, ParseException
    {
        System.out.println(eventResponse + " " + eventType);
        long l = eventEpoch - Presenter.startMillis + Presenter.startTime;
        String date = formatterDate.format(new Date(l));
        String time = formatterTime.format(new Date(l));
        String event = eventName + ", " + eventResponse.toString() + ", " + eventType + "," + l + ", " + date + ", " + time + ", " + locations.lat + ", " + locations.lon + ", " + locations.speed + ", " + locations.bearing + ", " + locations.elevation + ", " + locations.accuracy;
        boolean logged = false;
        if (mBound) {
            logged = mService.writeStringToFile(event);
        }
        System.out.println(logged);
        return logged;
    }

    public void onButtonClick(View view)
            throws IOException, ParseException, InterruptedException
    {

        btn = ((Button) view);

        v.vibrate(20L);
        String str = ((Button) view).getText().toString();
        System.out.println(str);

        boolean response = logEvent(experimentEvents[index].Title, str, "response", elapsedRealtime());
        System.out.println("logged " + response);

        if( isPressed ) {

            if (SystemClock.elapsedRealtime() - mLastClickTime < 500) {
                return;
            }

            if (SystemClock.elapsedRealtime() - updateUITime < 500) {
                return;
            }
            mLastClickTime = SystemClock.elapsedRealtime();
            btn.setClickable(false);

            if (response) {
                if (!str.equals("Back")) {
                    if (experimentEvents[index].Alert) {
                        AlertDialog alertDialog;
                        alertDialog = new AlertDialog.Builder(this)
                                .setTitle("New Instruction")
                                .setMessage("Are you ready to receive the next instruction?")
                                .setIcon(0)
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // if yes
                                        setToNeutral();

                                    }
                                })
                                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // if no

                                        btn.setClickable(true);
                                    }
                                })
                                .setCancelable(false)
                                .show();

                        alertDialog.setCanceledOnTouchOutside(false);


                    } else {
                        setToNeutral();
                    }

                } else if (str.equals("Back")) {
                    loadPreviousEvent();
                }
            } else {
                Toast.makeText(this, "Couldn't record this, press again.", Toast.LENGTH_SHORT).show();
            }
        }

    }



//    @Override
//    public boolean dispatchKeyEvent(KeyEvent event) {
//        int keyCode = event.getKeyCode();
//        int action = event.getAction();
//
//        LinearLayout mainLayout=(LinearLayout)this.findViewById(R.id.mainLayout);
//        LinearLayout backLayout=(LinearLayout)this.findViewById(R.id.backgroundLayout);
//
//        switch (keyCode) {
//            case KeyEvent.KEYCODE_VOLUME_UP:
//                if (action == KeyEvent.ACTION_DOWN && !isPressed && isWalking) {
//
//                    isPressed = true;
//                    startShowingMap  = elapsedRealtime();
//                    mainLayout.setVisibility(View.VISIBLE);
//                    backLayout.setVisibility(View.GONE);
//
//                } else if (action == KeyEvent.ACTION_UP && isWalking) {
//                    long mapReadingDuration = elapsedRealtime() - startShowingMap;
//                    startShowingMap=0;
//                    mainLayout.setVisibility(View.INVISIBLE);
//                    backLayout.setVisibility(View.VISIBLE);
//
//                    try {
//                        logEvent("hideMap", String.valueOf(mapReadingDuration), "null", elapsedRealtime());
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    } catch (ParseException e) {
//                        e.printStackTrace();
//                    }
//                    isPressed = false;
//
//                }
//                return true;
//            default:
//                return super.dispatchKeyEvent(event);
//        }
//
//    }


    protected void setupUI()
    {
        imageView = ((ImageView)findViewById(R.id.image));
        textViewTitle = ((TextView)findViewById(R.id.title));
        textViewDescription = ((TextView)findViewById(R.id.mainText));
        v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
    }

    long startWalking = 0;

    public void onTestingClick(View view){

        switch(view.getId())
        {
            case R.id.button1:
                // handle button A click;
                isWalking = true;
                startWalking = elapsedRealtime();
                updateButtonState();
                try {
                    logEvent("startRoute", "null", "null", elapsedRealtime());
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }


                break;
            case R.id.button2:
                // handle button B click;

                // Confirm this is not accidental
                new AlertDialog.Builder(this)
                        .setTitle("Arrived to Destination")
                        .setMessage("Press to confirm you have arrived at the destination")
                        .setIcon(0)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // continue with
                                isWalking = false;
                                updateButtonState();
                                long stopWalking = elapsedRealtime();
                                long walkingTime = stopWalking-startWalking;

                                try {
                                    logEvent("arrived", String.valueOf(walkingTime), "null", stopWalking);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }

                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .show();


                break;

            default:
                throw new RuntimeException("Unknown button ID");
        }

    }
    private void updateButtonState(){

        startBtn = (Button) findViewById(R.id.button1);
        stopBtn = (Button) findViewById(R.id.button2);

        if(!isWalking){
            startBtn.setVisibility(View.VISIBLE);
            stopBtn.setVisibility(View.INVISIBLE);
        } else {
            startBtn.setVisibility(View.INVISIBLE);
            stopBtn.setVisibility(View.VISIBLE);
        }

    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }


    float[] mGravity;
    float[] mGeomagnetic;

    @Override
    public void onSensorChanged(SensorEvent event) {

        /*
            Taken from Fernando Greenyway:
            http://www.codingforandroid.com/2011/01/using-orientation-sensors-simple.html
         */
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            mGravity = event.values;
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            mGeomagnetic = event.values;
        if (mGravity != null && mGeomagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                azimut = orientation[0]; // orientation contains: azimut, pitch and roll
                System.out.println("azimuth: "+azimut);
            }
        }
    }
}
