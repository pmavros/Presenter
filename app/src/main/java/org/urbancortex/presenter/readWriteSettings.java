package org.urbancortex.presenter;

/**
 * Created by Panos on 02/06/2015.
 */

import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import static java.lang.System.out;
import static org.urbancortex.presenter.Presenter.fileDirectory;
import static org.urbancortex.presenter.Presenter.fileWriteDirectory;
import static org.urbancortex.presenter.Presenter.imgDirectory;

public class readWriteSettings
{

    private static int numberOfEvents;
    static File[] arrayOfFiles;

    public readWriteSettings() {}

    public static boolean foldersReadyToReadWrite() {

        if (isExternalStorageWritable()) {
            out.println("external storage is fine");

//            File filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
//            out.println("external storage is fine"+filePath);

            // sets the files in the directory
            fileDirectory = new File(Environment.getExternalStorageDirectory(), "/Presenter-io");
            imgDirectory = new File(Environment.getExternalStorageDirectory(), "/Presenter-io/images");
            Presenter.fileWriteDirectory = new File(Environment.getExternalStorageDirectory(), "/Presenter-io/data");
            System.out.println(Presenter.fileWriteDirectory);


            // check if directory exists
            if (fileDirectory.exists()) {
                // do something here
                out.println("external storage "+fileDirectory);
                out.println("folder presenter exists in sd storage");

                // check if data folder exists
                if (!fileWriteDirectory.isDirectory()) {
                    // do something here
                    fileWriteDirectory.mkdirs();
                }

                if (!imgDirectory.exists()) {
                    // TODO
                    // do something here
//                    Toast.makeText(this, "Hey, did you forget to enter your participant ID?", Toast.LENGTH_LONG).show();
                }

            } else {
                System.out.println("no presenter-io folder");
            }
            return true;

        } else {
            out.println("external not writable");
            return false;
        }
    }



    private static String getStringFromFile(File filename) {
        out.println(filename);

        //Get the text file
        File localFile = new File(String.valueOf(filename));
        StringBuilder localStringBuilder = new StringBuilder();
        BufferedReader localBufferedReader;

        try {
            localBufferedReader = new BufferedReader(new FileReader(localFile));

            String line;

            while ((line = localBufferedReader.readLine()) != null) {
                localStringBuilder.append(line);
                localStringBuilder.append('\n');
            }
            localBufferedReader.close();
        }
        catch (IOException e) {
            //You'll need to add proper error handling here
        }

        out.println(localStringBuilder);
        //Make sure you close all streams.

        return localStringBuilder.toString();
    }



    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {

        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {

            System.out.println("external storage is writable");
            return true;
        }
        return false;
    }

    public static void loadFilesList() {
        System.out.println("loadFilesList");

        Presenter.files = new ArrayList<FileSpinner>();
        Presenter.filesNames = new ArrayList<String>();
        int compatibleFiles = 0;

        if (foldersReadyToReadWrite()) {
           arrayOfFiles = fileDirectory.listFiles();


            if (arrayOfFiles.length != 0) {

                for (int i = 0; i < arrayOfFiles.length; i++) {

                    System.out.println(arrayOfFiles[i].getName());

                    if (arrayOfFiles[i].getName().toString().toLowerCase().contains("presenter_events")) {

                        System.out.println(arrayOfFiles[i].getName());
                        compatibleFiles++;

                        Presenter.files.add(new FileSpinner(arrayOfFiles[i].getName().toString(), arrayOfFiles[i].getAbsoluteFile()));
                        Presenter.filesNames.add(arrayOfFiles[i].getName().toString());
                    }
                }
            }
        }
    }

    public static boolean loadEventSettings(int i) {
        System.out.println("load event settings");

        if (foldersReadyToReadWrite())
        {
            Presenter.EventSpinner = new ArrayList<String>();

            System.out.println("file is " + Presenter.files.get(i).fileName);
            String data = getStringFromFile(Presenter.files.get(i).fileAddress);
            String[] arrayOfEvents = makeArray(data);

            Presenter.experimentEvents = new Event[numberOfEvents];
            Presenter.EventSpinner = new ArrayList<String>();

            for (int j = 0; j < numberOfEvents; j++)
            {

                String[] arrayOfColumns = arrayOfEvents[j].split(",");
                System.out.println("arrayOfColumns.length "+ arrayOfColumns.length);

                if (arrayOfColumns.length == 9)
                {
                    Event[] arrayOfEvent = Presenter.experimentEvents;

//                                new Event(condition, code, title, text, buttons[3], img, alert);
                    arrayOfEvent[j] = new Event(arrayOfColumns[0], // condition
                            arrayOfColumns[1], // code
                            arrayOfColumns[2], // title
                            arrayOfColumns[3], // text
                            new String[]{arrayOfColumns[5],arrayOfColumns[6],arrayOfColumns[7]}, // buttons
                            arrayOfColumns[4], // img
                            arrayOfColumns[8].equals("yes")); // confirm before go to next event
                }

                Presenter.EventSpinner.add(arrayOfColumns[0]);

            }
            Presenter.EventSpinner.remove(0);
            return true;
        }
        return false;
    }

    protected static String[] makeArray(String paramString)
    {
        String[] arrayOfString1 = paramString.split("\n");
        System.out.println(arrayOfString1);

        // skip the first it is the header of the file
        numberOfEvents = arrayOfString1.length;
        System.out.println("there are "+numberOfEvents+ " events");

        return arrayOfString1;
    }
}
