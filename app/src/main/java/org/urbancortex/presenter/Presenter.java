package org.urbancortex.presenter;

import android.app.Application;

import java.io.File;

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

    public Presenter() {}
}