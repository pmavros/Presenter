package org.urbancortex.presenter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.os.SystemClock.elapsedRealtime;
import static org.urbancortex.presenter.Presenter.experimentEvents;
import static org.urbancortex.presenter.Presenter.imgDirectory;
import static org.urbancortex.presenter.Presenter.*;


public class WalkingToDestination extends Activity {

    private boolean isPressed = false;
    private boolean isDestinationDisplayed = false;
    private long startShowingDestination = 0;
    DecimalFormat df = new DecimalFormat("#.00");
    SimpleDateFormat formatterDate = new SimpleDateFormat("dd/MM/yyyy");
    SimpleDateFormat formatterTime = new SimpleDateFormat("HH:mm:ss.SSS");
    int lastIndex = 0;
    boolean mBound = false;
    csv_logger mService;

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
        setContentView(R.layout.activity_walking_to_destination);
        bindService(new Intent(this, csv_logger.class), mConnection, 0);

        setupUI();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_walking_to_destination, menu);
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

    @Override
    protected void onDestroy() {

        System.out.println("WalkingToDestination onDestroy");
        super.onDestroy();

        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        int action = event.getAction();

        System.out.println(keyCode);

        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (action == KeyEvent.ACTION_DOWN && !isPressed && isWalking) {

                    isDestinationDisplayed = true;
                    isPressed = true;
                    startShowingDestination  = elapsedRealtime();

                    try {
                        logEvent("showDestination", experimentEvents[index].Condition, experimentEvents[index].Code, experimentEvents[index].Title, "NA", elapsedRealtime());
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    showImage();

                } else if (action == KeyEvent.ACTION_UP && isDestinationDisplayed) {
                    long DestinationReadingDuration = elapsedRealtime() - startShowingDestination;
                    System.out.println(DestinationReadingDuration);
                    startShowingDestination = 0;

                    isPressed = false;
                    isDestinationDisplayed = false;

                    try {
                        logEvent("hideDestination", experimentEvents[index].Condition, experimentEvents[index].Code, experimentEvents[index].Title, String.valueOf(DestinationReadingDuration), elapsedRealtime());
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }


                    hideImage();
                }
                return true;
            default:
                return super.dispatchKeyEvent(event);
        }
    }

    private void setupUI(){

        ImageView imageView = (ImageView) findViewById(R.id.destination);
        String str = imgDirectory.toString() + "/" + experimentEvents[index].Image.replaceAll("\\s+", "");
        System.out.println(str);
        Bitmap localBitmap = BitmapFactory.decodeFile(str);
        imageView.setImageBitmap(localBitmap);
        imageView.setVisibility(View.INVISIBLE);
//        imageView.invalidate();


    }

    private void showImage() {
        System.out.println("show Image");

        ImageView image = (ImageView) findViewById(R.id.destination);
        image.setVisibility(View.VISIBLE);
        image.invalidate();

    }


    private void hideImage() {

        ImageView image = (ImageView) findViewById(R.id.destination);
        image.setVisibility(View.INVISIBLE);
        image.invalidate();
    }

    public void onTestingClick(View view){
        switch(view.getId())
        {
            case R.id.startWalking:
                // handle button A click;


                break;
            case R.id.arrived:
                // handle button B click;

                final Intent goToPresentation = new Intent(this, Presentation.class);
                goToPresentation.putExtra("methodName", "updateUI");

                final long walkToDestinationDuration = elapsedRealtime() - Presenter.startWalkingToDestination;

                // Confirm this is not accidental
                new AlertDialog.Builder(this)
                        .setTitle("Arrived to Destination")
                        .setMessage("Press to confirm you have arrived at the destination")
                        .setIcon(0)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // continue with

//                                recordEvents("arrived", null, elapsedRealtime());
                                try {
                                    logEvent("arrivedToDestination", experimentEvents[index].Condition, experimentEvents[index].Code, experimentEvents[index].Title, String.valueOf(walkToDestinationDuration), elapsedRealtime());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
//                                return to previous activity;
                                isWalking = false;
                                index++;
                                startActivity(goToPresentation);
                                finish();
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
//            responseTime = elapsedRealTime - showStimulus;
        }

        String eventLog = event +","+ eventCondition + ", " +  eventCode + ", " + eventText + ", " + eventResponse.toString() + ", " + responseTime + "," + monotonicEpoch   + ", " + date + ", " + time + ", " + locations.lat + ", " + locations.lon + ", " + locations.speed + ", " + locations.bearing + ", " + locations.elevation + ", " + locations.accuracy;
        boolean logged = false;
        if (mBound) {
            logged = mService.writeStringToFile(eventLog);
        }
        System.out.println(logged);
        return logged;
    }
}
