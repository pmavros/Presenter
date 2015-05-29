package org.urbancortex.presenter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Menu;

import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import java.io.File;

import static android.os.SystemClock.elapsedRealtime;

public class MainActivity
        extends Activity
{
    public static final String EXTRA_MESSAGE = "org.urbancortex.presenter.MESSAGE";
    protected static boolean exit = false;
    private File fileWriteDirectory;

    public MainActivity() {}

    /** Called when the user clicks the Send button */
    public void continueExperiment(View view) {

        EditText editText = (EditText) findViewById(R.id.edit_message);
        String participantID = editText.getText().toString();

        if(readWriteSettings.foldersReadyToReadWrite()){

            // prepare logger settings
            final Intent serviceIntent = new Intent(this, csv_logger.class);
//            serviceIntent.putExtra("fileDir", readWriteSettings.fileWriteDirectory.toString());
            serviceIntent.putExtra("participantID", participantID);

            if(!participantID.isEmpty()){

                // start second activity
                Intent intent = new Intent(this, Presentation.class);
                intent.putExtra(EXTRA_MESSAGE, participantID);
                startActivity(intent);

            } else {
                Toast.makeText(this, "Hey, did you forget to enter your participant ID?", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "Oops, I couldn't find the folders I need in the system!", Toast.LENGTH_LONG).show();
        }
    }

    /** Called when the user clicks the Send button */
    public void newExperiment(View view) {

        EditText editText = (EditText) findViewById(R.id.edit_message);
        final String participantID = editText.getText().toString();


        if(readWriteSettings.foldersReadyToReadWrite()){

            // prepare logger settings
            final Intent loggingIntent = new Intent(this, csv_logger.class);
            loggingIntent.putExtra("participantID", participantID);

            if(!participantID.isEmpty()){
                System.out.println(participantID);

                // start GPS logging
                if(Presenter.isRecording){
                    // check if the want to start new recording
                    final Intent activityIntent = new Intent(this, Presentation.class);
                    activityIntent.putExtra(EXTRA_MESSAGE, participantID);

                    new AlertDialog.Builder(this)
                            .setTitle("New Recording")
                            .setMessage("Are you sure you want to start a new recording session?")
                            .setIcon(0)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {

                                    // if yes
                                    startNewRecording(loggingIntent);

                                    // start second activity
                                    startActivity(activityIntent);
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // do nothing
                                }
                            })
                            .show();


                } else {
                    if (!Presenter.isRecording) {
                        // carry on with existing recording
                        startNewRecording(loggingIntent);

                        // start second activity
                        Intent activityIntent = new Intent(this, Presentation.class);
                        activityIntent.putExtra(EXTRA_MESSAGE, participantID);
                        startActivity(activityIntent);

                    }
                }

            } else {
                Toast.makeText(this, "Hey, did you forget to enter your participant ID?", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "Oops, I couldn't find the folders I need in the system!", Toast.LENGTH_LONG).show();
        }
    }

    private void startNewRecording(Intent intent){
        Presenter.isRecording = true;
        Presenter.index = 0;
        Presenter.startMillis = elapsedRealtime();
        Presenter.startTime = System.currentTimeMillis();
        startService(intent);
    }

    protected void onCreate(Bundle paramBundle)
    {
        super.onCreate(paramBundle);
        setContentView(R.layout.activity_main);
        new locations(this, locations.ProviderType.GPS).start();
    }

    public boolean onCreateOptionsMenu(Menu paramMenu)
    {
        getMenuInflater().inflate(R.menu.menu_main, paramMenu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem paramMenuItem)
    {

        return super.onOptionsItemSelected(paramMenuItem);
    }

    protected void onResume()
    {
        super.onResume();
        if (!Presenter.isRecording) {
            ((Button)findViewById(R.id.cont)).setVisibility(View.INVISIBLE);
        } else if (Presenter.isRecording) {
            ((Button)findViewById(R.id.cont)).setVisibility(View.VISIBLE);
        }

        if (readWriteSettings.foldersReadyToReadWrite()) {
            readWriteSettings.loadEventSettings();
        }

    }

    protected void onStart()
    {
        if (exit) {
            finish();
        }
        super.onStart();
    }
}
