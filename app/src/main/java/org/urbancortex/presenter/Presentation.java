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


public class Presentation extends Activity  {
//    implements SensorEventListener
    static long updateUITime;
    Button btn;
    DecimalFormat df = new DecimalFormat("#.00");
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
    long showStimulus;


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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_presentation);
        System.out.println("onCreate");

        // Get the message from the intent
        bindService(new Intent(this, csv_logger.class), mConnection, 0);

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

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if(intent.getStringExtra("methodName").equals("updateUI")){
            try {
                updateUI();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    protected void onPause()
    {
        System.out.println("onPause");
        super.onPause();

//        mSensorManager.unregisterListener(this);
        setupUI();
        try {
            updateUI();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    protected void onResume()
    {
        System.out.println("Presentation onResume");
        super.onResume();

//        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
//        mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);


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

            //  logEvent(String event, String eventCondition, String eventCode, String eventText, String eventResponse, long elapsedRealTime)
            logEvent("newStim", experimentEvents[index].Condition, experimentEvents[index].Code, experimentEvents[index].Title, "NA", elapsedRealtime());
            lastIndex = Presenter.index;
        }
        if (btn != null) {
            btn.setClickable(true);
        }

        condition = experimentEvents[index].Condition;
        showStimulus = elapsedRealtime();


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

        long d = (long) (1500+(1000.0D * Math.random()));
        System.out.println("jitter duration: "+d);


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

    public boolean logEvent(String event, String eventCondition, String eventCode, String eventText, String eventResponse, long elapsedRealTime)
            throws IOException, ParseException
    {
        long responseTime = 0;
        System.out.println(eventResponse + " " + eventCondition);

//        long responseTime = elapsedRealTime - showStimulus;
        long monotonicEpoch = elapsedRealTime - Presenter.startMillis + Presenter.startTime;
        String date = formatterDate.format(new Date(monotonicEpoch));
        String time = formatterTime.format(new Date(monotonicEpoch));

//          String record = "event, condition, code, eventText, eventResponse, eventDetails , date, time, epoch, lat, lon, speed, bearing, elevation, accuracy";

        if(event == "response"){
            responseTime = elapsedRealTime - showStimulus;
        }

        String eventLog = event +","+ eventCondition + ", " +  eventCode + ", " + eventText + ", " + eventResponse.toString() + ", " + responseTime + "," + monotonicEpoch   + ", " + date + ", " + time + ", " + locations.lat + ", " + locations.lon + ", " + locations.speed + ", " + locations.bearing + ", " + locations.elevation + ", " + locations.accuracy;
        boolean logged = false;
        if (mBound) {
            logged = mService.writeStringToFile(eventLog);
        }
        System.out.println(logged);
        return logged;
    }

    public void onButtonClick(View view)
            throws IOException, ParseException, InterruptedException
    {

        btn = ((Button) view);


        String buttonText = ((Button) view).getText().toString();
        System.out.println(buttonText);

        //          String record = "event, condition, code, eventText, eventResponse, eventDetails , date, time, epoch, lat, lon, speed, bearing, elevation, accuracy";
        //  logEvent(String event, String eventCondition, String eventCode, String eventText, String eventResponse, long elapsedRealTime)
//                           logEvent("newStim", experimentEvents[index].Condition, experimentEvents[index].Code, experimentEvents[index].Title, "NA", elapsedRealtime());

        boolean response = logEvent("response", experimentEvents[index].Condition, experimentEvents[index].Code, experimentEvents[index].Title, buttonText, elapsedRealtime());
        System.out.println("logged " + response);

        if( isPressed ) {

            if (SystemClock.elapsedRealtime() - mLastClickTime < 500) {
                return;
            }

            if (SystemClock.elapsedRealtime() - updateUITime < 500) {
                return;
            }

            final Intent intentToWalking = new Intent(this, WalkingToDestination.class);
            mLastClickTime = SystemClock.elapsedRealtime();
            btn.setClickable(false);
            
            v.vibrate(20L);
            if (response) {
                if(buttonText.equals("Start Walking")){
                    startWalkingToDestination = elapsedRealtime();
                    startActivity(intentToWalking);
                    isWalking = true;

                } else if (!buttonText.equals("Back")) {
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

                } else if (buttonText.equals("Back")) {
                    loadPreviousEvent();
                }
            } else {
                Toast.makeText(this, "Couldn't record this, press again.", Toast.LENGTH_SHORT).show();

            }
        }

    }

    protected void setupUI()
    {
        imageView = ((ImageView)findViewById(R.id.image));
        textViewTitle = ((TextView)findViewById(R.id.title));
        textViewDescription = ((TextView)findViewById(R.id.mainText));
        v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
    }

    long startWalking = 0;

//      @Override
//    public void onAccuracyChanged(Sensor sensor, int i) {
//
//    }
//
//
//    float[] mGravity;
//    float[] mGeomagnetic;
//
//    @Override
//    public void onSensorChanged(SensorEvent event) {
//
//        /*
//            Taken from Fernando Greenyway:
//            http://www.codingforandroid.com/2011/01/using-orientation-sensors-simple.html
//         */
//        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
//            mGravity = event.values;
//        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
//            mGeomagnetic = event.values;
//        if (mGravity != null && mGeomagnetic != null) {
//            float R[] = new float[9];
//            float I[] = new float[9];
//            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
//            if (success) {
//                float orientation[] = new float[3];
//                SensorManager.getOrientation(R, orientation);
//                azimut = orientation[0]; // orientation contains: azimut, pitch and roll
//                System.out.println("azimuth: "+azimut);
//            }
//        }
//    }
}
