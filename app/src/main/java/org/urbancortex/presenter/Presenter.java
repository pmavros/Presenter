package org.urbancortex.presenter;

import android.app.Application;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Panos on 02/06/2015.
 */


public class Presenter
        extends Application
{
    public static Event[] experimentEvents;
    protected static int index = 1;
    protected static boolean isRecording = false;
    public static long startMillis;
    public static long startTime;
    static File fileDirectory = null;
    static File fileWriteDirectory = null;
    static File imgDirectory;
    public static int timeOffset;
    public static String timeOffset_timestamp;
    public static String condition;
    public static boolean isWalking;
    public static String remoteIP;
    public static String timeOffset_Array;
    public static int code;
    public static ArrayList<String> EventSpinner;
    public static long startWalkingToDestination;
    public static String notes;
    public static ArrayList<FileSpinner> files;
    public static ArrayList<String> filesNames;
//    public static String EventsFile;

    public Presenter() {}
}