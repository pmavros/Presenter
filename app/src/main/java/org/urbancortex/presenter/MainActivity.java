package org.urbancortex.presenter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;

import static android.os.SystemClock.elapsedRealtime;


public class MainActivity extends Activity {

    public static final String EXTRA_MESSAGE = "org.urbancortex.presenter.MESSAGE";
    protected static boolean exit = false;
    private File fileWriteDirectory;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // start GPS service
        new locations(this, locations.ProviderType.GPS).start();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
            getTimeOffset();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected void onStart()
    {
        if (exit) {
            finish();
        }
        super.onStart();
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

    public boolean checkPassword(){

        // get the present password
        EditText passwordText = (EditText) findViewById(R.id.edit_password);

        if(passwordText!=null && !passwordText.equals("")){
            // get the present password
            String password = passwordText.getText().toString();
            System.out.println(password);


            if (password.equals("4321")) {
                // clean the password field
                passwordText.setText("");
                return true;
            } else {

                Toast.makeText(MainActivity.this, "Enter correct research password first.", Toast.LENGTH_LONG).show();
                return false;
            }

        } else {
            Toast.makeText(MainActivity.this, "Enter correct research password first.", Toast.LENGTH_LONG).show();
            return false;
        }
    }

    public void getTimeOffset(){
        //Do something about it

        // start second activity
        Intent intentTimeOffset = new Intent(this, TimeOffsetActivity.class);
        startActivity(intentTimeOffset);

    }
}
