package org.urbancortex.presenter;

/**
 * Created by Panos on 03/06/2015.
 */

import android.app.Activity;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import static android.os.SystemClock.elapsedRealtime;
import org.apache.commons.net.ntp.TimeInfo;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import static org.urbancortex.presenter.Presenter.*;

public class TimeOffsetActivity extends Activity {

    static TextView offsetAverageText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeoffset);


        // must give permission in manifest to access internet

        //        For use in ad-hoc, use static IP address.
        //        InetAddress.getByName("192.168.1.232");


        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        System.out.println(elapsedRealtime());

    }

    public void showTimeOffset(View view){

            EditText input = (EditText) findViewById(R.id.edit_message);
            if(input!=null && !input.equals("")){
                remoteIP = input.getText().toString();
                input.setHint(remoteIP);
            }



//        int average = getTimeOffset();
//        offsetText.setText(offset);
        offsetAverageText = (TextView) findViewById(R.id.offsetAverage);
        offsetAverageText.setText("Calculating...");
        NTPClient.getTimeOffset();

        offsetAverageText.setText("Offset average is "+Presenter.timeOffset);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
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
}
