package org.urbancortex.presenter;

/**
 * Created by Panos on 02/06/2015.
 */

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.text.format.Time;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import static android.os.SystemClock.elapsedRealtime;
import static org.urbancortex.presenter.Presenter.isRecording;
import static org.urbancortex.presenter.Presenter.startMillis;
import static org.urbancortex.presenter.Presenter.startTime;
import static org.urbancortex.presenter.Presenter.timeOffset;
import static org.urbancortex.presenter.Presenter.timeOffset_timestamp;

public class csv_logger extends Service {

    static String participantID = null;
    static long time = System.currentTimeMillis();
    static Time now = new Time();
    static BufferedWriter buf;
    static String outputFileName;
    SimpleDateFormat formatterDate = new SimpleDateFormat("dd/MM/yyyy");
    SimpleDateFormat formatterTime = new SimpleDateFormat("HH:mm:ss.SSS");
    private String date;
    String eventInfo;
    private int mGPSInterval = 5000; // 5 seconds by default, can be changed later

    Timer timer;
    TimerTask timerTask;
    //we are going to use a handler to be able to run in our TimerTask
    protected final Handler handler;


    public csv_logger() {
//        super(csv_logger.class.getName())
        handler = new Handler();
    }

    @Override
    public void onCreate() {
        System.out.println( "Service onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        participantID = intent.getStringExtra("participantID");
        System.out.println("On start command "+participantID);

        try {
            createFile(participantID);
        } catch (IOException e) {
            e.printStackTrace();
        }

        startTimer();

        return START_NOT_STICKY;
    }

    @Override
    public boolean stopService(Intent name) {
        System.out.println("stop service");

        stopTimerTask();
        isRecording = false;
        super.stopService(name);

        return false;
    }


    public void startTimer() {
        //set a new Timer
        timer = new Timer();

        //initialize the TimerTask's job
        initializeTimerTask();

        //schedule the timer, after the first 5000ms the TimerTask will run every 10000ms
        timer.schedule(timerTask, 5000, mGPSInterval); //

        new Timer().schedule(new TimerTask() {
            @Override

            public void run() {
                // this code will be executed after 2 seconds
               TimeOffsetActivity.getTimeOffset();
            }


        }, 1000, 10000);
    }

    public void stopTimerTask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public void initializeTimerTask() {

        timerTask = new TimerTask() {
            public void run() {
                //use a handler to run a toast that shows the current timestamp
                handler.post(new Runnable() {
                    public void run() {

                        // Do something
                        if(isRecording){
                            updateLog();
                        }
                    }
                });
            }
        };
    }

    @Override
    public void onDestroy() {
        System.out.println("Service.onDestroy");
        super.onDestroy();
        try {
            closeFile();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        csv_logger getService() {
            // Return this instance of LocalService so clients can call public methods
            return csv_logger.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        System.out.println("onBind");
        return mBinder;
    }

    public void updateLog(){
        if(isRecording) {

            long millisElapsed = elapsedRealtime() - startMillis;

            time = startTime + millisElapsed;
            date = formatterDate.format(new Date(time));
            String currentTime = formatterTime.format(new Date(time));
//            String record = "event, eventResponse, eventDetails, date, time, epoch, lat, lon, speed, bearing, elevation, accuracy";

            eventInfo = "GPS" + ", " +
                    "timeOffset "+ timeOffset +", " +
                    "timeOffset_timestamp " + timeOffset_timestamp + ", "+
                    time + ", " +
                    date + ", " +
                    currentTime + ", " +
                    locations.lat + ", " +
                    locations.lon + ", " +
                    locations.speed + ", " +
                    locations.bearing + ", " +
                    locations.elevation + ", " +
                    locations.accuracy;

            try {
                writeStringToFile(eventInfo);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean writeStringToFile(final String eventInfo) throws IOException {
        System.out.println(eventInfo);
        buf.append(eventInfo);
        buf.append("\n");
        buf.flush();

        return true;

    }

    public static void closeFile () throws IOException {
        buf.flush();
        buf.close();
    }

    public static void createFile(String participantID) throws IOException {
        System.out.println(participantID);
        now.setToNow();
        outputFileName =  participantID +"_" + now.format("%d.%m.%Y_%H.%M.%S") + "_" + "presenter.csv";
        System.out.println(outputFileName);

        System.out.println("fileWriteDirectory" + Presenter.fileWriteDirectory);
        buf = new BufferedWriter(new FileWriter(new File(Presenter.fileWriteDirectory, outputFileName) ));

        String record = "event, eventResponse, eventDetails, date, time, epoch, lat, lon, speed, bearing, elevation, accuracy";

        buf.write(record);
        buf.append("\n");
    }

    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }
}