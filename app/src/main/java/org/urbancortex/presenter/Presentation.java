package org.urbancortex.presenter;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
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

import static org.urbancortex.presenter.Presenter.experimentEvents;
import static org.urbancortex.presenter.Presenter.imgDirectory;
import static org.urbancortex.presenter.Presenter.index;


public class Presentation extends Activity {

    private static boolean clickable = true;
    public static int counter = 0;
    public static int frame = 0;
    static long updateUITime;
    Button btn;
    DecimalFormat df = new DecimalFormat("#.#");
    SimpleDateFormat formatterDate = new SimpleDateFormat("dd/MM/yyyy");
    SimpleDateFormat formatterTime = new SimpleDateFormat("HH:mm:ss.SSS");
    private ImageView imageView;
    int lastIndex = 0;
    boolean mBound = false;
    private Handler mHandler;
    csv_logger mService;
    private int mTimeElapsedDisplayInterval = 1000;
    private String participantID;
    TextView textViewDescription;
    TextView textViewTitle;
    private Vibrator v;

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
        participantID = getIntent().getStringExtra(MainActivity.EXTRA_MESSAGE);
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

    protected void onPause()
    {
        System.out.println("onPause");
        super.onPause();
    }

    protected void onResume()
    {
        System.out.println("onResume");
        super.onResume();

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

    private void renameButtons()
    {
        Button localButton = null;
        for (int i = 0; i < 3; i++)
        {
            // System.out.println(i);
            //System.out.println("experiment events" + experimentEvents[index].Buttons[i]);
            int id = 1 + i;
            String buttonID = "present_button" + id ;
//            System.out.println(buttonID);
            int layoutID = getResources().getIdentifier(buttonID, "id",  getPackageName());
            localButton = (Button) findViewById(layoutID);

            if (!experimentEvents[index].Buttons[i].equals("NA") && localButton!=null){
                localButton.setText(experimentEvents[index].Buttons[i]);
                localButton.setEnabled(true);
            } else if (localButton!=null){
                localButton.setText(" ");
                localButton.setEnabled(false);
            }
        }
    }

    private void updateUI()
            throws IOException, ParseException
    {
        System.out.println(Presenter.index);

        long d = (long) (2000.0D * Math.random());
        System.out.println("jitter duration "+d);

        try {
            Thread.sleep(d);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        renameButtons();

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
            logEvent(experimentEvents[index].Title, "new_stimulus", experimentEvents[index].Code);
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
        }
    }


    public void loadPreviousEvent()
    {
        if (Presenter.index > 0) {
            Presenter.index--;
        }
    }

    public boolean logEvent(String eventName, String eventResponse, String eventType)
            throws IOException, ParseException
    {
        System.out.println(eventResponse + " " + eventType);
        long l = SystemClock.elapsedRealtime() - Presenter.startMillis + Presenter.startTime;
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
        btn = ((Button)view);
        btn.setClickable(false);
        v.vibrate(20L);
        String str = ((Button)view).getText().toString();
        System.out.println(str);

        boolean response = logEvent(experimentEvents[index].Title, str, "response");
        System.out.println("logged " + response);
        if (response) {
            if (!str.equals("Back")) {
                loadNextEvent();
                updateUI();
            }
            else if (str.equals("Back")) {
                loadPreviousEvent();
            }
        } else {
            Toast.makeText(this, "Couldn't record this, press again.", Toast.LENGTH_SHORT).show();
        }
    }



    protected void setupUI()
    {
        imageView = ((ImageView)findViewById(R.id.image));
        textViewTitle = ((TextView)findViewById(R.id.title));
        textViewDescription = ((TextView)findViewById(R.id.mainText));
        v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
    }
}
